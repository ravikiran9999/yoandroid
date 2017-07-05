package com.yo.android.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.R;
import com.yo.android.api.YoApi;
import com.yo.android.flip.MagazineFlipArticlesFragment;
import com.yo.android.helpers.MagazinePreferenceEndPoint;
import com.yo.android.model.Articles;
import com.yo.android.model.LandingArticles;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by creatives on 12/8/2016.
 */
public class MagazineDashboardHelper {

    public static int currentReadArticles;
    public static int request;
    private static final int DASHBOARD_ARTICLES = 1;
    private static final int MORE_DASHBOARD_ARTICLES = 2;
    private static final int DASHBOARD_ARTICLES_AFTER_FOLLOW = 3;
    private static final int DASHBOARD_ARTICLES_DAILY_SERVICE = 4;


    public void getDashboardArticles(final MagazineFlipArticlesFragment magazineFlipArticlesFragment, YoApi.YoService yoService, final PreferenceEndPoint preferenceEndPoint, List<String> readArticleIds, List<String> unreadArticleIds, boolean renewal) {

        if (magazineFlipArticlesFragment != null) {
            String accessToken = preferenceEndPoint.getStringPreference("access_token");
            boolean autoRenewalSubscription = preferenceEndPoint.getBooleanPreference(Constants.AUTO_RENEWAL_SUBSCRIPTION, false);
            yoService.getDashboardArticlesAPI(accessToken, readArticleIds, unreadArticleIds, autoRenewalSubscription, renewal).enqueue(new Callback<LandingArticles>() {
                @Override
                public void onResponse(Call<LandingArticles> call, Response<LandingArticles> response) {

                    if (!magazineFlipArticlesFragment.isAdded()) {
                        return;
                    }
                    magazineFlipArticlesFragment.myBaseAdapter.clear();
                    if (magazineFlipArticlesFragment.mProgress != null) {
                        magazineFlipArticlesFragment.mProgress.setVisibility(View.GONE);
                    }
                    magazineFlipArticlesFragment.tvProgressText.setVisibility(View.GONE);
                    currentReadArticles = 0;
                    request = 1;
                    if (response.body() != null) {
                        List<Articles> totalArticles = getTotalArticles(response.body());
                        showDialog(response.body().getCode(), DASHBOARD_ARTICLES, preferenceEndPoint, magazineFlipArticlesFragment, totalArticles, null, null);

                    } else {
                        magazineFlipArticlesFragment.flipContainer.setVisibility(View.VISIBLE);
                        magazineFlipArticlesFragment.llNoArticles.setVisibility(View.GONE);
                        magazineFlipArticlesFragment.getLandingCachedArticles();
                    }

                }

                @Override
                public void onFailure(Call<LandingArticles> call, Throwable t) {
                    if (magazineFlipArticlesFragment.mProgress != null) {
                        magazineFlipArticlesFragment.mProgress.setVisibility(View.GONE);
                    }
                    magazineFlipArticlesFragment.tvProgressText.setVisibility(View.GONE);
                    magazineFlipArticlesFragment.flipContainer.setVisibility(View.GONE);
                    magazineFlipArticlesFragment.networkFailureText.setVisibility(View.VISIBLE);
                }
            });
        }

    }

    public List<Articles> removeReadIds(List<Articles> totalArticles, Context context, final PreferenceEndPoint preferenceEndPoint) {
        List<Articles> tempArticlesList = new ArrayList<>(totalArticles);
        String userId = preferenceEndPoint.getStringPreference(Constants.USER_ID);
        if (context != null) {
            String readCachedIds = MagazinePreferenceEndPoint.getInstance().getPref(context, userId).getString("read_article_ids", "");
            if (!TextUtils.isEmpty(readCachedIds)) {
                Type type1 = new TypeToken<List<String>>() {
                }.getType();
                String cachedIds = readCachedIds;
                List<String> cachedReadList = new Gson().fromJson(cachedIds, type1);
                for (Articles article : totalArticles) {
                    for (String artId : cachedReadList) {
                        if (article.getId().equals(artId)) {
                            tempArticlesList.remove(article);
                            break;
                        }
                    }
                }
                totalArticles = tempArticlesList;
            }
        }
        return totalArticles;
    }

