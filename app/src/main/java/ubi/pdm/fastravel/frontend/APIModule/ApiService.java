package ubi.pdm.fastravel.frontend.APIModule;

import android.content.Context;

import retrofit2.Call;
import ubi.pdm.fastravel.frontend.APIModule.RequestResponse.LoginRequest;
import ubi.pdm.fastravel.frontend.APIModule.RequestResponse.LoginResponse;
import ubi.pdm.fastravel.frontend.DataPersistenceModule.CacheManager;
import ubi.pdm.fastravel.frontend.DataPersistenceModule.User.UserData;

public class ApiService {
    private final ApiInterface api;
    private final CacheManager cache;

    private static final String TOKEN_KEY = "jwt_token";

    public ApiService(Context context) {
        this.api = ApiClient.getInstance();
        this.cache = new CacheManager(context);
    }

    public LoginResponse login(String email, String password) throws Exception {
        Call<LoginResponse> call = api.login(new LoginRequest(email, password));
        retrofit2.Response<LoginResponse> response = call.execute();

        if (!response.isSuccessful() || response.body() == null) {
            throw new Exception("Login falhou com código " + response.code());
        }

        LoginResponse loginData = response.body();

        // Guardar JWT
        cache.saveToCache(TOKEN_KEY, loginData.token);

        // Guardar user (opcional)
        cache.saveToCache("user_data", loginData.user);

        return loginData;
    }

    // ---------- BUSCAR USER COM TOKEN ----------
    public UserData fetchUser() throws Exception {
        String token = cache.getFromCache(TOKEN_KEY, String.class);

        if (token == null) {
            throw new Exception("Token não encontrado. User precisa de login.");
        }

        Call<UserData> call = api.getUser("Bearer " + token);
        retrofit2.Response<UserData> response = call.execute();

        if (!response.isSuccessful() || response.body() == null) {
            throw new Exception("Erro ao buscar user: " + response.code());
        }

        UserData user = response.body();

        // Opcional: atualizar cache
        cache.saveToCache("user_data", user);

        return user;
    }
}
