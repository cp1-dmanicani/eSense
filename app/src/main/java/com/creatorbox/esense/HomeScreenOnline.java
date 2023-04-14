/*
Application Name: Smart EduBox
Created Date: Sept. 15, 2022
Company: CreatorBox Solutions
Developer: DGMJr.
*/

package com.creatorbox.esense;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * ONLINE MODE
 * An activity that handles the Home Screen of the ONLINE MODE.
 */
public class HomeScreenOnline extends AppCompatActivity {

    private Button exit, files, chat, send, settings;
    private TextView loggedInUser;
    private CircleImageView profileDP;
    private ProgressDialog progressDialog;

    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private final String firebaseDBURL = "https://smart-edubox-90c5d-default-rtdb.asia-southeast1.firebasedatabase.app/";
    private DatabaseReference mUserDatabase;
    private FirebaseUser mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_screen_online);

        progressDialog = new ProgressDialog(HomeScreenOnline.this);
        loadHomeScreen();
    }

    /**
     * Show a progress dialog to load user data.
     */
    private void loadHomeScreen() {
        init();
        progressDialog.setTitle("LOADING HOME SCREEN");
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            progressDialog.dismiss();
        }, 1000);
    }

    /**
     * Initialize all components.
     */
    private void init() {
        profileDP = (CircleImageView) findViewById(R.id.home_profile_image);
        loggedInUser = (TextView) findViewById(R.id.userNameTextOnline);
        files = (Button) findViewById(R.id.btnFilesOnline);
        exit = (Button) findViewById(R.id.exitBtnOnline);
        chat = (Button) findViewById(R.id.btnChatOnline);
        send = (Button) findViewById(R.id.btnFileTransferOnline);
        settings = findViewById(R.id.btnSettingsOnline);

        exit.setOnClickListener(v -> logoutUser());
        files.setOnClickListener(v -> FileManagerForm());
        chat.setOnClickListener(v -> ChatAppForm());
        send.setOnClickListener(v -> goToSendForm());
        settings.setOnClickListener(v -> goToSettingsForm());


        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String current_uid = mCurrentUser.getUid();
        mUserDatabase = FirebaseDatabase.getInstance(firebaseDBURL).getReference().child("Users").child(current_uid);
        mUserDatabase.keepSynced(true);

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();
                String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();
                loggedInUser.setText("Hi, " + name);

                if (!thumb_image.equals("default")) {
                    //Picasso.get().load(thumb_image).placeholder(R.drawable.ic_person).into(profileDP);
                    Picasso.get().load(thumb_image).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.ic_person).into(profileDP, new Callback() {
                        @Override
                        public void onSuccess() {}
                        @Override
                        public void onError(Exception e) {
                            Picasso.get().load(thumb_image).placeholder(R.drawable.ic_person).into(profileDP);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseErrorError) {}
        });

        profileDP.setOnClickListener(v -> goToUserProfile());
    }

    /**
     * Redirect to user's Account Settings.
     */
    public void goToUserProfile() {
        Intent intent = new Intent(this, AccountSettings.class);
        startActivity(intent);
        onStartNewActivityAnimations();
    }

    @Override
    public void onStart() {
        super.onStart();
        checkUserStatus();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUserDatabase.child("online").setValue(false);
    }

    /**
     * This is called when user taps on the LOGOUT button in the home screen.
     */
    public void logoutUser() {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("LOGOUT")
                        .setMessage("Logout user?")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        LogOutUser();

                                        SharedPreferences sharedPreferencesA = getSharedPreferences("rememberMeCBOnline", MODE_PRIVATE);
                                        SharedPreferences.Editor editorA = sharedPreferencesA.edit();
                                        editorA.putString("rememberMeOnline", "false");
                                        editorA.apply();

                                        SharedPreferences sharedPreferencesB = getSharedPreferences("user_fingerprint_preference", MODE_PRIVATE);
                                        SharedPreferences.Editor editorB = sharedPreferencesB.edit();
                                        editorB.putBoolean("biometrics_login", false);
                                        editorB.apply();

                                        finish();
                                    }
                                }).setNegativeButton("Cancel", (dialog, which) ->
                        dialog.dismiss()).create();
        alertDialog.show();
    }

    /**
     * Log out Firebase User from the app.
     */
    private void LogOutUser() {
        firebaseAuth.signOut();
        Intent intentSignOut = new Intent(HomeScreenOnline.this, OnlineLogin.class);
        startActivity(intentSignOut);
        mUserDatabase.child("online").setValue(false);
        onStartNewActivityAnimations();
    }

    /**
     * Go to File Manager activity.
     */
    public void FileManagerForm() {
        Intent goToFiles = new Intent(this, FileManager.class);
        startActivity(goToFiles);
        onStartNewActivityAnimations();
    }

    /**
     * Go to Online Chat activity.
     */
    public void ChatAppForm() {
        Intent goToChat = new Intent(this, OnlineChatUsers.class);
        startActivity(goToChat);
        onStartNewActivityAnimations();
    }

    /**
     * Go to Online File Transfer activity.
     */
    public void goToSendForm() {
        Intent goToSend = new Intent(this, OnlineSend_Users.class);
        startActivity(goToSend);
        onStartNewActivityAnimations();
    }

    /**
     * Go to Settings Activity.
     */
    public void goToSettingsForm() {
        Intent goToSettings = new Intent(this, SettingsOnline.class);
        startActivity(goToSettings);
        onStartNewActivityAnimations();
    }

    //Animations when going in and out of activities.
    protected void onStartNewActivityAnimations() {
        overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
    }

    /**
     * Check user's online presence.
     */
    private void checkUserStatus() {
        if (mCurrentUser!=null) {
            mUserDatabase.child("online").setValue(true);
        } else {
            LogOutUser();
        }
    }
}
