package ubi.pdm.fastravel.frontend.DataPersistenceModule.User;

import android.content.Context;

import ubi.pdm.fastravel.frontend.APIModule.ApiService;
import ubi.pdm.fastravel.frontend.APIModule.RequestResponse.LoginResponse;
import ubi.pdm.fastravel.frontend.DataPersistenceModule.CacheManager;

public class UserRepository {

    private static final String USER_CACHE_KEY = "user_data";
    private final CacheManager cache;
    private final ApiService apiService;

    public UserRepository(Context context) {
        this.cache = new CacheManager(context);
        this.apiService = new ApiService(context);
    }

    public UserData registerUser(String name, String email, String password) {
        try {
            LoginResponse response = apiService.register(name, email, password);
            return response.user;
        } catch (Exception e) {
            e.printStackTrace();
            return null; // ou lançar RuntimeException
        }
    }

    public UserData loginAndGetUser(String email, String password) {
        try {
            LoginResponse response = apiService.login(email, password);
            return response.user;
        } catch (Exception e) {
            e.printStackTrace();
            return null; // ou lançar runtime exception
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
