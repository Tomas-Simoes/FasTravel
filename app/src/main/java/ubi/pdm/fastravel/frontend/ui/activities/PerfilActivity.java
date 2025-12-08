package ubi.pdm.fastravel.frontend.ui.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import ubi.pdm.fastravel.R;

public class PerfilActivity extends AppCompatActivity {

    private Button btnEditarPerfil, btnNotificacoes, btnSobre, btnSair;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_perfil);
        // ⚠️ Podes manter este layout se ele já estiver feito

        // Inicializa os botões
        btnEditarPerfil = findViewById(R.id.btn_editar_perfil);
        btnNotificacoes = findViewById(R.id.btn_notificacoes);
        btnSobre = findViewById(R.id.btn_sobre);
        btnSair = findViewById(R.id.btn_sair);

        // Ações dos botões
        btnEditarPerfil.setOnClickListener(v ->
                Toast.makeText(this, "Editar Perfil", Toast.LENGTH_SHORT).show()
        );

        btnNotificacoes.setOnClickListener(v ->
                Toast.makeText(this, "Configurações de Notificações", Toast.LENGTH_SHORT).show()
        );

        btnSobre.setOnClickListener(v ->
                Toast.makeText(this, "Sobre o FasTravel", Toast.LENGTH_SHORT).show()
        );

        btnSair.setOnClickListener(v ->
                Toast.makeText(this, "Sair da Conta", Toast.LENGTH_SHORT).show()
        );
    }
}
