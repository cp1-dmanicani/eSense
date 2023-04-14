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
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * ONLINE MODE
 * The activity for the users Account Settings.
 */
public class AccountSettings extends AppCompatActivity {

    private final String firebaseDBURL = "https://smart-edubox-90c5d-default-rtdb.asia-southeast1.firebasedatabase.app/";
    private DatabaseReference mUserDatabase;
    private FirebaseUser mCurrentUser;
    private StorageReference mImageStorage;
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private static final int GALLERY_PICK = 1;

    private DrawerLayout drawerLayout;
    private CircleImageView profilePic;
    private ImageButton changeProfileImage, copyID, menuDrawerBtn;
    private TextView profileStatus, displayName, joinedDate, userID;
    private ProgressDialog mProgressDialog;
    private Context context;
    private NavigationView navigationView;
    private FragmentTransaction ft;

    private String current_uuid;
    private String name,status,thumb_image,joined_date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_settings);

        init();
    }

    /**
     * Initialize all components.
     */
    private void init() {
        context = this;
        menuDrawerBtn = (ImageButton) findViewById(R.id.menuDrawerBtn);
        navigationView = (NavigationView) findViewById(R.id.navigationView);
        profilePic = (CircleImageView) findViewById(R.id.acctSettingsImage);
        profileStatus = (TextView) findViewById(R.id.profileStatusText);
        displayName = (TextView) findViewById(R.id.displayNameText);
        changeProfileImage = (ImageButton) findViewById(R.id.changeImage);
        joinedDate = (TextView) findViewById(R.id.joinedDateText);
        drawerLayout = (DrawerLayout) findViewById(R.id.accountDrawerSettingsLayout);

        ActionBar actionBar = getSupportActionBar();
        Objects.requireNonNull(actionBar).setTitle("Account Settings");
        actionBar.setSubtitle("Your Profile");

        mImageStorage = FirebaseStorage.getInstance().getReference();
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        current_uuid = mCurrentUser.getUid();
        mUserDatabase = FirebaseDatabase.getInstance(firebaseDBURL).getReference().child("Users").child(current_uuid);
        mUserDatabase.keepSynced(true);
        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //Get String value of user sub-values from Firebase Realtime Storage
                name = dataSnapshot.child("name").getValue().toString();
                status = dataSnapshot.child("status").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();
                thumb_image = dataSnapshot.child("thumb_image").getValue().toString();
                joined_date = dataSnapshot.child("account_creation_date").getValue().toString();

                displayName.setText(name);
                profileStatus.setText(status);
                joinedDate.setText("Joined on " + joined_date);

                if (!thumb_image.equals("default")) {
                    Picasso.get().load(thumb_image).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.baseline_person_dp).into(profilePic,
                            new Callback() {
                                @Override
                                public void onSuccess() {}
                                @Override
                                public void onError(Exception e) {
                                    Picasso.get().load(thumb_image).placeholder(R.drawable.ic_person).into(profilePic);
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseErrorError) {
                Toast.makeText(AccountSettings.this, databaseErrorError.toString(), Toast.LENGTH_SHORT).show();
            }
        });

        changeProfileImage.setOnClickListener(v -> pickImageFromGallery());
        menuDrawerBtn.setOnClickListener(v -> {
                    loadDataToNavHeader();
                    drawerLayout.openDrawer(GravityCompat.START);
                });
        navigationViewItems();
    }

    /**
     * Load data into the Navigation Drawer's Header layout.
     */
    private void loadDataToNavHeader() {
        View navHeaderView = navigationView.getHeaderView(0);
        TextView navUserName = navHeaderView.findViewById(R.id.nav_drawer_username);
        TextView navUserStatus = navHeaderView.findViewById(R.id.nav_drawer_user_status);
        CircleImageView navUserPicture = navHeaderView.findViewById(R.id.nav_drawer_user_picture);

        navUserName.setText(name);
        navUserStatus.setText(status);
        if (!thumb_image.equals("default")) {
            Picasso.get().load(thumb_image).networkPolicy(NetworkPolicy.OFFLINE)
                    .placeholder(R.drawable.baseline_person_dp).into(navUserPicture,
                            new Callback() {
                                @Override
                                public void onSuccess() {}
                                @Override
                                public void onError(Exception e) {
                                    Picasso.get().load(thumb_image).placeholder(R.drawable.ic_person).into(navUserPicture);
                                }
                            });
        }
    }

    /**
     * Set functions for the Navigation Drawer item's list.
     */
    private void navigationViewItems() {
        navigationView.setNavigationItemSelectedListener(menuItem -> {
            int id=menuItem.getItemId();
            if (id == R.id.friends) {
                viewFriendsListFragment();
            } else if (id == R.id.requests) {
                viewFriendRequestsFragment();
            } else if (id == R.id.all_users) {
                viewAllUsersFragment();
            } else if (id == R.id.update_account) {
                goToChangeStatus();
            }
            drawerLayout.closeDrawers();
            return true;
        });
        navigationView.setItemIconTintList(null);
    }

    /**
     * Inflate the FriendsFragment.java
     */
    private void viewFriendsListFragment() {
        ft = getSupportFragmentManager().beginTransaction();
        FriendsFragment fragmentFriends = new FriendsFragment();
        ft.setCustomAnimations(
                R.anim.fade_in,
                R.anim.slide_out
        );
        ft.replace(R.id.accountDrawerSettingsLayout, fragmentFriends, "friends");
        ft.addToBackStack(null);
        ft.commit();
    }

    /**
     * Inflate the RequestFragment.java
     */
    private void viewFriendRequestsFragment() {
        ft = getSupportFragmentManager().beginTransaction();
        RequestsFragment fragmentRequests = new RequestsFragment();
        ft.setCustomAnimations(
                R.anim.fade_in,
                R.anim.slide_out
        );
        ft.replace(R.id.accountDrawerSettingsLayout, fragmentRequests, "requests");
        ft.addToBackStack(null);
        ft.commit();
    }

    /**
     * Inflate the AllUsersFragment.java
     */
    private void viewAllUsersFragment() {
        ft = getSupportFragmentManager().beginTransaction();
        AllUsersFragment fragmentAllUsers = new AllUsersFragment();
        ft.setCustomAnimations(
                R.anim.fade_in,
                R.anim.slide_out
        );
        ft.replace(R.id.accountDrawerSettingsLayout, fragmentAllUsers, "all_users");
        ft.addToBackStack(null);
        ft.commit();
    }

    /**
     * Display a toast message.
     * @param message the toast message.
     */
    private void displayToast (String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * NOTE: Not in use.
     * A function to copy the UUID of the current user to the device's clipboard.
     */
    private void copyIDFun() {
        ClipboardManager clipboard = (ClipboardManager)
                getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("User ID", current_uuid);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "Copied User ID to clipboard.", Toast.LENGTH_SHORT).show();
    }

    /**
     * Go to the OnlineChat_Status.java activity.
     */
    private void goToChangeStatus() {
        Intent goToChangeStatusIntent = new Intent(this, OnlineChat_Status.class);
        startActivity(goToChangeStatusIntent);
        onStartNewActivityAnimations();
    }

    /**
     * Choose image from device's internal/removable storage.
     */
    private void pickImageFromGallery() {
        Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(galleryIntent, "SELECT IMAGE"), GALLERY_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        String current_uid = mCurrentUser.getUid();

        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setTitle("Uploading Image...");
            mProgressDialog.setMessage("Please wait while we upload the image.");
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.show();

            Uri imageUri = Uri.parse(data.getDataString());
            //Using FirebaseUser UID
            //StorageReference filepath = mImageStorage.child("profile_images").child(current_uid + ".jpg");
            //----------------------------------------------------------------------------------------------
            //Using Random UUID
            String randomUUID = null;
            randomUUID = UUID.randomUUID().toString();

            //Image Compression----------------------------
            Bitmap bmp = null;
            try {
                bmp = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.JPEG, 15, baos);
            byte[] bitmapData = baos.toByteArray();

            final StorageReference thumb_filePath_Reference = mImageStorage.child("profile_images").child("thumbs").child(randomUUID + ".jpg");
            thumb_filePath_Reference.putFile(imageUri).addOnSuccessListener(thumb_taskSnapshot -> {
                if (thumb_taskSnapshot.getMetadata() != null) {
                    if (thumb_taskSnapshot.getMetadata().getReference() != null) {
                        Task<Uri> thumb_result = thumb_taskSnapshot.getStorage().getDownloadUrl();
                        UploadTask uploadTask = thumb_filePath_Reference.putBytes(bitmapData);
                        thumb_result.addOnSuccessListener(thumb_uri -> uploadTask.addOnSuccessListener(taskSnapshot -> {
                            String thumb_url = thumb_uri.toString();
                            mUserDatabase.child("thumb_image").setValue(thumb_url).addOnCompleteListener(task -> {
                                //mProgressDialog.dismiss();
                                if (task.isSuccessful()) {
                                    Toast.makeText(AccountSettings.this, "Profile picture updated.", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(context, "Failed to update profile picture.", Toast.LENGTH_SHORT).show();
                                }
                            });
                            mProgressDialog.dismiss();
                        }));
                    }
                }
            });
            //Image Compression-----------------------------

            /*
            //Normal Image Upload-------------------------------------------
            StorageReference filepath = mImageStorage.child("profile_images").child(randomUUID + ".jpg");
            //final StorageReference thumb_filepath = mImageStorage.child("profile_images").child("thumbs").child(randomUUID + ".jpg");

            filepath.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
                if (taskSnapshot.getMetadata() != null) {
                    if (taskSnapshot.getMetadata().getReference() != null) {
                        Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
                        result.addOnSuccessListener(uri -> {
                           String imageUrl = uri.toString();
                            mUserDatabase.child("image").setValue(imageUrl).addOnCompleteListener(task -> {
                                //mProgressDialog.dismiss();
                                if (task.isSuccessful()) {
                                    Toast.makeText(AccountSettings.this, "Uploaded image successfully.", Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    Toast.makeText(context, "Failed to upload image.", Toast.LENGTH_SHORT).show();
                                }
                            });
                            mProgressDialog.dismiss();
                        });
                    }
                }
            });
            //Normal Image Upload-------------------------------------------
            */
        }
    }

    //Add animations when going into and out of activities.
    protected void onStartNewActivityAnimations() {
        overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
    }

    /**
     * Detaches the currently inflated fragment over the parent Activity when back navigation button is pressed.
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Fragment f1 = getSupportFragmentManager().findFragmentByTag("friends");
        Fragment f2 = getSupportFragmentManager().findFragmentByTag("requests");
        Fragment f3 = getSupportFragmentManager().findFragmentByTag("all_users");
        if (f1!=null) {
            ft.setCustomAnimations(
                    R.anim.fade_in,
                    R.anim.slide_out
            );
            ft.detach(f1);
        }
        else if (f2!=null) {
            ft.setCustomAnimations(
                    R.anim.fade_in,
                    R.anim.slide_out
            );
            ft.detach(f2);
        } else if (f3!=null) {
            ft.setCustomAnimations(
                    R.anim.fade_in,
                    R.anim.slide_out
            );
            ft.detach(f3);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mCurrentUser==null) {
            LogOutUser();
        } else {
            mUserDatabase.child("online").setValue(true);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mUserDatabase.child("online").setValue(false);
    }

    //Logout user
    private void LogOutUser() {
        mAuth.signOut();
        Intent intentSignOut = new Intent(AccountSettings.this, OnlineLogin.class);
        startActivity(intentSignOut);
        onStartNewActivityAnimations();
    }
}