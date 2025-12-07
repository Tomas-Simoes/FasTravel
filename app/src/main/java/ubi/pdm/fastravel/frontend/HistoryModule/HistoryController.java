package ubi.pdm.fastravel.frontend.HistoryModule;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.List;

import ubi.pdm.fastravel.R;

public class HistoryController extends RecyclerView.Adapter<HistoryController.TripViewHolder> {

    private final List<HistoryEntry> historyEntries;
    private final Context context;

    public HistoryController(Context context, List<HistoryEntry> historyEntries) {
        this.context = context;
        this.historyEntries = historyEntries;
    }

    @NonNull
    @Override
    public TripViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_historico, parent, false);
        return new TripViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TripViewHolder holder, int position) {
        final HistoryEntry HistoryEntry = historyEntries.get(position);

        // 1. Main Icon
        holder.ivMode.setImageResource(HistoryEntry.getMainModeIcon());

        // 2. Date of the HistoryEntry
        holder.tvDate.setText(HistoryEntry.getDate());

        // 3. Duration and Times
        String timeText = HistoryEntry.getDuration() + " • " + HistoryEntry.getStartTime() + " → " + HistoryEntry.getEndTime();
        holder.tvTimeDuration.setText(timeText);

        // 4. Origin and Destination
        holder.tvOrigin.setText("Origin: " + HistoryEntry.getOrigin());
        holder.tvDestination.setText("Destination: " + HistoryEntry.getDestination());

        // Optional: Card Click Listener
        holder.cardRoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigation logic or detail display
                Toast.makeText(context, "Details for HistoryEntry ID: " + HistoryEntry.getId(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return historyEntries.size();
    }

    // ViewHolder
    public static class TripViewHolder extends RecyclerView.ViewHolder {
        public final MaterialCardView cardRoot;
        public final ImageView ivMode;
        public final TextView tvDate;
        public final TextView tvTimeDuration;
        public final TextView tvOrigin;
        public final TextView tvDestination;

        public TripViewHolder(View itemView) {
            super(itemView);
            cardRoot = itemView.findViewById(R.id.card_root);
            ivMode = itemView.findViewById(R.id.iv_mode);
            // Renaming the IDs used in your previous XML for clarity in the class:
            tvDate = itemView.findViewById(R.id.tv_titulo); // Changed from tv_titulo to show date
            tvTimeDuration = itemView.findViewById(R.id.tv_tempo); // Changed from tv_tempo to show duration/times
            tvOrigin = itemView.findViewById(R.id.tv_origem);
            tvDestination = itemView.findViewById(R.id.tv_destino);
        }
    }
}