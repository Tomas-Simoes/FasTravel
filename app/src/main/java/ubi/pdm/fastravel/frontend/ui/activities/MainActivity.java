package ubi.pdm.fastravel.frontend.ui.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import ubi.pdm.fastravel.R;
import ubi.pdm.fastravel.frontend.ui.fragments.BuscarRotaFragment;
import ubi.pdm.fastravel.frontend.ui.fragments.HistoricoFragment;
import ubi.pdm.fastravel.frontend.ui.fragments.PerfilFragment;

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

    // Listener para navegação entre fragments
    private final BottomNavigationView.OnItemSelectedListener navListener =
            item -> {
                Fragment selectedFragment = null;
                int itemId = item.getItemId();

                if (itemId == R.id.nav_buscar) {
                    selectedFragment = new BuscarRotaFragment();
                } else if (itemId == R.id.nav_historico) {
                    selectedFragment = new HistoricoFragment();
                } else if (itemId == R.id.nav_perfil) {
                    selectedFragment = new PerfilFragment();
                }

                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, selectedFragment)
                            .commit();
                }

                return true;
            };
}