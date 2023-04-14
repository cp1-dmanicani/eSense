package com.creatorbox.esense;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

/**
 * FILE MANAGER
 * This fragment inflates when no files or subdirectories are found inside a directory.
 */
public class EmptyFolderFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_empty_folder, container, false);
    }
}