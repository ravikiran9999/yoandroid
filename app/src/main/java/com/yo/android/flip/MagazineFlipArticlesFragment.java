package com.yo.android.flip;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.orion.android.common.util.ConnectivityHelper;
import com.yo.android.R;
import com.yo.android.adapters.MagazineArticlesBaseAdapter;
import com.yo.android.api.YoApi;
import com.yo.android.chat.ui.fragments.BaseFragment;
import com.yo.android.helpers.MagazinePreferenceEndPoint;
import com.yo.android.model.Articles;
import com.yo.android.ui.FollowMoreTopicsActivity;
import com.yo.android.ui.fragments.MagazinesFragment;
import com.yo.android.util.ArticlesComparator;
import com.yo.android.util.Constants;
import com.yo.android.util.MagazineDashboardHelper;

import java.lang.reflect.Type;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import se.emilsjolander.flipview.FlipView;

/**
 * Created by creatives on 6/30/2016.
 */
public class MagazineFlipArticlesFragment extends BaseFragment implements SharedPreferences.OnSharedPreferenceChangeListener, FlipView.OnFlipListener, SwipeRefreshLayout.OnRefreshListener {

    private MagazineTopicsSelectionFragment magazineTopicsSelectionFragment;
    public MagazineArticlesBaseAdapter myBaseAdapter;

    @Bind(R.id.article_root_layout)
    public FrameLayout articlesRootLayout;
    @Bind(R.id.ll_no_articles)
    public LinearLayout llNoArticles;
    @Bind(R.id.flipView_container)
    public FrameLayout flipContainer;
    @Bind(R.id.network_failure)
    public TextView networkFailureText;
    @Bind(R.id.progress)
    public ProgressBar mProgress;
    @Bind(R.id.flip_view)
    public FlipView flipView;
    @Bind(R.id.btn_magazine_follow_topics)
    public Button followMoreTopics;
    @Bind(R.id.tv_progress_text)
    public TextView tvProgressText;
    @Bind(R.id.swipeContainer)
    SwipeRefreshLayout swipeRefreshContainer;

    @Inject
    YoApi.YoService yoService;
    @Inject
    ConnectivityHelper mHelper;

    public static int suggestionsPosition;

    public static int lastReadArticle = 0;


    public boolean isSearch;
    private int pageCount = 1;
    private String accessToken;
    private boolean isArticlesEndReached;
    private List<String> readArticleIds;
    private LinkedHashSet<List<String>> articlesIdsHashSet = new LinkedHashSet<>();
    public static int currentFlippedPosition;
    private MagazineDashboardHelper magazineDashboardHelper;
    private String followedTopicId;
    private static int articleCountThreshold = 2000;

    @SuppressLint("ValidFragment")
    public MagazineFlipArticlesFragment(MagazineTopicsSelectionFragment fragment) {
        // Required empty public constructor
        magazineTopicsSelectionFragment = fragment;
    }

