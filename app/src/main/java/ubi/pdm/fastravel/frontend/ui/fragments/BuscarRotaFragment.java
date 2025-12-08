package ubi.pdm.fastravel.frontend.ui.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.PlaceAutocomplete;
import com.google.android.libraries.places.widget.PlaceAutocompleteActivity;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.maps.android.PolyUtil;
import com.google.android.gms.tasks.Task;

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
import java.util.Collections;
import java.util.List;

import ubi.pdm.fastravel.R;
import ubi.pdm.fastravel.frontend.ThemedRoutesModule.ThemedRoute;
import ubi.pdm.fastravel.frontend.ui.activities.HistoryActivity;
import ubi.pdm.fastravel.frontend.ui.activities.ThemedRoutesActivity;
import ubi.pdm.fastravel.frontend.util.RouteAdapter;

public class BuscarRotaFragment extends Fragment {

    private TextInputEditText inputPontoA, inputPontoB;
    private MaterialButton btnEncontrarRota, btnInverter;
    private ChipGroup chipGroupTransporte;
    private RecyclerView recyclerRotas;

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

    private PlacesClient placesClient;

    private MaterialCardView fabMain, cardNavigation;

    private FloatingActionButton fab1, fabHistory, fab3, fabThemedRoutes;
    private boolean isFabOpen = false;
    private FrameLayout menuFabContainer;

    private LinearLayout favoritoCasa, favoritoTrabalho, btnAdicionarFavorito, containerFavoritos;

    private TextView textSubtituloCasa, textSubtituloTrabalho, tvNavInstruction, tvNavDetail, tvNavDistance, tvIniciais;

    private MaterialCardView bottomSheet;

    private SharedPreferences prefs;

    private NestedScrollView bottomSheetScroll;

    private boolean isNavigating = false;
    private int currentNavStepIndex = -1;
    private List<RouteInfo.NavStep> currentNavSteps = new ArrayList<>();
    private LocationCallback navLocationCallback;
    private ImageView ivNavDirection;

    private float currentDistanceToEndMeters = -1f;


    private enum FavoriteType {
        HOME,
        WORK,
        OTHER
    }

