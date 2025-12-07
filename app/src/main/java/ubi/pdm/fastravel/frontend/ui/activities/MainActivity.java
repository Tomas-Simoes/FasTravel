package ubi.pdm.fastravel.frontend.ui.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import ubi.pdm.fastravel.R;
import ubi.pdm.fastravel.frontend.ui.fragments.BuscarRotaFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(android.R.id.content, new BuscarRotaFragment())
                    .commit();
        }
    }

}