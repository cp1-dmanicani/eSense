/*
Application Name: Smart EduBox
Created Date: Sept. 15, 2022
Company: CreatorBox Solutions
Developer: DGMJr.
*/

package com.creatorbox.esense;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

/**
 * An activity that lets the user choose between Online and Offline modes.
 */
public class ModeScreen extends AppCompatActivity implements EasyPermissions.PermissionCallbacks{

    private Button online, offline;
    private ImageView info;
    private static final int STORAGE_PERMISSIONS_CODE = 101;
    private FragmentTransaction ft;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mode_screen);

        init();
    }

    /**
     * Initialize all components.
     */
    @SuppressLint("NewApi")
    private void init() {
        info = findViewById(R.id.modeInfoButton);
        online = findViewById(R.id.onlineMode);
        offline = findViewById(R.id.offlineMode);

        online.setOnClickListener(v -> OnlineLogin());
        offline.setOnClickListener(v -> OfflineLogin());
        //offline.setOnClickListener(v -> showAlertDialog());
        info.setOnClickListener(v -> viewModeInfoFrag());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            storagePermissions();
        } else {
            storagePermissions();
        }
    }

    /**
     * This function inflates the ModeInfo fragment.
     */
    public void viewModeInfoFrag() {
        ft = getSupportFragmentManager().beginTransaction();
        ModeInfo fragment = new ModeInfo();
        ft.setCustomAnimations(
                R.anim.fade_in,
                R.anim.slide_out
        );
        ft.replace(R.id.modeView, fragment, "first");
        ft.addToBackStack(null);
        ft.commit();

        online.setVisibility(View.INVISIBLE);
        offline.setVisibility(View.INVISIBLE);
    }

    /**
     * Redirects user to the Online Login when user chooses Online Mode.
     */
    public void OnlineLogin() {
        Intent onlineLoginIntent = new Intent(this, OnlineLogin.class);
        startActivity(onlineLoginIntent);
        onStartNewActivityAnimations();
    }

    /**
     * Redirects user to the Offline Login when user chooses Offline Mode.
     */
    public void OfflineLogin() {
        Intent offlineLoginIntent = new Intent(this, OfflineLogin.class);
        startActivity(offlineLoginIntent);
        onStartNewActivityAnimations();
    }

    /**
     * Ask for nearby devices, storage and location permissions.
     */
    @RequiresApi(api = Build.VERSION_CODES.R)
    private void storagePermissions() {
        String[] permsA = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.MANAGE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.BLUETOOTH_SCAN
        };
        if (EasyPermissions.hasPermissions(ModeScreen.this, permsA)) {
            //When permissions are already granted.
            //displayToastPermissions("Storage");
        } else {
            //When permission is not granted
            EasyPermissions.requestPermissions(ModeScreen.this,
                    "App needs access to Storage, Location and Nearby Devices.",
                    STORAGE_PERMISSIONS_CODE,
                    permsA);
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        switch (requestCode) {
            case STORAGE_PERMISSIONS_CODE:
                if (perms.size() >1) {
                    //displayToastPermissions("Bluetooth");
                }
                break;
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            //When permission deny multiple times
            //Open app settings
            //new AppSettingsDialog.Builder(this).build().show();
        } else {
            Toast.makeText(getApplicationContext(), "Permissions Denied", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Fragment f = getSupportFragmentManager().findFragmentByTag("first");
        if (f!=null) {
            ft.setCustomAnimations(
                    R.anim.fade_in,
                    R.anim.slide_out
            );
            ft.detach(f);
        }
        online.setVisibility(View.VISIBLE);
        offline.setVisibility(View.VISIBLE);
    }

    //Handles animations when going in and out of Activities.
    protected void onStartNewActivityAnimations() {
        overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
    }

    /**
     * Shows a dialog containing a message.
     */
    private void showAlertDialog() {
        String message = "Offline Mode is currently under development.";
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("OFFLINE MODE")
                .setIcon(R.drawable.ic_info)
                .setMessage("\n"+ message)
                .setPositiveButton("OK", null)
                .create();
        alertDialog.show();
    }

}