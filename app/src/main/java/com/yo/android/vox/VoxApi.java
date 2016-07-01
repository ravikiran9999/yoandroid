package com.yo.android.vox;


import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**
 * Created by Ramesh on 1/7/16.
 */
public class VoxApi {
    public static final String BASE_URL = "http://209.239.120.239/api/";


    public interface VoxService {
        @Multipart
        @POST("api.php")
        Call<ResponseBody> loginUserAPI(@Part("request") Task task);

        @Multipart
        @POST("api.php")
        Call<ResponseBody> sendOTP(@Part("request") OTPBody task);

    }
}
