package ubi.pdm.fastravel.frontend.APIModule;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

import retrofit2.Call;
import ubi.pdm.fastravel.frontend.APIModule.RequestResponse.LoginRequest;
import ubi.pdm.fastravel.frontend.APIModule.RequestResponse.LoginResponse;
import ubi.pdm.fastravel.frontend.DataPersistenceModule.CacheManager;
import ubi.pdm.fastravel.frontend.DataPersistenceModule.User.UserData;
import ubi.pdm.fastravel.frontend.APIModule.RequestResponse.RegisterRequest;

import java.lang.Exception;

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
            throw new Exception("Login falhou: " + response.code());
        }

        LoginResponse loginData = response.body();

        // Guardar JWT e user na cache
        cache.saveToCache(TOKEN_KEY, loginData.token);
        cache.saveToCache("user_data", loginData.user);

        return loginData;
    }

    public LoginResponse register(String name, String email, String password) throws Exception {
        RegisterRequest req = new RegisterRequest(name, email, password);
        Call<LoginResponse> call = api.register(req);
        Log.d("REGISTER_JSON", new Gson().toJson(req));

        retrofit2.Response<LoginResponse> response = call.execute();

        if (!response.isSuccessful() || response.body() == null) {
            String errorBody = (response.errorBody() != null) ? response.errorBody().string() : "Erro desconhecido";
            throw new Exception(errorBody);
        }

        LoginResponse registerData = response.body();

        cache.saveToCache(TOKEN_KEY, registerData.token);
        cache.saveToCache("user_data", registerData.user);

        return registerData;
    }

    public UserData fetchUser() throws Exception {
        String token = cache.getFromCache(TOKEN_KEY, String.class);

        if (token == null) throw new Exception("Token não encontrado.");

        Call<UserData> call = api.getUser("Bearer " + token);
        retrofit2.Response<UserData> response = call.execute();

        if (!response.isSuccessful() || response.body() == null) {
            throw new Exception("Erro ao buscar user: " + response.code());
        }

        UserData user = response.body();
        cache.saveToCache("user_data", user);
        return user;
    }
}
