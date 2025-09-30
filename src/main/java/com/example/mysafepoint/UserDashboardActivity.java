package com.example.mysafepoint.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.mysafepoint.R;
import com.example.mysafepoint.fragments.CirclesFragment;
import com.example.mysafepoint.fragments.HomeFragment;
import com.example.mysafepoint.fragments.MapFragment;
import com.example.mysafepoint.fragments.ProfileFragment;
import com.example.mysafepoint.fragments.ReportFragment;
import com.example.mysafepoint.utils.Constants;
import com.example.mysafepoint.utils.PermissionUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class UserDashboardActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_dashboard);

        // Initialize views
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);

        // Check and request required permissions
        requestPermissions();

        // Load Home fragment by default
        loadFragment(new HomeFragment());
        bottomNavigationView.setSelectedItemId(R.id.navigation_home);
    }

    private void requestPermissions() {
        if (!PermissionUtils.hasLocationPermission(this) || !PermissionUtils.hasSMSPermission(this)) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.permission_required)
                    .setMessage(R.string.location_permission_message + "\n\n" +
                            R.string.sms_permission_message)
                    .setPositiveButton(R.string.action_confirm, (dialog, which) -> {
                        PermissionUtils.requestRequiredPermissions(this);
                    })
                    .setCancelable(false)
                    .show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Permission result handling can be added here if needed
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment = null;

        int itemId = item.getItemId();
        if (itemId == R.id.navigation_home) {
            fragment = new HomeFragment();
        } else if (itemId == R.id.navigation_circles) {
            fragment = new CirclesFragment();
        } else if (itemId == R.id.navigation_report) {
            fragment = new ReportFragment();
        } else if (itemId == R.id.navigation_map) {
            fragment = new MapFragment();
        } else if (itemId == R.id.navigation_profile) {
            fragment = new ProfileFragment();
        }

        return loadFragment(fragment);
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        // Handle back button press - exit app if pressed on home fragment
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (currentFragment instanceof HomeFragment) {
            // Exit app (show confirmation dialog if needed)
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Exit App")
                    .setMessage("Are you sure you want to exit?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        finish();
                    })
                    .setNegativeButton("No", null)
                    .show();
        } else {
            // Navigate to home fragment
            loadFragment(new HomeFragment());
            bottomNavigationView.setSelectedItemId(R.id.navigation_home);
        }
    }
}