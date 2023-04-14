/*
Application Name: Smart EduBox
Created Date: Sept. 15, 2022
Company: CreatorBox Solutions
Developer: DGMJr.
*/

package com.creatorbox.esense;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * ONLINE MODE
 * An activity that handles the user's account information in which they can update it.
 */
public class OnlineChat_Status extends AppCompatActivity {

    private TextInputLayout mNewStatus, mNewDisplayName;
    private Button save;
    private SwitchCompat loginPrefSwitch;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor sharedPreferencesEditor;

    private boolean switch_status;
    private static final String SWITCH_STATUS = "switch_status";

    private final String firebaseDBURL = "https://smart-edubox-90c5d-default-rtdb.asia-southeast1.firebasedatabase.app/";
    private DatabaseReference mUserDatabase;
    private FirebaseUser mCurrentUser;

    private ProgressDialog mProgress;
    private ActionBar ab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.online_chat_status);

        init();
    }

    /**
     * Initialize all components.
     */
    private void init() {
        mNewStatus = (TextInputLayout) findViewById(R.id.textStatusInputLayout);
        mNewDisplayName = (TextInputLayout) findViewById(R.id.textNameInputLayout);
        save = (Button) findViewById(R.id.saveChangesBtn);
        loginPrefSwitch = (SwitchCompat) findViewById(R.id.enableBiometricSwitch);

        sharedPreferences= getSharedPreferences("user_fingerprint_preference", MODE_PRIVATE);
        sharedPreferencesEditor = getSharedPreferences("user_fingerprint_preference",MODE_PRIVATE).edit();
        switch_status = sharedPreferences.getBoolean(SWITCH_STATUS, false);

        ab = getSupportActionBar();
        ab.setTitle("Account Settings");
        ab.setSubtitle("Update Account Settings");

        getUserData();

        loginPrefSwitch.setChecked(switch_status);

        save.setOnClickListener(v -> {
            mProgress = new ProgressDialog(OnlineChat_Status.this);
            mProgress.setTitle("Saving Changes");
            mProgress.setMessage("Please wait while we save the changes.");
            mProgress.show();

            String status = mNewStatus.getEditText().getText().toString();
            String name = mNewDisplayName.getEditText().getText().toString();

            mUserDatabase.child("status").setValue(status);
            mUserDatabase.child("status").setValue(status).addOnCompleteListener(task -> {
               if (task.isSuccessful()) {
                   mProgress.dismiss();
                   goToAccountSettings();
               }
               else {
                   Toast.makeText(getApplicationContext(), "Error saving status.", Toast.LENGTH_LONG).show();
               }
            });

            mUserDatabase.child("name").setValue(name);
            mUserDatabase.child("name").setValue(name).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    mProgress.dismiss();
                    goToAccountSettings();
                }
                else {
                    Toast.makeText(getApplicationContext(), "Error saving name.", Toast.LENGTH_LONG).show();
                }
            });
        });

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
    }

    /**
     * Retrieve user data from Firebase Realtime Database.
     */
    private void getUserData() {
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String current_uid = mCurrentUser.getUid();
        mUserDatabase = FirebaseDatabase.getInstance(firebaseDBURL).getReference().child("Users").child(current_uid);
        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String statusFromFB = snapshot.child("status").getValue().toString();
                String nameFromFB = snapshot.child("name").getValue().toString();

                mNewStatus.getEditText().setText(statusFromFB);
                mNewDisplayName.getEditText().setText(nameFromFB);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    /**
     * Display a toast to the user.
     * @param message the toast message.
     */
    private void displayToast(String message) {
        Toast.makeText(OnlineChat_Status.this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    /**
     * Go to Users Account Profile Page.
     */
    private void goToAccountSettings() {
        Intent goToAccSettings = new Intent(this, AccountSettings.class);
        startActivity(goToAccSettings);
        finish();
    }
}