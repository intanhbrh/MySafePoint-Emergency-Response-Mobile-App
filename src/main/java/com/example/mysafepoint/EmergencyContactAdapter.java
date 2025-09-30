// EmergencyContactAdapter.java
package com.example.mysafepoint.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mysafepoint.R;
import com.example.mysafepoint.models.EmergencyContact;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class EmergencyContactAdapter extends RecyclerView.Adapter<EmergencyContactAdapter.ContactViewHolder> {

    private List<EmergencyContact> contactList;
    private ContactClickListener listener;

    public interface ContactClickListener {
        void onEditClick(EmergencyContact contact, int position);
        void onDeleteClick(EmergencyContact contact, int position);
    }

    public EmergencyContactAdapter(List<EmergencyContact> contactList, ContactClickListener listener) {
        this.contactList = contactList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_emergency_contact, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        EmergencyContact contact = contactList.get(position);
        holder.bind(contact, position);
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    class ContactViewHolder extends RecyclerView.ViewHolder {
        CircleImageView ivContactImage;
        TextView tvContactName, tvContactPhone, tvContactRelationship;
        ImageView ivEdit, ivDelete;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            ivContactImage = itemView.findViewById(R.id.ivContactImage);
            tvContactName = itemView.findViewById(R.id.tvContactName);
            tvContactPhone = itemView.findViewById(R.id.tvContactPhone);
            tvContactRelationship = itemView.findViewById(R.id.tvContactRelationship);
            ivEdit = itemView.findViewById(R.id.ivEdit);
            ivDelete = itemView.findViewById(R.id.ivDelete);
        }

        public void bind(EmergencyContact contact, int position) {
            tvContactName.setText(contact.getName());
            tvContactPhone.setText(contact.getPhoneNumber());
            tvContactRelationship.setText("Relationship: " + contact.getRelationship());

            ivEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditClick(contact, position);
                }
            });

            ivDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(contact, position);
                }
            });
        }
    }
}

