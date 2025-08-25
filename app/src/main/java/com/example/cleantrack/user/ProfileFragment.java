package com.example.cleantrack.user;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.cleantrack.R;

public class ProfileFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Get references to the TextViews
        TextView tvUsername = view.findViewById(R.id.tv_username);
        TextView tvEmail = view.findViewById(R.id.tv_email);
        TextView tvRole = view.findViewById(R.id.tv_role);

        // Get stored values from SharedPreferences
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserProfiles", Context.MODE_PRIVATE);
        String username = sharedPreferences.getString("username", "N/A");
        String email = sharedPreferences.getString("email", "N/A");
        String role = sharedPreferences.getString("role", "N/A");

        // Display the user information
        tvUsername.setText("Username: " + username);
        tvEmail.setText("Email: " + email);
        tvRole.setText("Role: " + role);

        return view;
    }
}