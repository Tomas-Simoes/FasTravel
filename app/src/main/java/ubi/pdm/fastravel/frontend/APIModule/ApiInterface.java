package ubi.pdm.fastravel.frontend.APIModule;


import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import ubi.pdm.fastravel.frontend.APIModule.RequestResponse.LoginRequest;
import ubi.pdm.fastravel.frontend.APIModule.RequestResponse.LoginResponse;
import ubi.pdm.fastravel.frontend.APIModule.RequestResponse.RegisterRequest;
import ubi.pdm.fastravel.frontend.DataPersistenceModule.User.UserData;

public interface ApiInterface {
    @POST("/register")
    Call<LoginResponse> register(@Body RegisterRequest request);
    @POST("/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @GET("/protected")
    Call<UserData> getUser(@Header("Authorization") String token);

}