    public void getMoreDashboardArticles(final MagazineFlipArticlesFragment magazineFlipArticlesFragment, YoApi.YoService yoService, final PreferenceEndPoint preferenceEndPoint, List<String> readArticleIds, List<String> unreadArticleIds, final SwipeRefreshLayout swipeRefreshContainer) {
        if (magazineFlipArticlesFragment != null) {
            String accessToken = preferenceEndPoint.getStringPreference("access_token");
            boolean autoRenewalSubscription = preferenceEndPoint.getBooleanPreference(Constants.AUTO_RENEWAL_SUBSCRIPTION, false);
            yoService.getDashboardArticlesAPI(accessToken, readArticleIds, unreadArticleIds, autoRenewalSubscription, false).enqueue(new Callback<LandingArticles>() {
                @Override
                public void onResponse(Call<LandingArticles> call, Response<LandingArticles> response) {
                    if (swipeRefreshContainer != null) {
                        swipeRefreshContainer.setRefreshing(false);
                    }
                    if (magazineFlipArticlesFragment.mProgress != null) {
                        magazineFlipArticlesFragment.mProgress.setVisibility(View.GONE);
                    }
                    magazineFlipArticlesFragment.tvProgressText.setVisibility(View.GONE);
                    request++;

                    if (response.body() != null) {
                        List<Articles> totalArticles = getTotalArticles(response.body());
                        showDialog(response.body().getCode(), MORE_DASHBOARD_ARTICLES, preferenceEndPoint, magazineFlipArticlesFragment, totalArticles, null, null);
                    } else {
                        magazineFlipArticlesFragment.flipContainer.setVisibility(View.VISIBLE);
                        magazineFlipArticlesFragment.llNoArticles.setVisibility(View.GONE);
                        magazineFlipArticlesFragment.getLandingCachedArticles();
                    }

                }

                @Override
                public void onFailure(Call<LandingArticles> call, Throwable t) {
                    if (magazineFlipArticlesFragment.mProgress != null) {
                        magazineFlipArticlesFragment.mProgress.setVisibility(View.GONE);
                    }
                    magazineFlipArticlesFragment.tvProgressText.setVisibility(View.GONE);
                    magazineFlipArticlesFragment.flipContainer.setVisibility(View.VISIBLE);
                    magazineFlipArticlesFragment.llNoArticles.setVisibility(View.GONE);
                    magazineFlipArticlesFragment.getLandingCachedArticles();
                    Toast.makeText(magazineFlipArticlesFragment.getActivity(), magazineFlipArticlesFragment.getActivity().getResources().getString(R.string.connectivity_network_settings), Toast.LENGTH_LONG).show();
                }
            });
        }

    }

    public void removeReadArticleIds(Context context, final PreferenceEndPoint preferenceEndPoint) {
        String userId = preferenceEndPoint.getStringPreference(Constants.USER_ID);
        if (context != null) {
            SharedPreferences.Editor editor = MagazinePreferenceEndPoint.getInstance().get(context, userId);
            editor.remove("read_article_ids");
            editor.commit();
            String readCachedIds = MagazinePreferenceEndPoint.getInstance().getPref(context, userId).getString("read_article_ids", "");
            Log.d("MagazineDashboardHelper", "After removing read_article_ids key " + readCachedIds);
        }
    }

