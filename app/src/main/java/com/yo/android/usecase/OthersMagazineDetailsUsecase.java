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
import com.yo.android.model.MagazineArticles;
import com.yo.android.ui.BaseActivity;
import com.yo.android.ui.OthersMagazinesDetailActivity;
import com.yo.android.util.Constants;

import javax.inject.Inject;
import javax.inject.Named;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by MYPC on 2/14/2018.
 */

public class OthersMagazineDetailsUsecase {

    @Inject
    YoApi.YoService yoService;
    @Inject
    @Named("login")
    PreferenceEndPoint preferenceEndPoint;
    @Inject
    protected ToastFactory mToastFactory;

    public void getArticlesOfMagazine(final OthersMagazinesDetailActivity othersMagazinesDetailActivity) {
        String accessToken = preferenceEndPoint.getStringPreference("access_token");
        yoService.getArticlesOfMagazineAPI(othersMagazinesDetailActivity.ownMagazine.getId(), accessToken).enqueue(new Callback<MagazineArticles>() {
            @Override
            public void onResponse(Call<MagazineArticles> call, final Response<MagazineArticles> response) {
                if (response.body() != null) {
                    final String id = response.body().getId();
                    if (response.body().getArticlesList() != null && response.body().getArticlesList().size() > 0) {
                        for (int i = 0; i < response.body().getArticlesList().size(); i++) {
                            othersMagazinesDetailActivity.flipContainer.setVisibility(View.VISIBLE);
                            if (othersMagazinesDetailActivity.noArticals != null) {
                                othersMagazinesDetailActivity.noArticals.setVisibility(View.GONE);
                            }
                            if (!"...".equalsIgnoreCase(response.body().getArticlesList().get(i).getSummary())) {
                                othersMagazinesDetailActivity.articlesList.add(response.body().getArticlesList().get(i));
                            }
                        }
                        othersMagazinesDetailActivity.myBaseAdapter.addItems(othersMagazinesDetailActivity.articlesList);
                    } else {
                        othersMagazinesDetailActivity.flipContainer.setVisibility(View.GONE);
                        if (othersMagazinesDetailActivity.noArticals != null) {
                            othersMagazinesDetailActivity.noArticals.setVisibility(View.VISIBLE);
                        }
                    }
                } else {
                    if (response.code() == 404) {
                        mToastFactory.showToast(othersMagazinesDetailActivity.getString(R.string.magazine_not_found));
                        othersMagazinesDetailActivity.isMagazineDeleted = true;
                    } else {
                        mToastFactory.showToast(othersMagazinesDetailActivity.getString(R.string.magazine_general_error));
                    }

                    othersMagazinesDetailActivity.flipContainer.setVisibility(View.GONE);
                    if (othersMagazinesDetailActivity.noArticals != null) {
                        othersMagazinesDetailActivity.noArticals.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onFailure(Call<MagazineArticles> call, Throwable t) {
                othersMagazinesDetailActivity.flipContainer.setVisibility(View.GONE);
                if (othersMagazinesDetailActivity.noArticals != null) {
                    othersMagazinesDetailActivity.noArticals.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    public void unlikeMyCollectionArticles(final Articles data, final Context context, final OthersMagazinesDetailActivity.MyBaseAdapter myBaseAdapter) {
        ((BaseActivity) context).showProgressDialog();
        String accessToken = preferenceEndPoint.getStringPreference("access_token");
        yoService.unlikeArticlesAPI(data.getId(), accessToken).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                handleLikeUnlikeMyCollectionSuccess(data, false, "false", "You have unliked the article ", context, myBaseAdapter);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                handleLikeUnlikeMyCollectionFailure(data, true, "true", "Error while unliking article ", context, myBaseAdapter);
            }
        });
    }

    private void handleLikeUnlikeMyCollectionFailure(Articles data, boolean isChecked, String setLiked, String toastMsg, Context context, OthersMagazinesDetailActivity.MyBaseAdapter myBaseAdapter) {
        ((BaseActivity) context).dismissProgressDialog();
        Toast.makeText(context, toastMsg + data.getTitle(), Toast.LENGTH_LONG).show();
        data.setIsChecked(isChecked);
        data.setLiked(setLiked);
        if (!((BaseActivity) context).hasDestroyed()) {
            myBaseAdapter.notifyDataSetChanged();
        }
    }

    public void likeMyCollectionArticles(final Articles data, final Context context, final OthersMagazinesDetailActivity.MyBaseAdapter myBaseAdapter) {
        ((BaseActivity) context).showProgressDialog();
        String accessToken = preferenceEndPoint.getStringPreference("access_token");
        yoService.likeArticlesAPI(data.getId(), accessToken).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                handleLikeUnlikeMyCollectionSuccess(data, true, "true", "You have liked the article ", context, myBaseAdapter);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                handleLikeUnlikeMyCollectionFailure(data, false, "false", "Error while liking article ", context, myBaseAdapter);
            }
        });
    }

    private void handleLikeUnlikeMyCollectionSuccess(final Articles data, boolean isChecked, String setLiked, String toastMsg, final Context context, final OthersMagazinesDetailActivity.MyBaseAdapter myBaseAdapter) {
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
