package com.example.zmorgan.deafradio;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;

import java.util.List;

public class PlaybackFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private OnFragmentInteractionListener mListener;
    private Context context;
    private Button play_pause_button;
    private Button stop_button;
    private ImageView radio_thumb;
    private TextView radio_title;

    public PlaybackFragment() {
        // Required empty public constructor
    }

    public static PlaybackFragment newInstance() {
        PlaybackFragment fragment = new PlaybackFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_playback, container, false);
        // Inflate the layout for this fragment
        play_pause_button = (Button) view.findViewById(R.id.playback_play_button);
        play_pause_button.setOnClickListener(new Button.OnClickListener(){

            @Override
            public void onClick(View v) {
                //When the play button is hit pause or play music depending on the state
                ExoPlayerController epc = ExoPlayerController.getInstance();
                Boolean playing = epc.getPlaying();
                if(playing){
                    epc.pause();
                    play_pause_button.setBackgroundResource(R.drawable.exo_controls_play);
                }
                else{
                    epc.resume(getContext());
                    play_pause_button.setBackgroundResource(R.drawable.exo_controls_pause);
                }
            }
        });
        stop_button = (Button) view.findViewById(R.id.playback_stop_button);
        stop_button.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                //If the stop button is hit hide the fragment and stop playback.
                ExoPlayerController epc = ExoPlayerController.getInstance();
                epc.stop();
                ((MainActivity)getActivity()).findViewById(R.id.playback_control).setVisibility(View.GONE);
                ((MainActivity)getActivity()).findViewById(R.id.toolbar_subtitles).setVisibility(View.INVISIBLE);
                ((MainActivity)getActivity()).findViewById(R.id.toolbar_back).setVisibility(View.INVISIBLE);
                ((MainActivity)getActivity()).findViewById(R.id.toolbar).setVisibility(View.VISIBLE);
            }
        });
        radio_thumb = (ImageView) view.findViewById(R.id.playback_image);
        radio_title = (TextView) view.findViewById(R.id.playback_station_name);
        radio_title.setText("Test");
        return view;
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    public void setPlaying(Radio r) {
        //Sets the now playing title and thumbnail
        this.radio_thumb.setImageResource(r.getThumbnail());
        this.radio_title.setText(r.getTitle());
        this.play_pause_button.setBackgroundResource(R.drawable.exo_controls_pause);
    }

    public void setPause() {
        this.play_pause_button.setBackgroundResource(R.drawable.exo_controls_play);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
