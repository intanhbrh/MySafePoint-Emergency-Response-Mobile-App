package com.example.mysafepoint.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class User implements Serializable {
    private String userId;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String nic; // National Identity Card
    private String userType; // "user" or "admin"
    private List<EmergencyContact> emergencyContacts;

    // Empty constructor needed for Firebase
    public User() {
        emergencyContacts = new ArrayList<>();
    }

    public User(String userId, String fullName, String email, String phoneNumber, String nic, String userType) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.nic = nic;
        this.userType = userType;
        this.emergencyContacts = new ArrayList<>();
    }

    // Convert User object to Map for Firebase
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("fullName", fullName);
        map.put("email", email);
        map.put("phoneNumber", phoneNumber);
        map.put("nic", nic);
        map.put("userType", userType);
        return map;
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getNic() {
        return nic;
    }

    public void setNic(String nic) {
        this.nic = nic;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public List<EmergencyContact> getEmergencyContacts() {
        return emergencyContacts;
    }

    public void setEmergencyContacts(List<EmergencyContact> emergencyContacts) {
        this.emergencyContacts = emergencyContacts;
    }

    public void addEmergencyContact(EmergencyContact contact) {
        this.emergencyContacts.add(contact);
    }
}
