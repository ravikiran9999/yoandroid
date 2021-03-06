package com.yo.android.usecase;

import android.content.Context;
import android.view.View;
import android.widget.Toast;

import com.orion.android.common.preferences.PreferenceEndPoint;
import com.orion.android.common.util.ToastFactory;
import com.yo.android.adapters.MagazineArticlesBaseAdapter;
import com.yo.android.api.YoApi;
import com.yo.android.model.Articles;
import com.yo.android.ui.BaseActivity;
import com.yo.android.ui.OtherProfilesLikedArticles;
import com.yo.android.util.Constants;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by MYPC on 2/14/2018.
 */

/**
 * This is the helper class that handles the service calls and other specific utility methods of the OtherProfilesLikedArticles class
 */
public class OthersProfileLikedArticlesUsecase {

    @Inject
    YoApi.YoService yoService;

    @Inject
    @Named("login")
    PreferenceEndPoint preferenceEndPoint;
    @Inject
    protected ToastFactory mToastFactory;

    /**
     * Gets the other Yo app user's liked articles
     *
     * @param userID The user id
     * @param otherProfilesLikedArticles The {@link OtherProfilesLikedArticles} object
     */
    public void loadLikedArticles(String userID, final OtherProfilesLikedArticles otherProfilesLikedArticles) {
        otherProfilesLikedArticles.articlesList.clear();
        otherProfilesLikedArticles.myBaseAdapter.addItems(otherProfilesLikedArticles.articlesList);
        otherProfilesLikedArticles.mProgress.setVisibility(View.VISIBLE);
        String accessToken = preferenceEndPoint.getStringPreference("access_token");
        yoService.getOtherProfilesLikedArticlesAPI(accessToken, userID).enqueue(new Callback<List<Articles>>() {
            @Override
            public void onResponse(Call<List<Articles>> call, Response<List<Articles>> response) {
                if (otherProfilesLikedArticles.mProgress != null) {
                    otherProfilesLikedArticles.mProgress.setVisibility(View.GONE);
                }
                otherProfilesLikedArticles.tvProgressText.setVisibility(View.GONE);
                if (!response.body().isEmpty()) {
                    try {
                        for (int i = 0; i < response.body().size(); i++) {
                            otherProfilesLikedArticles.flipContainer.setVisibility(View.VISIBLE);
                            if (otherProfilesLikedArticles.noArticals != null) {
                                otherProfilesLikedArticles.noArticals.setVisibility(View.GONE);
                            }
                            if (!"...".equalsIgnoreCase(response.body().get(i).getSummary())) {
                                otherProfilesLikedArticles.articlesList.add(response.body().get(i));
                            }
                        }
                        otherProfilesLikedArticles.myBaseAdapter.addItems(otherProfilesLikedArticles.articlesList);
                    } finally {
                        if (response != null && response.body() != null) {
                            try {
                                response.body().clear();
                                response = null;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } else {
                    otherProfilesLikedArticles.flipContainer.setVisibility(View.GONE);
                    if (otherProfilesLikedArticles.noArticals != null) {
                        otherProfilesLikedArticles.noArticals.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Articles>> call, Throwable t) {
                if (otherProfilesLikedArticles.mProgress != null) {
                    otherProfilesLikedArticles.mProgress.setVisibility(View.GONE);
                }
                otherProfilesLikedArticles.tvProgressText.setVisibility(View.GONE);
                otherProfilesLikedArticles.flipContainer.setVisibility(View.GONE);
                if (otherProfilesLikedArticles.noArticals != null) {
                    otherProfilesLikedArticles.noArticals.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    /**
     * Unlikes the other profiles liked articles
     * @param data The Articles object
     * @param context The context
     * @param myBaseAdapter The {@link com.yo.android.ui.OtherProfilesLikedArticles.MyBaseAdapter} object
     */
    public void unlikeOthersProfileLikedArticles(final Articles data, final Context context, final OtherProfilesLikedArticles.MyBaseAdapter myBaseAdapter) {
        ((BaseActivity) context).showProgressDialog();
        String accessToken = preferenceEndPoint.getStringPreference("access_token");
        yoService.unlikeArticlesAPI(data.getId(), accessToken).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                handleLikeUnlikeOtherLikedArticlesSuccess(data, false, "false", "You have unliked the article ", context, myBaseAdapter);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                handleLikeUnlikeOthersLikedArticlesFailure(data, true, "true", "Error while unliking article ", context, myBaseAdapter);
            }
        });
    }

    /**
     * Handles the like or unlike others articles failure response
     * @param data The Articles object
     * @param isChecked isChecked or not
     * @param setLiked isLiked or not
     * @param toastMsg The toast msg to be displayed
     * @param context The context
     * @param myBaseAdapter The {@link com.yo.android.ui.OtherProfilesLikedArticles.MyBaseAdapter} object
     */
    private void handleLikeUnlikeOthersLikedArticlesFailure(Articles data, boolean isChecked, String setLiked, String toastMsg, Context context, OtherProfilesLikedArticles.MyBaseAdapter myBaseAdapter) {
        ((BaseActivity) context).dismissProgressDialog();
        Toast.makeText(context, toastMsg + data.getTitle(), Toast.LENGTH_LONG).show();
        data.setIsChecked(isChecked);
        data.setLiked(setLiked);
        if (!((BaseActivity) context).hasDestroyed()) {
            myBaseAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Likes the others profile liked articles
     * @param data The Articles object
     * @param context The context
     * @param myBaseAdapter The {@link com.yo.android.ui.OtherProfilesLikedArticles.MyBaseAdapter} object
     */
    public void likeOthersProfileLikedArticles(final Articles data, final Context context, final OtherProfilesLikedArticles.MyBaseAdapter myBaseAdapter) {
        ((BaseActivity) context).showProgressDialog();
        String accessToken = preferenceEndPoint.getStringPreference("access_token");
        yoService.likeArticlesAPI(data.getId(), accessToken).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                handleLikeUnlikeOtherLikedArticlesSuccess(data, true, "true", "You have liked the article ", context, myBaseAdapter);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                handleLikeUnlikeOthersLikedArticlesFailure(data, false, "false", "Error while liking article ", context, myBaseAdapter);
            }
        });
    }

    /**
     * Handles the like or unlike of others liked articles success response
     * @param data The Articles object
     * @param isChecked isChecked or not
     * @param setLiked isLiked or not
     * @param toastMsg The toast msg to be displayed
     * @param context The context
     * @param myBaseAdapter The {@link com.yo.android.ui.OtherProfilesLikedArticles.MyBaseAdapter} object
     */
    private void handleLikeUnlikeOtherLikedArticlesSuccess(final Articles data, boolean isChecked, String setLiked, String toastMsg, final Context context, final OtherProfilesLikedArticles.MyBaseAdapter myBaseAdapter) {
        ((BaseActivity) context).dismissProgressDialog();
        data.setIsChecked(isChecked);
        data.setLiked(setLiked);
        if (MagazineArticlesBaseAdapter.reflectListener != null) {
            MagazineArticlesBaseAdapter.reflectListener.updateFollowOrLikesStatus(data, Constants.LIKE_EVENT);
        }
        if (MagazineArticlesBaseAdapter.mListener != null) {
            MagazineArticlesBaseAdapter.mListener.updateMagazineStatus(data, Constants.LIKE_EVENT);
        }
        if (!((BaseActivity) context).hasDestroyed()) {
            myBaseAdapter.notifyDataSetChanged();
        }
        mToastFactory.showToast(toastMsg + data.getTitle());
    }
}