    public void getMoreDashboardArticlesAfterFollow(final MagazineFlipArticlesFragment magazineFlipArticlesFragment, YoApi.YoService yoService, final PreferenceEndPoint preferenceEndPoint, List<String> readArticleIds, List<String> unreadArticleIds, final List<Articles> unreadOtherFollowedArticles, final List<Articles> followedArticlesList) {
        if (magazineFlipArticlesFragment != null) {
            String accessToken = preferenceEndPoint.getStringPreference("access_token");
            boolean autoRenewalSubscription = preferenceEndPoint.getBooleanPreference(Constants.AUTO_RENEWAL_SUBSCRIPTION, false);
            yoService.getDashboardArticlesAPI(accessToken, readArticleIds, unreadArticleIds, autoRenewalSubscription, false).enqueue(new Callback<LandingArticles>() {
                @Override
                public void onResponse(Call<LandingArticles> call, Response<LandingArticles> response) {
                    magazineFlipArticlesFragment.tvProgressText.setVisibility(View.GONE);
                    request++;

                    if (response.body() != null) {
                        List<Articles> totalArticles = getTotalArticles(response.body());
                        showDialog(response.body().getCode(), DASHBOARD_ARTICLES_AFTER_FOLLOW, preferenceEndPoint, magazineFlipArticlesFragment, totalArticles, null, null);
                    } else {
                        magazineFlipArticlesFragment.flipContainer.setVisibility(View.VISIBLE);
                        magazineFlipArticlesFragment.llNoArticles.setVisibility(View.GONE);
                        magazineFlipArticlesFragment.getLandingCachedArticles();
                    }
                }

                @Override
                public void onFailure(Call<LandingArticles> call, Throwable t) {
                    if (magazineFlipArticlesFragment.mProgress != null) {
                        magazineFlipArticlesFragment.mProgress.setVisibility(View.GONE);
                    }
                    magazineFlipArticlesFragment.tvProgressText.setVisibility(View.GONE);
                    magazineFlipArticlesFragment.flipContainer.setVisibility(View.VISIBLE);
                    magazineFlipArticlesFragment.llNoArticles.setVisibility(View.GONE);
                    magazineFlipArticlesFragment.getLandingCachedArticles();
                    if (magazineFlipArticlesFragment.getActivity() != null) {
                        Toast.makeText(magazineFlipArticlesFragment.getActivity(), magazineFlipArticlesFragment.getActivity().getResources().getString(R.string.connectivity_network_settings), Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    public void getDashboardArticlesForDailyService(final MagazineFlipArticlesFragment magazineFlipArticlesFragment, YoApi.YoService yoService, final PreferenceEndPoint preferenceEndPoint, List<String> readArticleIds, List<String> unreadArticleIds, final List<Articles> unreadOtherFollowedArticles) {
        if (magazineFlipArticlesFragment != null) {
            String accessToken = preferenceEndPoint.getStringPreference("access_token");
            boolean autoRenewalSubscription = preferenceEndPoint.getBooleanPreference(Constants.AUTO_RENEWAL_SUBSCRIPTION, false);
            yoService.getDashboardArticlesAPI(accessToken, readArticleIds, unreadArticleIds, autoRenewalSubscription, false).enqueue(new Callback<LandingArticles>() {
                @Override
                public void onResponse(Call<LandingArticles> call, Response<LandingArticles> response) {
                    if (magazineFlipArticlesFragment.mProgress != null) {
                        magazineFlipArticlesFragment.mProgress.setVisibility(View.GONE);
                    }
                    magazineFlipArticlesFragment.tvProgressText.setVisibility(View.GONE);
                    request++;

                    if (response.body() != null) {
                        List<Articles> totalArticles = getTotalArticles(response.body());
                        showDialog(response.body().getCode(), DASHBOARD_ARTICLES_DAILY_SERVICE, preferenceEndPoint, magazineFlipArticlesFragment, totalArticles, null, null);
                    } else {
                        magazineFlipArticlesFragment.flipContainer.setVisibility(View.VISIBLE);
                        magazineFlipArticlesFragment.llNoArticles.setVisibility(View.GONE);
                        magazineFlipArticlesFragment.getLandingCachedArticles();
                    }

                }

                @Override
                public void onFailure(Call<LandingArticles> call, Throwable t) {
                    if (magazineFlipArticlesFragment.mProgress != null) {
                        magazineFlipArticlesFragment.mProgress.setVisibility(View.GONE);
                    }
                    magazineFlipArticlesFragment.tvProgressText.setVisibility(View.GONE);
                    magazineFlipArticlesFragment.flipContainer.setVisibility(View.VISIBLE);
                    magazineFlipArticlesFragment.llNoArticles.setVisibility(View.GONE);
                    magazineFlipArticlesFragment.getLandingCachedArticles();
                    Toast.makeText(magazineFlipArticlesFragment.getActivity(), magazineFlipArticlesFragment.getActivity().getResources().getString(R.string.connectivity_network_settings), Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    public void removeArticlesFromCache(Context context, final PreferenceEndPoint preferenceEndPoint, String key) {
        String userId = preferenceEndPoint.getStringPreference(Constants.USER_ID);
        if (context != null) {
            SharedPreferences.Editor editor = MagazinePreferenceEndPoint.getInstance().get(context, userId);
            editor.remove(key);
            editor.commit();
            String cachedMagazines = MagazinePreferenceEndPoint.getInstance().getPref(context, userId).getString(key, "");
            Log.d("MagazineDashboardHelper", "After removing " + key + " key " + cachedMagazines);
        }
    }

    private List<Articles> getTotalArticles(LandingArticles landingArticles) {
        LinkedHashSet<LandingArticles> articlesHashSet = new LinkedHashSet<>();
        List<Articles> followedTopicArticles;
        List<Articles> randomTopicArticles;
        List<Articles> totalArticles;

        articlesHashSet.add(landingArticles);
        Iterator<LandingArticles> itr = articlesHashSet.iterator();
        LandingArticles tempList = new LandingArticles();
        while (itr.hasNext()) {
            tempList = itr.next();
        }
        LandingArticles articlesList = tempList;
        followedTopicArticles = articlesList.getFollowed_topic_articles();
        randomTopicArticles = articlesList.getRandom_articles();
        totalArticles = followedTopicArticles;
        totalArticles.addAll(randomTopicArticles);
        return totalArticles;
    }

    private void showDialog(int errorCode, int id, PreferenceEndPoint preferenceEndPoint, MagazineFlipArticlesFragment magazineFlipArticlesFragment, List<Articles> totalArticles, final List<Articles> unreadOtherFollowedArticles, List<Articles> followedArticlesList) {
        Activity activity = magazineFlipArticlesFragment.getActivity();
        if (totalArticles != null) {
            switch (errorCode) {
                case 200:
                    preferenceEndPoint.saveBooleanPreference(Constants.RENEWAL, true);
                    if (magazineFlipArticlesFragment.getActivity() != null) {
                        removeReadIds(totalArticles, magazineFlipArticlesFragment.getActivity(), preferenceEndPoint);
                    }
                    addArticles(id, magazineFlipArticlesFragment, totalArticles, unreadOtherFollowedArticles, followedArticlesList);
                    break;
                case 401:
                case 403:
                    preferenceEndPoint.saveBooleanPreference(Constants.RENEWAL, false);
                    if (id != DASHBOARD_ARTICLES_DAILY_SERVICE) {
                        YODialogs.renewMagazine(activity, magazineFlipArticlesFragment, activity.getString(R.string.renewal_message), preferenceEndPoint);
                    }
                    removeArticlesFromCache(activity, preferenceEndPoint, "followed_cached_magazines");
                    removeArticlesFromCache(activity, preferenceEndPoint, "random_cached_magazines");
                    if (magazineFlipArticlesFragment.mProgress != null) {
                        magazineFlipArticlesFragment.mProgress.setVisibility(View.GONE);
                    }
                    magazineFlipArticlesFragment.tvProgressText.setVisibility(View.GONE);
                    magazineFlipArticlesFragment.flipContainer.setVisibility(View.GONE);
                    magazineFlipArticlesFragment.llNoArticles.setVisibility(View.VISIBLE);
                    break;
                case 405:
                    preferenceEndPoint.saveBooleanPreference(Constants.RENEWAL, false);
                    if (id != DASHBOARD_ARTICLES_DAILY_SERVICE) {
                        YODialogs.addBalance(activity, activity.getString(R.string.no_sufficient_bal_wallet));
                    }
                    break;
                default:
                    if (magazineFlipArticlesFragment.mProgress != null) {
                        magazineFlipArticlesFragment.mProgress.setVisibility(View.GONE);
                    }
                    magazineFlipArticlesFragment.tvProgressText.setVisibility(View.GONE);
                    magazineFlipArticlesFragment.flipContainer.setVisibility(View.VISIBLE);
                    magazineFlipArticlesFragment.getLandingCachedArticles();
                    break;
            }
        } else {
            magazineFlipArticlesFragment.flipContainer.setVisibility(View.VISIBLE);
            magazineFlipArticlesFragment.llNoArticles.setVisibility(View.GONE);
            magazineFlipArticlesFragment.getLandingCachedArticles();
        }
    }

    private void addArticles(int id, MagazineFlipArticlesFragment articlesFragment, List<Articles> totalArticles, List<Articles> unreadOtherFollowedArticles, final List<Articles> followedArticlesList) {
        switch (id) {
            case DASHBOARD_ARTICLES:
                addDashboardArticlesList(articlesFragment, totalArticles);
                break;
            case MORE_DASHBOARD_ARTICLES:
                addMoreDashboardArticlesList(articlesFragment, totalArticles);
                break;
            case DASHBOARD_ARTICLES_AFTER_FOLLOW:
                addMoreDashboardArticlesAfterFollow(articlesFragment, totalArticles, unreadOtherFollowedArticles, followedArticlesList);
                break;
            case DASHBOARD_ARTICLES_DAILY_SERVICE:
                addDashboardArticlesForDailyService(articlesFragment, totalArticles, unreadOtherFollowedArticles);
                break;
        }
    }

    private void addDashboardArticlesList(MagazineFlipArticlesFragment articlesFragment, List<Articles> totalArticles) {
        articlesFragment.myBaseAdapter.addItems(totalArticles);
        articlesFragment.handleDashboardResponse(totalArticles);
    }

    private void addMoreDashboardArticlesList(MagazineFlipArticlesFragment articlesFragment, List<Articles> totalArticles) {
        articlesFragment.myBaseAdapter.addItemsAll(totalArticles);
        articlesFragment.handleMoreDashboardResponse(totalArticles, false);
    }

    private void addMoreDashboardArticlesAfterFollow(MagazineFlipArticlesFragment articlesFragment, List<Articles> totalArticles, List<Articles> unreadOtherFollowedArticles, final List<Articles> followedArticlesList) {
        articlesFragment.performSortingAfterFollow(totalArticles, unreadOtherFollowedArticles, followedArticlesList);
        articlesFragment.handleMoreDashboardResponse(totalArticles, true);
    }

    private void addDashboardArticlesForDailyService(MagazineFlipArticlesFragment articlesFragment, List<Articles> totalArticles, List<Articles> unreadOtherFollowedArticles) {
        articlesFragment.performSortingAfterDailyService(totalArticles, unreadOtherFollowedArticles);
        articlesFragment.handleMoreDashboardResponse(totalArticles, false);
    }
}