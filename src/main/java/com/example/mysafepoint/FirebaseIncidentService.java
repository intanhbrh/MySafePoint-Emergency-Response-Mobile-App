package com.example.mysafepoint.services;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.mysafepoint.models.EmergencyAlert;
import com.example.mysafepoint.models.IncidentReport;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class FirebaseIncidentService {
    private static final String TAG = "FirebaseIncidentService";
    private static final String INCIDENTS_COLLECTION = "incidents";
    private static final String ALERTS_COLLECTION = "alerts";

    private FirebaseFirestore db;

    public FirebaseIncidentService() {
        db = FirebaseFirestore.getInstance();
    }

    public interface IncidentCallback {
        void onSuccess(IncidentReport incident);
        void onError(String errorMessage);
    }

    public interface IncidentsCallback {
        void onSuccess(List<IncidentReport> incidents);
        void onError(String errorMessage);
    }

    public interface AlertCallback {
        void onSuccess(EmergencyAlert alert);
        void onError(String errorMessage);
    }

    public interface AlertsCallback {
        void onSuccess(List<EmergencyAlert> alerts);
        void onError(String errorMessage);
    }

    // Report a new incident
    public void reportIncident(IncidentReport incident, final IncidentCallback callback) {
        db.collection(INCIDENTS_COLLECTION)
                .add(incident.toMap())
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        incident.setReportId(documentReference.getId());
                        // Update with the generated ID
                        db.collection(INCIDENTS_COLLECTION)
                                .document(documentReference.getId())
                                .update("reportId", documentReference.getId())
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "Incident reported successfully");
                                        callback.onSuccess(incident);
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error reporting incident", e);
                        callback.onError(e.getMessage());
                    }
                });
    }

    // Get all incidents (for admin)
    public void getAllIncidents(final IncidentsCallback callback) {
        db.collection(INCIDENTS_COLLECTION)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<IncidentReport> incidents = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                IncidentReport incident = document.toObject(IncidentReport.class);
                                incidents.add(incident);
                            }
                            callback.onSuccess(incidents);
                        } else {
                            Log.w(TAG, "Error getting incidents", task.getException());
                            callback.onError(task.getException() != null ? task.getException().getMessage() : "Error getting incidents");
                        }
                    }
                });
    }

    // Get incidents for a specific user
    public void getUserIncidents(String userId, final IncidentsCallback callback) {
        db.collection(INCIDENTS_COLLECTION)
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<IncidentReport> incidents = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                IncidentReport incident = document.toObject(IncidentReport.class);
                                incidents.add(incident);
                            }
                            callback.onSuccess(incidents);
                        } else {
                            Log.w(TAG, "Error getting user incidents", task.getException());
                            callback.onError(task.getException() != null ? task.getException().getMessage() : "Error getting user incidents");
                        }
                    }
                });
    }

    // Update incident status (for admin)
    public void updateIncidentStatus(String incidentId, String status, final IncidentCallback callback) {
        db.collection(INCIDENTS_COLLECTION)
                .document(incidentId)
                .update("status", status)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Get the updated incident
                        db.collection(INCIDENTS_COLLECTION)
                                .document(incidentId)
                                .get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    IncidentReport incident = documentSnapshot.toObject(IncidentReport.class);
                                    Log.d(TAG, "Incident status updated successfully");
                                    callback.onSuccess(incident);
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating incident status", e);
                        callback.onError(e.getMessage());
                    }
                });
    }

    // Create a new emergency alert
    public void createEmergencyAlert(EmergencyAlert alert, final AlertCallback callback) {
        db.collection(ALERTS_COLLECTION)
                .add(alert.toMap())
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        alert.setAlertId(documentReference.getId());
                        // Update with the generated ID
                        db.collection(ALERTS_COLLECTION)
                                .document(documentReference.getId())
                                .update("alertId", documentReference.getId())
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "Emergency alert created successfully");
                                        callback.onSuccess(alert);
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error creating emergency alert", e);
                        callback.onError(e.getMessage());
                    }
                });
    }

    // Get emergency alerts for a user
    public void getUserAlerts(String userId, final AlertsCallback callback) {
        db.collection(ALERTS_COLLECTION)
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<EmergencyAlert> alerts = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                EmergencyAlert alert = document.toObject(EmergencyAlert.class);
                                alerts.add(alert);
                            }
                            callback.onSuccess(alerts);
                        } else {
                            Log.w(TAG, "Error getting user alerts", task.getException());
                            callback.onError(task.getException() != null ? task.getException().getMessage() : "Error getting user alerts");
                        }
                    }
                });
    }

    // Get all emergency alerts (for admin)
    public void getAllAlerts(final AlertsCallback callback) {
        db.collection(ALERTS_COLLECTION)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<EmergencyAlert> alerts = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                EmergencyAlert alert = document.toObject(EmergencyAlert.class);
                                alerts.add(alert);
                            }
                            callback.onSuccess(alerts);
                        } else {
                            Log.w(TAG, "Error getting alerts", task.getException());
                            callback.onError(task.getException() != null ? task.getException().getMessage() : "Error getting alerts");
                        }
                    }
                });
    }
}