    public MagazineFlipArticlesFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferenceEndPoint.getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        EventBus.getDefault().register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.magazine_flip_fragment, container, false);
        ButterKnife.bind(this, view);

        myBaseAdapter = new MagazineArticlesBaseAdapter(getActivity(), preferenceEndPoint, yoService, mToastFactory, this);
        flipView.setAdapter(myBaseAdapter);
        flipView.setOnFlipListener(this);
        readArticleIds = new ArrayList<>();
        magazineDashboardHelper = new MagazineDashboardHelper();
        swipeRefreshContainer.setOnRefreshListener(this);

        update();
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 60 && resultCode == getActivity().RESULT_OK) {
            if (data != null) {
                Articles topic = data.getParcelableExtra("UpdatedTopic");
                int pos = data.getIntExtra("Pos", 0);
                boolean isTopicFollowing = Boolean.valueOf(topic.getTopicFollowing());
                String articlePlace = data.getStringExtra("ArticlePlace");
                myBaseAdapter.updateTopic(isTopicFollowing, topic, pos, articlePlace);
            }

        } else if (requestCode == 500 && resultCode == getActivity().RESULT_OK) {
            if (data != null) {
                Articles articles = data.getParcelableExtra("UpdatedArticle");
                int pos = data.getIntExtra("Pos", 0);
                String articlePlace = data.getStringExtra("ArticlePlace");
                boolean isLiked = Boolean.valueOf(articles.getLiked());
                myBaseAdapter.updateArticle(isLiked, articles, pos, articlePlace);

                Log.d("FlipArticlesFragment", "Title and liked " + articles.getTitle() + " " + articles.getLiked());
            }

        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("FlipArticlesFragment", "In onResume()");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("FlipArticlesFragment", "In onPause()");
    }

    @Override
    public void onOptionsMenuClosed(Menu menu) {
        super.onOptionsMenuClosed(menu);
        Log.d("FlipArticlesFragment", "In onOptionsMenuClosed()");
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mLog.d("onActivityCreated", "In onActivityCreated");
        loadArticles(null, false);
        followMoreTopics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), FollowMoreTopicsActivity.class);
                intent.putExtra("From", "Magazines");
                startActivity(intent);
            }
        });

        // To navigate to the 1st article page once the end of all the articles is reached (Uncomment if this functionality is required)
        /*flipView.setOnOverFlipListener(new FlipView.OnOverFlipListener() {
            @Override
            public void onOverFlip(FlipView flipView, OverFlipMode overFlipMode, boolean overFlippingPrevious, float overFlipDistance, float flipDistancePerPage) {
                if(flipView.getCurrentPage()!=0) {
                    flipView.flipTo(0);
                } else if(flipView.getCurrentPage()==0) {
                    flipView.flipTo(myBaseAdapter.getCount()-1);
                }
            }
        });*/
    }

    public void loadArticles(List<String> tagIds, boolean renewal) {

        if (!mHelper.isConnected()) {

            Type type1 = new TypeToken<List<Articles>>() {
            }.getType();
            String userId = preferenceEndPoint.getStringPreference(Constants.USER_ID);
            if (getActivity() != null) {
                String sharedFollowedCachedMagazines = MagazinePreferenceEndPoint.getInstance().getPref(getActivity(), userId).getString("followed_cached_magazines", "");
                String sharedRandomCachedMagazines = MagazinePreferenceEndPoint.getInstance().getPref(getActivity(), userId).getString("random_cached_magazines", "");

                List<Articles> cachedMagazinesList = new ArrayList<>();

                if (!TextUtils.isEmpty(sharedFollowedCachedMagazines) || !TextUtils.isEmpty(sharedRandomCachedMagazines)) {
                    if (mProgress != null) {
                        mProgress.setVisibility(View.GONE);
                    }
                    tvProgressText.setVisibility(View.GONE);
                    myBaseAdapter.clear();
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

                    myBaseAdapter.addItems(cachedMagazinesList);
                    flipView.flipTo(lastReadArticle);
                    articlesRootLayout.setVisibility(View.VISIBLE);
                    networkFailureText.setVisibility(View.GONE);
                    return;
                }
            }
        } else {
            articlesRootLayout.setVisibility(View.VISIBLE);
            networkFailureText.setVisibility(View.GONE);
        }

        if (mProgress != null) {
            mProgress.setVisibility(View.VISIBLE);
        }

        accessToken = preferenceEndPoint.getStringPreference("access_token");
        if (tagIds != null) {
            isSearch = true;
            yoService.getArticlesAPI(accessToken, tagIds).enqueue(callback);
            tvProgressText.setVisibility(View.GONE);
        } else {

            isSearch = false;
            flipContainer.setVisibility(View.GONE);
            tvProgressText.setVisibility(View.VISIBLE);

            List<String> readArticlesList = new ArrayList<>();
            List<String> unreadArticlesList = new ArrayList<>();
            magazineDashboardHelper.getDashboardArticles(this, yoService, preferenceEndPoint, readArticlesList, unreadArticlesList, renewal);

        }
    }

    private Callback<List<Articles>> callback = new Callback<List<Articles>>() {
        @Override
        public void onResponse(Call<List<Articles>> call, Response<List<Articles>> response) {
            if (!isAdded()) {
                return;
            }
            myBaseAdapter.clear();
            if (mProgress != null) {
                mProgress.setVisibility(View.GONE);
            }
            tvProgressText.setVisibility(View.GONE);

            if (response.body() != null && !response.body().isEmpty()) {
                myBaseAdapter.addItems(response.body());
                mLog.d("Magazines", "lastReadArticle" + lastReadArticle);
                if (myBaseAdapter.getCount() > lastReadArticle) {
                    flipView.flipTo(lastReadArticle);
                }

                if (!isSearch) {
                    if (!TextUtils.isEmpty(preferenceEndPoint.getStringPreference("cached_magazines"))) {
                        preferenceEndPoint.removePreference("cached_magazines");
                    }
                    preferenceEndPoint.saveStringPreference("cached_magazines", new Gson().toJson(response.body()));
                }
                if (!isSearch) {
                    if (llNoArticles != null) {
                        llNoArticles.setVisibility(View.GONE);
                        flipContainer.setVisibility(View.VISIBLE);
                        if (myBaseAdapter.getCount() > 0) {
                            Random r = new Random();
                            suggestionsPosition = r.nextInt(myBaseAdapter.getCount() - 0) + 0;
                        }
                    }
                }
            } else {
                flipContainer.setVisibility(View.VISIBLE);
                llNoArticles.setVisibility(View.GONE);
                getLandingCachedArticles();
            }

        }

        @Override
        public void onFailure(Call<List<Articles>> call, Throwable t) {
            if (t instanceof UnknownHostException) {
                mLog.e("Magazine", "Please check network settings");
            }
            myBaseAdapter.clear();
            if (mProgress != null) {
                mProgress.setVisibility(View.GONE);
            }
            tvProgressText.setVisibility(View.GONE);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    if (llNoArticles != null) {
                        networkFailureText.setVisibility(View.GONE);
                        flipContainer.setVisibility(View.GONE);
                        llNoArticles.setVisibility(View.VISIBLE);
                        getLandingCachedArticles();
                    }

                }
            }, 500L);
        }

    };

    public void onDestroyView() {
        super.onDestroyView();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        preferenceEndPoint.getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        EventBus.getDefault().unregister(this);
    }

    public void update() {
        Log.d("FlipArticlesFragment", "In update() FlipArticlesFragment");
        getLandingCachedArticles();
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if ("magazine_tags".equals(key)) {
            if (!TextUtils.isEmpty(preferenceEndPoint.getStringPreference("magazine_tags"))) {
                String[] prefTags = TextUtils.split(preferenceEndPoint.getStringPreference("magazine_tags"), ",");
                if (prefTags != null) {
                    if (myBaseAdapter.getCount() > 0) {
                        if (mProgress != null) {
                            mProgress.setVisibility(View.GONE);
                        }
                        updateArticlesAfterFollowTopic(followedTopicId);
                    } else {
                        loadArticles(null, false);
                    }
                }
            } else {
                myBaseAdapter.addItems(new ArrayList<Articles>());
                if (llNoArticles != null) {
                    flipContainer.setVisibility(View.VISIBLE);
                    llNoArticles.setVisibility(View.GONE);
                    getLandingCachedArticles();
                }
            }
            if (getParentFragment() instanceof MagazinesFragment) {
                ((MagazinesFragment) getParentFragment()).refreshSearch();
            }
        }

    }


    public void refresh() {
        lastReadArticle = 0;
        getLandingCachedArticles();
    }


    @Override
    public void onFlippedToPage(FlipView v, int position, long id) {
        if (!isSearch) {
            lastReadArticle = position;
        } else {
            lastReadArticle = 0;
        }
        currentFlippedPosition = position;

        if (MagazineDashboardHelper.currentReadArticles != 0 || currentFlippedPosition == MagazineDashboardHelper.request * 100) {
            getReadArticleIds();
            String userId = preferenceEndPoint.getStringPreference(Constants.USER_ID);
            if (getActivity() != null) {
                String readCachedIds = MagazinePreferenceEndPoint.getInstance().getPref(getActivity(), userId).getString("read_article_ids", "");
                if (!TextUtils.isEmpty(readCachedIds)) {
                    Type type1 = new TypeToken<List<String>>() {
                    }.getType();
                    String cachedIds = readCachedIds;
                    List<String> cachedReadList = new Gson().fromJson(cachedIds, type1);
                    mLog.d("MagazineFlipArticlesFragment", "Calling service for next articles");
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
                        magazineDashboardHelper.getMoreDashboardArticles(this, yoService, preferenceEndPoint, cachedReadList, unreadArticleIds, null);
                    }
                }
            }
        }

    }

    public void onEventMainThread(String action) {
        followedTopicId = action;
        if (Constants.OTHERS_MAGAZINE_ACTION.equals(action) || Constants.TOPIC_NOTIFICATION_ACTION.equals(action) || Constants.TOPIC_FOLLOWING_ACTION.equals(action)) {
            updateArticlesAfterFollowTopic(followedTopicId);
        } else if (Constants.START_FETCHING_ARTICLES_ACTION.equals(action)) {
            getReadArticleIds();
            String userId = preferenceEndPoint.getStringPreference(Constants.USER_ID);
            if (getActivity() != null) {
                String readCachedIds = MagazinePreferenceEndPoint.getInstance().getPref(getActivity(), userId).getString("read_article_ids", "");
                if (!TextUtils.isEmpty(readCachedIds)) {
                    Type type1 = new TypeToken<List<String>>() {
                    }.getType();
                    String cachedIds = readCachedIds;
                    List<String> cachedReadList = new Gson().fromJson(cachedIds, type1);
                    mLog.d("MagazineFlipArticlesFragment", "Calling service for next articles");
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
                        updateArticlesAfterDailyService();
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
                        updateArticlesAfterDailyService();
                    }
                }
            }
        } else if (Constants.RENEWAL.equalsIgnoreCase(action)) {
            loadArticles(null, true);
        }
    }

    public void removeReadArticles() {

        if (currentFlippedPosition > 0) {
            getReadArticleIds();

            if (getActivity() != null) {
                String userId = preferenceEndPoint.getStringPreference(Constants.USER_ID);
                if (getActivity() != null) {
                    String sharedFollowedCachedMagazines = MagazinePreferenceEndPoint.getInstance().getPref(getActivity(), userId).getString("followed_cached_magazines", "");
                    String sharedRandomCachedMagazines = MagazinePreferenceEndPoint.getInstance().getPref(getActivity(), userId).getString("random_cached_magazines", "");
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

                    cachedMagazinesList.remove(myBaseAdapter.secondArticle);
                    cachedMagazinesList.remove(myBaseAdapter.thirdArticle);

                    List<Articles> tempArticlesList = new ArrayList<Articles>(cachedMagazinesList);
                    String readCachedIds = MagazinePreferenceEndPoint.getInstance().getPref(getActivity(), userId).getString("read_article_ids", "");
                    if (!TextUtils.isEmpty(readCachedIds)) {
                        Type type1 = new TypeToken<List<String>>() {
                        }.getType();
                        String cachedIds = readCachedIds;
                        List<String> cachedReadList = new Gson().fromJson(cachedIds, type1);
                        for (Articles article : cachedMagazinesList) {
                            for (String artId : cachedReadList) {
                                if (article.getId().equals(artId)) {
                                    tempArticlesList.remove(article);
                                    break;
                                }
                            }
                        }
                        cachedMagazinesList = tempArticlesList;
                    }

                    List<Articles> followedTopicArticles = new ArrayList<>();
                    List<Articles> randomTopicArticles = new ArrayList<>();
                    for (Articles articles : cachedMagazinesList) {
                        if ("true".equals(articles.getTopicFollowing())) {
                            followedTopicArticles.add(articles);
                        } else {
                            randomTopicArticles.add(articles);
                        }
                    }
                    SharedPreferences.Editor editor = MagazinePreferenceEndPoint.getInstance().get(getActivity(), userId);
                    editor.putString("followed_cached_magazines", new Gson().toJson(new LinkedHashSet<Articles>(followedTopicArticles)));
                    editor.putString("random_cached_magazines", new Gson().toJson(new LinkedHashSet<Articles>(randomTopicArticles)));
                    editor.commit();
                }
            }
        }

    }

    public void getLandingCachedArticles() {
        isSearch = false;
        String userId = preferenceEndPoint.getStringPreference(Constants.USER_ID);

        if (getActivity() != null) {

            String sharedFollowedCachedMagazines = MagazinePreferenceEndPoint.getInstance().getPref(getActivity(), userId).getString("followed_cached_magazines", "");
            String sharedRandomCachedMagazines = MagazinePreferenceEndPoint.getInstance().getPref(getActivity(), userId).getString("random_cached_magazines", "");

            if (!TextUtils.isEmpty(sharedFollowedCachedMagazines) && !sharedFollowedCachedMagazines.equalsIgnoreCase("[]") || !TextUtils.isEmpty(sharedRandomCachedMagazines) && !sharedRandomCachedMagazines.equalsIgnoreCase("[]")) {
                if (mProgress != null) {
                    mProgress.setVisibility(View.GONE);
                }
                tvProgressText.setVisibility(View.GONE);
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
                    flipView.flipTo(0);
                    articlesRootLayout.setVisibility(View.VISIBLE);
                    networkFailureText.setVisibility(View.GONE);
                    if (llNoArticles != null) {
                        llNoArticles.setVisibility(View.GONE);
                        flipContainer.setVisibility(View.VISIBLE);
                        if (myBaseAdapter.getCount() > 0) {
                            try {
                                Random r = new Random();
                                suggestionsPosition = r.nextInt(myBaseAdapter.getCount() - 0) + 0;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } else {
                    flipContainer.setVisibility(View.GONE);
                    llNoArticles.setVisibility(View.VISIBLE);

                }
                return;
            } /*else {
                loadArticles(null, false);
            }*/
        }
    }

    private List<String> getReadArticleIds() {
        List<String> articlesList1 = null;
        if (currentFlippedPosition > 0) {
            if (currentFlippedPosition == 1) {
                if (myBaseAdapter.getItem(0) != null) {
                    String articleId1 = myBaseAdapter.getItem(0).getId();
                    readArticleIds.add(articleId1);
                }
                if (myBaseAdapter.secondArticle != null) {
                    String articleId2 = myBaseAdapter.secondArticle.getId();
                    readArticleIds.add(articleId2);
                }
                if (myBaseAdapter.thirdArticle != null) {
                    String articleId3 = myBaseAdapter.thirdArticle.getId();
                    readArticleIds.add(articleId3);
                }
            }


            if (myBaseAdapter != null) {
                Log.d("FlipArticlesFragment", "currentFlippedPosition outside loop " + currentFlippedPosition);
                for (int i = 0; i <= currentFlippedPosition; i++) {
                    if (myBaseAdapter.getItem(i) != null) {
                        String articleId = myBaseAdapter.getItem(i).getId();
                        readArticleIds.add(articleId);
                    }
                }
            }


            List<String> articlesList = new ArrayList<String>(new LinkedHashSet<String>(readArticleIds));

            String userId = preferenceEndPoint.getStringPreference(Constants.USER_ID);

            articlesList1 = new ArrayList<String>(new LinkedHashSet<String>(articlesList));
            if (getActivity() != null) {
                SharedPreferences.Editor editor = MagazinePreferenceEndPoint.getInstance().get(getActivity(), userId);
                editor.putString("read_article_ids", new Gson().toJson(new LinkedHashSet<String>(articlesList1)));
                editor.commit();
            }
        }
        return articlesList1;
    }

    public void handleDashboardResponse(List<Articles> totalArticles) {
        mLog.d("Magazines", "lastReadArticle" + lastReadArticle);
        if (myBaseAdapter.getCount() > lastReadArticle) {
            flipView.flipTo(lastReadArticle);
        }

        if (!isSearch) {

            List<Articles> followedTopicArticles1 = new ArrayList<>();
            List<Articles> randomTopicArticles1 = new ArrayList<>();
            for (Articles articles : totalArticles) {
                if ("true".equals(articles.getTopicFollowing())) {
                    followedTopicArticles1.add(articles);
                } else {
                    randomTopicArticles1.add(articles);
                }
            }
            String userId = preferenceEndPoint.getStringPreference(Constants.USER_ID);
            if (getActivity() != null) {
                SharedPreferences.Editor editor = MagazinePreferenceEndPoint.getInstance().get(getActivity(), userId);
                editor.putString("followed_cached_magazines", new Gson().toJson(new LinkedHashSet<Articles>(followedTopicArticles1)));
                editor.putString("random_cached_magazines", new Gson().toJson(new LinkedHashSet<Articles>(randomTopicArticles1)));
                editor.commit();
            }
        }
        if (llNoArticles != null) {
            llNoArticles.setVisibility(View.GONE);
            flipContainer.setVisibility(View.VISIBLE);
            if (myBaseAdapter.getCount() > 0) {
                Random r = new Random();
                suggestionsPosition = r.nextInt(myBaseAdapter.getCount() - 0) + 0;
                myBaseAdapter.getAllItems().add(suggestionsPosition, new Articles());
                myBaseAdapter.notifyDataSetChanged();
            }
        }
    }

    public void handleMoreDashboardResponse(List<Articles> totalArticles, boolean isFromFollow) {
        mLog.d("Magazines", "lastReadArticle" + lastReadArticle);
        if (myBaseAdapter.getCount() > lastReadArticle) {
            flipView.flipTo(lastReadArticle);
        }

        if (!isSearch) {

            removeReadArticles();
            if (getActivity() != null) {
                magazineDashboardHelper.removeReadArticleIds(getActivity(), preferenceEndPoint);
            }

            List<Articles> followedTopicArticles1 = new ArrayList<>();
            List<Articles> randomTopicArticles1 = new ArrayList<>();
            for (Articles articles : totalArticles) {
                if ("true".equals(articles.getTopicFollowing())) {
                    followedTopicArticles1.add(articles);
                } else {
                    randomTopicArticles1.add(articles);
                }
            }
            String userId = preferenceEndPoint.getStringPreference(Constants.USER_ID);
            if (getActivity() != null) {
                String sharedFollowedCachedMagazines = MagazinePreferenceEndPoint.getInstance().getPref(getActivity(), userId).getString("followed_cached_magazines", "");
                String sharedRandomCachedMagazines = MagazinePreferenceEndPoint.getInstance().getPref(getActivity(), userId).getString("random_cached_magazines", "");

                List<Articles> cachedFollowedMagazinesList = new ArrayList<>();
                List<Articles> cachedRandomMagazinesList = new ArrayList<>();
                if (!TextUtils.isEmpty(sharedFollowedCachedMagazines) || !TextUtils.isEmpty(sharedRandomCachedMagazines)) {

                    Type type = new TypeToken<List<Articles>>() {
                    }.getType();

                    if (!TextUtils.isEmpty(sharedFollowedCachedMagazines)) {
                        String cachedMagazines = sharedFollowedCachedMagazines;
                        cachedFollowedMagazinesList = new Gson().fromJson(cachedMagazines, type);
                        cachedFollowedMagazinesList.addAll(followedTopicArticles1);
                    }
                    if (!TextUtils.isEmpty(sharedRandomCachedMagazines)) {
                        String cachedMagazines = sharedRandomCachedMagazines;
                        cachedRandomMagazinesList = new Gson().fromJson(cachedMagazines, type);
                        cachedRandomMagazinesList.addAll(randomTopicArticles1);
                    }
                }

                if (getActivity() != null) {
                    SharedPreferences.Editor editor = MagazinePreferenceEndPoint.getInstance().get(getActivity(), userId);
                    editor.putString("followed_cached_magazines", new Gson().toJson(new LinkedHashSet<>(cachedFollowedMagazinesList)));
                    editor.putString("random_cached_magazines", new Gson().toJson(new LinkedHashSet<>(cachedRandomMagazinesList)));
                    editor.commit();
                }
            }

            if (isFromFollow) {
                if (mProgress != null) {
                    mProgress.setVisibility(View.GONE);
                }
            }
        }
        if (!isFromFollow) {
            if (llNoArticles != null) {
                llNoArticles.setVisibility(View.GONE);
                flipContainer.setVisibility(View.VISIBLE);
                if (myBaseAdapter.getCount() > 0) {
                    Random r = new Random();
                    suggestionsPosition = r.nextInt(myBaseAdapter.getCount() - 0) + 0;
                    myBaseAdapter.getAllItems().add(suggestionsPosition, new Articles());
                    myBaseAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    public void updateArticlesAfterFollowTopic(String topicId) {
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
                        Log.d("FlipArticlesFragment", "Article Id is " + articleId + "currentFlippedPosition " + currentFlippedPosition + " Article Name is " + myBaseAdapter.getItem(i).getTitle() + " Articles size " + myBaseAdapter.getCount());

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
            List<Articles> cachedMagazinesList = getCachedMagazinesList();
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

        List<Articles> cachedMagazinesList = getCachedMagazinesList();
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
            saveCachedMagazinesList(cachedMagazinesList);

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

            magazineDashboardHelper.getMoreDashboardArticlesAfterFollow(this, yoService, preferenceEndPoint, readIdsList, unreadArticleIds, unreadOtherOrderedArticles, followedArticlesList);

        }

    }

    public void performSortingAfterFollow(List<Articles> totalArticles, List<Articles> unreadOtherFollowedArticles, List<Articles> followedArticlesList) {
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
        for (Articles followedArticle : followedArticlesList) {
            if (followedArticle != null) {
                followedIds.add(followedArticle.getId());
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
                totalOtherUnreadArticles.addAll(0, followedArticlesList);
            }
        }

        articlesList.removeAll(totalOtherUnreadArticles);
        myBaseAdapter.removeItems(totalOtherUnreadArticles);
        myBaseAdapter.addItemsAll(totalOtherUnreadArticles);
        myBaseAdapter.notifyDataSetChanged();
    }

    public void updateArticlesAfterDailyService() {
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
                        Log.d("FlipArticlesFragment", "Article Id is " + articleId + "currentFlippedPosition " + currentFlippedPosition + " Article Name is " + myBaseAdapter.getItem(currentFlippedPosition).getTitle() + " Articles size " + myBaseAdapter.getCount());

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
                unreadArticleIds.add(articles.getId());
            }
        }

        List<Articles> unreadOtherArticles = new ArrayList<>(unreadArticles);

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
        articlesList.removeAll(unreadOtherOrderedArticles);
        myBaseAdapter.removeItems(unreadOtherOrderedArticles);
        myBaseAdapter.addItemsAll(unreadOtherOrderedArticles);

        magazineDashboardHelper.getDashboardArticlesForDailyService(this, yoService, preferenceEndPoint, readIdsList, unreadArticleIds, unreadOtherOrderedArticles);

    }

    public void performSortingAfterDailyService(List<Articles> totalArticles, List<Articles> unreadOtherFollowedArticles) {
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
                    Log.d("FlipArticlesFragment", "Article Id is " + articleId + "currentFlippedPosition " + currentFlippedPosition + " Article Name is " + myBaseAdapter.getItem(currentFlippedPosition).getTitle() + " Articles size " + myBaseAdapter.getCount());

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

        int positionToAdd = 10;

        if (!followedTopicArticles.isEmpty()) {
            if (totalOtherUnreadArticles.size() > positionToAdd) {
                totalOtherUnreadArticles.addAll(positionToAdd, followedTopicArticles);
            } else {
                if (totalOtherUnreadArticles.size() > 0) {
                    totalOtherUnreadArticles.addAll(totalOtherUnreadArticles.size() - 1, followedTopicArticles);
                }
            }
        } else {
            if (totalOtherUnreadArticles.size() > positionToAdd) {
                totalOtherUnreadArticles.addAll(positionToAdd, randomTopicArticles);
            } else {
                if (totalOtherUnreadArticles.size() > 0) {
                    totalOtherUnreadArticles.addAll(totalOtherUnreadArticles.size() - 1, randomTopicArticles);
                }
            }
        }

        articlesList.removeAll(totalOtherUnreadArticles);
        myBaseAdapter.removeItems(totalOtherUnreadArticles);
        myBaseAdapter.addItemsAll(totalOtherUnreadArticles);

        deleteExtraArticlesFromCache();
    }

    private List<Articles> getCachedMagazinesList() {
        Type type1 = new TypeToken<List<Articles>>() {
        }.getType();
        String userId = preferenceEndPoint.getStringPreference(Constants.USER_ID);

        List<Articles> cachedMagazinesList = new ArrayList<>();
        if (getActivity() != null) {
            String sharedFollowedCachedMagazines = MagazinePreferenceEndPoint.getInstance().getPref(getActivity(), userId).getString("followed_cached_magazines", "");
            String sharedRandomCachedMagazines = MagazinePreferenceEndPoint.getInstance().getPref(getActivity(), userId).getString("random_cached_magazines", "");

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

    private void saveCachedMagazinesList(List<Articles> cachedMagazinesList) {
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
        if (getActivity() != null) {
            SharedPreferences.Editor editor = MagazinePreferenceEndPoint.getInstance().get(getActivity(), userId);
            editor.putString("followed_cached_magazines", new Gson().toJson(new LinkedHashSet<Articles>(followedTopicArticles)));
            editor.putString("random_cached_magazines", new Gson().toJson(new LinkedHashSet<Articles>(randomTopicArticles)));
            editor.commit();
        }
    }

    private void deleteExtraArticlesFromCache() {

        String userId = preferenceEndPoint.getStringPreference(Constants.USER_ID);
        if (getActivity() != null) {
            String sharedFollowedCachedMagazines = MagazinePreferenceEndPoint.getInstance().getPref(getActivity(), userId).getString("followed_cached_magazines", "");
            String sharedRandomCachedMagazines = MagazinePreferenceEndPoint.getInstance().getPref(getActivity(), userId).getString("random_cached_magazines", "");

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
                        if (getActivity() != null) {
                            magazineDashboardHelper.removeArticlesFromCache(getActivity(), preferenceEndPoint, "random_cached_magazines");
                        }
                        // Then get the remaining articles count after deleting the random articles
                        int remainingArticlesCount = extraArticlesCount - cachedRandomSize;
                        // Move to followed articles list
                        if (cachedFollowedSize <= remainingArticlesCount) {
                            // Delete all the followed articles
                            if (getActivity() != null) {
                                magazineDashboardHelper.removeArticlesFromCache(getActivity(), preferenceEndPoint, "followed_cached_magazines");
                            }
                        } else {
                            // Delete the articles equal to the remaining articles count
                            cachedFollowedMagazinesList.subList(0, remainingArticlesCount).clear();
                            if (getActivity() != null) {
                                SharedPreferences.Editor editor = MagazinePreferenceEndPoint.getInstance().get(getActivity(), userId);
                                editor.putString("followed_cached_magazines", new Gson().toJson(new LinkedHashSet<Articles>(cachedFollowedMagazinesList)));
                                editor.commit();
                            }
                        }
                    } else {
                        // Delete the articles equal to the extra articles count
                        cachedRandomMagazinesList.subList(0, extraArticlesCount).clear();
                        if (getActivity() != null) {
                            SharedPreferences.Editor editor = MagazinePreferenceEndPoint.getInstance().get(getActivity(), userId);
                            editor.putString("random_cached_magazines", new Gson().toJson(new LinkedHashSet<Articles>(cachedRandomMagazinesList)));
                            editor.commit();
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onRefresh() {
        refreshedArticles();
    }

    private void refreshedArticles() {
        List<String> cachedReadList = getReadArticleIds();
        List<Articles> allArticles = myBaseAdapter.getAllItems();
        if (allArticles != null) {
            List<String> allArticlesIds = new ArrayList<>();
            for (Articles articles : allArticles) {
                if (articles != null)
                    allArticlesIds.add(articles.getId());
            }
            List<String> unreadArticleIds = new ArrayList<>(allArticlesIds);
            List<String> mCachedReadList = cachedReadList != null ? cachedReadList : new ArrayList<String>();

            magazineDashboardHelper.getMoreDashboardArticles(this, yoService, preferenceEndPoint, mCachedReadList, unreadArticleIds, swipeRefreshContainer);
        }
    }
}