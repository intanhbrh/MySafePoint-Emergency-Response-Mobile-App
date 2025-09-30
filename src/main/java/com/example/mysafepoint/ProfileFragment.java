package com.example.mysafepoint.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mysafepoint.R;
import com.example.mysafepoint.activities.LoginActivity;
import com.example.mysafepoint.models.User;
import com.example.mysafepoint.services.FirebaseAuthService;
import com.example.mysafepoint.services.FirebaseUserService;
import com.example.mysafepoint.utils.Constants;
import com.example.mysafepoint.utils.SharedPrefManager;
import com.example.mysafepoint.utils.ValidationUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    private CircleImageView ivProfileImage;
    private TextInputEditText etFullName, etEmail, etPhone, etNIC;
    private Button btnUpdateProfile, btnLogout;
    private ProgressBar progressBar;

    private FirebaseAuthService authService;
    private FirebaseUserService userService;
    private SharedPrefManager prefManager;

    private String userId;
    private User currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize views
        ivProfileImage = view.findViewById(R.id.ivProfileImage);
        etFullName = view.findViewById(R.id.etFullName);
        etEmail = view.findViewById(R.id.etEmail);
        etPhone = view.findViewById(R.id.etPhone);
        etNIC = view.findViewById(R.id.etNIC);
        btnUpdateProfile = view.findViewById(R.id.btnUpdateProfile);
        btnLogout = view.findViewById(R.id.btnLogout);
        progressBar = view.findViewById(R.id.progressBar);

        // Initialize services
        authService = new FirebaseAuthService();
        userService = new FirebaseUserService();
        prefManager = SharedPrefManager.getInstance(requireContext());

        // Get user ID
        userId = prefManager.getString(Constants.KEY_USER_ID);

        // Load user data
        loadUserData();

        // Setup update button
        btnUpdateProfile.setOnClickListener(v -> updateProfile());

        // Setup logout button
        btnLogout.setOnClickListener(v -> showLogoutConfirmation());

        return view;
    }

    private void loadUserData() {
        progressBar.setVisibility(View.VISIBLE);

        userService.getUserById(userId, new FirebaseUserService.UserCallback() {
            @Override
            public void onSuccess(User user) {
                currentUser = user;

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        // Set profile data
                        etFullName.setText(user.getFullName());
                        etEmail.setText(user.getEmail());
                        etPhone.setText(user.getPhoneNumber());
                        etNIC.setText(user.getNic());

                        progressBar.setVisibility(View.GONE);
                    });
                }
            }

            @Override
            public void onError(String errorMessage) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Error loading profile: " + errorMessage, Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    });
                }
            }
        });
    }

    private void updateProfile() {
        // Validate inputs
        String fullName = etFullName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String nic = etNIC.getText().toString().trim();

        if (ValidationUtils.isEmptyOrNull(fullName)) {
            etFullName.setError("Full name is required");
            etFullName.requestFocus();
            return;
        }

        if (ValidationUtils.isEmptyOrNull(phone)) {
            etPhone.setError("Phone number is required");
            etPhone.requestFocus();
            return;
        }

        if (!ValidationUtils.isValidPhone(phone)) {
            etPhone.setError("Invalid phone number");
            etPhone.requestFocus();
            return;
        }

        if (ValidationUtils.isEmptyOrNull(nic)) {
            etNIC.setError("NIC is required");
            etNIC.requestFocus();
            return;
        }

        // Show progress
        progressBar.setVisibility(View.VISIBLE);
        btnUpdateProfile.setEnabled(false);

        // Update user object
        currentUser.setFullName(fullName);
        currentUser.setPhoneNumber(phone);
        currentUser.setNic(nic);

        // Update in Firebase
        userService.updateUser(currentUser, new FirebaseUserService.UserCallback() {
            @Override
            public void onSuccess(User user) {
                // Update shared preferences
                prefManager.saveString(Constants.KEY_USER_NAME, user.getFullName());
                prefManager.saveString(Constants.KEY_USER_PHONE, user.getPhoneNumber());

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        btnUpdateProfile.setEnabled(true);
                    });
                }
            }

            @Override
            public void onError(String errorMessage) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Error updating profile: " + errorMessage, Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        btnUpdateProfile.setEnabled(true);
                    });
                }
            }
        });
    }

    private void showLogoutConfirmation() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> logout())
                .setNegativeButton("No", null)
                .show();
    }

    private void logout() {
        // Logout from Firebase
        authService.logoutUser();

        // Clear shared preferences
        prefManager.clear();

        // Navigate to login screen
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }
}