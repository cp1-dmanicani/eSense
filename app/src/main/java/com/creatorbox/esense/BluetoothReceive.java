/*
Application Name: Smart EduBox
Created Date: Sept. 15, 2022
Company: CreatorBox Solutions
Developer: DGMJr.
*/

package com.creatorbox.esense;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.List;
import java.util.Objects;

import pub.devrel.easypermissions.EasyPermissions;

/**
 * OFFLINE MODE
 * The activity that handles the Receiving of Files from other app users using bluetooth.
 */
public class BluetoothReceive extends AppCompatActivity implements EasyPermissions.PermissionCallbacks{


    private BluetoothAdapter bluetoothAdapter;
    private SendUtils receiveUtils;

    private Context context;
    private TextView fileName;
    private ActionBar ab;

    private ArrayAdapter<String> adapterMainReceive;

    private final int BLUETOOTH_REQUEST_CODE = 101;
    private final int LOCATION_REQUEST_CODE = 100;
    private final int SELECT_DEVICE = 102;
    private static final int STORAGE_REQUEST_CODE = 103;

    public static final int MESSAGE_STATE_CHANGED = 0;
    public static final int MESSAGE_READ = 1;
    public static final int MESSAGE_WRITE = 2;
    public static final int MESSAGE_DEVICE_NAME = 3;
    public static final int MESSAGE_TOAST = 4;

    public static final String DEVICE_NAME = "deviceName";
    public static final String TOAST = "toast";
    private String connectedDevice;

