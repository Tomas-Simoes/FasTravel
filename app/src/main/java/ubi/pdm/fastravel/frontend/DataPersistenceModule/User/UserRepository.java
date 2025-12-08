package ubi.pdm.fastravel.frontend.DataPersistenceModule.User;

import android.content.Context;

import java.lang.reflect.Type;

import ubi.pdm.fastravel.frontend.APIModule.ApiService;
import ubi.pdm.fastravel.frontend.DataPersistenceModule.CacheManager;

public class UserRepository {
    private static final String USER_CACHE_KEY = "user_data";

    private final CacheManager cacheManager;
    private final ApiService apiService;

    public UserRepository(Context context, ApiService apiService) {
        this.cacheManager = new CacheManager(context);
        this.apiService = apiService;
    }

    public UserData getUser() {
        Type userType = new com.google.gson.reflect.TypeToken<UserData>() {}.getType();
        UserData cachedUser = cacheManager.getFromCache(USER_CACHE_KEY, userType);

        if (cachedUser != null) {
            return cachedUser;
        }

        try {
            UserData user = apiService.fetchUser();
            cacheManager.saveToCache(USER_CACHE_KEY, user);
            return user;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error getting user from API", e);
        }
    }

    public void clearUserCache() {
        cacheManager.clearCache(USER_CACHE_KEY);
    }

    public boolean hasCachedUser() {
        return cacheManager.hasCachedData(USER_CACHE_KEY);
    }
}
