package ubi.pdm.fastravel.frontend.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import ubi.pdm.fastravel.R;

public class RegisterFragment extends Fragment {

    private TextInputEditText etNome, etEmail, etSenha;
    private MaterialButton btnRegistar, btnGoogle;
    private TextView textIrLogin;

    public RegisterFragment() {
        // Construtor vazio
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflar o novo layout consistente
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Inicializar as Views
        etNome = view.findViewById(R.id.et_nome_registo);
        etEmail = view.findViewById(R.id.et_email_registo);
        etSenha = view.findViewById(R.id.et_senha_registo);
        btnRegistar = view.findViewById(R.id.btn_registar);
        btnGoogle = view.findViewById(R.id.btn_google_sign_in);
        textIrLogin = view.findViewById(R.id.text_ir_para_login);

        // 2. Configurar os Listeners
        btnRegistar.setOnClickListener(v -> {
            String nome = etNome.getText().toString();
            String email = etEmail.getText().toString();
            String senha = etSenha.getText().toString();

            if (validarCampos(nome, email, senha)) {
                // TODO: Chamar a tua função de registo
                Toast.makeText(getContext(), "A registar...", Toast.LENGTH_SHORT).show();
            }
        });

        btnGoogle.setOnClickListener(v -> {
            // TODO: Lógica para iniciar o fluxo Google Sign-In
            Toast.makeText(getContext(), "A iniciar Google Sign-In...", Toast.LENGTH_SHORT).show();
            // logarComGoogle();
        });

        textIrLogin.setOnClickListener(v -> {
            // TODO: Navegar para o LoginFragment
            Toast.makeText(getContext(), "Ir para Login", Toast.LENGTH_SHORT).show();
        });
    }

    private boolean validarCampos(String nome, String email, String senha) {
        if (nome.isEmpty()) {
            etNome.setError("Nome é obrigatório");
            return false;
        }
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Email inválido");
            return false;
        }
        if (senha.isEmpty() || senha.length() < 6) {
            etSenha.setError("A palavra-passe deve ter pelo menos 6 caracteres");
            return false;
        }
        return true;
    }
}