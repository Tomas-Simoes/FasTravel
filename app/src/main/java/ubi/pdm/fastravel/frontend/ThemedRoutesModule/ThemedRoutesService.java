package ubi.pdm.fastravel.frontend.ThemedRoutesModule;

import android.content.Context;
import android.util.Log;
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
import java.util.List;

public class ThemedRoutesService {

    private static final String TAG = "ThemedRoutesService";
    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?";

    private final String apiKey;
    private final Context context;

    public ThemedRoutesService(Context context, String apiKey) {
        this.context = context;
        this.apiKey = apiKey;
    }

    public enum RouteType {
        CULTURAL(
                "museum|art_gallery",
                "art culture museum exhibition",
                5000,
                "Cultural Route"
        ),
        HISTORICAL(
                "tourist_attraction|point_of_interest|church|museum|city_hall|establishment",
                "historic old monument landmark heritage cathedral church",
                5000,
                "Historical Route"
        ),
        GREEN(
                "park|natural_feature|campground",
                "nature trail hiking green area",
                15000,
                "Green Route"
        ),
        GASTRONOMIC(
                "restaurant|food|cafe",
                "traditional local cuisine gastronomy",
                5000,
                "Gastronomic Route"
        );

        private final String placeType;
        private final String keyword;
        private final int radius;
        private final String displayName;

        RouteType(String placeType, String keyword, int radius, String displayName) {
            this.placeType = placeType;
            this.keyword = keyword;
            this.radius = radius;
            this.displayName = displayName;
        }

        public String getPlaceType() { return placeType; }
        public String getKeyword() { return keyword; }
        public int getRadius() { return radius; }
        public String getDisplayName() { return displayName; }
    }


