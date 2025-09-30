package com.example.mysafepoint.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mysafepoint.R;
import com.example.mysafepoint.services.FirebaseAuthService;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_TIMEOUT = 2000; // 2 seconds
    private FirebaseAuthService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Make the splash screen fullscreen
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        // Initialize Firebase Auth Service
        authService = new FirebaseAuthService();

        // Add fade-in animation for logo and text
        ImageView ivLogo = findViewById(R.id.ivLogo);
        TextView tvAppName = findViewById(R.id.tvAppName);
        TextView tvSlogan = findViewById(R.id.tvSlogan);

        Animation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(1500);

        ivLogo.startAnimation(fadeIn);
        tvAppName.startAnimation(fadeIn);
        tvSlogan.startAnimation(fadeIn);

        // Handler to navigate after delay
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                checkUserLoggedIn();
            }
        }, SPLASH_TIMEOUT);
    }

    private void checkUserLoggedIn() {
        if (authService.isUserLoggedIn()) {
            // Check if the user is an admin or a normal user
            authService.getCurrentFirebaseUser().getIdToken(false).addOnSuccessListener(result -> {
                String userId = authService.getCurrentUserId();
                // Retrieve user from Firestore to check their role
                if (userId != null) {
                    authService.getUserById(userId, user -> {
                        if (user != null && "admin".equals(user.getUserType())) {
                            // User is an admin, navigate to admin dashboard
                            startActivity(new Intent(SplashActivity.this, AdminDashboardActivity.class));
                        } else {
                            // User is a normal user, navigate to user dashboard
                            startActivity(new Intent(SplashActivity.this, UserDashboardActivity.class));
                        }
                        finish();
                    }, error -> {
                        // Error retrieving user, go to login
                        startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                        finish();
                    });
                } else {
                    // No user ID, go to login
                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                    finish();
                }
            }).addOnFailureListener(e -> {
                // Error getting token, go to login
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                finish();
            });
        } else {
            // User not logged in, go to login screen
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            finish();
        }
    }
}