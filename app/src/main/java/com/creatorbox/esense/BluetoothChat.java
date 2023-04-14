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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;
import java.util.Objects;

import pub.devrel.easypermissions.EasyPermissions;

/**
 * OFFLINE MODE
 * The activity that handles the Chat using bluetooth.
 */
public class BluetoothChat extends AppCompatActivity implements EasyPermissions.PermissionCallbacks{

    private Context context;
    private BluetoothAdapter bluetoothAdapter;
    private ChatUtils chatUtils;

    private ListView listMainChat;
    private EditText inputMessage;
    private Button sendMessage;
    private ArrayAdapter<String> adapterMainChat;
    private ActionBar ab;

    private final int BLUETOOTH_REQUEST_CODE = 101;
    private final int LOCATION_REQUEST_CODE = 100;
    private final int SELECT_DEVICE = 102;

    public static final int MESSAGE_STATE_CHANGED = 0;
    public static final int MESSAGE_READ = 1;
    public static final int MESSAGE_WRITE = 2;
    public static final int MESSAGE_DEVICE_NAME = 3;
    public static final int MESSAGE_TOAST = 4;

    public static final String DEVICE_NAME = "deviceName";
    public static final String TOAST = "toast";
    private String connectedDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_chat);

        context = this;
        initBluetooth();
        init();
        chatUtils = new ChatUtils(context, handler);
        locationPermissions();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            bluetoothPermissions();
        }
    }

    /**
     * Initialize all components.
     */
    private void init() {
        listMainChat = findViewById(R.id.listConversation);
        inputMessage = findViewById(R.id.editTextSendMessage);
        sendMessage= findViewById(R.id.sendButton);

        ab = getSupportActionBar();
        ab.setTitle("Bluetooth CHAT");

        adapterMainChat = new ArrayAdapter<String>(context, R.layout.message_layout);
        listMainChat.setAdapter(adapterMainChat);

        sendMessage.setOnClickListener(view -> {
            String message = inputMessage.getText().toString();
            if (!message.isEmpty()) {
                inputMessage.setText("");
                chatUtils.write(message.getBytes());
            }
        });
    }

    /**
     * A function the handles the state of the Bluetooth Adapter and the sending an receiving of data
     * from the other device using the app.
     */
    private final Handler handler = new Handler(new Handler.Callback() {
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
                    //Write message
                    byte[] bufferWrite = (byte[]) message.obj;
                    String writeMessage  = new String(bufferWrite);
                    adapterMainChat.add("Me: " + writeMessage );
                    break;
                case MESSAGE_READ:
                    //Read message from sender
                    byte[] bufferRead = (byte[]) message.obj;
                    String readMessage  = new String(bufferRead, 0, message.arg1);
                    adapterMainChat.add(connectedDevice + ": " + readMessage );
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
            //displayToast("Turning on Bluetooth.");
            bluetoothAdapter.enable();
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
     * Ask user for Location Permissions.
     */
    private void locationPermissions() {
        if (EasyPermissions.hasPermissions(BluetoothChat.this,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            String s = "Location";
            displayToastPermissions(s);
            if (bluetoothAdapter.isEnabled()) {

            }
            else {
                displayToast("Please turn on bluetooth before searching for devices.");
            }
        }
        else {
            EasyPermissions.requestPermissions(BluetoothChat.this,
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
        if (EasyPermissions.hasPermissions(BluetoothChat.this, perms)) {
            //When permissions are already granted.
            displayToastPermissions("Bluetooth");
        } else {
            //When permission is not granted
            EasyPermissions.requestPermissions(BluetoothChat.this,
                    "App needs access to Bluetooth",
                    BLUETOOTH_REQUEST_CODE,
                    perms);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //Handle the permissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        //Check condition if permissions are granted
        switch (requestCode) {
            case LOCATION_REQUEST_CODE:
                //displayToastPermissions("Location");
                Log.d("Permission", "Location Permissions granted.");
                break;

            case BLUETOOTH_REQUEST_CODE:
                if (perms.size() >1) {
                    //displayToastPermissions("Bluetooth");
                    Log.d("Permission", "Bluetooth Permissions granted.");
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
            String address = Objects.requireNonNull(data).getStringExtra("deviceAddress");
            //Toast.makeText(context, "Device Address: " + bluetoothAdapter.getRemoteDevice(address), Toast.LENGTH_SHORT).show();
            chatUtils.connecting(bluetoothAdapter.getRemoteDevice(address));
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

    /**
     * Display a toast to the user to notify that permission was granted.
     * @param s the toast message.
     */
    private void displayToastPermissions(String s) {
        Toast.makeText(getApplicationContext(), s+ " Permission Granted", Toast.LENGTH_SHORT).show();
    }

    /**
     * A function that displays a toast.
     * @param message the toast message.
     */
    private void displayToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Stops the chatUtils class.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (chatUtils != null) { chatUtils.stop(); }
    }

    /**
     * Alert dialog to exit activity.
     */
    private void exitApp() {
        @SuppressLint("MissingPermission")
        android.app.AlertDialog alertbox = new AlertDialog.Builder(this)
                .setMessage("End session?")
                .setPositiveButton("Yes", (arg0, arg1) -> {
                    BluetoothChat.this.finish();
                    bluetoothAdapter.cancelDiscovery();
                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    public void onBackPressed() {
        exitApp();
    }
}