package com.example.zmorgan.deafradio;

import android.content.Context;
import android.net.Uri;
import android.view.View;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.extractor.mp3.Mp3Extractor;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.RawResourceDataSource;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.services.speech.v1p1beta1.Speech;
import com.google.api.services.speech.v1p1beta1.SpeechRequestInitializer;
import com.google.api.services.speech.v1p1beta1.model.RecognitionConfig;
import com.google.api.services.speech.v1p1beta1.model.SpeakerDiarizationConfig;

import java.nio.channels.AsynchronousFileChannel;

public class ExoPlayerController {
    private boolean playing = false;
    private boolean paused = false;
    private ExoPlayer exoPlayer;
    private String url;
    private PlaybackFragment playbackFragment;
    private Radio station_playing;
    private static ExoPlayerController instance = null;
    private String transcript = "";
    private Speech speechService;
    private RecognitionConfig recognitionConfig;

    private ExoPlayerController(ExoPlayer player,PlaybackFragment frag){
        //private constructor, takes an ExoPlayer instance and an instance of the Playback fragment
        this.exoPlayer = player;
        this.playbackFragment = frag;
        //Nothing is playing at the start
        playing= false;
        url = "";
    }

    public static void makeInstance(ExoPlayer player, PlaybackFragment frag){
        //This is the Public constructor and takes the same items as te private constructor
        instance = new ExoPlayerController(player,frag);

        //API key for google cloud
        final String CLOUD_API_KEY = "PUT_YOUR_API_KEY_HERE";
        //initialize the speech service
        instance.speechService = new Speech.Builder(
                AndroidHttp.newCompatibleTransport(),
                new AndroidJsonFactory(),
                null
        ).setSpeechRequestInitializer(
                new SpeechRequestInitializer(CLOUD_API_KEY))
                .build();
        SpeakerDiarizationConfig speakerDiarizationConfig = new SpeakerDiarizationConfig().setEnableSpeakerDiarization(true);
        instance.recognitionConfig = new RecognitionConfig();
        instance.recognitionConfig.setLanguageCode("en-GB");
        instance.recognitionConfig.setEncoding("LINEAR16");
        instance.recognitionConfig.setSampleRateHertz(48000);
        instance.recognitionConfig.setDiarizationConfig(speakerDiarizationConfig.setEnableSpeakerDiarization(true));
        instance.recognitionConfig.setEnableSpeakerDiarization(true);
    }
    //singleton style method
    public static ExoPlayerController getInstance(){
        return instance;
    }

    public void play(Radio r, Context context){
        //This function is called when a station is to be played, it updates the state and tells the playback fragment
        this.station_playing = r;
        String source = r.getURL();
        DataSource.Factory dataSourceFactory =
                new DefaultDataSourceFactory(context, System.getProperty("http.agent"));
        MediaSource mediaSource =
                new ExtractorMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(Uri.parse(source));
        this.exoPlayer.prepare(mediaSource);
        this.exoPlayer.setPlayWhenReady(true);
        this.playing = true;
        this.paused = false;
        this.url = source;
        this.playbackFragment.setPlaying(r);
    }

    public void resume(Context context){
        //this function resumes the audio and stores the state
        this.play(this.station_playing,context);
        this.playbackFragment.setPlaying(this.station_playing);
    }

    public void pause(){
        //this function pauses the audio and stores the state
        this.playing = false;
        this.paused = true;
        this.exoPlayer.setPlayWhenReady(false);
        this.playbackFragment.setPause();
    }

    public void stop(){
        //this function stops the audio and resets the state.
        this.playing = false;
        this.paused = false;
        this.url = "";
        this.station_playing = null;
        this.exoPlayer.setPlayWhenReady(false);

    }

    public boolean getPlaying(){
        return this.playing;
    }

    public boolean getPaused() { return this.paused;}

    public Radio getStation_playing(){ return this.station_playing;}

    public String add(String transcript) {
        //This functions adds a transcript to the end of the stored transcript
        this.transcript += transcript;
        return this.transcript;
    }

    public String get_transcript(){
        return this.transcript;
    }

    public void clear_transcript(){
        this.transcript = "";
    }

    public Speech getSpeech() {
        return this.speechService;
    }

    public PlaybackFragment getPlaybackFragment() { return this.playbackFragment;}

    public RecognitionConfig getSpeechConfig() {
        return this.recognitionConfig;
    }

    public void playLocalFile(int interview, Context applicationContext) {
        this.station_playing = new Radio("Test File","Interview","Description",R.drawable.radio1,"http://bbcmedia.ic.llnwd.net/stream/bbcmedia_radio1_mf_p");
        Uri uri = RawResourceDataSource.buildRawResourceUri(interview);
        final RawResourceDataSource dataSource = new RawResourceDataSource(applicationContext);
        try {
            dataSource.open(new DataSpec(uri));
        } catch (RawResourceDataSource.RawResourceDataSourceException e) {
            e.printStackTrace();
        }

        MediaSource source = new ExtractorMediaSource(uri, new DataSource.Factory() {
            @Override
            public DataSource createDataSource() {
                return dataSource;
            }
        }, Mp3Extractor.FACTORY, null, null);

        this.exoPlayer.prepare(source);
        this.exoPlayer.setPlayWhenReady(true);
        this.playing = true;
        this.paused = false;
        this.url = station_playing.getURL();
        this.playbackFragment.setPlaying(this.station_playing);
    }
}
