package com.creatorbox.esense;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Use this class to test new functionalities.
 */
public class TestActivity extends AppCompatActivity {

    private final String firebaseDBURL = "https://smart-edubox-90c5d-default-rtdb.asia-southeast1.firebasedatabase.app/";
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase database;
    private DatabaseReference reference;
    private FirebaseUser currentUser;

    OnlineUser_RecyclerAdapter recyclerAdapter;
    List<OnlineUser_ModelClass> list;
    RecyclerView userList;

    String userName, userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        init();
    }

    private void init() {
        userList = findViewById(R.id.userListRecyclerView);

        database = FirebaseDatabase.getInstance(firebaseDBURL);
        reference = database.getReference();

        userList.setHasFixedSize(true);
        userList.setLayoutManager(new LinearLayoutManager(this));
        list = new ArrayList<>();

        getUsers();
    }

    private void getUsers() {
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for (DataSnapshot dataSnapshot : snapshot.child("Users").getChildren()) {

                    if (dataSnapshot.hasChild("name") && dataSnapshot.hasChild("thumb_image")
                            && dataSnapshot.hasChild("status") && dataSnapshot.hasChild("account_creation_date")) {
                        userID = mAuth.getCurrentUser().getUid();
                        final String uuid = dataSnapshot.getKey();
                        if (!uuid.equals(userID)) {

                            final String name = dataSnapshot.child("name").getValue(String.class);
                            final String thumbImage = dataSnapshot.child("thumb_image").getValue(String.class);
                            final String status = dataSnapshot.child("status").getValue(String.class);
                            final String creationDate = dataSnapshot.child("account_creation_date").getValue(String.class);
                            OnlineUser_ModelClass modelClass = new OnlineUser_ModelClass(uuid, name, status, thumbImage, creationDate);
                            list.add(modelClass);
                            userName = name;
                        }
                    }
                }
                //recyclerAdapter = new OnlineUser_RecyclerAdapter(TestActivity.this, TestActivity.this, list, userName);
                userList.setAdapter(recyclerAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // inside on query text change method we are
                // calling a method to filter our recycler view.
                List<OnlineUser_ModelClass> filteredList = new ArrayList<>();

                // Loop through the original list of strings and add only those
                // that contain the search query to the filtered list
                for (OnlineUser_ModelClass s : list) {
                    if (s.getUserName().toLowerCase().contains(newText.toLowerCase())) {
                        filteredList.add(s);
                    }
                }
                //recyclerAdapter = new OnlineUser_RecyclerAdapter(TestActivity.this, TestActivity.this, filteredList, userName);
                userList.setAdapter(recyclerAdapter);
                return false;
            }
        });
        return true;
    }
}