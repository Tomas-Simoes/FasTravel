package ubi.pdm.fastravel.frontend.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import ubi.pdm.fastravel.R;

public class AboutActivity extends AppCompatActivity {

    private LinearLayout btnGithubTomas;
    private LinearLayout btnGithubLeonardo;
    private LinearLayout btnGithubRafael;
    private ImageView btnBack;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        initViews();
        setupListeners();
    }

    private void initViews() {
        btnGithubTomas = findViewById(R.id.btn_github_tomas);
        btnGithubLeonardo = findViewById(R.id.btn_github_leonardo);
        btnGithubRafael = findViewById(R.id.btn_github_rafael);
        btnBack = findViewById(R.id.btn_back);
    }

    private void setupListeners() {
        // Abrir GitHubs
        btnGithubTomas.setOnClickListener(v -> openGithub("tomasimoes"));
        btnGithubLeonardo.setOnClickListener(v -> openGithub("leorcf"));
        btnGithubRafael.setOnClickListener(v -> openGithub("Rafasta236"));

        // Botão de voltar com animação
        btnBack.setOnClickListener(v -> {
            finish();
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });
    }

    private void openGithub(String username) {
        try {
            String url = "https://github.com/" + username;
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Could not open GitHub", Toast.LENGTH_SHORT).show();
        }
    }
}
