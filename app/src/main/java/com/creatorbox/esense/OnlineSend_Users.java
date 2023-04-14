/*
Application Name: Smart EduBox
Created Date: Sept. 15, 2022
Company: CreatorBox Solutions
Developer: DGMJr.
*/

package com.creatorbox.esense;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

/**
 * ONLINE MODE
 * An activity that handles the listing of the user's friends before initiating the Online File Transfer.
 */
public class OnlineSend_Users extends AppCompatActivity {

    private OnlineSendUsersRecycleAdapter adapter;
    private RecyclerView mFriendsList;
    private TextView noFriendsText;
    private ActionBar ab;

    private FirebaseDatabase mFriendsDatabase, database;
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private DatabaseReference mUserDatabase;
    private FirebaseUser mCurrentUser;
    private final String firebaseDBURL = "https://smart-edubox-90c5d-default-rtdb.asia-southeast1.firebasedatabase.app/";

    private String mCurrent_user_id;
    private final int[] childCount = {0};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.online_send_users);

        init();
    }

    /**
     * Initialize all components.
     */
    private void init() {

        mCurrent_user_id = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        mFriendsDatabase = FirebaseDatabase.getInstance(firebaseDBURL);
        mFriendsList = (RecyclerView) findViewById(R.id.usersSendList);
        noFriendsText = (TextView) findViewById(R.id.noFriendsTextSend);

        database = FirebaseDatabase.getInstance(firebaseDBURL);
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mUserDatabase = FirebaseDatabase.getInstance(firebaseDBURL).getReference().child("Users").child(mCurrent_user_id);
        mUserDatabase.keepSynced(true);

        ab = getSupportActionBar();
        ab.setTitle("FILE TRANSFER");
        ab.setSubtitle("Friends List");

        try {
            setupRecyclerView();
        } catch (IndexOutOfBoundsException e) {
            Log.e("setupRecyclerView", "An error occured.");
        }

        Query query = mFriendsDatabase.getReference().child("Friends").child(mCurrent_user_id);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                childCount[0] = (int) snapshot.getChildrenCount();
                if (childCount[0]==0) {
                    noFriendsText.setVisibility(View.VISIBLE);
                } else {
                    noFriendsText.setVisibility(View.INVISIBLE);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    /**
     * Initialize the RecyclerView
     */
    private void setupRecyclerView() {
        Query query = mFriendsDatabase.getReference().child("Friends").child(mCurrent_user_id);
        query.keepSynced(true);
        FirebaseRecyclerOptions<Friends_ModelClass> options =
                new FirebaseRecyclerOptions.Builder<Friends_ModelClass>()
                        .setQuery(query, Friends_ModelClass.class)
                        .build();

        adapter = new OnlineSendUsersRecycleAdapter(options, OnlineSend_Users.this, OnlineSend_Users.this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false);
        mFriendsList.setLayoutManager(linearLayoutManager);
        mFriendsList.setAdapter(adapter);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, HomeScreenOnline.class);
        startActivity(intent);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    /**
     * Set value of online (the status indicator whether user is logged in the Online Mode or not) to true.
     * Start Listening to the adapter to retrieve data from Firebase.
     */
    @Override
    public void onStart() {
        super.onStart();
        if (adapter != null) {
            adapter.startListening();
        }
        checkUserStatus();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (adapter != null) {
            adapter.stopListening();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapter != null) {
            adapter.startListening();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (adapter != null) {
            adapter.stopListening();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUserDatabase.child("online").setValue(false);
    }

    /**
     * Logout User from the app.
     */
    private void LogOutUser() {
        mAuth.signOut();
        Intent intentSignOut = new Intent(OnlineSend_Users.this, OnlineLogin.class);
        startActivity(intentSignOut);
    }

    private void checkUserStatus() {
        if (mCurrentUser!=null) {
            mUserDatabase.child("online").setValue(true);
        } else {
            LogOutUser();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.chatapp_users_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.actionSearch);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint("Search Name...");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                processSearch(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                processSearch(newText);
                return false;
            }
        });

        return  true;
    }

    /**
     * A function that handles the filtering of names of users in the RecyclerView list.
     * @param s a string value that is received by the function when the user types on the
     *          MenuItem searchAction.
     */
    private void processSearch(String s) {
        Query query = database.getReference().child("Friends").child(mCurrent_user_id)
                .orderByChild("friend_name").startAt(s).endAt(s+"\uf8ff");

        FirebaseRecyclerOptions<Friends_ModelClass> optionsQuery =
                new FirebaseRecyclerOptions.Builder<Friends_ModelClass>()
                        .setQuery(query, Friends_ModelClass.class)
                        .build();

        adapter = new OnlineSendUsersRecycleAdapter(optionsQuery, this, this);
        adapter.startListening();
        mFriendsList.setAdapter(adapter);
    }
}