package com.creatorbox.esense;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
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
 * A fragment class that handles the listing of all the logged in users in-app friends.
 */

public class FriendsFragment extends Fragment {

    private Friends_RecyclerViewAdapter adapter;
    private RecyclerView mFriendsList;

    private FirebaseDatabase mFriendsDatabase;
    private DatabaseReference mUserDatabase;
    private FirebaseUser mCurrentUser;
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final String firebaseDBURL = "https://smart-edubox-90c5d-default-rtdb.asia-southeast1.firebasedatabase.app/";

    private String mCurrent_user_id;
    private View mMainView;

    private final int[] childCount = {0};

    public FriendsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mMainView = inflater.inflate(R.layout.fragment_friends, container, false);
        mFriendsList = (RecyclerView) mMainView.findViewById(R.id.friendsListRecyclerView);
        mCurrent_user_id = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        mFriendsDatabase = FirebaseDatabase.getInstance(firebaseDBURL);
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mUserDatabase = FirebaseDatabase.getInstance(firebaseDBURL).getReference().child("Users").child(mCurrent_user_id);
        mUserDatabase.keepSynced(true);

        setupRecyclerView();

        Query query = mFriendsDatabase.getReference().child("Friends").child(mCurrent_user_id);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                childCount[0] = (int) snapshot.getChildrenCount();
                ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle("Friends  " + "(" + childCount[0] + ")");
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // Inflate the layout for this fragment
        return mMainView;
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

        if (mCurrentUser==null) {
            LogOutUser();
        } else {
            mUserDatabase.child("online").setValue(true);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (adapter != null) {
            adapter.stopListening();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null) {
            adapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (adapter != null) {
            adapter.stopListening();
        }
        mUserDatabase.child("online").setValue(false);
    }

    /**
     * A custom function that extends the LinearLayoutManager for the fragment's RecyclerView.
     * Without this, using only the default LinearLayoutManager will cause an Inconsistency error
     * for the RecyclerView holder class that can crash the app when navigating back to this fragment.
     */
    public class WrapContentLinearLayoutManager extends LinearLayoutManager {
        public WrapContentLinearLayoutManager(Context context) {
            super(context);
        }

        public WrapContentLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
            super(context, orientation, reverseLayout);
        }

        public WrapContentLinearLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
            super(context, attrs, defStyleAttr, defStyleRes);
        }

        @Override
        public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
            try {
                super.onLayoutChildren(recycler, state);
            } catch (IndexOutOfBoundsException e) {
                Log.e("TAG", "meet a IOOBE in RecyclerView");
            }
        }
    }

    /**
     * Initialize the RecyclerView
     */
    private void setupRecyclerView() {
        Query query = mFriendsDatabase.getReference().child("Friends").child(mCurrent_user_id);

        FirebaseRecyclerOptions<Friends_ModelClass> options =
                new FirebaseRecyclerOptions.Builder<Friends_ModelClass>()
                        .setQuery(query, Friends_ModelClass.class)
                        .build();

        adapter = new Friends_RecyclerViewAdapter(options, getActivity());
        mFriendsList.setLayoutManager(new WrapContentLinearLayoutManager(getContext(),
                LinearLayoutManager.VERTICAL, false));
        mFriendsList.setAdapter(adapter);
    }

    /**
     * Logout User from the app.
     */
    private void LogOutUser() {
        mAuth.signOut();
        Intent intentSignOut = new Intent(getContext(), OnlineLogin.class);
        startActivity(intentSignOut);
    }
}