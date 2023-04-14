/*
Application Name: Smart EduBox
Created Date: Dec. 23, 2022
Company: CreatorBox Solutions
Developer: DGMJr.
*/

package com.creatorbox.esense;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

/**
 * ONLINE MODE
 * An application class that handles Firebase data to cache pictures and user's status when disconnecting from server.
 */
public class eSense extends Application {

    private DatabaseReference mUserDatabase;
    private FirebaseAuth mAuth;
    private final String firebaseDBURL = "https://smart-edubox-90c5d-default-rtdb.asia-southeast1.firebasedatabase.app/";
    private String loggedInUserID;

    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseDatabase.getInstance(firebaseDBURL).setPersistenceEnabled(true);

        //Picasso
        Picasso.Builder builder = new Picasso.Builder(this);
        builder.downloader(new OkHttp3Downloader(this, Integer.MAX_VALUE));
        Picasso built = builder.build();
        built.setIndicatorsEnabled(true);
        built.setLoggingEnabled(true);
        Picasso.setSingletonInstance(built);

        try {
            mAuth = FirebaseAuth.getInstance();
            loggedInUserID = mAuth.getCurrentUser().getUid();
            mUserDatabase = FirebaseDatabase.getInstance(firebaseDBURL).getReference().child("Users").child(loggedInUserID);
            mUserDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    if (snapshot!=null) {
                        mUserDatabase.child("online").onDisconnect().setValue(false);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        } catch (NullPointerException e) {
            Log.e("eSense.java", "NullPointerException caught.");
        }

    }
}
