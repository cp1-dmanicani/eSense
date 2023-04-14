/*
Application Name: Smart EduBox
Created Date: Sept. 15, 2022
Company: CreatorBox Solutions
Developer: DGMJr.
*/

package com.creatorbox.esense;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An activity that handles the Online Chat with different users.
 */
public class OnlineChat_Basic extends AppCompatActivity implements PinMessageListener{

    private final String firebaseDBURL = "https://smart-edubox-90c5d-default-rtdb.asia-southeast1.firebasedatabase.app/";
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase database;
    private DatabaseReference reference;
    private FirebaseUser currentUser;

    EditText messageBox;
    public TextView pinnedMessage;
    FloatingActionButton send;
    RecyclerView chatThread;

    OnlineChat_MessageAdapter messageAdapter;
    List<OnlineChat_ModelClass> list;

    String userName, otherName, otherNameID, userNameID, other_name, image_url, status, joined_date;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.online_chat_basic);

        init();
    }

    /**
     * Initialize all components.
     */
    private void init() {
        pinnedMessage = findViewById(R.id.pinnedMessageText);
        messageBox = findViewById(R.id.typeMessage);
        send = findViewById(R.id.fabSend);
        chatThread = findViewById(R.id.chatThreadRecyclerview);

        database = FirebaseDatabase.getInstance(firebaseDBURL);
        reference = database.getReference();
        userNameID = mAuth.getCurrentUser().getUid();

        chatThread.setLayoutManager(new LinearLayoutManager(this));
        list = new ArrayList<>();

        userName = getIntent().getStringExtra("userName");
        otherName = getIntent().getStringExtra("otherName");
        otherNameID = getIntent().getStringExtra("otherNameID");

        other_name = getIntent().getStringExtra("other_name");
        image_url = getIntent().getStringExtra("image_url");
        status = getIntent().getStringExtra("status");
        joined_date = getIntent().getStringExtra("joined_date");

        ActionBar ab = getSupportActionBar();
        ab.setTitle("CHAT");
        ab.setSubtitle("Chatting with: " + otherName);

        send.setOnClickListener(v -> {
            String message = messageBox.getText().toString();
            if (!message.isEmpty()) {
                sendMessage(message);
                messageBox.getText().clear();
            }
            else {
                Toast.makeText(OnlineChat_Basic.this, "Message Box Empty", Toast.LENGTH_SHORT).show();
            }
        });
        try {
            getMessage();
        } catch (NullPointerException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        pinnedMessage.setOnClickListener(v -> {
            showPinnedMessageDialog();
        });
        getPinnedMessageFromFirebase();
    }

    /**
     * Gets Messages from the Firebase Realtime Database.
     * userName refers to the current logged in user.
     * otherName refers to the user being sent the message.
     */
    private void getMessage() {
        reference.child("Messages").child(userNameID).child(otherNameID)
                .addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                OnlineChat_ModelClass modelClass = snapshot.getValue(OnlineChat_ModelClass.class);
                list.add(modelClass);
                messageAdapter.notifyDataSetChanged();
                chatThread.scrollToPosition(list.size()-1);
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        messageAdapter = new OnlineChat_MessageAdapter(this, this, list, userNameID);
        chatThread.setAdapter(messageAdapter);
    }

    /**
     * Send message to other users.
     * @param message the message to be sent.
     */
    private void sendMessage(String message) {
        SimpleDateFormat currentDate = new SimpleDateFormat("dd/MM/yyyy");
        String date = currentDate.format(new Date());

        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
        String time = currentTime.format(new Date());

        String key = reference.child("Messages").child(userNameID).child(otherNameID).push().getKey();
        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put("message", message);
        messageMap.put("from", userNameID);
        messageMap.put("date", date);
        messageMap.put("time", time);
        reference.child("Messages").child(userNameID).child(otherNameID).child(key).setValue(messageMap).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                reference.child("Messages").child(otherNameID).child(userNameID).child(key).setValue(messageMap);
                Toast.makeText(OnlineChat_Basic.this, "Message sent.", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(this, "Failed to send message.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Retrieve the pinned message of the conversation from Firebase.
     */
    private void getPinnedMessageFromFirebase() {
        reference.child("PinnedMessage").child(userNameID).child(otherNameID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    String getPinnedMessage = snapshot.child("pinned_message").getValue().toString();
                    pinnedMessage.setText(getPinnedMessage);
                    if (getPinnedMessage.isEmpty() || getPinnedMessage.equals("")) {
                        savePinnedMessageToFirebase();
                    }
                    else {
                        //
                    }
                } catch (NullPointerException e) {
                    Log.e("PinnedMessageException", e.getMessage());
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    /**
     * If Reference inside PinnedMessage is empty for the sender and receiver, this will upload an empty value
     * to the database so app won't crash when trying to retrieve from conversations that doesn't have an
     * existing pinned message.
     */
    private void savePinnedMessageToFirebase() {
            reference.child("PinnedMessage").child(userNameID).child(otherNameID).child("pinned_message").setValue("").addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    reference.child("PinnedMessage").child(otherNameID).child(userNameID).child("pinned_message").setValue("");
                }
                else {
                    Toast.makeText(this, "Failed to save message.", Toast.LENGTH_SHORT).show();
                }
            });
    }

    /**
     * Passes the other users value via intent when going to their Profile Page.
     */
    private void userInfo() {
        Intent intentVisit = new Intent(OnlineChat_Basic.this, OnlineUserAccountPage.class);
        intentVisit.putExtra("user_id", otherNameID);
        intentVisit.putExtra("other_name", other_name);
        intentVisit.putExtra("image_url", image_url);
        intentVisit.putExtra("status", status);
        intentVisit.putExtra("joined_date", joined_date);
        startActivity(intentVisit);
        onStartNewActivityAnimations();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.chatapp_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.chatapp_menu_searchInConvo);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint("Search in conversation...");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // inside on query text change method we are
                // calling a method to filter our recycler view.
                List<OnlineChat_ModelClass> filteredList = new ArrayList<>();

                // Loop through the original list of strings and add only those
                // that contain the search query to the filtered list
                for (OnlineChat_ModelClass s : list) {
                    if (s.getMessage().toLowerCase().contains(newText.toLowerCase())) {
                        filteredList.add(s);
                    }
                }
                messageAdapter = new OnlineChat_MessageAdapter(OnlineChat_Basic.this, OnlineChat_Basic.this, filteredList, userNameID);
                chatThread.setAdapter(messageAdapter);
                return false;
            }
        });

        return  true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case R.id.chatapp_menu_userInfo:
                userInfo();
                return true;
        }
        return false;
    }

    @Override
    public void onStart() {
        super.onStart();
        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "No signed-in user. Please sign in first.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, OnlineLogin.class);
            startActivity(intent);
            finish();
        }
    }

    /**
     * Uses the PinMessageListener to get the message text to be pinned from the RecyclerView Component.
     * Data is passed here by the OnlineChat_MessageAdapter.
     * @param value the message to be pinned and displayed on the pinnedMessage textview.
     */
    @Override
    public void onPin(String value) {
        if (!value.equals("")) {
            pinnedMessage.setText(value);

            reference.child("PinnedMessage").child(userNameID).child(otherNameID).child("pinned_message").setValue(value).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    reference.child("PinnedMessage").child(otherNameID).child(userNameID).child("pinned_message").setValue(value);
                }
                else {
                    Toast.makeText(this, "Failed to save message.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /**
     * Shows a dialog containing the pinned message.
     */
    private void showPinnedMessageDialog() {
        String pinned_message = pinnedMessage.getText().toString();
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("Pinned Message")
                .setIcon(R.drawable.ic_pin)
                .setMessage("\n"+ pinned_message)
                .setPositiveButton("Close", null)
                .create();
        alertDialog.show();
    }

    //Handles the animation when going from activity to activity.
    protected void onStartNewActivityAnimations() {
        overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, OnlineChatUsers.class);
        startActivity(intent);
        this.finish();
    }
}