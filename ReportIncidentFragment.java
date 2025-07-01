package com.example.safepoint.fragments;


import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.example.safepoint.R;
import com.example.safepoint.models.IncidentReport;
import com.example.safepoint.models.User;
import com.example.safepoint.services.FirebaseAuthService;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class ReportIncidentFragment extends Fragment {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;
    private static final int PERMISSION_REQUEST_CODE = 100;

    private Spinner spinnerIncidentType;
    private TextView tvCurrentLocation;
    private EditText etDescription;
    private ImageView ivIncidentImage;
    private Button btnSelectImage, btnSubmitReport, btnGetLocation;
    private ProgressBar progressBar;

    private FirebaseAuthService authService;
    private FirebaseFirestore firestore;
    private FirebaseStorage storage;
    private FusedLocationProviderClient fusedLocationClient;

    private User currentUser;
    private double currentLatitude = 0.0;
    private double currentLongitude = 0.0;
    private String currentPostcode = "";
    private String currentAddress = "";
    private Uri selectedImageUri;
    private Bitmap capturedImageBitmap;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        authService = new FirebaseAuthService();
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_report_incident, container, false);

        initializeViews(view);
        setupSpinner();
        setupClickListeners();
        loadUserData();
        checkPermissions();

        return view;
    }

    private void initializeViews(View view) {
        spinnerIncidentType = view.findViewById(R.id.spinnerIncidentType);
        tvCurrentLocation = view.findViewById(R.id.tvCurrentLocation);
        etDescription = view.findViewById(R.id.etDescription);
        ivIncidentImage = view.findViewById(R.id.ivIncidentImage);
        btnSelectImage = view.findViewById(R.id.btnSelectImage);
        btnSubmitReport = view.findViewById(R.id.btnSubmitReport);
        btnGetLocation = view.findViewById(R.id.btnGetLocation);
        progressBar = view.findViewById(R.id.progressBar);
    }

    private void setupSpinner() {
        String[] incidentTypes = {"Assault/Harassment", "Robbery", "Kidnapping", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, incidentTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerIncidentType.setAdapter(adapter);
    }

    private void setupClickListeners() {
        btnGetLocation.setOnClickListener(v -> getCurrentLocation());
        btnSelectImage.setOnClickListener(v -> showImageSelectionDialog());
        btnSubmitReport.setOnClickListener(v -> submitIncidentReport());
    }

    private void loadUserData() {
        String userId = authService.getCurrentUserId();
        if (userId != null) {
            authService.getUserById(userId, new FirebaseAuthService.UserCallback() {
                @Override
                public void onSuccess(User user) {
                    currentUser = user;
                }

                @Override
                public void onError(String errorMessage) {
                    Toast.makeText(getContext(), "Error loading user data", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void checkPermissions() {
        String[] permissions = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.CAMERA
        };

        boolean allPermissionsGranted = true;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(requireContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                allPermissionsGranted = false;
                break;
            }
        }

        if (!allPermissionsGranted) {
            ActivityCompat.requestPermissions(requireActivity(), permissions, PERMISSION_REQUEST_CODE);
        } else {
            getCurrentLocation(); // Auto-get location when permissions are available
        }
    }

    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getContext(), "Location permission required", Toast.LENGTH_SHORT).show();
            return;
        }

        btnGetLocation.setEnabled(false);
        tvCurrentLocation.setText("Getting location...");

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    btnGetLocation.setEnabled(true);
                    if (location != null) {
                        currentLatitude = location.getLatitude();
                        currentLongitude = location.getLongitude();
                        getAddressFromLocation(location);
                    } else {
                        tvCurrentLocation.setText("Location not available");
                    }
                })
                .addOnFailureListener(e -> {
                    btnGetLocation.setEnabled(true);
                    tvCurrentLocation.setText("Failed to get location");
                });
    }

    private void getAddressFromLocation(Location location) {
        try {
            Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                currentPostcode = address.getPostalCode() != null ? address.getPostalCode() : "";

                StringBuilder addressBuilder = new StringBuilder();
                if (address.getFeatureName() != null) {
                    addressBuilder.append(address.getFeatureName()).append(", ");
                }
                if (address.getLocality() != null) {
                    addressBuilder.append(address.getLocality()).append(", ");
                }
                if (address.getAdminArea() != null) {
                    addressBuilder.append(address.getAdminArea());
                }

                currentAddress = addressBuilder.toString();

                String locationText = String.format("📍 %s\nPostcode: %s\nCoordinates: %.6f, %.6f",
                        currentAddress, currentPostcode, currentLatitude, currentLongitude);
                tvCurrentLocation.setText(locationText);
            } else {
                currentAddress = String.format("Lat: %.6f, Lng: %.6f", currentLatitude, currentLongitude);
                tvCurrentLocation.setText(currentAddress);
            }
        } catch (IOException e) {
            currentAddress = String.format("Lat: %.6f, Lng: %.6f", currentLatitude, currentLongitude);
            tvCurrentLocation.setText(currentAddress);
        }
    }

    private void showImageSelectionDialog() {
        String[] options = {"Take Photo", "Select from Gallery"};
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
        builder.setTitle("Select Image")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        openCamera();
                    } else {
                        openGallery();
                    }
                })
                .show();
    }

    private void openCamera() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getContext(), "Camera permission required", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void openGallery() {
        Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhotoIntent, REQUEST_IMAGE_PICK);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE && data != null) {
                Bundle extras = data.getExtras();
                capturedImageBitmap = (Bitmap) extras.get("data");
                ivIncidentImage.setImageBitmap(capturedImageBitmap);
                ivIncidentImage.setVisibility(View.VISIBLE);
                btnSelectImage.setText("Change Image");
            } else if (requestCode == REQUEST_IMAGE_PICK && data != null) {
                selectedImageUri = data.getData();
                ivIncidentImage.setImageURI(selectedImageUri);
                ivIncidentImage.setVisibility(View.VISIBLE);
                btnSelectImage.setText("Change Image");
            }
        }
    }

    private void submitIncidentReport() {
        if (!validateInput()) {
            return;
        }

        if (currentUser == null) {
            Toast.makeText(getContext(), "User data not loaded", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);

        String incidentType = spinnerIncidentType.getSelectedItem().toString().toLowerCase().replace("/", "_");
        String description = etDescription.getText().toString().trim();

        IncidentReport report = new IncidentReport(
                currentUser.getUserId(),
                currentUser.getFullName(),
                currentUser.getPhoneNumber(),
                incidentType,
                description,
                currentAddress,
                currentPostcode,
                currentLatitude,
                currentLongitude
        );

        if (capturedImageBitmap != null || selectedImageUri != null) {
            uploadImageAndSaveReport(report);
        } else {
            saveReportToFirestore(report);
        }
    }

    private boolean validateInput() {
        if (spinnerIncidentType.getSelectedItem() == null) {
            Toast.makeText(getContext(), "Please select incident type", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (currentLatitude == 0.0 && currentLongitude == 0.0) {
            Toast.makeText(getContext(), "Please get your current location first", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void uploadImageAndSaveReport(IncidentReport report) {
        StorageReference storageRef = storage.getReference();
        StorageReference imageRef = storageRef.child("incident_images/" + report.getReportId() + ".jpg");

        if (capturedImageBitmap != null) {
            // Upload captured image
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            capturedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
            byte[] data = baos.toByteArray();

            imageRef.putBytes(data)
                    .addOnSuccessListener(taskSnapshot -> {
                        imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            report.setImageUrl(uri.toString());
                            saveReportToFirestore(report);
                        });
                    })
                    .addOnFailureListener(e -> {
                        showLoading(false);
                        Toast.makeText(getContext(), "Failed to upload image", Toast.LENGTH_SHORT).show();
                    });
        } else if (selectedImageUri != null) {
            // Upload selected image
            imageRef.putFile(selectedImageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            report.setImageUrl(uri.toString());
                            saveReportToFirestore(report);
                        });
                    })
                    .addOnFailureListener(e -> {
                        showLoading(false);
                        Toast.makeText(getContext(), "Failed to upload image", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void saveReportToFirestore(IncidentReport report) {
        firestore.collection("incident_reports")
                .document(report.getReportId())
                .set(report)
                .addOnSuccessListener(aVoid -> {
                    showLoading(false);
                    Toast.makeText(getContext(), "Incident report submitted successfully", Toast.LENGTH_SHORT).show();
                    clearForm();
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(getContext(), "Failed to submit report: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void clearForm() {
        spinnerIncidentType.setSelection(0);
        etDescription.setText("");
        ivIncidentImage.setVisibility(View.GONE);
        btnSelectImage.setText("Add Image (Optional)");
        selectedImageUri = null;
        capturedImageBitmap = null;

        // Keep location as it might still be relevant
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnSubmitReport.setEnabled(!show);
        btnSelectImage.setEnabled(!show);
        btnGetLocation.setEnabled(!show);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean locationPermissionGranted = false;
            for (int i = 0; i < permissions.length; i++) {
                if ((permissions[i].equals(Manifest.permission.ACCESS_FINE_LOCATION) ||
                        permissions[i].equals(Manifest.permission.ACCESS_COARSE_LOCATION)) &&
                        grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true;
                    break;
                }
            }

            if (locationPermissionGranted) {
                getCurrentLocation();
            } else {
                Toast.makeText(getContext(), "Location permission is required for incident reporting", Toast.LENGTH_LONG).show();
            }
        }
    }
}