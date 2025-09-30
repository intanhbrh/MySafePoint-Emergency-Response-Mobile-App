package com.example.mysafepoint.utils;

import android.text.TextUtils;
import android.util.Patterns;

public class ValidationUtils {

    public static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean isValidPhone(String phone) {
        return !TextUtils.isEmpty(phone) && Patterns.PHONE.matcher(phone).matches();
    }

    public static boolean isValidPassword(String password) {
        // Password must be at least 6 characters
        return !TextUtils.isEmpty(password) && password.length() >= 6;
    }

    public static boolean isValidName(String name) {
        return !TextUtils.isEmpty(name) && name.length() >= 3;
    }

    public static boolean isEmptyOrNull(String text) {
        return TextUtils.isEmpty(text) || text.equalsIgnoreCase("null");
    }
}