package com.example.cleantrack.truck;

import android.os.Build;
import android.os.Bundle;
import android.view.WindowInsets;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.cleantrack.R;
import com.example.cleantrack.truck.fragment.DustbinFragment;
import com.example.cleantrack.truck.fragment.HomeFragement;
import com.example.cleantrack.truck.fragment.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Objects;

public class DriverDash extends AppCompatActivity {
    TextView text_driverInfo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.driver_dash_board);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Objects.requireNonNull(getWindow().getInsetsController()).show(WindowInsets.Type.statusBars());
        }
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation_driver);

        // Set up the listener for when a tab is selected
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            // Determine which fragment to load based on the selected menu item
            int itemId = item.getItemId();
            if (itemId == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
            } else if (itemId == R.id.nav_home) {
                selectedFragment = new HomeFragement();
            } else if (itemId == R.id.nav_dustbin) {
                selectedFragment = new DustbinFragment();
            }

            // Replace the current fragment with the selected one
            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container_driver, selectedFragment)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .commit();
            }
            return true;
        });

    }
}
