package com.creatorbox.esense;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

/**
 * This fragment inflates when the Mode Info button is pressed in the Mode Screen activity.
 */
public class ModeInfo extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mode_info, container, false);
    }
}