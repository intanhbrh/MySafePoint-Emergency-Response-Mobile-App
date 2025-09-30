package com.example.mysafepoint.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mysafepoint.R;
import com.example.mysafepoint.models.IncidentReport;
import com.example.mysafepoint.utils.Constants;
import com.google.android.material.chip.Chip;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class IncidentAdapter extends RecyclerView.Adapter<IncidentAdapter.IncidentViewHolder> {

    private List<IncidentReport> incidentList;
    private IncidentItemClickListener listener;

    public interface IncidentItemClickListener {
        void onItemClick(IncidentReport incident, int position);
    }

    public IncidentAdapter(List<IncidentReport> incidentList, IncidentItemClickListener listener) {
        this.incidentList = incidentList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public IncidentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_incident, parent, false);
        return new IncidentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IncidentViewHolder holder, int position) {
        IncidentReport incident = incidentList.get(position);
        holder.bind(incident, position);
    }

    @Override
    public int getItemCount() {
        return incidentList.size();
    }

    class IncidentViewHolder extends RecyclerView.ViewHolder {
        TextView tvIncidentType, tvUserName, tvTimestamp, tvDescription;
        Chip chipStatus;

        public IncidentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvIncidentType = itemView.findViewById(R.id.tvIncidentType);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            chipStatus = itemView.findViewById(R.id.chipStatus);
        }

        public void bind(IncidentReport incident, int position) {
            tvIncidentType.setText(incident.getIncidentType());
            tvUserName.setText("From: " + incident.getUserFullName());

            // Format timestamp
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
            tvTimestamp.setText(sdf.format(incident.getTimestamp()));

            // Set description (truncate if too long)
            String description = incident.getDescription();
            if (description.length() > 100) {
                description = description.substring(0, 97) + "...";
            }
            tvDescription.setText(description);

            // Set status chip
            String status = incident.getStatus();
            chipStatus.setText(status);

            if (Constants.INCIDENT_STATUS_PENDING.equals(status)) {
                chipStatus.setChipBackgroundColorResource(R.color.colorWarning);
            } else if (Constants.INCIDENT_STATUS_IN_PROGRESS.equals(status)) {
                chipStatus.setChipBackgroundColorResource(R.color.colorInfo);
            } else if (Constants.INCIDENT_STATUS_RESOLVED.equals(status)) {
                chipStatus.setChipBackgroundColorResource(R.color.colorSuccess);
            }

            // Set click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(incident, position);
                }
            });
        }
    }
}