package ubi.pdm.fastravel.frontend.ui.fragments;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.List;

import ubi.pdm.fastravel.R;

public class RouteAdapter extends RecyclerView.Adapter<RouteAdapter.RouteViewHolder> {

    public interface OnRouteClickListener {
        void onRouteClick(BuscarRotaFragment.RouteInfo route);
    }

    private final List<BuscarRotaFragment.RouteInfo> routes;
    private final OnRouteClickListener listener;

    public RouteAdapter(List<BuscarRotaFragment.RouteInfo> routes,
                        OnRouteClickListener listener) {
        this.routes = routes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RouteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_rota, parent, false);
        return new RouteViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RouteViewHolder holder, int position) {
        BuscarRotaFragment.RouteInfo r = routes.get(position);

        StringBuilder titulo = new StringBuilder();
        if (r.summary != null && !r.summary.isEmpty()) {
            titulo.append(r.summary);
        } else {
            titulo.append("Rota ").append(position + 1);
        }
        if (r.durationText != null && !r.durationText.isEmpty()) {
            titulo.append(" • ").append(r.durationText);
        }
        holder.tvTitulo.setText(titulo.toString());

        StringBuilder tempo = new StringBuilder();
        if (r.departureTimeText != null && !r.departureTimeText.isEmpty()) {
            tempo.append("Partida ").append(r.departureTimeText);
        }
        if (r.arrivalTimeText != null && !r.arrivalTimeText.isEmpty()) {
            if (tempo.length() > 0) tempo.append(" • ");
            tempo.append("Chegada ").append(r.arrivalTimeText);
        }
        holder.tvTempo.setText(tempo.toString());

        if (!r.itineraryLines.isEmpty()) {
            StringBuilder sub = new StringBuilder();
            for (int i = 0; i < r.itineraryLines.size() && i < 3; i++) {
                if (i > 0) sub.append("\n");
                sub.append("• ").append(r.itineraryLines.get(i));
            }
            holder.tvItinerario.setText(sub.toString());
        } else {
            holder.tvItinerario.setText("Itinerário detalhado indisponível.");
        }

        int iconRes;
        if (r.hasTransit) {
            String type = r.primaryVehicleType != null ? r.primaryVehicleType : "";
            if (type.contains("BUS")) {
                iconRes = R.drawable.ic_bus;
            } else if (type.contains("RAIL") || type.contains("TRAIN")) {
                iconRes = R.drawable.ic_train;
            } else if (type.contains("SUBWAY") || type.contains("METRO")) {
                iconRes = R.drawable.ic_bus;
            } else if (type.contains("TRAM")) {
                iconRes = R.drawable.ic_bus;
            } else {
                iconRes = R.drawable.ic_navigation;
            }
        } else {
            iconRes = R.drawable.ic_car;
        }

        holder.ivMode.setImageResource(iconRes);

        holder.card.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRouteClick(r);
            }
        });
    }

    @Override
    public int getItemCount() {
        return routes.size();
    }

    static class RouteViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView card;
        ImageView ivMode;
        TextView tvTitulo;
        TextView tvTempo;
        TextView tvItinerario;

        RouteViewHolder(@NonNull View itemView) {
            super(itemView);
            card = (MaterialCardView) itemView;
            ivMode = itemView.findViewById(R.id.iv_mode);
            tvTitulo = itemView.findViewById(R.id.tv_titulo);
            tvTempo = itemView.findViewById(R.id.tv_tempo);
            tvItinerario = itemView.findViewById(R.id.tv_itinerario);
        }
    }
}
