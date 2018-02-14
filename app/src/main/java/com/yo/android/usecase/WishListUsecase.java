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
import com.yo.android.ui.OthersMagazinesDetailActivity;
import com.yo.android.ui.WishListActivity;
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

public class WishListUsecase {

    @Inject
    YoApi.YoService yoService;
    @Inject
    @Named("login")
    PreferenceEndPoint preferenceEndPoint;
    @Inject
    protected ToastFactory mToastFactory;

    public void getLikedArticles(final WishListActivity wishListActivity) {
        wishListActivity.showProgressDialog();
        String accessToken = preferenceEndPoint.getStringPreference("access_token");
        yoService.getWishListAPI(accessToken, "true").enqueue(new Callback<List<Articles>>() {
            @Override
            public void onResponse(Call<List<Articles>> call, Response<List<Articles>> response) {
                wishListActivity.dismissProgressDialog();
                if (response.body() != null && response.body().size() > 0) {
                    try {
                        for (int i = 0; i < response.body().size(); i++) {
                            wishListActivity.flipContainer.setVisibility(View.VISIBLE);
                            if ( wishListActivity.noArticals != null) {
                                wishListActivity.noArticals.setVisibility(View.GONE);
                                wishListActivity.llNoWishlist.setVisibility(View.GONE);
                            }
                            if (!"...".equalsIgnoreCase(response.body().get(i).getSummary())) {
                                wishListActivity.articlesList.add(response.body().get(i));
                            }
                        }
                        wishListActivity.myBaseAdapter.addItems( wishListActivity.articlesList);
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
                    wishListActivity.flipContainer.setVisibility(View.GONE);
                    if ( wishListActivity.noArticals != null) {
                        wishListActivity.noArticals.setVisibility(View.GONE);
                        wishListActivity.llNoWishlist.setVisibility(View.VISIBLE);
                    }
                }

            }

            @Override
            public void onFailure(Call<List<Articles>> call, Throwable t) {
                wishListActivity.dismissProgressDialog();
                wishListActivity.flipContainer.setVisibility(View.GONE);
                if ( wishListActivity.noArticals != null) {
                    wishListActivity.noArticals.setVisibility(View.GONE);
                    wishListActivity.llNoWishlist.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    public void unlikeMyCollectionArticles(final Articles data, final Context context, final WishListActivity.MyBaseAdapter myBaseAdapter) {
        ((BaseActivity) context).showProgressDialog();
        String accessToken = preferenceEndPoint.getStringPreference("access_token");
        yoService.unlikeArticlesAPI(data.getId(), accessToken).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                handleUnlikeMyCollectionSuccess(data, false, "false", "You have unliked the article ", context, myBaseAdapter);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                handleLikeUnlikeMyCollectionFailure(data, true, "true", "Error while unliking article ", context, myBaseAdapter);
            }
        });
    }

    private void handleLikeUnlikeMyCollectionFailure(Articles data, boolean isChecked, String setLiked, String toastMsg, Context context, WishListActivity.MyBaseAdapter myBaseAdapter) {
        ((BaseActivity) context).dismissProgressDialog();
        Toast.makeText(context, toastMsg + data.getTitle(), Toast.LENGTH_LONG).show();
        data.setIsChecked(isChecked);
        data.setLiked(setLiked);
        if (!((BaseActivity) context).hasDestroyed()) {
            myBaseAdapter.notifyDataSetChanged();
        }
    }

    public void likeMyCollectionArticles(final Articles data, final Context context, final WishListActivity.MyBaseAdapter myBaseAdapter) {
        ((BaseActivity) context).showProgressDialog();
        String accessToken = preferenceEndPoint.getStringPreference("access_token");
        yoService.likeArticlesAPI(data.getId(), accessToken).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                handleLikeMyCollectionSuccess(data, true, "true", "You have liked the article ", context, myBaseAdapter);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                handleLikeUnlikeMyCollectionFailure(data, false, "false", "Error while liking article ", context, myBaseAdapter);
            }
        });
    }

