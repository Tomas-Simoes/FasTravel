package ubi.pdm.fastravel.frontend;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.textfield.TextInputEditText;

import ubi.pdm.fastravel.R;

public class BuscarRotaFragment extends Fragment {

    private TextInputEditText inputPontoA, inputPontoB;
    private RadioGroup radioGroupTransporte;
    private Button btnEncontrarRota;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_buscar_rota, container, false);

        // Inicializa os componentes
        inputPontoA = view.findViewById(R.id.input_ponto_a);
        inputPontoB = view.findViewById(R.id.input_ponto_b);
        radioGroupTransporte = view.findViewById(R.id.radio_group_transporte);
        btnEncontrarRota = view.findViewById(R.id.btn_encontrar_rota);

        // Placeholder para ação do botão (implementar lógica depois)
        btnEncontrarRota.setOnClickListener(v -> {
            String pontoA = inputPontoA.getText().toString();
            String pontoB = inputPontoB.getText().toString();

            // Verifica qual modo de transporte foi selecionado
            int selectedId = radioGroupTransporte.getCheckedRadioButtonId();
            String transporte = "";

            if (selectedId == R.id.radio_comboio) {
                transporte = "Comboio";
            } else if (selectedId == R.id.radio_uber) {
                transporte = "Uber";
            } else if (selectedId == R.id.radio_autocarro) {
                transporte = "Autocarro";
            } else if (selectedId == R.id.radio_misto) {
                transporte = "Misto";
            }

            // Toast temporário para feedback
            Toast.makeText(getContext(),
                    "Buscar rota de " + pontoA + " para " + pontoB + " via " + transporte,
                    Toast.LENGTH_SHORT).show();

            // Aqui será implementada a lógica de busca de rota
        });

        return view;
    }
}