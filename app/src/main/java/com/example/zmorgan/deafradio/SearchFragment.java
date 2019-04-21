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
import android.widget.SearchView;
import android.widget.Toolbar;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;

import java.util.List;


public class SearchFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private OnFragmentInteractionListener mListener;

    private List<Radio> lstRadio;
    private ExoPlayerController exoPlayerController;
    private RecyclerStationViewAdapter recyclerAdapter;
    private RecyclerViewClickListener onPlayPauseClickedListener = new RecyclerViewClickListener() {
        @Override
        public void onClick (View view, int Position){
            //When you click a search result play it and load the subtitle fragment
            Radio r = (Radio) recyclerAdapter.getDataSource().get(Position);
            String url = r.getURL();
            if (!exoPlayerController.getPlaying() || url != exoPlayerController.getStation_playing().getURL()) {
                ((MainActivity)getActivity()).loadFragment(SubtitleFragment.newInstance("", ""),R.id.frame_container);
                ((MainActivity)getActivity()).activeFragment = "Subtitles";
                exoPlayerController.play(r,view.getContext());
            } else {
                exoPlayerController.stop();
                ((MainActivity)getActivity()).findViewById(R.id.toolbar_subtitles).setVisibility(View.INVISIBLE);
                ((MainActivity)getActivity()).findViewById(R.id.toolbar_back).setVisibility(View.INVISIBLE);
                ((MainActivity)getActivity()).findViewById(R.id.toolbar).setVisibility(View.VISIBLE);
                ((MainActivity)getActivity()).setActionBar((Toolbar) ((MainActivity)getActivity()).findViewById(R.id.toolbar));
            }
        }
    };

    private SearchView.OnQueryTextListener searchListener = new SearchView.OnQueryTextListener() {

        @Override
        public boolean onQueryTextSubmit(String query) {
            return false;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            //When typing display the search results live
            recyclerAdapter.search(newText);
            return false;
        }
    };
    private Context context;
    private boolean playing = false;

    public SearchFragment() {
        // Required empty public constructor
    }

    public static SearchFragment newInstance(List<Radio> radioList, ExoPlayerController exoPlayerController) {
        SearchFragment fragment = new SearchFragment();
        fragment.lstRadio = radioList;
        fragment.exoPlayerController = exoPlayerController;
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
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        ((MainActivity)getActivity()).findViewById(R.id.toolbar).setVisibility(View.VISIBLE);
        ((MainActivity)getActivity()).findViewById(R.id.toolbar_subtitles).setVisibility(View.INVISIBLE);
        ((MainActivity)getActivity()).setActionBar((Toolbar) ((MainActivity)getActivity()).findViewById(R.id.toolbar));
        // Inflate the layout for this fragment
        SearchView simpleSearchView = (SearchView) view.findViewById(R.id.search_stations); // inititate a search view
        RecyclerView recyleView = (RecyclerView) view.findViewById(R.id.searchRecycle);
        // perform set on query text listener event
        simpleSearchView.setOnQueryTextListener(this.searchListener);
        this.recyclerAdapter = new RecyclerStationViewAdapter(this.context,this.lstRadio,onPlayPauseClickedListener,false, true);
        recyleView.setAdapter(recyclerAdapter);
        recyleView.setLayoutManager(new GridLayoutManager(context,2));
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
