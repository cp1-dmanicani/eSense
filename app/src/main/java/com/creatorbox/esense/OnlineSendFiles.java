/*
Application Name: Smart EduBox
Created Date: Sept. 15, 2022
Company: CreatorBox Solutions
Developer: DGMJr.
*/

package com.creatorbox.esense;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

/**
 * ONLINE MODE
 * An activity that handles the Online File Transfer.
 */
public class OnlineSendFiles extends AppCompatActivity{

    private final String firebaseDBURL = "https://smart-edubox-90c5d-default-rtdb.asia-southeast1.firebasedatabase.app/";
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase database;
    private DatabaseReference databaseReference;
    private FirebaseUser currentUser;
    private StorageReference storageReference;

    private ProgressDialog mProgressDialog;

    private static final int IMAGE_PICKER = 101;
    private static final int PDF_PICKER = 102;
    private static final int TEXT_PICKER = 103;

    OnlineChat_FileTransferAdapter messageAdapter;
    RecyclerView fileTransferThread;
    Button attach;
    List<OnlineChat_ModelClass> list;

    String userNameSend, otherNameSend, otherNameSendID, userNameSendID, other_name, image_url, status, joined_date;
    private String checker = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.online_send_files);

        init();
    }

    /**
     * Initializes all components.
     */
    private void init() {
        attach = (Button) findViewById(R.id.attachFilesBtn);
        fileTransferThread = (RecyclerView) findViewById(R.id.fileTransferThreadRecyclerview);

        userNameSendID = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        userNameSend = getIntent().getStringExtra("userNameSend");
        otherNameSend = getIntent().getStringExtra("otherNameSend");
        otherNameSendID = getIntent().getStringExtra("otherNameSendID");

        other_name = getIntent().getStringExtra("other_name");
        image_url = getIntent().getStringExtra("image_url");
        status = getIntent().getStringExtra("status");
        joined_date = getIntent().getStringExtra("joined_date");

        database = FirebaseDatabase.getInstance(firebaseDBURL);
        databaseReference = database.getReference();

        storageReference = FirebaseStorage.getInstance().getReference();

        fileTransferThread.setLayoutManager(new LinearLayoutManager(OnlineSendFiles.this));
        list = new ArrayList<>();

        messageAdapter = new OnlineChat_FileTransferAdapter(this, list, userNameSendID);
        fileTransferThread.setAdapter(messageAdapter);

        ActionBar ab = getSupportActionBar();
        ab.setTitle("FILE TRANSFER");
        ab.setSubtitle("Receiver: " + otherNameSend);

        attach.setOnClickListener(view -> {
            CharSequence[] options = new CharSequence[]{
              "Image",
              "PDF (.pdf)",
              "Text (.txt)"
            };
            AlertDialog.Builder builder = new AlertDialog.Builder(OnlineSendFiles.this);
            builder.setTitle("Select File");
            builder.setItems(options, (dialog, which) -> {
                switch (which) {
                    case 0:
                        checker = "image";
                        Intent intentImage = new Intent();
                        intentImage.setAction(Intent.ACTION_GET_CONTENT);
                        intentImage.setType("image/*");
                        intentImage = Intent.createChooser(intentImage, "Select Image");
                        startActivityForResult(intentImage, IMAGE_PICKER);
                        break;
                    case 1:
                        checker = "pdf";
                        Intent intentPdf = new Intent();
                        intentPdf.setAction(Intent.ACTION_GET_CONTENT);
                        intentPdf.setType("application/pdf");
                        intentPdf = Intent.createChooser(intentPdf, "Select PDF");
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
        });
        getMessage();
    }

    /**
     * Generate a random 6 number value to be used in naming the file going to be sent.
     * @return
     */
    public static String getRandomNumberString() {
        // It will generate 6 digit random Number.
        // from 0 to 999999
        Random rnd = new Random();
        int number = rnd.nextInt(999999);

        // this will convert any number sequence into 6 character.
        return String.format("%06d", number);
    }

    /**
     * Retrieve all files sent and received during the file transfer with other users.
     */
    private void getMessage() {
        databaseReference.keepSynced(true);
        databaseReference.child("FileTransfer").child(userNameSendID).child(otherNameSendID)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        OnlineChat_ModelClass modelClass = snapshot.getValue(OnlineChat_ModelClass.class);
                        list.add(modelClass);
                        messageAdapter.notifyDataSetChanged();
                        fileTransferThread.scrollToPosition(list.size()-1);
                    }
                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        String key = databaseReference.child("FileTransfer").child(userNameSendID).child(otherNameSendID).push().getKey();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MM-yyyy");
        String date = currentDate.format(new Date());
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss z");
        SimpleDateFormat currentTimeNoTimeZone = new SimpleDateFormat("HH:mm:ss");
        String time = currentTime.format(new Date());
        String timeNoZ = currentTimeNoTimeZone.format(new Date());

        mProgressDialog = new ProgressDialog(OnlineSendFiles.this);

        switch (requestCode) {
            case IMAGE_PICKER:
                if (resultCode == RESULT_OK && data != null && data.getData() != null) {
                    if (!checker.equals("image")) {
                        Toast.makeText(this, "File selected is not Image", Toast.LENGTH_SHORT).show();
                    }
                    else if (checker.equals("image")) {
                        androidx.appcompat.app.AlertDialog alertDialog = new androidx.appcompat.app.AlertDialog.Builder(this)
                                .setTitle("Send File")
                                .setMessage("Send Image?")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        progressDialog(OnlineSendFiles.this,
                                                "Sending Image",
                                                "Please wait while we send the image.");

                                        Uri imageUri = Uri.parse(data.getDataString());
                                        DocumentFile file = DocumentFile.fromSingleUri(OnlineSendFiles.this, imageUri);
                                        String fileName = file.getName();

                                        Bitmap bmp = null;
                                        try {
                                            bmp = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        ByteArrayOutputStream baosA = new ByteArrayOutputStream();
                                        bmp.compress(Bitmap.CompressFormat.JPEG, 50, baosA);
                                        byte[] bitmapData = baosA.toByteArray();

                                        final StorageReference imagePath_reference = storageReference.child("uploaded_files").child("image_files").child(getRandomNumberString() + "_" + fileName);
                                        imagePath_reference.putFile(imageUri).addOnSuccessListener(file_taskSnapshot -> {
                                            if (file_taskSnapshot.getMetadata() != null) {
                                                if (file_taskSnapshot.getMetadata().getReference() != null) {
                                                    Task<Uri> result = file_taskSnapshot.getStorage().getDownloadUrl();
                                                    UploadTask uploadTask = imagePath_reference.putBytes(bitmapData);
                                                    result.addOnSuccessListener(image_uri -> uploadTask.addOnSuccessListener(taskSnapshot -> {
                                                        String image_url = image_uri.toString();

                                                        Map<String, Object> messageMap = new HashMap<>();
                                                        messageMap.put("message", image_url);
                                                        messageMap.put("from", userNameSendID);
                                                        messageMap.put("type", checker);
                                                        messageMap.put("date", date);
                                                        messageMap.put("time", time);

                                                        databaseReference.child("FileTransfer").child(userNameSendID).child(otherNameSendID).child(key).setValue(messageMap).addOnCompleteListener(task -> {
                                                            if (task.isSuccessful()) {
                                                                databaseReference.child("FileTransfer").child(otherNameSendID).child(userNameSendID).child(key).setValue(messageMap);
                                                                Toast.makeText(OnlineSendFiles.this, "Image sent.", Toast.LENGTH_SHORT).show();
                                                            }
                                                            else {
                                                                Toast.makeText(OnlineSendFiles.this, "Image not sent.", Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                                        mProgressDialog.dismiss();
                                                    }));
                                                }
                                            }
                                        });
                                    }
                                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                }).create();
                        alertDialog.show();
                    }
                    else {
                        Toast.makeText(this, "No image selected.", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case PDF_PICKER:
                if (resultCode == RESULT_OK && data != null && data.getData() != null) {
                    if (!checker.equals("pdf")) {
                        Toast.makeText(this, "File selected is not PDF", Toast.LENGTH_SHORT).show();
                    }
                    else if (checker.equals("pdf")) {

                        androidx.appcompat.app.AlertDialog alertDialog = new androidx.appcompat.app.AlertDialog.Builder(this)
                                .setTitle("Sending File")
                                .setMessage("Send PDF File?")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        progressDialog(OnlineSendFiles.this,
                                                "Sending PDF",
                                                "Please wait while we send the PDF.");

                                        Uri pdfUri = data.getData();
                                        DocumentFile file = DocumentFile.fromSingleUri(OnlineSendFiles.this, pdfUri);
                                        String fileName = file.getName();

                                        final StorageReference pdfPath_reference = storageReference.child("uploaded_files").child("pdf_files").child(getRandomNumberString() + "_" + fileName);
                                        pdfPath_reference.putFile(pdfUri).addOnSuccessListener(file_taskSnapshot -> {
                                            if (file_taskSnapshot.getMetadata() != null) {
                                                if (file_taskSnapshot.getMetadata().getReference() != null) {
                                                    Task<Uri> result = file_taskSnapshot.getStorage().getDownloadUrl();
                                                    UploadTask uploadTask = pdfPath_reference.putFile(pdfUri);
                                                    result.addOnSuccessListener(pdf_uri -> uploadTask.addOnSuccessListener(taskSnapshot -> {
                                                        String pdf_url = pdf_uri.toString();

                                                        Map<String, Object> messageMap = new HashMap<>();
                                                        messageMap.put("message", pdf_url);
                                                        messageMap.put("from", userNameSendID);
                                                        messageMap.put("type", checker);
                                                        messageMap.put("date", date);
                                                        messageMap.put("time", time);

                                                        databaseReference.child("FileTransfer").child(userNameSendID).child(otherNameSendID).child(key).setValue(messageMap).addOnCompleteListener(task -> {
                                                            if (task.isSuccessful()) {
                                                                databaseReference.child("FileTransfer").child(otherNameSendID).child(userNameSendID).child(key).setValue(messageMap);
                                                                Toast.makeText(OnlineSendFiles.this, "PDF File sent.", Toast.LENGTH_SHORT).show();
                                                            }
                                                            else {
                                                                Toast.makeText(OnlineSendFiles.this, "PDF File not sent.", Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                                        mProgressDialog.dismiss();
                                                    }));
                                                }
                                            }
                                        });
                                    }
                                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                }).create();
                        alertDialog.show();
                    }
                    else {
                        Toast.makeText(this, "No PDF file selected.", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case TEXT_PICKER:
                if (resultCode == RESULT_OK && data != null && data.getData() != null) {
                    if (!checker.equals("text")) {
                        Toast.makeText(this, "File selected is not Text", Toast.LENGTH_SHORT).show();
                    }
                    else if (checker.equals("text")) {

                        androidx.appcompat.app.AlertDialog alertDialog = new androidx.appcompat.app.AlertDialog.Builder(this)
                                .setTitle("Sending File")
                                .setMessage("Send Text File?")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        progressDialog(OnlineSendFiles.this,
                                                "Sending Text File",
                                                "Please wait while we send the Text File.");

                                        Uri textUri = data.getData();
                                        DocumentFile file = DocumentFile.fromSingleUri(OnlineSendFiles.this, textUri);
                                        String fileName = file.getName();

                                        final StorageReference textPath_reference = storageReference.child("uploaded_files").child("text_files").child(getRandomNumberString()+ "_" + fileName);
                                        textPath_reference.putFile(textUri).addOnSuccessListener(file_taskSnapshot -> {
                                            if (file_taskSnapshot.getMetadata() != null) {
                                                if (file_taskSnapshot.getMetadata().getReference() != null) {
                                                    Task<Uri> result = file_taskSnapshot.getStorage().getDownloadUrl();
                                                    UploadTask uploadTask = textPath_reference.putFile(textUri);
                                                    result.addOnSuccessListener(text_uri -> uploadTask.addOnSuccessListener(taskSnapshot -> {
                                                        String text_url = text_uri.toString();

                                                        Map<String, Object> messageMap = new HashMap<>();
                                                        messageMap.put("message", text_url);
                                                        messageMap.put("from", userNameSendID);
                                                        messageMap.put("type", checker);
                                                        messageMap.put("date", date);
                                                        messageMap.put("time", time);

                                                        databaseReference.child("FileTransfer").child(userNameSendID).child(otherNameSendID).child(key).setValue(messageMap).addOnCompleteListener(task -> {
                                                            if (task.isSuccessful()) {
                                                                databaseReference.child("FileTransfer").child(otherNameSendID).child(userNameSendID).child(key).setValue(messageMap);
                                                                Toast.makeText(OnlineSendFiles.this, "Text File sent.", Toast.LENGTH_SHORT).show();
                                                            }
                                                            else {
                                                                Toast.makeText(OnlineSendFiles.this, "Text File not sent.", Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                                        mProgressDialog.dismiss();
                                                    }));
                                                }
                                            }
                                        });
                                    }
                                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                }).create();
                        alertDialog.show();
                    }
                    else {
                        Toast.makeText(this, "No Text file selected.", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }

    /**
     * Show a progress dialog.
     * @param context interface context.
     * @param title title of the progress dialog.
     * @param message message of the progress dialog.
     */
    private void progressDialog(Context context, String title, String message) {
        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setTitle(title);
        mProgressDialog.setMessage(message);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onStart() {
        super.onStart();
        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "No signed-in user. Please sign in first.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, OnlineLogin.class);
            startActivity(intent);
            finish();
        }
    }

    //Handles the animation when going from activity to activity.
    protected void onStartNewActivityAnimations() {
        overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, OnlineSend_Users.class);
        startActivity(intent);
        this.finish();
    }
}