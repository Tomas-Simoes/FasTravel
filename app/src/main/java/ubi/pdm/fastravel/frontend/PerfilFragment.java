package ubi.pdm.fastravel.frontend;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import ubi.pdm.fastravel.R;

public class PerfilFragment extends Fragment {

    private Button btnEditarPerfil, btnNotificacoes, btnSobre, btnSair;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_perfil, container, false);

        // Inicializa os botões
        btnEditarPerfil = view.findViewById(R.id.btn_editar_perfil);
        btnNotificacoes = view.findViewById(R.id.btn_notificacoes);
        btnSobre = view.findViewById(R.id.btn_sobre);
        btnSair = view.findViewById(R.id.btn_sair);

        // Placeholders para ações dos botões
        btnEditarPerfil.setOnClickListener(v ->
                Toast.makeText(getContext(), "Editar Perfil", Toast.LENGTH_SHORT).show()
        );

        btnNotificacoes.setOnClickListener(v ->
                Toast.makeText(getContext(), "Configurações de Notificações", Toast.LENGTH_SHORT).show()
        );

        btnSobre.setOnClickListener(v ->
                Toast.makeText(getContext(), "Sobre o FasTravel", Toast.LENGTH_SHORT).show()
        );

        btnSair.setOnClickListener(v ->
                Toast.makeText(getContext(), "Sair da Conta", Toast.LENGTH_SHORT).show()
        );

        return view;
    }
}