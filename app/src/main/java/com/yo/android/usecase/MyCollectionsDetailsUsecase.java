package com.yo.android.usecase;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.orion.android.common.preferences.PreferenceEndPoint;
import com.orion.android.common.util.ConnectivityHelper;
import com.orion.android.common.util.ToastFactory;
import com.yo.android.R;
import com.yo.android.adapters.MagazineArticlesBaseAdapter;
import com.yo.android.api.YoApi;
import com.yo.android.helpers.MagazinePreferenceEndPoint;
import com.yo.android.model.Articles;
import com.yo.android.model.MagazineArticles;
import com.yo.android.ui.BaseActivity;
import com.yo.android.ui.MyCollectionDetails;
import com.yo.android.util.ArticlesComparator;
import com.yo.android.util.Constants;

import java.lang.reflect.Type;
import java.net.SocketTimeoutException;
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
 * Created by ec on 12/2/18.
 */

/**
 * This is the helper class that handles the service calls and other specific utility methods of the MyCollectionDetails class
 */
public class MyCollectionsDetailsUsecase {

    public static final String TAG = MyCollectionsDetailsUsecase.class.getSimpleName();
    @Inject
    YoApi.YoService yoService;

    @Inject
    @Named("login")
    PreferenceEndPoint preferenceEndPoint;
    @Inject
    ConnectivityHelper mHelper;
    @Inject
    public MagazinesServicesUsecase magazinesServicesUsecase;
    private List<Articles> cachedArticlesList = new ArrayList<>();
    @Inject
    protected ToastFactory mToastFactory;

    /**
     * Displays the unread cached magazines
     * @param context The Context
     * @param topicId The topic id
     * @param myBaseAdapter The {@link MyCollectionDetails.MyBaseAdapter} object
     */
    public void displayUnreadCachedMagazines(Context context, String topicId, MyCollectionDetails.MyBaseAdapter myBaseAdapter) {
        String userId = preferenceEndPoint.getStringPreference(Constants.USER_ID);
        List<Articles> cachedTopicMagazinesList = new ArrayList<Articles>();
        List<Articles> cachedMagazinesList = magazinesServicesUsecase.getCachedMagazinesList(context);

        if (cachedMagazinesList != null) {
            for (Articles article : cachedMagazinesList) {
                if (article.getTopicId().equals(topicId)) {
                    cachedTopicMagazinesList.add(article);
                }
            }
        }

        cachedArticlesList.addAll(cachedTopicMagazinesList);
        List<Articles> tempArticlesList = new ArrayList<Articles>(cachedArticlesList);
        String readCachedIds = MagazinePreferenceEndPoint.getInstance().getPref(context, userId).getString("read_article_ids", "");
        if (!TextUtils.isEmpty(readCachedIds)) {
            Type type1 = new TypeToken<List<String>>() {
            }.getType();
            String cachedIds = readCachedIds;
            List<String> cachedReadList = new Gson().fromJson(cachedIds, type1);


            for (Articles article : cachedArticlesList) {
                for (String artId : cachedReadList) {
                    if (article.getId().equals(artId)) {
                        tempArticlesList.remove(article);
                        break;
                    }
                }
            }
        }
        cachedArticlesList = tempArticlesList;
        List<Articles> emptyUpdatedArticles = new ArrayList<>();
        List<Articles> notEmptyUpdatedArticles = new ArrayList<>();
        for (Articles updatedArticles : cachedArticlesList) {
            if (!TextUtils.isEmpty(updatedArticles.getUpdated())) {
                notEmptyUpdatedArticles.add(updatedArticles);
            } else {
                emptyUpdatedArticles.add(updatedArticles);
            }
        }
        Collections.sort(notEmptyUpdatedArticles, new ArticlesComparator());
        Collections.reverse(notEmptyUpdatedArticles);
        notEmptyUpdatedArticles.addAll(emptyUpdatedArticles);
        cachedArticlesList = notEmptyUpdatedArticles;
        LinkedHashSet<Articles> hashSet = new LinkedHashSet<>();
        hashSet.addAll(cachedArticlesList);
        cachedArticlesList = new ArrayList<Articles>(hashSet);
        myBaseAdapter.addItems(cachedArticlesList);
    }

