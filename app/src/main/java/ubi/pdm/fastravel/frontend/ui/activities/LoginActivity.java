package ubi.pdm.fastravel.frontend.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import ubi.pdm.fastravel.R;
import ubi.pdm.fastravel.frontend.DataPersistenceModule.User.UserData;
import ubi.pdm.fastravel.frontend.DataPersistenceModule.User.UserRepository;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private TextInputEditText etEmail, etSenha;
    private MaterialButton btnEntrar;
    private TextView textIrRegisto;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_login);

        etEmail = findViewById(R.id.et_email_login);
        etSenha = findViewById(R.id.et_senha_login);
        btnEntrar = findViewById(R.id.btn_entrar);
        textIrRegisto = findViewById(R.id.text_ir_para_registo);


        btnEntrar.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String senha = etSenha.getText().toString().trim();

            if (email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
                return;
            }

            UserRepository repo = new UserRepository(this);

            new Thread(() -> {
                UserData user = repo.loginAndGetUser(email, senha);

                runOnUiThread(() -> {
                    if (user != null) {
                        Toast.makeText(this, "Welcome " + user.name, Toast.LENGTH_SHORT).show();
                        Intent data = new Intent();
                        setResult(RESULT_OK, data);
                        finish();
                    } else {
                        Toast.makeText(this, "Invalid credentials.", Toast.LENGTH_SHORT).show();
                    }
                });
            }).start();
        });


        textIrRegisto.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }
}

