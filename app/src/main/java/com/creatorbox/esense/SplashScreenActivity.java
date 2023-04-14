/*
Application Name: Smart EduBox
Created Date: Sept. 15, 2022
Company: CreatorBox Solutions
Developer: DGMJr.
*/

package com.creatorbox.esense;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

/**
 * The activity that handles the splash screen.
 */
public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

        new Handler().postDelayed(() -> {
            startActivity(new Intent(SplashScreenActivity.this,ModeScreen.class));
            onStartNewActivityAnimations();
            finish();
        }, 3000);
    }

    //Handles animation when going from activity to activity.
    protected void onStartNewActivityAnimations() {
        overridePendingTransition(R.anim.fade_in, R.anim.slide_out);
    }
}