    /**
     * Fetches nearby places for a specific thematic route type.
     * Executes in a background thread and returns via the callback.
     *
     * @param routeType The type of themed route to search (CULTURAL, HISTORICAL, GREEN, GASTRONOMIC)
     * @param lat       User latitude
     * @param lng       User longitude
     * @param callback  Callback invoked when the request finishes
     */
    public void fetchRouteByType(RouteType routeType, double lat, double lng, RouteFetchCallback callback) {
        new Thread(() -> {
            try {
                Log.d(TAG, "Searching " + routeType.getDisplayName() + "...");

                String urlString = buildPlacesUrl(
                        routeType.getPlaceType(),
                        routeType.getKeyword(),
                        lat,
                        lng,
                        routeType.getRadius()
                );

                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);
                conn.connect();

                int responseCode = conn.getResponseCode();
                if (responseCode != 200) {
                    callback.onError("HTTP Error: " + responseCode);
                    return;
                }

                InputStream is = conn.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                StringBuilder sb = new StringBuilder();
                String line;

                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }

                br.close();
                is.close();
                conn.disconnect();

                String json = sb.toString();

                List<Place> places = parsePlaces(json);
               List<Place> filtered = filterPlaces(places);
                Log.d(TAG, routeType.getDisplayName() + " encontrou " + places.size() + " lugares");

                callback.onSuccess(routeType, filtered);
            } catch (Exception e) {
                Log.e(TAG, "Erro ao buscar " + routeType.getDisplayName(), e);
                callback.onError("Erro: " + e.getMessage());
            }
        }).start();
    }

    /**
     * Fetches places for all thematic route types in parallel.
     * Calls the callback only after all individual searches complete.
     *
     * @param lat      User latitude
     * @param lng      User longitude
     * @param callback Callback invoked once all route searches are completed
     */
    public void fetchAllRoutes(double lat, double lng, AllRoutesFetchCallback callback) {
        RouteType[] allTypes = RouteType.values();
        final int totalRoutes = allTypes.length;
        final List<RouteResult> results = new ArrayList<>();
        final int[] completedCount = {0};

        RouteFetchCallback individualCallback = new RouteFetchCallback() {
            @Override
            public void onSuccess(RouteType type, List<Place> places) {
                synchronized (results) {
                    results.add(new RouteResult(type, places, true, null));
                    completedCount[0]++;

                    if (completedCount[0] == totalRoutes) {
                        callback.onAllRoutesCompleted(results);
                    }
                }
            }

            @Override
            public void onError(String error) {
                synchronized (results) {
                    completedCount[0]++;

                    if (completedCount[0] == totalRoutes) {
                        callback.onAllRoutesCompleted(results);
                    }
                }
            }
        };

        for (RouteType type : allTypes) {
            fetchRouteByType(type, lat, lng, individualCallback);
        }
    }

    /**
     * Builds the final URL for a Google Places API Nearby Search request.
     *
     * @param type    Comma-separated list of place types
     * @param keyword Keyword string to improve relevance
     * @param lat     Latitude
     * @param lng     Longitude
     * @param radius  Search radius in meters
     * @return Fully constructed request URL
     */
    private String buildPlacesUrl(String type, String keyword, double lat, double lng, int radius)
            throws UnsupportedEncodingException {

        String location = lat + "," + lng;

        StringBuilder params = new StringBuilder();
        params.append("location=").append(location)
                .append("&radius=").append(radius)
                .append("&type=").append(type);

        if (keyword != null && !keyword.isEmpty()) {
            params.append("&keyword=").append(URLEncoder.encode(keyword, "UTF-8"));
        }

        params.append("&key=").append(apiKey);

        String finalUrl = PLACES_API_BASE + params.toString();
        Log.d(TAG, "URL: " + finalUrl.replace(apiKey, "***API_KEY***")); // Log sem expor key

        return finalUrl;
    }

    /**
     * Parses the JSON response from the Google Places API
     * into a list of internal Place objects.
     *
     * @param json Raw JSON response
     * @return Parsed list of places
     */
    private List<Place> parsePlaces(String json) throws JSONException {
        List<Place> places = new ArrayList<>();

        JSONObject jsonObject = new JSONObject(json);
        String status = jsonObject.optString("status");

        Log.d(TAG, "API Status: " + status);

        if (!"OK".equals(status)) {
            String errorMessage = jsonObject.optString("error_message", "Status: " + status);
            Log.w(TAG, "API returned status: " + status + " - " + errorMessage);
            return places;
        }

        JSONArray results = jsonObject.getJSONArray("results");
        Log.d(TAG, "Places found: " + results.length());

        for (int i = 0; i < results.length(); i++) {
            JSONObject placeObj = results.getJSONObject(i);

            Place place = new Place();
            place.placeId = placeObj.optString("place_id");
            place.name = placeObj.optString("name");
            place.rating = (float) placeObj.optDouble("rating", 0.0);
            place.userRatingsTotal = placeObj.optInt("user_ratings_total", 0);
            place.vicinity = placeObj.optString("vicinity", "");

            JSONArray types = placeObj.optJSONArray("types");

            if (types != null) {
                place.types = new ArrayList<>();
                for (int t = 0; t < types.length(); t++) {
                    place.types.add(types.getString(t));
                }
            }

            if (placeObj.has("geometry")) {
                JSONObject geometry = placeObj.getJSONObject("geometry");
                JSONObject location = geometry.getJSONObject("location");
                place.lat = location.getDouble("lat");
                place.lng = location.getDouble("lng");
            }

            if (placeObj.has("photos")) {
                JSONArray photos = placeObj.getJSONArray("photos");
                if (photos.length() > 0) {
                    JSONObject firstPhoto = photos.getJSONObject(0);
                    place.photoReference = firstPhoto.optString("photo_reference");
                }
            }

            places.add(place);
        }

        return places;
    }

    /**
     * Sorts places by rating and popularity, and limits the size of the list.
     *
     * @param places Raw list of places returned by the API
     * @return Filtered and sorted list of places (max 10)
     */
    private List<Place> filterPlaces(List<Place> places) {

        places.sort((p1, p2) -> {
            int ratingCompare = Float.compare(p2.rating, p1.rating);
            if (ratingCompare != 0) return ratingCompare;

            return Integer.compare(p2.userRatingsTotal, p1.userRatingsTotal);
        });

        int limit = Math.min(10, places.size());
        return places.subList(0, limit);
    }

    public static class Place {
        public String placeId;
        public String name;
        public float rating;
        public int userRatingsTotal;
        public String vicinity;
        public double lat;
        public double lng;
        public List<String> types;
        public String photoReference;

        @Override
        public String toString() {
            return name + " (" + rating + "⭐ - " + userRatingsTotal + " reviews)";
        }
    }

    public static class RouteResult {
        public RouteType type;
        public List<Place> places;
        public boolean success;
        public String errorMessage;

        public RouteResult(RouteType type, List<Place> places, boolean success, String errorMessage) {
            this.type = type;
            this.places = places;
            this.success = success;
            this.errorMessage = errorMessage;
        }
    }

    public interface RouteFetchCallback {
        void onSuccess(RouteType type, List<Place> places);
        void onError(String error);
    }

    public interface AllRoutesFetchCallback {
        void onAllRoutesCompleted(List<RouteResult> results);
    }
}

