package com.creatorbox.esense;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * ONLINE MODE
 * The adapter that handles the RecyclerView of the OnlineSendFiles.
 */
public class OnlineChat_FileTransferAdapter extends  RecyclerView.Adapter<OnlineChat_FileTransferAdapter.MessageViewHolder>{

    Context context;
    List<OnlineChat_ModelClass> list;
    String name;

    boolean status;
    int send;
    int receive;

    FirebaseDatabase database;
    DatabaseReference reference;
    StorageReference storageReference;
    private final String firebaseDBURL = "https://smart-edubox-90c5d-default-rtdb.asia-southeast1.firebasedatabase.app/";

    public OnlineChat_FileTransferAdapter(Context context, List<OnlineChat_ModelClass> list, String name) {
        this.context = context;
        this.list = list;
        this.name = name;

        status = false;
        send = 1;
        receive = 2;

        database = FirebaseDatabase.getInstance(firebaseDBURL);
        reference = database.getReference();
    }

    @NonNull
    @Override
    public OnlineChat_FileTransferAdapter.MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v;
        if (viewType == send) {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.onlineftp_send, parent, false);
        }
        else {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.onlineftp_receive, parent, false);
        }
        return new MessageViewHolder(v);
    }

    public void viewFileInBrowser(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        context.startActivity(intent);
    }

    public void downloadImageFile(String url, String filename, String fileNamePath) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setDescription("Downloading Image");
        request.setTitle(filename);
        // in order for this if to run, you must use the android 3.2 to compile your app
        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileNamePath);
        // get download service and enqueue file
        DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(request);
    }

    public void downloadOtherFile(String url, String filename, String fileNamePath) {
        DownloadManager.Request requestA = new DownloadManager.Request(Uri.parse(url));
        requestA.setDescription("Downloading File");
        requestA.setTitle(filename);
        // in order for this if to run, you must use the android 3.2 to compile your app
        requestA.allowScanningByMediaScanner();
        requestA.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        requestA.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileNamePath);
        // get download service and enqueue file
        DownloadManager managerA = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        managerA.enqueue(requestA);
    }

    /**
     * @param url downloadUrl of file from FirebaseStorage.
     * Show an alert dialog when opening file via browser to avoid accidental
     * redirecting on the user's end.
     */
    private void showAlertDialog(String url) {
        AlertDialog alertDialog = new AlertDialog.Builder(context)
                .setTitle("OPENING FILE IN BROWSER")
                .setMessage("Open File?")
                .setPositiveButton("OK", (dialog, which) ->
                        viewFileInBrowser(url))
                .setNegativeButton("Cancel", (dialog, which) ->
                        dialog
                                .dismiss()).create();
        alertDialog.show();
    }

    public void displayToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBindViewHolder(@NonNull OnlineChat_FileTransferAdapter.MessageViewHolder holder, int position) {
        String messageUrl = list.get(position).getMessage();
        storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(messageUrl);
        String fileName = storageReference.getName();
        String type = list.get(position).getType();

        switch (type) {
            case "image":
                holder.textView.setText(fileName);
                Picasso.get().load(messageUrl).networkPolicy(NetworkPolicy.OFFLINE)
                        .placeholder(R.drawable.ic_person).into(holder.imageView, new Callback() {
                            @Override
                            public void onSuccess() {}
                            @Override
                            public void onError(Exception e) {
                                Picasso.get().load(messageUrl).placeholder(R.drawable.ic_person).into(holder.imageView);
                            }
                        });
                holder.imageView.setOnClickListener(v -> showAlertDialog(messageUrl));
                break;
            case "pdf":
                holder.textView.setText(fileName);
                Picasso.get().load(messageUrl).placeholder(R.drawable.ic_pdf).into(holder.imageView);
                holder.imageView.setOnClickListener(v -> showAlertDialog(messageUrl));
                break;
            case "text":
                holder.textView.setText(fileName);
                Picasso.get().load(messageUrl).placeholder(R.drawable.ic_text_snippet).into(holder.imageView);
                holder.imageView.setOnClickListener(v -> showAlertDialog(messageUrl));
                break;
        }

        holder.cardView.setOnLongClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(context, v);
            popupMenu.getMenu().add("Download File");

            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getTitle().equals("Download File")) {
                    switch (type) {
                        case "image":
                            downloadImageFile(messageUrl, fileName,fileName);
                            break;
                        case "pdf":
                        case "text":
                            downloadOtherFile(messageUrl, fileName,fileName);
                            break;
                    }
                }
                return true;
            });
            popupMenu.show();
            return true;
        });

        holder.button.setOnClickListener(v -> {
            switch (type) {
                case "image":
                    downloadImageFile(messageUrl, fileName,fileName);
                    displayToast("Image Download Started.");
                    break;
                case "pdf":
                    downloadOtherFile(messageUrl, fileName,fileName);
                    displayToast("PDF File Download Started.");
                    break;
                case "text":
                    downloadOtherFile(messageUrl, fileName,fileName);
                    displayToast("Text File Download Started.");
                    break;
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView imageView;
        TextView textView;
        Button button;
        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            if (status) {
                imageView = itemView.findViewById(R.id.imageViewSend);
                textView = itemView.findViewById(R.id.ftp_textViewSend);
                cardView = itemView.findViewById(R.id.ftpSend_cardView);
                button = itemView.findViewById(R.id.downloadFileSendButton);
            }
            else {
                imageView = itemView.findViewById(R.id.imageViewReceive);
                textView = itemView.findViewById(R.id.ftp_textViewReceive);
                cardView = itemView.findViewById(R.id.ftpReceive_cardView);
                button = itemView.findViewById(R.id.downloadFileReceiveButton);
            }
        }
    }

    /**
     * if (list.get(position).getFrom().equals(name)) will determine whether the sender is a receiver
     * or not. Sender textview card will be placed on the right while the other user on the left.
     *
     * @param position position of the data inside the RecyclerView list.
     * @return returns position value.
     */
    @Override
    public int getItemViewType(int position) {
        try {
            if (list.get(position).getFrom().equals(name)) {
                status = true;
                return send;
            } else {
                status = false;
                return receive;
            }
        } catch (NullPointerException e) {
            Log.e("NullPointer", e.getMessage());
            e.printStackTrace();
        }
        return position;
    }
}