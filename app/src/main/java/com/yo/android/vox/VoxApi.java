package com.yo.android.vox;


import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**
 * Created by Ramesh on 1/7/16.
 */
public class VoxApi {

    public VoxService buildAdapter() {
        // Log the URL for debugging purposes
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient defaultHttpClient = new OkHttpClient
                .Builder()
                .addInterceptor(interceptor)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .client(defaultHttpClient)
                .baseUrl("http://209.239.120.239/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        return retrofit.create(VoxService.class);
    }

    public interface VoxService {
        @Multipart
        @POST("api.php")
        Call<ResponseBody> loginUserAPI(@Part("request") Task task);

        @Multipart
        @POST("api.php")
        Call<ResponseBody> sendOTP(@Part("request") OTPBody task);

    }
}