    /**
     * Calls the service to unlike the article
     * @param data The Articles object
     * @param context The Context
     * @param myBaseAdapter The {@link MyCollectionDetails.MyBaseAdapter} object
     */
    public void unlikeMyCollectionArticles(final Articles data, final Context context, final MyCollectionDetails.MyBaseAdapter myBaseAdapter) {
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

    /**
     * Handles the like or unlike failure response
     * @param data The Articles object
     * @param isChecked isChecked or not
     * @param setLiked whether liked or unliked
     * @param toastMsg The msg displayed in the toast
     * @param context The Context
     * @param myBaseAdapter The {@link MyCollectionDetails.MyBaseAdapter} object
     */
    private void handleLikeUnlikeMyCollectionFailure(Articles data, boolean isChecked, String setLiked, String toastMsg, Context context, MyCollectionDetails.MyBaseAdapter myBaseAdapter) {
        ((BaseActivity) context).dismissProgressDialog();
        Toast.makeText(context, toastMsg + data.getTitle(), Toast.LENGTH_LONG).show();
        data.setIsChecked(isChecked);
        data.setLiked(setLiked);
        if (!((BaseActivity) context).hasDestroyed()) {
            myBaseAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Calls the service to like the article
     * @param data The Articles object
     * @param context The Context
     * @param myBaseAdapter The {@link MyCollectionDetails.MyBaseAdapter} object
     */
    public void likeMyCollectionArticles(final Articles data, final Context context, final MyCollectionDetails.MyBaseAdapter myBaseAdapter) {
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

    /**
     * Handles the like or unlike success response
     * @param data The Articles object
     * @param isChecked isChecked or not
     * @param setLiked whether liked or unliked
     * @param toastMsg The msg displayed in the toast
     * @param context The Context
     * @param myBaseAdapter The {@link MyCollectionDetails.MyBaseAdapter} object
     */
    private void handleLikeUnlikeMyCollectionSuccess(final Articles data, boolean isChecked, String setLiked, String toastMsg, final Context context, final MyCollectionDetails.MyBaseAdapter myBaseAdapter) {
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

    /**
     * Gets the remaining articles of the topic
     *
     * @param existingArticles The list of existing article ids
     * @param myBaseAdapter The {@link MyCollectionDetails.MyBaseAdapter} object
     * @param  myCollectionDetails The {@link MyCollectionDetails} object
     * @param topicId The topic id
     */
    public void getRemainingArticlesInTopics(List<String> existingArticles, String topicId, final MyCollectionDetails myCollectionDetails, final MyCollectionDetails.MyBaseAdapter myBaseAdapter) {
        String accessToken = preferenceEndPoint.getStringPreference("access_token");
        yoService.getRemainingArticlesInTopicAPI(accessToken, topicId, existingArticles).enqueue(new Callback<List<Articles>>() {
            @Override
            public void onResponse(Call<List<Articles>> call, Response<List<Articles>> response) {
                myCollectionDetails.dismissProgressDialog();
                if (response.body() != null && response.body().size() > 0) {
                    try {
                        for (int i = 0; i < response.body().size(); i++) {
                            if (!"...".equalsIgnoreCase(response.body().get(i).getSummary())) {
                                myCollectionDetails.articlesHashSet.add(response.body().get(i));
                            }
                        }
                        myCollectionDetails.articlesList = new ArrayList<>(myCollectionDetails.articlesHashSet);
                        myCollectionDetails.articlesList.addAll(cachedArticlesList);
                    } finally {
                        if(response != null && response.body() != null) {
                            try {
                                response.body().clear();
                                response = null;
                            }catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    getUnreadArticlesAndSort(myCollectionDetails, myBaseAdapter);
                } else {
                    if (cachedArticlesList.size() == 0) {
                        myCollectionDetails.tvNoArticles.setVisibility(View.VISIBLE);
                        myCollectionDetails.flipView.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Articles>> call, Throwable t) {
                myCollectionDetails.dismissProgressDialog();
                if (cachedArticlesList.size() == 0) {
                    failureError(t, myCollectionDetails);
                }
            }
        });
    }

    /**
     * Gets the unread articles and sorts them
     * @param myCollectionDetails The {@link MyCollectionDetails} object
     * @param myBaseAdapter The {@link MyCollectionDetails.MyBaseAdapter} object
     */
    private void getUnreadArticlesAndSort(MyCollectionDetails myCollectionDetails, MyCollectionDetails.MyBaseAdapter myBaseAdapter) {
        List<Articles> tempArticlesList = new ArrayList<Articles>(myCollectionDetails.articlesList);
        String userId = preferenceEndPoint.getStringPreference(Constants.USER_ID);
        String readCachedIds = MagazinePreferenceEndPoint.getInstance().getPref(myCollectionDetails, userId).getString("read_article_ids", "");
        if (!TextUtils.isEmpty(readCachedIds)) {
            Type type1 = new TypeToken<List<String>>() {
            }.getType();
            String cachedIds = readCachedIds;
            List<String> cachedReadList = new Gson().fromJson(cachedIds, type1);


            for (Articles article : myCollectionDetails.articlesList) {
                for (String artId : cachedReadList) {
                    if (article.getId().equals(artId)) {
                        tempArticlesList.remove(article);
                        break;
                    }
                }
            }
        }
        myCollectionDetails.articlesList = tempArticlesList;

        List<Articles> emptyUpdatedArticles = new ArrayList<>();
        List<Articles> notEmptyUpdatedArticles = new ArrayList<>();
        for (Articles updatedArticles : myCollectionDetails.articlesList) {
            if (!TextUtils.isEmpty(updatedArticles.getUpdated())) {
                notEmptyUpdatedArticles.add(updatedArticles);
            } else {
                emptyUpdatedArticles.add(updatedArticles);
            }
        }
        Collections.sort(notEmptyUpdatedArticles, new ArticlesComparator());
        Collections.reverse(notEmptyUpdatedArticles);
        notEmptyUpdatedArticles.addAll(emptyUpdatedArticles);
        myCollectionDetails.articlesList = notEmptyUpdatedArticles;

                    /*for (Articles a : articlesList) {
                        Log.d("MyCollectionDetails", "The sorted list is " + a.getId() + " updated " + a.getUpdated());
                    }*/
        myBaseAdapter.addItems(myCollectionDetails.articlesList);
        if (myCollectionDetails.articlesList.size() == 0) {
            myCollectionDetails.tvNoArticles.setVisibility(View.VISIBLE);
            myCollectionDetails.flipView.setVisibility(View.GONE);
        } else {
            myCollectionDetails.tvNoArticles.setVisibility(View.GONE);
            myCollectionDetails.flipView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Gets the remaining articles of the magazine
     *
     * @param existingArticles The list of existing article ids
     * @param myBaseAdapter The {@link MyCollectionDetails.MyBaseAdapter} object
     * @param myCollectionDetails The {@link MyCollectionDetails} object
     * @param topicId The topic id
     */
    public void getRemainingArticlesInMagazine(List<String> existingArticles, final MyCollectionDetails myCollectionDetails, String topicId, final MyCollectionDetails.MyBaseAdapter myBaseAdapter) {
        String accessToken = preferenceEndPoint.getStringPreference("access_token");
        yoService.getRemainingArticlesInMagAPI(accessToken, topicId, existingArticles).enqueue(new Callback<MagazineArticles>() {
            @Override
            public void onResponse(Call<MagazineArticles> call, Response<MagazineArticles> response) {
                myCollectionDetails.dismissProgressDialog();
                if (response.body().getArticlesList() != null && response.body().getArticlesList().size() > 0) {
                    try {
                        for (int i = 0; i < response.body().getArticlesList().size(); i++) {
                            if (!"...".equalsIgnoreCase(response.body().getArticlesList().get(i).getSummary())) {
                                myCollectionDetails.articlesHashSet.add(response.body().getArticlesList().get(i));
                            }
                        }
                        myCollectionDetails.articlesList = new ArrayList<Articles>(myCollectionDetails.articlesHashSet);
                        myCollectionDetails.articlesList.addAll(cachedArticlesList);
                    } finally {
                        if(response != null && response.body() != null) {
                            try {
                                response = null;
                            }catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    getUnreadArticlesAndSort(myCollectionDetails, myBaseAdapter );
                } else {
                    if (cachedArticlesList.size() == 0) {
                        myCollectionDetails.tvNoArticles.setVisibility(View.VISIBLE);
                        myCollectionDetails.flipView.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onFailure(Call<MagazineArticles> call, Throwable t) {
                myCollectionDetails.dismissProgressDialog();
                if (cachedArticlesList.size() == 0) {
                    failureError(t, myCollectionDetails);
                }
            }
        });
    }

    /**
     * Handles the failure of getting the remaining articles in a topic or magazine
     * @param t The Throwable instance
     * @param myCollectionDetails The {@link MyCollectionDetails} object
     */
    private void failureError(Throwable t, MyCollectionDetails myCollectionDetails) {
        if(t instanceof SocketTimeoutException) {
            myCollectionDetails.tvNoArticles.setText(R.string.socket_time_out);
        } else {
            myCollectionDetails.tvNoArticles.setText(R.string.no_articles);
        }
        myCollectionDetails.tvNoArticles.setVisibility(View.VISIBLE);
        myCollectionDetails.flipView.setVisibility(View.GONE);
    }

}
