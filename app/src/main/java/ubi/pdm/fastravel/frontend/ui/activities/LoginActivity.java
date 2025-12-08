package ubi.pdm.fastravel.frontend.ui.activities; // É uma boa prática mover para um pacote 'activities'

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity; // Mudar de Fragment para AppCompatActivity

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

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_login);

        etEmail = findViewById(R.id.et_email_login);
        etSenha = findViewById(R.id.et_senha_login);
        btnEntrar = findViewById(R.id.btn_entrar);
        btnGoogle = findViewById(R.id.btn_google_login);
        textIrRegisto = findViewById(R.id.text_ir_para_registo);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        btnEntrar.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String senha = etSenha.getText().toString().trim();

            if (email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this, "Preenche todos os campos.", Toast.LENGTH_SHORT).show();
                return;
            }

            UserRepository repo = new UserRepository(this);

            new Thread(() -> {
                UserData user = repo.loginAndGetUser(email, senha);

                runOnUiThread(() -> {
                    if (user != null) {
                        Toast.makeText(this, "Bem-vindo " + user.name, Toast.LENGTH_SHORT).show();
                        navigateToBuscarRotaActivity();
                    } else {
                        Toast.makeText(this, "Credenciais inválidas.", Toast.LENGTH_SHORT).show();
                    }
                });
            }).start();
        });


        btnGoogle.setOnClickListener(v -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });

        textIrRegisto.setOnClickListener(v -> {
            navigateToRegisterActivity();
        });
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            Toast.makeText(this, "Bem-vindo " + account.getDisplayName(), Toast.LENGTH_SHORT).show();

            navigateToBuscarRotaActivity();

        } catch (ApiException e) {
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            Toast.makeText(this, "Erro no login Google.", Toast.LENGTH_SHORT).show();
        }
    }


    private void navigateToBuscarRotaActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class); // Altere para a sua Activity principal
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Limpa o back stack
        startActivity(intent);
        finish(); // Termina LoginActivity para que o utilizador não possa voltar
    }

    private void navigateToRegisterActivity() {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class); // Altere para o nome da sua Activity de Registo
        startActivity(intent);
    }
}