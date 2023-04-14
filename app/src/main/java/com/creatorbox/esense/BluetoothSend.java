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
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.hbisoft.pickit.PickiT;
import com.hbisoft.pickit.PickiTCallbacks;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import pub.devrel.easypermissions.EasyPermissions;

/**
 * OFFLINE MODE
 * The activity that handles the Sending of Files to other app users using bluetooth.
 */
public class BluetoothSend extends AppCompatActivity implements EasyPermissions.PermissionCallbacks, PickiTCallbacks{

    private BluetoothAdapter bluetoothAdapter;
    private SendUtils sendUtils;

    private Context context;
    private Button send, attachFile;
    private TextView fileName;
    private ImageView fileImage;
    private ActionBar ab;

    private final int BLUETOOTH_REQUEST_CODE = 101;
    private final int LOCATION_REQUEST_CODE = 100;
    private final int STORAGE_REQUEST_CODE = 103;
    private final int SELECT_DEVICE = 102;

    private static final int IMAGE_PICKER = 104;
    private static final int PDF_PICKER = 105;
    private static final int TEXT_PICKER = 106;


    public static final int MESSAGE_STATE_CHANGED = 0;
    public static final int MESSAGE_READ = 1;
    public static final int MESSAGE_WRITE = 2;
    public static final int MESSAGE_DEVICE_NAME = 3;
    public static final int MESSAGE_TOAST = 4;

    public static final String DEVICE_NAME = "deviceName";
    public static final String TOAST = "toast";
    private String connectedDevice, filePath, encodedImage;

    PickiT pickiT;

