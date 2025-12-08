package ubi.pdm.fastravel.frontend.APIModule;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import retrofit2.Call;
import ubi.pdm.fastravel.frontend.APIModule.RequestResponse.HistoryRequest;
import ubi.pdm.fastravel.frontend.APIModule.RequestResponse.HistoryResponse;
import ubi.pdm.fastravel.frontend.APIModule.RequestResponse.LoginRequest;
import ubi.pdm.fastravel.frontend.APIModule.RequestResponse.LoginResponse;
import ubi.pdm.fastravel.frontend.DataPersistenceModule.CacheManager;
import ubi.pdm.fastravel.frontend.DataPersistenceModule.User.UserData;
import ubi.pdm.fastravel.frontend.APIModule.RequestResponse.RegisterRequest;
import ubi.pdm.fastravel.frontend.DataPersistenceModule.User.UserHistory;

public class ApiService {

    private final ApiInterface api;
    private final CacheManager cache;
    private static final String TOKEN_KEY = "jwt_token";
    private static final String HISTORY_KEY = "user_histories";

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

    // ==================== MÉTODOS DE HISTÓRICO ====================

    public UserHistory createHistory(String origin, String destiny, String travelDate) throws Exception {
        String token = cache.getFromCache(TOKEN_KEY, String.class);
        if (token == null) throw new Exception("Token não encontrado.");

        // Criar request com parâmetros wrapped em "history"
        HistoryRequest request = new HistoryRequest(origin, destiny, travelDate);

        Log.d("HISTORY_REQUEST", new Gson().toJson(request));

        Call<HistoryResponse> call = api.createHistory("Bearer " + token, request);
        retrofit2.Response<HistoryResponse> response = call.execute();

        if (!response.isSuccessful() || response.body() == null) {
            String errorBody = (response.errorBody() != null)
                    ? response.errorBody().string()
                    : "Erro desconhecido";

            Log.e("HISTORY_ERROR", "Status: " + response.code() + ", Body: " + errorBody);
            throw new Exception(errorBody);
        }

        // Rails retorna o history diretamente
        UserHistory history = response.body();

        // Limpar cache de históricos para forçar refresh na próxima leitura
        cache.clearCache(HISTORY_KEY);

        return history;
    }

    public List<UserHistory> fetchHistories() throws Exception {
        String token = cache.getFromCache(TOKEN_KEY, String.class);
        if (token == null) throw new Exception("Token não encontrado.");

        Call<List<UserHistory>> call = api.getHistories("Bearer " + token);
        retrofit2.Response<List<UserHistory>> response = call.execute();

        if (!response.isSuccessful() || response.body() == null) {
            String errorBody = (response.errorBody() != null)
                    ? response.errorBody().string()
                    : "Erro desconhecido";

            Log.e("HISTORY_FETCH_ERROR", "Status: " + response.code() + ", Body: " + errorBody);
            throw new Exception("Erro ao buscar histórico: " + response.code());
        }

        List<UserHistory> histories = response.body();
        cache.saveToCache(HISTORY_KEY, histories);

        Log.d("HISTORIES_FETCHED", "Total: " + histories.size());
        return histories;
    }
}