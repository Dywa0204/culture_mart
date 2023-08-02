package com.udin.culturemart.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.udin.culturemart.R;

public class HomeFragment extends Fragment {

    View view;
    TextView tvWelcomeFullname;

    String fullname;

    public HomeFragment(String fullname) {
        this.fullname = fullname;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_home, container, false);

        tvWelcomeFullname = view.findViewById(R.id.welcome_fullname);
        tvWelcomeFullname.setText(fullname);

        return view;
    }
}