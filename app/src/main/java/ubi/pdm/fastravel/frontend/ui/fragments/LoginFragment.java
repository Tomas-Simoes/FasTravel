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

public class LoginFragment extends Fragment {

    private TextInputEditText etEmail, etSenha;
    private MaterialButton btnEntrar, btnGoogle;
    private TextView textIrRegisto;
    private GoogleSignInClient mGoogleSignInClient;

    private final ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                handleSignInResult(task);
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etEmail = view.findViewById(R.id.et_email_login);
        etSenha = view.findViewById(R.id.et_senha_login);
        btnEntrar = view.findViewById(R.id.btn_entrar);
        btnGoogle = view.findViewById(R.id.btn_google_login);
        textIrRegisto = view.findViewById(R.id.text_ir_para_registo);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);

        btnEntrar.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String senha = etSenha.getText().toString().trim();

            if (email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(getContext(), "Por favor, preenche todos os campos.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Login com sucesso!", Toast.LENGTH_SHORT).show();

                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new BuscarRotaFragment())
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .commit();
            }
        });

        // 4. Botão Google
        btnGoogle.setOnClickListener(v -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });

        // 5. Ir para Registo
        textIrRegisto.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new RegisterFragment())
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .commit();
        });
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            Toast.makeText(getContext(), "Bem-vindo " + account.getDisplayName(), Toast.LENGTH_SHORT).show();

            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new BuscarRotaFragment())
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .commit();

        } catch (ApiException e) {
            Log.w("LoginFragment", "signInResult:failed code=" + e.getStatusCode());
            Toast.makeText(getContext(), "Erro no login Google.", Toast.LENGTH_SHORT).show();
        }
    }
}