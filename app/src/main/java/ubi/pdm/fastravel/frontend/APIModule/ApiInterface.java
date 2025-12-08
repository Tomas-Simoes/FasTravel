package ubi.pdm.fastravel.frontend.APIModule;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import ubi.pdm.fastravel.frontend.APIModule.RequestResponse.HistoryRequest;
import ubi.pdm.fastravel.frontend.APIModule.RequestResponse.HistoryResponse;
import ubi.pdm.fastravel.frontend.APIModule.RequestResponse.LoginRequest;
import ubi.pdm.fastravel.frontend.APIModule.RequestResponse.LoginResponse;
import ubi.pdm.fastravel.frontend.APIModule.RequestResponse.RegisterRequest;
import ubi.pdm.fastravel.frontend.DataPersistenceModule.User.UserData;
import ubi.pdm.fastravel.frontend.DataPersistenceModule.User.UserHistory;

public interface ApiInterface {
    @POST("/users")
    Call<LoginResponse> register(@Body RegisterRequest request);

    @POST("/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @GET("/protected")
    Call<UserData> getUser(@Header("Authorization") String token);

    @POST("/histories")
    Call<HistoryResponse> createHistory(@Header("Authorization") String token, @Body HistoryRequest request);

    @GET("/histories")
    Call<List<UserHistory>> getHistories(@Header("Authorization") String token);
}