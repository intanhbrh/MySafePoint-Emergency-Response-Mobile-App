package com.example.mysafepoint.models;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class EmergencyContact implements Serializable {
    private String contactId;
    private String name;
    private String phoneNumber;
    private String relationship;

    // Empty constructor needed for Firebase
    public EmergencyContact() {
    }

    public EmergencyContact(String contactId, String name, String phoneNumber, String relationship) {
        this.contactId = contactId;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.relationship = relationship;
    }

    // Convert EmergencyContact object to Map for Firebase
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("contactId", contactId);
        map.put("name", name);
        map.put("phoneNumber", phoneNumber);
        map.put("relationship", relationship);
        return map;
    }

    // Getters and Setters
    public String getContactId() {
        return contactId;
    }

    public void setContactId(String contactId) {
        this.contactId = contactId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }
}