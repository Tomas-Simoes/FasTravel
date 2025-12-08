package ubi.pdm.fastravel.frontend.util;

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
import ubi.pdm.fastravel.frontend.ui.fragments.BuscarRotaFragment;

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

        StringBuilder tempoPartida = new StringBuilder();
        if (r.departureTimeText != null && !r.departureTimeText.isEmpty()) {
            tempoPartida.append("Partida ").append(r.departureTimeText);
        }
        StringBuilder tempoChegada = new StringBuilder();
        if (r.arrivalTimeText != null && !r.arrivalTimeText.isEmpty()) {
            if (tempoChegada.length() > 0) tempoChegada.append(" • ");
            tempoChegada.append("Chegada ").append(r.arrivalTimeText);
        }
        holder.tvChegada.setText(tempoChegada.toString());
        holder.tvPartida.setText(tempoPartida.toString());

        int iconRes;

        if (r.hasTransit) {
            String type = r.primaryVehicleType != null ? r.primaryVehicleType : "";

            if (type.contains("BUS")) {
                iconRes = R.drawable.ic_bus;

            } else if (type.contains("RAIL") || type.contains("TRAIN")) {
                iconRes = R.drawable.ic_train;

            } else if (type.contains("SUBWAY") || type.contains("METRO")) {
                iconRes = R.drawable.ic_train;

            } else if (type.contains("TRAM")) {
                iconRes = R.drawable.ic_tram;

            } else {
                iconRes = R.drawable.ic_navigation;
            }

        } else {

            if ("walking".equalsIgnoreCase(r.mode)) {
                iconRes = R.drawable.ic_walk;

            } else if ("bicycling".equalsIgnoreCase(r.mode)) {
                iconRes = R.drawable.ic_bike;

            } else {
                iconRes = R.drawable.ic_car;
            }
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
        TextView tvPartida, tvChegada;

        RouteViewHolder(@NonNull View itemView) {
            super(itemView);
            card = (MaterialCardView) itemView;
            ivMode = itemView.findViewById(R.id.iv_mode);
            tvTitulo = itemView.findViewById(R.id.tv_titulo);
            tvPartida = itemView.findViewById(R.id.tv_partida);
            tvChegada = itemView.findViewById(R.id.tv_chegada);
        }
    }
}
