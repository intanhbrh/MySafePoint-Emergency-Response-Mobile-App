package com.example.mysafepoint.services;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.mysafepoint.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class FirebaseAuthService {
    private static final String TAG = "FirebaseAuthService";

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUserService userService;

    public FirebaseAuthService() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        userService = new FirebaseUserService();
    }

    public interface AuthCallback {
        void onSuccess(User user);
        void onError(String errorMessage);
    }

    public void registerUser(String fullName, String email, String phoneNumber, String nic, String password, final AuthCallback callback) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            if (firebaseUser != null) {
                                // Create a new user with Firebase userId
                                User newUser = new User(
                                        firebaseUser.getUid(),
                                        fullName,
                                        email,
                                        phoneNumber,
                                        nic,
                                        "user" // Default role is normal user
                                );

                                // Save user to Firestore
                                userService.saveUser(newUser, new FirebaseUserService.UserCallback() {
                                    @Override
                                    public void onSuccess(User user) {
                                        callback.onSuccess(user);
                                    }

                                    @Override
                                    public void onError(String errorMessage) {
                                        callback.onError(errorMessage);
                                    }
                                });
                            }
                        } else {
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            callback.onError(task.getException() != null ? task.getException().getMessage() : "Registration failed");
                        }
                    }
                });
    }

    public void loginUser(String email, String password, final AuthCallback callback) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            if (firebaseUser != null) {
                                // Get user from Firestore
                                userService.getUserById(firebaseUser.getUid(), new FirebaseUserService.UserCallback() {
                                    @Override
                                    public void onSuccess(User user) {
                                        callback.onSuccess(user);
                                    }

                                    @Override
                                    public void onError(String errorMessage) {
                                        callback.onError(errorMessage);
                                    }
                                });
                            }
                        } else {
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            callback.onError(task.getException() != null ? task.getException().getMessage() : "Authentication failed");
                        }
                    }
                });
    }

    public void logoutUser() {
        mAuth.signOut();
    }

    public FirebaseUser getCurrentFirebaseUser() {
        return mAuth.getCurrentUser();
    }

    public String getCurrentUserId() {
        FirebaseUser user = mAuth.getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    public boolean isUserLoggedIn() {
        return mAuth.getCurrentUser() != null;
    }
}