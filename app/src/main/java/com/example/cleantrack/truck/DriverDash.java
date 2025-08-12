package com.example.cleantrack.truck;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cleantrack.R;

public class DriverDash extends AppCompatActivity {
    TextView text_driverInfo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.driver_dash_board);

        text_driverInfo = findViewById(R.id.text_driverInfo);
        // Get stored values from SharedPreferences
        String role = getSharedPreferences("UserProfiles", MODE_PRIVATE).getString("role", "N/A");
        String username = getSharedPreferences("UserProfiles", MODE_PRIVATE).getString("username", "N/A");
        String email = getSharedPreferences("UserProfiles", MODE_PRIVATE).getString("email", "N/A");

        String driverInfo = "Username: " + username + "\n"
                + "Email: " + email + "\n"
                + "Role: " + role;

        // Display in TextView
        text_driverInfo.setText(driverInfo);

    }
}
