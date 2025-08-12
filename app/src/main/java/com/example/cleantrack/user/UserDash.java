package com.example.cleantrack.user;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cleantrack.R;

public class UserDash extends AppCompatActivity {

    TextView text_userInfo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_dash_borad);

        text_userInfo = findViewById(R.id.userInfo);
        // Get stored values from SharedPreferences
        String role = getSharedPreferences("UserProfiles", MODE_PRIVATE).getString("role", "N/A");
        String username = getSharedPreferences("UserProfiles", MODE_PRIVATE).getString("username", "N/A");
        String email = getSharedPreferences("UserProfiles", MODE_PRIVATE).getString("email", "N/A");

        // Prepare the text
        String userInfo = "Username: " + username + "\n"
                + "Email: " + email + "\n"
                + "Role: " + role;

        // Display in TextView
        text_userInfo.setText(userInfo);



    }
}
