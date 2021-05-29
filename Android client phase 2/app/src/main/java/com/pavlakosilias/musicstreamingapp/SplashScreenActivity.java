package com.pavlakosilias.musicstreamingapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.transition.Explode;
import android.view.Window;

/**
 * The type Splash screen activity.
 */
public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Start the timer
        new CountDownTimer(2000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {}

            @Override
            public void onFinish() {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                finish();
            }
        }.start();

    }
}
