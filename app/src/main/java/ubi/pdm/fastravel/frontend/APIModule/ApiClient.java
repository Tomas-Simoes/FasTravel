package ubi.pdm.fastravel.frontend.APIModule;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static String BACKEND_URL = "test";
    private static ApiInterface instance;

    public static ApiInterface getInstance() {
        if(instance == null) {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(interceptor)
                    .build();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BACKEND_URL)  // muda isto
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();

            instance = retrofit.create(ApiInterface.class);
        }
        return instance;
    }

}
