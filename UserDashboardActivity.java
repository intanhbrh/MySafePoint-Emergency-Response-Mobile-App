package com.example.safepoint.activities;

import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.safepoint.R;
import com.example.safepoint.fragments.SOSFragment;
import com.example.safepoint.fragments.EmergencyCirclesFragment;
import com.example.safepoint.fragments.ReportIncidentFragment;
import com.example.safepoint.fragments.SafetyResourcesFragment;
import com.example.safepoint.fragments.UserProfileFragment;

public class UserDashboardActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_dashboard);

        initializeViews();
        setupBottomNavigation();

        // Set default fragment (SOS)
        if (savedInstanceState == null) {
            loadFragment(new SOSFragment());
        }
    }

    private void initializeViews() {
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;

                int itemId = item.getItemId();
                if (itemId == R.id.nav_sos) {
                    selectedFragment = new SOSFragment();
                } else if (itemId == R.id.nav_emergency_circles) {
                    selectedFragment = new EmergencyCirclesFragment();
                } else if (itemId == R.id.nav_report_incident) {
                    selectedFragment = new ReportIncidentFragment();
                } else if (itemId == R.id.nav_safety_resources) {
                    selectedFragment = new SafetyResourcesFragment();
                } else if (itemId == R.id.nav_profile) {
                    selectedFragment = new UserProfileFragment();
                }

                return loadFragment(selectedFragment);
            }
        });
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .commit();
            return true;
        }
        return false;
    }
}