package com.yo.android.api;

import com.google.gson.JsonElement;
import com.yo.android.model.Alerts;
import com.yo.android.model.Articles;
import com.yo.android.model.Categories;
import com.yo.android.model.ChatNotificationResponse;
import com.yo.android.model.Collections;
import com.yo.android.model.Contact;
import com.yo.android.model.FindPeople;
import com.yo.android.model.LandingArticles;
import com.yo.android.model.Lock;
import com.yo.android.model.MagazineArticles;
import com.yo.android.model.Notification;
import com.yo.android.model.OTPResponse;
import com.yo.android.model.OwnMagazine;
import com.yo.android.model.PackageDenomination;
import com.yo.android.model.PaymentHistoryItem;
import com.yo.android.model.Response;
import com.yo.android.model.Room;
import com.yo.android.model.Subscriber;
import com.yo.android.model.Topics;
import com.yo.android.model.TransferBalanceDenomination;
import com.yo.android.model.UpdateMagazine;
import com.yo.android.model.UserProfileInfo;
import com.yo.android.model.denominations.Denominations;
import com.yo.android.model.dialer.CallRateDetail;
import com.yo.android.model.wallet.Balance;

import org.json.JSONObject;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public class YoApi {

    public static final String ACCESS_TOKEN = "access_token";
    public static final String REFRESH_TOKEN = "refresh_token";


    public interface YoService {
        @FormUrlEncoded
        @POST("api/otp.json")
        Call<Response> loginUserAPI(@Field("phone_no") String phone_no, @Field("type") String type);

        @FormUrlEncoded
        @POST("oauth/token.json")
        Call<OTPResponse> verifyOTP(
                @Field("client_id") String client_id,
                @Field("client_secret") String client_secret,
                @Field("grant_type") String grant_type,
                @Field("phone_no") String phone_no,
                @Field("otp") String otp
        );

        @POST("api/user/nexge_subscribe.json")
        Call<Subscriber> subscribe(@Query("access_token") String access_token);

        @GET("api/tags.json")
        Call<List<Topics>> tagsAPI(@Query("access_token") String access_token);

        /*@GET("api/tags/current_user_articles.json")
        Call<List<Articles>> getUserArticlesAPI(@Query("access_token") String access_token);*/

        //For Search
        @FormUrlEncoded
        @POST("api/tags/get_articles.json")
        Call<List<Articles>> getArticlesAPI(@Field("access_token") String access_token, @Field("tag_ids[]") List<String> tag_ids);

        @FormUrlEncoded
        @POST("/api/articles/{article_id}/like.json")
        Call<ResponseBody> likeArticlesAPI(@Path("article_id") String article_id, @Field("access_token") String access_token);

        @FormUrlEncoded
        @POST("/api/articles/{article_id}/unlike.json")
        Call<ResponseBody> unlikeArticlesAPI(@Path("article_id") String article_id, @Field("access_token") String access_token);

        @FormUrlEncoded
        @POST("/api/magzines.json")
        Call<OwnMagazine> createMagazinesAPI(@Field("access_token") String access_token, @Field("magzine[name]") String magzine_name, @Field("magzine[description]") String magzine_description, @Field("magzine[privacy]") String magzine_privacy);

        @GET("api/collections.json")
        Call<List<Collections>> getCollectionsAPI(@Query("access_token") String access_token);

        @FormUrlEncoded
        @POST("/api/tags/add_tags.json")
        Call<ResponseBody> addTopicsAPI(@Field("access_token") String access_token, @Field("tag_ids[]") List<String> tag_ids, @Field("req_type") String req_type);

        @GET("api/magzines.json")
        Call<List<OwnMagazine>> getMagazinesAPI(@Query("access_token") String access_token);

        @FormUrlEncoded
        @POST("api/tags/remove_tags.json")
        Call<ResponseBody> removeTopicsAPI(@Field("access_token") String access_token, @Field("tag_ids[]") List<String> tag_ids);

        @GET("api/magzines/{magzine_id}.json")
        Call<MagazineArticles> getArticlesOfMagazineAPI(@Path("magzine_id") String magzine_id, @Query("access_token") String access_token);

        @FormUrlEncoded
        @PUT("api/magzines/{magzine_id}.json")
        Call<UpdateMagazine> updateMagazinesAPI(@Path("magzine_id") String magzine_id, @Field("access_token") String access_token, @Field("magzine[name]") String name, @Field("magzine[description]") String description, @Field("magzine[privacy]") String privacy);

        @DELETE("api/magzines/{magzine_id}.json")
        Call<ResponseBody> deleteMagazineAPI(@Path("magzine_id") String magzine_id, @Query("access_token") String access_token);

        @FormUrlEncoded
        @POST("api/user/contacts_sync_with_name.json")
        Call<JsonElement> syncContactsWithNameAPI(@Field("access_token") String access_token, @Field("user[contacts][]") List<JSONObject> user);

        @FormUrlEncoded
        @POST("api/user/firebase_token.json")
        Call<ResponseBody> firebaseAuthToken(@Field("access_token") String access_token);

        @GET("api/user/contacts_with_name.json")
        Call<List<Contact>> getContacts(@Query("access_token") String access_token);

        // get contacts on search in transfer balance
        @GET("api/user/contacts_with_name.json")
        Call<List<Contact>> getContactsOnSearch(@Query("access_token") String access_token, @Query("is_from_balance_transfer") boolean is_from_balance_transfer);

        @FormUrlEncoded
        @POST("/api/magzines/{magzine_id}.json")
        Call<Response> addArticleMagazineApi(@Field("access_token") String access_token, @Path("magzine_id") String magzine_id, @Field("article_ids[]") List<String> articles);

        @GET("api/user.json")
        Call<List<FindPeople>> getFindPeopleAPI(@Query("access_token") String access_token, @Query("page") int page, @Query("limit") int limit);

        @GET("api/user.json")
        Call<List<FindPeople>> getRepresentativePeopleAPI(@Query("access_token") String access_token, @Query("page") int page, @Query("limit") int limit, @Query("transferBalance") boolean transferBalance);

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
        Call<UserProfileInfo> updateProfile(
                @Path("user_id") String userId,
                @Header("Authorization") String authorization,
                @Part("user[description]") RequestBody descBody,
                @Part("user[first_name]") RequestBody firstName,
                @Part("user[phone_no]") RequestBody phoneNo,
                @Part("user[email]") RequestBody email,
                @Part("user[dob]") RequestBody dob,
                @Part("user[gender]") RequestBody gender,
                @Part("user[notification_alert]") RequestBody notificationsAlert,
                @Part("user[contacts_sync]") RequestBody syncContacts,
                @Part MultipartBody.Part file);

        @GET("api/articles.json")
        Call<List<Articles>> getWishListAPI(@Query("access_token") String access_token, @Query("liked") String liked);

        @FormUrlEncoded
        @POST("api/user/follow.json")
        Call<ResponseBody> followUsersAPI(@Field("access_token") String access_token, @Field("followed_id") String followed_id);

        @FormUrlEncoded
        @POST("api/user/unfollow.json")
        Call<ResponseBody> unfollowUsersAPI(@Field("access_token") String access_token, @Field("followed_id") String followed_id);

        @GET("api/user/followings.json")
        Call<List<FindPeople>> getFollowingsAPI(@Query("access_token") String access_token);

        @Multipart
        @POST("api/rooms.json")
        Call<Room> createGroupAPI(
                @Header("Authorization") String authorization,
                @Part("room[user_ids][]") List<String> user,
                @Part("room[group_name]") RequestBody groupName,
                @Part MultipartBody.Part file);


        @GET("api/articles.json")
        Call<List<Articles>> getOtherProfilesLikedArticlesAPI(@Query("access_token") String access_token, @Query("user_id") String user_id);

        @FormUrlEncoded
        @POST("api/rooms/get_room.json")
        Call<Room> getRoomAPI(@Field("access_token") String access_token, @Field("room[user_ids][]") List<String> user);

        @GET("api/rooms.json")
        Call<List<Room>> getAllRoomsAPI(@Query("access_token") String access_token);

        @FormUrlEncoded
        @POST("api/articles/{article_id}/follow.json")
        Call<ResponseBody> followArticleAPI(@Path("article_id") String article_id, @Field("access_token") String access_token);

        @FormUrlEncoded
        @POST("api/articles/{article_id}/unfollow.json")
        Call<ResponseBody> unfollowArticleAPI(@Path("article_id") String article_id, @Field("access_token") String access_token);

        @FormUrlEncoded
        @POST("api/magzines/{magzine_id}/follow.json")
        Call<ResponseBody> followMagazineAPI(@Path("magzine_id") String magzine_id, @Field("access_token") String access_token);

        @FormUrlEncoded
        @POST("api/magzines/{magzine_id}/unfollow.json")
        Call<ResponseBody> unfollowMagazineAPI(@Path("magzine_id") String magzine_id, @Field("access_token") String access_token);

        @FormUrlEncoded
        @POST("api/user/update_device_token.json")
        Call<ResponseBody> updateDeviceTokenAPI(@Field("access_token") String access_token, @Field("device_token") String device_token);

        @GET("api/user/payment_history.json")
        Call<List<PaymentHistoryItem>> getPaymentHistory(@Query("access_token") String access_token);

        @FormUrlEncoded
        @POST("api/user/add_balance.json")
        Call<Balance> addBalance(@Field("access_token") String access_token,
                                      @Field("subscriber_id") String subscriber_id,
                                      @Field("credit") String credit);

        @GET("api/user/notifications.json")
        Call<List<Notification>> getNotifications(@Query("access_token") String access_token, @Query("req_type") String type, @Query("notification_ids") String notificationIds);

        // need to combine with above api based on parameters : name, description and privacy
        @FormUrlEncoded
        @POST("api/articles.json")
        Call<Articles> postStoryMagazineAPI(@Field("access_token") String access_token, @Field("article[url]") String article_url, @Field("name") String name, @Field("description") String description, @Field("privacy") String privacy, @Field("magzine_id") String magzine_id, @Field("tag") String tag);

        @FormUrlEncoded
        @POST("api/magzines/remove_magzines.json")
        Call<ResponseBody> removeMagazinesAPI(@Field("access_token") String access_token, @Field("magzine_ids[]") List<String> magzine_ids);

        @GET("api/user/search.json")
        Call<List<FindPeople>> searchInFindPeople(@Query("access_token") String access_token, @Query("search_item") String search_item, @Query("page") int page, @Query("limit") int limit);

        @FormUrlEncoded
        @POST("/api/user/voucher_recharge.json")
        Call<Response> voucherRechargeAPI(@Field("access_token") String access_token, @Field("voucher_number") String voucher_number);

        @GET("/api/user/get_balance.json")
        Call<Balance> executeBalanceAction(@Query("access_token") String access_token);

        @GET("api/package_rates_lists.json")
        Call<List<CallRateDetail>> getCallsRatesListAPI(@Query("access_token") String access_token);

        @GET("/api/call_costs.json")
        Call<ResponseBody> getSpentDetailsHistory(@Query("access_token") String access_token);

        @GET("/api/user/app_users.json")
        Call<List<FindPeople>> getAppUsersAPI(@Query("access_token") String access_token);

        @FormUrlEncoded
        @POST("api/user/balance_transfer.json")
        Call<Response> balanceTransferAPI(@Field("access_token") String access_token, @Field("phone_no") String receiver_id, @Field("credit") String credit);

        @GET("api/user/receiver_search.json")
        Call<List<Contact>> searchInBalanceTransferContacts(@Query("access_token") String access_token, @Query("search_item") String search_item);

        @GET("api/user/{id}.json")
        Call<FindPeople> getUserInfoFromId(@Path("id") String id, @Query("access_token") String access_token);


        @GET("api/articles/{article_id}.json")
        Call<Articles> getArticleInfo(@Path("article_id") String article_id, @Query("access_token") String access_token);

        //TODO we can remove it
        // old denominations
        @GET("api/denominations.json")
        Call<List<Denominations>> getDenominations(@Query("access_token") String access_token);

        /*@GET("api/articles/articles_with_pagination.json")
        Call<List<Articles>> getArticlesWithPaginationAPI(@Query("access_token") String access_token, @Query("page") int page, @Query("limit") int limit);*/

        @FormUrlEncoded
        @POST("api/user/other_user_info.json")
        Call<YOUserInfo> getYOUserInfoBYYOName(@Field("nexge_subscriber_username") String vox_user_name);

        @FormUrlEncoded
        @POST("api/tags/{ID}/filtered_articles.json")
        Call<List<Articles>> getRemainingArticlesInTopicAPI(@Field("access_token") String access_token, @Path("ID") String ID, @Field("article_ids[]") List<String> article_ids);

        @FormUrlEncoded
        @POST("api/magzines/{ID}/filtered_articles.json")
        Call<MagazineArticles> getRemainingArticlesInMagAPI(@Field("access_token") String access_token, @Path("ID") String ID, @Field("article_ids[]") List<String> article_ids);

        @FormUrlEncoded
        @POST("api/articles/dashboard.json")
        Call<LandingArticles> getDashboardArticlesAPI(@Field("access_token") String access_token, @Field("read_article_ids[]") List<String> read_article_ids, @Field("unread_article_ids[]") List<String> unread_article_ids, @Field("autorenwal_subscription") boolean autorenwal_subscription, @Field("autorenewal") boolean autorenewal);

        @GET("api/categories.json")
        Call<List<Categories>> categoriesAPI(@Query("access_token") String access_token, @Query("is_for_tiles") boolean tiles);

        @FormUrlEncoded
        @POST("api/user/user_magzines_info.json")
        Call<Lock> lockAPI(@Field("access_token") String access_token);

        @GET("api/balance_transfer_denominations.json")
        Call<List<TransferBalanceDenomination>> transferBalanceDenominationAPI(@Query("access_token") String access_token);

        @GET("api/packages.json")
        Call<List<PackageDenomination>> giftPackageDenominationApi(@Query("access_token") String access_token);

        @FormUrlEncoded
        @POST("api/rooms/store_message.json")
        Call<ChatNotificationResponse> chatNotificationApi(@Field("access_token") String access_token, @Field("chat") String chatMessage);

        @FormUrlEncoded
        @POST("api/tags/random_tags.json")
        Call<List<Categories>> randomTagsAPI(@Field("access_token") String access_token);

        @GET("api/user/generate_alerts.json")
        Call<Alerts> sendAlerts(@Query("access_token") String access_token, @Query("title") String title, @Query("message") String reason, @Query("status") String status, @Query("status_code") int code);
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
