package com.example.mysafepoint.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mysafepoint.R;
import com.example.mysafepoint.adapters.EmergencyContactAdapter;
import com.example.mysafepoint.models.EmergencyContact;
import com.example.mysafepoint.services.FirebaseUserService;
import com.example.mysafepoint.utils.Constants;
import com.example.mysafepoint.utils.SharedPrefManager;
import com.example.mysafepoint.utils.ValidationUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CirclesFragment extends Fragment implements EmergencyContactAdapter.ContactClickListener {

    private RecyclerView recyclerContacts;
    private TextView tvNoContacts;
    private FloatingActionButton fabAddContact;

    private EmergencyContactAdapter adapter;
    private List<EmergencyContact> contactList;

    private FirebaseUserService userService;
    private SharedPrefManager prefManager;
    private String userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_circles, container, false);

        // Initialize views
        recyclerContacts = view.findViewById(R.id.recyclerContacts);
        tvNoContacts = view.findViewById(R.id.tvNoContacts);
        fabAddContact = view.findViewById(R.id.fabAddContact);

        // Initialize services and data
        userService = new FirebaseUserService();
        prefManager = SharedPrefManager.getInstance(requireContext());
        userId = prefManager.getString(Constants.KEY_USER_ID);
        contactList = new ArrayList<>();

        // Setup RecyclerView
        adapter = new EmergencyContactAdapter(contactList, this);
        recyclerContacts.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerContacts.setAdapter(adapter);

        // Load emergency contacts
        loadEmergencyContacts();

        // Setup FAB click listener
        fabAddContact.setOnClickListener(v -> showAddContactDialog());

        return view;
    }

    private void loadEmergencyContacts() {
        userService.getEmergencyContacts(userId, new FirebaseUserService.EmergencyContactsCallback() {
            @Override
            public void onSuccess(List<EmergencyContact> contacts) {
                contactList.clear();
                contactList.addAll(contacts);

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        adapter.notifyDataSetChanged();
                        updateEmptyView();
                    });
                }
            }

            @Override
            public void onError(String errorMessage) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Error loading contacts: " + errorMessage, Toast.LENGTH_SHORT).show();
                        updateEmptyView();
                    });
                }
            }
        });
    }

    private void updateEmptyView() {
        if (contactList.isEmpty()) {
            tvNoContacts.setVisibility(View.VISIBLE);
            recyclerContacts.setVisibility(View.GONE);
        } else {
            tvNoContacts.setVisibility(View.GONE);
            recyclerContacts.setVisibility(View.VISIBLE);
        }
    }

    private void showAddContactDialog() {
        // Inflate the dialog view
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_contact, null);

        TextInputLayout tilName = dialogView.findViewById(R.id.tilName);
        TextInputLayout tilPhone = dialogView.findViewById(R.id.tilPhone);
        TextInputLayout tilRelationship = dialogView.findViewById(R.id.tilRelationship);

        TextInputEditText etName = dialogView.findViewById(R.id.etName);
        TextInputEditText etPhone = dialogView.findViewById(R.id.etPhone);
        TextInputEditText etRelationship = dialogView.findViewById(R.id.etRelationship);

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.add_contact)
                .setView(dialogView)
                .setPositiveButton(R.string.action_save, null) // Set later to prevent auto-dismiss
                .setNegativeButton(R.string.action_cancel, null);

        // Create and show the dialog
        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();

        // Set positive button click listener
        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            // Validate inputs
            boolean isValid = true;

            String name = etName.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String relationship = etRelationship.getText().toString().trim();

            if (name.isEmpty()) {
                tilName.setError("Name is required");
                isValid = false;
            } else {
                tilName.setError(null);
            }

            if (phone.isEmpty()) {
                tilPhone.setError("Phone number is required");
                isValid = false;
            } else if (!ValidationUtils.isValidPhone(phone)) {
                tilPhone.setError("Invalid phone number");
                isValid = false;
            } else {
                tilPhone.setError(null);
            }

            if (relationship.isEmpty()) {
                tilRelationship.setError("Relationship is required");
                isValid = false;
            } else {
                tilRelationship.setError(null);
            }

            if (isValid) {
                // Create new emergency contact
                EmergencyContact newContact = new EmergencyContact(
                        UUID.randomUUID().toString(), // Temporary ID, will be replaced by Firebase
                        name,
                        phone,
                        relationship
                );

                // Add to Firebase
                userService.addEmergencyContact(userId, newContact, new FirebaseUserService.EmergencyContactCallback() {
                    @Override
                    public void onSuccess(EmergencyContact contact) {
                        // Add to local list
                        contactList.add(contact);
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                adapter.notifyDataSetChanged();
                                updateEmptyView();
                                Toast.makeText(getContext(), R.string.contact_added, Toast.LENGTH_SHORT).show();
                            });
                        }
                        dialog.dismiss();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(getContext(), "Error adding contact: " + errorMessage, Toast.LENGTH_SHORT).show();
                            });
                        }
                    }
                });
            }
        });
    }

    private void showEditContactDialog(EmergencyContact contact, int position) {
        // Inflate the dialog view
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_contact, null);

        TextInputLayout tilName = dialogView.findViewById(R.id.tilName);
        TextInputLayout tilPhone = dialogView.findViewById(R.id.tilPhone);
        TextInputLayout tilRelationship = dialogView.findViewById(R.id.tilRelationship);

        TextInputEditText etName = dialogView.findViewById(R.id.etName);
        TextInputEditText etPhone = dialogView.findViewById(R.id.etPhone);
        TextInputEditText etRelationship = dialogView.findViewById(R.id.etRelationship);

        // Pre-fill with contact data
        etName.setText(contact.getName());
        etPhone.setText(contact.getPhoneNumber());
        etRelationship.setText(contact.getRelationship());

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.edit_contact)
                .setView(dialogView)
                .setPositiveButton(R.string.action_save, null) // Set later to prevent auto-dismiss
                .setNegativeButton(R.string.action_cancel, null);

        // Create and show the dialog
        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();

        // Set positive button click listener
        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            // Validate inputs
            boolean isValid = true;

            String name = etName.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String relationship = etRelationship.getText().toString().trim();

            if (name.isEmpty()) {
                tilName.setError("Name is required");
                isValid = false;
            } else {
                tilName.setError(null);
            }

            if (phone.isEmpty()) {
                tilPhone.setError("Phone number is required");
                isValid = false;
            } else if (!ValidationUtils.isValidPhone(phone)) {
                tilPhone.setError("Invalid phone number");
                isValid = false;
            } else {
                tilPhone.setError(null);
            }

            if (relationship.isEmpty()) {
                tilRelationship.setError("Relationship is required");
                isValid = false;
            } else {
                tilRelationship.setError(null);
            }

            if (isValid) {
                // Update contact data
                contact.setName(name);
                contact.setPhoneNumber(phone);
                contact.setRelationship(relationship);

                // Update in Firebase
                userService.updateEmergencyContact(userId, contact, new FirebaseUserService.EmergencyContactCallback() {
                    @Override
                    public void onSuccess(EmergencyContact updatedContact) {
                        // Update in local list
                        contactList.set(position, updatedContact);
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                adapter.notifyDataSetChanged();
                                Toast.makeText(getContext(), R.string.contact_updated, Toast.LENGTH_SHORT).show();
                            });
                        }
                        dialog.dismiss();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(getContext(), "Error updating contact: " + errorMessage, Toast.LENGTH_SHORT).show();
                            });
                        }
                    }
                });
            }
        });
    }

    @Override
    public void onEditClick(EmergencyContact contact, int position) {
        showEditContactDialog(contact, position);
    }

    @Override
    public void onDeleteClick(EmergencyContact contact, int position) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.delete_contact)
                .setMessage(R.string.confirm_delete_contact)
                .setPositiveButton(R.string.action_delete, (dialog, which) -> {
                    // Delete from Firebase
                    userService.deleteEmergencyContact(userId, contact.getContactId(), new FirebaseUserService.EmergencyContactCallback() {
                        @Override
                        public void onSuccess(EmergencyContact deletedContact) {
                            // Remove from local list
                            contactList.remove(position);
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    adapter.notifyDataSetChanged();
                                    updateEmptyView();
                                    Toast.makeText(getContext(), R.string.contact_deleted, Toast.LENGTH_SHORT).show();
                                });
                            }
                        }

                        @Override
                        public void onError(String errorMessage) {
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    Toast.makeText(getContext(), "Error deleting contact: " + errorMessage, Toast.LENGTH_SHORT).show();
                                });
                            }
                        }
                    });
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }
}