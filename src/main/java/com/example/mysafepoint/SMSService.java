package com.example.mysafepoint.services;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.telephony.SmsManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.mysafepoint.models.EmergencyAlert;
import com.example.mysafepoint.models.EmergencyContact;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SMSService {
    private static final String TAG = "SMSService";
    private static final int SMS_PERMISSION_REQUEST_CODE = 1002;

    private Context context;

    public SMSService(Context context) {
        this.context = context;
    }

    public interface SMSCallback {
        void onSMSSent(EmergencyContact contact);
        void onSMSError(EmergencyContact contact, String errorMessage);
    }

    public boolean hasSMSPermission() {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;
    }

    public void requestSMSPermission(Activity activity) {
        ActivityCompat.requestPermissions(
                activity,
                new String[]{Manifest.permission.SEND_SMS},
                SMS_PERMISSION_REQUEST_CODE
        );
    }

    public void sendEmergencyAlertSMS(EmergencyAlert alert, List<EmergencyContact> contacts, final SMSCallback callback) {
        if (!hasSMSPermission()) {
            for (EmergencyContact contact : contacts) {
                callback.onSMSError(contact, "SMS permission not granted");
            }
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault());
        String timestamp = sdf.format(alert.getTimestamp() != null ? alert.getTimestamp() : new Date());

        String message = String.format(
                "EMERGENCY ALERT from %s\n" +
                        "Incident Type: %s\n" +
                        "Location: %s\n" +
                        "Time: %s\n" +
                        "Map Link: https://maps.google.com/?q=%s,%s",
                alert.getUserFullName(),
                alert.getIncidentType(),
                alert.getLocation(),
                timestamp,
                alert.getLatitude(),
                alert.getLongitude()
        );

        SmsManager smsManager = SmsManager.getDefault();

        for (EmergencyContact contact : contacts) {
            try {
                smsManager.sendTextMessage(
                        contact.getPhoneNumber(),
                        null,
                        message,
                        null,
                        null
                );
                Log.d(TAG, "SMS sent to " + contact.getName());
                callback.onSMSSent(contact);
            } catch (Exception e) {
                Log.e(TAG, "Error sending SMS to " + contact.getName(), e);
                callback.onSMSError(contact, "Error sending SMS: " + e.getMessage());
            }
        }
    }
}