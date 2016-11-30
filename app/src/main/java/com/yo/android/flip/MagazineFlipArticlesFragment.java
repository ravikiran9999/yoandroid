package com.yo.android.flip;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
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
import com.yo.android.helpers.LruCacheHelper;
import com.yo.android.helpers.MagazinePreferenceEndPoint;
import com.yo.android.model.Articles;
import com.yo.android.ui.FollowMoreTopicsActivity;
import com.yo.android.ui.fragments.MagazinesFragment;
import com.yo.android.util.Constants;

import java.lang.reflect.Type;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import se.emilsjolander.flipview.FlipView;
import se.emilsjolander.flipview.OverFlipMode;

/**
 * Created by creatives on 6/30/2016.
 */
public class MagazineFlipArticlesFragment extends BaseFragment implements SharedPreferences.OnSharedPreferenceChangeListener, FlipView.OnFlipListener {

    private static MagazineTopicsSelectionFragment magazineTopicsSelectionFragment;
    private MagazineArticlesBaseAdapter myBaseAdapter;
    @Inject
    YoApi.YoService yoService;
    private LinearLayout llNoArticles;
    private FrameLayout flipContainer;
    public ProgressBar mProgress;
    private Button followMoreTopics;

    @Inject
    ConnectivityHelper mHelper;
    private FrameLayout articlesRootLayout;
    private TextView networkFailureText;
    @Inject
    //protected LruCacheHelper lruCacheHelper;

    public static int suggestionsPosition;

    public static int lastReadArticle = 0;

    private FlipView flipView;

    public boolean isSearch;
    private int pageCount = 1;
    private String accessToken;
    private boolean isArticlesEndReached;
    private TextView tvProgressText;
    private List<String> readArticleIds;
    private LinkedHashSet<List<String>> articlesIdsHashSet = new LinkedHashSet<>();
    private static int currentFlippedPosition;

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
        articlesRootLayout = (FrameLayout) view.findViewById(R.id.article_root_layout);
        llNoArticles = (LinearLayout) view.findViewById(R.id.ll_no_articles);
        flipContainer = (FrameLayout) view.findViewById(R.id.flipView_container);
        networkFailureText = (TextView) view.findViewById(R.id.network_failure);
        mProgress = (ProgressBar) view.findViewById(R.id.progress);
        flipView = (FlipView) view.findViewById(R.id.flip_view);
        myBaseAdapter = new MagazineArticlesBaseAdapter(getActivity(), preferenceEndPoint, yoService, mToastFactory, this);
        flipView.setAdapter(myBaseAdapter);
        followMoreTopics = (Button) view.findViewById(R.id.btn_magazine_follow_topics);
        flipView.setOnFlipListener(this);
        tvProgressText = (TextView) view.findViewById(R.id.tv_progress_text);
        readArticleIds = new ArrayList<>();

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
                myBaseAdapter.updateTopic(isTopicFollowing, topic, pos);
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

        currentFlippedPosition = 0;

        /*if (!TextUtils.isEmpty(preferenceEndPoint.getStringPreference("magazine_tags"))) {
            String[] prefTags = TextUtils.split(preferenceEndPoint.getStringPreference("magazine_tags"), ",");
            if (prefTags != null) {
                //loadArticles(null);
                getCachedArticles();
            }
        }*/ /*else {
            mProgress.setVisibility(View.GONE);
            *//*flipContainer.setVisibility(View.GONE);
            llNoArticles.setVisibility(View.VISIBLE);*//*
            flipContainer.setVisibility(View.VISIBLE);
            llNoArticles.setVisibility(View.GONE);
            //loadArticles(null);
            getCachedArticles();
        }*/

