package com.yo.android.usecase;

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
import com.orion.android.common.util.ConnectivityHelper;
import com.yo.android.R;
import com.yo.android.adapters.MagazineArticlesBaseAdapter;
import com.yo.android.api.YoApi;
import com.yo.android.flip.MagazineFlipArticlesFragment;
import com.yo.android.helpers.MagazinePreferenceEndPoint;
import com.yo.android.model.Articles;
import com.yo.android.util.ArticlesComparator;
import com.yo.android.util.Constants;
import com.yo.android.util.MagazineDashboardHelper;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import static com.yo.android.flip.MagazineFlipArticlesFragment.currentFlippedPosition;

/**
 * Created by MYPC on 2/10/2018.
 */

public class MagazinesFlipArticlesUsecase {

    public static final String TAG = MagazinesFlipArticlesUsecase.class.getSimpleName();
    @Inject
    YoApi.YoService yoService;

    @Inject
    @Named("login")
    PreferenceEndPoint preferenceEndPoint;
    @Inject
    public MagazinesServicesUsecase magazinesServicesUsecase;
    @Inject
    ConnectivityHelper mHelper;

    /**
     * Calls the service to get the articles service daily
     * @param swipeRefreshContainer The SwipeRefreshLayout object
     */
    public void callDailyArticlesService(final SwipeRefreshLayout swipeRefreshContainer, Context context, MagazineArticlesBaseAdapter myBaseAdapter, MagazineFlipArticlesFragment magazineFlipArticlesFragment, MagazineDashboardHelper magazineDashboardHelper) {
        getReadArticleIds(context, magazineFlipArticlesFragment, myBaseAdapter);
        String userId = preferenceEndPoint.getStringPreference(Constants.USER_ID);
        if (context != null) {
            String readCachedIds = MagazinePreferenceEndPoint.getInstance().getPref(context, userId).getString("read_article_ids", "");
            if (!TextUtils.isEmpty(readCachedIds)) {
                Type type1 = new TypeToken<List<String>>() {
                }.getType();
                String cachedIds = readCachedIds;
                List<String> cachedReadList = new Gson().fromJson(cachedIds, type1);
                Log.d(TAG, "Calling service for next articles");
                List<Articles> allArticles = myBaseAdapter.getAllItems();
                if (allArticles != null) {
                    List<String> allArticlesIds = new ArrayList<>();
                    for (Articles articles : allArticles) {
                        if (articles != null) {
                            allArticlesIds.add(articles.getId());
                        }
                    }
                    List<String> unreadArticleIds = new ArrayList<>(allArticlesIds);
                    unreadArticleIds.removeAll(cachedReadList);
                    updateArticlesAfterDailyService(swipeRefreshContainer, myBaseAdapter, magazineDashboardHelper, magazineFlipArticlesFragment);
                }
            } else {
                List<String> cachedReadList = new ArrayList<>();
                List<Articles> allArticles = myBaseAdapter.getAllItems();
                if (allArticles != null) {
                    List<String> allArticlesIds = new ArrayList<>();
                    for (Articles articles : allArticles) {
                        if (articles != null) {
                            allArticlesIds.add(articles.getId());
                        }
                    }
                    List<String> unreadArticleIds = new ArrayList<>(allArticlesIds);
                    updateArticlesAfterDailyService(swipeRefreshContainer, myBaseAdapter, magazineDashboardHelper, magazineFlipArticlesFragment);
                }
            }
        }
    }

