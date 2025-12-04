package ubi.pdm.fastravel.frontend.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

import ubi.pdm.fastravel.R;
import ubi.pdm.fastravel.frontend.util.HistoricoAdapter;
import ubi.pdm.fastravel.frontend.routes.Route;

public class HistoricoFragment extends Fragment {

    private RecyclerView recyclerView;
    private HistoricoAdapter adapter;
    private List<Route> listaViagens;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_historico, container, false);

        recyclerView = view.findViewById(R.id.recycler_historico);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Lista de viagens (vazia por enquanto, será preenchida depois)
        listaViagens = new ArrayList<>();

        // Dados de exemplo (remover quando implementar banco de dados)
        listaViagens.add(new Route("Lisboa", "Porto", "Comboio", "01/12/2025"));
        listaViagens.add(new Route("Coimbra", "Aveiro", "Autocarro", "28/11/2025"));
        listaViagens.add(new Route("Braga", "Faro", "Misto", "25/11/2025"));

        adapter = new HistoricoAdapter(listaViagens);
        recyclerView.setAdapter(adapter);

        return view;
    }
}