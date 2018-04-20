package com.yo.android.usecase;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.aphidmobile.utils.AphidLog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
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
import com.yo.android.photo.util.ColorGenerator;
import com.yo.android.ui.BaseActivity;
import com.yo.android.ui.BitmapScaler;
import com.yo.android.ui.CreateMagazineActivity;
import com.yo.android.ui.DeviceDimensionsHelper;
import com.yo.android.ui.OtherProfilesLikedArticles;
import com.yo.android.ui.TopicsDetailActivity;
import com.yo.android.util.ArticlesComparator;
import com.yo.android.util.Constants;
import com.yo.android.util.MagazineDashboardHelper;
import com.yo.android.util.Util;
import com.yo.android.video.InAppVideoActivity;

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

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

/**
 * Created by ec on 9/2/18.
 */

/**
 * This is the helper class that handles the service calls and other specific utility methods of the MagazineArticlesBaseAdapter class
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

    private ColorGenerator mColorGenerator = ColorGenerator.MATERIAL;
    private static int articleCountThreshold = 2000;

    /**
     * This method is used to call the service to like an article
     * @param magazineArticlesBaseAdapter The MagazineArticlesBaseAdapter object
     * @param context The Context
     * @param data The Articles object
     * @param mToastFactory The ToastFactory object
     */
    public void likeArticles(final MagazineArticlesBaseAdapter magazineArticlesBaseAdapter, final Context context, final Articles data, final ToastFactory mToastFactory) {
        ((BaseActivity) context).showProgressDialog();
        String accessToken = preferenceEndPoint.getStringPreference(YoApi.ACCESS_TOKEN);
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
     * @param context The Context
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
     * @param cachedMagazinesList The list of articles to be cached
     * @param context The Context
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

    /**
     * This method is used to call the service to unlike an article
     * @param magazineArticlesBaseAdapter The MagazineArticlesBaseAdapter object
     * @param context The Context
     * @param data The Articles object
     * @param mToastFactory The ToastFactory object
     */
    public void unlikeArticles(final MagazineArticlesBaseAdapter magazineArticlesBaseAdapter, final Context context, final Articles data, final ToastFactory mToastFactory) {
        ((BaseActivity) context).showProgressDialog();
        String accessToken = preferenceEndPoint.getStringPreference(YoApi.ACCESS_TOKEN);
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

    /**
     * This method is used to handle the like or unlike success response
     * @param magazineArticlesBaseAdapter The MagazineArticlesBaseAdapter object
     * @param context The Context
     * @param data The articles object
     * @param mToastFactory The ToastFactory object
     * @param isChecked boolean whether checkbox is checked or not
     * @param setLiked boolean whether liked or unliked
     * @param toastMsg The msg to be displayed in the toast
     */
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

    /**
     * This method is used to handle the like or unlike failure response
     * @param magazineArticlesBaseAdapter The MagazineArticlesBaseAdapter object
     * @param context The Context
     * @param data The Articles object
     * @param mToastFactory The ToastFactory object
     * @param isChecked boolean whether checkbox is checked or not
     * @param setLiked boolean whether liked or unliked
     * @param toastMsg The msg to be displayed in the toast
     */
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

    /**
     * Handles the loading of the article image
     * @param holder The ViewHolder object
     * @param context The Context
     * @param data The Articles object
     * @param photoView The ImageView
     */
    public void handleImageLoading(MagazineArticlesBaseAdapter.ViewHolder holder, Context context, final Articles data, ImageView photoView) {
        if (!((BaseActivity) context).hasDestroyed()) {
            final TextView fullImageTitle = holder.fullImageTitle;
            final TextView articleTitle = holder.articleTitle;
            final ImageView blackMask = holder.blackMask;
            final RelativeLayout rlFullImageOptions = holder.rlFullImageOptions;
            final TextView textView = holder.articleShortDesc;
            RequestOptions requestOptions = new RequestOptions()
                    //.placeholder(mColorGenerator.getColorPlaceholder())
                    .placeholder(R.drawable.magazine_backdrop)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .dontAnimate();
            Glide.with(context)
                    .load(data.getS3_image_filename())
                    .apply(requestOptions)
                    .into(photoView);

            if(articleTitle != null) {
                articleTitle.setText(AphidLog.format("%s", data.getTitle()));
            }

            if (textView != null) {
                textView.setText(Html.fromHtml(data.getSummary()));
            }

        }
    }

    /**
     * Loads the image from S3
     * @param context The Context
     * @param data The Articles object
     * @param photoView The ImageView
     */
    public void loadImageFromS3(final Context context, final Articles data, final ImageView photoView) {
        if (!((BaseActivity) context).hasDestroyed()) {
            RequestOptions requestOptions = new RequestOptions()
                    .placeholder(R.drawable.magazine_backdrop)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .dontAnimate();
            Glide.with(context)
                    .asBitmap()
                    .load(data.getS3_image_filename())
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            int screenWidth = DeviceDimensionsHelper.getDisplayWidth(context);
                            Bitmap bmp = null;
                            if (resource != null) {
                                try {
                                    bmp = BitmapScaler.scaleToFitWidth(resource, screenWidth);
                                    RequestOptions options = new RequestOptions()
                                            .override(bmp.getWidth(), bmp.getHeight())
                                            //.placeholder(R.drawable.magazine_backdrop)
                                            .placeholder(R.drawable.magazine_backdrop)
                                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                                            .dontAnimate();
                                    Glide.with(context).clear(photoView);
                                    Glide.with(context)
                                            .load(data.getS3_image_filename())
                                            //.transition(withCrossFade())
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

    /**
     * Navigates to the article WebView
     * @param context The Context
     * @param data The Articles object
     * @param position The article position
     */
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

    /**
     * Handles clicking on the Share icon
     * @param v The View
     * @param data The Articles object
     */
    public void onShareClick(View v, Articles data) {
        if (data.getImage_filename() != null) {
            new Util.ImageLoaderTask(v, data).execute(data.getImage_filename());
        } else {
            String summary = Html.fromHtml(data.getSummary()).toString();
            Util.shareNewIntent(v, data.getGenerated_url(), "Article: " + data.getTitle(), summary, null);
        }
    }

    /**
     * Handles clicking on the Add icon
     * @param context The Context
     * @param data The Articles object
     */
    public void onAddClick(Context context, Articles data) {
        Intent intent = new Intent(context, CreateMagazineActivity.class);
        intent.putExtra(Constants.MAGAZINE_ADD_ARTICLE_ID, data.getId());
        if (context instanceof Activity) {
            ((Activity) context).startActivityForResult(intent, Constants.ADD_ARTICLES_TO_MAGAZINE);
        }
    }

    /**
     * Navigates to the particular Topic
     * @param magazineFlipArticlesFragment The {@link MagazineFlipArticlesFragment object}
     * @param context The Context
     * @param data The Articles object
     * @param position The article position
     */
    public void navigateToTopicDetails(MagazineFlipArticlesFragment magazineFlipArticlesFragment, Context context,Articles data, int position) {
        Intent intent = new Intent(context, TopicsDetailActivity.class);
        intent.putExtra("Topic", data);
        intent.putExtra("Position", position);
        magazineFlipArticlesFragment.startActivityForResult(intent, 60);
    }

    /**
     * Navigates from the left and right article in the Landing screen to the particular Topic
     * @param magazineFlipArticlesFragment The {@link MagazineFlipArticlesFragment object}
     * @param context The Context
     * @param data The Articles object
     * @param position The article position
     * @param placement whether left or right article
     */
    public void navigateFromLeftRightToTopicsDetail(MagazineFlipArticlesFragment magazineFlipArticlesFragment, Context context, Articles data, int position, String placement) {
        Intent intent = new Intent(context, TopicsDetailActivity.class);
        intent.putExtra("Topic", data);
        intent.putExtra("Position", position);
        intent.putExtra("ArticlePlacement", placement);
        magazineFlipArticlesFragment.startActivityForResult(intent, 60);
    }

    /**
     * Navigates from the left and right article in the Landing screen to the particular article WebView
     * @param magazineFlipArticlesFragment The {@link MagazineFlipArticlesFragment object}
     * @param context The Context
     * @param data The Articles object
     * @param position The article position
     * @param placement whether left or right article
     */
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
     * @param allArticles The list of all articles
     * @param context The Context
     * @param magazineArticlesBaseAdapter The MagazineArticlesBaseAdapter object
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
     * @param tagIds The topic ids
     * @param context The Context
     * @param magazineFlipArticlesFragment The {@link MagazineFlipArticlesFragment object}
     * @param renewal boolean whether renewal is true or false
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

        String accessToken = preferenceEndPoint.getStringPreference(YoApi.ACCESS_TOKEN);
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
     * @param context The Context
     * @param magazineDashboardHelper The MagazineDashboardHelper object
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

    /**
     * Handles sharing the article
     * @param imageView The {@link ImageView} object
     * @param data The Articles data
     */
    public void handleArticleShare(ImageView imageView, final Articles data) {
        if (imageView != null) {
            ImageView share = imageView;
            share.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onShareClick(v, data);
                }
            });
        }
    }

    /**
     * Handles adding the article to a magazine
     * @param imageView The {@link ImageView} object
     * @param data The Articles data
     * @param context The Context
     */
    public void handleArticleAdd(ImageView imageView, final Articles data, final Context context) {
        if (imageView != null) {
            ImageView add = imageView;
            add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onAddClick(context, data);
                }
            });
        }
    }

    /**
     * Handles the image of the article
     * @param position The article position
     * @param holder The ViewHolder object
     * @param imageView The {@link ImageView} object
     * @param data The Articles data
     * @param context The Context
     */
    public void handleArticleImage(final int position, MagazineArticlesBaseAdapter.ViewHolder holder, ImageView imageView, final Articles data, final Context context) {
        if (imageView != null) {

            imageView.setImageResource(R.drawable.magazine_backdrop);
            if (data.getImage_filename() != null) {
                handleImageLoading(holder, context, data, imageView);
            } else {
                imageView.setImageResource(R.drawable.magazine_backdrop);
            }

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String videoUrl = data.getVideo_url();
                    if (videoUrl != null && !TextUtils.isEmpty(videoUrl)) {
                        InAppVideoActivity.start((Activity) context, videoUrl, data.getTitle());
                    } else {
                        navigateToArticleWebView(context, data, position);
                    }
                }
            });
        }
    }

    /**
     * Handles the liking of an article
     * @param position The article position
     * @param checkBox The {@link CheckBox} object
     * @param data The Articles data
     * @param magazineArticlesBaseAdapter The MagazineArticlesBaseAdapter object
     * @param context The Context
     * @param mToastFactory The ToastFactory object
     */
    public void handleArticleLike(int position, CheckBox checkBox, final Articles data, final MagazineArticlesBaseAdapter magazineArticlesBaseAdapter, final Context context, final ToastFactory mToastFactory) {
        if (checkBox != null) {
            checkBox.setTag(position);
        }

        if (checkBox != null) {
            checkBox.setOnCheckedChangeListener(null);
            if (Boolean.valueOf(data.getLiked())) {
                data.setIsChecked(true);
            } else {
                data.setIsChecked(false);
            }

            checkBox.setText("");
            checkBox.setChecked(Boolean.valueOf(data.getLiked()));

            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Log.d("MagazineBaseAdapter", "Title and liked... " + data.getTitle() + " " + Boolean.valueOf(data.getLiked()));

                    likeUnlikeServiceCall(isChecked, data, magazineArticlesBaseAdapter, context, mToastFactory);
                }
            });
        }
    }

    /**
     * Calls the like or unlike article
     * @param isChecked boolean whether checkbox is checked or not
     * @param data The Articles data
     * @param magazineArticlesBaseAdapter The MagazineArticlesBaseAdapter object
     * @param context The Context
     * @param mToastFactory The ToastFactory object
     */
    private void likeUnlikeServiceCall(boolean isChecked, Articles data, MagazineArticlesBaseAdapter magazineArticlesBaseAdapter, Context context, ToastFactory mToastFactory) {
        if (isChecked) {
            likeArticles(magazineArticlesBaseAdapter, context, data, mToastFactory);
        } else {
            unlikeArticles(magazineArticlesBaseAdapter, context, data, mToastFactory);
        }
    }

    /**
     * Displays the left or right article summary based on the density
     * @param textView The {@link TextView} object
     * @param data The Articles data
     * @param context The Context
     */
    public void displayLeftRightSummaryBasedOnDensity(TextView textView, Articles data, Context context) {
        if (textView != null) {

            float density = context.getResources().getDisplayMetrics().density;

            if (density == 4.0) {
                textView.setVisibility(View.VISIBLE);
            } else if (density == 3.5) {
                textView.setVisibility(View.VISIBLE);
            } else if (density == 3.0) {
                textView.setVisibility(View.VISIBLE);
            } else if (density == 2.0) {
                textView.setVisibility(View.VISIBLE);
            } else {
                textView.setVisibility(View.GONE);
            }

            if (data.getSummary() != null && textView != null) {
                textView
                        .setText(Html.fromHtml(data.getSummary()));
            }
        }
    }

    /**
     * Populates the empty left article
     *
     * @param holder The view holder object
     */
    public void populateEmptyLeftArticle(MagazineArticlesBaseAdapter.ViewHolder holder) {
        if (holder.magazineLikeLeft != null) {
            holder.magazineLikeLeft.setVisibility(View.GONE);
        }

        if (holder.articleTitleLeft != null) {
            holder.articleTitleLeft.setVisibility(View.GONE);
        }

        if (holder.articlePhotoLeft != null) {
            ImageView photoView = holder.articlePhotoLeft;
            photoView.setVisibility(View.GONE);
        }

        if (holder.magazineAddLeft != null) {
            ImageView add = holder.magazineAddLeft;
            add.setVisibility(View.GONE);
        }

        if (holder.magazineShareLeft != null) {
            ImageView share = holder.magazineShareLeft;
            share.setVisibility(View.GONE);
        }

        if (holder.articleFollowLeft != null) {
            holder.articleFollowLeft.setVisibility(View.GONE);
        }

        if (holder.tvTopicNameLeft != null) {
            holder.tvTopicNameLeft.setVisibility(View.GONE);
        }

    }

    /**
     * Populates the empty right article
     * @param holder  The view holder object
     */
    public void populateEmptyRightArticle(MagazineArticlesBaseAdapter.ViewHolder holder) {
        if (holder.magazineLikeRight != null) {
            holder.magazineLikeRight.setVisibility(View.GONE);
        }

        if (holder.articleTitleRight != null) {
            holder.articleTitleRight.setVisibility(View.GONE);
        }

        if (holder.articlePhotoRight != null) {
            ImageView photoView = holder.articlePhotoRight;
            photoView.setVisibility(View.GONE);
        }

        if (holder.magazineAddRight != null) {
            ImageView add = holder.magazineAddRight;
            add.setVisibility(View.GONE);
        }

        if (holder.magazineShareRight != null) {
            ImageView share = holder.magazineShareRight;
            share.setVisibility(View.GONE);
        }

        if (holder.articleFollowRight != null) {
            holder.articleFollowRight.setVisibility(View.GONE);
        }

        if (holder.tvTopicNameRight != null) {
            holder.tvTopicNameRight.setVisibility(View.GONE);
        }

    }

}
