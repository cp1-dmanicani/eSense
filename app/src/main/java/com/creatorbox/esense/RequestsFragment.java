package com.creatorbox.esense;

import android.content.Context;
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
 * A fragment that lists all the user's friend request, either he sent it or other user's sent a request to them.
 */
public class RequestsFragment extends Fragment {

    private RecyclerView friendReqList;
    private FriendRequests_RecyclerViewAdapter adapter;

    private final String firebaseDBURL = "https://smart-edubox-90c5d-default-rtdb.asia-southeast1.firebasedatabase.app/";
    private DatabaseReference mFriendDatabase, mNotificationDatabase, mGetCurrentUserInfo;
    private FirebaseUser mCurrentUser;
    private FirebaseDatabase mFriendReqDatabase;
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private String image_url, other_name, status, joined_date, loggedIn_user_id, user_id, current_state;
    private final int[] childCount = {0};

    public RequestsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_requests, container, false);

        friendReqList = (RecyclerView) view.findViewById(R.id.friendReqList);
        mCurrentUser = mAuth.getCurrentUser();
        loggedIn_user_id = Objects.requireNonNull(mCurrentUser).getUid();
        mFriendDatabase = FirebaseDatabase.getInstance(firebaseDBURL).getReference().child("FriendRequests");
        mFriendReqDatabase = FirebaseDatabase.getInstance(firebaseDBURL);


        Query query = mFriendReqDatabase.getReference().child("FriendRequests").child(loggedIn_user_id);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                childCount[0] = (int) snapshot.getChildrenCount();
                ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle("Friend Requests  " + "(" + childCount[0] + ")");
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        try {
            setupRecyclerView();
        } catch (IndexOutOfBoundsException ex) {
            Log.e("setupRecyclerView", "IndexOutOfBoundsException in setupRecyclerView, AllUsersFragment");
        }

        return view;
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
        Query query = mFriendDatabase.child(loggedIn_user_id).orderByChild("request_type");

        FirebaseRecyclerOptions<FriendRequests_ModelClass> options =
                new FirebaseRecyclerOptions.Builder<FriendRequests_ModelClass>()
                        .setQuery(query, FriendRequests_ModelClass.class)
                        .build();

        adapter = new FriendRequests_RecyclerViewAdapter(options, getContext(), getActivity());
        friendReqList.setLayoutManager(new WrapContentLinearLayoutManager(getContext(),
                LinearLayoutManager.VERTICAL, false));
        friendReqList.setAdapter(adapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (adapter != null) {
            adapter.startListening();
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
    }
}