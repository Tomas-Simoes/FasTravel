package ubi.pdm.fastravel.frontend.DataPersistenceModule;
import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.concurrent.TimeUnit;

public class CacheManager {
    private static final String PREFS_NAME = "ApiCache";
    private static final String CACHE_PREFIX = "cache_";
    private static final String TIMESTAMP_PREFIX = "timestamp_";
    private static final long DEFAULT_CACHE_DURATION = TimeUnit.HOURS.toMillis(1); // 1 hora por defeito

    private SharedPreferences prefs;
    private Gson gson;

    public CacheManager(Context context) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }

    public <T> void saveToCache(String key, T data) {
        String json = gson.toJson(data);
        long timestamp = System.currentTimeMillis();

        prefs.edit()
                .putString(CACHE_PREFIX + key, json)
                .putLong(TIMESTAMP_PREFIX + key, timestamp)
                .apply();
    }

    public <T> T getFromCache(String key, Class<T> classType) {
        String json = prefs.getString(CACHE_PREFIX + key, null);
        long timestamp = prefs.getLong(TIMESTAMP_PREFIX + key, 0);

        if (json == null || timestamp == 0) {
            return null;
        }

        if (System.currentTimeMillis() - timestamp > DEFAULT_CACHE_DURATION) {
            clearCache(key);
            return null;
        }

        return gson.fromJson(json, classType);
    }

    public <T> T getFromCache(String key, Type typeToken) {
        String json = prefs.getString(CACHE_PREFIX + key, null);
        long timestamp = prefs.getLong(TIMESTAMP_PREFIX + key, 0);

        if (json == null || timestamp == 0) {
            return null;
        }

        if (System.currentTimeMillis() - timestamp > DEFAULT_CACHE_DURATION) {
            clearCache(key);
            return null;
        }

        return gson.fromJson(json, typeToken);
    }

    public void clearCache(String key) {
        prefs.edit()
                .remove(CACHE_PREFIX + key)
                .remove(TIMESTAMP_PREFIX + key)
                .apply();
    }

    public void clearAllCache() {
        prefs.edit().clear().apply();
    }

    public boolean hasCachedData(String key) {
        String json = prefs.getString(CACHE_PREFIX + key, null);
        long timestamp = prefs.getLong(TIMESTAMP_PREFIX + key, 0);

        if (json == null || timestamp == 0) {
            return false;
        }

        return System.currentTimeMillis() - timestamp <= DEFAULT_CACHE_DURATION;
    }
}