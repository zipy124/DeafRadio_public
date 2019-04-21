package com.example.zmorgan.deafradio;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.media.MediaPlayer;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Renderer;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.audio.AudioCapabilities;
import com.google.android.exoplayer2.audio.AudioFocusManager;
import com.google.android.exoplayer2.audio.AudioProcessor;
import com.google.android.exoplayer2.audio.AudioRendererEventListener;
import com.google.android.exoplayer2.decoder.DecoderCounters;
import com.google.android.exoplayer2.mediacodec.MediaCodecSelector;
import com.google.android.exoplayer2.metadata.Metadata;
import com.google.android.exoplayer2.metadata.MetadataOutput;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.text.Cue;
import com.google.android.exoplayer2.text.Subtitle;
import com.google.android.exoplayer2.text.TextOutput;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.video.VideoRendererEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.ConsoleHandler;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

public class MainActivity extends AppCompatActivity
        implements CategoriesFragment.OnFragmentInteractionListener,
                   SearchFragment.OnFragmentInteractionListener,
                   SettingsFragment.OnFragmentInteractionListener,
                   PlaybackFragment.OnFragmentInteractionListener,
                   SubtitleFragment.OnFragmentInteractionListener {
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private MediaPlayer player;
    private Boolean playing = false;
    private Button playPauseButton;
    private ProgressBar playSeekBar;
    private SubtitleFragment sFragment;
    private TextView textView;
    private List<Radio> lstRadio;
    private Boolean sentData = false;
    private String savedTranscript;
    public String activeFragment = "Search";


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            //When an item is selected on the bottom navigation this function is called, it loads the appropriate fragment
            switch (item.getItemId()) {
                case R.id.navigation_stations:
                    loadFragment(SearchFragment.newInstance(lstRadio,exoPlayerController),R.id.frame_container);
                    activeFragment = "Search";
                    return true;
                case R.id.navigation_categories:
                    loadFragment(CategoriesFragment.newInstance(lstRadio,exoPlayerController),R.id.frame_container);
                    activeFragment = "Categories";
                    return true;
                case R.id.navigation_settings:
                    loadFragment(new SettingsFragment(),R.id.frame_container);
                    activeFragment = "Settings";
                    return true;
            }
            return false;
        }
    };

    private BufferListener bl
            = new BufferListener() {

        @Override
        public void finished(byte[] data, int sampleRate) {
            //This is our BufferListener class which has a finished function. It is called when our buffer is full of 15 seconds of audio
            String transcript;
            if(sentData == false) {
                //uncomment below line to limit the API to one call
                //sentData = true;
                //We create a seperate thread here to run the audio recognition in, so the app continues to play while waiting
                class OneShotTask implements Runnable {
                    byte[] data;
                    int sampleRate;
                    OneShotTask(byte[] d,int s) { data = d;
                    sampleRate = s;}
                    public void run() {
                        Log.i("DeafRadioTranscript", "Sent Data to Cloud");
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        boolean multiple_speakers = prefs.getBoolean("DetectSpeakers",false);
                        String transcript = "Music is playing <br>";
                        Log.i("DeafRadioTranscript", String.valueOf(multiple_speakers));
                        Radio was_playing = ExoPlayerController.getInstance().getStation_playing();
                        ExoPlayerController.getInstance().getSpeechConfig().setSampleRateHertz(sampleRate);
                        try {
                            transcript = StreamingAudioRecog.streamingMicRecognize(data,multiple_speakers,getApplicationContext());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        Log.i("DeafRadioTranscript", transcript);
                        getSupportFragmentManager().popBackStack();
                        Radio playing = ExoPlayerController.getInstance().getStation_playing();
                        if(playing == was_playing) {
                            //if we haven't switched stations since add and show the transcript
                            transcript = ExoPlayerController.getInstance().add(transcript);
                            savedTranscript = transcript;
                        }
                        if(ExoPlayerController.getInstance().getPlaying() && activeFragment.equals("Subtitles")) {
                            //only re-load the subtitle fragment screen if we are on it and playing
                            loadFragment(SubtitleFragment.newInstance(transcript, ""),R.id.frame_container);
                            activeFragment = "Subtitles";
                        }
                    }
                }
                Thread t = new Thread(new OneShotTask(data.clone(),sampleRate));
                t.start();
            }
            else {
                transcript = "Manual API Rate Limiting";
                // do something with transcript
                Log.i("DeafRadioURTranscript",transcript);
            }


        }

    };

    public String getSavedTranscript(){

        return exoPlayerController.get_transcript();

    }

    public void deleteSavedTranscript() {

        savedTranscript = "";

    }

    public ExoPlayer exoPlayer;
    private Player.EventListener mediaListener;
    private DefaultBandwidthMeter BANDWIDTH_METER;
    private RecyclerStationViewAdapter myAdapter;
    private RecyclerView myrv;
    private ExoPlayerController exoPlayerController;
    public ImageButton button;
    public CheckBox dontShowAgain;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = (MenuInflater) getMenuInflater();
        inflater.inflate(R.menu.top_menus, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        //When you select an item in the top menu this function is called
        //For testing purposes you can ucomment the below line to inject local audio files into the app
        //activeFragment = "Subtitles";
        //ExoPlayerController.getInstance().playLocalFile(R.raw.news,getApplicationContext());

        if(item.getTitle().equals("add_station")) {
            //opens a dialog to add your own radio station
            LayoutInflater factory = LayoutInflater.from(this);
            final View deleteDialogView = factory.inflate(R.layout.dialog_new_station, null);
            final AlertDialog deleteDialog = new AlertDialog.Builder(this).create();
            deleteDialog.setView(deleteDialogView);
            deleteDialogView.findViewById(R.id.button_add).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //your business logic
                    EditText stationName = (EditText) deleteDialogView.findViewById(R.id.station_name);
                    EditText stationUrl = (EditText) deleteDialogView.findViewById(R.id.station_url);
                    lstRadio.add(new Radio(stationName.getText().toString(), "User added", "Description", R.drawable.ic_favorite_black_24dp, stationUrl.getText().toString()));
                    switch (activeFragment) {
                        case "Search":
                            loadFragment(SearchFragment.newInstance(lstRadio, exoPlayerController), R.id.frame_container);
                            break;
                        case "Categories":
                            loadFragment(CategoriesFragment.newInstance(lstRadio, exoPlayerController), R.id.frame_container);
                            break;
                        case "Settings":
                            loadFragment(new SettingsFragment(), R.id.frame_container);
                            break;
                    }
                    deleteDialog.dismiss();
                }
            });
            deleteDialogView.findViewById(R.id.button_cancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteDialog.dismiss();
                }
            });

            deleteDialog.show();
        }
        else if(item.getTitle().equals("go_back")){
            //When going back from subtitle view to another fragment
            getSupportFragmentManager().popBackStack();
            findViewById(R.id.playback_control).setVisibility(View.VISIBLE);
            findViewById(R.id.toolbar_subtitles).setVisibility(View.INVISIBLE);
            findViewById(R.id.toolbar_back).setVisibility(View.VISIBLE);
            findViewById(R.id.toolbar).setVisibility(View.INVISIBLE);
            setActionBar((Toolbar)findViewById(R.id.toolbar_back));
        }
        else if(item.getTitle().equals("stop_playback")){
            //When stopping playback in the subtitle screen
            getSupportFragmentManager().popBackStack();
            deleteSavedTranscript();
            ExoPlayerController.getInstance().stop();
        }
        else if(item.getTitle().equals("go_forward")){
            //When returning to the subtitle screen
            loadFragment(SubtitleFragment.newInstance(getSavedTranscript(), ""),R.id.frame_container);
        }

        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Create and inflate all our toolbar variations
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_subtitles);
        toolbar.inflateMenu(R.menu.top_menus_subtitles);
        toolbar = (Toolbar) findViewById(R.id.toolbar_back);
        toolbar.setTitle("DeafRadio");
        toolbar.inflateMenu(R.menu.top_menus_back);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("DeafRadio");
        toolbar.inflateMenu(R.menu.top_menus);
        findViewById(R.id.toolbar_subtitles).setVisibility(View.INVISIBLE);
        findViewById(R.id.toolbar_back).setVisibility(View.INVISIBLE);
        setActionBar(toolbar);

        //Set up our list of radio stations
        lstRadio = new ArrayList<>();
        lstRadio.add(new Radio("Radio 1", "Music", "Description", R.drawable.radio1,"http://bbcmedia.ic.llnwd.net/stream/bbcmedia_radio1_mf_p"));
        lstRadio.add(new Radio("Radio 2", "Music", "Description", R.drawable.radio2,"http://bbcmedia.ic.llnwd.net/stream/bbcmedia_radio2_mf_p"));
        lstRadio.add(new Radio("Radio 4", "Talking", "Description", R.drawable.radio4,"http://bbcmedia.ic.llnwd.net/stream/bbcmedia_radio4fm_mf_p"));
        lstRadio.add(new Radio("Smooth Radio", "Music", "Description", R.drawable.smoothradio,"http://media-ice.musicradio.com:80/SmoothUKMP3"));
        lstRadio.add(new Radio("Radio 5 Live", "Talking", "Description", R.drawable.radio5,"http://sc52.lon.llnw.net:80/stream/bbcmedia_radio5live_mf_p"));
        lstRadio.add(new Radio("iHeart Radio", "Music", "Description", R.drawable.iheartradio,"http://c10icyelb.prod.playlists.ihrhls.com/4342_icy"));
        lstRadio.add(new Radio("Capital Fm", "Music", "Description", R.drawable.capitalfm,"http://media-ice.musicradio.com:80/CapitalUKMP3"));
        //setup the bottom navigation
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        //Set up our ExoPlayer setup with our own renderer taking our buffer listener.
        RenderersFactory rendererFactory = new DefaultRenderersFactory(getApplicationContext());
        ComponentListener cl = new ComponentListener();
        Renderer[] renderers = new Renderer[1];
        renderers[0] = new MyMediaCodecAudioRenderer(bl,
                getApplicationContext(),
                MediaCodecSelector.DEFAULT,
                null,
                /* playClearSamplesWithoutKeys= */ false,
                new Handler(Util.getLooper()),
                cl,
                AudioCapabilities.getCapabilities(getApplicationContext()),
                new AudioProcessor[0]);
        ExoPlayerController.makeInstance(ExoPlayerFactory.newInstance(renderers,new DefaultTrackSelector()),PlaybackFragment.newInstance());
        exoPlayerController = ExoPlayerController.getInstance();
        //load the main screen to start
        loadFragment(SearchFragment.newInstance(lstRadio,exoPlayerController),R.id.frame_container);

        //Show dialog telling you how to use the app, This has a checkbox if you don't want to see it again
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        LayoutInflater adbInflater = LayoutInflater.from(this);
        View eulaLayout = adbInflater.inflate(R.layout.checkbox, null);
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = settings.edit();
        //Uncomment the lines below to re-display the Start up message
        //editor.putString("skipMessage", "NOT checked");
        //editor.commit();
        String skipMessage = settings.getString("skipMessage", "NOT checked");

        dontShowAgain = (CheckBox) eulaLayout.findViewById(R.id.skip);
        adb.setView(eulaLayout);
        adb.setTitle("How to use this App");
        adb.setMessage(Html.fromHtml("To start listening to a station click on it. A screen will open and after a short delay subtitles will slowly appear. " +
                "You can go back and continue generating subtitles in the background by clicking the back button at the top, or stop playback with the stop button. " +
                "By pressing the  plus button at the top, your own radio stations can be added, which requires a title and a link to an mp3 stream. It's also possible " +
                "to customize how the subtitles display by going to the settings menu. Warning: We make no guarantees about the accuracy of subtitles and " +
                "this should be kept in mind when using the application."));

        adb.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String checkBoxResult = "NOT checked";

                if (dontShowAgain.isChecked()) {
                    checkBoxResult = "checked";
                }

                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = settings.edit();

                editor.putString("skipMessage", checkBoxResult);
                editor.commit();

                return;
            }
        });

        adb.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // Cancel button by default does nothing
                return;
            }
        });
        Log.i("skipMessage",skipMessage);
        if (!skipMessage.equals("checked")) {
            adb.show();
        }
        //Load the playback control fragment
        loadFragment(exoPlayerController.getPlaybackFragment(),R.id.playback_control);
        super.onResume();
    }


    public void loadFragment(Fragment fragment,int frame_id) {
        //This function is called to load fragments
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(frame_id, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onFragmentInteraction(Uri uri){
        //you can leave it empty
    }
    protected final class ComponentListener
            implements VideoRendererEventListener,
            AudioRendererEventListener,
            TextOutput,
            MetadataOutput,
            SurfaceHolder.Callback,
            TextureView.SurfaceTextureListener,
            AudioFocusManager.PlayerControl{

        @Override
        public void surfaceCreated(SurfaceHolder holder) {

        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

        }

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }

        @Override
        public void setVolumeMultiplier(float volumeMultiplier) {

        }

        @Override
        public void executePlayerCommand(int playerCommand) {

        }

        @Override
        public void onAudioEnabled(DecoderCounters counters) {

        }

        @Override
        public void onAudioSessionId(int audioSessionId) {

        }

        @Override
        public void onAudioDecoderInitialized(String decoderName, long initializedTimestampMs, long initializationDurationMs) {

        }

        @Override
        public void onAudioInputFormatChanged(Format format) {

        }

        @Override
        public void onAudioSinkUnderrun(int bufferSize, long bufferSizeMs, long elapsedSinceLastFeedMs) {

        }

        @Override
        public void onAudioDisabled(DecoderCounters counters) {

        }

        @Override
        public void onMetadata(Metadata metadata) {

        }

        @Override
        public void onCues(List<Cue> cues) {

        }

        @Override
        public void onVideoEnabled(DecoderCounters counters) {

        }

        @Override
        public void onVideoDecoderInitialized(String decoderName, long initializedTimestampMs, long initializationDurationMs) {

        }

        @Override
        public void onVideoInputFormatChanged(Format format) {

        }

        @Override
        public void onDroppedFrames(int count, long elapsedMs) {

        }

        @Override
        public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {

        }

        @Override
        public void onRenderedFirstFrame(@Nullable Surface surface) {

        }

        @Override
        public void onVideoDisabled(DecoderCounters counters) {

        }
    }

}

