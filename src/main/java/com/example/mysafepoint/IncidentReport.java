package com.example.mysafepoint.models;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class IncidentReport implements Serializable {
    private String reportId;
    private String userId;
    private String userFullName;
    private String userPhoneNumber;
    private String incidentType; // "assault", "robbery", "kidnap", etc.
    private String description;
    private String location;
    private double latitude;
    private double longitude;
    private Date timestamp;
    private String status; // "pending", "in_progress", "resolved"

    // Empty constructor needed for Firebase
    public IncidentReport() {
    }

    public IncidentReport(String reportId, String userId, String userFullName, String userPhoneNumber,
                          String incidentType, String description, String location,
                          double latitude, double longitude) {
        this.reportId = reportId;
        this.userId = userId;
        this.userFullName = userFullName;
        this.userPhoneNumber = userPhoneNumber;
        this.incidentType = incidentType;
        this.description = description;
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = new Date();
        this.status = "pending";
    }

    // Convert IncidentReport object to Map for Firebase
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("reportId", reportId);
        map.put("userId", userId);
        map.put("userFullName", userFullName);
        map.put("userPhoneNumber", userPhoneNumber);
        map.put("incidentType", incidentType);
        map.put("description", description);
        map.put("location", location);
        map.put("latitude", latitude);
        map.put("longitude", longitude);
        map.put("timestamp", timestamp);
        map.put("status", status);
        return map;
    }

    // Getters and Setters
    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
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

    public String getUserPhoneNumber() {
        return userPhoneNumber;
    }

    public void setUserPhoneNumber(String userPhoneNumber) {
        this.userPhoneNumber = userPhoneNumber;
    }

    public String getIncidentType() {
        return incidentType;
    }

    public void setIncidentType(String incidentType) {
        this.incidentType = incidentType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
