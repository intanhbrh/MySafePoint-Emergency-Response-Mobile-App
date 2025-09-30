package com.example.mysafepoint.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class PermissionUtils {

    public static boolean hasLocationPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean hasSMSPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestLocationPermission(Activity activity) {
        ActivityCompat.requestPermissions(
                activity,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                Constants.LOCATION_PERMISSION_REQUEST_CODE
        );
    }

    public static void requestSMSPermission(Activity activity) {
        ActivityCompat.requestPermissions(
                activity,
                new String[]{Manifest.permission.SEND_SMS},
                Constants.SMS_PERMISSION_REQUEST_CODE
        );
    }

    public static void requestRequiredPermissions(Activity activity) {
        List<String> permissionsNeeded = new ArrayList<>();

        if (!hasLocationPermission(activity)) {
            permissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
            permissionsNeeded.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }

        if (!hasSMSPermission(activity)) {
            permissionsNeeded.add(Manifest.permission.SEND_SMS);
        }

        if (!permissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(
                    activity,
                    permissionsNeeded.toArray(new String[0]),
                    1000 // Generic request code
            );
        }
    }
}
