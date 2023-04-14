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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * OFFLINE MODE
 * An activity that handles the Home Screen of the OFFLINE MODE.
 */
public class HomeScreenOffline extends AppCompatActivity {

    Button exit, settings, send, chat, receive, files;
    TextView loggedInUser;
    CircleImageView profilePic;
    private SharedPreferences sharedPreferences;
    private String userConnectionPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_screen_offline);

        sharedPreferences = getSharedPreferences("user_connectivity_preference_offline", Context.MODE_PRIVATE);
        userConnectionPref = sharedPreferences.getString("connection_preference", "Wireless Sensor Network");
        init();
    }

    /**
     * Initialize all components.
     */
    private void init() {
        exit = (Button) findViewById(R.id.exitBtnOffline);
        settings = (Button) findViewById(R.id.btnSettingsOffline);
        send = (Button) findViewById(R.id.btnSendOffline);
        receive = (Button) findViewById(R.id.btnReceiveOffline);
        chat = (Button) findViewById(R.id.btnChatOffline);
        files = (Button) findViewById(R.id.btnFilesOffline);
        loggedInUser = (TextView) findViewById(R.id.userNameText);

        exit.setOnClickListener(v -> QuitApp());
        settings.setOnClickListener(v -> SettingsOfflineForm());
        files.setOnClickListener(v -> FileManagerForm());

        send.setOnClickListener(v -> {
            if (userConnectionPref.equals("Bluetooth")) {
                SendFormBluetooth();
            }
            else displayToast("WSN Send is WIP.");
        });

        receive.setOnClickListener(v -> {
            if (userConnectionPref.equals("Bluetooth")) {
                ReceiveFormBluetooth();
            }
            else displayToast("WSN Receive is WIP.");
        });

        chat.setOnClickListener(v -> {
            if (userConnectionPref.equals("Bluetooth")) {
                ChatFormBluetooth();
            }
            else WSN_Chat();
        });

        Bundle extras = getIntent().getExtras();
        String email = null;
        if (extras != null) {
            email = extras.getString("email");
            loggedInUser.setText("Hello, "+ email);
        }

        sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);
        String userEmail = sharedPreferences.getString("email", "");
        loggedInUser.setText(userEmail);
        profilePic = findViewById(R.id.profileImageView);
        Picasso.get().load(R.drawable.esense_logo_4).into(profilePic);
    }

    private void displayToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * This is called when user taps on the LOGOUT button in the home screen.
     */
    public void QuitApp() {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("LOGOUT")
                .setMessage("Logout user?")
                .setPositiveButton("OK", (dialog, which) -> {
                    LogOut();

                    SharedPreferences sharedPreferences = getSharedPreferences("rememberMeCB", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("remember", "false");
                    editor.apply();
                    finish();
                }).setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss()).create();
        alertDialog.show();
    }

    //Log out of the app.
    public void LogOut() {
        Intent intentSignOut = new Intent(HomeScreenOffline.this, OfflineLogin.class);
        startActivity(intentSignOut);
        onStartNewActivityAnimations();
    }

    /**
     * Go to Offline Send Activity.
     */
    public void SendFormBluetooth() {
        Intent goToBTReceive = new Intent(this, BluetoothSend.class);
        startActivity(goToBTReceive);
        onStartNewActivityAnimations();
    }

    /**
     * Go to Offline Receive Activity.
     */
    public void ReceiveFormBluetooth() {
        Intent goToBTSend = new Intent(this, BluetoothReceive.class);
        startActivity(goToBTSend);
        onStartNewActivityAnimations();
    }

    /**
     * Go to Offline Chat Activity.
     */
    public void ChatFormBluetooth() {
        Intent goToChat = new Intent(this, BluetoothChat.class);
        startActivity(goToChat);
        onStartNewActivityAnimations();
    }

    /**
     * Go to File Manager Activity.
     */
    public void FileManagerForm() {
        Intent goToFiles = new Intent(this, FileManager.class);
        startActivity(goToFiles);
        onStartNewActivityAnimations();
    }

    /**
     * Go to Settings Activity.
     */
    public void SettingsOfflineForm() {
        Intent goToFiles = new Intent(this, SettingsOffline.class);
        startActivity(goToFiles);
        onStartNewActivityAnimations();
    }

    /**
     * Go to WSN Chat Activity.
     */
    public void WSN_Chat() {
        Intent goToWSNChat = new Intent(this, WSN_Chat.class);
        startActivity(goToWSNChat);
        onStartNewActivityAnimations();
    }

    //Handles animations when going in and out of Activities.
    protected void onStartNewActivityAnimations() {
        overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
    }
}