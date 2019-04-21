package com.example.zmorgan.deafradio;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.Spanned;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toolbar;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Renderer;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;

public class SubtitleFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private ImageButton button;
    private WebView webView;
    public static String txt;
    public ExoPlayer exoPlayer;
    private ImageButton button2;

    private OnFragmentInteractionListener mListener;

    public SubtitleFragment() {
        // Required empty public constructor
    }

    public static SubtitleFragment newInstance(String param1, String param2) {
        SubtitleFragment fragment = new SubtitleFragment();
        Bundle args = new Bundle();
        txt = param1;
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
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
        // Inflate the layout for this fragment

        final View  view = inflater.inflate(R.layout.fragment_subtitle, container, false);

        webView = (WebView) view.findViewById(R.id.subtitle_text);

        ((MainActivity)getActivity()).findViewById(R.id.playback_control).setVisibility(View.GONE);
        Toolbar toolbar = (Toolbar) ((MainActivity)getActivity()).findViewById(R.id.toolbar_subtitles);
        ((MainActivity)getActivity()).findViewById(R.id.toolbar).setVisibility(View.INVISIBLE);
        ((MainActivity)getActivity()).findViewById(R.id.toolbar_back).setVisibility(View.INVISIBLE);
        toolbar.setVisibility(View.VISIBLE);
        ((MainActivity)getActivity()).setActionBar(toolbar);
        if (txt.equals("")){
            txt = "Setting up the service and waiting for text to be generated! This can take 20-30 Seconds.";
        }
        final WebSettings webSettings = webView.getSettings();
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        //Set up webView according to the user preferences
        String text_size = preferences.getString("TextSize", "12");
        webSettings.setDefaultFontSize(Integer.parseInt(text_size));
        webView.setBackgroundColor(Color.parseColor(preferences.getString("BackgroundColour","white").toLowerCase()));
        webView.loadData(Base64.encodeToString(txt.getBytes(), Base64.NO_PADDING),"text/html", "base64");
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                //use the param "view", and call getContentHeight in scrollTo
                view.scrollTo(0, 1000000000);
            }
        });

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
