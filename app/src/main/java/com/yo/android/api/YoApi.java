package com.yo.android.api;

import com.yo.android.model.Articles;
import com.yo.android.model.Collections;
import com.yo.android.model.Contact;
import com.yo.android.model.FindPeople;
import com.yo.android.model.MagazineArticles;
import com.yo.android.model.OTPResponse;
import com.yo.android.model.OwnMagazine;
import com.yo.android.model.Response;
import com.yo.android.model.Topics;
import com.yo.android.model.UpdateMagazine;
import com.yo.android.model.UserProfileInfo;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by Ramesh on 1/7/16.
 */
public class YoApi {
    public static final String BASE_URL = "http://yoapp-dev.herokuapp.com/";
    public static final String CLIENT_ID = "83ade053e48c03568ab9f5c48884b8fb6fa0abb0ba5a0979da840417779e5c60";
    public static final String CLIENT_SECRET = "1c1a8a358e287759f647285c847f2b95976993651e09d2d4523331f1f271ad49";
    public static final String ACCESS_TOKEN = "access_token";
    public static final String REFRESH_TOKEN = "refresh_token";


    public interface YoService {
        //http://yoapp-dev.herokuapp.com/api/otp.json?phone_no=123456789
        @FormUrlEncoded
        @POST("api/otp.json")
        Call<ResponseBody> loginUserAPI(@Field("phone_no") String phone_no,@Field("type") String type);

        //http://yoapp-dev.herokuapp.com/oauth/token.json?client_id=83ade053e48c03568ab9f5c48884b8fb6fa0abb0ba5a0979da840417779e5c60
        // &client_secret=1c1a8a358e287759f647285c847f2b95976993651e09d2d4523331f1f271ad49
        // &grant_type=password&phone_no=123456789&otp=1234
        @FormUrlEncoded
        @POST("oauth/token.json")
        Call<OTPResponse> verifyOTP(
                @Field("client_id") String client_id,
                @Field("client_secret") String client_secret,
                @Field("grant_type") String grant_type,
                @Field("phone_no") String phone_no,
                @Field("otp") String otp
        );

        @GET("api/tags.json")
        Call<List<Topics>> tagsAPI(@Query("access_token") String access_token);

        @FormUrlEncoded
        @POST("api/tags/get_articles.json")
        Call<List<Articles>> getArticlesAPI(@Field("access_token") String access_token, @Field("tag_ids[]") String tag_ids);

        @FormUrlEncoded
        @POST("/api/articles/{article_id}/like.json")
        Call<ResponseBody> likeArticlesAPI(@Path("article_id") String article_id, @Field("access_token") String access_token);

        @FormUrlEncoded
        @POST("/api/articles/{article_id}/unlike.json")
        Call<ResponseBody> unlikeArticlesAPI(@Path("article_id") String article_id, @Field("access_token") String access_token);

        @FormUrlEncoded
        @POST("/api/magzines.json")
        Call<OwnMagazine> createMagazinesAPI(@Field("access_token") String access_token, @Field("magzine[name]") String magzine_name, @Field("magzine[description]") String magzine_description, @Field("magzine[privacy]") String magzine_privacy);

        @GET("api/articles.json")
        Call<List<Articles>> getAllArticlesAPI(@Query("access_token") String access_token);

        @GET("api/collections.json")
        Call<List<Collections>> getCollectionsAPI(@Query("access_token") String access_token);

        @FormUrlEncoded
        @POST("/api/tags/add_tags.json")
        Call<ResponseBody> addTopicsAPI(@Field("access_token") String access_token, @Field("tag_ids[]") List<String> tag_ids);

        @GET("api/magzines.json")
        Call<List<OwnMagazine>> getMagazinesAPI(@Query("access_token") String access_token);

        @FormUrlEncoded
        @POST("api/tags/remove_tags.json")
        Call<ResponseBody> removeTopicsAPI(@Field("access_token") String access_token, @Field("tag_ids[]") List<String> tag_ids);

        @FormUrlEncoded
        @POST("/api/articles.json")
        Call<Articles> addStoryMagazineAPI(@Field("access_token") String access_token, @Field("article[url]") String article_url, @Field("magzine_id") String magzine_id);

        @GET("api/magzines/{magzine_id}.json")
        Call<MagazineArticles> getArticlesOfMagazineAPI(@Path("magzine_id") String magzine_id, @Query("access_token") String access_token);

        @FormUrlEncoded
        @PUT("api/magzines/{magzine_id}.json")
        Call<UpdateMagazine> updateMagazinesAPI(@Path("magzine_id") String magzine_id, @Field("access_token") String access_token, @Field("magzine[name]") String name, @Field("magzine[description]") String description, @Field("magzine[privacy]") String privacy);

        @DELETE("api/magzines/{magzine_id}.json")
        Call<ResponseBody> deleteMagazineAPI(@Path("magzine_id") String magzine_id, @Query("access_token") String access_token);

        @FormUrlEncoded
        @POST("api/user/contacts_sync.json")
        Call<List<Contact>> syncContactsAPI(@Field("access_token") String access_token, @Field("user[contacts][]") List<String> user);

        @GET("/api/user/contacts.json")
        Call<List<Contact>> getContacts(@Query("access_token") String access_token);

        @FormUrlEncoded
        @PUT("/api/magzines/{magzine_id}.json")
        Call<Response> addArticleMagazineApi(@Field("access_token") String access_token, @Path("magzine_id") String magzine_id, @Field("article_ids[]") List<String> articles);

        @GET("api/user.json")
        Call<List<FindPeople>> getFindPeopleAPI(@Query("access_token") String access_token, @Query("page") int page, @Query("limit") int limit);

        @GET("api/user/followers.json")
        Call<List<FindPeople>> getFollowersAPI(@Query("access_token") String access_token);

        @GET("api/magzines.json")
        Call<List<OwnMagazine>> getOtherProfilesMagazinesAPI(@Query("access_token") String access_token, @Query("user_id") String userId);

        @GET("api/user/followers.json")
        Call<List<FindPeople>> getOtherProfilesFollowersAPI(@Query("access_token") String access_token, @Query("user_id") String userId);

        @GET("api/user/info.json")
        Call<UserProfileInfo> getUserInfo(@Query("access_token") String access_token);

        @Multipart
        @PUT("/api/user/{user_id}.json")
        Call<UserProfileInfo> updateProfile(@Path("user_id") String userId,
                                            @Part MultipartBody.Part file
        );
        @GET("api/articles.json")
        Call<List<Articles>> getWishListAPI(@Query("access_token") String access_token, @Query("liked") String liked);
    }

    public interface YoRefreshTokenService {
        @FormUrlEncoded
        @POST("oauth/token.json")
        Call<OTPResponse> refreshToken(
                @Field("client_id") String client_id,
                @Field("client_secret") String client_secret,
                @Field("grant_type") String grant_type,
                @Field("refresh_token") String refresh_token
        );

    }

}
