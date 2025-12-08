package ubi.pdm.fastravel.frontend.ui.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;
import ubi.pdm.fastravel.R;
import ubi.pdm.fastravel.frontend.ThemedRoutesModule.ThemedRoutesController;
import ubi.pdm.fastravel.frontend.ThemedRoutesModule.ThemedRoutesService;
import ubi.pdm.fastravel.frontend.ThemedRoutesModule.ThemedRoute;

public class ThemedRoutesActivity extends AppCompatActivity {

    private static final String TAG = "ThemedRoutesFragment";
    private static final int LOCATION_PERMISSION_REQUEST = 1001;

    private static final double DEFAULT_LAT = 41.1579;
    private static final double DEFAULT_LNG = -8.6291;

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvLoading;
    private TextView tvError;

    private List<ThemedRoute> themedRoutes = new ArrayList<>();
    private ThemedRoutesController adapter;

    private ThemedRoutesService routesService;
    private FusedLocationProviderClient fusedLocationClient;

    private double userLat = DEFAULT_LAT;
    private double userLng = DEFAULT_LNG;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_themedroutes);

        ImageButton btnBack = findViewById(R.id.btnBack);
        recyclerView = findViewById(R.id.routesRecycler);
        progressBar = findViewById(R.id.progressBar);
        tvLoading = findViewById(R.id.tvLoading);
        tvError = findViewById(R.id.tvError);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(gridLayoutManager);

        int spacing = getResources().getDimensionPixelSize(R.dimen.card_spacing);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, spacing, true));

        btnBack.setOnClickListener(v -> finish());

        ThemedRoutesController.OnRouteClickListener listener = new ThemedRoutesController.OnRouteClickListener() {
            @Override
            public void onSelect(ThemedRoute selectedRoute) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("selected_themed_route", selectedRoute);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }

            @Override
            public void onLearnMore(ThemedRoute r) {
                Toast.makeText(ThemedRoutesActivity.this,
                        "Details: " + r.getTitle(),
                        Toast.LENGTH_SHORT).show();
            }
        };

        adapter = new ThemedRoutesController(themedRoutes, listener);
        recyclerView.setAdapter(adapter);

        String apiKey = getString(R.string.google_maps_key);
        routesService = new ThemedRoutesService(this, apiKey);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        getLocationAndFetchRoutes();
    }

    private void getLocationAndFetchRoutes() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST
            );
            return;
        }

        showLoading(true);

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        userLat = location.getLatitude();
                        userLng = location.getLongitude();
                        Log.d(TAG, "User Location obtained: " + userLat + ", " + userLng);
                    } else {
                        Log.w(TAG, "Location null, using default: Porto");
                        Toast.makeText(ThemedRoutesActivity.this,
                                "Using default location (Porto)",
                                Toast.LENGTH_SHORT).show();
                    }

                    fetchAllRoutes();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting location", e);
                    Toast.makeText(ThemedRoutesActivity.this,
                            "Error getting location. Using default location (Porto)",
                            Toast.LENGTH_SHORT).show();

                    fetchAllRoutes();
                });
    }

    private void fetchAllRoutes() {
        Log.d(TAG, "Searching routes for: " + userLat + ", " + userLng);

        showLoading(true);
        tvLoading.setText("Searching for routes near you...");

        routesService.fetchAllRoutes(userLat, userLng, results -> {
            runOnUiThread(() -> {
                showLoading(false);

                int successCount = 0;

                for (ThemedRoutesService.RouteResult result : results) {
                    if (result.success && result.places != null && !result.places.isEmpty()) {
                        ThemedRoute route = createRouteFromPlaces(result.type, result.places);
                        themedRoutes.add(route);
                        successCount++;

                        StringBuilder placesList = new StringBuilder();

                        for (ThemedRoutesService.Place p : result.places) {
                            placesList.append("\n   • ")
                                    .append(p.name)
                                    .append(" (")
                                    .append(p.lat).append(", ").append(p.lng).append(")");
                            if (p.rating > 0) {
                                placesList.append(" ⭐").append(p.rating);
                            }
                        }

                        Log.d(TAG,
                                result.type.getDisplayName() + ": " +
                                        result.places.size() + " places found:" +
                                        placesList
                        );

                    } else {
                        Log.w(TAG, result.type.getDisplayName() + " failed: " +
                                (result.errorMessage != null ? result.errorMessage : "No places found"));
                    }
                }

                if (successCount > 0) {
                    adapter.notifyDataSetChanged();
                    recyclerView.setVisibility(View.VISIBLE);
                    tvError.setVisibility(View.GONE);

                    Toast.makeText(ThemedRoutesActivity.this,
                            successCount + " routes found!!",
                            Toast.LENGTH_SHORT).show();
                } else {
                    showError("No routes found near you. Try another location.");
                }
            });
        });
    }

    private ThemedRoute createRouteFromPlaces(ThemedRoutesService.RouteType type,
                                              List<ThemedRoutesService.Place> places) {

        String photoUrl = null;
        if (!places.isEmpty() && places.get(0).photoReference != null) {
            photoUrl = "https://maps.googleapis.com/maps/api/place/photo" +
                    "?maxwidth=800" +
                    "&photo_reference=" + places.get(0).photoReference +
                    "&key=" + getString(R.string.google_maps_key);
        }

        String pathText = buildPathDescription(places);
        String distance = calculateTotalDistance(places);
        String time = calculateTotalTime(places);

        return new ThemedRoute(
                type.getDisplayName(),
                getColorForType(type),
                getImageForType(type),
                photoUrl,
                distance,
                time,
                pathText
        );
    }

    private String calculateTotalTime(List<ThemedRoutesService.Place> places) {
        int stops = places.size();
        int minutes = (stops * 12) + ((stops - 1) * 8);

        int h = minutes / 60;
        int m = minutes % 60;

        if (h > 0)
            return h + "h " + m + "m";

        return m + " min";
    }

    private String buildPathDescription(List<ThemedRoutesService.Place> places) {
        if (places.isEmpty()) return "No points found.";

        StringBuilder path = new StringBuilder();

        for (int i = 0; i < places.size(); i++) {
            path.append(places.get(i).name);

            if (i < places.size() - 1) {
                path.append("  →  ");
            }
        }

        return path.toString();
    }

    private String calculateTotalDistance(List<ThemedRoutesService.Place> places) {
        if (places.size() < 2) return "0 m";

        double total = 0;
        for (int i = 0; i < places.size() - 1; i++) {
            total += distanceBetween(places.get(i), places.get(i+1));
        }

        if (total > 1000)
            return String.format("%.1f km", total / 1000.0);
        else
            return String.format("%.0f m", total);
    }

    private double distanceBetween(ThemedRoutesService.Place a, ThemedRoutesService.Place b) {
        float[] result = new float[1];
        Location.distanceBetween(a.lat, a.lng, b.lat, b.lng, result);
        return result[0];
    }

    private String getColorForType(ThemedRoutesService.RouteType type) {
        switch (type) {
            case CULTURAL:
                return "#E91E63";
            case HISTORICAL:
                return "#FF6F00";
            case GREEN:
                return "#2E7D32";
            case GASTRONOMIC:
                return "#1565C0";
            default:
                return "#1565C0";
        }
    }

    private int getImageForType(ThemedRoutesService.RouteType type) {
        switch (type) {
            case CULTURAL:
                return R.drawable.route_arts;
            case HISTORICAL:
                return R.drawable.route_heritage;
            case GREEN:
                return R.drawable.route_eco;
            case GASTRONOMIC:
                return R.drawable.route_modern;
            default:
                return R.drawable.route_arts;
        }
    }

    private void showLoading(boolean show) {
        if (progressBar != null && tvLoading != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            tvLoading.setVisibility(show ? View.VISIBLE : View.GONE);
            recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
            tvError.setVisibility(View.GONE);
        }
    }

    private void showError(String message) {
        if (tvError != null) {
            tvError.setText(message);
            tvError.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
            tvLoading.setVisibility(View.GONE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocationAndFetchRoutes();
            } else {
                Toast.makeText(this, "Location permission denied. Using default location (Porto).", Toast.LENGTH_LONG).show();
                fetchAllRoutes();
            }
        }
    }

    public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {
        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view);
            int column = position % spanCount;

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount;
                outRect.right = (column + 1) * spacing / spanCount;
                if (position < spanCount) {
                    outRect.top = spacing;
                }
                outRect.bottom = spacing;
            } else {
                outRect.left = column * spacing / spanCount;
                outRect.right = spacing - (column + 1) * spacing / spanCount;
                if (position >= spanCount) {
                    outRect.top = spacing;
                }
            }
        }
    }
}