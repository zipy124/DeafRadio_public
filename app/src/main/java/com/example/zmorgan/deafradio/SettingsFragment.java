package com.example.zmorgan.deafradio;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toolbar;

import java.util.List;
import java.util.Objects;

public class SettingsFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public SettingsFragment() {
        // Required empty public constructor
    }
    Spinner spinner;
    String s;
    SwitchCompat switch1, switch2, switch3, switch4;
    boolean stateSwitch1, stateSwitch2, stateSwitch3, stateSwitch4;
    SharedPreferences preferences;

    public static SettingsFragment newInstance(String param1, String param2) {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
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
        View v =  inflater.inflate(R.layout.fragment_settings, container, false);
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        ((MainActivity)getActivity()).findViewById(R.id.toolbar).setVisibility(View.VISIBLE);
        ((MainActivity)getActivity()).findViewById(R.id.toolbar_subtitles).setVisibility(View.INVISIBLE);
        ((MainActivity)getActivity()).setActionBar((Toolbar) ((MainActivity)getActivity()).findViewById(R.id.toolbar));
        switch1 = (SwitchCompat) v.findViewById(R.id.switch1);

        spinner = (Spinner) v.findViewById(R.id.spinner);
        ArrayAdapter<String> myAdapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.names));
        myAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(myAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //Settings for first speakers colour
                String[] colours = getResources().getStringArray(R.array.names);
                if (position != 0){
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("FirstColour", colours[position]);
                    editor.apply();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        final EditText font_size = v.findViewById(R.id.font_size);


        spinner = (Spinner) v.findViewById(R.id.spinner2);
        ArrayAdapter<String> myAdapter3 = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.names2));
        myAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(myAdapter3);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //settings for second speakers colour
                String[] colours = getResources().getStringArray(R.array.names2);
                if (position != 0){
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("SecondColour", colours[position]);
                    editor.apply();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinner = (Spinner) v.findViewById(R.id.spinner3);
        ArrayAdapter<String> myAdapter4 = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.names3));
        myAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(myAdapter4);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //settings for background colour
                String[] colours = getResources().getStringArray(R.array.names3);
                if (position != 0){
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("BackgroundColour", colours[position]);
                    editor.apply();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        s = spinner.getSelectedItem().toString();

        stateSwitch1 = preferences.getBoolean("DetectSpeakers", false);

        switch1.setChecked(stateSwitch1);
        String text_size = preferences.getString("TextSize","12");
        font_size.setText(text_size);

        font_size.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus){
                    //If user has finished editing text size save the size
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("TextSize", font_size.getText().toString());
                    editor.apply();
                }
            }
        });

        switch1.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                //If we toggle the switch toggle the preferences
                stateSwitch1 = !stateSwitch1;
                switch1.setChecked(stateSwitch1);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("DetectSpeakers", stateSwitch1);
                editor.apply();
            }
        });

        return v;

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
