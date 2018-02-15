package com.yo.android.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.StringRes;
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
import com.yo.android.usecase.MagazinesFlipArticlesUsecase;

import java.lang.reflect.Type;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MagazineDashboardHelper {

    public static int currentReadArticles;
    public static int request;
    private static final int DASHBOARD_ARTICLES = 1;
    private static final int MORE_DASHBOARD_ARTICLES = 2;
    private static final int DASHBOARD_ARTICLES_AFTER_FOLLOW = 3;
    private static final int DASHBOARD_ARTICLES_DAILY_SERVICE = 4;
    @Inject
    public MagazinesFlipArticlesUsecase magazinesFlipArticlesUsecase;

    /**
     * Gets the dashboard articles
     *
     * @param magazineFlipArticlesFragment The MagazineFlipArticlesFragment object
     * @param yoService                    The YoService object
     * @param preferenceEndPoint           The PreferenceEndPoint object
     * @param readArticleIds               The list of read articles ids
     * @param unreadArticleIds             The list of unread article ids
     */

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
                        try {
                            List<Articles> totalArticles = getTotalArticles(response.body());
                            List<Articles> totalArticlesWithSummary = new ArrayList<Articles>();
                            for (Articles articles : totalArticles) {
                                if (!"...".equalsIgnoreCase(articles.getSummary())) {
                                    totalArticlesWithSummary.add(articles);
                                }
                            }
                            showDialog(response.body().getCode(), DASHBOARD_ARTICLES, preferenceEndPoint, magazineFlipArticlesFragment, totalArticlesWithSummary, null, null);
                        } finally {
                            if (response != null && response.body() != null) {
                                try {
                                    response = null;
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    } else {
                        magazineFlipArticlesFragment.flipContainer.setVisibility(View.VISIBLE);
                        magazineFlipArticlesFragment.llNoArticles.setVisibility(View.GONE);
                        if(magazineFlipArticlesFragment.magazinesFlipArticlesUsecase != null) {
                            magazineFlipArticlesFragment.magazinesFlipArticlesUsecase.getLandingCachedArticles(magazineFlipArticlesFragment.getActivity(), magazineFlipArticlesFragment.myBaseAdapter, magazineFlipArticlesFragment, MagazineDashboardHelper.this);}
                    }

                }

                @Override
                public void onFailure(Call<LandingArticles> call, Throwable t) {
                    if (magazineFlipArticlesFragment.mProgress != null) {
                        magazineFlipArticlesFragment.mProgress.setVisibility(View.GONE);
                    }
                    magazineFlipArticlesFragment.tvProgressText.setVisibility(View.GONE);
                    magazineFlipArticlesFragment.flipContainer.setVisibility(View.GONE);
                    if (t != null && t.getMessage() != null && t.getMessage().equalsIgnoreCase("timeout")) {
                        magazineFlipArticlesFragment.serverFailureText.setVisibility(View.VISIBLE);
                    } else {
                        magazineFlipArticlesFragment.networkFailureText.setVisibility(View.VISIBLE);
                    }
                }
            });
        }

    }

    /**
     * Removes the read articles
     *
     * @param totalArticles      The list of total articles
     * @param context            The Context
     * @param preferenceEndPoint The PreferenceEndPoint object
     * @return The list of unread articles
     */
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

    /**
     * Gets the next page of Dashboard articles
     *
     * @param magazineFlipArticlesFragment The MagazineFlipArticlesFragment object
     * @param yoService                    The YoService object
     * @param preferenceEndPoint           The PreferenceEndPoint object
     * @param readArticleIds               The list of read articles ids
     * @param unreadArticleIds             The list of unread article ids
     */
    public void getMoreDashboardArticles(final MagazineFlipArticlesFragment magazineFlipArticlesFragment, YoApi.YoService yoService, final PreferenceEndPoint preferenceEndPoint, List<String> readArticleIds, List<String> unreadArticleIds, final SwipeRefreshLayout swipeRefreshContainer) {
        if (magazineFlipArticlesFragment != null) {
            String accessToken = preferenceEndPoint.getStringPreference("access_token");
            boolean autoRenewalSubscription = preferenceEndPoint.getBooleanPreference(Constants.AUTO_RENEWAL_SUBSCRIPTION, false);
            yoService.getDashboardArticlesAPI(accessToken, readArticleIds, unreadArticleIds, autoRenewalSubscription, false).enqueue(new Callback<LandingArticles>() {
                @Override
                public void onResponse(Call<LandingArticles> call, Response<LandingArticles> response) {
                    MagazineFlipArticlesFragment.refreshing = false;

                    if (swipeRefreshContainer != null) {
                        swipeRefreshContainer.setRefreshing(false);
                    }
                    if (magazineFlipArticlesFragment.mProgress != null) {
                        magazineFlipArticlesFragment.mProgress.setVisibility(View.GONE);
                    }
                    magazineFlipArticlesFragment.tvProgressText.setVisibility(View.GONE);
                    request++;

                    if (response.body() != null) {
                        try {
                            List<Articles> totalArticles = getTotalArticles(response.body());
                            List<Articles> totalArticlesWithSummary = new ArrayList<Articles>();
                            for (Articles articles : totalArticles) {
                                if (!"...".equalsIgnoreCase(articles.getSummary())) {
                                    totalArticlesWithSummary.add(articles);
                                }
                            }
                            showDialog(response.body().getCode(), MORE_DASHBOARD_ARTICLES, preferenceEndPoint, magazineFlipArticlesFragment, totalArticlesWithSummary, null, null);
                        } finally {
                            if (response != null && response.body() != null) {
                                try {
                                    response = null;
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    } else {
                        magazineFlipArticlesFragment.flipContainer.setVisibility(View.VISIBLE);
                        magazineFlipArticlesFragment.llNoArticles.setVisibility(View.GONE);
                        if(magazineFlipArticlesFragment.magazinesFlipArticlesUsecase != null) {
                            magazineFlipArticlesFragment.magazinesFlipArticlesUsecase.getLandingCachedArticles(magazineFlipArticlesFragment.getActivity(), magazineFlipArticlesFragment.myBaseAdapter, magazineFlipArticlesFragment, MagazineDashboardHelper.this);
                        }
                    }

                }

                @Override
                public void onFailure(Call<LandingArticles> call, Throwable t) {
                    MagazineFlipArticlesFragment.refreshing = false;
                    if (swipeRefreshContainer != null) {
                        swipeRefreshContainer.setRefreshing(false);
                    }

                    if (magazineFlipArticlesFragment.mProgress != null) {
                        magazineFlipArticlesFragment.mProgress.setVisibility(View.GONE);
                    }
                    magazineFlipArticlesFragment.tvProgressText.setVisibility(View.GONE);
                    magazineFlipArticlesFragment.flipContainer.setVisibility(View.VISIBLE);
                    magazineFlipArticlesFragment.llNoArticles.setVisibility(View.GONE);
                    if(magazineFlipArticlesFragment.magazinesFlipArticlesUsecase != null) {
                        magazineFlipArticlesFragment.magazinesFlipArticlesUsecase.getLandingCachedArticles(magazineFlipArticlesFragment.getActivity(), magazineFlipArticlesFragment.myBaseAdapter, magazineFlipArticlesFragment, MagazineDashboardHelper.this);
                    }
                    if(t instanceof SocketTimeoutException) {
                        Toast.makeText(magazineFlipArticlesFragment.getActivity(), magazineFlipArticlesFragment.getActivity().getResources().getString(R.string.socket_time_out), Toast.LENGTH_LONG).show();
                    } else if (magazineFlipArticlesFragment.getActivity() != null) {
                        Toast.makeText(magazineFlipArticlesFragment.getActivity(), magazineFlipArticlesFragment.getActivity().getResources().getString(R.string.connectivity_network_settings), Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

    }

    /**
     * Removes the read article ids and stores the remaining unread article ids
     *
     * @param context            The Context
     * @param preferenceEndPoint The PreferenceEndPoint object
     */
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

    /**
     * Gets more Dashboard articles after following a topic
     *
     * @param magazineFlipArticlesFragment The MagazineFlipArticlesFragment object
     * @param yoService                    The YoService object
     * @param preferenceEndPoint           The PreferenceEndPoint object
     * @param readArticleIds               The list of read articles ids
     * @param unreadArticleIds             The list of unread article ids
     * @param unreadOtherFollowedArticles  The unread other followed articles list
     * @param followedArticlesList         The followed articles list
     */
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
                        try {
                            List<Articles> totalArticles = getTotalArticles(response.body());
                            List<Articles> totalArticlesWithSummary = new ArrayList<Articles>();
                            for (Articles articles : totalArticles) {
                                if (!"...".equalsIgnoreCase(articles.getSummary())) {
                                    totalArticlesWithSummary.add(articles);
                                }
                            }
                            showDialog(response.body().getCode(), DASHBOARD_ARTICLES_AFTER_FOLLOW, preferenceEndPoint, magazineFlipArticlesFragment, totalArticlesWithSummary, null, null);
                        } finally {
                            if (response != null && response.body() != null) {
                                try {
                                    response = null;
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    } else {
                        magazineFlipArticlesFragment.flipContainer.setVisibility(View.VISIBLE);
                        magazineFlipArticlesFragment.llNoArticles.setVisibility(View.GONE);
                        if(magazineFlipArticlesFragment.magazinesFlipArticlesUsecase != null) {
                            magazineFlipArticlesFragment.magazinesFlipArticlesUsecase.getLandingCachedArticles(magazineFlipArticlesFragment.getActivity(), magazineFlipArticlesFragment.myBaseAdapter, magazineFlipArticlesFragment, MagazineDashboardHelper.this);
                        }
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
                    if (magazineFlipArticlesFragment.magazinesFlipArticlesUsecase != null) {
                        magazineFlipArticlesFragment.magazinesFlipArticlesUsecase.getLandingCachedArticles(magazineFlipArticlesFragment.getActivity(), magazineFlipArticlesFragment.myBaseAdapter, magazineFlipArticlesFragment, MagazineDashboardHelper.this);
                    }
                    if (magazineFlipArticlesFragment.getActivity() != null) {
                        Toast.makeText(magazineFlipArticlesFragment.getActivity(), magazineFlipArticlesFragment.getActivity().getResources().getString(R.string.connectivity_network_settings), Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }


    /**
     * Gets the latest Dashboard articles once a day
     *
     * @param magazineFlipArticlesFragment The MagazineFlipArticlesFragment object
     * @param yoService                    The YoService object
     * @param preferenceEndPoint           The PreferenceEndPoint object
     * @param readArticleIds               The list of read articles ids
     * @param unreadArticleIds             The list of unread article ids
     */
    public void getDashboardArticlesForDailyService(final MagazineFlipArticlesFragment magazineFlipArticlesFragment, YoApi.YoService yoService, final PreferenceEndPoint preferenceEndPoint, List<String> readArticleIds, List<String> unreadArticleIds, final SwipeRefreshLayout swipeRefreshContainer) {
        if (magazineFlipArticlesFragment != null) {
            String accessToken = preferenceEndPoint.getStringPreference("access_token");
            boolean autoRenewalSubscription = preferenceEndPoint.getBooleanPreference(Constants.AUTO_RENEWAL_SUBSCRIPTION, false);
            yoService.getDashboardArticlesAPI(accessToken, readArticleIds, unreadArticleIds, autoRenewalSubscription, false).enqueue(new Callback<LandingArticles>() {
                @Override
                public void onResponse(Call<LandingArticles> call, Response<LandingArticles> response) {
                    MagazineFlipArticlesFragment.refreshing = false;

                    if (swipeRefreshContainer != null) {
                        swipeRefreshContainer.setRefreshing(false);
                    }
                    if (magazineFlipArticlesFragment.mProgress != null) {
                        magazineFlipArticlesFragment.mProgress.setVisibility(View.GONE);
                    }
                    magazineFlipArticlesFragment.tvProgressText.setVisibility(View.GONE);
                    request++;

                    if (response.body() != null) {
                        try {
                            List<Articles> totalArticles = getTotalArticles(response.body());
                            List<Articles> totalArticlesWithSummary = new ArrayList<Articles>();
                            for (Articles articles : totalArticles) {
                                if (!"...".equalsIgnoreCase(articles.getSummary())) {
                                    totalArticlesWithSummary.add(articles);
                                }
                            }
                            showDialog(response.body().getCode(), DASHBOARD_ARTICLES_DAILY_SERVICE, preferenceEndPoint, magazineFlipArticlesFragment, totalArticlesWithSummary, null, null);
                        } finally {
                            if (response != null && response.body() != null) {
                                try {
                                    response = null;
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    } else {
                        magazineFlipArticlesFragment.flipContainer.setVisibility(View.VISIBLE);
                        magazineFlipArticlesFragment.llNoArticles.setVisibility(View.GONE);
                        if(magazineFlipArticlesFragment.magazinesFlipArticlesUsecase != null) {
                            magazineFlipArticlesFragment.magazinesFlipArticlesUsecase.getLandingCachedArticles(magazineFlipArticlesFragment.getActivity(), magazineFlipArticlesFragment.myBaseAdapter, magazineFlipArticlesFragment, MagazineDashboardHelper.this);
                        }
                    }

                }

                @Override
                public void onFailure(Call<LandingArticles> call, Throwable t) {
                    MagazineFlipArticlesFragment.refreshing = false;
                    if (swipeRefreshContainer != null) {
                        swipeRefreshContainer.setRefreshing(false);
                    }
                    if (magazineFlipArticlesFragment.mProgress != null) {
                        magazineFlipArticlesFragment.mProgress.setVisibility(View.GONE);
                    }
                    magazineFlipArticlesFragment.tvProgressText.setVisibility(View.GONE);
                    magazineFlipArticlesFragment.flipContainer.setVisibility(View.VISIBLE);
                    magazineFlipArticlesFragment.llNoArticles.setVisibility(View.GONE);
                    if(magazineFlipArticlesFragment.magazinesFlipArticlesUsecase != null) {
                        magazineFlipArticlesFragment.magazinesFlipArticlesUsecase.getLandingCachedArticles(magazineFlipArticlesFragment.getActivity(), magazineFlipArticlesFragment.myBaseAdapter, magazineFlipArticlesFragment, MagazineDashboardHelper.this);
                    }
                    if(t instanceof SocketTimeoutException) {
                        Toast.makeText(magazineFlipArticlesFragment.getActivity(), magazineFlipArticlesFragment.getActivity().getResources().getString(R.string.socket_time_out), Toast.LENGTH_LONG).show();
                    } else if (magazineFlipArticlesFragment.getActivity() != null) {
                        Toast.makeText(magazineFlipArticlesFragment.getActivity(), magazineFlipArticlesFragment.getActivity().getResources().getString(R.string.connectivity_network_settings), Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    /**
     * Removes the particular key from the cache
     *
     * @param context            The Context
     * @param preferenceEndPoint The PreferenceEndPoint object
     * @param key                The key
     */
    public static void removeArticlesFromCache(Context context, final PreferenceEndPoint preferenceEndPoint, String key) {
        String userId = preferenceEndPoint.getStringPreference(Constants.USER_ID);
        if (context != null) {
            SharedPreferences.Editor editor = MagazinePreferenceEndPoint.getInstance().get(context, userId);
            editor.remove(key);
            editor.commit();
            String cachedMagazines = MagazinePreferenceEndPoint.getInstance().getPref(context, userId).getString(key, "");
            Log.d("MagazineDashboardHelper", "After removing " + key + " key " + cachedMagazines);
        }
    }

    /**
     * The total list of articles containing the followed topic articles and random topic articles
     * @param landingArticles The LandingArticles list
     * @return The list of followed topic articles and random topic articles
     */
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
                    preferenceEndPoint.saveBooleanPreference(Constants.RENEWAL, false);
                    if (magazineFlipArticlesFragment.getActivity() != null) {
                        removeReadIds(totalArticles, magazineFlipArticlesFragment.getActivity(), preferenceEndPoint);
                    }
                    addArticles(id, magazineFlipArticlesFragment, totalArticles, unreadOtherFollowedArticles, followedArticlesList);
                    break;
                case 401: // auto renewal failed. Please try again
                case 403: // You should do auto renewal to access Magazines
                    renewMagazines(R.string.renewal_message, activity, id, preferenceEndPoint, magazineFlipArticlesFragment, totalArticles, unreadOtherFollowedArticles, followedArticlesList);
                    break;
                case 702:
                    renewMagazines(R.string.renewal_error_message, activity, id, preferenceEndPoint, magazineFlipArticlesFragment, totalArticles, unreadOtherFollowedArticles, followedArticlesList);
                    break;
                // no sufficient balance in wallet
                case 405:
                    preferenceEndPoint.saveBooleanPreference(Constants.RENEWAL, false);
                    if (id != DASHBOARD_ARTICLES_DAILY_SERVICE) {
                        YODialogs.addBalance(activity, activity.getString(R.string.no_sufficient_bal_wallet), preferenceEndPoint);
                    }
                    magazineFlipArticlesFragment.llNoArticles.setVisibility(View.VISIBLE);
                    break;
                default:
                    if (magazineFlipArticlesFragment.mProgress != null) {
                        magazineFlipArticlesFragment.mProgress.setVisibility(View.GONE);
                    }
                    magazineFlipArticlesFragment.tvProgressText.setVisibility(View.GONE);
                    magazineFlipArticlesFragment.flipContainer.setVisibility(View.VISIBLE);
                    if(magazineFlipArticlesFragment.magazinesFlipArticlesUsecase != null) {
                        magazineFlipArticlesFragment.magazinesFlipArticlesUsecase.getLandingCachedArticles(magazineFlipArticlesFragment.getActivity(), magazineFlipArticlesFragment.myBaseAdapter, magazineFlipArticlesFragment, MagazineDashboardHelper.this);
                    }
                    break;
            }
        } else {
            magazineFlipArticlesFragment.flipContainer.setVisibility(View.VISIBLE);
            magazineFlipArticlesFragment.llNoArticles.setVisibility(View.GONE);
            if(magazineFlipArticlesFragment.magazinesFlipArticlesUsecase != null) {
                magazineFlipArticlesFragment.magazinesFlipArticlesUsecase.getLandingCachedArticles(magazineFlipArticlesFragment.getActivity(), magazineFlipArticlesFragment.myBaseAdapter, magazineFlipArticlesFragment, MagazineDashboardHelper.this);
            }
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
        articlesFragment.handleMoreDashboardResponse(totalArticles, false, false);
    }

    private void addMoreDashboardArticlesAfterFollow(MagazineFlipArticlesFragment articlesFragment, List<Articles> totalArticles, List<Articles> unreadOtherFollowedArticles, final List<Articles> followedArticlesList) {
        if(articlesFragment.magazinesFlipArticlesUsecase != null) {
            articlesFragment.magazinesFlipArticlesUsecase.performSortingAfterFollow(totalArticles, unreadOtherFollowedArticles, followedArticlesList, articlesFragment.myBaseAdapter);
        }
        articlesFragment.handleMoreDashboardResponse(totalArticles, true, false);
    }

    private void addDashboardArticlesForDailyService(MagazineFlipArticlesFragment articlesFragment, List<Articles> totalArticles, List<Articles> unreadOtherFollowedArticles) {
        if(articlesFragment.magazinesFlipArticlesUsecase != null) {
            articlesFragment.magazinesFlipArticlesUsecase.performSortingAfterDailyService(totalArticles, unreadOtherFollowedArticles, articlesFragment.myBaseAdapter, articlesFragment.getActivity(), articlesFragment, MagazineDashboardHelper.this);
        }
        //articlesFragment.handleMoreDashboardResponse(totalArticles, false);
    }

    private void renewMagazines(@StringRes int message, Activity activity, int id, PreferenceEndPoint preferenceEndPoint, MagazineFlipArticlesFragment magazineFlipArticlesFragment, List<Articles> totalArticles, final List<Articles> unreadOtherFollowedArticles, List<Articles> followedArticlesList) {
        preferenceEndPoint.saveBooleanPreference(Constants.RENEWAL, true);
        if (id != DASHBOARD_ARTICLES_DAILY_SERVICE) {
            YODialogs.renewMagazine(activity, magazineFlipArticlesFragment, message, preferenceEndPoint);
        }
        removeArticlesFromCache(activity, preferenceEndPoint, "followed_cached_magazines");
        removeArticlesFromCache(activity, preferenceEndPoint, "random_cached_magazines");
        if (magazineFlipArticlesFragment.mProgress != null) {
            magazineFlipArticlesFragment.mProgress.setVisibility(View.GONE);
        }
        magazineFlipArticlesFragment.tvProgressText.setVisibility(View.GONE);
        magazineFlipArticlesFragment.flipContainer.setVisibility(View.GONE);
        magazineFlipArticlesFragment.llNoArticles.setVisibility(View.VISIBLE);
    }
}