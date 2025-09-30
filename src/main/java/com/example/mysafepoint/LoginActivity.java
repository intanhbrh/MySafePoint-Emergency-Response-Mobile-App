package com.example.mysafepoint.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mysafepoint.R;
import com.example.mysafepoint.services.FirebaseAuthService;
import com.example.mysafepoint.utils.Constants;
import com.example.mysafepoint.utils.SharedPrefManager;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegister;
    private ProgressBar progressBar;
    private FirebaseAuthService authService;
    private SharedPrefManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize views
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);
        progressBar = findViewById(R.id.progressBar);

        // Initialize Firebase Auth Service
        authService = new FirebaseAuthService();
        prefManager = SharedPrefManager.getInstance(this);

        // Login button click
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        // Register text click
        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validate inputs
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }

        // Show progress bar
        progressBar.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);

        // Attempt to login
        authService.loginUser(email, password, new FirebaseAuthService.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                // Hide progress bar
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    btnLogin.setEnabled(true);

                    // Save user to shared preferences
                    prefManager.saveString(Constants.KEY_USER_ID, user.getUserId());
                    prefManager.saveString(Constants.KEY_USER_NAME, user.getFullName());
                    prefManager.saveString(Constants.KEY_USER_EMAIL, user.getEmail());
                    prefManager.saveString(Constants.KEY_USER_PHONE, user.getPhoneNumber());
                    prefManager.saveString(Constants.KEY_USER_TYPE, user.getUserType());

                    // Navigate based on user type
                    if ("admin".equals(user.getUserType())) {
                        startActivity(new Intent(LoginActivity.this, AdminDashboardActivity.class));
                    } else {
                        startActivity(new Intent(LoginActivity.this, UserDashboardActivity.class));
                    }
                    finish();
                });
            }

            @Override
            public void onError(String errorMessage) {
                // Hide progress bar and show error
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    btnLogin.setEnabled(true);
                    Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}