    /**
     * Getting the cached articles
     */
    public void getLandingCachedArticles(Context context, MagazineArticlesBaseAdapter myBaseAdapter, MagazineFlipArticlesFragment magazineFlipArticlesFragment, MagazineDashboardHelper magazineDashboardHelper) {
        magazineFlipArticlesFragment.isSearch = false;
        String userId = preferenceEndPoint.getStringPreference(Constants.USER_ID);

        if (context != null) {

            String sharedFollowedCachedMagazines = MagazinePreferenceEndPoint.getInstance().getPref(context, userId).getString("followed_cached_magazines", "");
            String sharedRandomCachedMagazines = MagazinePreferenceEndPoint.getInstance().getPref(context, userId).getString("random_cached_magazines", "");

            if (!TextUtils.isEmpty(sharedFollowedCachedMagazines) && !sharedFollowedCachedMagazines.equalsIgnoreCase("[]") || !TextUtils.isEmpty(sharedRandomCachedMagazines) && !sharedRandomCachedMagazines.equalsIgnoreCase("[]")) {
                if (magazineFlipArticlesFragment.mProgress != null) {
                    magazineFlipArticlesFragment.mProgress.setVisibility(View.GONE);
                }
                magazineFlipArticlesFragment.tvProgressText.setVisibility(View.GONE);
                myBaseAdapter.clear();

                Type type = new TypeToken<List<Articles>>() {
                }.getType();
                List<Articles> cachedMagazinesList = new ArrayList<>();
                if (!TextUtils.isEmpty(sharedFollowedCachedMagazines)) {
                    String cachedMagazines = sharedFollowedCachedMagazines;
                    List<Articles> cachedFollowedMagazinesList = new Gson().fromJson(cachedMagazines, type);
                    cachedMagazinesList.addAll(cachedFollowedMagazinesList);
                }
                if (!TextUtils.isEmpty(sharedRandomCachedMagazines)) {
                    String cachedMagazines = sharedRandomCachedMagazines;
                    List<Articles> cachedRandomMagazinesList = new Gson().fromJson(cachedMagazines, type);
                    cachedMagazinesList.addAll(cachedRandomMagazinesList);
                }
                myBaseAdapter.addItems(cachedMagazinesList);
                if (myBaseAdapter.getCount() > 0) {
                    magazineFlipArticlesFragment.flipView.flipTo(0);
                    magazineFlipArticlesFragment.articlesRootLayout.setVisibility(View.VISIBLE);
                    magazineFlipArticlesFragment.networkFailureText.setVisibility(View.GONE);
                    if (magazineFlipArticlesFragment.llNoArticles != null) {
                        magazineFlipArticlesFragment.llNoArticles.setVisibility(View.GONE);
                        magazineFlipArticlesFragment.flipContainer.setVisibility(View.VISIBLE);
                    }

                    if (mHelper.isConnected()) {
                        Calendar calendar = Calendar.getInstance();
                        SimpleDateFormat mdformat = new SimpleDateFormat("yyyy / MM / dd ");
                        String currentDateString = mdformat.format(calendar.getTime());
                        String savedDateString = preferenceEndPoint.getStringPreference(Constants.SAVED_TIME);


                        if (!TextUtils.isEmpty(savedDateString)) {
                            try {
                                Date currentDate = mdformat.parse(currentDateString);
                                Date savedDate = mdformat.parse(savedDateString);
                                if (currentDate.compareTo(savedDate) > 0) { // Comparing the saved dated to the current one. If it is later then calling the service to fetch new articles
                                    preferenceEndPoint.saveBooleanPreference(Constants.IS_ARTICLES_POSTED, true);
                                    callDailyArticlesService(null, context, myBaseAdapter, magazineFlipArticlesFragment, magazineDashboardHelper);
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }

                        if (preferenceEndPoint.getBooleanPreference(Constants.IS_SERVICE_RUNNING) && !preferenceEndPoint.getBooleanPreference(Constants.IS_ARTICLES_POSTED)) {
                            preferenceEndPoint.saveBooleanPreference(Constants.IS_ARTICLES_POSTED, true);
                            callDailyArticlesService(null, context, myBaseAdapter, magazineFlipArticlesFragment, magazineDashboardHelper);
                        }
                    } else {
                        Toast.makeText(context, context.getResources().getString(R.string.unable_to_fetch_new_articles), Toast.LENGTH_LONG).show();
                    }
                } else {
                    magazineFlipArticlesFragment.flipContainer.setVisibility(View.GONE);
                    magazineFlipArticlesFragment.llNoArticles.setVisibility(View.VISIBLE);

                }
                return;
            } else {

                magazinesServicesUsecase.loadArticles(null, false, context, magazineFlipArticlesFragment);
            }
        }
    }

    /**
     * Getting the read article ids
     */
    public List<String> getReadArticleIds(Context context, MagazineFlipArticlesFragment magazineFlipArticlesFragment, MagazineArticlesBaseAdapter myBaseAdapter) {
        List<String> articlesList1 = null;

        if (currentFlippedPosition > 0) {
            if (currentFlippedPosition == 1) {
                if (myBaseAdapter.getItem(0) != null) {
                    String articleId1 = myBaseAdapter.getItem(0).getId();
                    magazineFlipArticlesFragment.readArticleIds.add(articleId1);
                }
                if (myBaseAdapter.secondArticle != null) {
                    String articleId2 = myBaseAdapter.secondArticle.getId();
                    magazineFlipArticlesFragment.readArticleIds.add(articleId2);
                }
                if (myBaseAdapter.thirdArticle != null) {
                    String articleId3 = myBaseAdapter.thirdArticle.getId();
                    magazineFlipArticlesFragment.readArticleIds.add(articleId3);
                }
            }


            if (myBaseAdapter != null) {
                for (int i = 0; i <= currentFlippedPosition; i++) {
                    if (myBaseAdapter.getItem(i) != null) {
                        String articleId = myBaseAdapter.getItem(i).getId();
                        magazineFlipArticlesFragment.readArticleIds.add(articleId);
                    }
                }
            }


            List<String> articlesList = new ArrayList<String>(new LinkedHashSet<String>(magazineFlipArticlesFragment.readArticleIds));

            String userId = preferenceEndPoint.getStringPreference(Constants.USER_ID);

            articlesList1 = new ArrayList<String>(new LinkedHashSet<String>(articlesList));
            if (context != null) {
                SharedPreferences.Editor editor = MagazinePreferenceEndPoint.getInstance().get(context, userId);
                editor.putString("read_article_ids", new Gson().toJson(new LinkedHashSet<String>(articlesList1)));
                editor.commit();
            }
        }
        return articlesList1;
    }

    /**
     * Updating the articles after following a topic
     *
     * @param topicId The topic id
     */
    public void updateArticlesAfterFollowTopic(String topicId, MagazineArticlesBaseAdapter myBaseAdapter, Context context, MagazineDashboardHelper magazineDashboardHelper, MagazineFlipArticlesFragment magazineFlipArticlesFragment) {
        List<Articles> articlesList = myBaseAdapter.getAllItems();
        List<Articles> unreadArticles = new ArrayList<>();
        List<String> readIdsList = new ArrayList<>();
        List<String> unreadArticleIds = new ArrayList<>();
        if (currentFlippedPosition > 0) {
            List<String> readIds = new ArrayList<>();
            if (myBaseAdapter.secondArticle != null) {
                String articleId2 = myBaseAdapter.secondArticle.getId();
                readIds.add(articleId2);
            }
            if (myBaseAdapter.thirdArticle != null) {
                String articleId3 = myBaseAdapter.thirdArticle.getId();
                readIds.add(articleId3);
            }

            if (myBaseAdapter != null) {
                Log.d("FlipArticlesFragment", "currentFlippedPosition outside loop " + currentFlippedPosition);
                for (int i = 0; i <= currentFlippedPosition; i++) {
                    if (myBaseAdapter.getItem(i) != null) {
                        String articleId = myBaseAdapter.getItem(i).getId();
                        //Log.d("FlipArticlesFragment", "Article Id is " + articleId + "currentFlippedPosition " + currentFlippedPosition + " Article Name is " + myBaseAdapter.getItem(i).getTitle() + " Articles size " + myBaseAdapter.getCount());

                        readIds.add(articleId);
                    }
                }
            }

            readIdsList = new ArrayList<String>(new LinkedHashSet<String>(readIds));
            List<String> allArticlesIds = new ArrayList<>();
            for (Articles articles : articlesList) {
                if (articles != null) {
                    allArticlesIds.add(articles.getId());
                }
            }
            unreadArticleIds = new ArrayList<>(allArticlesIds);
            unreadArticleIds.removeAll(readIdsList);

            for (Articles unreadArt : articlesList) {
                if (unreadArt != null) {
                    for (String unreadId : unreadArticleIds) {
                        if (unreadArt.getId().equals(unreadId)) {
                            unreadArticles.add(unreadArt);
                        }
                    }
                }
            }
        } else {
            List<Articles> cachedMagazinesList = magazinesServicesUsecase.getCachedMagazinesList(context);
            if (cachedMagazinesList != null) {
                unreadArticles.addAll(cachedMagazinesList);
            }
        }
        List<Articles> followedArticlesList = new ArrayList<>();
        if (!TextUtils.isEmpty(topicId)) {
            for (Articles articles : unreadArticles) {
                if (topicId.equals(articles.getTopicId())) {
                    followedArticlesList.add(articles);
                }
            }
        }

        List<Articles> cachedMagazinesList = magazinesServicesUsecase.getCachedMagazinesList(context);
        if (cachedMagazinesList != null) {
            List<Articles> tempList = cachedMagazinesList;
            for (int i = 0; i < cachedMagazinesList.size(); i++) {
                for (Articles followedArticles : followedArticlesList) {
                    if (followedArticles.getId().equals(tempList.get(i).getId())) {
                        tempList.get(i).setTopicFollowing("true");
                    }
                }
            }

            cachedMagazinesList = tempList;
            magazinesServicesUsecase.saveCachedMagazinesList(cachedMagazinesList, context);

            List<Articles> emptyUpdatedArticles = new ArrayList<>();
            List<Articles> notEmptyUpdatedArticles = new ArrayList<>();
            for (Articles updatedArticles : followedArticlesList) {
                if (!TextUtils.isEmpty(updatedArticles.getUpdated())) {
                    notEmptyUpdatedArticles.add(updatedArticles);
                } else {
                    emptyUpdatedArticles.add(updatedArticles);
                }
            }
            Collections.sort(notEmptyUpdatedArticles, new ArticlesComparator());
            Collections.reverse(notEmptyUpdatedArticles);
            notEmptyUpdatedArticles.addAll(emptyUpdatedArticles);
            followedArticlesList = notEmptyUpdatedArticles;

            for (Articles a : followedArticlesList) {
                if (a != null) {
                    Log.d("MyCollectionDetails", "The sorted list is " + a.getId() + " updated " + a.getUpdated());
                }
            }

            int positionToAdd = 10;

            List<Articles> unreadOtherArticles = new ArrayList<>(unreadArticles);
            unreadOtherArticles.removeAll(followedArticlesList);

            List<Articles> followedUnreadTopicArticles = new ArrayList<>();
            List<Articles> randomUnreadTopicArticles = new ArrayList<>();

            for (Articles articles : unreadOtherArticles) {
                if ("true".equals(articles.getTopicFollowing())) {
                    followedUnreadTopicArticles.add(articles);
                } else {
                    randomUnreadTopicArticles.add(articles);
                }
            }

            List<Articles> unreadOtherOrderedArticles = new ArrayList<>();

            List<Articles> followedUnreadTopicArticles1 = new ArrayList<Articles>(new LinkedHashSet<Articles>(followedUnreadTopicArticles));
            List<Articles> randomUnreadTopicArticles1 = new ArrayList<Articles>(new LinkedHashSet<Articles>(randomUnreadTopicArticles));

            unreadOtherOrderedArticles.addAll(followedUnreadTopicArticles1);
            unreadOtherOrderedArticles.addAll(randomUnreadTopicArticles1);

            if (unreadOtherOrderedArticles.size() > positionToAdd) {
                unreadOtherOrderedArticles.addAll(positionToAdd, followedArticlesList);
            } else {
                if (unreadOtherOrderedArticles.size() > 0) {
                    unreadOtherOrderedArticles.addAll(unreadOtherOrderedArticles.size() - 1, followedArticlesList);
                } else {
                    unreadOtherOrderedArticles.addAll(0, followedArticlesList);
                }
            }

            articlesList.removeAll(unreadOtherOrderedArticles);
            myBaseAdapter.removeItems(unreadOtherOrderedArticles);
            myBaseAdapter.addItemsAll(unreadOtherOrderedArticles);
            myBaseAdapter.notifyDataSetChanged();

            magazineDashboardHelper.getMoreDashboardArticlesAfterFollow(magazineFlipArticlesFragment, yoService, preferenceEndPoint, readIdsList, unreadArticleIds, unreadOtherOrderedArticles, followedArticlesList);

        }

    }

    /**
     * Sorting after following a topic
     *
     * @param totalArticles               The total articles
     * @param unreadOtherFollowedArticles The unread other followed articles
     * @param followedArticlesList        The followed articles list
     */
    public void performSortingAfterFollow(List<Articles> totalArticles, List<Articles> unreadOtherFollowedArticles, List<Articles> followedArticlesList, MagazineArticlesBaseAdapter myBaseAdapter) {
        List<Articles> followedTopicArticles = new ArrayList<>();
        List<Articles> randomTopicArticles = new ArrayList<>();
        for (Articles articles : totalArticles) {
            if ("true".equals(articles.getTopicFollowing())) {
                followedTopicArticles.add(articles);
            } else {
                randomTopicArticles.add(articles);
            }
        }

        List<String> readIds = new ArrayList<>();

        if (myBaseAdapter.secondArticle != null) {
            String articleId2 = myBaseAdapter.secondArticle.getId();
            readIds.add(articleId2);
        }
        if (myBaseAdapter.thirdArticle != null) {
            String articleId3 = myBaseAdapter.thirdArticle.getId();
            readIds.add(articleId3);
        }

        if (myBaseAdapter != null) {
            for (int i = 0; i <= currentFlippedPosition; i++) {
                if (myBaseAdapter.getItem(i) != null) {
                    String articleId = myBaseAdapter.getItem(i).getId();
                    //Log.d("FlipArticlesFragment", "Article Id is " + articleId + "currentFlippedPosition " + currentFlippedPosition + " Article Name is " + myBaseAdapter.getItem(currentFlippedPosition).getTitle() + " Articles size " + myBaseAdapter.getCount());

                    readIds.add(articleId);
                }
            }
        }

        List<String> readIdsList = new ArrayList<String>(new LinkedHashSet<String>(readIds));
        List<String> allArticlesIds = new ArrayList<>();
        List<Articles> articlesList = myBaseAdapter.getAllItems();
        for (Articles articles : articlesList) {
            if (articles != null) {
                allArticlesIds.add(articles.getId());
            }
        }
        List<String> unreadArticleIds = new ArrayList<>(allArticlesIds);
        unreadArticleIds.removeAll(readIdsList);

        List<String> followedIds = new ArrayList<>();
        if (followedArticlesList != null) {
            for (Articles followedArticle : followedArticlesList) {
                if (followedArticle != null) {
                    followedIds.add(followedArticle.getId());
                }
            }
        }

        unreadArticleIds.removeAll(followedIds);

        List<Articles> unreadArticles = new ArrayList<>();
        for (Articles unreadArt : articlesList) {
            if (unreadArt != null) {
                for (String unreadId : unreadArticleIds) {
                    if (unreadArt.getId().equals(unreadId)) {
                        unreadArticles.add(unreadArt);
                    }
                }
            }
        }

        List<Articles> followedUnreadTopicArticles = new ArrayList<>();
        List<Articles> randomUnreadTopicArticles = new ArrayList<>();
        for (Articles articles : unreadArticles) {
            if ("true".equals(articles.getTopicFollowing())) {
                followedUnreadTopicArticles.add(articles);
            } else {
                randomUnreadTopicArticles.add(articles);
            }
        }

        List<Articles> totalOtherUnreadArticles = new ArrayList<>();

        List<Articles> followedUnreadTopicArticles1 = new ArrayList<Articles>(new LinkedHashSet<Articles>(followedUnreadTopicArticles));
        List<Articles> randomUnreadTopicArticles1 = new ArrayList<Articles>(new LinkedHashSet<Articles>(randomUnreadTopicArticles));

        totalOtherUnreadArticles.addAll(followedUnreadTopicArticles1);
        totalOtherUnreadArticles.addAll(randomUnreadTopicArticles1);

        int positionToAdd = 10;

        if (totalOtherUnreadArticles.size() > positionToAdd) {
            totalOtherUnreadArticles.addAll(positionToAdd, followedTopicArticles);
        } else {
            if (totalOtherUnreadArticles.size() > 0) {
                totalOtherUnreadArticles.addAll(totalOtherUnreadArticles.size() - 1, followedTopicArticles);
            } else {
                if (followedArticlesList != null) {
                    totalOtherUnreadArticles.addAll(0, followedArticlesList);
                }
            }
        }

        articlesList.removeAll(totalOtherUnreadArticles);
        myBaseAdapter.removeItems(totalOtherUnreadArticles);
        myBaseAdapter.addItemsAll(totalOtherUnreadArticles);
        myBaseAdapter.notifyDataSetChanged();
    }

    /**
     * Updating the articles after the daily service to fetch the new articles
     */
    public void updateArticlesAfterDailyService(final SwipeRefreshLayout swipeRefreshContainer, MagazineArticlesBaseAdapter myBaseAdapter, MagazineDashboardHelper magazineDashboardHelper, MagazineFlipArticlesFragment magazineFlipArticlesFragment) {
        List<Articles> articlesList = myBaseAdapter.getAllItems();
        List<Articles> unreadArticles = new ArrayList<>();
        List<String> readIdsList = new ArrayList<>();
        List<String> unreadArticleIds = new ArrayList<>();
        if (currentFlippedPosition > 0) {
            List<String> readIds = new ArrayList<>();
            if (myBaseAdapter.secondArticle != null) {
                String articleId2 = myBaseAdapter.secondArticle.getId();
                readIds.add(articleId2);
            }
            if (myBaseAdapter.thirdArticle != null) {
                String articleId3 = myBaseAdapter.thirdArticle.getId();
                readIds.add(articleId3);
            }

            if (myBaseAdapter != null) {
                Log.d("FlipArticlesFragment", "currentFlippedPosition outside loop " + currentFlippedPosition);
                for (int i = 0; i <= currentFlippedPosition; i++) {
                    if (myBaseAdapter.getItem(i) != null) {
                        String articleId = myBaseAdapter.getItem(i).getId();
                        //Log.d("FlipArticlesFragment", "Article Id is " + articleId + "currentFlippedPosition " + currentFlippedPosition + " Article Name is " + myBaseAdapter.getItem(currentFlippedPosition).getTitle() + " Articles size " + myBaseAdapter.getCount());

                        readIds.add(articleId);
                    }
                }
            }

            readIdsList = new ArrayList<String>(new LinkedHashSet<String>(readIds));
            List<String> allArticlesIds = new ArrayList<>();
            for (Articles articles : articlesList) {
                if (articles != null) {
                    allArticlesIds.add(articles.getId());
                }
            }
            unreadArticleIds = new ArrayList<>(allArticlesIds);
            unreadArticleIds.removeAll(readIdsList);

            for (Articles unreadArt : articlesList) {
                if (unreadArt != null) {
                    for (String unreadId : unreadArticleIds) {
                        if (unreadArt.getId().equals(unreadId)) {
                            unreadArticles.add(unreadArt);
                        }
                    }
                }
            }
        } else {
            unreadArticles.addAll(articlesList);
            for (Articles articles : unreadArticles) {
                if (articles != null) {
                    unreadArticleIds.add(articles.getId());
                }
            }
        }

        List<Articles> unreadOtherArticles = new ArrayList<>(unreadArticles);

        List<Articles> followedUnreadTopicArticles = new ArrayList<>();
        List<Articles> randomUnreadTopicArticles = new ArrayList<>();

        for (Articles articles : unreadOtherArticles) {
            if (articles != null) {
                if ("true".equals(articles.getTopicFollowing())) {
                    followedUnreadTopicArticles.add(articles);
                } else {
                    randomUnreadTopicArticles.add(articles);
                }
            }
        }

        List<Articles> unreadOtherOrderedArticles = new ArrayList<>();

        List<Articles> followedUnreadTopicArticles1 = new ArrayList<Articles>(new LinkedHashSet<Articles>(followedUnreadTopicArticles));
        List<Articles> randomUnreadTopicArticles1 = new ArrayList<Articles>(new LinkedHashSet<Articles>(randomUnreadTopicArticles));

        unreadOtherOrderedArticles.addAll(followedUnreadTopicArticles1);
        unreadOtherOrderedArticles.addAll(randomUnreadTopicArticles1);
        articlesList.removeAll(unreadOtherOrderedArticles);
        myBaseAdapter.removeItems(unreadOtherOrderedArticles);
        myBaseAdapter.addItemsAll(unreadOtherOrderedArticles);

        magazineDashboardHelper.getDashboardArticlesForDailyService(magazineFlipArticlesFragment, yoService, preferenceEndPoint, readIdsList, unreadArticleIds, swipeRefreshContainer);

    }

    /**
     * Sorting the articles after the daily service to fetch the new articles
     *
     * @param totalArticles               The total articles
     * @param unreadOtherFollowedArticles The unread other followed articles
     */
    public void performSortingAfterDailyService(List<Articles> totalArticles, List<Articles> unreadOtherFollowedArticles, MagazineArticlesBaseAdapter myBaseAdapter, Context context, MagazineFlipArticlesFragment magazineFlipArticlesFragment, MagazineDashboardHelper magazineDashboardHelper) {
        List<Articles> followedTopicArticles = new ArrayList<>();
        List<Articles> randomTopicArticles = new ArrayList<>();
        for (Articles articles : totalArticles) {
            if ("true".equals(articles.getTopicFollowing())) {
                followedTopicArticles.add(articles);
            } else {
                randomTopicArticles.add(articles);
            }
        }

        List<String> readIds = new ArrayList<>();

        if (myBaseAdapter.secondArticle != null) {
            String articleId2 = myBaseAdapter.secondArticle.getId();
            readIds.add(articleId2);
        }
        if (myBaseAdapter.thirdArticle != null) {
            String articleId3 = myBaseAdapter.thirdArticle.getId();
            readIds.add(articleId3);
        }

        if (myBaseAdapter != null) {
            for (int i = 0; i <= currentFlippedPosition; i++) {
                if (myBaseAdapter.getItem(i) != null) {
                    String articleId = myBaseAdapter.getItem(i).getId();
                    //Log.d("FlipArticlesFragment", "Article Id is " + articleId + "currentFlippedPosition " + currentFlippedPosition + " Article Name is " + myBaseAdapter.getItem(currentFlippedPosition).getTitle() + " Articles size " + myBaseAdapter.getCount());

                    readIds.add(articleId);
                }
            }
        }

        List<String> readIdsList = new ArrayList<String>(new LinkedHashSet<String>(readIds));
        List<String> allArticlesIds = new ArrayList<>();
        List<Articles> articlesList = myBaseAdapter.getAllItems();
        for (Articles articles : articlesList) {
            if (articles != null) {
                allArticlesIds.add(articles.getId());
            }
        }
        List<String> unreadArticleIds = new ArrayList<>(allArticlesIds);
        unreadArticleIds.removeAll(readIdsList);

        List<Articles> unreadArticles = new ArrayList<>();
        for (Articles unreadArt : articlesList) {
            if (unreadArt != null) {
                for (String unreadId : unreadArticleIds) {
                    if (unreadArt.getId().equals(unreadId)) {
                        unreadArticles.add(unreadArt);
                    }
                }
            }
        }

        List<Articles> followedUnreadTopicArticles = new ArrayList<>();
        List<Articles> randomUnreadTopicArticles = new ArrayList<>();
        for (Articles articles : unreadArticles) {
            if ("true".equals(articles.getTopicFollowing())) {
                followedUnreadTopicArticles.add(articles);
            } else {
                randomUnreadTopicArticles.add(articles);
            }
        }

        List<Articles> totalOtherUnreadArticles = new ArrayList<>();

        List<Articles> followedUnreadTopicArticles1 = new ArrayList<Articles>(new LinkedHashSet<Articles>(followedUnreadTopicArticles));
        List<Articles> randomUnreadTopicArticles1 = new ArrayList<Articles>(new LinkedHashSet<Articles>(randomUnreadTopicArticles));

        totalOtherUnreadArticles.addAll(followedUnreadTopicArticles1);
        totalOtherUnreadArticles.addAll(randomUnreadTopicArticles1);

        int positionToAdd = 0;

        if (currentFlippedPosition > 0) {
            positionToAdd = 10;
        } else {
            positionToAdd = 0;
        }

        if (!followedTopicArticles.isEmpty()) {
            if (totalOtherUnreadArticles.size() > positionToAdd) {
                if (positionToAdd == 0) {
                    totalOtherUnreadArticles.addAll(positionToAdd, followedTopicArticles);
                } else {
                    totalOtherUnreadArticles.addAll(currentFlippedPosition + positionToAdd, followedTopicArticles);
                }
            } else {
                if (totalOtherUnreadArticles.size() > 0) {
                    totalOtherUnreadArticles.addAll(totalOtherUnreadArticles.size() - 1, followedTopicArticles);
                }
            }
        } else {
            if (totalOtherUnreadArticles.size() > 0) {
                if (followedUnreadTopicArticles1.isEmpty()) {
                    if (totalOtherUnreadArticles.size() > positionToAdd) {
                        if (positionToAdd == 0) {
                            totalOtherUnreadArticles.addAll(positionToAdd, randomTopicArticles);
                        } else {
                            totalOtherUnreadArticles.addAll(currentFlippedPosition + positionToAdd, randomTopicArticles);
                        }
                    } else {
                        if (totalOtherUnreadArticles.size() > 0) {
                            totalOtherUnreadArticles.addAll(0, randomTopicArticles);
                        }
                    }
                } else {
                    totalOtherUnreadArticles.addAll(totalOtherUnreadArticles.size() - 1, randomTopicArticles);
                }
            }
        }

        if (currentFlippedPosition == 0) {
            articlesList.removeAll(totalOtherUnreadArticles);
            myBaseAdapter.removeItems(totalOtherUnreadArticles);
            myBaseAdapter.removeItems(articlesList);
            myBaseAdapter.clear();
            myBaseAdapter.addItems(totalOtherUnreadArticles);
            myBaseAdapter.notifyDataSetChanged();
        } else {
            articlesList.removeAll(totalOtherUnreadArticles);
            myBaseAdapter.removeItems(totalOtherUnreadArticles);
            myBaseAdapter.addItemsAll(totalOtherUnreadArticles);
            myBaseAdapter.notifyDataSetChanged();
        }

        magazineFlipArticlesFragment.handleMoreDashboardResponse(totalOtherUnreadArticles, false, true);
        magazinesServicesUsecase.deleteExtraArticlesFromCache(context, magazineDashboardHelper);
    }
}
