package ubi.pdm.fastravel.frontend.DataPersistenceModule.User;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import ubi.pdm.fastravel.frontend.APIModule.ApiService;
import ubi.pdm.fastravel.frontend.APIModule.RequestResponse.LoginResponse;
import ubi.pdm.fastravel.frontend.DataPersistenceModule.CacheManager;

public class UserRepository {

    private static final String USER_CACHE_KEY = "user_data";
    private final CacheManager cache;
    private final ApiService apiService;

    // Variável para guardar a última mensagem de erro do servidor
    private String lastErrorMessage;

    public UserRepository(Context context) {
        this.cache = new CacheManager(context);
        this.apiService = new ApiService(context);
    }

    public UserData registerUser(String name, String email, String password) {
        try {
            LoginResponse response = apiService.register(name, email, password);
            lastErrorMessage = null;
            return response.user;
        } catch (Exception e) {
            e.printStackTrace();
            String message = e.getMessage();
            Log.d("aaaa", e.toString());
            try {
                JSONObject json = new JSONObject(message);
                JSONArray errors = json.optJSONArray("errors");
                lastErrorMessage = (errors != null && errors.length() > 0) ? errors.getString(0) : "Erro desconhecido";
            } catch (Exception jsonEx) {
                lastErrorMessage = "Erro desconhecido";
            }
            return null;
        }

    }

    public String getLastErrorMessage() {
        return lastErrorMessage;
    }

    public UserData loginAndGetUser(String email, String password) {
        try {
            LoginResponse response = apiService.login(email, password);
            return response.user;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public UserData getUserFromCacheOrApi() {
        UserData cachedUser = cache.getFromCache(USER_CACHE_KEY, UserData.class);
        if (cachedUser != null) return cachedUser;

        try {
            return apiService.fetchUser();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
