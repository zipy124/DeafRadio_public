package com.example.zmorgan.deafradio;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class Splash extends AppCompatActivity {

    @Override

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Start home activity

        startActivity(new Intent(Splash.this, MainActivity.class));

        // close splash activity when main activity has loaded

        finish();

    }

}
