
package com.example.cleantrack.user;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.cleantrack.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class UserDash extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_dash_borad);

        // Get reference to the BottomNavigationView
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        // Set up the listener for when a tab is selected
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            // Determine which fragment to load based on the selected menu item
            int itemId = item.getItemId();
            if (itemId == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
            } else if (itemId == R.id.nav_trucks) {
                selectedFragment = new TrucksFragment();
            } else if (itemId == R.id.nav_dustbin) {
                selectedFragment = new DustbinFragment();
            }

            // Replace the current fragment with the selected one
            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .commit();
            }
            return true;
        });

    }
}
