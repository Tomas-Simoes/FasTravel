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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.google.android.libraries.places.api.model.AddressComponent;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.ChipGroup;
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
    private MaterialButton btnEncontrarRota;
    private ChipGroup chipGroupTransporte;
    private RecyclerView recyclerRotas;
    private MaterialButton btnInverter;

    private GoogleMap map;
    private FusedLocationProviderClient fused;

    private static final int REQ_LOC = 1001;

    private String apiKey;

    private LatLng originLatLng, destLatLng;

    private static final int REQ_AUTOCOMPLETE_ORIGIN = 2001;
    private static final int REQ_AUTOCOMPLETE_DEST = 2002;

    private ActivityResultLauncher<Intent> autocompleteLauncher;
    private boolean isSelectingOrigin = false;

    private BottomSheetBehavior<MaterialCardView> bottomSheetBehavior;

    private RouteAdapter routeAdapter;
    private final List<RouteInfo> currentRoutes = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_buscar_rota, container, false);

        apiKey = getString(R.string.google_maps_key);

        MaterialCardView bottomSheet = view.findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setHideable(false);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        // Inputs
        inputPontoA = view.findViewById(R.id.input_ponto_a);
        inputPontoB = view.findViewById(R.id.input_ponto_b);

        // CHANGED: til_ponto_a e til_ponto_b agora são LinearLayout (não TextInputLayout)
        LinearLayout tilPontoA = view.findViewById(R.id.til_ponto_a);
        LinearLayout tilPontoB = view.findViewById(R.id.til_ponto_b);

        // Autocomplete + expand sheet
        inputPontoA.setFocusable(false);
        inputPontoA.setOnClickListener(v -> {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            openAutocomplete(REQ_AUTOCOMPLETE_ORIGIN);
        });
        inputPontoB.setFocusable(false);
        inputPontoB.setOnClickListener(v -> {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            openAutocomplete(REQ_AUTOCOMPLETE_DEST);
        });

        // Se ganhar foco por qualquer razão, expande também
        inputPontoA.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        });
        inputPontoB.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        });

        // Tocar no layout também expande
        View.OnClickListener expandListener = v ->
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        tilPontoA.setOnClickListener(expandListener);
        tilPontoB.setOnClickListener(expandListener);

        // ChipGroup transporte
        chipGroupTransporte = view.findViewById(R.id.chip_group_transporte);

        // Botão inverter
        btnInverter = view.findViewById(R.id.btn_inverter);
        btnInverter.setOnClickListener(v -> {
            CharSequence tempText = inputPontoA.getText();
            inputPontoA.setText(inputPontoB.getText());
            inputPontoB.setText(tempText);

            LatLng tempLatLng = originLatLng;
            originLatLng = destLatLng;
            destLatLng = tempLatLng;
        });

        // Recycler de resultados
        recyclerRotas = view.findViewById(R.id.recycler_rotas);
        recyclerRotas.setLayoutManager(new LinearLayoutManager(requireContext()));
        routeAdapter = new RouteAdapter(currentRoutes);
        recyclerRotas.setAdapter(routeAdapter);

        btnEncontrarRota = view.findViewById(R.id.btn_encontrar_rota);
        btnEncontrarRota.setOnClickListener(v -> {
            String pontoA = inputPontoA.getText() != null ? inputPontoA.getText().toString() : "";
            String pontoB = inputPontoB.getText() != null ? inputPontoB.getText().toString() : "";

            if (pontoA.isEmpty() || pontoB.isEmpty()) {
                Toast.makeText(getContext(),
                        "Escolhe origem e destino primeiro",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // Qual modo de transporte foi escolhido?
            int checkedId = chipGroupTransporte.getCheckedChipId();
            String mode = "driving";
            String transitMode = null;
            String modoLabel = "Carro";

            if (checkedId == R.id.chip_comboio) {
                mode = "transit";
                transitMode = "train";
                modoLabel = "Comboio";
            } else if (checkedId == R.id.chip_autocarro) {
                mode = "transit";
                transitMode = "bus";
                modoLabel = "Autocarro";
            } else if (checkedId == R.id.chip_misto) {
                mode = "transit";
                transitMode = null; // misto de todos os transportes
                modoLabel = "Transportes (misto)";
            } else if (checkedId == R.id.chip_uber) {
                mode = "driving";
                transitMode = null;
                modoLabel = "Uber (rota de carro)";
                Toast.makeText(getContext(),
                        "Uber real (preço/tempo) precisa integração com a API da Uber. Para já é rota de carro.",
                        Toast.LENGTH_SHORT).show();
            }

            Toast.makeText(getContext(),
                    "A calcular rota de " + pontoA + " para " + pontoB + " via " + modoLabel,
                    Toast.LENGTH_SHORT).show();

            requestRoute(pontoA, pontoB, mode, transitMode);
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        super.onViewCreated(v, s);
        fused = LocationServices.getFusedLocationProviderClient(requireContext());
        initMap();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        autocompleteLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {

                        Place place = Autocomplete.getPlaceFromIntent(result.getData());

                        if (isSelectingOrigin) {
                            originLatLng = place.getLocation();
                            inputPontoA.setText(place.getShortFormattedAddress() != null ? place.getShortFormattedAddress() : place.getPrimaryTypeDisplayName());
                        } else {
                            destLatLng = place.getLocation();
                            inputPontoB.setText(place.getShortFormattedAddress() != null ? place.getShortFormattedAddress(): place.getPrimaryTypeDisplayName());
                        }

                    } else if (result.getResultCode() == AutocompleteActivity.RESULT_ERROR) {
                        Status status = Autocomplete.getStatusFromIntent(result.getData());
                        Toast.makeText(requireContext(),
                                "Erro no autocomplete: " + status.getStatusMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void initMap() {
        SupportMapFragment smf = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (smf == null) return;
        smf.getMapAsync(this::onMapReady);
    }

    private void onMapReady(GoogleMap gm) {
        map = gm;
        map.getUiSettings().setZoomControlsEnabled(true);
        enableMyLocationAndCenter();
    }

    // ============= DIRECTIONS / ROTAS ============= //

    private void requestRoute(String origin, String destination, String mode, @Nullable String transitMode) {
        new Thread(() -> {
            try {
                String urlString = buildDirectionsUrl(origin, destination, mode, transitMode);
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.connect();

                InputStream is = conn.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                StringBuilder sb = new StringBuilder();
                String line;

                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }

                String json = sb.toString();
                List<RouteInfo> routes = parseRoutes(json);

                requireActivity().runOnUiThread(() -> {
                    if (routes.isEmpty()) {
                        Toast.makeText(requireContext(), "Nenhuma rota encontrada", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Desenha só a primeira rota no mapa
                    drawRoute(routes.get(0).routePoints);

                    // Atualiza lista de resultados
                    currentRoutes.clear();
                    currentRoutes.addAll(routes);
                    routeAdapter.notifyDataSetChanged();

                    View tvResultados = requireView().findViewById(R.id.tv_resultados_titulo);
                    tvResultados.setVisibility(View.VISIBLE);
                    recyclerRotas.setVisibility(View.VISIBLE);

                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                });

            } catch (Exception e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "Erro ao obter rota", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private List<RouteInfo> parseRoutes(String json) throws JSONException {
        List<RouteInfo> routes = new ArrayList<>();

        JSONObject jsonObject = new JSONObject(json);
        JSONArray routesArray = jsonObject.getJSONArray("routes");
        if (routesArray.length() == 0) return routes;

        for (int i = 0; i < routesArray.length(); i++) {
            JSONObject routeObj = routesArray.getJSONObject(i);
            RouteInfo info = new RouteInfo();

            info.summary = routeObj.optString("summary", "Rota " + (i + 1));

            // polyline para desenhar no mapa
            JSONObject overviewPolyline = routeObj.getJSONObject("overview_polyline");
            String points = overviewPolyline.getString("points");
            info.routePoints.addAll(PolyUtil.decode(points));

            JSONArray legs = routeObj.getJSONArray("legs");
            if (legs.length() > 0) {
                JSONObject leg = legs.getJSONObject(0);

                if (leg.has("duration")) {
                    info.durationText = leg.getJSONObject("duration").optString("text", "");
                }
                if (leg.has("arrival_time")) {
                    info.arrivalTimeText = leg.getJSONObject("arrival_time").optString("text", "");
                }
                if (leg.has("departure_time")) {
                    info.departureTimeText = leg.getJSONObject("departure_time").optString("text", "");
                }

                // Itinerário (steps)
                JSONArray steps = leg.getJSONArray("steps");
                for (int s = 0; s < steps.length(); s++) {
                    JSONObject step = steps.getJSONObject(s);
                    String travelMode = step.optString("travel_mode", "");

                    if ("TRANSIT".equalsIgnoreCase(travelMode) && step.has("transit_details")) {
                        JSONObject transitDetails = step.getJSONObject("transit_details");

                        String lineName = "";
                        if (transitDetails.has("line")) {
                            JSONObject line = transitDetails.getJSONObject("line");
                            lineName = line.optString("short_name",
                                    line.optString("name", "Linha"));

                            // tipo de veículo (BUS, RAIL, SUBWAY, etc.)
                            JSONObject vehicle = line.optJSONObject("vehicle");
                            if (vehicle != null) {
                                String type = vehicle.optString("type", null);
                                if (type != null && info.primaryVehicleType == null) {
                                    info.primaryVehicleType = type;
                                }
                            }
                        }

                        info.hasTransit = true;

                        String depStop = transitDetails.getJSONObject("departure_stop").optString("name", "Paragem origem");
                        String arrStop = transitDetails.getJSONObject("arrival_stop").optString("name", "Paragem destino");
                        int numStops = transitDetails.optInt("num_stops", -1);

                        StringBuilder sb = new StringBuilder();
                        sb.append("Apanhar ").append(lineName)
                                .append(" em ").append(depStop)
                                .append(" → sair em ").append(arrStop);
                        if (numStops >= 0) {
                            sb.append(" (").append(numStops).append(" paragens)");
                        }

                        info.itineraryLines.add(sb.toString());

                    } else {
                        // Para passos a pé / carro: usar instrução simples
                        String htmlInstr = step.optString("html_instructions", "");
                        if (!htmlInstr.isEmpty()) {
                            String plain = htmlInstr.replaceAll("<[^>]*>", "");
                            info.itineraryLines.add(plain);
                        }
                    }
                }
            }

            routes.add(info);
        }

        return routes;
    }

    private String buildDirectionsUrl(String origin, String destination, String mode, @Nullable String transitMode)
            throws UnsupportedEncodingException {

        // Usa texto (endereço); podes trocar para lat,lng com originLatLng/destLatLng se quiseres
        String strOrigin = "origin=" + URLEncoder.encode(origin, "UTF-8");
        String strDest = "destination=" + URLEncoder.encode(destination, "UTF-8");

        StringBuilder params = new StringBuilder();
        params.append(strOrigin)
                .append("&")
                .append(strDest)
                .append("&mode=").append(mode)
                .append("&alternatives=true");

        if ("transit".equals(mode) && transitMode != null) {
            params.append("&transit_mode=").append(transitMode);
        }

        params.append("&key=").append(apiKey);

        return "https://maps.googleapis.com/maps/api/directions/json?" + params;
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

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng latLng : routePoints) {
            builder.include(latLng);
        }
        LatLngBounds bounds = builder.build();
        int padding = 100;
        map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
    }

    private void openAutocomplete(int requestCode) {
        isSelectingOrigin = (requestCode == REQ_AUTOCOMPLETE_ORIGIN);

        List<Place.Field> fields = Arrays.asList(
                Place.Field.ID,
                Place.Field.DISPLAY_NAME,
                Place.Field.FORMATTED_ADDRESS,
                Place.Field.SHORT_FORMATTED_ADDRESS,
                Place.Field.LOCATION
        );

        Intent intent = new Autocomplete.IntentBuilder(
                AutocompleteActivityMode.OVERLAY, fields
        ).build(requireContext());

        autocompleteLauncher.launch(intent);
    }


    @SuppressWarnings("deprecation")
    private void enableMyLocationAndCenter() {
        if (map == null) return;

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
    @Override
    public void onRequestPermissionsResult(int code, @NonNull String[] perms, @NonNull int[] res) {
        super.onRequestPermissionsResult(code, perms, res);
        if (code == REQ_LOC && res.length > 0 && res[0] == PackageManager.PERMISSION_GRANTED) {
            enableMyLocationAndCenter();
        } else {
            Toast.makeText(requireContext(), "Permissão de localização negada", Toast.LENGTH_SHORT).show();
        }
    }

    // ========= MODELO + ADAPTER PARA A RECYCLER ========= //

    private static class RouteInfo {
        String summary;
        String durationText;
        String arrivalTimeText;
        String departureTimeText;
        List<String> itineraryLines = new ArrayList<>();
        List<LatLng> routePoints = new ArrayList<>();

        boolean hasTransit = false;
        String primaryVehicleType = null; // BUS, RAIL, SUBWAY, etc.
    }

    private static class RouteViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView card;
        ImageView ivMode;
        TextView tvTitulo;
        TextView tvTempo;
        TextView tvItinerario;

        RouteViewHolder(@NonNull View itemView) {
            super(itemView);
            card = (MaterialCardView) itemView;
            ivMode = itemView.findViewById(R.id.iv_mode);
            tvTitulo = itemView.findViewById(R.id.tv_titulo);
            tvTempo = itemView.findViewById(R.id.tv_tempo);
            tvItinerario = itemView.findViewById(R.id.tv_itinerario);
        }
    }

    private static class RouteAdapter extends RecyclerView.Adapter<RouteViewHolder> {

        private final List<RouteInfo> routes;

        RouteAdapter(List<RouteInfo> routes) {
            this.routes = routes;
        }

        @NonNull
        @Override
        public RouteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_rota, parent, false);
            return new RouteViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull RouteViewHolder holder, int position) {
            RouteInfo r = routes.get(position);

            // Título: resumo + duração
            StringBuilder titulo = new StringBuilder();
            if (r.summary != null && !r.summary.isEmpty()) {
                titulo.append(r.summary);
            } else {
                titulo.append("Rota ").append(position + 1);
            }
            if (r.durationText != null && !r.durationText.isEmpty()) {
                titulo.append(" • ").append(r.durationText);
            }
            holder.tvTitulo.setText(titulo.toString());

            // Tempo: partida / chegada
            StringBuilder tempo = new StringBuilder();
            if (r.departureTimeText != null && !r.departureTimeText.isEmpty()) {
                tempo.append("Partida ").append(r.departureTimeText);
            }
            if (r.arrivalTimeText != null && !r.arrivalTimeText.isEmpty()) {
                if (tempo.length() > 0) tempo.append(" • ");
                tempo.append("Chegada ").append(r.arrivalTimeText);
            }
            holder.tvTempo.setText(tempo.toString());

            // Itinerário (primeiras 3 linhas)
            if (!r.itineraryLines.isEmpty()) {
                StringBuilder sub = new StringBuilder();
                for (int i = 0; i < r.itineraryLines.size() && i < 3; i++) {
                    if (i > 0) sub.append("\n");
                    sub.append("• ").append(r.itineraryLines.get(i));
                }
                holder.tvItinerario.setText(sub.toString());
            } else {
                holder.tvItinerario.setText("Itinerário detalhado indisponível.");
            }

            // Ícone de modo estilo Moovit
            int iconRes;

            if (r.hasTransit) {
                String type = r.primaryVehicleType != null ? r.primaryVehicleType : "";
                if (type.contains("BUS")) {
                    iconRes = R.drawable.ic_bus;
                } else if (type.contains("RAIL") || type.contains("TRAIN")) {
                    iconRes = R.drawable.ic_train;
                } else if (type.contains("SUBWAY") || type.contains("METRO")) {
                    iconRes = R.drawable.ic_bus; // CHANGED: use ic_bus como fallback se não tiver ic_metro
                } else if (type.contains("TRAM")) {
                    iconRes = R.drawable.ic_bus; // CHANGED: use ic_bus como fallback se não tiver ic_tram
                } else {
                    iconRes = R.drawable.ic_navigation; // CHANGED: ic_walk não existe, usa ic_navigation
                }
            } else {
                iconRes = R.drawable.ic_car; // Uber e carro
            }

            holder.ivMode.setImageResource(iconRes);

            // FUTURO: onClick para desenhar rota escolhida
            // holder.card.setOnClickListener(v -> { ... });
        }

        @Override
        public int getItemCount() {
            return routes.size();
        }
    }
}