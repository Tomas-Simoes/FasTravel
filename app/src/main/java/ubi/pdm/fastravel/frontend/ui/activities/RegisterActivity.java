package ubi.pdm.fastravel.frontend.ui.activities; // Pacote mantido

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity; // MUDANÇA: Estende AppCompatActivity

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import ubi.pdm.fastravel.R;
import ubi.pdm.fastravel.frontend.DataPersistenceModule.User.UserData;
import ubi.pdm.fastravel.frontend.DataPersistenceModule.User.UserRepository;
// Importar a LoginActivity para navegação
// import ubi.pdm.fastravel.frontend.ui.activities.LoginActivity;
// import ubi.pdm.fastravel.frontend.ui.activities.MainActivity; // Assumindo que esta é a próxima Activity

public class RegisterActivity extends AppCompatActivity { // 1. Mudar de Fragment para Activity

    private static final String TAG = "RegisterActivity";

    private TextInputEditText etNome, etEmail, etSenha;
    private MaterialButton btnRegistar, btnGoogle;
    private TextView textIrLogin;
    private GoogleSignInClient mGoogleSignInClient;

    private final ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                handleSignInResult(task);
            }
    );

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) { // 2. Mudar onCreateView para onCreate
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_register); // 3. Carregar o layout

        // 4. Inicializar Views (agora usando findViewById diretamente)
        etNome = findViewById(R.id.et_nome_registo);
        etEmail = findViewById(R.id.et_email_registo);
        etSenha = findViewById(R.id.et_senha_registo);
        btnRegistar = findViewById(R.id.btn_registar);
        btnGoogle = findViewById(R.id.btn_google_sign_in);
        textIrLogin = findViewById(R.id.text_ir_para_login);

        // 5. Configurar Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        // Usamos 'this' como Contexto
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        btnRegistar.setOnClickListener(v -> {
            if (!validarCampos()) return;

            String nome = etNome.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String senha = etSenha.getText().toString().trim();

            UserRepository repo = new UserRepository(this);

            new Thread(() -> {
                UserData user = repo.registerUser(nome, email, senha);

                runOnUiThread(() -> {
                    if (user != null) {
                        Toast.makeText(this, "Bem-vindo " + user.name, Toast.LENGTH_SHORT).show();
                        navigateToNextActivity();
                    } else {
                        Toast.makeText(this, "Falha no registo.", Toast.LENGTH_SHORT).show();
                    }
                });
            }).start();
        });


        // 7. Botão Google
        btnGoogle.setOnClickListener(v -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });

        // 8. Link "Entrar" (NAVEGAÇÃO PARA LOGIN)
        textIrLogin.setOnClickListener(v -> {
            // Navegar para LoginActivity usando Intent
            navigateToLoginActivity();
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
        // Usamos this. para o contexto em vez de getContext()
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
            Toast.makeText(this, "Bem-vindo " + account.getDisplayName(), Toast.LENGTH_SHORT).show();

            // Navegar para a próxima Activity após login/registo via Google
            navigateToNextActivity();

        } catch (ApiException e) {
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            Toast.makeText(this, "Erro no login Google", Toast.LENGTH_SHORT).show();
        }
    }

    // --- Métodos de Navegação ---

    private void navigateToLoginActivity() {
        // Redireciona para a LoginActivity
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
        finish(); // Termina RegisterActivity se não quiser que o utilizador volte a ela facilmente
    }

    private void navigateToNextActivity() {
        // ASSUNÇÃO: A próxima Activity é a Activity Principal (e.g., MainActivity)
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Limpa o back stack
        startActivity(intent);
        finish();
    }
}