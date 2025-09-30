package com.example.mysafepoint.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mysafepoint.R;
import com.example.mysafepoint.models.EmergencyContact;
import com.example.mysafepoint.services.FirebaseUserService;
import com.example.mysafepoint.services.LocationService;
import com.example.mysafepoint.utils.Constants;
import com.example.mysafepoint.utils.PermissionUtils;
import com.example.mysafepoint.utils.SharedPrefManager;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private TextView tvNoMap;
    private GoogleMap mMap;
    private SupportMapFragment mapFragment;

    private LocationService locationService;
    private FirebaseUserService userService;
    private SharedPrefManager prefManager;

    private String userId;
    private String userName;

    private Handler locationUpdateHandler;
    private static final int LOCATION_UPDATE_INTERVAL = 30000; // 30 seconds

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        // Initialize views
        tvNoMap = view.findViewById(R.id.tvNoMap);

        // Initialize services
        locationService = new LocationService(requireContext());
        userService = new FirebaseUserService();
        prefManager = SharedPrefManager.getInstance(requireContext());
        locationUpdateHandler = new Handler(Looper.getMainLooper());

        // Get user data
        userId = prefManager.getString(Constants.KEY_USER_ID);
        userName = prefManager.getString(Constants.KEY_USER_NAME);

        // Check location permission
        if (PermissionUtils.hasLocationPermission(requireContext())) {
            // Initialize map
            mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
            if (mapFragment != null) {
                mapFragment.getMapAsync(this);
            }
        } else {
            tvNoMap.setVisibility(View.VISIBLE);
            tvNoMap.setText("Location permission required");
            PermissionUtils.requestLocationPermission(requireActivity());
        }

        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Enable my location button if permission granted
        if (PermissionUtils.hasLocationPermission(requireContext())) {
            try {
                mMap.setMyLocationEnabled(true);

                // Start updating location
                startLocationUpdates();

                // Load emergency contacts
                loadEmergencyContacts();
            } catch (SecurityException e) {
                Toast.makeText(requireContext(), "Location permission required", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startLocationUpdates() {
        // Get initial location
        updateUserLocation();

        // Schedule periodic updates
        locationUpdateHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateUserLocation();
                locationUpdateHandler.postDelayed(this, LOCATION_UPDATE_INTERVAL);
            }
        }, LOCATION_UPDATE_INTERVAL);
    }

    private void updateUserLocation() {
        locationService.getCurrentLocation(new LocationService.LocationCallback() {
            @Override
            public void onLocationReceived(double latitude, double longitude, String address) {
                if (mMap != null && isAdded()) {
                    LatLng userLocation = new LatLng(latitude, longitude);

                    // Clear previous marker if exists
                    mMap.clear();

                    // Add marker for user
                    mMap.addMarker(new MarkerOptions()
                            .position(userLocation)
                            .title("You are here")
                            .snippet(address)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

                    // Move camera to user location (only on first update)
                    if (mMap.getCameraPosition().zoom < 10) {
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
                    }

                    // Reload contacts after updating user location
                    loadEmergencyContacts();
                }
            }

            @Override
            public void onLocationError(String errorMessage) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), "Error getting location: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadEmergencyContacts() {
        userService.getEmergencyContacts(userId, new FirebaseUserService.EmergencyContactsCallback() {
            @Override
            public void onSuccess(List<EmergencyContact> contacts) {
                // In a real app, you would get the actual locations of the contacts from Firebase
                // For this example, we'll just add markers with fake positions around the user
                if (mMap != null && isAdded()) {
                    // Add markers for contacts (example locations)
                    for (int i = 0; i < contacts.size(); i++) {
                        EmergencyContact contact = contacts.get(i);

                        // Get user location from map (center)
                        LatLng userLocation = mMap.getCameraPosition().target;

                        // Create a fake location nearby for the contact (for demonstration)
                        double offsetLat = (Math.random() - 0.5) * 0.01; // Random offset within ~1km
                        double offsetLng = (Math.random() - 0.5) * 0.01;
                        LatLng contactLocation = new LatLng(
                                userLocation.latitude + offsetLat,
                                userLocation.longitude + offsetLng
                        );

                        // Add marker
                        mMap.addMarker(new MarkerOptions()
                                .position(contactLocation)
                                .title(contact.getName())
                                .snippet(contact.getRelationship())
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                    }
                }
            }

            @Override
            public void onError(String errorMessage) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), "Error loading contacts: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        // Stop location updates
        locationUpdateHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Resume location updates
        if (mMap != null && PermissionUtils.hasLocationPermission(requireContext())) {
            startLocationUpdates();
        }
    }
}