    private void handleUnlikeMyCollectionSuccess(final Articles data, boolean isChecked, String setLiked, String toastMsg, final Context context, final WishListActivity.MyBaseAdapter myBaseAdapter) {
        ((BaseActivity) context).dismissProgressDialog();
        data.setIsChecked(isChecked);
        data.setLiked(setLiked);
        MagazineArticlesBaseAdapter.initListener();
        if (MagazineArticlesBaseAdapter.reflectListener != null) {
            MagazineArticlesBaseAdapter.reflectListener.updateFollowOrLikesStatus(data, Constants.LIKE_EVENT);
        }

        if (OthersMagazinesDetailActivity.myBaseAdapter != null) {
            if (OthersMagazinesDetailActivity.myBaseAdapter.reflectListener != null) {
                OthersMagazinesDetailActivity.myBaseAdapter.reflectListener.updateFollowOrLikesStatus(data, Constants.LIKE_EVENT);
            }
        }
        if (!((BaseActivity) context).hasDestroyed()) {
            myBaseAdapter.notifyDataSetChanged();
        }
        mToastFactory.showToast(toastMsg + data.getTitle());
        ((WishListActivity)context).articlesList.clear();
        myBaseAdapter.addItems(((WishListActivity)context).articlesList);

        refreshWishList(((WishListActivity) context));
    }

    private void handleLikeMyCollectionSuccess(final Articles data, boolean isChecked, String setLiked, String toastMsg, final Context context, final WishListActivity.MyBaseAdapter myBaseAdapter) {
        ((BaseActivity) context).dismissProgressDialog();
        data.setIsChecked(isChecked);
        data.setLiked(setLiked);
        MagazineArticlesBaseAdapter.initListener();
        if (MagazineArticlesBaseAdapter.reflectListener != null) {
            MagazineArticlesBaseAdapter.reflectListener.updateFollowOrLikesStatus(data, Constants.LIKE_EVENT);
        }
        if (!((BaseActivity) context).hasDestroyed()) {
            myBaseAdapter.notifyDataSetChanged();
        }
        mToastFactory.showToast("You have liked the article " + data.getTitle());
    }

    /**
     * Refreshes the liked articles list
     */
    public void refreshWishList(final WishListActivity wishListActivity) {
        String accessToken = preferenceEndPoint.getStringPreference("access_token");
        yoService.getWishListAPI(accessToken, "true").enqueue(new Callback<List<Articles>>() {
            @Override
            public void onResponse(Call<List<Articles>> call, Response<List<Articles>> response) {
                if (response.body() != null && response.body().size() > 0) {
                    try {
                        for (int i = 0; i < response.body().size(); i++) {
                            if (!wishListActivity.hasDestroyed()) {
                                if (wishListActivity.noArticals != null) {
                                    wishListActivity.noArticals.setVisibility(View.GONE);
                                    wishListActivity.llNoWishlist.setVisibility(View.GONE);
                                    wishListActivity.flipContainer.setVisibility(View.VISIBLE);
                                }
                                wishListActivity.articlesList.add(response.body().get(i));
                            }
                        }
                        wishListActivity.myBaseAdapter.addItems(wishListActivity.articlesList);
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
                    if (!wishListActivity.hasDestroyed()) {
                        if (wishListActivity.noArticals != null) {
                            wishListActivity.noArticals.setVisibility(View.GONE);
                            wishListActivity.flipContainer.setVisibility(View.GONE);
                            wishListActivity.llNoWishlist.setVisibility(View.VISIBLE);
                        }
                    }
                }

            }

            @Override
            public void onFailure(Call<List<Articles>> call, Throwable t) {
                if (!wishListActivity.hasDestroyed()) {
                    if (wishListActivity.noArticals != null) {
                        wishListActivity.noArticals.setVisibility(View.GONE);
                        wishListActivity.flipContainer.setVisibility(View.GONE);
                        wishListActivity.llNoWishlist.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
    }
}
