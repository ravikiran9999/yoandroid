package com.yo.android.flip;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.orion.android.common.util.ConnectivityHelper;
import com.yo.android.BuildConfig;
import com.yo.android.R;
import com.yo.android.adapters.MagazineArticlesBaseAdapter;
import com.yo.android.api.YoApi;
import com.yo.android.chat.ui.fragments.BaseFragment;
import com.yo.android.helpers.MagazinePreferenceEndPoint;
import com.yo.android.model.Articles;
import com.yo.android.ui.FollowMoreTopicsActivity;
import com.yo.android.ui.NewFollowMoreTopicsActivity;
import com.yo.android.ui.fragments.MagazinesFragment;
import com.yo.android.usecase.AddTopicsUsecase;
import com.yo.android.usecase.MagazinesFlipArticlesUsecase;
import com.yo.android.usecase.MagazinesServicesUsecase;
import com.yo.android.util.ArticlesComparator;
import com.yo.android.util.Constants;
import com.yo.android.util.MagazineDashboardHelper;
import com.yo.android.util.YODialogs;

import java.lang.reflect.Type;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import se.emilsjolander.flipview.FlipView;
import se.emilsjolander.flipview.OverFlipMode;

/**
 * This fragment is used to display the magazine articles with flip in the Landing screen
 */
public class MagazineFlipArticlesFragment extends BaseFragment implements SharedPreferences.OnSharedPreferenceChangeListener, FlipView.OnFlipListener, FlipView.OnOverFlipListener {

    public static boolean refreshing;
    public static int updateCalled;

    public MagazineArticlesBaseAdapter myBaseAdapter;

    @Bind(R.id.refreshContainer)
    SwipeRefreshLayout swipeRefreshContainer;
    @Bind(R.id.article_root_layout)
    public FrameLayout articlesRootLayout;
    @Bind(R.id.ll_no_articles)
    public LinearLayout llNoArticles;
    @Bind(R.id.flipView_container)
    public FrameLayout flipContainer;
    @Bind(R.id.network_failure)
    public TextView networkFailureText;
    @Bind(R.id.server_failure)
    public TextView serverFailureText;
    @Bind(R.id.progress)
    public ProgressBar mProgress;
    @Bind(R.id.flip_view)
    public FlipView flipView;
    @Bind(R.id.btn_magazine_follow_topics)
    public Button followMoreTopics;
    @Bind(R.id.tv_progress_text)
    public TextView tvProgressText;

    @Inject
    YoApi.YoService yoService;
    @Inject
    AddTopicsUsecase addTopicsUsecase;
    @Inject
    ConnectivityHelper mHelper;
    @Inject
    public MagazinesServicesUsecase magazinesServicesUsecase;
    @Inject
    public MagazinesFlipArticlesUsecase magazinesFlipArticlesUsecase;

    public static int suggestionsPosition = 0;
    public static int lastReadArticle = 0;
    public boolean isSearch;
    public List<String> readArticleIds;
    public static int currentFlippedPosition;
    public MagazineDashboardHelper magazineDashboardHelper;
    private String followedTopicId;
    private Context mContext;
    Handler handler;

