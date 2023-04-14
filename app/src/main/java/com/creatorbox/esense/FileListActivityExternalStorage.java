/*
Application Name: Smart EduBox
Created Date: Sept. 15, 2022
Company: CreatorBox Solutions
Developer: DGMJr.
*/

package com.creatorbox.esense;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;

public class FileListActivityExternalStorage extends AppCompatActivity {

    RecyclerView recyclerView;
    TextView noFilesText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_list_externalstorage);

        recyclerView = findViewById(R.id.recycler_view);
        noFilesText = findViewById(R.id.nofiles_textview);

        String path = getIntent().getStringExtra("path");
        File root = new File(path);
        File[] filesAndFolders = root.listFiles();

        if(filesAndFolders==null || filesAndFolders.length ==0){
            noFilesText.setVisibility(View.VISIBLE);
            return;
        }
        noFilesText.setVisibility(View.INVISIBLE);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new FileAdapter(getApplicationContext(),filesAndFolders));
    }

}