    public static String FILEPATH = "";
    public static File file = new File(FILEPATH);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_receive);

        context = this;
        initBluetooth();
        init();
        receiveUtils = new SendUtils(context, handler);
        locationPermissions();
        storagePermissions();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            bluetoothPermissions();
        }
    }

    /**
     * Initialize all components.
     */
    private void init() {
        ab = getSupportActionBar();
        ab.setTitle("Bluetooth RECEIVE");

        if (!bluetoothAdapter.isEnabled()) {
            displayToast("Bluetooth is OFF. Please enable it first to initiate a file transfer.");
        }
    }

    /**
     * A function the handles the state of the Bluetooth Adapter and the sending an receiving of data
     * from the other device using the app.
     */
    private final Handler handler = new Handler(new Handler.Callback() {
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public boolean handleMessage(@NonNull Message message) {
            switch (message.what) {
                case MESSAGE_STATE_CHANGED:
                    switch (message.arg1) {
                        case ChatUtils.STATE_NONE:
                        case ChatUtils.STATE_LISTEN:
                            setState("Not Connected");
                            break;
                        case ChatUtils.STATE_CONNECTING:
                            setState("Connecting");
                            break;
                        case ChatUtils.STATE_CONNECTED:
                            setState("Connected with: " + connectedDevice);
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    //
                    break;
                case MESSAGE_READ:
                    //Read message from sender
                    byte[] receivedByte = (byte[]) message.obj;
                    int byteLength = receivedByte.length;
                    Log.d("MESSAGE_READ", String.valueOf(byteLength));
                    /*
                    if (receivedByte.length > 0) {
                        String readMessage  = new String(receivedByte, 0, message.arg1);
                        //String readMessage  = Base64.encodeToString(receivedByte, Base64.DEFAULT);
                        Log.d("readMessage", readMessage);
                        byte[] imageBytes = Base64.decode(readMessage, Base64.NO_WRAP);
                        Bitmap decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                        fileView.setImageBitmap(decodedImage);
                    } else {
                        displayToast("No byte array received.");
                    }*/
                    break;
                case MESSAGE_DEVICE_NAME:
                    connectedDevice = message.getData().getString(DEVICE_NAME);
                    displayToast(connectedDevice);
                    break;
                case MESSAGE_TOAST:
                    displayToast(message.getData().getString(TOAST));
                    break;
            }
            return false;
        }
    });

    /**
     * Set the subtitle of the ActionBar
     * @param subTitle indicates the status of the bluetooth connection that is retrieved by the handler.
     */
    private void setState(CharSequence subTitle) {
        Objects.requireNonNull(getSupportActionBar()).setSubtitle(subTitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.bluetoothchat_menu, menu);
        menu.findItem(R.id.menu_search_devices).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_search_devices:
                goToDeviceListForm();
                return true;
            case R.id.menu_enable_bluetooth:
                enableBluetooth();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Enable bluetooth
     */
    @SuppressLint("MissingPermission")
    private void enableBluetooth() {
        if (!bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable();
            displayToast("Turning on Bluetooth.");
        }
        if (bluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoveryIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoveryIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoveryIntent);
        }
    }

    /**
     * Initialize bluetooth adapter.
     */
    private void initBluetooth() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            displayToast("No bluetooth device found on this device.");
            finish();
            return;
        }
    }

    /**
     * Ask user for Storage Permissions.
     */
    private void storagePermissions() {
        String[] permsA = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        if (EasyPermissions.hasPermissions(BluetoothReceive.this, permsA)) {
            //When permissions are already granted.
            //displayToastPermissions("Storage");
        } else {
            //When permission is not granted
            EasyPermissions.requestPermissions(BluetoothReceive.this,
                    "App needs access to Storage",
                    STORAGE_REQUEST_CODE,
                    permsA);
        }
    }

    /**
     * Ask user for Location Permissions.
     */
    private void locationPermissions() {
        if (EasyPermissions.hasPermissions(BluetoothReceive.this,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            String s = "Location";
            //displayToastPermissions(s);
        }
        else {
            EasyPermissions.requestPermissions(BluetoothReceive.this,
                    "App needs access to your location",
                    LOCATION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    /**
     * Ask user for Nearby Devices permission.
     */
    @RequiresApi(api = Build.VERSION_CODES.S)
    private void bluetoothPermissions() {
        String[] perms = {
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.BLUETOOTH_SCAN
        };
        if (EasyPermissions.hasPermissions(BluetoothReceive.this, perms)) {
            //When permissions are already granted.
            displayToastPermissions("Bluetooth");
        } else {
            //When permission is not granted
            EasyPermissions.requestPermissions(BluetoothReceive.this,
                    "App needs access to Bluetooth",
                    BLUETOOTH_REQUEST_CODE,
                    perms);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    private void displayToastPermissions(String s) {
        Toast.makeText(getApplicationContext(), s+ " Permission Granted", Toast.LENGTH_SHORT).show();
    }

    private void displayToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        //Check condition if permissions are granted
        switch (requestCode) {
            case LOCATION_REQUEST_CODE:
                //displayToastPermissions("Location");
                //Log.d("Permission", "Location Permissions granted.");
                break;

            case STORAGE_REQUEST_CODE:
            case BLUETOOTH_REQUEST_CODE:
                if (perms.size() >1) {
                    //displayToastPermissions("Bluetooth");
                    //Log.d("Permission", "Bluetooth Permissions granted.");
                }
                break;
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        //Check condition if permissions are denied
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            //When permission deny multiple times
            //Open app settings
            //new AppSettingsDialog.Builder(this).build().show();
        } else {
            Toast.makeText(getApplicationContext(), "Permissions Denied", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == SELECT_DEVICE && resultCode == RESULT_OK) {
            String address = data.getStringExtra("deviceAddress");
            Toast.makeText(context, "Device Address: " + bluetoothAdapter.getRemoteDevice(address), Toast.LENGTH_SHORT).show();
            receiveUtils.connecting(bluetoothAdapter.getRemoteDevice(address));
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Go to the DeviceListActivity to choose the bluetooth device you wish to connect to.
     */
    private void goToDeviceListForm() {
        Intent intent = new Intent(context, DeviceListActivity.class);
        startActivityForResult(intent, SELECT_DEVICE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (receiveUtils != null) { receiveUtils.stop(); }
    }

    /**
     * Alert dialog to exit activity.
     */
    private void exitApp() {
        AlertDialog alertbox = new AlertDialog.Builder(this)
                .setMessage("End Session?")
                .setPositiveButton("Yes", (arg0, arg1) -> {
                    BluetoothReceive.this.finish();
                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    public void onBackPressed() {
        exitApp();
    }
}