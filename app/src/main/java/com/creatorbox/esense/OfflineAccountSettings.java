package com.creatorbox.esense;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

public class OfflineAccountSettings extends Fragment {

    private SwitchCompat loginPrefSwitch;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor sharedPreferencesEditor;

    private boolean switch_status;
    private static final String SWITCH_STATUS = "switch_status_offline";

    public OfflineAccountSettings() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_offline_account_settings, container, false);
        loginPrefSwitch = (SwitchCompat) view.findViewById(R.id.enableBiometricSwitchOffline);

        sharedPreferences= getContext().getSharedPreferences("user_fingerprint_preference_offline", Context.MODE_PRIVATE);
        sharedPreferencesEditor = getContext().getSharedPreferences("user_fingerprint_preference_offline",Context.MODE_PRIVATE).edit();
        switch_status = sharedPreferences.getBoolean(SWITCH_STATUS, false);

        loginPrefSwitch.setChecked(switch_status);
        loginPrefSwitch.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (compoundButton.isChecked()) {
                sharedPreferencesEditor.putBoolean(SWITCH_STATUS, true);
                sharedPreferencesEditor.apply();
                sharedPreferencesEditor.commit();
                loginPrefSwitch.setChecked(true);
                displayToast("Fingerprint login enabled.");
            } else {
                sharedPreferencesEditor.putBoolean(SWITCH_STATUS, false);
                sharedPreferencesEditor.apply();
                sharedPreferencesEditor.commit();
                loginPrefSwitch.setChecked(false);
                displayToast("Fingerprint login disabled.");
            }
        });
        return view;
    }

    private void displayToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}