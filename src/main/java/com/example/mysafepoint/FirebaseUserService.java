package com.example.mysafepoint.services;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.mysafepoint.models.EmergencyContact;
import com.example.mysafepoint.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class FirebaseUserService {
    private static final String TAG = "FirebaseUserService";
    private static final String USERS_COLLECTION = "users";
    private static final String EMERGENCY_CONTACTS_COLLECTION = "emergency_contacts";

    private FirebaseFirestore db;

    public FirebaseUserService() {
        db = FirebaseFirestore.getInstance();
    }

    public interface UserCallback {
        void onSuccess(User user);
        void onError(String errorMessage);
    }

    public interface UsersCallback {
        void onSuccess(List<User> users);
        void onError(String errorMessage);
    }

    public interface EmergencyContactCallback {
        void onSuccess(EmergencyContact contact);
        void onError(String errorMessage);
    }

    public interface EmergencyContactsCallback {
        void onSuccess(List<EmergencyContact> contacts);
        void onError(String errorMessage);
    }

    // Save user to Firestore
    public void saveUser(User user, final UserCallback callback) {
        db.collection(USERS_COLLECTION).document(user.getUserId())
                .set(user.toMap())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "User saved successfully");
                        callback.onSuccess(user);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error saving user", e);
                        callback.onError(e.getMessage());
                    }
                });
    }

    // Get user by ID
    public void getUserById(String userId, final UserCallback callback) {
        db.collection(USERS_COLLECTION).document(userId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                User user = document.toObject(User.class);
                                // Get emergency contacts for this user
                                getEmergencyContacts(userId, new EmergencyContactsCallback() {
                                    @Override
                                    public void onSuccess(List<EmergencyContact> contacts) {
                                        user.setEmergencyContacts(contacts);
                                        callback.onSuccess(user);
                                    }

                                    @Override
                                    public void onError(String errorMessage) {
                                        // Still return the user even if getting contacts fails
                                        callback.onSuccess(user);
                                    }
                                });
                            } else {
                                callback.onError("User not found");
                            }
                        } else {
                            Log.w(TAG, "Error getting user", task.getException());
                            callback.onError(task.getException() != null ? task.getException().getMessage() : "Error getting user");
                        }
                    }
                });
    }

    // Update user profile
    public void updateUser(User user, final UserCallback callback) {
        db.collection(USERS_COLLECTION).document(user.getUserId())
                .update(user.toMap())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "User updated successfully");
                        callback.onSuccess(user);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating user", e);
                        callback.onError(e.getMessage());
                    }
                });
    }

    // Get all users (for admin)
    public void getAllUsers(final UsersCallback callback) {
        db.collection(USERS_COLLECTION)
                .whereEqualTo("userType", "user") // Only get normal users, not admins
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<User> users = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                User user = document.toObject(User.class);
                                users.add(user);
                            }
                            callback.onSuccess(users);
                        } else {
                            Log.w(TAG, "Error getting users", task.getException());
                            callback.onError(task.getException() != null ? task.getException().getMessage() : "Error getting users");
                        }
                    }
                });
    }

    // Add emergency contact
    public void addEmergencyContact(String userId, EmergencyContact contact, final EmergencyContactCallback callback) {
        CollectionReference contactsRef = db.collection(USERS_COLLECTION)
                .document(userId)
                .collection(EMERGENCY_CONTACTS_COLLECTION);

        contactsRef.add(contact.toMap())
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        contact.setContactId(documentReference.getId());
                        // Update with the generated ID
                        contactsRef.document(documentReference.getId())
                                .update("contactId", documentReference.getId())
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "Contact added successfully");
                                        callback.onSuccess(contact);
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding contact", e);
                        callback.onError(e.getMessage());
                    }
                });
    }

    // Get emergency contacts for a user
    public void getEmergencyContacts(String userId, final EmergencyContactsCallback callback) {
        db.collection(USERS_COLLECTION)
                .document(userId)
                .collection(EMERGENCY_CONTACTS_COLLECTION)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<EmergencyContact> contacts = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                EmergencyContact contact = document.toObject(EmergencyContact.class);
                                contacts.add(contact);
                            }
                            callback.onSuccess(contacts);
                        } else {
                            Log.w(TAG, "Error getting contacts", task.getException());
                            callback.onError(task.getException() != null ? task.getException().getMessage() : "Error getting contacts");
                        }
                    }
                });
    }

    // Update emergency contact
    public void updateEmergencyContact(String userId, EmergencyContact contact, final EmergencyContactCallback callback) {
        db.collection(USERS_COLLECTION)
                .document(userId)
                .collection(EMERGENCY_CONTACTS_COLLECTION)
                .document(contact.getContactId())
                .update(contact.toMap())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Contact updated successfully");
                        callback.onSuccess(contact);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating contact", e);
                        callback.onError(e.getMessage());
                    }
                });
    }

    // Delete emergency contact
    public void deleteEmergencyContact(String userId, String contactId, final EmergencyContactCallback callback) {
        db.collection(USERS_COLLECTION)
                .document(userId)
                .collection(EMERGENCY_CONTACTS_COLLECTION)
                .document(contactId)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Contact deleted successfully");
                        callback.onSuccess(null);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error deleting contact", e);
                        callback.onError(e.getMessage());
                    }
                });
    }
}