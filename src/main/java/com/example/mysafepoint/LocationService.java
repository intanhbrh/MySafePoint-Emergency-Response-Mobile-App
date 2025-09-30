package com.example.mysafepoint.services;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LocationService {
    private static final String TAG = "LocationService";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private Context context;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    public LocationService(Context context) {
        this.context = context;
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        createLocationRequest();
    }

    public interface LocationCallback {
        void onLocationReceived(double latitude, double longitude, String address);
        void onLocationError(String errorMessage);
    }

    private void createLocationRequest() {
        locationRequest = new LocationRequest.Builder(1000) // Update interval in milliseconds
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .setMinUpdateIntervalMillis(500) // Fastest update interval in milliseconds
                .build();
    }

    public boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public void requestLocationPermission(Activity activity) {
        ActivityCompat.requestPermissions(
                activity,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE
        );
    }

    public void getCurrentLocation(final LocationCallback callback) {
        if (hasLocationPermission()) {
            fusedLocationClient.getLastLocation()
                    .addOnCompleteListener(new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                Location location = task.getResult();
                                double latitude = location.getLatitude();
                                double longitude = location.getLongitude();

                                // Get address from coordinates
                                getAddressFromLocation(latitude, longitude, new AddressCallback() {
                                    @Override
                                    public void onAddressReceived(String address) {
                                        callback.onLocationReceived(latitude, longitude, address);
                                    }

                                    @Override
                                    public void onAddressError(String errorMessage) {
                                        // Still return location even if address retrieval fails
                                        callback.onLocationReceived(latitude, longitude, "Unknown address");
                                    }
                                });
                            } else {
                                // If last location is null, request location updates
                                requestLocationUpdates(callback);
                            }
                        }
                    });
        } else {
            callback.onLocationError("Location permission not granted");
        }
    }

    private void requestLocationUpdates(final LocationCallback callback) {
        if (hasLocationPermission()) {
            locationCallback = new com.google.android.gms.location.LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (locationResult == null) {
                        callback.onLocationError("Could not get location");
                        return;
                    }

                    for (Location location : locationResult.getLocations()) {
                        if (location != null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();

                            // Get address from coordinates
                            getAddressFromLocation(latitude, longitude, new AddressCallback() {
                                @Override
                                public void onAddressReceived(String address) {
                                    callback.onLocationReceived(latitude, longitude, address);
                                }

                                @Override
                                public void onAddressError(String errorMessage) {
                                    // Still return location even if address retrieval fails
                                    callback.onLocationReceived(latitude, longitude, "Unknown address");
                                }
                            });

                            // Stop location updates once we get a location
                            stopLocationUpdates();
                            return;
                        }
                    }
                }
            };

            try {
                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
            } catch (SecurityException e) {
                Log.e(TAG, "Location permission exception", e);
                callback.onLocationError("Location permission exception: " + e.getMessage());
            }
        } else {
            callback.onLocationError("Location permission not granted");
        }
    }

    private void stopLocationUpdates() {
        if (locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    private interface AddressCallback {
        void onAddressReceived(String address);
        void onAddressError(String errorMessage);
    }

    private void getAddressFromLocation(double latitude, double longitude, final AddressCallback callback) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                StringBuilder sb = new StringBuilder();

                // Add each address line if available
                for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                    sb.append(address.getAddressLine(i));
                    if (i < address.getMaxAddressLineIndex()) {
                        sb.append(", ");
                    }
                }

                callback.onAddressReceived(sb.toString());
            } else {
                callback.onAddressError("No address found for the location");
            }
        } catch (IOException e) {
            Log.e(TAG, "Error getting address from location", e);
            callback.onAddressError("Error getting address: " + e.getMessage());
        }
    }
}