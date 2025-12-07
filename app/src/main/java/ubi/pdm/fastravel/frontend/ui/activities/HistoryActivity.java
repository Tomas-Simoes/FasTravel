package ubi.pdm.fastravel.frontend.ui.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ubi.pdm.fastravel.R;
import ubi.pdm.fastravel.frontend.HistoryModule.HistoryController;
import ubi.pdm.fastravel.frontend.HistoryModule.HistoryEntry;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private HistoryController adapter;
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // 1. Initialize Back Button
        btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Close the current Activity and return to the previous one
                onBackPressed();
            }
        });

        // 2. Mock Data
        List<HistoryEntry> mockTrips = createMockTrips();

        // 3. Configure RecyclerView
        recyclerView = findViewById(R.id.recycler_historico);

        // Ensure a LayoutManager is set
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Create and set the Adapter
        adapter = new HistoryController(this, mockTrips);
        recyclerView.setAdapter(adapter);
    }

    /**
     * Function to create a list of mock historyEntries.
     * *NOTE: Replace android.R.drawable.ic_menu_directions 
     * with your actual app drawables (e.g., R.drawable.ic_bus, R.drawable.ic_metro)*
     */
    private List<HistoryEntry> createMockTrips() {
        // Default Android icon for simulation
        int defaultIcon = android.R.drawable.ic_menu_directions;

        List<HistoryEntry> historyEntries = new ArrayList<>();

        historyEntries.add(new HistoryEntry(
                1,
                "Today, December 7, 2025",
                "25 min",
                "19:40",
                "20:05",
                "My House, Covilhã",
                "Praça do Município",
                defaultIcon
        ));

        historyEntries.add(new HistoryEntry(
                2,
                "December 5, 2025",
                "1h 15 min",
                "08:30",
                "09:45",
                "Gare do Oriente, Lisbon",
                "Praça do Comércio, Lisbon",
                defaultIcon
        ));

        historyEntries.add(new HistoryEntry(
                3,
                "November 28, 2025",
                "12 min",
                "13:10",
                "13:22",
                "University of Beira Interior (UBI)",
                "Alegro Shopping Center",
                defaultIcon
        ));

        return historyEntries;
    }
}