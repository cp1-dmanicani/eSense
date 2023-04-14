/*
Application Name: Smart EduBox
Created Date: Sept. 15, 2022
Company: CreatorBox Solutions
Developer: DGMJr.
*/

package com.creatorbox.esense;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;

public class FileManager extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.file_manager);

        init();
    }

    private void init() {
        ImageButton internalStorage = findViewById(R.id.internal_storage);
        ImageButton removableStorage = findViewById(R.id.removable_storage);

        internalStorage.setOnClickListener(v -> {
            Intent intent = new Intent(this, FileManagerActivity.class);
            startActivity(intent);
            onStartNewActivityAnimations();
        });

        removableStorage.setOnClickListener(v -> {
            if (externalSDAvailable()) {
                Intent intent = new Intent(this, FileManagerActivity_RemovableStorage.class);
                startActivity(intent);
                onStartNewActivityAnimations();
            }
            else {
                Toast.makeText(this, "No SD Card Available.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public String getExternalSDPath() {
        StorageHelper.StorageVolume sv = new StorageHelper().getStorage(StorageHelper.StorageVolume.Type.EXTERNAL);
        if (sv != null) {
            return sv.file.getPath();
        } else {
            String sdpath = System.getenv("SECONDARY_STORAGE");
            if (sdpath == null || sdpath.isEmpty()) {
                sdpath = "/storage/extSdCard";
            }
            return sdpath;
        }
    }

    public boolean externalSDAvailable() {
        String path = getExternalSDPath();
        if (path != null) {
            File file = new File(path);
            return file.exists() && file.list() != null;
        }
        return false;
    }

    protected void onStartNewActivityAnimations() {
        overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
    }
}

