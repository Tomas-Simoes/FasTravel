package ubi.pdm.fastravel.frontend;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import ubi.pdm.fastravel.frontend.ui.activities.MainActivity;

public class SplashActivity extends AppCompatActivity {

        @Override
        protected void onCreate(Bundle savedInstanceState) {

            SplashScreen.installSplashScreen(this);

            super.onCreate(savedInstanceState);

            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }
