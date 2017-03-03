package com.yo.android.util;

import android.content.Context;
import android.content.SharedPreferences;
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
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by creatives on 12/8/2016.
 */
public class MagazineDashboardHelper {

    public static int currentReadArticles;
    public static int request;

    /*public static void getDashboardArticles(final MagazineFlipArticlesFragment magazineFlipArticlesFragment, YoApi.YoService yoService, PreferenceEndPoint preferenceEndPoint, List<String> readArticleIds, List<String> unreadArticleIds) {

        String accessToken = preferenceEndPoint.getStringPreference("access_token");
     yoService.getDashboardArticlesAPI(accessToken,readArticleIds,unreadArticleIds).enqueue(new Callback<List<LandingArticles>>() {
         @Override
         public void onResponse(Call<List<LandingArticles>> call, Response<List<LandingArticles>> response) {
             if(magazineFlipArticlesFragment.mProgress != null) {
                 magazineFlipArticlesFragment.mProgress.setVisibility(View.GONE);
             }
             magazineFlipArticlesFragment.tvProgressText.setVisibility(View.GONE);

             *//*for(LandingArticles res : response.body()) {

             }*//*

         }

         @Override
         public void onFailure(Call<List<LandingArticles>> call, Throwable t) {
             if(magazineFlipArticlesFragment.mProgress != null) {
                 magazineFlipArticlesFragment.mProgress.setVisibility(View.GONE);
             }
             magazineFlipArticlesFragment.tvProgressText.setVisibility(View.GONE);

         }
     });

    }*/

    public void getDashboardArticles(final MagazineFlipArticlesFragment magazineFlipArticlesFragment, YoApi.YoService yoService, final PreferenceEndPoint preferenceEndPoint,List<String> readArticleIds, List<String> unreadArticleIds) {

        if(magazineFlipArticlesFragment != null) {
        String accessToken = preferenceEndPoint.getStringPreference("access_token");
        yoService.getDashboardArticlesAPI(accessToken, readArticleIds, unreadArticleIds).enqueue(new Callback<LandingArticles>() {
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
                LinkedHashSet<LandingArticles> articlesHashSet = new LinkedHashSet<>();
                if (response.body() != null) {
                    articlesHashSet.add(response.body());
                    Iterator<LandingArticles> itr = articlesHashSet.iterator();
                    LandingArticles tempList = new LandingArticles();
                    while (itr.hasNext()) {
                        tempList = itr.next();
                    }
                    LandingArticles articlesList = tempList;
                    List<Articles> followedTopicArticles = new ArrayList<Articles>();
                    List<Articles> randomTopicArticles = new ArrayList<Articles>();
                    followedTopicArticles = articlesList.getFollowed_topic_articles();
                    randomTopicArticles = articlesList.getRandom_articles();
                    List<Articles> totalArticles = new ArrayList<Articles>();
                    totalArticles = followedTopicArticles;
                    totalArticles.addAll(randomTopicArticles);

                    if(magazineFlipArticlesFragment.getActivity() != null) {
                        removeReadIds(totalArticles, magazineFlipArticlesFragment.getActivity(), preferenceEndPoint);
                    }

                    magazineFlipArticlesFragment.myBaseAdapter.addItems(totalArticles);
                    magazineFlipArticlesFragment.handleDashboardResponse(totalArticles);

                } else {
                    magazineFlipArticlesFragment.flipContainer.setVisibility(View.VISIBLE);
                    magazineFlipArticlesFragment.llNoArticles.setVisibility(View.GONE);
                    //getCachedArticles();
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
                //magazineFlipArticlesFragment.llNoArticles.setVisibility(View.VISIBLE);
                magazineFlipArticlesFragment.networkFailureText.setVisibility(View.VISIBLE);

            }
        });
    }

    }