    Uri pdfUri, imageUri, txtUri;
    private String checker = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_send);

        context = this;
        initBluetooth();
        init();
        //sendUtils = new SendUtils(context, handler);
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
        send = findViewById(R.id.sendButtonSend);
        attachFile = findViewById(R.id.attachFileButton);
        fileName = findViewById(R.id.textFilePathImage);
        fileImage = findViewById(R.id.fileImageViewSend);

        ab = getSupportActionBar();
        ab.setTitle("Bluetooth SEND");

        pickiT = new PickiT(this, this, this);

        attachFile.setOnClickListener(view -> {
            if (!bluetoothAdapter.isEnabled()) {
                AlertDialog alertbox = new AlertDialog.Builder(this)
                        .setMessage("Please enable bluetooth first.")
                        .setPositiveButton("OK", null)
                        .setNegativeButton("Cancel", null)
                        .show();
            }
            else {
                CharSequence[] options = new CharSequence[]{
                        "Image",
                        "PDF (.pdf)",
                        "Text (.txt)"
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(BluetoothSend.this);
                builder.setTitle("Select File");
                builder.setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            checker = "image";
                            Intent intentImage = new Intent();
                            intentImage.setAction(Intent.ACTION_GET_CONTENT);
                            intentImage.setType("image/*");
                            intentImage = Intent.createChooser(intentImage, "Select Image.");
                            startActivityForResult(intentImage, IMAGE_PICKER);
                            break;
                        case 1:
                            checker = "pdf";
                            Intent intentPdf = new Intent();
                            intentPdf.setAction(Intent.ACTION_GET_CONTENT);
                            intentPdf.setType("application/*");
                            intentPdf = Intent.createChooser(intentPdf, "Select PDF.");
                            startActivityForResult(intentPdf, PDF_PICKER);
                            break;
                        case 2:
                            checker = "text";
                            Intent intentText = new Intent();
                            intentText.setAction(Intent.ACTION_GET_CONTENT);
                            intentText.setType("text/*");
                            intentText = Intent.createChooser(intentText, "Select Text File.");
                            startActivityForResult(intentText, TEXT_PICKER);
                    }
                }).show();
            }
        });

        send.setOnClickListener(view -> {
            if (fileName.getText().equals("...")) {
                AlertDialog alertbox = new AlertDialog.Builder(this)
                        .setMessage("Please attach a file first.")
                        .setPositiveButton("OK", null)
                        .setNegativeButton("Cancel", null)
                        .show();
            }
            else {
                /*
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    Path path = Paths.get(filePath);
                    try {
                        byte[] pdfByteArray = Files.readAllBytes(path);
                        sendUtils.write(pdfByteArray);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }*/

                AlertDialog alertbox = new AlertDialog.Builder(this)
                        .setMessage("Send file?")
                        .setPositiveButton("OK", (dialog, which) -> shareFileUsingIntent(filePath))
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });
    }

    /**
     * WIP:
     * Sends the byte array of the file to the receiving device.
     * @param path The file path of the chosen file to be sent.
     */
    private void sendBytesToReceiver(String path) {
        File file = new File(path);
        Uri uri = FileProvider.getUriForFile(
                this,
                getApplicationContext().getPackageName() + ".fileprovider",
                file
        );
        if (file.exists()) {
            try {
                //Convert bitmap from URI to byte[], then encode it to Base64 String then send it to the receiver.
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] bitmapBytes = stream.toByteArray();
                encodedImage = Base64.encodeToString(bitmapBytes, Base64.DEFAULT);
                sendUtils.write(encodedImage.getBytes(StandardCharsets.UTF_8));

                int subArraySize = 400;

                for (int i=0; i<bitmapBytes.length; i+=subArraySize) {
                    byte[] tempArray;
                    tempArray = Arrays.copyOfRange(bitmapBytes, i, Math.min(bitmapBytes.length, i+subArraySize));
                    sendUtils.write(tempArray);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            displayToast("File doesn't exist.");
        }
    }

    /**
     * Returns a string with a value of the MIME type of the given Uri value.
     * @param uri the Uri value of the file or path when this method is called.
     * @return returns the String value of this method.
     */
    public String getMimeType(Uri uri) {
        String mimeType;
        if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
            ContentResolver cr = getContentResolver();
            mimeType = cr.getType(uri);
        } else {
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri
                    .toString());
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    fileExtension.toLowerCase());
        }
        return mimeType;
    }

    /**
     * Sends the file using Android's Bluetooth FTP.
     */
    private void shareFileUsingIntent(String path) {
        File fileToBeSent = new File(path);
        String mimeType = getMimeType(Uri.fromFile(fileToBeSent));
        Uri uri = FileProvider.getUriForFile(
                BluetoothSend.this,
                getApplicationContext().getPackageName() + ".fileprovider",
                fileToBeSent
        );
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.setPackage("com.android.bluetooth");
        sendIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
        sendIntent.setType(mimeType);
        startActivity(Intent.createChooser(sendIntent, "Share file"));
    }

    /**
     * A function the handles the state of the Bluetooth Adapter and the sending an receiving of data
     * from the other device using the app.
     */
    /*
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
                    break;
                case MESSAGE_READ:
                    //Read message from sender
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
     */

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
                if (!bluetoothAdapter.isEnabled()) {
                    AlertDialog alertbox = new AlertDialog.Builder(this)
                            .setMessage("Please enable bluetooth first.")
                            .setPositiveButton("OK", null)
                            .setNegativeButton("Cancel", null)
                            .show();
                }
                else {
                    goToDeviceListForm();
                }
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
    @SuppressLint("MissingPermission")
    private void initBluetooth() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            displayToast("No bluetooth device found on this device.");
            finish();
        }
    }

    /**
     * Ask user for Location Permissions.
     */
    private void locationPermissions() {
        if (EasyPermissions.hasPermissions(BluetoothSend.this,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            //String s = "Location";
            //displayToastPermissions(s);
        }
        else {
            EasyPermissions.requestPermissions(BluetoothSend.this,
                    "App needs access to your location",
                    LOCATION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION);
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
        if (EasyPermissions.hasPermissions(BluetoothSend.this, permsA)) {
            //When permissions are already granted.
            //displayToastPermissions("Storage");
        } else {
            //When permission is not granted
            EasyPermissions.requestPermissions(BluetoothSend.this,
                    "App needs access to Storage",
                    STORAGE_REQUEST_CODE,
                    permsA);
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
        if (EasyPermissions.hasPermissions(BluetoothSend.this, perms)) {
            //When permissions are already granted.
            //displayToastPermissions("Bluetooth");
        } else {
            //When permission is not granted
            EasyPermissions.requestPermissions(BluetoothSend.this,
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

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        //Check condition if permissions are granted
        switch (requestCode) {
            case LOCATION_REQUEST_CODE:
                //displayToastPermissions("Location");
                break;
            case BLUETOOTH_REQUEST_CODE:
            case STORAGE_REQUEST_CODE:
                if (perms.size() >1) {
                    //displayToastPermissions("Bluetooth");
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
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            /*
            case SELECT_DEVICE:
                if (resultCode == RESULT_OK) {
                    String address = data.getStringExtra("deviceAddress");
                    Toast.makeText(context, "Device Address: " + bluetoothAdapter.getRemoteDevice(address), Toast.LENGTH_SHORT).show();
                    sendUtils.connecting(bluetoothAdapter.getRemoteDevice(address));
                }
                break;*/
            case IMAGE_PICKER:
                if (resultCode == RESULT_OK) {
                    imageUri = Objects.requireNonNull(data).getData();
                    pickiT.getPath(imageUri, Build.VERSION.SDK_INT);
                    File fileImg = new File(filePath);

                    if (isFileLessThan5MB(fileImg)) {
                        AlertDialog alertbox = new AlertDialog.Builder(BluetoothSend.this)
                                .setTitle("File too large.")
                                .setMessage("Selected file is larger than 5MB")
                                .setPositiveButton("OK", (arg0, arg1) -> {
                                    displayToast("Please choose another file.");
                                    fileName.setText("...");
                                    Picasso.get().load(R.drawable.drive_file)
                                            .placeholder(R.drawable.drive_file).into(fileImage);
                                })
                                .show();
                    }
                    else {
                        try {
                            Glide.with(BluetoothSend.this)
                                    .load(imageUri)
                                    .placeholder(R.drawable.drive_file)
                                    .error(R.drawable.ic_info)
                                    .into(fileImage);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                break;
            case PDF_PICKER:
                if (resultCode == RESULT_OK) {
                    pdfUri = Objects.requireNonNull(data).getData();
                    pickiT.getPath(pdfUri, Build.VERSION.SDK_INT);
                    Picasso.get().load(R.drawable.ic_pdf).placeholder(R.drawable.ic_pdf).into(fileImage);
                }
                break;
            case TEXT_PICKER:
                if (resultCode == RESULT_OK) {
                    txtUri = Objects.requireNonNull(data).getData();
                    pickiT.getPath(txtUri, Build.VERSION.SDK_INT);
                    Picasso.get().load(R.drawable.ic_text_snippet).placeholder(R.drawable.ic_text_snippet).into(fileImage);
                }
                break;
        }
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
        if (!isChangingConfigurations()) {
            pickiT.deleteTemporaryFile(this);
        }
    }

    /**
     * Alert dialog to exit activity.
     */
    private void exitApp() {
        AlertDialog alertbox = new AlertDialog.Builder(this)
                .setMessage("End session?")
                .setPositiveButton("Yes", (arg0, arg1) -> {
                    BluetoothSend.this.finish();
                })
                .setNegativeButton("No", null)
                .show();
        pickiT.deleteTemporaryFile(this);
    }

    @Override
    public void onBackPressed() {
        exitApp();
    }

    /**
     * A boolean function that prevents the user from selecting a file larger than 5MB
     * @param file the file selected by the user.
     * @return returns a boolean value.
     */
    private boolean isFileLessThan5MB(File file) {
        int maxFileSize = 5 * 1024 * 1024;
        long l = file.length();
        String fileSize = Long.toString(l);
        int finalFileSize = Integer.parseInt(fileSize);
        return finalFileSize >= maxFileSize;
    }

    @Override
    public void PickiTonUriReturned() {
        //
    }

    @Override
    public void PickiTonStartListener() {
        //
    }

    @Override
    public void PickiTonProgressUpdate(int progress) {
        //
    }

    /**
     * Uses PickIt to retrieve the actual file path of the chosen file by the user.
     * @param path
     * @param wasDriveFile
     * @param wasUnknownProvider
     * @param wasSuccessful
     * @param Reason
     */
    @Override
    public void PickiTonCompleteListener(String path, boolean wasDriveFile, boolean wasUnknownProvider, boolean wasSuccessful, String Reason) {
        //  Chick if it was successful
        if (wasSuccessful) {
            //  Set returned path to TextView
            if (path.contains("/proc/")) {
                fileName.setText("Sub-directory inside Downloads was selected." + "\n" + " We will be making use of the /proc/ protocol." + "\n" + " You can use this path as you would normally." + "\n\n" + "PickiT path:" + "\n" + path);
            } else {
                fileName.setText(path.substring(path.lastIndexOf('/') +1));
                filePath = path;
                String[] invalidChars = {"~", "'"};
                if (path.contains(invalidChars[0]) || path.contains(invalidChars[1])) {
                    AlertDialog alertDialog = new AlertDialog.Builder(BluetoothSend.this)
                            .setTitle("Invalid File Name")
                            .setMessage("File name contains these special characters ( ~  ' ). Please remove characters by renaming file and attach again.")
                            .setPositiveButton("OK", (dialog, which) -> {
                                fileName.setText("...");
                                Picasso.get().load(R.drawable.drive_file)
                                        .placeholder(R.drawable.drive_file).into(fileImage);
                            })
                            .setCancelable(false)
                            .show();
                }
            }
        } else {
            showLongToast("Error, please see the log..");
            fileName.setText(Reason);
        }
    }

    @Override
    public void PickiTonMultipleCompleteListener(ArrayList<String> paths, boolean wasSuccessful, String Reason) {
        //
    }

    /**
     * A function that displays a toast with a long duration.
     * @param msg the toast message.
     */
    private void showLongToast(final String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
    }
}