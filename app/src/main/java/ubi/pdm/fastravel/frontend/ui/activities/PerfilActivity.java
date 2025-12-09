package ubi.pdm.fastravel.frontend.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;

import ubi.pdm.fastravel.R;
import ubi.pdm.fastravel.frontend.DataPersistenceModule.User.UserData;
import ubi.pdm.fastravel.frontend.DataPersistenceModule.User.UserRepository;

public class PerfilActivity extends AppCompatActivity {

    // Declaração das views
    private LinearLayout btnSobre;
    private MaterialButton btnSair;
    private SwitchMaterial switchDarkMode;
    private SwitchMaterial switchCo2Routes;
    private TextView textIniciaisUsuario;
    private TextView textNomeUsuario;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_perfil);

        // Inicializa as views
        initViews();

        // Configura o perfil do usuário
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
        switchCo2Routes = findViewById(R.id.switch_co2_routes);

        // Textos do perfil
        textIniciaisUsuario = findViewById(R.id.text_iniciais_usuario);
        textNomeUsuario = findViewById(R.id.text_nome_usuario);
    }

    private void setupUserProfile() {
        UserRepository userRepository = new UserRepository(getApplicationContext());
        UserData userData = userRepository.getUserFromCacheOrApi();

        String[] parts = userData.name.trim().split("\\s+");
        StringBuilder initials = new StringBuilder();

        for (String part : parts) {
            initials.append(Character.toUpperCase(part.charAt(0)));
        }

        textNomeUsuario.setText(userData.name);
        textIniciaisUsuario.setText(initials.toString());
    }

    private void setupListeners() {
        // Botão About
        btnSobre.setOnClickListener(v -> {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
        });

        // Botão Logout
        btnSair.setOnClickListener(v -> {
            Toast.makeText(this, "Logging out...", Toast.LENGTH_SHORT).show();

            // Criar intent para MainActivity
            Intent intent = new Intent(PerfilActivity.this, MainActivity.class);

            // Limpar a pilha para evitar voltar para o perfil
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(intent);

            // Fecha a activity atual
            finish();
        });

        // Switch Dark Mode
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                Toast.makeText(this, "Dark Mode Enabled 🌙", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Dark Mode Disabled", Toast.LENGTH_SHORT).show();
            }
        });

        // Switch Eco-Friendly Routes
        switchCo2Routes.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                Toast.makeText(this, "Eco-Friendly Routes Enabled 🌱", Toast.LENGTH_SHORT).show();
                // Aqui podes salvar a preferência
            } else {
                Toast.makeText(this, "Eco-Friendly Routes Disabled", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
