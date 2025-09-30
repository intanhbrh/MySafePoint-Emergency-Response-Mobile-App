package com.example.mysafepoint.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.mysafepoint.R;
import com.example.mysafepoint.models.IncidentReport;
import com.example.mysafepoint.services.FirebaseIncidentService;
import com.example.mysafepoint.utils.Constants;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class IncidentDetailsActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView tvIncidentType, tvUserName, tvUserPhone, tvLocation, tvDescription, tvTimestamp;
    private Chip chipStatus;
    private Button btnViewMap, btnUpdateStatus, btnCallUser;
    private ProgressBar progressBar;

    private FirebaseIncidentService incidentService;
    private String incidentId;
    private IncidentReport currentIncident;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incident_details);

        // Initialize views
        toolbar = findViewById(R.id.toolbar);
        tvIncidentType = findViewById(R.id.tvIncidentType);
        tvUserName = findViewById(R.id.tvUserName);
        tvUserPhone = findViewById(R.id.tvUserPhone);
        tvLocation = findViewById(R.id.tvLocation);
        tvDescription = findViewById(R.id.tvDescription);
        tvTimestamp = findViewById(R.id.tvTimestamp);
        chipStatus = findViewById(R.id.chipStatus);
        btnViewMap = findViewById(R.id.btnViewMap);
        btnUpdateStatus = findViewById(R.id.btnUpdateStatus);
        btnCallUser = findViewById(R.id.btnCallUser);
        progressBar = findViewById(R.id.progressBar);

        // Setup toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Incident Details");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize service
        incidentService = new FirebaseIncidentService();

        // Get incident ID from intent
        incidentId = getIntent().getStringExtra("incidentId");
        if (incidentId == null) {
            Toast.makeText(this, "Invalid incident ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Load incident details
        loadIncidentDetails();

        // Setup buttons
        btnViewMap.setOnClickListener(v -> openMap());
        btnUpdateStatus.setOnClickListener(v -> showUpdateStatusDialog());
        btnCallUser.setOnClickListener(v -> callUser());
    }

    private void loadIncidentDetails() {
        progressBar.setVisibility(View.VISIBLE);

        incidentService.getAllIncidents(new FirebaseIncidentService.IncidentsCallback() {
            @Override
            public void onSuccess(List<IncidentReport> incidents) {
                for (IncidentReport incident : incidents) {
                    if (incidentId.equals(incident.getReportId())) {
                        currentIncident = incident;
                        break;
                    }
                }

                if (currentIncident != null) {
                    runOnUiThread(() -> {
                        displayIncidentDetails();
                        progressBar.setVisibility(View.GONE);
                    });
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(IncidentDetailsActivity.this, "Incident not found", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        finish();
                    });
                }
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> {
                    Toast.makeText(IncidentDetailsActivity.this, "Error loading incident: " + errorMessage, Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    finish();
                });
            }
        });
    }

    private void displayIncidentDetails() {
        tvIncidentType.setText(currentIncident.getIncidentType());
        tvUserName.setText(currentIncident.getUserFullName());
        tvUserPhone.setText(currentIncident.getUserPhoneNumber());
        tvLocation.setText(currentIncident.getLocation());
        tvDescription.setText(currentIncident.getDescription());

        // Format timestamp
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault());
        tvTimestamp.setText(sdf.format(currentIncident.getTimestamp()));

        // Set status chip
        String status = currentIncident.getStatus();
        chipStatus.setText(status);

        if (Constants.INCIDENT_STATUS_PENDING.equals(status)) {
            chipStatus.setChipBackgroundColorResource(R.color.colorWarning);
        } else if (Constants.INCIDENT_STATUS_IN_PROGRESS.equals(status)) {
            chipStatus.setChipBackgroundColorResource(R.color.colorInfo);
        } else if (Constants.INCIDENT_STATUS_RESOLVED.equals(status)) {
            chipStatus.setChipBackgroundColorResource(R.color.colorSuccess);
        }
    }

    private void openMap() {
        // Open Google Maps with the incident location
        Uri gmmIntentUri = Uri.parse("geo:" + currentIncident.getLatitude() + "," + currentIncident.getLongitude() +
                "?q=" + currentIncident.getLatitude() + "," + currentIncident.getLongitude() +
                "(" + currentIncident.getIncidentType() + ")");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");

        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            Toast.makeText(this, "Google Maps app not installed", Toast.LENGTH_SHORT).show();
        }
    }

    private void showUpdateStatusDialog() {
        final String[] statusOptions = {
                Constants.INCIDENT_STATUS_PENDING,
                Constants.INCIDENT_STATUS_IN_PROGRESS,
                Constants.INCIDENT_STATUS_RESOLVED
        };

        final String[] statusLabels = {
                "Pending",
                "In Progress",
                "Resolved"
        };

        // Find current status index
        int currentIndex = 0;
        for (int i = 0; i < statusOptions.length; i++) {
            if (statusOptions[i].equals(currentIncident.getStatus())) {
                currentIndex = i;
                break;
            }
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle("Update Status")
                .setSingleChoiceItems(statusLabels, currentIndex, null)
                .setPositiveButton("Update", (dialog, which) -> {
                    int selectedPosition = ((androidx.appcompat.app.AlertDialog) dialog).getListView().getCheckedItemPosition();
                    if (selectedPosition != -1) {
                        updateIncidentStatus(statusOptions[selectedPosition]);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateIncidentStatus(String newStatus) {
        if (newStatus.equals(currentIncident.getStatus())) {
            return; // No change
        }

        progressBar.setVisibility(View.VISIBLE);

        incidentService.updateIncidentStatus(incidentId, newStatus, new FirebaseIncidentService.IncidentCallback() {
            @Override
            public void onSuccess(IncidentReport incident) {
                currentIncident = incident;

                runOnUiThread(() -> {
                    displayIncidentDetails();
                    Toast.makeText(IncidentDetailsActivity.this, "Status updated successfully", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> {
                    Toast.makeText(IncidentDetailsActivity.this, "Error updating status: " + errorMessage, Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                });
            }
        });
    }

    private void callUser() {
        String phoneNumber = currentIncident.getUserPhoneNumber();
        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            Uri uri = Uri.parse("tel:" + phoneNumber);
            Intent intent = new Intent(Intent.ACTION_DIAL, uri);
            startActivity(intent);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