    public List<Articles> removeReadIds(List<Articles> totalArticles, Context context, final PreferenceEndPoint preferenceEndPoint) {
        List<Articles> tempArticlesList = new ArrayList<Articles>(totalArticles);
        String userId = preferenceEndPoint.getStringPreference(Constants.USER_ID);
        if(context != null) {
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

    public void getMoreDashboardArticles(final MagazineFlipArticlesFragment magazineFlipArticlesFragment, YoApi.YoService yoService, final PreferenceEndPoint preferenceEndPoint,List<String> readArticleIds, List<String> unreadArticleIds) {
        if(magazineFlipArticlesFragment != null) {
        String accessToken = preferenceEndPoint.getStringPreference("access_token");
        yoService.getDashboardArticlesAPI(accessToken, readArticleIds, unreadArticleIds).enqueue(new Callback<LandingArticles>() {
            @Override
            public void onResponse(Call<LandingArticles> call, Response<LandingArticles> response) {

                /*if (!isAdded()) {
                    return;
                }
                myBaseAdapter.clear();*/
                if (magazineFlipArticlesFragment.mProgress != null) {
                    magazineFlipArticlesFragment.mProgress.setVisibility(View.GONE);
                }
                magazineFlipArticlesFragment.tvProgressText.setVisibility(View.GONE);
                request++;

                LinkedHashSet<LandingArticles> articlesHashSet = new LinkedHashSet<>();
                if (response.body() != null) {
                    articlesHashSet.add(response.body());
                    Iterator<LandingArticles> itr = articlesHashSet.iterator();
                    LandingArticles tempList = new LandingArticles();
                    while (itr.hasNext()) {
                        tempList = itr.next();
                    }
                    LandingArticles articlesList = tempList;
                    List<Articles> followedTopicArticles = new ArrayList<Articles>();
                    List<Articles> randomTopicArticles = new ArrayList<Articles>();
                    followedTopicArticles = articlesList.getFollowed_topic_articles();
                    randomTopicArticles = articlesList.getRandom_articles();
                    List<Articles> totalArticles = new ArrayList<Articles>();
                    totalArticles = followedTopicArticles;
                    totalArticles.addAll(randomTopicArticles);

                    if(magazineFlipArticlesFragment.getActivity() != null) {
                        removeReadIds(totalArticles, magazineFlipArticlesFragment.getActivity(), preferenceEndPoint);
                    }

                    magazineFlipArticlesFragment.myBaseAdapter.addItemsAll(totalArticles);
                    magazineFlipArticlesFragment.handleMoreDashboardResponse(totalArticles, false);


                } else {
                    magazineFlipArticlesFragment.flipContainer.setVisibility(View.VISIBLE);
                    magazineFlipArticlesFragment.llNoArticles.setVisibility(View.GONE);
                    //getCachedArticles();
                    magazineFlipArticlesFragment.getLandingCachedArticles();
                }

            }

            @Override
            public void onFailure(Call<LandingArticles> call, Throwable t) {
                if (magazineFlipArticlesFragment.mProgress != null) {
                    magazineFlipArticlesFragment.mProgress.setVisibility(View.GONE);
                }
                magazineFlipArticlesFragment.tvProgressText.setVisibility(View.GONE);
               /* magazineFlipArticlesFragment.flipContainer.setVisibility(View.GONE);
                //magazineFlipArticlesFragment.llNoArticles.setVisibility(View.VISIBLE);
                magazineFlipArticlesFragment.networkFailureText.setVisibility(View.VISIBLE);*/
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
        if(context != null) {
            SharedPreferences.Editor editor = MagazinePreferenceEndPoint.getInstance().get(context, userId);
            editor.remove("read_article_ids");
            editor.commit();
            String readCachedIds = MagazinePreferenceEndPoint.getInstance().getPref(context, userId).getString("read_article_ids", "");
            Log.d("MagazineDashboardHelper", "After removing read_article_ids key " + readCachedIds);
        }
    }

    public void getMoreDashboardArticlesAfterFollow(final MagazineFlipArticlesFragment magazineFlipArticlesFragment, YoApi.YoService yoService, final PreferenceEndPoint preferenceEndPoint, List<String> readArticleIds, List<String> unreadArticleIds, final List<Articles> unreadOtherFollowedArticles, final List<Articles> followedArticlesList) {
        if(magazineFlipArticlesFragment != null) {
            String accessToken = preferenceEndPoint.getStringPreference("access_token");
            yoService.getDashboardArticlesAPI(accessToken, readArticleIds, unreadArticleIds).enqueue(new Callback<LandingArticles>() {
                @Override
                public void onResponse(Call<LandingArticles> call, Response<LandingArticles> response) {

                    /*if (magazineFlipArticlesFragment.mProgress != null) {
                        magazineFlipArticlesFragment.mProgress.setVisibility(View.GONE);
                    }*/
                    magazineFlipArticlesFragment.tvProgressText.setVisibility(View.GONE);
                    request++;

                    LinkedHashSet<LandingArticles> articlesHashSet = new LinkedHashSet<>();
                    if (response.body() != null) {
                        articlesHashSet.add(response.body());
                        Iterator<LandingArticles> itr = articlesHashSet.iterator();
                        LandingArticles tempList = new LandingArticles();
                        while (itr.hasNext()) {
                            tempList = itr.next();
                        }
                        LandingArticles articlesList = tempList;
                        List<Articles> followedTopicArticles = new ArrayList<Articles>();
                        List<Articles> randomTopicArticles = new ArrayList<Articles>();
                        followedTopicArticles = articlesList.getFollowed_topic_articles();
                        randomTopicArticles = articlesList.getRandom_articles();
                        List<Articles> totalArticles = new ArrayList<Articles>();
                        totalArticles = followedTopicArticles;
                        totalArticles.addAll(randomTopicArticles);

                        if(magazineFlipArticlesFragment.getActivity() != null) {
                            removeReadIds(totalArticles, magazineFlipArticlesFragment.getActivity(), preferenceEndPoint);
                        }

                        magazineFlipArticlesFragment.performSortingAfterFollow(totalArticles, unreadOtherFollowedArticles, followedArticlesList);

                        //magazineFlipArticlesFragment.myBaseAdapter.addItemsAll(totalArticles);
                        magazineFlipArticlesFragment.handleMoreDashboardResponse(totalArticles, true);


                    } else {
                        magazineFlipArticlesFragment.flipContainer.setVisibility(View.VISIBLE);
                        magazineFlipArticlesFragment.llNoArticles.setVisibility(View.GONE);
                        //getCachedArticles();
                        magazineFlipArticlesFragment.getLandingCachedArticles();
                    }

                }

                @Override
                public void onFailure(Call<LandingArticles> call, Throwable t) {
                    if (magazineFlipArticlesFragment.mProgress != null) {
                        magazineFlipArticlesFragment.mProgress.setVisibility(View.GONE);
                    }
                    magazineFlipArticlesFragment.tvProgressText.setVisibility(View.GONE);
                    /*magazineFlipArticlesFragment.flipContainer.setVisibility(View.GONE);
                    //magazineFlipArticlesFragment.llNoArticles.setVisibility(View.VISIBLE);
                    magazineFlipArticlesFragment.networkFailureText.setVisibility(View.VISIBLE);*/
                    magazineFlipArticlesFragment.flipContainer.setVisibility(View.VISIBLE);
                    magazineFlipArticlesFragment.llNoArticles.setVisibility(View.GONE);
                    magazineFlipArticlesFragment.getLandingCachedArticles();
                    Toast.makeText(magazineFlipArticlesFragment.getActivity(), magazineFlipArticlesFragment.getActivity().getResources().getString(R.string.connectivity_network_settings), Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    public void getDashboardArticlesForDailyService(final MagazineFlipArticlesFragment magazineFlipArticlesFragment, YoApi.YoService yoService, final PreferenceEndPoint preferenceEndPoint, List<String> readArticleIds, List<String> unreadArticleIds, final List<Articles> unreadOtherFollowedArticles) {
        if(magazineFlipArticlesFragment != null) {
            String accessToken = preferenceEndPoint.getStringPreference("access_token");
            yoService.getDashboardArticlesAPI(accessToken, readArticleIds, unreadArticleIds).enqueue(new Callback<LandingArticles>() {
                @Override
                public void onResponse(Call<LandingArticles> call, Response<LandingArticles> response) {

                    if (magazineFlipArticlesFragment.mProgress != null) {
                        magazineFlipArticlesFragment.mProgress.setVisibility(View.GONE);
                    }
                    magazineFlipArticlesFragment.tvProgressText.setVisibility(View.GONE);
                    request++;

                    LinkedHashSet<LandingArticles> articlesHashSet = new LinkedHashSet<>();
                    if (response.body() != null) {
                        articlesHashSet.add(response.body());
                        Iterator<LandingArticles> itr = articlesHashSet.iterator();
                        LandingArticles tempList = new LandingArticles();
                        while (itr.hasNext()) {
                            tempList = itr.next();
                        }
                        LandingArticles articlesList = tempList;
                        List<Articles> followedTopicArticles = new ArrayList<Articles>();
                        List<Articles> randomTopicArticles = new ArrayList<Articles>();
                        followedTopicArticles = articlesList.getFollowed_topic_articles();
                        randomTopicArticles = articlesList.getRandom_articles();
                        List<Articles> totalArticles = new ArrayList<Articles>();
                        totalArticles = followedTopicArticles;
                        totalArticles.addAll(randomTopicArticles);

                        if(magazineFlipArticlesFragment.getActivity() != null) {
                            removeReadIds(totalArticles, magazineFlipArticlesFragment.getActivity(), preferenceEndPoint);
                        }

                        magazineFlipArticlesFragment.performSortingAfterDailyService(totalArticles, unreadOtherFollowedArticles);

                        //magazineFlipArticlesFragment.myBaseAdapter.addItemsAll(totalArticles);
                        magazineFlipArticlesFragment.handleMoreDashboardResponse(totalArticles, false);


                    } else {
                        magazineFlipArticlesFragment.flipContainer.setVisibility(View.VISIBLE);
                        magazineFlipArticlesFragment.llNoArticles.setVisibility(View.GONE);
                        //getCachedArticles();
                        magazineFlipArticlesFragment.getLandingCachedArticles();
                    }

                }

                @Override
                public void onFailure(Call<LandingArticles> call, Throwable t) {
                    if (magazineFlipArticlesFragment.mProgress != null) {
                        magazineFlipArticlesFragment.mProgress.setVisibility(View.GONE);
                    }
                    magazineFlipArticlesFragment.tvProgressText.setVisibility(View.GONE);
                    /*magazineFlipArticlesFragment.flipContainer.setVisibility(View.GONE);
                    //magazineFlipArticlesFragment.llNoArticles.setVisibility(View.VISIBLE);
                    magazineFlipArticlesFragment.networkFailureText.setVisibility(View.VISIBLE);*/
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
        if(context != null) {
            SharedPreferences.Editor editor = MagazinePreferenceEndPoint.getInstance().get(context, userId);
            editor.remove(key);
            editor.commit();
            String cachedMagazines = MagazinePreferenceEndPoint.getInstance().getPref(context, userId).getString(key, "");
            Log.d("MagazineDashboardHelper", "After removing " + key + " key " + cachedMagazines);
        }
    }
}
