/*
Application Name: Smart EduBox
Created Date: Sept. 15, 2022
Company: CreatorBox Solutions
Developer: DGMJr.
*/

package com.creatorbox.esense;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

/**
 * OFFLINE MODE
 * An activity that handles the settings for OFFLINE MODE.
 */
public class SettingsOffline extends AppCompatActivity {

    private Button about, connectivity, accountSettings;
    private ImageView home;
    private Context context;
    private FragmentTransaction ft;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private int connectionPos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_offline);

        init();
    }

    private void init() {
        context = this;
        home = findViewById(R.id.homeButtonOffline);
        about = findViewById(R.id.aboutButtonOffline);
        connectivity = findViewById(R.id.connectivitySettingsBtn);
        accountSettings = findViewById(R.id.accountSettingsBtnOffline);

        sharedPreferences= getSharedPreferences("user_connectivity_preference_offline", Context.MODE_PRIVATE);
        editor = getSharedPreferences("user_connectivity_preference_offline",Context.MODE_PRIVATE).edit();

        home.setOnClickListener(v -> {
            Intent intentHome = new Intent(context, HomeScreenOffline.class);
            startActivity(intentHome);
            finish();
        });

        about.setOnClickListener(v -> viewAboutUsFrag());
        accountSettings.setOnClickListener(v -> viewAccountSettingsFrag());
        connectivity.setOnClickListener(v -> chooseConnectivityPref());
        getUserConnectionPref();
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
        ft.replace(R.id.settings_offline, fragment, "first");
        ft.addToBackStack(null);
        ft.commit();

        hideButtons();
    }

    /**
     * This function inflates the Offline Account Settings fragment.
     */
    private void viewAccountSettingsFrag() {
        ft = getSupportFragmentManager().beginTransaction();
        OfflineAccountSettings fragment = new OfflineAccountSettings();
        ft.setCustomAnimations(
                R.anim.fade_in,
                R.anim.slide_out
        );
        ft.replace(R.id.settings_offline, fragment, "first");
        ft.addToBackStack(null);
        ft.commit();

        hideButtons();
    }

    /**
     * This function hides the buttons when inflating a fragment.
     */
    private void hideButtons() {
        home.setVisibility(View.INVISIBLE);
        about.setVisibility(View.INVISIBLE);
        connectivity.setVisibility(View.INVISIBLE);
        accountSettings.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onBackPressed() {
        Fragment f = getSupportFragmentManager().findFragmentByTag("first");
        if (f!=null) {
            ft.setCustomAnimations(
                    R.anim.fade_in,
                    R.anim.slide_out
            );
            ft.detach(f);
            home.setVisibility(View.VISIBLE);
            about.setVisibility(View.VISIBLE);
            connectivity.setVisibility(View.VISIBLE);
            accountSettings.setVisibility(View.VISIBLE);
            super.onBackPressed();
        }
        else {
            Intent intent = new Intent(this, HomeScreenOffline.class);
            startActivity(intent);
            this.finish();
        }
    }

    /**
     * Show an alert dialog containing choices for the user's connection preferences.
     */
    private void chooseConnectivityPref() {
        final String[] connectionPrefs = {"Bluetooth" , "Wireless Sensor Network"};
        String userConnectionPref = sharedPreferences.getString("connection_preference", "Wireless Sensor Network");

        if (userConnectionPref.equals("Bluetooth")) {
            connectionPos = 0;
        } else connectionPos = 1;

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(SettingsOffline.this);
        alertDialog.setTitle("Choose Connection Preferences");
        alertDialog.setSingleChoiceItems(connectionPrefs, connectionPos, (dialog, which) ->
                editor.putString("connection_preference", connectionPrefs[which]));
        alertDialog.setPositiveButton("Save", (dialog, which) -> {
            editor.apply();
            displayToast("Preferences saved.");
        });
        alertDialog.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        alertDialog.setCancelable(false);
        alertDialog.show();
    }

    /**
     * Get user connection preference value saved in the SharedPreferences.
     */
    private void getUserConnectionPref() {
        String userConnectionPref = sharedPreferences.getString("connection_preference", "Wireless Sensor Network");
        displayToast("Using " + userConnectionPref + " connection.");
    }

    private void displayToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}