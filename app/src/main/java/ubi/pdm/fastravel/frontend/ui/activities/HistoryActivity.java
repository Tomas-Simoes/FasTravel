package ubi.pdm.fastravel.frontend.ui.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ubi.pdm.fastravel.R;
import ubi.pdm.fastravel.frontend.DataPersistenceModule.User.HistoryRepository;
import ubi.pdm.fastravel.frontend.DataPersistenceModule.User.UserHistory;
import ubi.pdm.fastravel.frontend.HistoryModule.HistoryController;
import ubi.pdm.fastravel.frontend.HistoryModule.HistoryEntry;
import android.widget.ImageView;
public class HistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private HistoryController adapter;
    private ImageView btnBack;
    private ProgressBar progressBar;
    private LinearLayout emptyState;

    private HistoryRepository historyRepo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        historyRepo = new HistoryRepository(this);

        btnBack = findViewById(R.id.btn_back);
        recyclerView = findViewById(R.id.recycler_historico);
        progressBar = findViewById(R.id.progress_bar);
        emptyState = findViewById(R.id.empty_state);

        btnBack.setOnClickListener(v -> onBackPressed());

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadHistory();
    }

    private void loadHistory() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        emptyState.setVisibility(View.GONE);

        new Thread(() -> {
            List<UserHistory> histories = historyRepo.getHistoriesFromCacheOrApi();

            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);

                if (histories != null && !histories.isEmpty()) {
                    List<HistoryEntry> historyEntries = convertToHistoryEntries(histories);

                    adapter = new HistoryController(this, historyEntries);
                    recyclerView.setAdapter(adapter);
                    recyclerView.setVisibility(View.VISIBLE);
                } else {
                    emptyState.setVisibility(View.VISIBLE);
                }
            });
        }).start();
    }

    private List<HistoryEntry> convertToHistoryEntries(List<UserHistory> histories) {
        List<HistoryEntry> entries = new ArrayList<>();
        int defaultIcon = android.R.drawable.ic_menu_directions;

        for (UserHistory history : histories) {
            HistoryEntry entry = new HistoryEntry(
                    history.id,
                    formatDate(history.travel_date),
                    "",
                    "",
                    "",
                    history.origin,
                    history.destiny,
                    defaultIcon
            );
            entries.add(entry);
        }

        return entries;
    }

    private String formatDate(String dateString) {
        try {
            String[] parts = dateString.split("-");
            if (parts.length == 3) {
                int year = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]);
                int day = Integer.parseInt(parts[2]);

                String monthName = getMonthName(month);
                return day + " de " + monthName + ", " + year;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dateString;
    }

    private String getMonthName(int month) {
        String[] months = {
                "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
                "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
        };
        if (month >= 1 && month <= 12) {
            return months[month - 1];
        }
        return "";
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadHistory();
    }
}