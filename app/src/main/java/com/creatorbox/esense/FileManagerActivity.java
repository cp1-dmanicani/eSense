/*
Application Name: Smart EduBox
Created Date: Nov. 28, 2022
Company: CreatorBox Solutions
Developer: DGMJr.
*/

package com.creatorbox.esense;

import static com.creatorbox.esense.OnlineLogin.Theme_Material_Light_Dialog_Alert;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Vibrator;
import android.text.InputType;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * ONLINE MODE & OFFLINE MODE
 * The activity that is used by the File Manager feature.
 * Used by Internal Storage.
 */
public class FileManagerActivity extends AppCompatActivity {

    private TextView filePathText;
    private ListView listView;
    private List<String> list;
    private Vibrator vibe;
    private FloatingActionButton fabUpDir;
    private ProgressDialog progressDialog;

    private final String rootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
    private File dir = new File(rootPath);
    private File[] files;
    private int filesFoundCount;
    private boolean isFileManagerInitialized = false;
    private boolean[] selection;
    private boolean isLongClick;
    private boolean flag = false;

    private final TextAdapter textAdapter = new TextAdapter();

    private String currentPath, copyPath, movePath, sharePath;
    private int selectedItemIndex;
    private Menu hideMenu;
    private FragmentTransaction ft;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.file_manager_activity);

        init();
    }

    /**
     * Initialize all components.
     */
    private void init() {
        vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        filePathText = findViewById(R.id.filePathText);
        listView = findViewById(R.id.files_listview);
        fabUpDir = findViewById(R.id.fabGoUpDirectory);
        progressDialog = new ProgressDialog(FileManagerActivity.this, Theme_Material_Light_Dialog_Alert);
        list = new ArrayList<>();

        ActionBar ab = getSupportActionBar();
        ab.setTitle("FILE MANAGER");

        if (!isFileManagerInitialized) {
            loadListInit();
        }
    }

    /**
     * Load list view with data upon calling onCreate
     */
    private void loadListInit() {
        currentPath = rootPath;
        filePathText.setText(currentPath);
        files = dir.listFiles();
        filesFoundCount = files.length;
        list = new ArrayList<>();

        for (int i = 0; i < filesFoundCount; i++) {
            list.add(files[i].getAbsolutePath());
        }
        listView.setAdapter(textAdapter);
        textAdapter.setData(list);
        selection = new boolean[files.length];

        //When item is tapped.
        listView.setOnItemClickListener((parent, view, position, id) -> new Handler().postDelayed(() -> {
            if (!isLongClick) {
                currentPath = files[position].getAbsolutePath();
                dir = new File(currentPath);
                String dirName = currentPath.substring(currentPath.lastIndexOf('/') + 1);
                if (dir.isDirectory()) {
                    if (dirName.contains(".") || dirName.equals("Android")) {
                        displayToast("Cannot access directory.");
                    } else {
                        try {
                            updateList();
                        } catch (NullPointerException e) {
                            Log.e("NullPointer", e.getMessage());
                            displayToast("Invalid path.");
                        }
                    }
                } else {
                    try {
                        File file = new File(dir.getAbsolutePath());
                        Uri uri = FileProvider.getUriForFile(
                                this,
                                getApplicationContext().getPackageName() + ".fileprovider",
                                file
                        );
                        String mimeType = getMimeType(Uri.fromFile(file));
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(uri, mimeType);
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(getApplicationContext(),"No applications installed to open file.",Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }, 50));

        //When item is hold tapped.
        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            isLongClick = true;
            selection[position] = !selection[position];
            textAdapter.setSelection(selection);

            int selectionCount = 0;
            for (boolean aSelection : selection) {
                if (aSelection) {
                    selectionCount++;
                }
            }

            if (hideMenu!=null) {
                if (selectionCount>0) {
                    selectedItemIndex = position;
                    if(selectionCount==1) {
                        //Show action bar menu items if an item is selected
                        showAllMenuItems();
                    } else {
                        //Hide action bar menu items if no item is selected
                        hideSomeMenuItems();
                    }
                } else {
                    hideAllMenuItems();
                }
            }
            new Handler().postDelayed(() -> isLongClick = false, 1000);
            return false;
        });

        fabUpDir.setOnClickListener(v -> {
            vibe.vibrate(40);
            goUpDirectory();
        });

        isFileManagerInitialized = true;
    }
    /**
     * A function that shows all menu items that is used for task operations.
     */
    private void showAllMenuItems() {
        hideMenu.findItem(R.id.fm_menu_rename).setVisible(true);
        hideMenu.findItem(R.id.fm_menu_copy).setVisible(true);
        hideMenu.findItem(R.id.fm_menu_move).setVisible(true);
        hideMenu.findItem(R.id.fm_menu_delete).setVisible(true);
        hideMenu.findItem(R.id.fm_menu_share).setVisible(true);
    }

    /**
     * A function that hides menu items when more than one item is selected.
     * This is because file manager can only do operations of copy and move to only
     * one item selected per operation, for now.
     */
    private void hideSomeMenuItems() {
        hideMenu.findItem(R.id.fm_menu_rename).setVisible(false);
        hideMenu.findItem(R.id.fm_menu_copy).setVisible(false);
        hideMenu.findItem(R.id.fm_menu_move).setVisible(false);
        hideMenu.findItem(R.id.fm_menu_share).setVisible(false);
    }

    /**
     * A function that hides menu items when an operation is being used.
     */
    private void hideAllMenuItems() {
        hideMenu.findItem(R.id.fm_menu_delete).setVisible(false);
        hideMenu.findItem(R.id.fm_menu_rename).setVisible(false);
        hideMenu.findItem(R.id.fm_menu_copy).setVisible(false);
        hideMenu.findItem(R.id.fm_menu_move).setVisible(false);
        hideMenu.findItem(R.id.fm_menu_share).setVisible(false);
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
     * A method that gets the extension of the file in the list.
     * This is used to display the icon for a given file extension.
     * @param fileName the path of the file in the list.
     * @return returns the file extension in lower case without the dot (.), e.g. ("jpg", "pdf")
     */
    public static String getExtension(String fileName) {
        String encoded;
        try {
            encoded = URLEncoder.encode(fileName, "UTF-8").replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            encoded = fileName;
        }

        return MimeTypeMap.getFileExtensionFromUrl(encoded).toLowerCase();
    }

    /**
     * Refreshes the list view with new data added.
     * e.g. When creating a new folder, or deleting a file or folder.
     */
    private void updateList() {
        dir = new File(currentPath);
        files = dir.listFiles();
        filesFoundCount = files.length;
        selection = new boolean[filesFoundCount];
        list.clear();

        for (int i=0; i<filesFoundCount; i++) {
            list.add(files[i].getAbsolutePath());
        }

        textAdapter.setData(list);
        textAdapter.setSelection(selection);
        filePathText.setText(currentPath);
    }

    /**
     * A function that when the Home button in the ActionBar is pressed
     * will redirect the user to the home directory of the File Manager.
     */
    private void goHomeDirectory() {
        currentPath = rootPath;
        filePathText.setText(currentPath);
        files = dir.listFiles();
        filesFoundCount = files.length;
        list = new ArrayList<>();

        for (int i = 0; i < filesFoundCount; i++) {
            list.add(files[i].getAbsolutePath());
        }

        listView.setAdapter(textAdapter);
        textAdapter.setData(list);
        selection = new boolean[files.length];
    }

    /**
     * Go up one directory from the current directory the user is in.
     */
    private void goUpDirectory() {
        if (!currentPath.equals(rootPath)) {
            currentPath = currentPath.substring(0, currentPath.lastIndexOf("/"));
            dir = new File(currentPath);
            updateList();
            closeFragment();
            filePathText.setText(currentPath);
        } else {
            displayToast("Cannot go up directory.");
        }
    }

    /**
     * View the fragment that contains the text to show the user that no files
     * and folders exist in the directory.
     */
    private void viewEmptyFolderFragment() {
        if (!flag) {
            ft = getSupportFragmentManager().beginTransaction();
            EmptyFolderFragment fragment = new EmptyFolderFragment();
            ft.setCustomAnimations(
                    R.anim.fade_in,
                    R.anim.slide_out
            );
            ft.replace(R.id.file_manager_activity_linearLayout, fragment, "first");
            ft.addToBackStack(null);
            ft.commit();
            fabUpDir.setVisibility(View.INVISIBLE);
            flag = true;
        }
    }

    /**
     * Closes the fragment.
     */
    private void closeFragment() {
        Fragment f = getSupportFragmentManager().findFragmentByTag("first");
        if (f!=null) {
            ft.setCustomAnimations(
                    R.anim.fade_in,
                    R.anim.slide_out
            );
            ft.detach(f);
            getSupportFragmentManager().popBackStack();
            flag = false;
        }
        fabUpDir.setVisibility(View.VISIBLE);
    }

    public static
    <T extends Comparable<? super T>> List<T> asSortedList(Collection<T> c) {
        List<T> list = new ArrayList<T>(c);
        java.util.Collections.sort(list);
        return list;
    }

    /**
     * Adapter class that handles the ListView component.
     */
    public class TextAdapter extends BaseAdapter {
        private final List<String> data = new ArrayList<>();
        private boolean[] fileSelection;

        public void setData(List<String> data) {
            if (data!=null) {
                this.data.clear();
                if (data.size() > 0) {
                    this.data.addAll(data);
                } else {
                    viewEmptyFolderFragment();
                }
                notifyDataSetChanged();
            } else {
                displayToast("Failed to retrieve data.");
            }
        }

        /**
         * Void method that handles whether a file is selected (true) and not (false).
         * @param selection requires a boolean array value based on the items position
         *                  inside the ListView component.
         */
        void setSelection(boolean[] selection) {
            if (selection != null) {
                this.fileSelection = new boolean[selection.length];
                for (int i=0; i<selection.length; i++) {
                    this.fileSelection[i] = selection[i];
                }
            } else Log.d("setSelection", "No Items Selected.");
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public String getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).
                        inflate(R.layout.listview_item, parent,
                        false);
                convertView.setTag(new ViewHolder
                        (convertView.findViewById(R.id.listview_text),
                        (convertView.findViewById(R.id.listview_image)),
                        (convertView.findViewById(R.id.listview_linearlayout)),
                        (convertView.findViewById(R.id.listview_selectedindicator)),
                        (convertView.findViewById(R.id.listview_filesize)))
                );
            }

            ViewHolder holder = (ViewHolder) convertView.getTag();
            final String selectedItem = getItem(position);
            final String selectedFilePath = selectedItem.substring(selectedItem.lastIndexOf('/') + 1);

            holder.filePathName.setText(selectedFilePath);

            try {
                if (fileSelection != null) {
                    if (!fileSelection[position]) {
                        holder.fileLinearLayout.setBackgroundColor(Color.WHITE);
                        holder.fileSelectedIndicator.setVisibility(View.INVISIBLE);
                        notifyDataSetChanged();
                    } else {
                        holder.fileLinearLayout.setBackgroundColor(Color.LTGRAY);
                        holder.fileSelectedIndicator.setVisibility(View.VISIBLE);
                        notifyDataSetChanged();
                    }
                } else {
                    //Log.d("getView", "fileSelection value is null.");
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                e.printStackTrace();
            }

            File selectedItemFile = new File(selectedItem);
            holder.filePathSize.setText(Formatter.formatShortFileSize(FileManagerActivity.this, selectedItemFile.length()));
            Uri imageUri = Uri.fromFile(selectedItemFile);
            RequestOptions myOptions = new RequestOptions()
                    .override(50, 50);
            String fileExtension = getExtension(selectedFilePath);

            if (!selectedItemFile.isDirectory()) {
                switch(fileExtension) {
                    case "jpeg":
                    case "jpg":
                    case "mp4":
                    case "mkv":
                    case "png":
                    case "gif":
                        Glide.with(FileManagerActivity.this)
                                .asBitmap()
                                .apply(myOptions)
                                .load(imageUri)
                                .placeholder(R.drawable.ic_baseline_insert_drive_file_24)
                                .into(holder.filePathTypeImage);
                        break;
                    case "flac":
                    case "mp3":
                    case "m4a":
                    case "aac":
                    case "alac":
                    case "wav":
                    case "ogg":
                        holder.filePathTypeImage.setImageResource(R.drawable.ic_baseline_audio_file_24);
                        break;
                    case "pdf":
                        holder.filePathTypeImage.setImageResource(R.drawable.ic_pdf);
                        break;
                    case "txt":
                        holder.filePathTypeImage.setImageResource(R.drawable.ic_text_snippet);
                        break;
                    case "docx":
                    case "doc":
                        holder.filePathTypeImage.setImageResource(R.drawable.microsoft_word_2013_logo_logo_svgrepo_com);
                        break;
                    case "xls":
                    case "xlsx":
                    case "xlsb":
                        holder.filePathTypeImage.setImageResource(R.drawable.microsoft_excel_2013_logo_svgrepo_com);
                        break;
                    case "zip":
                    case "7z":
                    case "rar":
                        holder.filePathTypeImage.setImageResource(R.drawable.zip_svgrepo_com);
                        break;
                    case "apk":
                        holder.filePathTypeImage.setImageResource(R.drawable.apk_svgrepo_com);
                        break;
                    default:
                        holder.filePathTypeImage.setImageResource(R.drawable.ic_baseline_insert_drive_file_24);
                        break;
                }
            } else {
                holder.filePathTypeImage.setImageResource(R.drawable.ic_baseline_folder_24);
            }

            return convertView;
        }

        class ViewHolder {
            TextView filePathName, filePathSize;
            ImageView filePathTypeImage, fileSelectedIndicator;
            LinearLayout fileLinearLayout;

            ViewHolder(TextView filePath, ImageView fileType, LinearLayout linearLayout, ImageView selectedIndicator, TextView fileSize) {
                this.filePathName = filePath;
                this.filePathTypeImage = fileType;
                this.fileLinearLayout = linearLayout;
                this.fileSelectedIndicator = selectedIndicator;
                this.filePathSize = fileSize;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.filemanager_menu, menu);

        hideMenu = menu;
        hideMenu.findItem(R.id.fm_menu_paste).setVisible(false);
        hideMenu.findItem(R.id.fm_menu_move_here).setVisible(false);
        hideMenu.findItem(R.id.fm_menu_rename).setVisible(false);
        hideMenu.findItem(R.id.fm_menu_copy).setVisible(false);
        hideMenu.findItem(R.id.fm_menu_move).setVisible(false);
        hideMenu.findItem(R.id.fm_menu_delete).setVisible(false);
        hideMenu.findItem(R.id.fm_menu_share).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case R.id.fm_menu_home:
                try {
                    hideAllMenuItems();
                    goHomeDirectory();
                    showProgressDialog("Home Directory", "Going to home directory.");
                    if (Objects.equals(currentPath, rootPath)) {
                        Handler handler = new Handler();
                        handler.postDelayed(() -> {
                            closeFragment();
                            updateList();
                            progressDialog.dismiss();
                        }, 1000);
                    }
                } catch (NullPointerException e) {
                    Log.e("goHome", e.getMessage());
                }
                return true;
            case R.id.fm_menu_refresh:
                displayToast("Refreshed list.");
                hideAllMenuItems();
                updateList();
                return true;
            case R.id.fm_menu_newfolder:
                createNewFolderTask();
                return true;
            case R.id.fm_menu_rename:
                renameFileTask();
                return true;
            case R.id.fm_menu_copy:
                copyFileTask();
                hideAllMenuItems();
                return true;
            case R.id.fm_menu_paste:
                try {
                    pasteFileTask();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            case R.id.fm_menu_move:
                moveFileTask();
                hideAllMenuItems();
                return true;
            case R.id.fm_menu_move_here:
                try {
                    moveFileHereTask();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            case R.id.fm_menu_delete:
                deleteFileTask();
                return true;
            case R.id.fm_menu_share:
                shareFileUsingIntent();
                return true;
        }
        return false;
    }

    private void displayToast(String message) {
        Toast.makeText(FileManagerActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Shows a progress dialog when copying and moving file and folder.
     * @param title a string value that is used in the dialog's Title.
     * @param message a string value that is used in the dialog's Message.
     */
    public void showProgressDialog(String title, String message) {
        progressDialog.setTitle(title);
        progressDialog.setMessage(message);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }

    /**
     * Adds a delay for the progress dialog while copy and move task is being used.
     * @param toastMessage the message to be used in the toast widget.
     * @param delayInMillis the delay in millisecond value.
     */
    private void delayProgressDialog(String toastMessage, int delayInMillis) {
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            updateList();
            progressDialog.dismiss();
            displayToast(toastMessage);
        }, delayInMillis);
    }

    /**
     * A function that handles the create new folder task.
     */
    private void createNewFolderTask() {
        final View customETView = getLayoutInflater().inflate(R.layout.custom_edittext_dialog, null);
        final EditText customETVal = customETView.findViewById(R.id.editText_alertDialog);
        customETVal.setInputType(InputType.TYPE_CLASS_TEXT);

        final AlertDialog.Builder newFolderDialog = new AlertDialog.Builder(FileManagerActivity.this);
        newFolderDialog.setTitle("Create New Folder");
        newFolderDialog.setIcon(R.drawable.ic_baseline_create_new_folder_24);
        newFolderDialog.setView(customETView);
        newFolderDialog.setPositiveButton("Confirm", (dialog, which) -> {
                String newPath = currentPath + "/" + customETVal.getText().toString();
                final File newFolder = new File(newPath);
                boolean folderExists = newFolder.exists();
                if (!folderExists) {
                    try {
                        boolean created = newFolder.mkdir();
                        if (created) {
                            displayToast("New Folder Created");
                        } else {
                            displayToast("Failed to create folder.");
                        }
                    } catch (IOError e) {
                        Log.e("createNewFolderTask", "Something went wrong when creating a folder.");
                    }
                } else {
                    if (customETVal.getText().toString().trim().length()==0) {
                        displayToast("Folder name cannot be blank.");
                    } else {
                        displayToast("Folder already exists.");
                    }
                }
                updateList();
        });
        newFolderDialog.setNegativeButton("Cancel", null);
        newFolderDialog.show();
    }

    /**
     * A method that handles the input and output streams when copying files.
     * Used by pastFileTask()
     * @param sourceFile the File object to be copied.
     * @param destFile the File object where the sourceFile will be copied to.
     */
    private void copyFileStream(File sourceFile, File destFile) {
        try {
            InputStream in = new FileInputStream(sourceFile);
            OutputStream out = new FileOutputStream(destFile);
            byte[] buffer = new byte[2048];
            int length;

            while ((length=in.read(buffer)) > 0){
                out.write(buffer, 0, length);
            }

            out.flush();
            out.close();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * A method that handles the input and output streams when copying folders.
     * Used by pasteFileTask()
     * @param sourceFile the File path to be copied.
     * @param destFile the File path where the sourceFile will be copied to.
     */
    private void copyFolderStream(String sourceFile, String destFile) {
        try {
            File src = new File(sourceFile);
            File dest = new File(destFile, src.getName());

            if (src.isDirectory()) {
                String[] files = src.list();
                int filesLength = files.length;
                for (int i = 0; i < filesLength; i++) {
                    String src1 = (new File(src, files[i]).getPath());
                    String dest1 = dest.getPath();
                    copyFolderStream(src1, dest1);
                }

                if (!dest.exists()) {
                    dest.mkdir();
                }
            } else {
                copyFileInsideDir(src, dest);
            }
        } catch (Exception e) {
            Log.e("copyFolderStream", e.getMessage());
        }
    }

    /**
     * A method used by copyFolderStream to copy folders and all its child directories.
     * @param sourceFile the File object to be copied.
     * @param destFile the File path where the sourceFile will be copied to.
     * @throws IOException throws IOException when it catches an exception.
     */
    public static void copyFileInsideDir(File sourceFile, File destFile) throws IOException {
        if (!destFile.getParentFile().exists()) {
            destFile.getParentFile().mkdirs();
        }

        FileChannel source = null;
        FileChannel destination = null;

        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }

    /**
     * A method that handles the input and output streams when moving files.
     * Used by moveFileTask()
     * @param sourceLocation the File path to be moved.
     * @param targetLocation the File path where the sourceFile will be moved to.
     * @throws IOException throws IOException when it catches an exception.
     */
    private void moveFileStream(File sourceLocation, File targetLocation) throws IOException {

        if (sourceLocation.exists()) {
            FileInputStream fin = null;
            FileOutputStream fout = null;
            Log.i("debug","source "+sourceLocation);
            Log.i("debug","des "+targetLocation);
            try {
                fin = new FileInputStream(sourceLocation);
                new File(String.valueOf(targetLocation)).delete();
                fout = new FileOutputStream(targetLocation);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            // Copy the bits from instream to outstream
            byte[] buf = new byte[2048];
            int len;
            BufferedOutputStream bufferedOutputStream=new BufferedOutputStream(fout);
            BufferedInputStream bufferedInputStream=new BufferedInputStream(fin);
            while ((len = bufferedInputStream.read(buf)) > 0) {
                bufferedOutputStream.write(buf, 0, len);
            }
            fin.close();
            bufferedOutputStream.close();
            fout.close();


            boolean deleted = sourceLocation.delete();
            if (deleted) {
                Log.d("moveFileStream", "File moved to new directory. Previous directory deleted.");
            } else {
                Log.d("moveFileStream", "Failed to delete dir.");
            }
        } else {
            Log.v("debug", "Copy file failed. Source file missing.");
        }
    }

    /**
     * A method that handles the input and output streams when moving folders.
     * Used by moveFileTask()
     * @param sourceFile the File path of directory to be moved.
     * @param destFile the File path of directory where the sourceFile will be moved to.
     */
    private void moveFolderStream(String sourceFile, String destFile) {
        try {
            File src = new File(sourceFile);
            File dest = new File(destFile, src.getName());

            if (src.isDirectory()) {
                String[] files = src.list();
                int filesLength = files.length;
                for (int i = 0; i < filesLength; i++) {
                    String src1 = (new File(src, files[i]).getPath());
                    String dest1 = dest.getPath();
                    moveFolderStream(src1, dest1);
                }
                if (!dest.exists()) {
                    dest.mkdir();
                }
                deleteFileOrFolder(new File(sourceFile));
            } else {
                moveFileInsideDir(src, dest);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * A method used by moveFolderStream to move folders and all its child directories.
     * @param sourceFile the File object to be moved.
     * @param destFile the File path where the sourceFile will be moved to.
     * @throws IOException throws IOException when it catches an exception.
     */
    public void moveFileInsideDir(File sourceFile, File destFile) throws IOException {

        if (sourceFile.exists()) {
            if (!destFile.getParentFile().exists()) {
                boolean createDir = destFile.getParentFile().mkdirs();
                if (createDir) {
                    Log.d("moveFileInsideDir", "Folder and child contents moved.");
                } else {
                    Log.d("moveFileInsideDir", "Failed to create folder.");
                }
            }

            try (FileChannel source = new FileInputStream(sourceFile).getChannel();
                 FileChannel destination = new FileOutputStream(destFile).getChannel()) {
                destination.transferFrom(source, 0, source.size());
            }
        } else {
            Log.v("debug", "Move file failed. Source file missing.");
        }
    }

    /**
     * A function that gets the chosen file or folder to be copied.
     */
    private void copyFileTask() {
        copyPath = files[selectedItemIndex].getAbsolutePath();
        hideMenu.findItem(R.id.fm_menu_paste).setVisible(true);
    }

    /**
     * A function that handles the pasting of files or folders to other directories.
     */
    private void pasteFileTask() throws IOException {
        hideMenu.findItem(R.id.fm_menu_paste).setVisible(false);
        String destPath = currentPath + "/" + copyPath.substring(copyPath.lastIndexOf('/'));
        File sourceFilePath = new File(copyPath);
        File destFilePath = new File(destPath);
        boolean destFileExists = destFilePath.exists();

        if (!sourceFilePath.isDirectory()) {
            if (destFileExists) {
                displayToast("File already exists in directory.");
            } else {
                showProgressDialog("Copying file", "Please wait...");
                copyFileStream(sourceFilePath, destFilePath);
                closeFragment();
                delayProgressDialog("File copied here.", 2000);
            }
        } else {
            if (destFileExists) {
                displayToast("Folder already exists in directory. Task cancelled.");
            } else {
                showProgressDialog("Copying folder", "Please wait...");
                copyFolderStream(copyPath, currentPath);
                closeFragment();
                delayProgressDialog("Folder copied here.", 2000);
            }
        }
    }

    /**
     * A function that gets the chosen file or folder to be moved.
     */
    private void moveFileTask() {
        movePath = files[selectedItemIndex].getAbsolutePath();
        hideMenu.findItem(R.id.fm_menu_move_here).setVisible(true);
    }

    /**
     * A function that handles the moving of a file or a folder to a specific directory.
     */
    private void moveFileHereTask() throws IOException{
        hideMenu.findItem(R.id.fm_menu_move_here).setVisible(false);
        String movePathSubstring = movePath.substring(movePath.lastIndexOf('/'));
        String destPath = currentPath + "/" + movePathSubstring;
        File sourceFilePath = new File(movePath);
        File destFilePath = new File(destPath);
        boolean fileExistsInDir = destFilePath.exists();

        if (!sourceFilePath.isDirectory()) {
            if (!fileExistsInDir) {
                showProgressDialog("Moving file", "Please wait...");
                try {
                    moveFileStream(sourceFilePath, destFilePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                closeFragment();
                delayProgressDialog("File moved here.", 3000);
            } else {
                final AlertDialog.Builder moveTaskDialog = new AlertDialog.Builder(FileManagerActivity.this);
                moveTaskDialog.setTitle("MOVE");
                moveTaskDialog.setIcon(R.drawable.ic_move);
                moveTaskDialog.setMessage("File exists in chosen directory. Replace file?");
                moveTaskDialog.setPositiveButton("Replace", (dialog, which) -> {
                    showProgressDialog("Moving file", "Please wait...");
                    try {
                        moveFileStream(sourceFilePath, destFilePath);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    closeFragment();
                    delayProgressDialog("File replaced.", 3000);
                });
                moveTaskDialog.setNegativeButton("Cancel", null);
                moveTaskDialog.show();
            }
        } else {
            if (!fileExistsInDir) {
                showProgressDialog("Moving folder", "Please wait...");
                moveFolderStream(movePath, currentPath);
                closeFragment();
                delayProgressDialog("Folder moved here.", 3000);
            } else {
                final AlertDialog.Builder moveTaskDialog = new AlertDialog.Builder(FileManagerActivity.this);
                moveTaskDialog.setTitle("MOVE");
                moveTaskDialog.setIcon(R.drawable.ic_move);
                moveTaskDialog.setMessage("Folder exists in chosen directory. Replace folder?");
                moveTaskDialog.setPositiveButton("Replace", (dialog, which) -> {
                    showProgressDialog("Moving folder", "Please wait...");
                    moveFolderStream(movePath, currentPath);
                    closeFragment();
                    delayProgressDialog("Folder replaced.", 3000);
                });
                moveTaskDialog.setNegativeButton("Cancel", null);
                moveTaskDialog.show();
            }
        }
    }

    /**
     * A function that handles the renaming of a file or a folder.
     */
    private void renameFileTask() {
        String renamePath = files[selectedItemIndex].getAbsolutePath();
        final AlertDialog.Builder renameAlertDialog = new AlertDialog.Builder(FileManagerActivity.this);
        final View customETView = getLayoutInflater().inflate(R.layout.custom_edittext_dialog, null);
        final EditText customETVal = customETView.findViewById(R.id.editText_alertDialog);
        customETVal.setText(renamePath.substring(renamePath.lastIndexOf('/') +1));
        customETVal.setInputType(InputType.TYPE_CLASS_TEXT);

        renameAlertDialog.setTitle("RENAME");
        renameAlertDialog.setIcon(R.drawable.ic_rename);
        renameAlertDialog.setView(customETView);
        renameAlertDialog.setPositiveButton("Confirm", (dialog, which) -> {
            String tempRenamePath = new File(renamePath).getParent() + "/" + customETVal.getText().toString();
            File renamedFile = new File(tempRenamePath);
            if (!renamedFile.exists()) {
                try {
                    boolean rename = new File(renamePath).renameTo(renamedFile);
                    if (rename) {
                        updateList();
                        displayToast("Renamed.");
                    } else {
                        displayToast("Failed to rename.");
                    }
                } catch (IOError e) {
                    Log.e("renameFileTask", "Something went wrong when renaming.");
                }
            } else {
                displayToast("File name already exists in directory.");
            }
        });
        renameAlertDialog.setNegativeButton("Cancel", null);
        renameAlertDialog.show();
    }

    /**
     * A function that handles the delete task.
     */
    private void deleteFileTask() {
        final AlertDialog.Builder deleteFileDialog = new AlertDialog.Builder(this);
        deleteFileDialog.setTitle("DELETE");
        deleteFileDialog.setIcon(R.drawable.ic_delete_forever);
        deleteFileDialog.setMessage("Delete this permanently?");
        deleteFileDialog.setPositiveButton("Yes", (dialog, which) -> {
            if (dir.exists()) {
                for (int i = 0; i< files.length; i++) {
                    if (selection[i]) {
                        deleteFileOrFolder(files[i]);
                        selection[i] = false;
                    }
                }
                showProgressDialog("DELETE", "Deleting");
                Handler handler = new Handler();
                handler.postDelayed(() -> {
                    updateList();
                    progressDialog.dismiss();
                    displayToast("Deleted.");
                }, 500);
                textAdapter.setSelection(selection);
            } else {
                displayToast("Item doesn't exist.");
            }
        });
        deleteFileDialog.setNegativeButton("Cancel", (dialog, which) -> updateList());
        deleteFileDialog.show();
    }

    /**
     * A method that checks if file is successfully deleted or not.
     * Used in the deleteFileOrFolder method.
     * @param deleted - a boolean value to determine whether file/folder
     *                is deleted successfully.
     */
    private void deleteState(Boolean deleted) {
        if (deleted) {
            //Log.d("deleteState", "Deleted.");
        } else {
            displayToast("Error deleting file.");
        }
    }

    /**
     * A method that deletes and checks the selected items in the listview
     * whether they are deleted or not.
     *
     * @param fileOrFolder is the File object that is called in the function
     *                     deleteFile()
     */
    private void deleteFileOrFolder(File fileOrFolder) {
        if (fileOrFolder.exists()) {
            //If file is a directory (folder)
            if (fileOrFolder.isDirectory()) {
                if (Objects.requireNonNull(fileOrFolder.list()).length == 0) {
                    boolean deleted = fileOrFolder.delete();
                    deleteState(deleted);
                } else {
                    String[] files = fileOrFolder.list();
                    //Iterate over all the selected files
                    assert files != null;
                    for (String temp : files) {
                        File fileToDelete = new File(fileOrFolder, temp);
                        deleteFileOrFolder(fileToDelete);
                    }
                    if (Objects.requireNonNull(fileOrFolder.list()).length == 0) {
                        boolean deleted = fileOrFolder.delete();
                        deleteState(deleted);
                    }
                }
            }
            //If file is a lone file.
            else {
                boolean deleted = fileOrFolder.delete();
                deleteState(deleted);
            }
        }
        else displayToast("No file/s to delete.");
    }

    /**
     * A function that is used to share any file that the user has chosen from the list
     * to other devices that has Nearby Share.
     */
    private void shareFileUsingIntent() {
        sharePath = files[selectedItemIndex].getAbsolutePath();
        File fileToBeSent = new File(sharePath);
        String mimeType = getMimeType(Uri.parse(sharePath));
        Uri uri = FileProvider.getUriForFile(
                this,
                getApplicationContext().getPackageName() + ".fileprovider",
                fileToBeSent
        );
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
        sendIntent.setType(mimeType);
        startActivity(Intent.createChooser(sendIntent, "Share file using"));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isFileManagerInitialized) {
            loadListInit();
        }
    }

    @Override
    public void onBackPressed() {
        if (flag) {
            closeFragment();
        }
        else {
            super.onBackPressed();
            goUpDirectory();
        }
    }
}