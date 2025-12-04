package ubi.pdm.fastravel.frontend.util;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

import ubi.pdm.fastravel.R;
import ubi.pdm.fastravel.frontend.routes.Route;

public class HistoricoAdapter extends RecyclerView.Adapter<HistoricoAdapter.HistoricoViewHolder> {

    private List<Route> listaViagens;

    public HistoricoAdapter(List<Route> listaViagens) {
        this.listaViagens = listaViagens;
    }

    @NonNull
    @Override
    public HistoricoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_historico, parent, false);
        return new HistoricoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoricoViewHolder holder, int position) {
        Route viagem = listaViagens.get(position);
        holder.textOrigem.setText("De: " + viagem.getOrigem());
        holder.textDestino.setText("Para: " + viagem.getDestino());
        holder.textTransporte.setText("Transporte: " + viagem.getTransporte());
        holder.textData.setText("Data: " + viagem.getData());
    }

    @Override
    public int getItemCount() {
        return listaViagens.size();
    }

    static class HistoricoViewHolder extends RecyclerView.ViewHolder {
        TextView textOrigem, textDestino, textTransporte, textData;

        public HistoricoViewHolder(@NonNull View itemView) {
            super(itemView);
            textOrigem = itemView.findViewById(R.id.text_origem);
            textDestino = itemView.findViewById(R.id.text_destino);
            textTransporte = itemView.findViewById(R.id.text_transporte);
            textData = itemView.findViewById(R.id.text_data);
        }
    }
}