        followMoreTopics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), FollowMoreTopicsActivity.class);
                intent.putExtra("From", "Magazines");
                startActivity(intent);
            }
        });

        //YODialogs.showPopup(getActivity(), "YO! Get $50 Cashback on Recharge of $100+", "YO! is doing a wonderful promotion of its wallet feature by collaborating with many banking companies these days and they are proving some specific coupon codes to obtain the benefit of adding money in Yo! wallet.");

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

    public void getCachedArticles() {
        isSearch = false;
        String userId = preferenceEndPoint.getStringPreference(Constants.USER_ID);
        String sharedCachedMagazines = MagazinePreferenceEndPoint.getInstance().getPref(getActivity(), userId).getString("cached_magazines", "");

        //if (!TextUtils.isEmpty(preferenceEndPoint.getStringPreference("cached_magazines"))) {
        if (!TextUtils.isEmpty(sharedCachedMagazines)) {
            if (mProgress != null) {
                mProgress.setVisibility(View.GONE);
            }
            tvProgressText.setVisibility(View.GONE);
            myBaseAdapter.clear();
                /*if (articlesRootLayout.getChildCount() > 0) {
                    articlesRootLayout.setVisibility(View.GONE);
                    networkFailureText.setText(getActivity().getResources().getString(R.string.unable_to_fetch));
                    networkFailureText.setVisibility(View.VISIBLE);
                }*/
            Type type = new TypeToken<List<Articles>>() {
            }.getType();
            //String cachedMagazines = preferenceEndPoint.getStringPreference("cached_magazines", null);
            String cachedMagazines = sharedCachedMagazines;
            List<Articles> cachedMagazinesList = new Gson().fromJson(cachedMagazines, type);
            myBaseAdapter.addItems(cachedMagazinesList);
            //removeFirstPageArticles();
            //flipView.flipTo(lastReadArticle);
            flipView.flipTo(0);
            articlesRootLayout.setVisibility(View.VISIBLE);
            networkFailureText.setVisibility(View.GONE);
            if (llNoArticles != null) {
                llNoArticles.setVisibility(View.GONE);
                flipContainer.setVisibility(View.VISIBLE);
                if(myBaseAdapter.getCount()>0) {
                    Random r = new Random();
                    suggestionsPosition = r.nextInt(myBaseAdapter.getCount() - 0) + 0;
                }
            }
            return;
        } else {
         loadArticles(null);
        }
    }

    public void loadArticles(List<String> tagIds) {

        if (!mHelper.isConnected()) {
            /*if(lruCacheHelper.get("magazines_cache") != null) {
              List<Articles> cacheArticlesList = (List<Articles>) lruCacheHelper.get("magazines_cache");
                myBaseAdapter.addItems(cacheArticlesList);
                articlesRootLayout.setVisibility(View.VISIBLE);
                networkFailureText.setVisibility(View.GONE);
            } else {*/
            String userId = preferenceEndPoint.getStringPreference(Constants.USER_ID);
            String sharedCachedMagazines = MagazinePreferenceEndPoint.getInstance().getPref(getActivity(), userId).getString("cached_magazines", "");

            //if (!TextUtils.isEmpty(preferenceEndPoint.getStringPreference("cached_magazines"))) {
            if (!TextUtils.isEmpty(sharedCachedMagazines)) {
                if (mProgress != null) {
                    mProgress.setVisibility(View.GONE);
                }
                tvProgressText.setVisibility(View.GONE);
                myBaseAdapter.clear();
                /*if (articlesRootLayout.getChildCount() > 0) {
                    articlesRootLayout.setVisibility(View.GONE);
                    networkFailureText.setText(getActivity().getResources().getString(R.string.unable_to_fetch));
                    networkFailureText.setVisibility(View.VISIBLE);
                }*/
                Type type = new TypeToken<List<Articles>>() {
                }.getType();
                //String cachedMagazines = preferenceEndPoint.getStringPreference("cached_magazines", null);
                String cachedMagazines = sharedCachedMagazines;
                List<Articles> cachedMagazinesList = new Gson().fromJson(cachedMagazines, type);
                myBaseAdapter.addItems(cachedMagazinesList);
                flipView.flipTo(lastReadArticle);
                articlesRootLayout.setVisibility(View.VISIBLE);
                networkFailureText.setVisibility(View.GONE);
                return;
            }
            //}
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
            //yoService.getUserArticlesAPI(accessToken).enqueue(callback);
            isSearch = false;
            flipContainer.setVisibility(View.GONE);
            tvProgressText.setVisibility(View.VISIBLE);
            /*yoService.getAllArticlesAPI(accessToken).enqueue(new Callback<List<Articles>>() {
                @Override
                public void onResponse(Call<List<Articles>> call, Response<List<Articles>> response) {
                    if (!isAdded()) {
                        return;
                    }
                    myBaseAdapter.clear();
                    if (mProgress != null) {
                        mProgress.setVisibility(View.GONE);
                    }

                    if (response.body() != null && !response.body().isEmpty()) {
                        myBaseAdapter.addItems(response.body());
                        mLog.d("Magazines", "lastReadArticle" + lastReadArticle);
                        flipView.flipTo(lastReadArticle);
                        //lruCacheHelper.put("magazines_cache", response.body());
                        if (!isSearch) {
                            if (!TextUtils.isEmpty(preferenceEndPoint.getStringPreference("cached_magazines"))) {
                                preferenceEndPoint.removePreference("cached_magazines");
                            }
                            preferenceEndPoint.saveStringPreference("cached_magazines", new Gson().toJson(response.body()));
                        }
                        if (llNoArticles != null) {
                            llNoArticles.setVisibility(View.GONE);
                            flipContainer.setVisibility(View.VISIBLE);
                            if (myBaseAdapter.getCount() > 0) {
                                Random r = new Random();
                                suggestionsPosition = r.nextInt(myBaseAdapter.getCount() - 0) + 0;
                            }
                        }
                    } else {
                        flipContainer.setVisibility(View.VISIBLE);
                        llNoArticles.setVisibility(View.GONE);
                        getCachedArticles();
                    }
                }

                @Override
                public void onFailure(Call<List<Articles>> call, Throwable t) {
                    if (mProgress != null) {
                        mProgress.setVisibility(View.GONE);
                    }
                    flipContainer.setVisibility(View.GONE);
                    llNoArticles.setVisibility(View.VISIBLE);
                }
            });*/

            getArticlesWithPagination();

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
                flipView.flipTo(lastReadArticle);
                //lruCacheHelper.put("magazines_cache", response.body());
                if(!isSearch) {
                    if(!TextUtils.isEmpty(preferenceEndPoint.getStringPreference("cached_magazines"))) {
                        preferenceEndPoint.removePreference("cached_magazines");
                    }
                    preferenceEndPoint.saveStringPreference("cached_magazines", new Gson().toJson(response.body()));
                }
                if(!isSearch) {
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
                /*if (llNoArticles != null) {
                    flipContainer.setVisibility(View.GONE);
                    llNoArticles.setVisibility(View.VISIBLE);
                }*/
                flipContainer.setVisibility(View.VISIBLE);
                llNoArticles.setVisibility(View.GONE);
                //loadArticles(null);
                getCachedArticles();
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
                    /*if(lruCacheHelper.get("magazines_cache") != null) {
                        List<Articles> cacheArticlesList = (List<Articles>) lruCacheHelper.get("magazines_cache");
                        myBaseAdapter.addItems(cacheArticlesList);
                        articlesRootLayout.setVisibility(View.VISIBLE);
                        networkFailureText.setVisibility(View.GONE);
                    } else {*/
                    if (llNoArticles != null) {
                        networkFailureText.setVisibility(View.GONE);
                        /*llNoArticles.setVisibility(View.VISIBLE);
                        flipContainer.setVisibility(View.GONE);*/
                        flipContainer.setVisibility(View.GONE);
                        llNoArticles.setVisibility(View.VISIBLE);
                        //loadArticles(null);
                        getCachedArticles();
                    }
                    //}

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
        if (!TextUtils.isEmpty(preferenceEndPoint.getStringPreference("magazine_tags"))) {
            String[] prefTags = TextUtils.split(preferenceEndPoint.getStringPreference("magazine_tags"), ",");
            if (prefTags != null) {
                //loadArticles(null);
                getCachedArticles();
            }
        }
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if ("magazine_tags".equals(key)) {
            if (!TextUtils.isEmpty(preferenceEndPoint.getStringPreference("magazine_tags"))) {
                String[] prefTags = TextUtils.split(preferenceEndPoint.getStringPreference("magazine_tags"), ",");
                if (prefTags != null) {
                    loadArticles(null);
                    //getCachedArticles();
                }
            } else {
                myBaseAdapter.addItems(new ArrayList<Articles>());
                //flipView.flipTo(lastReadArticle);
                if (llNoArticles != null) {
                    /*llNoArticles.setVisibility(View.VISIBLE);
                    flipContainer.setVisibility(View.GONE);*/
                    flipContainer.setVisibility(View.VISIBLE);
                    llNoArticles.setVisibility(View.GONE);
                    //loadArticles(null);
                    getCachedArticles();
                }
            }
            if (getParentFragment() instanceof MagazinesFragment) {
                ((MagazinesFragment) getParentFragment()).refreshSearch();
            }
        }

    }


    public void refresh() {
        if (!TextUtils.isEmpty(preferenceEndPoint.getStringPreference("magazine_tags"))) {
            String[] prefTags = TextUtils.split(preferenceEndPoint.getStringPreference("magazine_tags"), ",");
            if (prefTags != null) {
                lastReadArticle = 0;
                //loadArticles(null);
                getCachedArticles();
            }
        }
    }


    @Override
    public void onFlippedToPage(FlipView v, int position, long id) {
        if(!isSearch) {
            lastReadArticle = position;
        } else {
            lastReadArticle = 0;
        }
        currentFlippedPosition = position;

        /*if(position == 0) {
            String articleId1 = myBaseAdapter.getItem(position).getId();
            String articleId2 = myBaseAdapter.secondArticle.getId();
            String articleId3 = myBaseAdapter.thirdArticle.getId();

            Log.d("FlipArticlesFragment", "Article Id1 is " + articleId1 + "Article Id2 is " + articleId2 + " Article Id3 is " + articleId3);
            readArticleIds.add(articleId1);
            readArticleIds.add(articleId2);
            readArticleIds.add(articleId3);

            articlesIdsHashSet.add(readArticleIds);
            Iterator<List<String>> itr = articlesIdsHashSet.iterator();
            List<String> tempList = new ArrayList<String>();
            while(itr.hasNext()){
                tempList = itr.next();
            }
            List<String> articlesList = new ArrayList<String>(tempList);

            String userId = preferenceEndPoint.getStringPreference(Constants.USER_ID);

            SharedPreferences.Editor editor = MagazinePreferenceEndPoint.getInstance().get(getActivity(), userId);
            editor.putString("read_article_ids", new Gson().toJson(articlesList));
            editor.commit();

            String sharedCachedMagazines = MagazinePreferenceEndPoint.getInstance().getPref(getActivity(), userId).getString("cached_magazines", "");
            Type type = new TypeToken<List<Articles>>() {
            }.getType();
            String cachedMagazines = sharedCachedMagazines;
            List<Articles> cachedMagazinesList = new Gson().fromJson(cachedMagazines, type);
            cachedMagazinesList.remove(myBaseAdapter.getItem(0));
            cachedMagazinesList.remove(myBaseAdapter.secondArticle);
            cachedMagazinesList.remove(myBaseAdapter.thirdArticle);

            editor.putString("cached_magazines", new Gson().toJson(cachedMagazinesList));
            editor.commit();

        } else {
            String articleId1 = myBaseAdapter.getItem(position).getId();
            String articleId2 = myBaseAdapter.secondArticle.getId();
            String articleId3 = myBaseAdapter.thirdArticle.getId();

            Log.d("FlipArticlesFragment", "Article Id1 is " + articleId1 + "Article Id2 is " + articleId2 + " Article Id3 is " + articleId3);
            readArticleIds.add(articleId1);
            readArticleIds.add(articleId2);
            readArticleIds.add(articleId3);
            String articleId = myBaseAdapter.getItem(position).getId();
            Log.d("FlipArticlesFragment", "Article Id is " + articleId);

            readArticleIds.add(articleId);

            articlesIdsHashSet.add(readArticleIds);
            Iterator<List<String>> itr = articlesIdsHashSet.iterator();
            List<String> tempList = new ArrayList<String>();
            while(itr.hasNext()){
                tempList = itr.next();
            }
            List<String> articlesList = new ArrayList<String>(tempList);

            String userId = preferenceEndPoint.getStringPreference(Constants.USER_ID);
            SharedPreferences.Editor editor = MagazinePreferenceEndPoint.getInstance().get(getActivity(), userId);
            editor.putString("read_article_ids", new Gson().toJson(articlesList));
            editor.commit();

            String sharedCachedMagazines = MagazinePreferenceEndPoint.getInstance().getPref(getActivity(), userId).getString("cached_magazines", "");
            Type type = new TypeToken<List<Articles>>() {
            }.getType();
            String cachedMagazines = sharedCachedMagazines;
            List<Articles> cachedMagazinesList = new Gson().fromJson(cachedMagazines, type);
            cachedMagazinesList.remove(myBaseAdapter.getItem(0));
            cachedMagazinesList.remove(myBaseAdapter.secondArticle);
            cachedMagazinesList.remove(myBaseAdapter.thirdArticle);
            cachedMagazinesList.remove(myBaseAdapter.getItem(position));

            editor.putString("cached_magazines", new Gson().toJson(cachedMagazinesList));
            editor.commit();
        }*/

        if(position+2 == myBaseAdapter.getCount()/2 && !isArticlesEndReached) {
            pageCount++;
            getMoreArticlesWithPagination();
        }
    }

    public void onEventMainThread(String action) {
        if (Constants.OTHERS_MAGAZINE_ACTION.equals(action) || Constants.TOPIC_NOTIFICATION_ACTION.equals(action) || Constants.TOPIC_FOLLOWING_ACTION.equals(action)) {
            loadArticles(null);
        }
    }

    private void getArticlesWithPagination() {
        isArticlesEndReached = false;
        yoService.getArticlesWithPaginationAPI(accessToken, 1, 200).enqueue(new Callback<List<Articles>>() {
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
                LinkedHashSet<List<Articles>> articlesHashSet = new LinkedHashSet<>();
                if (response.body() != null && !response.body().isEmpty()) {
                    articlesHashSet.add(response.body());
                    Iterator<List<Articles>> itr = articlesHashSet.iterator();
                    List<Articles> tempList = new ArrayList<Articles>();
                    while(itr.hasNext()){
                        tempList = itr.next();
                    }
                    List<Articles> articlesList = new ArrayList<Articles>(tempList);
                    //myBaseAdapter.addItems(response.body());
                    String userId = preferenceEndPoint.getStringPreference(Constants.USER_ID);
                        String readCachedIds = MagazinePreferenceEndPoint.getInstance().getPref(getActivity(), userId).getString("read_article_ids", "");
                    if(!TextUtils.isEmpty(readCachedIds)) {
                        Type type1 = new TypeToken<List<String>>() {
                        }.getType();
                        String cachedIds = readCachedIds;
                        List<String> cachedReadList = new Gson().fromJson(cachedIds, type1);
                        for (int i = 0; i < articlesList.size(); i++) {
                            for (int j = 0; j < cachedReadList.size(); j++) {
                                if (articlesList.size()>0 && articlesList.get(i).getId().equals(cachedReadList.get(j)))
                                    articlesList.remove(i);
                                //Log.d("FlipArticlesFragment", "Cached Article Name is " + cachedMagazinesList.get(i).getTitle() + " Cached Articles size " + cachedMagazinesList.size());
                            }
                        }
                    }
                    myBaseAdapter.addItems(articlesList);
                    mLog.d("Magazines", "lastReadArticle" + lastReadArticle);
                    flipView.flipTo(lastReadArticle);

                    //lruCacheHelper.put("magazines_cache", response.body());
                    if(!isSearch) {
                        /*if(!TextUtils.isEmpty(preferenceEndPoint.getStringPreference("cached_magazines"))) {
                            preferenceEndPoint.removePreference("cached_magazines");
                        }*/
                        //String userId = preferenceEndPoint.getStringPreference(Constants.USER_ID);
                        SharedPreferences.Editor editor = MagazinePreferenceEndPoint.getInstance().get(getActivity(), userId);
                        editor.putString("cached_magazines", new Gson().toJson(articlesList));
                        editor.commit();
                        //preferenceEndPoint.saveStringPreference("cached_magazines", new Gson().toJson(response.body()));

                        /*if(flipView.getCurrentPage() == 0) {
                            String articleId1 = myBaseAdapter.getItem(0).getId();
                            String articleId2 = myBaseAdapter.secondArticle.getId();
                            String articleId3 = myBaseAdapter.thirdArticle.getId();

                            Log.d("FlipArticlesFragment", "Article Id1 is " + articleId1 + "Article Id2 is " + articleId2 + " Article Id3 is " + articleId3);
                            readArticleIds.add(articleId1);
                            readArticleIds.add(articleId2);
                            readArticleIds.add(articleId3);

                            articlesIdsHashSet.add(readArticleIds);
                            Iterator<List<String>> itr1 = articlesIdsHashSet.iterator();
                            List<String> tempList1 = new ArrayList<String>();
                            while(itr1.hasNext()){
                                tempList1 = itr1.next();
                            }
                            List<String> articlesList1 = new ArrayList<String>(tempList1);

                            editor.putString("read_article_ids", new Gson().toJson(articlesList1));
                            editor.commit();

                            String sharedCachedMagazines = MagazinePreferenceEndPoint.getInstance().getPref(getActivity(), userId).getString("cached_magazines", "");
                            Type type = new TypeToken<List<Articles>>() {
                            }.getType();
                            String cachedMagazines = sharedCachedMagazines;
                            List<Articles> cachedMagazinesList = new Gson().fromJson(cachedMagazines, type);
                            cachedMagazinesList.remove(myBaseAdapter.getItem(0));
                            cachedMagazinesList.remove(myBaseAdapter.secondArticle);
                            cachedMagazinesList.remove(myBaseAdapter.thirdArticle);

                            editor.putString("cached_magazines", new Gson().toJson(cachedMagazinesList));
                            editor.commit();
                        }*/
                    }
                    if (llNoArticles != null) {
                        llNoArticles.setVisibility(View.GONE);
                        flipContainer.setVisibility(View.VISIBLE);
                        if(myBaseAdapter.getCount()>0) {
                            Random r = new Random();
                            suggestionsPosition = r.nextInt(myBaseAdapter.getCount() - 0) + 0;
                            myBaseAdapter.getAllItems().add(suggestionsPosition,new Articles());
                            myBaseAdapter.notifyDataSetChanged();
                        }
                    }
                } else {
                    flipContainer.setVisibility(View.VISIBLE);
                    llNoArticles.setVisibility(View.GONE);
                    getCachedArticles();
                }
            }

            @Override
            public void onFailure(Call<List<Articles>> call, Throwable t) {
                if (mProgress != null) {
                    mProgress.setVisibility(View.GONE);
                }
                tvProgressText.setVisibility(View.GONE);
                flipContainer.setVisibility(View.GONE);
                llNoArticles.setVisibility(View.VISIBLE);
            }
        });
    }

    private void getMoreArticlesWithPagination() {
        yoService.getArticlesWithPaginationAPI(accessToken, pageCount, 200).enqueue(new Callback<List<Articles>>() {
            @Override
            public void onResponse(Call<List<Articles>> call, Response<List<Articles>> response) {
                /*if (!isAdded()) {
                    return;
                }
                myBaseAdapter.clear();*/
                if (mProgress != null) {
                    mProgress.setVisibility(View.GONE);
                }
                tvProgressText.setVisibility(View.GONE);
                LinkedHashSet<List<Articles>> articlesHashSet = new LinkedHashSet<>();
                if (response.body() != null && !response.body().isEmpty()) {
                    articlesHashSet.add(response.body());
                    Iterator<List<Articles>> itr = articlesHashSet.iterator();
                    List<Articles> tempList = new ArrayList<Articles>();
                    while(itr.hasNext()){
                        tempList = itr.next();
                    }
                    List<Articles> articlesList = new ArrayList<Articles>(tempList);
                    //myBaseAdapter.addItemsAll(response.body());
                    myBaseAdapter.addItemsAll(articlesList);
                    mLog.d("Magazines", "lastReadArticle" + lastReadArticle);
                    flipView.flipTo(lastReadArticle);
                    //lruCacheHelper.put("magazines_cache", response.body());
                    if(!isSearch) {
                        /*if(!TextUtils.isEmpty(preferenceEndPoint.getStringPreference("cached_magazines"))) {
                            preferenceEndPoint.removePreference("cached_magazines");
                        }*/
                        String userId = preferenceEndPoint.getStringPreference(Constants.USER_ID);
                        SharedPreferences.Editor editor = MagazinePreferenceEndPoint.getInstance().get(getActivity(), userId);
                        editor.putString("cached_magazines", new Gson().toJson(myBaseAdapter.getAllItems()));
                        editor.commit();
                        //preferenceEndPoint.saveStringPreference("cached_magazines", new Gson().toJson(myBaseAdapter.getAllItems()));
                    }
                    if (llNoArticles != null) {
                        llNoArticles.setVisibility(View.GONE);
                        flipContainer.setVisibility(View.VISIBLE);
                        if(myBaseAdapter.getCount()>0) {
                            Random r = new Random();
                            suggestionsPosition = r.nextInt(myBaseAdapter.getCount() - 0) + 0;
                            myBaseAdapter.getAllItems().add(suggestionsPosition,new Articles());
                            myBaseAdapter.notifyDataSetChanged();
                        }
                    }
                } else {
                    if(response.body().isEmpty()) {
                     isArticlesEndReached = true;
                    }
                    flipContainer.setVisibility(View.VISIBLE);
                    llNoArticles.setVisibility(View.GONE);
                    //getCachedArticles();
                }
            }

            @Override
            public void onFailure(Call<List<Articles>> call, Throwable t) {
                if (mProgress != null) {
                    mProgress.setVisibility(View.GONE);
                }
                tvProgressText.setVisibility(View.GONE);
                flipContainer.setVisibility(View.GONE);
                llNoArticles.setVisibility(View.VISIBLE);
            }
        });
    }

    private void removeFirstPageArticles() {
            String articleId1 = myBaseAdapter.getItem(0).getId();
            String articleId2 = myBaseAdapter.secondArticle.getId();
            String articleId3 = myBaseAdapter.thirdArticle.getId();

            Log.d("FlipArticlesFragment", "Article Id1 is " + articleId1 + "Article Id2 is " + articleId2 + " Article Id3 is " + articleId3);
            readArticleIds.add(articleId1);
            readArticleIds.add(articleId2);
            readArticleIds.add(articleId3);

            articlesIdsHashSet.add(readArticleIds);
            Iterator<List<String>> itr1 = articlesIdsHashSet.iterator();
            List<String> tempList1 = new ArrayList<String>();
            while(itr1.hasNext()){
                tempList1 = itr1.next();
            }
            List<String> articlesList1 = new ArrayList<String>(tempList1);
            String userId = preferenceEndPoint.getStringPreference(Constants.USER_ID);
            SharedPreferences.Editor editor = MagazinePreferenceEndPoint.getInstance().get(getActivity(), userId);
            editor.putString("read_article_ids", new Gson().toJson(articlesList1));
            editor.commit();

            String sharedCachedMagazines = MagazinePreferenceEndPoint.getInstance().getPref(getActivity(), userId).getString("cached_magazines", "");
            Type type = new TypeToken<List<Articles>>() {
            }.getType();
            String cachedMagazines = sharedCachedMagazines;
            List<Articles> cachedMagazinesList = new Gson().fromJson(cachedMagazines, type);
            cachedMagazinesList.remove(myBaseAdapter.getItem(0));
            cachedMagazinesList.remove(myBaseAdapter.secondArticle);
            cachedMagazinesList.remove(myBaseAdapter.thirdArticle);

            editor.putString("cached_magazines", new Gson().toJson(cachedMagazinesList));
            editor.commit();
    }

    public void removeReadArticles() {

        if(currentFlippedPosition >0) {
            String articleId2 = myBaseAdapter.secondArticle.getId();
            String articleId3 = myBaseAdapter.thirdArticle.getId();
            readArticleIds.add(articleId2);
            readArticleIds.add(articleId3);


            Log.d("FlipArticlesFragment", "currentFlippedPosition outside loop " + currentFlippedPosition);
            for (int i = 0; i <= currentFlippedPosition; i++) {
                String articleId = myBaseAdapter.getItem(i).getId();
                Log.d("FlipArticlesFragment", "Article Id is " + articleId + "currentFlippedPosition " + currentFlippedPosition + " Article Name is " + myBaseAdapter.getItem(i).getTitle() + " Articles size " + myBaseAdapter.getCount());

                readArticleIds.add(articleId);
            }


            articlesIdsHashSet.add(readArticleIds);
            Iterator<List<String>> itr = articlesIdsHashSet.iterator();
            List<String> tempList = new ArrayList<String>();
            while (itr.hasNext()) {
                tempList = itr.next();
            }
            List<String> articlesList = new ArrayList<String>(tempList);

            String userId = preferenceEndPoint.getStringPreference(Constants.USER_ID);
            SharedPreferences.Editor editor = MagazinePreferenceEndPoint.getInstance().get(getActivity(), userId);
            editor.putString("read_article_ids", new Gson().toJson(articlesList));
            editor.commit();


            String sharedCachedMagazines = MagazinePreferenceEndPoint.getInstance().getPref(getActivity(), userId).getString("cached_magazines", "");
            Type type = new TypeToken<List<Articles>>() {
            }.getType();
            String cachedMagazines = sharedCachedMagazines;
            List<Articles> cachedMagazinesList = new Gson().fromJson(cachedMagazines, type);
            //if(currentFlippedPosition >0) {
            cachedMagazinesList.remove(myBaseAdapter.secondArticle);
            cachedMagazinesList.remove(myBaseAdapter.thirdArticle);
            //}
            /*for (int i = 0; i <= currentFlippedPosition; i++) {
                cachedMagazinesList.remove(i);
                Log.d("FlipArticlesFragment", "Cached Article Name is " + cachedMagazinesList.get(i).getTitle() + " Cached Articles size " + cachedMagazinesList.size());
            }*/
            String readCachedIds = MagazinePreferenceEndPoint.getInstance().getPref(getActivity(), userId).getString("read_article_ids", "");
            Type type1 = new TypeToken<List<String>>() {
            }.getType();
            String cachedIds = readCachedIds;
            List<String> cachedReadList = new Gson().fromJson(cachedIds, type1);
            for (int i = 0; i < cachedMagazinesList.size(); i++) {
                for(int j=0; j<cachedReadList.size(); j++) {
                    if (cachedMagazinesList.size()>0 && cachedMagazinesList.get(i).getId().equals(cachedReadList.get(j)))
                        cachedMagazinesList.remove(i);
                    Log.d("FlipArticlesFragment", "Cached Article Name is " + cachedMagazinesList.get(i).getTitle() + " Cached Articles size " + cachedMagazinesList.size());
                }
            }
        /*cachedMagazinesList.remove(myBaseAdapter.getItem(0));
        cachedMagazinesList.remove(myBaseAdapter.secondArticle);
        cachedMagazinesList.remove(myBaseAdapter.thirdArticle);
        cachedMagazinesList.remove(myBaseAdapter.getItem(position));*/

            editor.putString("cached_magazines", new Gson().toJson(cachedMagazinesList));
            editor.commit();
        }

    }
}
