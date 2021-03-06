package com.yo.android.usecase;

import android.content.Context;
import android.view.View;
import android.widget.Toast;

import com.orion.android.common.preferences.PreferenceEndPoint;
import com.orion.android.common.util.ToastFactory;
import com.yo.android.R;
import com.yo.android.adapters.MagazineArticlesBaseAdapter;
import com.yo.android.api.YoApi;
import com.yo.android.model.Articles;
import com.yo.android.ui.BaseActivity;
import com.yo.android.ui.TopicsDetailActivity;
import com.yo.android.util.Constants;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
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
 * This is the helper class that handles the service calls and other specific utility methods of the TopicsDetailActivity class
 */
public class TopicDetailsUsecase {

    @Inject
    YoApi.YoService yoService;
    @Inject
    @Named("login")
    PreferenceEndPoint preferenceEndPoint;
    @Inject
    protected ToastFactory mToastFactory;

    /**
     * Gets the articles of a topic
     * @param topicsDetailActivity The {@link TopicsDetailActivity} object
     */
    public void getArticlesOfTopic(final TopicsDetailActivity topicsDetailActivity) {
        String accessToken = preferenceEndPoint.getStringPreference("access_token");
        List<String> tagIds = new ArrayList<>();
        tagIds.add(topicsDetailActivity.topic.getTopicId());
        topicsDetailActivity.showProgressDialog();
        yoService.getArticlesAPI(accessToken, tagIds).enqueue(new Callback<List<Articles>>() {
            @Override
            public void onResponse(Call<List<Articles>> call, Response<List<Articles>> response) {
                topicsDetailActivity.dismissProgressDialog();
                if (response.body() != null && response.body().size() > 0) {
                    for (int i = 0; i < response.body().size(); i++) {
                        if (!"...".equalsIgnoreCase(response.body().get(i).getSummary())) {
                            topicsDetailActivity.articlesList.add(response.body().get(i));
                        }
                    }
                    topicsDetailActivity.myBaseAdapter.addItems(topicsDetailActivity.articlesList);
                    if (topicsDetailActivity.articlesList.size() == 0) {
                        topicsDetailActivity.tvNoArticles.setText(R.string.no_articles);
                        topicsDetailActivity.tvNoArticles.setVisibility(View.VISIBLE);
                        topicsDetailActivity.flipView.setVisibility(View.GONE);
                    } else {
                        topicsDetailActivity.tvNoArticles.setVisibility(View.GONE);
                        topicsDetailActivity.flipView.setVisibility(View.VISIBLE);
                    }
                } else {
                    mToastFactory.showToast("No Articles");
                }
            }

            @Override
            public void onFailure(Call<List<Articles>> call, Throwable t) {
                topicsDetailActivity.dismissProgressDialog();
                failureError(t, topicsDetailActivity);

            }
        });
    }

    /**
     * Handles the failure of getting the articles of the topic
     * @param t The {@link Throwable } object
     * @param topicsDetailActivity The {@link TopicsDetailActivity} object
     */
    private void failureError(Throwable t, TopicsDetailActivity topicsDetailActivity) {
        if(t instanceof SocketTimeoutException) {
            topicsDetailActivity.tvNoArticles.setText(R.string.socket_time_out);
        } else {
            topicsDetailActivity.tvNoArticles.setText(R.string.error_retrieving_articles);
        }
        topicsDetailActivity.tvNoArticles.setVisibility(View.VISIBLE);
        topicsDetailActivity.flipView.setVisibility(View.GONE);
    }

    /**
     * Unlikes the article of a topic
     * @param data The Articles object
     * @param context The Context
     * @param myBaseAdapter The {@link com.yo.android.ui.TopicsDetailActivity.MyBaseAdapter} object
     */
    public void unlikeTopicArticles(final Articles data, final Context context, final TopicsDetailActivity.MyBaseAdapter myBaseAdapter) {
        ((BaseActivity) context).showProgressDialog();
        String accessToken = preferenceEndPoint.getStringPreference("access_token");
        yoService.unlikeArticlesAPI(data.getId(), accessToken).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                handleLikeUnlikeMyCollectionSuccess(data, false, "false", "You have unliked the article ", context, myBaseAdapter);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                handleLikeUnlikeTopicArticleFailure(data, true, "true", "Error while unliking article ", context, myBaseAdapter);
            }
        });
    }

    /**
     * Handles the like or unlike topic article failure response
     * @param data The Articles object
     * @param isChecked isChecked or not
     * @param setLiked isLiked or not
     * @param toastMsg The toast msg to be displayed
     * @param context The Context
     * @param myBaseAdapter The {@link com.yo.android.ui.TopicsDetailActivity.MyBaseAdapter} object
     */
    private void handleLikeUnlikeTopicArticleFailure(Articles data, boolean isChecked, String setLiked, String toastMsg, Context context, TopicsDetailActivity.MyBaseAdapter myBaseAdapter) {
        ((BaseActivity) context).dismissProgressDialog();
        Toast.makeText(context, toastMsg + data.getTitle(), Toast.LENGTH_LONG).show();
        data.setIsChecked(isChecked);
        data.setLiked(setLiked);
        if (!((BaseActivity) context).hasDestroyed()) {
            myBaseAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Likes the article of the topic
     * @param data The Articles object
     * @param context The Context
     * @param myBaseAdapter The {@link com.yo.android.ui.TopicsDetailActivity.MyBaseAdapter} object
     */
    public void likeTopicArticles(final Articles data, final Context context, final TopicsDetailActivity.MyBaseAdapter myBaseAdapter) {
        ((BaseActivity) context).showProgressDialog();
        String accessToken = preferenceEndPoint.getStringPreference("access_token");
        yoService.likeArticlesAPI(data.getId(), accessToken).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                handleLikeUnlikeMyCollectionSuccess(data, true, "true", "You have liked the article ", context, myBaseAdapter);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                handleLikeUnlikeTopicArticleFailure(data, false, "false", "Error while liking article ", context, myBaseAdapter);
            }
        });
    }

    /**
     * Handles like or unlike of the article of a topic success response
     * @param data The Articles object
     * @param isChecked isChecked or not
     * @param setLiked isLiked or not
     * @param toastMsg The toast msg to be displayed
     * @param context The Context
     * @param myBaseAdapter The {@link com.yo.android.ui.TopicsDetailActivity.MyBaseAdapter} object
     */
    private void handleLikeUnlikeMyCollectionSuccess(final Articles data, boolean isChecked, String setLiked, String toastMsg, final Context context, final TopicsDetailActivity.MyBaseAdapter myBaseAdapter) {
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
