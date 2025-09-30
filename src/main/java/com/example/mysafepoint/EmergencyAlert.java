package com.example.mysafepoint.models;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class EmergencyAlert implements Serializable {
    private String alertId;
    private String userId;
    private String userFullName;
    private String incidentType; // "assault", "robbery", "kidnap", etc.
    private String location;
    private double latitude;
    private double longitude;
    private Date timestamp;

    // Empty constructor needed for Firebase
    public EmergencyAlert() {
    }

    public EmergencyAlert(String alertId, String userId, String userFullName, String incidentType,
                          String location, double latitude, double longitude) {
        this.alertId = alertId;
        this.userId = userId;
        this.userFullName = userFullName;
        this.incidentType = incidentType;
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = new Date();
    }

    // Convert EmergencyAlert object to Map for Firebase
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("alertId", alertId);
        map.put("userId", userId);
        map.put("userFullName", userFullName);
        map.put("incidentType", incidentType);
        map.put("location", location);
        map.put("latitude", latitude);
        map.put("longitude", longitude);
        map.put("timestamp", timestamp);
        return map;
    }

    // Getters and Setters
    public String getAlertId() {
        return alertId;
    }

    public void setAlertId(String alertId) {
        this.alertId = alertId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserFullName() {
        return userFullName;
    }

    public void setUserFullName(String userFullName) {
        this.userFullName = userFullName;
    }

    public String getIncidentType() {
        return incidentType;
    }

    public void setIncidentType(String incidentType) {
        this.incidentType = incidentType;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}