    private FavoriteType tipoFavoritoAtual;
    private final ActivityResultLauncher<Intent> themedRouteLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    ThemedRoute selectedRoute = (ThemedRoute) result.getData().getSerializableExtra("selected_themed_route");
                    if (selectedRoute != null) {
                        String fullPath = selectedRoute.getPathText();
                        List<String> places = Arrays.asList(fullPath.split("  →  "));

                        if (places.size() >= 2) {
                            // 1. Define os pontos chave da rota temática
                            String originalOrigin = places.get(0); // A origem da rota temática (agora o 1º waypoint)
                            String destination = places.get(places.size() - 1);

                            // 2. Waypoints interiores (se existirem)
                            List<String> originalInnerWaypoints = new ArrayList<>();
                            if (places.size() > 2) {
                                // Pega os stops entre a origem e o destino
                                originalInnerWaypoints.addAll(places.subList(1, places.size() - 1));
                            }

                            // Lista de waypoints com a ORIGEM ORIGINAL no início
                            List<String> waypointsWithOriginalOriginAsFirst = new ArrayList<>();
                            waypointsWithOriginalOriginAsFirst.add(originalOrigin);
                            waypointsWithOriginalOriginAsFirst.addAll(originalInnerWaypoints);

                            // 3. Verifica a Permissão de Localização
                            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                                // ❌ Permissão Negada: Reverte para a origem original da rota temática
                                Toast.makeText(getContext(), "Permissão de localização negada. A usar a origem original da rota.", Toast.LENGTH_LONG).show();

                                inputPontoA.setText(originalOrigin);
                                inputPontoB.setText(destination);

                                requestRoute(
                                        originalOrigin,
                                        destination,
                                        "driving",
                                        null,
                                        originalInnerWaypoints // Sem a origem original
                                );
                                return;
                            }

                            fused.getLastLocation().addOnSuccessListener(location -> {
                                String finalOrigin;
                                List<String> finalWaypoints;
                                String originDisplayText;

                                if (location != null) {
                                    finalOrigin = location.getLatitude() + "," + location.getLongitude();

                                    // O primeiro waypoint é o ponto de partida original da rota
                                    finalWaypoints = waypointsWithOriginalOriginAsFirst;

                                    // NOTA: Pode usar um String Resource como R.string.my_location
                                    originDisplayText = "Sua Localização Atual";

                                } else {
                                    // ⚠️ Falha Temporária (ex: GPS desligado): Reverte para a origem original
                                    Toast.makeText(getContext(), "Não foi possível obter a sua localização. A usar a origem original da rota.", Toast.LENGTH_LONG).show();

                                    finalOrigin = originalOrigin;
                                    finalWaypoints = originalInnerWaypoints;
                                    originDisplayText = originalOrigin;
                                }

                                // 5. Atualiza UI e chama a rota
                                inputPontoA.setText(originDisplayText);
                                inputPontoB.setText(destination);

                                requestRoute(
                                        finalOrigin,
                                        destination,
                                        "driving",
                                        null,
                                        finalWaypoints
                                );
                            }).addOnFailureListener(e -> {
                                // ❌ Falha Assíncrona: Reverte para a origem original
                                Toast.makeText(getContext(), "Erro ao obter localização. A usar a origem original da rota.", Toast.LENGTH_LONG).show();

                                inputPontoA.setText(originalOrigin);
                                inputPontoB.setText(destination);

                                requestRoute(
                                        originalOrigin,
                                        destination,
                                        "driving",
                                        null,
                                        originalInnerWaypoints
                                );
                            });
                        }
                    }
                }
            });

    private final ActivityResultLauncher<Intent> historyLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {});
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_buscar_rota, container, false);

        apiKey = view.getContext().getString(R.string.google_maps_key);

        setupIds(view);

        prefs = requireContext().getSharedPreferences("fastravel_prefs", Context.MODE_PRIVATE);

        setupFavouriteClicks();

        loadFavourites();
        loadExtraFavourites();

        tvIniciais.setText("JD");

        setupFabClicks();

        // Recycler de resultados
        recyclerRotas = view.findViewById(R.id.recycler_rotas);
        recyclerRotas.setLayoutManager(new LinearLayoutManager(requireContext()));

        routeAdapter = new RouteAdapter(
                currentRoutes,
                route -> {
                    int count = (route.routePoints != null) ? route.routePoints.size() : 0;
                    if (count == 0) return;

                    drawRoute(route.routePoints);

                    bottomSheetScroll.post(() -> bottomSheetScroll.scrollTo(0, 0));

                    startNavigation(route);

                    if (bottomSheetBehavior != null) {
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    }
                }
        );
        recyclerRotas.setAdapter(routeAdapter);
        
        setupFindRoutes(view);

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

        placesClient = Places.createClient(requireContext());

        autocompleteLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    Intent data = result.getData();
                    if (result.getResultCode() == Activity.RESULT_OK && data != null) {

                        AutocompletePrediction prediction =
                                PlaceAutocomplete.getPredictionFromIntent(data);

                        if (prediction == null) {
                            Toast.makeText(requireContext(),
                                    "Não foi possível obter o local selecionado",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String placeId = prediction.getPlaceId();
                        AutocompleteSessionToken token =
                                PlaceAutocomplete.getSessionTokenFromIntent(data);

                        List<Place.Field> placeFields = Arrays.asList(
                                Place.Field.ID,
                                Place.Field.LOCATION,
                                Place.Field.DISPLAY_NAME,
                                Place.Field.FORMATTED_ADDRESS,
                                Place.Field.SHORT_FORMATTED_ADDRESS
                        );

                        FetchPlaceRequest request =
                                FetchPlaceRequest.builder(placeId, placeFields)
                                        .setSessionToken(token)
                                        .build();

                        Task<FetchPlaceResponse> task = placesClient.fetchPlace(request);
                        task.addOnSuccessListener(response -> {
                            Place place = response.getPlace();
                            LatLng latLng = place.getLocation();

                            if (tipoFavoritoAtual != null) {
                                saveFavourite(place);
                                tipoFavoritoAtual = null;
                                return;
                            }

                            String displayText = place.getShortFormattedAddress();
                            if (displayText == null || displayText.isEmpty()) {
                                displayText = place.getFormattedAddress();
                            }
                            if (displayText == null || displayText.isEmpty()) {
                                displayText = place.getDisplayName();
                            }
                            if (displayText == null) displayText = "";

                            if (isSelectingOrigin) {
                                originLatLng = latLng;
                                inputPontoA.setText(displayText);
                            } else {
                                destLatLng = latLng;
                                inputPontoB.setText(displayText);
                            }
                        }).addOnFailureListener(e -> {
                            String msg = "Erro ao obter detalhes do local";
                            if (e instanceof ApiException) {
                                msg += ": " + ((ApiException) e).getStatusCode();
                            }
                            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                        });

                    } else if (result.getResultCode() == PlaceAutocompleteActivity.RESULT_ERROR && data != null) {
                        Status status = PlaceAutocomplete.getResultStatusFromIntent(data);
                        Toast.makeText(requireContext(),
                                "Erro no autocomplete: " + status.getStatusMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void clearFavourite(FavoriteType type) {
        String prefix = (type == FavoriteType.HOME) ? "fav_home_" : "fav_work_";

        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(prefix + "id");
        editor.remove(prefix + "name");
        editor.remove(prefix + "address");
        editor.remove(prefix + "lat");
        editor.remove(prefix + "lng");
        editor.apply();
    }

    private void deleteExtraFavourite(int index) {
        int count = prefs.getInt("extra_fav_count", 0);
        if (index < 0 || index >= count) return;

        SharedPreferences.Editor editor = prefs.edit();

        for (int i = index; i < count - 1; i++) {
            String src = "extra_fav_" + (i + 1) + "_";
            String dst = "extra_fav_" + i + "_";

            editor.putString(dst + "id", prefs.getString(src + "id", null));
            editor.putString(dst + "name", prefs.getString(src + "name", null));
            editor.putString(dst + "address", prefs.getString(src + "address", null));
            editor.putFloat(dst + "lat", prefs.getFloat(src + "lat", 0f));
            editor.putFloat(dst + "lng", prefs.getFloat(src + "lng", 0f));
        }

        String last = "extra_fav_" + (count - 1) + "_";
        editor.remove(last + "id");
        editor.remove(last + "name");
        editor.remove(last + "address");
        editor.remove(last + "lat");
        editor.remove(last + "lng");

        editor.putInt("extra_fav_count", count - 1);
        editor.apply();
    }

    private void saveFavourite(Place place) {
        if (tipoFavoritoAtual == null) {
            destLatLng = place.getLocation();
            inputPontoB.setText(place.getFormattedAddress());
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            return;
        }

        if (tipoFavoritoAtual == FavoriteType.HOME || tipoFavoritoAtual == FavoriteType.WORK) {
            String prefixo = (tipoFavoritoAtual == FavoriteType.HOME) ? "fav_home_" : "fav_work_";

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(prefixo + "id", place.getId());
            editor.putString(prefixo + "name", place.getDisplayName());
            editor.putString(prefixo + "address", place.getFormattedAddress());

            if (place.getLocation() != null) {
                editor.putFloat(prefixo + "lat", (float) place.getLocation().latitude);
                editor.putFloat(prefixo + "lng", (float) place.getLocation().longitude);
            }

            editor.apply();
        } else if (tipoFavoritoAtual == FavoriteType.OTHER) {
            addExtraFavourite(place);
        }

        loadFavourites();
        loadExtraFavourites();
        Toast.makeText(requireContext(), "Favorito guardado: " + place.getDisplayName(), Toast.LENGTH_SHORT).show();
    }

    private void addExtraFavourite(Place place) {
        if (place.getLocation() == null) return;

        int count = prefs.getInt("extra_fav_count", 0);
        String baseKey = "extra_fav_" + count + "_";

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(baseKey + "id", place.getId());
        editor.putString(baseKey + "name", place.getDisplayName());
        editor.putString(baseKey + "address", place.getShortFormattedAddress());
        editor.putFloat(baseKey + "lat", (float) place.getLocation().latitude);
        editor.putFloat(baseKey + "lng", (float) place.getLocation().longitude);
        editor.putInt("extra_fav_count", count + 1);
        editor.apply();
    }

    private void loadExtraFavourites() {
        int count = prefs.getInt("extra_fav_count", 0);

        int indexPlus = containerFavoritos.indexOfChild(btnAdicionarFavorito);

        for (int i = indexPlus - 1; i >= 0; i--) {
            View child = containerFavoritos.getChildAt(i);
            if (child != favoritoCasa && child != favoritoTrabalho) {
                containerFavoritos.removeViewAt(i);
            } else {
                break;
            }
        }

        if (count == 0) {
            return;
        }

        indexPlus = containerFavoritos.indexOfChild(btnAdicionarFavorito);

        for (int i = 0; i < count; i++) {
            String baseKey = "extra_fav_" + i + "_";
            String name = prefs.getString(baseKey + "name", null);
            String address = prefs.getString(baseKey + "address", null);
            float lat = prefs.getFloat(baseKey + "lat", 0f);
            float lng = prefs.getFloat(baseKey + "lng", 0f);

            if (name == null) continue;

            View favView = createViewExtraFavourites(i, name, address, lat, lng);
            containerFavoritos.addView(favView, indexPlus);
            indexPlus++;
        }
    }

    private View createViewExtraFavourites(int index, String name, String address, float lat, float lng) {
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(0, 0, dpToPx(20), dpToPx(20));

        MaterialCardView card = new MaterialCardView(requireContext());
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(dpToPx(64), dpToPx(64));
        card.setLayoutParams(cardParams);
        card.setRadius(dpToPx(36));
        card.setCardElevation(dpToPx(2));
        card.setStrokeWidth(dpToPx(1));
        card.setStrokeColor(Color.parseColor("#E2E8F0"));
        card.setCardBackgroundColor(Color.parseColor("#F8FAFC"));

        ImageView icon = new ImageView(requireContext());
        FrameLayout.LayoutParams iconParams =
                new FrameLayout.LayoutParams(dpToPx(52), dpToPx(52), Gravity.CENTER);
        icon.setLayoutParams(iconParams);
        icon.setImageResource(R.drawable.ic_star);
        icon.setColorFilter(Color.parseColor("#F97316"));

        card.addView(icon);

        TextView titulo = new TextView(requireContext());
        titulo.setText(name);
        titulo.setTextSize(12);
        titulo.setTextColor(Color.parseColor("#475569"));
        titulo.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        titulo.setGravity(Gravity.CENTER_HORIZONTAL);
        titulo.setMaxLines(1);
        titulo.setEllipsize(TextUtils.TruncateAt.END);

        LinearLayout.LayoutParams tituloParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
        tituloParams.topMargin = dpToPx(8);
        titulo.setLayoutParams(tituloParams);

        TextView subtitulo = new TextView(requireContext());
        subtitulo.setTextSize(10);
        subtitulo.setTextColor(Color.parseColor("#64748B"));
        subtitulo.setGravity(Gravity.CENTER_HORIZONTAL);
        subtitulo.setMaxLines(1);
        subtitulo.setEllipsize(TextUtils.TruncateAt.END);

        LinearLayout.LayoutParams subtituloParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
        subtituloParams.topMargin = dpToPx(2);
        subtitulo.setLayoutParams(subtituloParams);

        layout.addView(card);
        layout.addView(titulo);
        layout.addView(subtitulo);

        LatLng latLng = new LatLng(lat, lng);

        layout.setOnClickListener(v ->
                useLatLngAsDestination(latLng, name)
        );

        layout.setTag(index);

        layout.setOnLongClickListener(v -> {
            Object tag = v.getTag();
            if (!(tag instanceof Integer)) return true;

            int idx = (Integer) tag;

            new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Remover favorito?")
                    .setMessage(name)
                    .setPositiveButton("Remover", (dialog, which) -> {
                        deleteExtraFavourite(idx);
                        loadExtraFavourites();
                        Toast.makeText(requireContext(), "Favorito removido", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();

            return true;
        });

        return layout;
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    private void loadFavourites() {
        String homeName = prefs.getString("fav_home_name", null);
        String homeAddress = prefs.getString("fav_home_address", null);

        if (homeName != null) {
            textSubtituloCasa.setText("");
            textSubtituloCasa.setTextColor(Color.parseColor("#475569"));
        } else {
            textSubtituloCasa.setText("Adicionar");
            textSubtituloCasa.setTextColor(Color.parseColor("#3B82F6"));
        }

        String workName = prefs.getString("fav_work_name", null);
        String workAddress = prefs.getString("fav_work_address", null);

        if (workName != null) {
            textSubtituloTrabalho.setText("");
            textSubtituloTrabalho.setTextColor(Color.parseColor("#475569"));
        } else {
            textSubtituloTrabalho.setText("Adicionar");
            textSubtituloTrabalho.setTextColor(Color.parseColor("#3B82F6"));
        }
    }

    private boolean existsFavourite(FavoriteType type) {
        String prefix = (type == FavoriteType.HOME) ? "fav_home_" : "fav_work_";
        return prefs.getString(prefix + "name", null) != null;
    }

    private void useFavourite(FavoriteType tipo) {
        String prefixo = (tipo == FavoriteType.HOME) ? "fav_home_" : "fav_work_";

        String name = prefs.getString(prefixo + "name", null);
        String address = prefs.getString(prefixo + "address", null);
        float lat = prefs.getFloat(prefixo + "lat", 0f);
        float lng = prefs.getFloat(prefixo + "lng", 0f);

        if (name == null || address == null) {
            Toast.makeText(requireContext(), "Favorito não definido", Toast.LENGTH_SHORT).show();
            return;
        }

        LatLng latLng = new LatLng(lat, lng);

        useLatLngAsDestination(latLng, name);
    }

    private void showTurnByTurnDirections(RouteInfo route) {
        if (route.itineraryLines == null || route.itineraryLines.isEmpty()) {
            Toast.makeText(requireContext(),
                    "Sem instruções detalhadas para esta rota",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < route.itineraryLines.size(); i++) {
            sb.append(i + 1)
                    .append(") ")
                    .append(route.itineraryLines.get(i))
                    .append("\n\n");
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Direções passo a passo")
                .setMessage(sb.toString().trim())
                .setPositiveButton("OK", null)
                .show();
    }

    private void useLatLngAsDestination(LatLng latLng, String name) {
        destLatLng = latLng;
        inputPontoB.setText(name);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    private void setupFavouriteClicks() {

        favoritoCasa.setOnClickListener(v -> {
            if (existsFavourite(FavoriteType.HOME)) {
                useFavourite(FavoriteType.HOME);
            } else {
                openAutocomplete(REQ_AUTOCOMPLETE_DEST, FavoriteType.HOME);
            }
        });

        favoritoCasa.setOnLongClickListener(v -> {
            if (!existsFavourite(FavoriteType.HOME)) return true;

            new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Remover Casa?")
                    .setMessage("Queres apagar o favorito Casa?")
                    .setPositiveButton("Remover", (dialog, which) -> {
                        clearFavourite(FavoriteType.HOME);
                        loadFavourites();
                        Toast.makeText(requireContext(), "Casa removida dos favoritos", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();

            return true;
        });

        favoritoTrabalho.setOnLongClickListener(v -> {
            if (!existsFavourite(FavoriteType.WORK)) return true;

            new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Remover Trabalho?")
                    .setMessage("Queres apagar o favorito Trabalho?")
                    .setPositiveButton("Remover", (dialog, which) -> {
                        clearFavourite(FavoriteType.WORK);
                        loadFavourites();
                        Toast.makeText(requireContext(), "Trabalho removido dos favoritos", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();

            return true;
        });

        favoritoTrabalho.setOnClickListener(v -> {
            if (existsFavourite(FavoriteType.WORK)) {
                useFavourite(FavoriteType.WORK);
            } else {
                openAutocomplete(REQ_AUTOCOMPLETE_DEST, FavoriteType.WORK);
            }
        });

        btnAdicionarFavorito.setOnClickListener(v -> {
            openAutocomplete(REQ_AUTOCOMPLETE_DEST, FavoriteType.OTHER);
        });
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

    private void requestRoute(String origin, String destination, String mode,
                              @Nullable String transitMode, @Nullable List<String> waypoints) {
        new Thread(() -> {
            try {
                String urlString = buildDirectionsUrl(origin, destination, mode, transitMode, waypoints);
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.connect();

                InputStream is = conn.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);

                List<RouteInfo> routes = parseRoutes(sb.toString(), mode);

                requireActivity().runOnUiThread(() -> {
                    if (routes.isEmpty()) {
                        Toast.makeText(requireContext(), "Nenhuma rota encontrada", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    drawRoute(routes.get(0).routePoints);

                    currentRoutes.clear();
                    currentRoutes.addAll(routes);
                    routeAdapter.notifyDataSetChanged();

                    View tvResultados = requireView().findViewById(R.id.tv_resultados_titulo);
                    tvResultados.setVisibility(View.VISIBLE);
                    recyclerRotas.setVisibility(View.VISIBLE);

                    startNavigation(routes.get(0));

                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                });

            } catch (Exception e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "Erro ao obter rota", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }


    private List<RouteInfo> parseRoutes(String json, String mode) throws JSONException {
        List<RouteInfo> routes = new ArrayList<>();

        JSONObject jsonObject = new JSONObject(json);
        JSONArray routesArray = jsonObject.getJSONArray("routes");
        if (routesArray.length() == 0) return routes;

        for (int i = 0; i < routesArray.length(); i++) {
            JSONObject routeObj = routesArray.getJSONObject(i);
            RouteInfo info = new RouteInfo();
            info.mode = mode;

            info.summary = routeObj.optString("summary", "Rota " + (i + 1));

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
                        String htmlInstr = step.optString("html_instructions", "");
                        if (!htmlInstr.isEmpty()) {
                            String plain = htmlInstr.replaceAll("<[^>]*>", "");
                            info.itineraryLines.add(plain);

                            if (step.has("end_location")) {
                                JSONObject endLoc = step.getJSONObject("end_location");
                                double endLat = endLoc.getDouble("lat");
                                double endLng = endLoc.getDouble("lng");
                                LatLng endLatLng = new LatLng(endLat, endLng);

                                info.navSteps.add(new RouteInfo.NavStep(plain, endLatLng));
                            }
                        }
                    }
                }
            }

            routes.add(info);
        }

        return routes;
    }

    private String buildDirectionsUrl(String origin, String destination, String mode,
                                      @Nullable String transitMode, @Nullable List<String> waypoints)
            throws UnsupportedEncodingException {

        String strOrigin, strDest;

        if (originLatLng != null && destLatLng != null) {
            strOrigin = "origin=" + originLatLng.latitude + "," + originLatLng.longitude;
            strDest   = "destination=" + destLatLng.latitude + "," + destLatLng.longitude;
        } else {
            strOrigin = "origin=" + URLEncoder.encode(origin, "UTF-8");
            strDest   = "destination=" + URLEncoder.encode(destination, "UTF-8");
        }

        StringBuilder params = new StringBuilder();
        params.append(strOrigin)
                .append("&")
                .append(strDest)
                .append("&mode=").append(mode)
                .append("&alternatives=true");

        params.append("&language=pt-PT");
        if (waypoints != null && !waypoints.isEmpty()) {
            params.append("&waypoints=");
            for (int i = 0; i < waypoints.size(); i++) {
                if (i > 0) params.append("|");
                params.append(URLEncoder.encode(waypoints.get(i), "UTF-8"));
            }
        }

        if ("transit".equals(mode) && transitMode != null) {
            params.append("&transit_mode=").append(transitMode);
        }

        params.append("&key=").append(apiKey);

        System.out.println(params.toString());

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

    private void openAutocomplete(int requestCode, @Nullable FavoriteType type) {
        tipoFavoritoAtual = type;
        isSelectingOrigin = (requestCode == REQ_AUTOCOMPLETE_ORIGIN);

        PlaceAutocomplete.IntentBuilder builder = new PlaceAutocomplete.IntentBuilder();
        builder.setCountries(Collections.singletonList("PT"));

        Intent intent = builder.build(requireContext());
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

    // ========= MODELO PARA A RECYCLER ========= //

    public static class RouteInfo {
        public String summary;
        public String durationText;
        public String arrivalTimeText;
        public String departureTimeText;
        public List<String> itineraryLines = new ArrayList<>();
        public List<LatLng> routePoints = new ArrayList<>();

        public boolean hasTransit = false;
        public String primaryVehicleType = null;

        public List<NavStep> navSteps = new ArrayList<>();

        public String mode;


        public static class NavStep {
            public String instruction;
            public LatLng endLocation;

            public NavStep(String instruction, LatLng endLocation) {
                this.instruction = instruction;
                this.endLocation = endLocation;
            }
        }
    }

    private void toggleFabMenu() {
        if (isFabOpen) {
            closeFabMenu();
        } else {
            openFabMenu();
        }
    }

    private void openFabMenu() {
        fab1.setVisibility(View.VISIBLE);
        fabHistory.setVisibility(View.VISIBLE);
        fab3.setVisibility(View.VISIBLE);
        fabThemedRoutes.setVisibility(View.VISIBLE);


        fab1.setAlpha(0f); fabHistory.setAlpha(0f); fab3.setAlpha(0f); fabThemedRoutes.setAlpha(0f);
        fab1.setTranslationY(100f); fabHistory.setTranslationY(100f); fab3.setTranslationY(100f); fabThemedRoutes.setTranslationY(100f);

        fab1.animate().translationY(0).alpha(1).setDuration(200).setStartDelay(0).start();
        fabHistory.animate().translationY(0).alpha(1).setDuration(200).setStartDelay(50).start();
        fab3.animate().translationY(0).alpha(1).setDuration(200).setStartDelay(100).start();
        fabThemedRoutes.animate().translationY(0).alpha(1).setDuration(200).setStartDelay(150).start();

        isFabOpen = true;
    }

    private void closeFabMenu() {

        fab1.animate().translationY(100f).alpha(0).setDuration(150).withEndAction(() -> fab1.setVisibility(View.GONE)).start();
        fabHistory.animate().translationY(100f).alpha(0).setDuration(150).withEndAction(() -> fabHistory.setVisibility(View.GONE)).start();
        fab3.animate().translationY(100f).alpha(0).setDuration(150).withEndAction(() -> fab3.setVisibility(View.GONE)).start();
        fabThemedRoutes.animate().translationY(100f).alpha(0).setDuration(150).withEndAction(() -> fabThemedRoutes.setVisibility(View.GONE)).start();

        isFabOpen = false;
    }

    @Override
    public void onStop() {
        super.onStop();
        stopNavigation();
    }

    private void updateNavInstructionUI() {
        if (cardNavigation == null) return;

        if (!isNavigating ||
                currentNavSteps.isEmpty() ||
                currentNavStepIndex < 0 ||
                currentNavStepIndex >= currentNavSteps.size()) {

            cardNavigation.setVisibility(View.GONE);
            return;
        }

        RouteInfo.NavStep step = currentNavSteps.get(currentNavStepIndex);

        if (tvNavInstruction != null) {
            tvNavInstruction.setText(step.instruction);
        }

        if (tvNavDistance != null) {
            String distanceText;

            if (currentDistanceToEndMeters < 0) {
                distanceText = "A calcular…";

            } else if (currentDistanceToEndMeters < 25) {
                distanceText = "Agora";

            } else if (currentDistanceToEndMeters < 1000) {
                int m = Math.round(currentDistanceToEndMeters);
                distanceText = "Em " + m + " m";

            } else if (currentDistanceToEndMeters < 5000) {
                float km = currentDistanceToEndMeters / 1000f;
                distanceText = String.format("Em %.1f km", km);

            } else {
                distanceText = "Continuar em frente";
            }

            tvNavDistance.setText(distanceText);
        }

        if (tvNavDetail != null) {
            if (currentNavStepIndex < currentNavSteps.size() - 1) {
                RouteInfo.NavStep nextStep = currentNavSteps.get(currentNavStepIndex + 1);
                tvNavDetail.setText("Depois: " + nextStep.instruction);
            } else {
                tvNavDetail.setText("Este é o último passo antes do destino.");
            }
        }


        cardNavigation.setVisibility(View.VISIBLE);
    }


    private void handleNavigationLocationUpdate(Location loc) {
        if (!isNavigating ||
                currentNavSteps.isEmpty() ||
                currentNavStepIndex < 0 ||
                currentNavStepIndex >= currentNavSteps.size()) {
            return;
        }

        RouteInfo.NavStep step = currentNavSteps.get(currentNavStepIndex);
        LatLng end = step.endLocation;
        if (end == null) return;

        float[] results = new float[1];
        Location.distanceBetween(
                loc.getLatitude(), loc.getLongitude(),
                end.latitude, end.longitude,
                results
        );
        float distanceToEnd = results[0];

        currentDistanceToEndMeters = distanceToEnd;

        updateNavInstructionUI();

        if (distanceToEnd < 25f) {
            if (currentNavStepIndex < currentNavSteps.size() - 1) {
                currentNavStepIndex++;
                currentDistanceToEndMeters = -1f;
                updateNavInstructionUI();
            } else {
                Toast.makeText(requireContext(), "Chegaste ao destino!", Toast.LENGTH_LONG).show();
                stopNavigation();
            }
        }
    }


    private void stopNavigation() {
        if (!isNavigating) return;

        isNavigating = false;
        currentNavSteps.clear();
        currentNavStepIndex = -1;
        currentDistanceToEndMeters = -1f;

        if (navLocationCallback != null && fused != null) {
            fused.removeLocationUpdates(navLocationCallback);
        }

        if (cardNavigation != null) {
            cardNavigation.setVisibility(View.GONE);
        }

    }

    private void startNavigation(RouteInfo route) {
        if (route.navSteps == null || route.navSteps.isEmpty()) {
            Toast.makeText(requireContext(), "Sem passos de navegação para esta rota", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isNavigating) {
            stopNavigation();
        }

        currentNavSteps.clear();
        currentNavSteps.addAll(route.navSteps);
        currentNavStepIndex = 0;
        isNavigating = true;

        currentDistanceToEndMeters = -1f;
        updateNavInstructionUI();

        if (navLocationCallback == null) {
            navLocationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(@NonNull LocationResult locationResult) {
                    if (!isNavigating) return;

                    for (Location loc : locationResult.getLocations()) {
                        handleNavigationLocationUpdate(loc);
                    }
                }
            };
        }

        LocationRequest request = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                2000L
        )
                .setMinUpdateIntervalMillis(1000L)
                .build();

        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(requireContext(), "Sem permissão de localização para navegação", Toast.LENGTH_SHORT).show();
            return;
        }

        fused.requestLocationUpdates(request, navLocationCallback, requireActivity().getMainLooper());

    }

    private void setupIds(View view) {

        MaterialCardView bottomSheet = view.findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setHideable(false);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        bottomSheetScroll = view.findViewById(R.id.bottom_sheet_scroll);

        menuFabContainer = view.findViewById(R.id.menu_fab_container);
        fabMain = view.findViewById(R.id.fab_main);
        fab1 = view.findViewById(R.id.fab_1);
        fabHistory = view.findViewById(R.id.fab_2);
        fab3 = view.findViewById(R.id.fab_3);
        fabThemedRoutes = view.findViewById(R.id.fab_themed_routes);

        tvIniciais = view.findViewById(R.id.tv_iniciais);

        containerFavoritos = view.findViewById(R.id.container_favoritos);
        favoritoCasa = view.findViewById(R.id.favorito_casa);
        favoritoTrabalho = view.findViewById(R.id.favorito_trabalho);
        btnAdicionarFavorito = view.findViewById(R.id.btn_adicionar_favorito);

        textSubtituloCasa = view.findViewById(R.id.text_subtitulo_casa);
        textSubtituloTrabalho = view.findViewById(R.id.text_subtitulo_trabalho);

        cardNavigation = view.findViewById(R.id.card_navigation);
        tvNavInstruction = view.findViewById(R.id.tv_nav_instruction);
        tvNavDetail = view.findViewById(R.id.tv_nav_detail);
        tvNavDistance = view.findViewById(R.id.tv_distance);
        ivNavDirection = view.findViewById(R.id.iv_nav_direction);

        inputPontoA = view.findViewById(R.id.input_ponto_a);
        inputPontoB = view.findViewById(R.id.input_ponto_b);

        LinearLayout tilPontoA = view.findViewById(R.id.til_ponto_a);
        LinearLayout tilPontoB = view.findViewById(R.id.til_ponto_b);

        inputPontoA.setFocusable(false);
        inputPontoA.setOnClickListener(v -> {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            openAutocomplete(REQ_AUTOCOMPLETE_ORIGIN, null);
        });

        inputPontoB.setFocusable(false);
        inputPontoB.setOnClickListener(v -> {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            openAutocomplete(REQ_AUTOCOMPLETE_DEST, null);
        });

        inputPontoA.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        });
        inputPontoB.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        });

        View.OnClickListener expandListener = v ->
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        tilPontoA.setOnClickListener(expandListener);
        tilPontoB.setOnClickListener(expandListener);

        chipGroupTransporte = view.findViewById(R.id.chip_group_transporte);

        btnInverter = view.findViewById(R.id.btn_inverter);
        btnInverter.setOnClickListener(v -> {
            CharSequence tempText = inputPontoA.getText();
            inputPontoA.setText(inputPontoB.getText());
            inputPontoB.setText(tempText);

            LatLng tempLatLng = originLatLng;
            originLatLng = destLatLng;
            destLatLng = tempLatLng;
        });

    }

    private void setupFindRoutes(View view) {
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

            int checkedId = chipGroupTransporte.getCheckedChipId();

            String mode = "driving";
            String transitMode = null;
            String modoLabel = "Carro";

            if (checkedId == R.id.chip_pe) {
                mode = "walking";
                transitMode = null;
                modoLabel = "A pé";

            } else if (checkedId == R.id.chip_comboio) {
                mode = "transit";
                transitMode = "train";
                modoLabel = "Comboio";

            } else if (checkedId == R.id.chip_bicicleta) {
                mode = "bicycling";
                transitMode = null;
                modoLabel = "Bicicleta";

            } else if (checkedId == R.id.chip_autocarro) {
                mode = "transit";
                transitMode = "bus";
                modoLabel = "Autocarro";

            } else if (checkedId == R.id.chip_carro) {
                mode = "driving";
                transitMode = null;
                modoLabel = "Carro";
            }

            Toast.makeText(getContext(),
                    "A calcular rota de " + pontoA + " para " + pontoB + " via " + modoLabel,
                    Toast.LENGTH_SHORT).show();

            requestRoute(pontoA, pontoB, mode, transitMode, null);
            bottomSheetScroll.post(() -> bottomSheetScroll.scrollTo(0, 0));

            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        });
    }

    private void setupFabClicks() {

        fabMain.setOnClickListener(v -> toggleFabMenu());

        fab1.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Perfil", Toast.LENGTH_SHORT).show();
            closeFabMenu();
        });

        fabHistory.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), HistoryActivity.class);
            historyLauncher.launch(intent);
            closeFabMenu();
        });

        fab3.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Definições", Toast.LENGTH_SHORT).show();
            closeFabMenu();
        });

        fabThemedRoutes.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), ThemedRoutesActivity.class);
            themedRouteLauncher.launch(intent);
            closeFabMenu();
        });

    }


}
