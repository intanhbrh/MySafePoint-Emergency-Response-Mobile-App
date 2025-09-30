package com.example.mysafepoint.utils;

public class Constants {
    // Shared Preference Keys
    public static final String PREF_NAME = "SafePointPrefs";
    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_USER_NAME = "user_name";
    public static final String KEY_USER_EMAIL = "user_email";
    public static final String KEY_USER_PHONE = "user_phone";
    public static final String KEY_USER_TYPE = "user_type";

    // User Types
    public static final String USER_TYPE_NORMAL = "user";
    public static final String USER_TYPE_ADMIN = "admin";

    // Incident Types
    public static final String INCIDENT_TYPE_ASSAULT = "Assault/Harassment";
    public static final String INCIDENT_TYPE_ROBBERY = "Robbery/Theft";
    public static final String INCIDENT_TYPE_KIDNAP = "Kidnapping";
    public static final String INCIDENT_TYPE_OTHER = "Other";

    // Incident Status
    public static final String INCIDENT_STATUS_PENDING = "pending";
    public static final String INCIDENT_STATUS_IN_PROGRESS = "in_progress";
    public static final String INCIDENT_STATUS_RESOLVED = "resolved";

    // Permission Request Codes
    public static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    public static final int SMS_PERMISSION_REQUEST_CODE = 1002;

    // Fragment Tags
    public static final String TAG_HOME_FRAGMENT = "home_fragment";
    public static final String TAG_CIRCLES_FRAGMENT = "circles_fragment";
    public static final String TAG_REPORT_FRAGMENT = "report_fragment";
    public static final String TAG_MAP_FRAGMENT = "map_fragment";
    public static final String TAG_PROFILE_FRAGMENT = "profile_fragment";
}