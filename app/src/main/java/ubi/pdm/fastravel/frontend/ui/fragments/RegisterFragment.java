package ubi.pdm.fastravel.frontend.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import ubi.pdm.fastravel.R;

public class RegisterFragment extends Fragment {

    private TextInputEditText etNome, etEmail, etSenha;
    private MaterialButton btnRegistar, btnGoogle;
    private TextView textIrLogin;
    private GoogleSignInClient mGoogleSignInClient;

    // Lançador para o Google
    private final ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                // O código de resultado do Google às vezes não é RESULT_OK padrão,
                // mas a task traz o estado correto.
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                handleSignInResult(task);
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Inicializar Views
        etNome = view.findViewById(R.id.et_nome_registo);
        etEmail = view.findViewById(R.id.et_email_registo);
        etSenha = view.findViewById(R.id.et_senha_registo);
        btnRegistar = view.findViewById(R.id.btn_registar);
        btnGoogle = view.findViewById(R.id.btn_google_sign_in);
        textIrLogin = view.findViewById(R.id.text_ir_para_login);

        // 2. Configurar Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);

        // 3. Botão Registar
        btnRegistar.setOnClickListener(v -> {
            if (validarCampos()) {
                // TODO: Coloca aqui a tua lógica de guardar na base de dados
                Toast.makeText(getContext(), "Registo efetuado com sucesso!", Toast.LENGTH_SHORT).show();
                // Depois de registar, podes enviar para o Mapa ou Activity Principal
            }
        });

        // 4. Botão Google
        btnGoogle.setOnClickListener(v -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });

        // 5. Link "Entrar" (NAVEGAÇÃO PARA LOGIN)
        textIrLogin.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new LoginFragment()) // ATENÇÃO: Confirma se o ID do teu container na Activity é 'fragment_container'
                    .addToBackStack(null) // Permite voltar atrás com o botão do telemóvel
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .commit();
        });
    }

    private boolean validarCampos() {
        String nome = etNome.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String senha = etSenha.getText().toString().trim();

        if (nome.isEmpty()) {
            etNome.setError("Nome obrigatório");
            return false;
        }
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Email inválido");
            return false;
        }
        if (senha.isEmpty() || senha.length() < 6) {
            etSenha.setError("A senha precisa de 6 caracteres");
            return false;
        }
        return true;
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            // Login Google com sucesso
            Toast.makeText(getContext(), "Bem-vindo " + account.getDisplayName(), Toast.LENGTH_SHORT).show();
        } catch (ApiException e) {
            Log.w("Register", "signInResult:failed code=" + e.getStatusCode());
            Toast.makeText(getContext(), "Erro no login Google", Toast.LENGTH_SHORT).show();
        }
    }
}