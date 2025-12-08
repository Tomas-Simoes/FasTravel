package ubi.pdm.fastravel.frontend.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import ubi.pdm.fastravel.R;
import ubi.pdm.fastravel.frontend.ui.fragments.BuscarRotaFragment;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private final ActivityResultLauncher<Intent> loginLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK) {
                            Log.d(TAG, "Login successful. Loading main fragment.");
                            getSupportFragmentManager().beginTransaction()
                                    .replace(android.R.id.content, new BuscarRotaFragment())
                                    .commit();
                        } else {
                            Log.d(TAG, "Login cancelled. Closing application.");
                            finish();
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            loginLauncher.launch(intent);
        }
    }
}