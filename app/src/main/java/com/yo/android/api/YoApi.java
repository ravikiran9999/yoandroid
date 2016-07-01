package com.yo.android.api;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by Ramesh on 1/7/16.
 */
public class YoApi {
    public static final String BASE_URL = "http://yoapp-dev.herokuapp.com/";


    public interface YoService {
        //http://yoapp-dev.herokuapp.com/api/otp.json?phone_no=123456789
        @FormUrlEncoded
        @POST("api/otp.json")
        Call<ResponseBody> loginUserAPI(@Field("phone_no") String phone_no);

        //http://yoapp-dev.herokuapp.com/oauth/token.json?client_id=83ade053e48c03568ab9f5c48884b8fb6fa0abb0ba5a0979da840417779e5c60
        // &client_secret=1c1a8a358e287759f647285c847f2b95976993651e09d2d4523331f1f271ad49
        // &grant_type=password&phone_no=123456789&otp=1234
        @FormUrlEncoded
        @POST("oauth/token.json")
        Call<ResponseBody> verifyOTP(
                @Field("client_id") String client_id,
                @Field("client_secret") String client_secret,
                @Field("grant_type") String grant_type,
                @Field("phone_no") String phone_no,
                @Field("otp") String otp
        );

    }

}
