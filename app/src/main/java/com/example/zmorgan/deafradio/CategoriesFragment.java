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
import android.widget.Toolbar;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoriesFragment extends Fragment {

    private RecyclerView myrv;
    private RecyclerStationViewAdapter myAdapter;


    private OnFragmentInteractionListener mListener;
    private Context context;
    private List<Radio> lstRadio;
    private List<String> categories = new ArrayList<String>();
    private Map<String, RecyclerStationViewAdapter> categoryAdapters = new HashMap<String,RecyclerStationViewAdapter>();
    private ExoPlayerController exoPlayerController;
    private RecyclerStationViewAdapter current;

    private RecyclerViewClickListener onCategoryClickedListener = new RecyclerViewClickListener() {
        @Override
        public void onClick (View view, int position){
            //When clicking a category set the view adapter to show the stations in that category
            current = categoryAdapters.get(myAdapter.getCategory(position));
            myrv.setAdapter(categoryAdapters.get(myAdapter.getCategory(position)));
        }
    };
    private RecyclerViewClickListener onPlayPauseClickedListener;
    {
        onPlayPauseClickedListener = new RecyclerViewClickListener() {
            @Override
            public void onClick(View view, int Position) {
                //When clicking a station this function will run, it starts the subtitles fragment up and toggles the play state of the music
                Radio r = (Radio) current.getDataCategorySource().get(Position);
                String url = r.getURL();

                if (!exoPlayerController.getPlaying() || (url != exoPlayerController.getStation_playing().getURL())) {
                    exoPlayerController.play(r,view.getContext());
                    ((MainActivity) getActivity()).loadFragment(SubtitleFragment.newInstance("", ""),R.id.frame_container);
                    ((MainActivity)getActivity()).activeFragment = "Subtitles";

                } else {
                    exoPlayerController.stop();
                    ((MainActivity)getActivity()).findViewById(R.id.toolbar_subtitles).setVisibility(View.INVISIBLE);
                    ((MainActivity)getActivity()).findViewById(R.id.toolbar_back).setVisibility(View.INVISIBLE);
                    ((MainActivity)getActivity()).findViewById(R.id.toolbar).setVisibility(View.VISIBLE);
                    ((MainActivity)getActivity()).setActionBar((Toolbar) ((MainActivity)getActivity()).findViewById(R.id.toolbar));
                }
            }
        };
    }

    public CategoriesFragment() {
        // Required empty public constructor
    }

    public static CategoriesFragment newInstance(List<Radio> radio_list, ExoPlayerController exoPlayerController) {
        CategoriesFragment fragment = new CategoriesFragment();
        fragment.lstRadio = radio_list;
        fragment.exoPlayerController = exoPlayerController;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_stations, container, false);
        ((MainActivity)getActivity()).findViewById(R.id.toolbar).setVisibility(View.VISIBLE);
        ((MainActivity)getActivity()).findViewById(R.id.toolbar_subtitles).setVisibility(View.INVISIBLE);
        ((MainActivity)getActivity()).setActionBar((Toolbar) ((MainActivity)getActivity()).findViewById(R.id.toolbar));
        myrv = (RecyclerView) view.findViewById(R.id.recyclerview_id);
        myAdapter = new RecyclerStationViewAdapter(context, lstRadio, onCategoryClickedListener,true,false);
        // create views for each category containing the stations in that category
        for(Radio r : lstRadio){
            if (!categories.contains(r.getCategory())){
                categories.add(r.getCategory());
                categoryAdapters.put(r.getCategory(),new RecyclerStationViewAdapter(context,lstRadio,onPlayPauseClickedListener,true, false).setCategory(r.getCategory()));
            }
        }
        myrv.setLayoutManager(new GridLayoutManager( context, 2));
        myrv.setAdapter(myAdapter);

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
