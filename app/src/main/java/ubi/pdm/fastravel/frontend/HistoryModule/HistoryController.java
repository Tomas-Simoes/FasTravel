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
        final HistoryEntry historyEntry = historyEntries.get(position);

        // 1. Main Icon
        holder.ivMode.setImageResource(historyEntry.getMainModeIcon());

        // 2. Date of the trip (agora é o título principal)
        holder.tvDate.setText(historyEntry.getDate());

        // 3. Origin
        holder.tvOrigin.setText(historyEntry.getOrigin());

        // 4. Destination
        holder.tvDestination.setText(historyEntry.getDestination());

        // Optional: Card Click Listener
        holder.cardRoot.setOnClickListener(v -> {
            // Navigation logic or detail display
            Toast.makeText(context,
                    "Viagem: " + historyEntry.getOrigin() + " → " + historyEntry.getDestination(),
                    Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return historyEntries != null ? historyEntries.size() : 0;
    }

    // ViewHolder
    public static class TripViewHolder extends RecyclerView.ViewHolder {
        public final MaterialCardView cardRoot;
        public final ImageView ivMode;
        public final TextView tvDate;
        public final TextView tvOrigin;
        public final TextView tvDestination;

        public TripViewHolder(View itemView) {
            super(itemView);
            cardRoot = itemView.findViewById(R.id.card_root);
            ivMode = itemView.findViewById(R.id.iv_mode);
            tvDate = itemView.findViewById(R.id.tv_titulo);      // Data da viagem
            tvOrigin = itemView.findViewById(R.id.tv_origem);    // Origem
            tvDestination = itemView.findViewById(R.id.tv_destino); // Destino
        }
    }
}