package com.example.mysafepoint.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mysafepoint.R;
import com.example.mysafepoint.models.IncidentReport;
import com.example.mysafepoint.services.FirebaseIncidentService;
import com.example.mysafepoint.services.LocationService;
import com.example.mysafepoint.utils.Constants;
import com.example.mysafepoint.utils.PermissionUtils;
import com.example.mysafepoint.utils.SharedPrefManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.UUID;

public class ReportFragment extends Fragment {

    private RadioGroup rgIncidentType;
    private RadioButton rbAssault, rbRobbery, rbKidnap, rbOther;
    private TextView tvCurrentLocation;
    private EditText etDescription;
    private Button btnSubmitReport;
    private ProgressBar progressBar;

    private FirebaseIncidentService incidentService;
    private LocationService locationService;
    private SharedPrefManager prefManager;

    private String userId;
    private String userName;
    private String userPhone;
    private String currentLocation;
    private double currentLatitude;
    private double currentLongitude;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_report, container, false);

        // Initialize views
        rgIncidentType = view.findViewById(R.id.rgIncidentType);
        rbAssault = view.findViewById(R.id.rbAssault);
        rbRobbery = view.findViewById(R.id.rbRobbery);
        rbKidnap = view.findViewById(R.id.rbKidnap);
        rbOther = view.findViewById(R.id.rbOther);
        tvCurrentLocation = view.findViewById(R.id.tvCurrentLocation);
        etDescription = view.findViewById(R.id.etDescription);
        btnSubmitReport = view.findViewById(R.id.btnSubmitReport);
        progressBar = view.findViewById(R.id.progressBar);

        // Initialize services
        incidentService = new FirebaseIncidentService();
        locationService = new LocationService(requireContext());
        prefManager = SharedPrefManager.getInstance(requireContext());

        // Get user data
        userId = prefManager.getString(Constants.KEY_USER_ID);
        userName = prefManager.getString(Constants.KEY_USER_NAME);
        userPhone = prefManager.getString(Constants.KEY_USER_PHONE);

        // Get current location
        if (PermissionUtils.hasLocationPermission(requireContext())) {
            getCurrentLocation();
        } else {
            tvCurrentLocation.setText("Location permission required");
            PermissionUtils.requestLocationPermission(requireActivity());
        }

        // Set submit button click listener
        btnSubmitReport.setOnClickListener(v -> submitReport());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh location when fragment resumes
        if (PermissionUtils.hasLocationPermission(requireContext())) {
            getCurrentLocation();
        }
    }

    private void getCurrentLocation() {
        tvCurrentLocation.setText("Getting your current location...");

        locationService.getCurrentLocation(new LocationService.LocationCallback() {
            @Override
            public void onLocationReceived(double latitude, double longitude, String address) {
                currentLatitude = latitude;
                currentLongitude = longitude;
                currentLocation = address;

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        tvCurrentLocation.setText(address);
                    });
                }
            }

            @Override
            public void onLocationError(String errorMessage) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        tvCurrentLocation.setText("Error getting location: " + errorMessage);
                    });
                }
            }
        });
    }

    private void submitReport() {
        // Validate inputs
        if (rgIncidentType.getCheckedRadioButtonId() == -1) {
            Toast.makeText(requireContext(), "Please select an incident type", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentLocation == null || currentLocation.isEmpty() ||
                currentLocation.equals("Getting your current location...") ||
                currentLocation.contains("Error getting location")) {
            Toast.makeText(requireContext(), "Location not available. Please wait or check permissions.", Toast.LENGTH_SHORT).show();
            return;
        }

        String description = etDescription.getText().toString().trim();
        if (description.isEmpty()) {
            etDescription.setError("Please provide a description");
            etDescription.requestFocus();
            return;
        }

        // Get selected incident type
        String incidentType;
        int selectedId = rgIncidentType.getCheckedRadioButtonId();
        if (selectedId == R.id.rbAssault) {
            incidentType = Constants.INCIDENT_TYPE_ASSAULT;
        } else if (selectedId == R.id.rbRobbery) {
            incidentType = Constants.INCIDENT_TYPE_ROBBERY;
        } else if (selectedId == R.id.rbKidnap) {
            incidentType = Constants.INCIDENT_TYPE_KIDNAP;
        } else {
            incidentType = Constants.INCIDENT_TYPE_OTHER;
        }

        // Show progress
        progressBar.setVisibility(View.VISIBLE);
        btnSubmitReport.setEnabled(false);

        // Create incident report
        IncidentReport report = new IncidentReport(
                UUID.randomUUID().toString(), // Temporary ID, will be replaced by Firebase
                userId,
                userName,
                userPhone,
                incidentType,
                description,
                currentLocation,
                currentLatitude,
                currentLongitude
        );

        // Submit to Firebase
        incidentService.reportIncident(report, new FirebaseIncidentService.IncidentCallback() {
            @Override
            public void onSuccess(IncidentReport incident) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        btnSubmitReport.setEnabled(true);

                        // Clear form
                        rgIncidentType.clearCheck();
                        etDescription.setText("");

                        // Show success message
                        new MaterialAlertDialogBuilder(requireContext())
                                .setTitle("Report Submitted")
                                .setMessage("Your incident report has been submitted successfully. Emergency responders will review your report shortly.")
                                .setPositiveButton("OK", null)
                                .show();
                    });
                }
            }

            @Override
            public void onError(String errorMessage) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        btnSubmitReport.setEnabled(true);
                        Toast.makeText(requireContext(), "Error submitting report: " + errorMessage, Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }
}