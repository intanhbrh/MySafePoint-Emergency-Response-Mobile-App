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

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etFullName, etEmail, etPhone, etNIC, etPassword, etConfirmPassword;
    private Button btnRegister;
    private TextView tvLogin;
    private ProgressBar progressBar;
    private FirebaseAuthService authService;
    private SharedPrefManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize views
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etNIC = findViewById(R.id.etNIC);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);
        progressBar = findViewById(R.id.progressBar);

        // Initialize Firebase Auth Service
        authService = new FirebaseAuthService();
        prefManager = SharedPrefManager.getInstance(this);

        // Register button click
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        // Login text click
        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Go back to login screen
            }
        });
    }

    private void registerUser() {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String nic = etNIC.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Validate inputs
        if (TextUtils.isEmpty(fullName)) {
            etFullName.setError("Full name is required");
            etFullName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(phone)) {
            etPhone.setError("Phone number is required");
            etPhone.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(nic)) {
            etNIC.setError("NIC is required");
            etNIC.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            etConfirmPassword.setError("Confirm your password");
            etConfirmPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return;
        }

        // Show progress bar
        progressBar.setVisibility(View.VISIBLE);
        btnRegister.setEnabled(false);

        // Attempt to register
        authService.registerUser(fullName, email, phone, nic, password, new FirebaseAuthService.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                // Hide progress bar
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    btnRegister.setEnabled(true);

                    // Save user to shared preferences
                    prefManager.saveString(Constants.KEY_USER_ID, user.getUserId());
                    prefManager.saveString(Constants.KEY_USER_NAME, user.getFullName());
                    prefManager.saveString(Constants.KEY_USER_EMAIL, user.getEmail());
                    prefManager.saveString(Constants.KEY_USER_PHONE, user.getPhoneNumber());
                    prefManager.saveString(Constants.KEY_USER_TYPE, user.getUserType());

                    // Navigate to user dashboard
                    Toast.makeText(RegisterActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(RegisterActivity.this, UserDashboardActivity.class));
                    finish();
                });
            }

            @Override
            public void onError(String errorMessage) {
                // Hide progress bar and show error
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    btnRegister.setEnabled(true);
                    Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}