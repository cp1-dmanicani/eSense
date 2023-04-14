/*
Application Name: Smart EduBox
Created Date: Sept. 15, 2022
Company: CreatorBox Solutions
Developer: DGMJr.
*/

package com.creatorbox.esense;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

/**
 * ONLINE MODE
 * An activity that handles the settings for ONLINE MODE.
 */
public class SettingsOnline extends AppCompatActivity {

    private Button about, accSettings;
    private ImageButton home;
    private Context context;
    private FragmentTransaction ft;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_online);

        init();
    }

    private void init() {
        context = this;
        home = findViewById(R.id.homeButtonOnline);
        about = findViewById(R.id.aboutButton);
        accSettings = findViewById(R.id.accountSettings);

        home.setOnClickListener(v -> {
            Intent intentHome = new Intent(context, HomeScreenOnline.class);
            startActivity(intentHome);
            finish();
        });

        about.setOnClickListener(v -> {
            viewAboutUsFrag();
        });

        accSettings.setOnClickListener(v -> {
            Intent intentAccSettings = new Intent(context, AccountSettings.class);
            startActivity(intentAccSettings);
        });
    }

    /**
     * This function inflates the AboutUs fragment.
     */
    private void viewAboutUsFrag() {
        ft = getSupportFragmentManager().beginTransaction();
        AboutUsFragment fragment = new AboutUsFragment();
        ft.setCustomAnimations(
                R.anim.fade_in,
                R.anim.slide_out
        );
        ft.replace(R.id.settings_online, fragment, "first");
        ft.addToBackStack(null);
        ft.commit();

        home.setVisibility(View.INVISIBLE);
        about.setVisibility(View.INVISIBLE);
        accSettings.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Fragment f = getSupportFragmentManager().findFragmentByTag("first");
        if (f!=null) {
            ft.setCustomAnimations(
                    R.anim.fade_in,
                    R.anim.slide_out
            );
            ft.detach(f);
        }
        home.setVisibility(View.VISIBLE);
        about.setVisibility(View.VISIBLE);
        accSettings.setVisibility(View.VISIBLE);
    }
}