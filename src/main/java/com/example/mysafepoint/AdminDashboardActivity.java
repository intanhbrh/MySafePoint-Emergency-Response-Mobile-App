package com.example.mysafepoint.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.mysafepoint.R;
import com.example.mysafepoint.adapters.IncidentAdapter;
import com.example.mysafepoint.models.IncidentReport;
import com.example.mysafepoint.services.FirebaseAuthService;
import com.example.mysafepoint.services.FirebaseIncidentService;
import com.example.mysafepoint.utils.Constants;
import com.example.mysafepoint.utils.SharedPrefManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class AdminDashboardActivity extends AppCompatActivity implements IncidentAdapter.IncidentItemClickListener {

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private RecyclerView recyclerIncidents;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView tvNoIncidents;

    private IncidentAdapter adapter;
    private List<IncidentReport> incidentList;

    private FirebaseIncidentService incidentService;
    private FirebaseAuthService authService;
    private SharedPrefManager prefManager;

    private static final int TAB_ALL = 0;
    private static final int TAB_PENDING = 1;
    private static final int TAB_IN_PROGRESS = 2;
    private static final int TAB_RESOLVED = 3;

    private int currentTab = TAB_ALL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // Initialize views
        toolbar = findViewById(R.id.toolbar);
        tabLayout = findViewById(R.id.tabLayout);
        recyclerIncidents = findViewById(R.id.recyclerIncidents);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        tvNoIncidents = findViewById(R.id.tvNoIncidents);

        // Setup toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Admin Dashboard");
        }

        // Initialize services
        incidentService = new FirebaseIncidentService();
        authService = new FirebaseAuthService();
        prefManager = SharedPrefManager.getInstance(this);

        // Setup RecyclerView
        incidentList = new ArrayList<>();
        adapter = new IncidentAdapter(incidentList, this);
        recyclerIncidents.setLayoutManager(new LinearLayoutManager(this));
        recyclerIncidents.setAdapter(adapter);

        // Setup TabLayout
        setupTabs();

        // Setup SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(this::loadIncidents);

        // Load incidents
        loadIncidents();
    }

    private void setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("All"));
        tabLayout.addTab(tabLayout.newTab().setText("Pending"));
        tabLayout.addTab(tabLayout.newTab().setText("In Progress"));
        tabLayout.addTab(tabLayout.newTab().setText("Resolved"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentTab = tab.getPosition();
                loadIncidents();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Not needed
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Not needed
            }
        });
    }

    private void loadIncidents() {
        swipeRefreshLayout.setRefreshing(true);

        incidentService.getAllIncidents(new FirebaseIncidentService.IncidentsCallback() {
            @Override
            public void onSuccess(List<IncidentReport> incidents) {
                incidentList.clear();

                // Filter by status if needed
                if (currentTab == TAB_PENDING) {
                    for (IncidentReport incident : incidents) {
                        if (Constants.INCIDENT_STATUS_PENDING.equals(incident.getStatus())) {
                            incidentList.add(incident);
                        }
                    }
                } else if (currentTab == TAB_IN_PROGRESS) {
                    for (IncidentReport incident : incidents) {
                        if (Constants.INCIDENT_STATUS_IN_PROGRESS.equals(incident.getStatus())) {
                            incidentList.add(incident);
                        }
                    }
                } else if (currentTab == TAB_RESOLVED) {
                    for (IncidentReport incident : incidents) {
                        if (Constants.INCIDENT_STATUS_RESOLVED.equals(incident.getStatus())) {
                            incidentList.add(incident);
                        }
                    }
                } else {
                    // All incidents
                    incidentList.addAll(incidents);
                }

                runOnUiThread(() -> {
                    adapter.notifyDataSetChanged();
                    swipeRefreshLayout.setRefreshing(false);
                    updateEmptyView();
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> {
                    swipeRefreshLayout.setRefreshing(false);
                    new MaterialAlertDialogBuilder(AdminDashboardActivity.this)
                            .setTitle("Error")
                            .setMessage("Failed to load incidents: " + errorMessage)
                            .setPositiveButton("Retry", (dialog, which) -> loadIncidents())
                            .setNegativeButton("Cancel", null)
                            .show();
                    updateEmptyView();
                });
            }
        });
    }

    private void updateEmptyView() {
        if (incidentList.isEmpty()) {
            tvNoIncidents.setVisibility(View.VISIBLE);
            recyclerIncidents.setVisibility(View.GONE);
        } else {
            tvNoIncidents.setVisibility(View.GONE);
            recyclerIncidents.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onItemClick(IncidentReport incident, int position) {
        // Open incident details activity
        Intent intent = new Intent(this, IncidentDetailsActivity.class);
        intent.putExtra("incidentId", incident.getReportId());
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload incidents when resuming
        loadIncidents();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_admin, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_logout) {
            showLogoutConfirmation();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showLogoutConfirmation() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> logout())
                .setNegativeButton("No", null)
                .show();
    }

    private void logout() {
        // Logout from Firebase
        authService.logoutUser();

        // Clear shared preferences
        prefManager.clear();

        // Navigate to login screen
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
