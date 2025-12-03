package ubi.pdm.fastravel.frontend;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.textfield.TextInputEditText;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ubi.pdm.fastravel.R;


public class BuscarRotaFragment extends Fragment {

    private TextInputEditText inputPontoA, inputPontoB;
    private RadioGroup radioGroupTransporte;
    private Button btnEncontrarRota;

    private GoogleMap map;
    private FusedLocationProviderClient fused;

    private static final int REQ_LOC = 1001;

    private String apiKey;

    private LatLng originLatLng, destLatLng;

    private static final int REQ_AUTOCOMPLETE_ORIGIN = 2001;
    private static final int REQ_AUTOCOMPLETE_DEST = 2002;

    private ActivityResultLauncher<Intent> autocompleteLauncher;
    private boolean isSelectingOrigin = false;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_buscar_rota, container, false);


        apiKey = getString(R.string.google_maps_key);

        if (!Places.isInitialized()) {
            Places.initialize(requireContext().getApplicationContext(), apiKey);
        }

        // Inicializa os componentes
        inputPontoA = view.findViewById(R.id.input_ponto_a);
        inputPontoB = view.findViewById(R.id.input_ponto_b);

        inputPontoA.setFocusable(false);
        inputPontoA.setOnClickListener(v -> openAutocomplete(REQ_AUTOCOMPLETE_ORIGIN));

        inputPontoB.setFocusable(false);
        inputPontoB.setOnClickListener(v -> openAutocomplete(REQ_AUTOCOMPLETE_DEST));

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

            requestRoute(pontoA, pontoB);
        });

        return view;
    }

    @Override public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        super.onViewCreated(v, s);
        fused = LocationServices.getFusedLocationProviderClient(requireContext());
        initMap();
    }

    @Override public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        autocompleteLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {

                        Place place = Autocomplete.getPlaceFromIntent(result.getData());

                        if (isSelectingOrigin) {
                            originLatLng = place.getLatLng();
                            inputPontoA.setText(place.getAddress() != null ? place.getAddress() : place.getName());
                        } else {
                            destLatLng = place.getLatLng();
                            inputPontoB.setText(place.getAddress() != null ? place.getAddress() : place.getName());
                        }

                    } else if (result.getResultCode() == AutocompleteActivity.RESULT_ERROR) {
                        Status status = Autocomplete.getStatusFromIntent(result.getData());
                        System.out.println(status.getStatusMessage());
                        Toast.makeText(requireContext(),
                                "Erro no autocomplete: " + status.getStatusMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void initMap() {
        SupportMapFragment smf = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map); // get the supportMapFragment
        if (smf == null) return;
        smf.getMapAsync(this::onMapReady); // if not null, when the map is ready call onMapReady
    }

    private void onMapReady(GoogleMap gm) {
        map = gm;

        map.getUiSettings().setZoomControlsEnabled(true); // turn on UI Setting (zoom buttons)

        enableMyLocationAndCenter(); // activate location
    }

    private void requestRoute(String origin, String destination) {
        new Thread(() -> {
            try {
                String urlString = buildDirectionsUrl(origin, destination); // build the get endpoint url
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection(); // open the connection
                conn.setRequestMethod("GET"); // setup the method
                conn.connect(); // connect

                InputStream is = conn.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                StringBuilder sb = new StringBuilder();
                String line;

                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }

                String json = sb.toString();
                List<LatLng> routePoints = parseRoute(json);

                requireActivity().runOnUiThread(() -> drawRoute(routePoints));

            } catch (Exception e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), "Erro ao obter rota", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private List<LatLng> parseRoute(String json) throws JSONException {
        List<LatLng> path = new ArrayList<>();

        JSONObject jsonObject = new JSONObject(json);
        JSONArray routes = jsonObject.getJSONArray("routes");
        if (routes.length() == 0) return path;

        JSONObject route = routes.getJSONObject(0);
        JSONObject overviewPolyline = route.getJSONObject("overview_polyline");
        String points = overviewPolyline.getString("points");

        path.addAll(PolyUtil.decode(points));
        return path;
    }

    private String buildDirectionsUrl(String origin, String destination) throws UnsupportedEncodingException {
        String strOrigin = "origin=" + URLEncoder.encode(origin, "UTF-8");
        String strDest = "destination=" + URLEncoder.encode(destination, "UTF-8");
        String mode = "mode=driving";

        String parameters = strOrigin + "&" + strDest + "&" + mode + "&key=" + apiKey;
        return "https://maps.googleapis.com/maps/api/directions/json?" + parameters;
    }

    private void drawRoute(List<LatLng> routePoints) {
        if (map == null || routePoints == null || routePoints.isEmpty()) {
            Toast.makeText(requireContext(), "Não foi possível desenhar a rota", Toast.LENGTH_SHORT).show();
            return;
        }

        map.clear();

        PolylineOptions polylineOptions = new PolylineOptions()
                .addAll(routePoints)
                .width(12f)
                .color(Color.BLUE)
                .geodesic(true);

        map.addPolyline(polylineOptions);

        // Ajusta a câmara à rota
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng latLng : routePoints) {
            builder.include(latLng);
        }
        LatLngBounds bounds = builder.build();
        int padding = 100; // padding em px
        map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
    }

    private void openAutocomplete(int requestCode) {
        if (requestCode == REQ_AUTOCOMPLETE_ORIGIN) {
            isSelectingOrigin = true;
        } else if (requestCode == REQ_AUTOCOMPLETE_DEST) {
            isSelectingOrigin = false;
        }

        List<Place.Field> fields = Arrays.asList(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.LAT_LNG,
                Place.Field.ADDRESS
        );

        Intent intent = new Autocomplete.IntentBuilder(
                AutocompleteActivityMode.OVERLAY, fields)
                .build(requireContext());

        autocompleteLauncher.launch(intent);
    }

    @SuppressWarnings("deprecation")
    private void enableMyLocationAndCenter() {
        if (map == null) return;

        // if no permission then asks for permission on location, else it tries to get last location via FusedLocationProviderClient

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQ_LOC);
            return;
        }

        map.setMyLocationEnabled(true);

        fused.getLastLocation().addOnSuccessListener(loc -> {
            if (loc != null) {
                LatLng me = new LatLng(loc.getLatitude(), loc.getLongitude());
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(me, 15f));
            }
        });
    }

    @SuppressWarnings("deprecation")
    @Override public void onRequestPermissionsResult(int code, @NonNull String[] perms, @NonNull int[] res) {
        super.onRequestPermissionsResult(code, perms, res);
        if (code == REQ_LOC && res.length > 0 && res[0] == PackageManager.PERMISSION_GRANTED) {
            enableMyLocationAndCenter();
        } else {
            Toast.makeText(requireContext(), "Permissão de localização negada", Toast.LENGTH_SHORT).show();
        }
    }

}