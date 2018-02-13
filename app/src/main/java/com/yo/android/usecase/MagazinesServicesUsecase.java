package com.yo.android.usecase;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.aphidmobile.utils.AphidLog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.orion.android.common.preferences.PreferenceEndPoint;
import com.orion.android.common.util.ConnectivityHelper;
import com.orion.android.common.util.ToastFactory;
import com.yo.android.R;
import com.yo.android.adapters.MagazineArticlesBaseAdapter;
import com.yo.android.api.YoApi;
import com.yo.android.flip.MagazineArticleDetailsActivity;
import com.yo.android.flip.MagazineFlipArticlesFragment;
import com.yo.android.helpers.MagazinePreferenceEndPoint;
import com.yo.android.model.Articles;
import com.yo.android.ui.BaseActivity;
import com.yo.android.ui.BitmapScaler;
import com.yo.android.ui.CreateMagazineActivity;
import com.yo.android.ui.DeviceDimensionsHelper;
import com.yo.android.ui.MyCollectionDetails;
import com.yo.android.ui.OtherProfilesLikedArticles;
import com.yo.android.ui.TopicsDetailActivity;
import com.yo.android.util.ArticlesComparator;
import com.yo.android.util.Constants;
import com.yo.android.util.MagazineDashboardHelper;
import com.yo.android.util.Util;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by ec on 9/2/18.
 */

public class MagazinesServicesUsecase {

    public static final String TAG = MagazinesServicesUsecase.class.getSimpleName();
    @Inject
    YoApi.YoService yoService;

    @Inject
    @Named("login")
    PreferenceEndPoint preferenceEndPoint;
    @Inject
    ConnectivityHelper mHelper;
    private static int articleCountThreshold = 2000;

