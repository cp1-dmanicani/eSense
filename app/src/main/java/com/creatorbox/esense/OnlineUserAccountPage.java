/*
Application Name: Smart EduBox
Created Date: Sept. 15, 2022
Company: CreatorBox Solutions
Developer: DGMJr.
*/

package com.creatorbox.esense;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * ONLINE MODE
 * An activity that used in displaying the user's profile pages.
 * This is used to Send and Cancel Friend Requests, Accept and Decline Friend Requests, and Unfriend the User.
 */
public class OnlineUserAccountPage extends AppCompatActivity{

    private CircleImageView profileDP;
    private TextView userName, userStatus, joinedDate, myUserName;
    private Button sendRequest, declineRequest;
    private ProgressDialog progressDialog;

    private String image_url, other_name, status, joined_date, loggedIn_user_id, user_id, current_state;

    private final String firebaseDBURL = "https://smart-edubox-90c5d-default-rtdb.asia-southeast1.firebasedatabase.app/";
    private DatabaseReference mFriendReqDatabase, mFriendDatabase, mNotificationDatabase, mGetCurrentUserInfo;
    private FirebaseUser mCurrentUser;

    private String current_user_name;
    ActionBar ac;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_user_account_page);

        init();

        progressDialog = new ProgressDialog(OnlineUserAccountPage.this);
        loadUserInfo();
    }

    /**
     * Initialize all components.
     */
    private void init() {
        myUserName = (TextView) findViewById(R.id.myUserNameText);
        profileDP = (CircleImageView) findViewById(R.id.acctSettingsImagePage);
        userName = (TextView) findViewById(R.id.displayNameTextPage);
        userStatus = (TextView) findViewById(R.id.profileStatusTextPage);
        joinedDate = (TextView) findViewById(R.id.joinedDateText);
        sendRequest = (Button) findViewById(R.id.sendFriendRequestBtn);
        declineRequest = (Button) findViewById(R.id.declineFriendReqBtn);

        user_id = getIntent().getStringExtra("user_id");
        other_name = getIntent().getStringExtra("other_name");
        image_url = getIntent().getStringExtra("image_url");
        status = getIntent().getStringExtra("status");
        joined_date = getIntent().getStringExtra("joined_date");
        current_state = "not_friends";

        ac = getSupportActionBar();
        ac.setTitle("User Profile");


        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        loggedIn_user_id = mCurrentUser.getUid();
        mFriendReqDatabase = FirebaseDatabase.getInstance(firebaseDBURL).getReference().child("FriendRequests");
        mFriendReqDatabase.keepSynced(true);
        mFriendDatabase = FirebaseDatabase.getInstance(firebaseDBURL).getReference().child("Friends");
        mFriendDatabase.keepSynced(true);
        mNotificationDatabase = FirebaseDatabase.getInstance(firebaseDBURL).getReference().child("Notifications");

        mGetCurrentUserInfo = FirebaseDatabase.getInstance(firebaseDBURL).getReference()
                .child("Users").child(loggedIn_user_id);
        mGetCurrentUserInfo.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String user_name = snapshot.child("name").getValue().toString();
                myUserName.setText(user_name);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        if (!image_url.equals("default")) {
            Picasso.get().load(image_url).placeholder(R.drawable.baseline_person_dp).into(profileDP);
        }
        userName.setText(other_name);
        userStatus.setText(status);
        joinedDate.setText("Joined on " + joined_date);

        if (!loggedIn_user_id.equals(user_id)) {
            sendRequest.setVisibility(View.VISIBLE);
            declineRequest.setVisibility(View.VISIBLE);
        } else {
            sendRequest.setVisibility(View.GONE);
            declineRequest.setVisibility(View.GONE);
        }

        sendRequest.setOnClickListener(v -> {
            friendRequest();
            declineRequest.setVisibility(View.INVISIBLE);
        });

        declineRequest.setOnClickListener(v -> {
            declineFriendRequest();
        });
        declineRequest.setVisibility(View.INVISIBLE);
        getFriendRequestFromOtherUsers();
    }

    /**
     * Show progress dialog before showing the user's info.
     */
    private void loadUserInfo() {
        progressDialog.setTitle("LOADING USER INFO");
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        Handler handler = new Handler();
        handler.postDelayed(() ->
                progressDialog.dismiss(), 1000);
    }

    /**
     * Retrieve the Friend Request status from other users.
     */
    private void getFriendRequestFromOtherUsers() {
        mFriendReqDatabase.child(loggedIn_user_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild(user_id)) {
                    String req_type = snapshot.child(user_id).child("request_type").getValue(String.class);
                    if (req_type.equals("received")) {
                        current_state = "request_received";
                        sendRequest.setText("Accept Friend Request");
                        declineRequest.setVisibility(View.VISIBLE);
                    } else if (req_type.equals("sent")) {
                        current_state = "request_sent";
                        sendRequest.setText("Cancel Friend Request.");
                        declineRequest.setVisibility(View.INVISIBLE);
                    }
                } else {
                    mFriendDatabase.child(loggedIn_user_id).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.hasChild(user_id)) {
                                current_state = "friends";
                                sendRequest.setText("Unfriend User");
                                declineRequest.setVisibility(View.INVISIBLE);
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    /**
     * A function that handles the Friend Requests feature of the app.
     */
    private void friendRequest() {
        //Sending Friend Request
        if (current_state.equals("not_friends")) {
            sendRequest.setEnabled(false);
            declineRequest.setVisibility(View.INVISIBLE);
            mFriendReqDatabase.child(loggedIn_user_id).child(user_id).child("request_type")
                    .setValue("sent").addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            mFriendReqDatabase.child(user_id).child(loggedIn_user_id).child("request_type")
                                    .setValue("received").addOnSuccessListener(unused -> {

                                        HashMap<String, String> notificationData = new HashMap<>();
                                        notificationData.put("from", loggedIn_user_id);
                                        notificationData.put("type", "request");

                                        String key = mNotificationDatabase.child(user_id).push().getKey();
                                        mNotificationDatabase.child(user_id).child(key).setValue(notificationData).addOnSuccessListener(unused1 -> {
                                            sendRequest.setEnabled(true);
                                            sendRequest.setText("Cancel Friend Request");
                                            current_state = "request_sent";
                                            displayToast("Friend request sent.");
                                        });
                                    });
                        } else {
                            displayToast("Failed to send friend request.");
                        }
                    });
        }

        //Cancelling Friend Request
        if (current_state.equals("request_sent")) {
            declineRequest.setVisibility(View.INVISIBLE);
            mFriendReqDatabase.child(loggedIn_user_id).child(user_id).removeValue().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    mFriendReqDatabase.child(user_id).child(loggedIn_user_id).removeValue().addOnSuccessListener(unused -> {
                        sendRequest.setEnabled(true);
                        sendRequest.setText("Send Friend Request");
                        current_state = "not_friends";
                        displayToast("Friend request cancelled.");
                    });
                } else {
                    displayToast("Failed to cancel friend request.");
                }
            });
        }

        //Request Received State
        //Accept the Friend Request
        if (current_state.equals("request_received")) {
            String currentDate = DateFormat.getDateTimeInstance().format(new Date());
            String current_user_name = myUserName.getText().toString();
            HashMap<String, String> loggedInUserMap = new HashMap<>();
            loggedInUserMap.put("friend_name", current_user_name);
            loggedInUserMap.put("friend_since_date", currentDate);

            HashMap<String, String> friendUserMap = new HashMap<>();
            friendUserMap.put("friend_name", other_name);
            friendUserMap.put("friend_since_date", currentDate);

            mFriendDatabase.child(loggedIn_user_id).child(user_id).setValue(friendUserMap)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            mFriendDatabase.child(user_id).child(loggedIn_user_id).setValue(loggedInUserMap)
                                    .addOnSuccessListener(unused ->
                                            mFriendReqDatabase.child(loggedIn_user_id).child(user_id).removeValue()
                                                    .addOnSuccessListener(unused12 ->
                                                            mFriendReqDatabase.child(user_id).child(loggedIn_user_id).removeValue()
                                                            .addOnSuccessListener(unused121 -> {
                                                                sendRequest.setEnabled(true);
                                                                sendRequest.setText("Unfriend User");
                                                                current_state = "friends";
                                                                displayToast("Accepted Friend Request.");
                                                            })));
                        } else {
                            displayToast("Failed to accept friend request from user.");
                        }
                    });
        }

        //Unfriend User
        if (current_state.equals("friends")) {
            declineRequest.setVisibility(View.INVISIBLE);
            mFriendDatabase.child(loggedIn_user_id).child(user_id).removeValue().addOnCompleteListener(task -> {
                AlertDialog alertDialog = new AlertDialog.Builder(this)
                        .setTitle("Unfriend User?")
                        .setMessage("Do you really want to remove this user from your friends list?")
                        .setIcon(R.drawable.ic_baseline_person_remove_24)
                        .setPositiveButton("OK", (dialog, which) -> {
                            if (task.isSuccessful()) {
                                mFriendDatabase.child(user_id).child(loggedIn_user_id).removeValue().addOnSuccessListener(unused -> {
                                    sendRequest.setEnabled(true);
                                    sendRequest.setText("Send Friend Request");
                                    current_state = "not_friends";
                                    displayToast("User Unfriended.");
                                });
                            } else {
                                displayToast("Failed to unfriend user.");
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        }
    }

    private void declineFriendRequest() {
        //Declining Friend Request
        if (current_state.equals("request_received")) {
            mFriendReqDatabase.child(loggedIn_user_id).child(user_id).removeValue().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    mFriendReqDatabase.child(user_id).child(loggedIn_user_id).removeValue().addOnSuccessListener(unused -> {
                        sendRequest.setEnabled(true);
                        sendRequest.setText("Send Friend Request");
                        current_state = "not_friends";
                        displayToast("Declined Friend Request.");
                        declineRequest.setVisibility(View.INVISIBLE);
                    });
                } else {
                    displayToast("Failed to cancel friend request.");
                }
            });
        }
    }

    private void displayToast(String message) {
        Toast.makeText(OnlineUserAccountPage.this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * NOTE: NOT IN USE
     * Used to copy the user's ID to the phone's clipboard.
     */
    private void copyIDFun() {
        ClipboardManager clipboard = (ClipboardManager)
                getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("User ID", user_id);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "Copied User ID to clipboard.", Toast.LENGTH_SHORT).show();
    }
}