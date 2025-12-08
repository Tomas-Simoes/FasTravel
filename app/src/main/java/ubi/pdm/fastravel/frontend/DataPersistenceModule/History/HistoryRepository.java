package ubi.pdm.fastravel.frontend.DataPersistenceModule.User;

import android.content.Context;
import android.util.Log;

import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import ubi.pdm.fastravel.frontend.APIModule.ApiService;
import ubi.pdm.fastravel.frontend.DataPersistenceModule.CacheManager;

public class HistoryRepository {

    private static final String HISTORY_CACHE_KEY = "user_histories";
    private final CacheManager cache;
    private final ApiService apiService;
    private String lastErrorMessage;

    public HistoryRepository(Context context) {
        this.cache = new CacheManager(context);
        this.apiService = new ApiService(context);
    }

    /**
     * Cria um novo histórico de viagem
     */
    public UserHistory createHistory(String origin, String destiny, String travelDate) {
        try {
            UserHistory history = apiService.createHistory(origin, destiny, travelDate);
            lastErrorMessage = null;
            return history;
        } catch (Exception e) {
            e.printStackTrace();
            String message = e.getMessage();
            Log.e("HistoryRepository", "Erro ao criar histórico: " + message);

            try {
                JSONObject json = new JSONObject(message);
                JSONArray errors = json.optJSONArray("errors");
                lastErrorMessage = (errors != null && errors.length() > 0)
                        ? errors.getString(0)
                        : "Erro desconhecido ao criar histórico";
            } catch (Exception jsonEx) {
                lastErrorMessage = message != null ? message : "Erro desconhecido";
            }
            return null;
        }
    }

    /**
     * Obtém históricos da cache ou da API
     */
    public List<UserHistory> getHistoriesFromCacheOrApi() {
        // Tentar obter da cache primeiro
        Type listType = new TypeToken<List<UserHistory>>(){}.getType();
        List<UserHistory> cachedHistories = cache.getFromCache(HISTORY_CACHE_KEY, listType);

        if (cachedHistories != null) {
            Log.d("HistoryRepository", "Históricos obtidos da cache");
            return cachedHistories;
        }

        // Se não estiver na cache, buscar da API
        try {
            List<UserHistory> histories = apiService.fetchHistories();
            Log.d("HistoryRepository", "Históricos obtidos da API");
            return histories;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("HistoryRepository", "Erro ao buscar históricos: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Força refresh dos históricos da API
     */
    public List<UserHistory> refreshHistories() {
        cache.clearCache(HISTORY_CACHE_KEY);
        return getHistoriesFromCacheOrApi();
    }

    /**
     * Limpa o cache de históricos
     */
    public void clearHistoryCache() {
        cache.clearCache(HISTORY_CACHE_KEY);
    }

    public String getLastErrorMessage() {
        return lastErrorMessage;
    }
}