    public void likeArticles(final MagazineArticlesBaseAdapter magazineArticlesBaseAdapter, final Context context, final Articles data, final ToastFactory mToastFactory) {
        ((BaseActivity) context).showProgressDialog();
        String accessToken = preferenceEndPoint.getStringPreference("access_token");
        yoService.likeArticlesAPI(data.getId(), accessToken).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                handleLikeUnlikeSuccess(magazineArticlesBaseAdapter, context, data, mToastFactory, true, "true","You have liked the article ");
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                handleLikeUnlikeFailure(magazineArticlesBaseAdapter, context, data, mToastFactory, false, "false","Error while liking article ");
            }
        });
    }

    /**
     * Gets the cached magazines list
     *
     * @return The list of articles
     */
    public List<Articles> getCachedMagazinesList(Context context) {
        Type type1 = new TypeToken<List<Articles>>() {
        }.getType();
        String userId = preferenceEndPoint.getStringPreference(Constants.USER_ID);
        List<Articles> cachedMagazinesList = new ArrayList<>();
        if (context != null) {
            String sharedFollowedCachedMagazines = MagazinePreferenceEndPoint.getInstance().getPref(context, userId).getString("followed_cached_magazines", "");
            String sharedRandomCachedMagazines = MagazinePreferenceEndPoint.getInstance().getPref(context, userId).getString("random_cached_magazines", "");

            if (!TextUtils.isEmpty(sharedFollowedCachedMagazines)) {
                String cachedMagazines = sharedFollowedCachedMagazines;
                List<Articles> cachedFollowedMagazinesList = new Gson().fromJson(cachedMagazines, type1);
                cachedMagazinesList.addAll(cachedFollowedMagazinesList);
            }
            if (!TextUtils.isEmpty(sharedRandomCachedMagazines)) {
                String cachedMagazines = sharedRandomCachedMagazines;
                List<Articles> cachedRandomMagazinesList = new Gson().fromJson(cachedMagazines, type1);
                cachedMagazinesList.addAll(cachedRandomMagazinesList);
            }
        }

        return cachedMagazinesList;
    }

    /**
     * Saves the cached magazines list
     *
     * @param cachedMagazinesList The list of articles to be cached
     */
    public void saveCachedMagazinesList(List<Articles> cachedMagazinesList, Context context) {
        List<Articles> followedTopicArticles = new ArrayList<>();
        List<Articles> randomTopicArticles = new ArrayList<>();
        for (Articles articles : cachedMagazinesList) {
            if ("true".equals(articles.getTopicFollowing())) {
                followedTopicArticles.add(articles);
            } else {
                randomTopicArticles.add(articles);
            }
        }

        String userId = preferenceEndPoint.getStringPreference(Constants.USER_ID);
        if (context != null) {
            SharedPreferences.Editor editor = MagazinePreferenceEndPoint.getInstance().get(context, userId);
            editor.putString("followed_cached_magazines", new Gson().toJson(new LinkedHashSet<Articles>(followedTopicArticles)));
            editor.putString("random_cached_magazines", new Gson().toJson(new LinkedHashSet<Articles>(randomTopicArticles)));
            editor.commit();
        }
    }

    public void unlikeArticles(final MagazineArticlesBaseAdapter magazineArticlesBaseAdapter, final Context context, final Articles data, final ToastFactory mToastFactory) {
        ((BaseActivity) context).showProgressDialog();
        String accessToken = preferenceEndPoint.getStringPreference("access_token");
        yoService.unlikeArticlesAPI(data.getId(), accessToken).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                handleLikeUnlikeSuccess(magazineArticlesBaseAdapter, context, data, mToastFactory, false, "false","You have un-liked the article ");
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                handleLikeUnlikeFailure(magazineArticlesBaseAdapter, context, data, mToastFactory, true, "true","Error while un liking article ");
            }
        });
    }

    private void handleLikeUnlikeSuccess(final MagazineArticlesBaseAdapter magazineArticlesBaseAdapter, final Context context, final Articles data, final ToastFactory mToastFactory, boolean isChecked, String setLiked, String toastMsg) {
        ((BaseActivity) context).dismissProgressDialog();
        data.setIsChecked(isChecked);
        data.setLiked(setLiked);
        if (OtherProfilesLikedArticles.getListener() != null) {
            OtherProfilesLikedArticles.getListener().updateOtherPeopleStatus(data, Constants.LIKE_EVENT);
        }
        if (!((BaseActivity) context).hasDestroyed()) {
            magazineArticlesBaseAdapter.notifyDataSetChanged();
        }

        mToastFactory.showToast(toastMsg + data.getTitle());

        List<Articles> cachedMagazinesList = getCachedMagazinesList(context);
        if (cachedMagazinesList != null) {
            List<Articles> tempList = cachedMagazinesList;
            for (int i = 0; i < cachedMagazinesList.size(); i++) {
                if (data.getId().equals(tempList.get(i).getId())) {
                    tempList.get(i).setLiked(setLiked);
                }
            }

            cachedMagazinesList = tempList;

            saveCachedMagazinesList(cachedMagazinesList, context);
        }

    }

    private void handleLikeUnlikeFailure(final MagazineArticlesBaseAdapter magazineArticlesBaseAdapter, final Context context, final Articles data, final ToastFactory mToastFactory, boolean isChecked, String setLiked, String toastMsg) {
        ((BaseActivity) context).dismissProgressDialog();
        mToastFactory.showToast(toastMsg + data.getTitle());
        data.setIsChecked(isChecked);
        data.setLiked(setLiked);
        if (!((BaseActivity) context).hasDestroyed()) {
            magazineArticlesBaseAdapter.notifyDataSetChanged();
        }

        List<Articles> cachedMagazinesList = getCachedMagazinesList(context);
        if (cachedMagazinesList != null) {
            List<Articles> tempList = cachedMagazinesList;
            for (int i = 0; i < cachedMagazinesList.size(); i++) {
                if (data.getId().equals(tempList.get(i).getId())) {
                    tempList.get(i).setLiked(setLiked);
                }
            }

            cachedMagazinesList = tempList;

            saveCachedMagazinesList(cachedMagazinesList, context);
        }
    }

    public void handleImageLoading(MagazineArticlesBaseAdapter.ViewHolder holder, Context context, final Articles data, ImageView photoView) {
        if (!((BaseActivity) context).hasDestroyed()) {
            final TextView fullImageTitle = holder.fullImageTitle;
            final TextView articleTitle = holder.articleTitle;
            final ImageView blackMask = holder.blackMask;
            final RelativeLayout rlFullImageOptions = holder.rlFullImageOptions;
            final TextView textView = holder.articleShortDesc;

            Glide.with(context)
                    .load(data.getS3_image_filename())
                    .placeholder(R.drawable.magazine_backdrop)
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .dontAnimate()
                    .into(photoView);


            /*if (articleTitle != null) {
                ViewTreeObserver vto1 = articleTitle.getViewTreeObserver();
                vto1.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    private int maxLines = -1;

                    @Override
                    public void onGlobalLayout() {
                        if (maxLines < 0 && articleTitle.getHeight() > 0 && articleTitle.getLineHeight() > 0) {
                            //Log.d("BaseAdapter", "Max lines inside if" + maxLines);
                            int height = articleTitle.getHeight();
                            int lineHeight = articleTitle.getLineHeight();
                            maxLines = height / lineHeight;
                            articleTitle.setMaxLines(maxLines);
                            articleTitle.setEllipsize(TextUtils.TruncateAt.END);
                            // Re-assign text to ensure ellipsize is performed correctly.
                            // Ellipsizing the article title once the article image is loaded
                            articleTitle.setText(AphidLog.format("%s", data.getTitle()));
                        } else if (maxLines == -1 && articleTitle.getHeight() > 0) {
                            //Log.d("BaseAdapter", "Max lines inside else if" + maxLines);
                            articleTitle.setMaxLines(1);
                            articleTitle.setEllipsize(TextUtils.TruncateAt.END);
                            // Re-assign text to ensure ellipsize is performed correctly.
                            // Ellipsizing the article title once the article image is loaded
                            articleTitle.setText(AphidLog.format("%s", data.getTitle()));
                        } else if (maxLines == -1 && articleTitle.getHeight() == 0) { // Full screen article
                            // Log.d("BaseAdapter", "Full screen image after options cut or not shown");
                            if (fullImageTitle != null && articleTitle != null && blackMask != null && rlFullImageOptions != null) {
                                fullImageTitle.setVisibility(View.VISIBLE);
                                fullImageTitle.setText(articleTitle.getText().toString());
                                blackMask.setVisibility(View.VISIBLE);
                                rlFullImageOptions.setVisibility(View.VISIBLE);

                            }
                        }
                    }
                });


            }*/

            if(articleTitle != null) {
                articleTitle.setText(AphidLog.format("%s", data.getTitle()));
            }

            if (textView != null) {
                textView.setText(Html.fromHtml(data.getSummary()));
            }

        }
    }

    public void loadImageFromS3(final Context context, final Articles data, final ImageView photoView) {
        if (!((BaseActivity) context).hasDestroyed()) {

            Glide.with(context)
                    .load(data.getS3_image_filename())
                    .asBitmap()
                    .placeholder(R.drawable.magazine_backdrop)
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .dontAnimate()
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                            int screenWidth = DeviceDimensionsHelper.getDisplayWidth(context);
                            Bitmap bmp = null;
                            if (resource != null) {
                                try {
                                    bmp = BitmapScaler.scaleToFitWidth(resource, screenWidth);
                                    Glide.clear(photoView);
                                    Glide.with(context)
                                            .load(data.getS3_image_filename())
                                            .override(bmp.getWidth(), bmp.getHeight())
                                            .placeholder(R.drawable.magazine_backdrop)
                                            .crossFade()
                                            .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                                            .dontAnimate()
                                            .into(photoView);
                                } finally {
                                    if (bmp != null) {
                                        bmp.recycle();
                                        bmp = null;
                                    }
                                }
                            }
                        }
                    });
        }
    }

    public void navigateToArticleWebView(Context context, Articles data, int position) {
        Intent intent = new Intent(context, MagazineArticleDetailsActivity.class);
        intent.putExtra("Title", data.getTitle());
        intent.putExtra("Image", data.getUrl());
        intent.putExtra("Article", data);
        intent.putExtra("Position", position);
        if (context instanceof Activity) {
            ((Activity) context).startActivityForResult(intent, 500);
        }
    }

    public void onShareClick(View v, Articles data) {
        if (data.getImage_filename() != null) {
            new Util.ImageLoaderTask(v, data).execute(data.getImage_filename());
        } else {
            String summary = Html.fromHtml(data.getSummary()).toString();
            Util.shareNewIntent(v, data.getGenerated_url(), "Article: " + data.getTitle(), summary, null);
        }
    }

    public void onAddClick(Context context, Articles data) {
        Intent intent = new Intent(context, CreateMagazineActivity.class);
        intent.putExtra(Constants.MAGAZINE_ADD_ARTICLE_ID, data.getId());
        if (context instanceof Activity) {
            ((Activity) context).startActivityForResult(intent, Constants.ADD_ARTICLES_TO_MAGAZINE);
        }
    }

    public void navigateToTopicDetails(MagazineFlipArticlesFragment magazineFlipArticlesFragment, Context context,Articles data, int position) {
        Intent intent = new Intent(context, TopicsDetailActivity.class);
        intent.putExtra("Topic", data);
        intent.putExtra("Position", position);
        magazineFlipArticlesFragment.startActivityForResult(intent, 60);
    }

    public void navigateFromLeftRightToTopicsDetail(MagazineFlipArticlesFragment magazineFlipArticlesFragment, Context context, Articles data, int position, String placement) {
        Intent intent = new Intent(context, TopicsDetailActivity.class);
        intent.putExtra("Topic", data);
        intent.putExtra("Position", position);
        intent.putExtra("ArticlePlacement", placement);
        magazineFlipArticlesFragment.startActivityForResult(intent, 60);
    }

    public void navigateFromLeftRightArticleToWebView(MagazineFlipArticlesFragment magazineFlipArticlesFragment, Context context, Articles data, int position, String placement) {
        Intent intent = new Intent(context, MagazineArticleDetailsActivity.class);
        intent.putExtra("Title", data.getTitle());
        intent.putExtra("Image", data.getUrl());
        intent.putExtra("Article", data);
        intent.putExtra("Position", position);
        intent.putExtra("ArticlePlacement", placement);
        magazineFlipArticlesFragment.startActivityForResult(intent, 500);
    }


    /**
     * Updates the Follow and Like status of the articles
     * @param data The articles object
     * @param type Whether it is Follow or Like
     * @param allArticles
     * @param context
     * @param magazineArticlesBaseAdapter
     */
    public void autoReflectStatus(Articles data, String type, List<Articles> allArticles, Context context, MagazineArticlesBaseAdapter magazineArticlesBaseAdapter) {
        if (data != null) {

            if (Constants.FOLLOW_EVENT.equals(type)) {
                for (Articles article : allArticles) {
                    if (data.getId() != null && data.getId().equals(article.getId())) {
                        article.setIsFollowing(data.getIsFollowing());
                        article.setIsFollow(data.isFollow());
                        if (!((BaseActivity) context).hasDestroyed()) {
                            magazineArticlesBaseAdapter.notifyDataSetChanged();
                        }
                        break;
                    }
                }
            } else {
                allArticles = magazineArticlesBaseAdapter.getAllItems();
                for (Articles article : allArticles) {
                    if (data.getId() != null && data.getId().equals(article.getId())) {
                        article.setLiked(data.getLiked());
                        article.setIsChecked(data.isChecked());
                        if (!((BaseActivity) context).hasDestroyed()) {
                            magazineArticlesBaseAdapter.notifyDataSetChanged();
                        }

                        List<Articles> cachedMagazinesList = getCachedMagazinesList(context);

                        if (cachedMagazinesList != null) {
                            List<Articles> tempList = cachedMagazinesList;
                            for (int i = 0; i < cachedMagazinesList.size(); i++) {
                                if (data.getId().equals(tempList.get(i).getId())) {
                                    tempList.get(i).setLiked(data.getLiked());
                                }
                            }
                            cachedMagazinesList = tempList;

                            saveCachedMagazinesList(cachedMagazinesList, context);
                        }
                        break;
                    }

                }
            }
        }
    }

    /**
     * Loading articles
     *
     * @param tagIds The topic ids
     */
    public void loadArticles(List<String> tagIds, boolean renewal, Context context, MagazineFlipArticlesFragment magazineFlipArticlesFragment) {

        if (!mHelper.isConnected()) {

            Type type1 = new TypeToken<List<Articles>>() {
            }.getType();
            String userId = preferenceEndPoint.getStringPreference(Constants.USER_ID);
            if (context != null) {
                String sharedFollowedCachedMagazines = MagazinePreferenceEndPoint.getInstance().getPref(context, userId).getString("followed_cached_magazines", "");
                String sharedRandomCachedMagazines = MagazinePreferenceEndPoint.getInstance().getPref(context, userId).getString("random_cached_magazines", "");

                List<Articles> cachedMagazinesList = new ArrayList<>();

                if (!TextUtils.isEmpty(sharedFollowedCachedMagazines) || !TextUtils.isEmpty(sharedRandomCachedMagazines)) {
                    if (magazineFlipArticlesFragment.mProgress != null) {
                        magazineFlipArticlesFragment.mProgress.setVisibility(View.GONE);
                    }
                    magazineFlipArticlesFragment.tvProgressText.setVisibility(View.GONE);
                    magazineFlipArticlesFragment.myBaseAdapter.clear();
                    if (!TextUtils.isEmpty(sharedFollowedCachedMagazines)) {
                        String cachedMagazines = sharedFollowedCachedMagazines;
                        List<Articles> cachedFollowedMagazinesList = new Gson().fromJson(cachedMagazines, type1);
                        cachedMagazinesList.addAll(cachedFollowedMagazinesList);
                    }

                    if (!TextUtils.isEmpty(sharedRandomCachedMagazines)) {
                        String cachedMagazines = sharedRandomCachedMagazines;
                        List<Articles> cachedRandomMagazinesList = new Gson().fromJson(cachedMagazines, type1);
                        cachedMagazinesList.addAll(cachedRandomMagazinesList);
                    }

                    magazineFlipArticlesFragment.myBaseAdapter.addItems(cachedMagazinesList);
                    magazineFlipArticlesFragment.flipView.flipTo(magazineFlipArticlesFragment.lastReadArticle);
                    magazineFlipArticlesFragment.articlesRootLayout.setVisibility(View.VISIBLE);
                    magazineFlipArticlesFragment.networkFailureText.setVisibility(View.GONE);
                    return;
                }
            }
        } else {
            magazineFlipArticlesFragment.articlesRootLayout.setVisibility(View.VISIBLE);
            magazineFlipArticlesFragment.networkFailureText.setVisibility(View.GONE);
        }

        if (magazineFlipArticlesFragment.mProgress != null) {
            magazineFlipArticlesFragment.mProgress.setVisibility(View.VISIBLE);
        }

        String accessToken = preferenceEndPoint.getStringPreference("access_token");
        if (tagIds != null) { // Getting articles of the selected topic
            magazineFlipArticlesFragment.isSearch = true;
            yoService.getArticlesAPI(accessToken, tagIds).enqueue(magazineFlipArticlesFragment.callback);
            magazineFlipArticlesFragment.tvProgressText.setVisibility(View.GONE);
        } else {

            magazineFlipArticlesFragment.isSearch = false;
            magazineFlipArticlesFragment.flipContainer.setVisibility(View.GONE);
            magazineFlipArticlesFragment.tvProgressText.setVisibility(View.VISIBLE);

            List<String> readArticlesList = new ArrayList<>();
            List<String> unreadArticlesList = new ArrayList<>();
            magazineFlipArticlesFragment.magazineDashboardHelper.getDashboardArticles(magazineFlipArticlesFragment, yoService, preferenceEndPoint, readArticlesList, unreadArticlesList, renewal);

        }
    }

    /**
     * Deleting extra articles from the cache if the cache exceeds the limit
     */
    public void deleteExtraArticlesFromCache(Context context, MagazineDashboardHelper magazineDashboardHelper) {

        String userId = preferenceEndPoint.getStringPreference(Constants.USER_ID);
        if (context != null) {
            Log.d("FlipArticlesFragment", "Deleting extra articles from the cache");
            String sharedFollowedCachedMagazines = MagazinePreferenceEndPoint.getInstance().getPref(context, userId).getString("followed_cached_magazines", "");
            String sharedRandomCachedMagazines = MagazinePreferenceEndPoint.getInstance().getPref(context, userId).getString("random_cached_magazines", "");

            List<Articles> cachedFollowedMagazinesList = new ArrayList<>();
            List<Articles> cachedRandomMagazinesList = new ArrayList<>();
            if (!TextUtils.isEmpty(sharedFollowedCachedMagazines) || !TextUtils.isEmpty(sharedRandomCachedMagazines)) {

                Type type = new TypeToken<List<Articles>>() {
                }.getType();

                if (!TextUtils.isEmpty(sharedFollowedCachedMagazines)) {
                    String cachedMagazines = sharedFollowedCachedMagazines;
                    cachedFollowedMagazinesList = new Gson().fromJson(cachedMagazines, type);
                }
                if (!TextUtils.isEmpty(sharedRandomCachedMagazines)) {
                    String cachedMagazines = sharedRandomCachedMagazines;
                    cachedRandomMagazinesList = new Gson().fromJson(cachedMagazines, type);
                }

                List<Articles> emptyUpdatedArticles = new ArrayList<>();
                List<Articles> notEmptyUpdatedArticles = new ArrayList<>();
                for (Articles updatedArticles : cachedFollowedMagazinesList) {
                    if (!TextUtils.isEmpty(updatedArticles.getUpdated())) {
                        notEmptyUpdatedArticles.add(updatedArticles);
                    } else {
                        emptyUpdatedArticles.add(updatedArticles);
                    }
                }
                Collections.sort(notEmptyUpdatedArticles, new ArticlesComparator());
                notEmptyUpdatedArticles.addAll(emptyUpdatedArticles);
                cachedFollowedMagazinesList = notEmptyUpdatedArticles;

                List<Articles> emptyUpdatedArticles1 = new ArrayList<>();
                List<Articles> notEmptyUpdatedArticles1 = new ArrayList<>();
                for (Articles updatedArticles : cachedRandomMagazinesList) {
                    if (!TextUtils.isEmpty(updatedArticles.getUpdated())) {
                        notEmptyUpdatedArticles1.add(updatedArticles);
                    } else {
                        emptyUpdatedArticles1.add(updatedArticles);
                    }
                }
                Collections.sort(notEmptyUpdatedArticles1, new ArticlesComparator());
                notEmptyUpdatedArticles1.addAll(emptyUpdatedArticles1);
                cachedRandomMagazinesList = notEmptyUpdatedArticles1;

                int totalCachedSize = cachedFollowedMagazinesList.size() + cachedRandomMagazinesList.size();

                if (totalCachedSize > articleCountThreshold) {
                    int extraArticlesCount = totalCachedSize - articleCountThreshold;
                    int cachedRandomSize = cachedRandomMagazinesList.size();
                    int cachedFollowedSize = cachedFollowedMagazinesList.size();
                    if (cachedRandomSize <= extraArticlesCount) {
                        //Delete all the random articles
                        if (context != null) {
                            magazineDashboardHelper.removeArticlesFromCache(context, preferenceEndPoint, "random_cached_magazines");
                        }
                        // Then get the remaining articles count after deleting the random articles
                        int remainingArticlesCount = extraArticlesCount - cachedRandomSize;
                        // Move to followed articles list
                        if (cachedFollowedSize <= remainingArticlesCount) {
                            // Delete all the followed articles
                            if (context != null) {
                                magazineDashboardHelper.removeArticlesFromCache(context, preferenceEndPoint, "followed_cached_magazines");
                            }
                        } else {
                            // Delete the articles equal to the remaining articles count
                            cachedFollowedMagazinesList.subList(0, remainingArticlesCount).clear();
                            if (context != null) {
                                SharedPreferences.Editor editor = MagazinePreferenceEndPoint.getInstance().get(context, userId);
                                editor.putString("followed_cached_magazines", new Gson().toJson(new LinkedHashSet<Articles>(cachedFollowedMagazinesList)));
                                editor.commit();
                            }
                        }
                    } else {
                        // Delete the articles equal to the extra articles count
                        cachedRandomMagazinesList.subList(0, extraArticlesCount).clear();
                        if (context != null) {
                            SharedPreferences.Editor editor = MagazinePreferenceEndPoint.getInstance().get(context, userId);
                            editor.putString("random_cached_magazines", new Gson().toJson(new LinkedHashSet<Articles>(cachedRandomMagazinesList)));
                            editor.commit();
                        }
                    }
                }
            }
        }
    }

}
