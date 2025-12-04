package ubi.pdm.fastravel.frontend;

import android.app.Application;

import com.google.android.libraries.places.api.Places;

import ubi.pdm.fastravel.R;

public class FastTravelApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        String apiKey = getString(R.string.google_maps_key);

        if (!Places.isInitialized()) {
            Places.initializeWithNewPlacesApiEnabled(getApplicationContext(), apiKey);
        }
    }
}