    @SuppressLint("ValidFragment")
    public MagazineFlipArticlesFragment(MagazineTopicsSelectionFragment fragment) {
        // Required empty public constructor
        MagazineTopicsSelectionFragment magazineTopicsSelectionFragment = fragment;
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

        myBaseAdapter = new MagazineArticlesBaseAdapter(getActivity(), preferenceEndPoint, yoService, mToastFactory, this, addTopicsUsecase, magazinesServicesUsecase);
        flipView.setAdapter(myBaseAdapter);
        flipView.setOnFlipListener(this);
        flipView.setOnOverFlipListener(this);
        readArticleIds = new ArrayList<>();
        magazineDashboardHelper = new MagazineDashboardHelper();
        swipeRefreshContainer.setOnRefreshListener(swipeRefreshLayout);
        swipeRefreshContainer.setEnabled(false);
        swipeRefreshContainer.setRefreshing(false);

        boolean value = preferenceEndPoint.getBooleanPreference(Constants.LAUNCH_APP, false);
        if (value) {
            preferenceEndPoint.saveBooleanPreference(Constants.LAUNCH_APP, false);
            updateCalled = 1;
            update();
        }

        // clear glide for every 10 seconds
        handler = new Handler();
        handler.postDelayed(new Runnable() {

            @Override
            public void run() {
                clearGlideMemory(mContext);
            }
        }, 10000);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 60 && resultCode == getActivity().RESULT_OK) { // On coming back from Topics Detail screen
            if (data != null) {
                Articles topic = data.getParcelableExtra("UpdatedTopic");
                int pos = data.getIntExtra("Pos", 0);
                boolean isTopicFollowing = Boolean.valueOf(topic.getTopicFollowing());
                String articlePlace = data.getStringExtra("ArticlePlace");
                if (isTopicFollowing) {
                    myBaseAdapter.updateTopic(isTopicFollowing, topic, pos, articlePlace);
                }
            }

        } else if (requestCode == 500 && resultCode == getActivity().RESULT_OK) { // On coming back from Magazine Webview Detail screen
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
        if (showAddBalance()) {
            YODialogs.addBalance(getActivity(), getString(R.string.no_sufficient_bal_wallet), preferenceEndPoint);
        }

        super.onResume();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mLog.d("onActivityCreated", "In onActivityCreated");
        followMoreTopics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                if (!BuildConfig.NEW_FOLLOW_MORE_TOPICS) {
                    intent = new Intent(getActivity(), FollowMoreTopicsActivity.class);
                } else {
                    intent = new Intent(getActivity(), NewFollowMoreTopicsActivity.class);
                }
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

    public Callback<List<Articles>> callback = new Callback<List<Articles>>() {
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
                List<Articles> totalArticlesWithSummary = new ArrayList<Articles>();
                for (Articles articles : response.body()) {
                    if (!"...".equalsIgnoreCase(articles.getSummary())) {
                        totalArticlesWithSummary.add(articles);
                    }
                }
                myBaseAdapter.addItems(totalArticlesWithSummary);
                mLog.d("Magazines", "lastReadArticle" + lastReadArticle);
                if (myBaseAdapter.getCount() > lastReadArticle) {
                    flipView.flipTo(lastReadArticle);
                }

                if (!isSearch) {
                    if (!TextUtils.isEmpty(preferenceEndPoint.getStringPreference("cached_magazines"))) {
                        preferenceEndPoint.removePreference("cached_magazines");
                    }
                    preferenceEndPoint.saveStringPreference("cached_magazines", new Gson().toJson(totalArticlesWithSummary));
                }
                if (!isSearch) {
                    if (llNoArticles != null) {
                        llNoArticles.setVisibility(View.GONE);
                        flipContainer.setVisibility(View.VISIBLE);
                    }
                }
            } else {
                flipContainer.setVisibility(View.VISIBLE);
                llNoArticles.setVisibility(View.GONE);
                magazinesFlipArticlesUsecase.getLandingCachedArticles(getActivity(), myBaseAdapter, MagazineFlipArticlesFragment.this, magazineDashboardHelper);
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
                        magazinesFlipArticlesUsecase.getLandingCachedArticles(getActivity(), myBaseAdapter, MagazineFlipArticlesFragment.this, magazineDashboardHelper);
                    }

                }
            }, 500L);
        }

    };

    public void onDestroyView() {
        super.onDestroyView();
        try {
            clearGlideMemory(mContext);
            if(handler != null) {
                handler.removeCallbacksAndMessages(null);
            }
        } finally {

        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        preferenceEndPoint.getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        EventBus.getDefault().unregister(this);
    }

    /**
     * Updating the articles in the landing screen
     */
    public void update() {

        boolean magazineRenewal = preferenceEndPoint.getBooleanPreference(Constants.MAGAZINE_LOCK, false);
        if (!magazineRenewal) {
            magazinesFlipArticlesUsecase.getLandingCachedArticles(getActivity(), myBaseAdapter, MagazineFlipArticlesFragment.this, magazineDashboardHelper);
        } else {
            YODialogs.addBalance(getActivity(), getActivity().getString(R.string.no_sufficient_bal_wallet), preferenceEndPoint);
            tvProgressText.setVisibility(View.GONE);
            if (mProgress != null) {
                mProgress.setVisibility(View.GONE);
            }
            flipContainer.setVisibility(View.GONE);
            llNoArticles.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        boolean magazineRenewal = preferenceEndPoint.getBooleanPreference(Constants.MAGAZINE_LOCK, false);
        if ("magazine_tags".equals(key)) {
            if (!TextUtils.isEmpty(preferenceEndPoint.getStringPreference("magazine_tags"))) {
                String[] prefTags = TextUtils.split(preferenceEndPoint.getStringPreference("magazine_tags"), ",");
                if (prefTags != null) {
                    if (myBaseAdapter.getCount() > 0) {
                        if (mProgress != null) {
                            mProgress.setVisibility(View.GONE);
                        }
                        magazinesFlipArticlesUsecase.updateArticlesAfterFollowTopic(followedTopicId, myBaseAdapter, getActivity(), magazineDashboardHelper, this);
                    } else if (!magazineRenewal) {
                        magazinesServicesUsecase.loadArticles(null, false, getActivity(), this);
                    }
                }
            } else {
                myBaseAdapter.addItems(new ArrayList<Articles>());
                if (llNoArticles != null) {
                    flipContainer.setVisibility(View.VISIBLE);
                    llNoArticles.setVisibility(View.GONE);
                    magazinesFlipArticlesUsecase.getLandingCachedArticles(getActivity(), myBaseAdapter, MagazineFlipArticlesFragment.this, magazineDashboardHelper);
                }
            }
            if (getParentFragment() instanceof MagazinesFragment) {
                ((MagazinesFragment) getParentFragment()).refreshSearch();
            }
        }

    }


    /**
     * Refreshing the articles in the landing screen
     */
    public void refresh() {
        lastReadArticle = 0;
        boolean magazineRenewal = preferenceEndPoint.getBooleanPreference(Constants.MAGAZINE_LOCK, false);
        if (!magazineRenewal) {
            magazinesFlipArticlesUsecase.getLandingCachedArticles(getActivity(), myBaseAdapter, MagazineFlipArticlesFragment.this, magazineDashboardHelper);
        } else {
            YODialogs.addBalance(getActivity(), getActivity().getString(R.string.no_sufficient_bal_wallet), preferenceEndPoint);
        }
    }


    @Override
    public void onFlippedToPage(FlipView v, int position, long id) {
        if (!isSearch) {
            lastReadArticle = position;
        } else {
            lastReadArticle = 0;
        }

        if (position != 0) {
            swipeRefreshContainer.setEnabled(false);
            swipeRefreshContainer.setRefreshing(false);
        }

        if (!isSearch) {
            if (position > 0 && position % Constants.SUGGESTIONS_PAGE_FREQUENCY == 0) { // Showing the Suggestions Page at a particular frequency ie; 25
                if (myBaseAdapter.getCount() > 0) {
                    try {
                        suggestionsPosition = position + 5;
                        myBaseAdapter.getAllItems().add(suggestionsPosition, new Articles());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        currentFlippedPosition = position;

        if ((MagazineDashboardHelper.currentReadArticles != 0 || currentFlippedPosition % 100 == 0) && !isSearch) {
            magazinesFlipArticlesUsecase.getReadArticleIds(getActivity(), this, myBaseAdapter);
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
            magazinesFlipArticlesUsecase.updateArticlesAfterFollowTopic(followedTopicId, myBaseAdapter, getActivity(), magazineDashboardHelper, this);
        } else if (Constants.START_FETCHING_ARTICLES_ACTION.equals(action)) {
            //isFetchArticlesPosted = true;
            if (mHelper.isConnected()) {
                preferenceEndPoint.saveBooleanPreference(Constants.IS_ARTICLES_POSTED, true);
                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat mdformat = new SimpleDateFormat("yyyy / MM / dd ");
                String savedDate = mdformat.format(calendar.getTime());
                preferenceEndPoint.saveStringPreference(Constants.SAVED_TIME, savedDate);
                magazinesFlipArticlesUsecase.callDailyArticlesService(null, getActivity(), myBaseAdapter, this, magazineDashboardHelper);
            } else {
                Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.unable_to_fetch_new_articles), Toast.LENGTH_LONG).show();
            }
        } else if (Constants.RENEWAL.equalsIgnoreCase(action)) {
            magazinesServicesUsecase.loadArticles(null, true, getActivity(), this);
        }
    }

    /**
     * Calls the service to get the articles service daily
     * @param swipeRefreshContainer The SwipeRefreshLayout object
     *//*
    private void callDailyArticlesService(final SwipeRefreshLayout swipeRefreshContainer) {
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
                    updateArticlesAfterDailyService(swipeRefreshContainer);
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
                    updateArticlesAfterDailyService(swipeRefreshContainer);
                }
            }
        }
    }*/

    /**
     * Removing the read articles
     */
    public void removeReadArticles() {

        if (currentFlippedPosition > 0) {
            magazinesFlipArticlesUsecase.getReadArticleIds(getActivity(), this, myBaseAdapter);

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

    public void moveToFirst() {
        if (myBaseAdapter.getCount() > 0) {
            flipView.flipTo(0);
        }
    }

/*    *//**
     * Getting the cached articles
     *//*
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
                                    callDailyArticlesService(null);
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }

                        if (preferenceEndPoint.getBooleanPreference(Constants.IS_SERVICE_RUNNING) && !preferenceEndPoint.getBooleanPreference(Constants.IS_ARTICLES_POSTED)) {
                            preferenceEndPoint.saveBooleanPreference(Constants.IS_ARTICLES_POSTED, true);
                            callDailyArticlesService(null);
                        }
                    } else {
                        Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.unable_to_fetch_new_articles), Toast.LENGTH_LONG).show();
                    }
                } else {
                    flipContainer.setVisibility(View.GONE);
                    llNoArticles.setVisibility(View.VISIBLE);

                }
                return;
            } else {

                magazinesServicesUsecase.loadArticles(null, false, getActivity(), this);
            }
        }
    }*/

/*    *//**
     * Getting the read article ids
     *//*
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
    }*/

    /**
     * Handling the dashboard response
     *
     * @param totalArticles The total articles
     */
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
        }
    }

    /**
     * Handling the dashboard response after getting more articles
     *
     * @param totalArticles The total articles
     * @param isFromFollow  Is from clicking Follow button
     */
    public void handleMoreDashboardResponse(List<Articles> totalArticles, boolean isFromFollow, boolean isFromDailyService) {
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
                        if (isFromDailyService) {
                            cachedFollowedMagazinesList.addAll(0, followedTopicArticles1);
                        } else {
                            cachedFollowedMagazinesList.addAll(followedTopicArticles1);
                        }
                    }
                    if (!TextUtils.isEmpty(sharedRandomCachedMagazines)) {
                        String cachedMagazines = sharedRandomCachedMagazines;
                        cachedRandomMagazinesList = new Gson().fromJson(cachedMagazines, type);
                        if (isFromDailyService) {
                            cachedRandomMagazinesList.addAll(0, randomTopicArticles1);
                        } else {
                            cachedRandomMagazinesList.addAll(randomTopicArticles1);
                        }
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
            }
        }
    }

    /**
     * Updating the articles after following a topic
     *
     * @param topicId The topic id
     *//*
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
            List<Articles> cachedMagazinesList = magazinesServicesUsecase.getCachedMagazinesList(getActivity());
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

        List<Articles> cachedMagazinesList = magazinesServicesUsecase.getCachedMagazinesList(getActivity());
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
            magazinesServicesUsecase.saveCachedMagazinesList(cachedMagazinesList, getActivity());

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

    *//**
     * Sorting after following a topic
     *
     * @param totalArticles               The total articles
     * @param unreadOtherFollowedArticles The unread other followed articles
     * @param followedArticlesList        The followed articles list
     *//*
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

    *//**
     * Updating the articles after the daily service to fetch the new articles
     *//*
    public void updateArticlesAfterDailyService(final SwipeRefreshLayout swipeRefreshContainer) {
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

        magazineDashboardHelper.getDashboardArticlesForDailyService(this, yoService, preferenceEndPoint, readIdsList, unreadArticleIds, swipeRefreshContainer);

    }

  /**
     * Sorting the articles after the daily service to fetch the new articles
     *
     * @param totalArticles               The total articles
     * @param unreadOtherFollowedArticles The unread other followed articles
     *//**//**//**//*
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

        handleMoreDashboardResponse(totalOtherUnreadArticles, false, true);
        magazinesServicesUsecase.deleteExtraArticlesFromCache(getActivity(), magazineDashboardHelper);
    }*/

    private void refreshedArticles() {
        Log.d("FlipArticlesFragment", "Calling pull to refresh to load articles refreshedArticles");
        if (mHelper.isConnected() && !isSearch) {
            magazinesFlipArticlesUsecase.callDailyArticlesService(swipeRefreshContainer, getActivity(), myBaseAdapter, this, magazineDashboardHelper);
        } else {
            refreshing = false;
            swipeRefreshContainer.setEnabled(false);
            swipeRefreshContainer.setRefreshing(false);
        }

    }

    @Override
    public void onOverFlip(FlipView v, OverFlipMode mode, boolean overFlippingPrevious, float overFlipDistance, float flipDistancePerPage) {
        if (!refreshing) {
            refreshing = true;
            swipeRefreshContainer.setEnabled(true);
            swipeRefreshContainer.setRefreshing(true);
            refreshedArticles();
        }
    }

    SwipeRefreshLayout.OnRefreshListener swipeRefreshLayout = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            swipeRefreshContainer.setEnabled(false);
            swipeRefreshContainer.setRefreshing(false);
        }
    };

    /**
     * work around to show add balance
     *
     * @return
     */
    private boolean showAddBalance() {
        boolean appLockStatus = preferenceEndPoint.getBooleanPreference(Constants.APP_LOCK, false);
        return appLockStatus && updateCalled == 0;
    }

}