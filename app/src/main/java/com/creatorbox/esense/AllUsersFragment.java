package com.creatorbox.esense;

import static com.creatorbox.esense.OfflineLogin.Theme_Material_Light_Dialog_Alert;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
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
 * The fragment that handles the listing of All Registered Users of the application.
 */
public class AllUsersFragment extends Fragment {

    private ProgressDialog progressDialog;

    private AllUsers_RecyclerViewAdapter adapter;
    private RecyclerView allUsersList;
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private DatabaseReference mUserDatabase;
    private FirebaseUser mCurrentUser;
    private FirebaseDatabase database;
    private final String firebaseDBURL = "https://smart-edubox-90c5d-default-rtdb.asia-southeast1.firebasedatabase.app/";

    private final int[] childCount = {0};
    private String loggedInUserID;

    public AllUsersFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragments_all_users, container, false);

        mCurrentUser = mAuth.getCurrentUser();
        loggedInUserID = Objects.requireNonNull(mCurrentUser).getUid();
        database = FirebaseDatabase.getInstance(firebaseDBURL);
        mUserDatabase = FirebaseDatabase.getInstance(firebaseDBURL).getReference().child("Users").child(loggedInUserID);
        mUserDatabase.keepSynced(true);
        progressDialog = new ProgressDialog(getContext(), Theme_Material_Light_Dialog_Alert);
        allUsersList = view.findViewById(R.id.allUsersList);

        showProgressDialog("Loading Users", "Please wait...");

        Query query = database.getReference().child("Users").orderByChild("name");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                childCount[0] = (int) snapshot.getChildrenCount();
                ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle("All Users  " + "(" + childCount[0] + ")");
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        //Add progress dialog to load all users in RecyclerVIew.
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            progressDialog.dismiss();
        }, 1000);

        try {
            setupRecyclerView();
        } catch (IndexOutOfBoundsException ex) {
            Log.e("setupRecyclerView", "IndexOutOfBoundsException in setupRecyclerView, AllUsersFragment");
        }
        inflateMenu();

        return view;
    }

    /**
     * Show a progress dialog when the fragment is inflated.
     * @param title the title of the Progress Dialog.
     * @param message the message of the Progress Dialog.
     */
    public void showProgressDialog(String title, String message) {
        progressDialog.setTitle(title);
        progressDialog.setMessage(message);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }

    /**
     * Inflate the Menu Items of the Fragment.
     */
    private void inflateMenu() {
        MenuHost menuHost = getActivity();
        menuHost.addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.chatapp_users_menu, menu);
                MenuItem item = menu.findItem(R.id.actionSearch);
                SearchView searchView = (SearchView) item.getActionView();
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
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
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
        Query query = database.getReference().child("Users").orderByChild("name");

        FirebaseRecyclerOptions<AllUsers_ModelClass> options =
                new FirebaseRecyclerOptions.Builder<AllUsers_ModelClass>()
                        .setQuery(query, AllUsers_ModelClass.class)
                        .build();

        adapter = new AllUsers_RecyclerViewAdapter(options, getActivity());
        allUsersList.setLayoutManager(new WrapContentLinearLayoutManager(getContext(),
                LinearLayoutManager.VERTICAL , false));
        allUsersList.setAdapter(adapter);
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
     * A function that handles the filtering of names of users in the RecyclerView list.
     * @param s a string value that is received by the function when the user types on the
     *          MenuItem searchAction.
     */
    private void processSearch(String s) {
        Query query = database.getReference().child("Users")
                .orderByChild("name").startAt(s).endAt(s+"\uf8ff");

        FirebaseRecyclerOptions<AllUsers_ModelClass> optionsQuery =
                new FirebaseRecyclerOptions.Builder<AllUsers_ModelClass>()
                        .setQuery(query, AllUsers_ModelClass.class)
                        .build();

        adapter = new AllUsers_RecyclerViewAdapter(optionsQuery, getActivity());
        adapter.startListening();
        allUsersList.setAdapter(adapter);
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