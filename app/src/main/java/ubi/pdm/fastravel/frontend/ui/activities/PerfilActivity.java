package ubi.pdm.fastravel.frontend.ui.activities;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;

import ubi.pdm.fastravel.R;

public class PerfilActivity extends AppCompatActivity {

    // Declaração das views
    private LinearLayout btnSobre;
    private MaterialButton btnSair;
    private SwitchMaterial switchDarkMode;
    private SwitchMaterial switchNotifications;
    private SwitchMaterial switchCo2Routes;
    private TextView textIniciaisUsuario;
    private TextView textNomeUsuario;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_perfil);

        // Inicializa as views
        initViews();

        // Configura as iniciais do usuário
        setupUserProfile();

        // Configura os listeners
        setupListeners();
    }

    private void initViews() {
        // Botão de navegação
        btnSobre = findViewById(R.id.btn_sobre);
        btnSair = findViewById(R.id.btn_sair);

        // Switches de preferências
        switchDarkMode = findViewById(R.id.switch_dark_mode);
        switchNotifications = findViewById(R.id.switch_notifications);
        switchCo2Routes = findViewById(R.id.switch_co2_routes);

        // Textos do perfil
        textIniciaisUsuario = findViewById(R.id.text_iniciais_usuario);
        textNomeUsuario = findViewById(R.id.text_nome_usuario);
    }

    private void setupUserProfile() {
        // Pega o nome do usuário (podes substituir por dados reais)
        String nomeCompleto = textNomeUsuario.getText().toString();

        // Gera as iniciais automaticamente
        String iniciais = gerarIniciais(nomeCompleto);
        textIniciaisUsuario.setText(iniciais);
    }

    private String gerarIniciais(String nomeCompleto) {
        if (nomeCompleto == null || nomeCompleto.isEmpty()) {
            return "??";
        }

        String[] partes = nomeCompleto.trim().split("\\s+");
        StringBuilder iniciais = new StringBuilder();

        // Pega a primeira letra de cada palavra (máximo 2)
        int count = 0;
        for (String parte : partes) {
            if (count >= 2) break;
            if (!parte.isEmpty()) {
                iniciais.append(parte.charAt(0));
                count++;
            }
        }

        return iniciais.toString().toUpperCase();
    }

    private void setupListeners() {
        // Botão Sobre
        btnSobre.setOnClickListener(v ->
                Toast.makeText(this, "Sobre o FasTravel", Toast.LENGTH_SHORT).show()
        );

        // Botão Sair
        btnSair.setOnClickListener(v -> {
            Toast.makeText(this, "Saindo da conta...", Toast.LENGTH_SHORT).show();
            // Aqui podes adicionar a lógica de logout
            // Por exemplo: Firebase.auth.signOut();
            // finish();
        });

        // Switch Modo Escuro
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                Toast.makeText(this, "Modo Escuro Ativado 🌙", Toast.LENGTH_SHORT).show();
                // Implementar modo escuro
                 AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                Toast.makeText(this, "Modo Escuro Desativado", Toast.LENGTH_SHORT).show();
                 AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        // Switch Notificações
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                Toast.makeText(this, "Notificações Ativadas 🔔", Toast.LENGTH_SHORT).show();
                // Ativar notificações
                // NotificationManagerCompat.from(this).areNotificationsEnabled();
            } else {
                Toast.makeText(this, "Notificações Desativadas", Toast.LENGTH_SHORT).show();
                // Desativar notificações
            }
        });

        // Switch Rotas Ecológicas
        switchCo2Routes.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                Toast.makeText(this, "Rotas Ecológicas Ativadas 🌱", Toast.LENGTH_SHORT).show();
                // Salvar preferência
                // SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
                // prefs.edit().putBoolean("eco_routes", true).apply();
            } else {
                Toast.makeText(this, "Rotas Ecológicas Desativadas", Toast.LENGTH_SHORT).show();
                // prefs.edit().putBoolean("eco_routes", false).apply();
            }
        });
    }

    // Método auxiliar para atualizar o perfil do usuário
    public void atualizarPerfil(String nome) {
        textNomeUsuario.setText(nome);
        String iniciais = gerarIniciais(nome);
        textIniciaisUsuario.setText(iniciais);
    }
}