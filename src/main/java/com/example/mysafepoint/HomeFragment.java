package com.example.mysafepoint.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mysafepoint.R;
import com.example.mysafepoint.models.EmergencyAlert;
import com.example.mysafepoint.models.EmergencyContact;
import com.example.mysafepoint.services.FirebaseIncidentService;
import com.example.mysafepoint.services.FirebaseUserService;
import com.example.mysafepoint.services.LocationService;
import com.example.mysafepoint.services.SMSService;
import com.example.mysafepoint.utils.Constants;
import com.example.mysafepoint.utils.PermissionUtils;
import com.example.mysafepoint.utils.SharedPrefManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

public class HomeFragment extends Fragment {

    private TextView tvWelcome;
    private MaterialButton btnEmergency;

    private SharedPrefManager prefManager;
    private FirebaseUserService userService;
    private FirebaseIncidentService incidentService;
    private LocationService locationService;
    private SMSService smsService;

    private String userId;
    private String userName;
    private boolean isDialogShowing = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize views
        tvWelcome = view.findViewById(R.id.tvWelcome);
        btnEmergency = view.findViewById(R.id.btnEmergency);

        // Initialize services
        prefManager = SharedPrefManager.getInstance(requireContext());
        userService = new FirebaseUserService();
        incidentService = new FirebaseIncidentService();
        locationService = new LocationService(requireContext());
        smsService = new SMSService(requireContext());

        // Get user data
        userId = prefManager.getString(Constants.KEY_USER_ID);
        userName = prefManager.getString(Constants.KEY_USER_NAME);

        // Set welcome message
        tvWelcome.setText("Welcome, " + userName);

        // Setup emergency button click
        btnEmergency.setOnClickListener(v -> {
            if (!isDialogShowing) {
                showEmergencyTypeDialog();
            }
        });

        return view;
    }

    private void showEmergencyTypeDialog() {
        isDialogShowing = true;

        final String[] emergencyTypes = {
                Constants.INCIDENT_TYPE_ASSAULT,
                Constants.INCIDENT_TYPE_ROBBERY,
                Constants.INCIDENT_TYPE_KIDNAP,
                Constants.INCIDENT_TYPE_OTHER
        };

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Emergency Type")
                .setItems(emergencyTypes, (dialog, which) -> {
                    String selectedType = emergencyTypes[which];
                    triggerEmergencyAlert(selectedType);
                })
                .setOnCancelListener(dialog -> isDialogShowing = false)
                .show();
    }

    private void triggerEmergencyAlert(String incidentType) {
        // Check permissions first
        if (!PermissionUtils.hasLocationPermission(requireContext())) {
            Toast.makeText(requireContext(), R.string.location_permission_message, Toast.LENGTH_LONG).show();
            PermissionUtils.requestLocationPermission(requireActivity());
            isDialogShowing = false;
            return;
        }

        if (!PermissionUtils.hasSMSPermission(requireContext())) {
            Toast.makeText(requireContext(), R.string.sms_permission_message, Toast.LENGTH_LONG).show();
            PermissionUtils.requestSMSPermission(requireActivity());
            isDialogShowing = false;
            return;
        }

        // Show processing dialog
        AlertDialog progressDialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Sending Alert")
                .setMessage("Processing your emergency alert...")
                .setCancelable(false)
                .show();

        // Get user's emergency contacts
        userService.getEmergencyContacts(userId, new FirebaseUserService.EmergencyContactsCallback() {
            @Override
            public void onSuccess(List<EmergencyContact> contacts) {
                if (contacts.isEmpty()) {
                    progressDialog.dismiss();
                    isDialogShowing = false;
                    Toast.makeText(requireContext(), "You have no emergency contacts. Please add contacts first.", Toast.LENGTH_LONG).show();
                    return;
                }

                // Get current location
                locationService.getCurrentLocation(new LocationService.LocationCallback() {
                    @Override
                    public void onLocationReceived(double latitude, double longitude, String address) {
                        // Create emergency alert
                        EmergencyAlert alert = new EmergencyAlert(
                                null, // ID will be generated by Firebase
                                userId,
                                userName,
                                incidentType,
                                address,
                                latitude,
                                longitude
                        );

                        // Save to Firebase
                        incidentService.createEmergencyAlert(alert, new FirebaseIncidentService.AlertCallback() {
                            @Override
                            public void onSuccess(EmergencyAlert alert) {
                                // Send SMS to emergency contacts
                                smsService.sendEmergencyAlertSMS(alert, contacts, new SMSService.SMSCallback() {
                                    @Override
                                    public void onSMSSent(EmergencyContact contact) {
                                        // Successfully sent to at least one contact
                                        if (progressDialog.isShowing()) {
                                            progressDialog.dismiss();
                                            isDialogShowing = false;
                                            Toast.makeText(requireContext(), "Emergency alert sent successfully", Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    @Override
                                    public void onSMSError(EmergencyContact contact, String errorMessage) {
                                        // Error sending SMS
                                        if (progressDialog.isShowing()) {
                                            progressDialog.dismiss();
                                            isDialogShowing = false;
                                            Toast.makeText(requireContext(), "Error sending SMS: " + errorMessage, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }

                            @Override
                            public void onError(String errorMessage) {
                                // Error saving alert to Firebase
                                progressDialog.dismiss();
                                isDialogShowing = false;
                                Toast.makeText(requireContext(), "Error creating alert: " + errorMessage, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onLocationError(String errorMessage) {
                        // Error getting location
                        progressDialog.dismiss();
                        isDialogShowing = false;
                        Toast.makeText(requireContext(), "Error getting location: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                // Error getting contacts
                progressDialog.dismiss();
                isDialogShowing = false;
                Toast.makeText(requireContext(), "Error getting contacts: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }
}