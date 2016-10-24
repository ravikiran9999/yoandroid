package com.yo.android.flip;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
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
import com.yo.android.model.Articles;
import com.yo.android.ui.FollowMoreTopicsActivity;
import com.yo.android.ui.fragments.MagazinesFragment;
import com.yo.android.util.Constants;

import java.lang.reflect.Type;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import se.emilsjolander.flipview.FlipView;

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
    private ProgressBar mProgress;
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
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 60 && resultCode == getActivity().RESULT_OK) {
            if (data != null) {
                Articles topic = data.getParcelableExtra("UpdatedTopic");
                int pos = data.getIntExtra("Pos", 0);
                myBaseAdapter.updateTopic(topic, pos);
            }

        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mLog.d("onActivityCreated", "In onActivityCreated");

        if (!TextUtils.isEmpty(preferenceEndPoint.getStringPreference("magazine_tags"))) {
            String[] prefTags = TextUtils.split(preferenceEndPoint.getStringPreference("magazine_tags"), ",");
            if (prefTags != null) {
                //loadArticles(null);
                getCachedArticles();
            }
        } else {
            mProgress.setVisibility(View.GONE);
            /*flipContainer.setVisibility(View.GONE);
            llNoArticles.setVisibility(View.VISIBLE);*/
            flipContainer.setVisibility(View.VISIBLE);
            llNoArticles.setVisibility(View.GONE);
            //loadArticles(null);
            getCachedArticles();
        }

        followMoreTopics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), FollowMoreTopicsActivity.class);
                intent.putExtra("From", "Magazines");
                startActivity(intent);
            }
        });

        //YODialogs.showPopup(getActivity(), "YO! Get $50 Cashback on Recharge of $100+", "YO! is doing a wonderful promotion of its wallet feature by collaborating with many banking companies these days and they are proving some specific coupon codes to obtain the benefit of adding money in Yo! wallet.");

    }

    public void getCachedArticles() {
        isSearch = false;
        if (!TextUtils.isEmpty(preferenceEndPoint.getStringPreference("cached_magazines"))) {
            if (mProgress != null) {
                mProgress.setVisibility(View.GONE);
            }
            myBaseAdapter.clear();
                /*if (articlesRootLayout.getChildCount() > 0) {
                    articlesRootLayout.setVisibility(View.GONE);
                    networkFailureText.setText(getActivity().getResources().getString(R.string.unable_to_fetch));
                    networkFailureText.setVisibility(View.VISIBLE);
                }*/
            Type type = new TypeToken<List<Articles>>() {
            }.getType();
            String cachedMagazines = preferenceEndPoint.getStringPreference("cached_magazines", null);
            List<Articles> cachedMagazinesList = new Gson().fromJson(cachedMagazines, type);
            myBaseAdapter.addItems(cachedMagazinesList);
            flipView.flipTo(lastReadArticle);
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
            if (!TextUtils.isEmpty(preferenceEndPoint.getStringPreference("cached_magazines"))) {
                if (mProgress != null) {
                    mProgress.setVisibility(View.GONE);
                }
                myBaseAdapter.clear();
                /*if (articlesRootLayout.getChildCount() > 0) {
                    articlesRootLayout.setVisibility(View.GONE);
                    networkFailureText.setText(getActivity().getResources().getString(R.string.unable_to_fetch));
                    networkFailureText.setVisibility(View.VISIBLE);
                }*/
                Type type = new TypeToken<List<Articles>>() {
                }.getType();
                String cachedMagazines = preferenceEndPoint.getStringPreference("cached_magazines", null);
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
        String accessToken = preferenceEndPoint.getStringPreference("access_token");
        if (tagIds != null) {
            isSearch = true;
            yoService.getArticlesAPI(accessToken, tagIds).enqueue(callback);
        } else {
            //yoService.getUserArticlesAPI(accessToken).enqueue(callback);
            isSearch = false;
            yoService.getAllArticlesAPI(accessToken).enqueue(new Callback<List<Articles>>() {
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
                        if(!isSearch) {
                            if(!TextUtils.isEmpty(preferenceEndPoint.getStringPreference("cached_magazines"))) {
                                preferenceEndPoint.removePreference("cached_magazines");
                            }
                            preferenceEndPoint.saveStringPreference("cached_magazines", new Gson().toJson(response.body()));
                        }
                        if (llNoArticles != null) {
                            llNoArticles.setVisibility(View.GONE);
                            flipContainer.setVisibility(View.VISIBLE);
                            if(myBaseAdapter.getCount()>0) {
                                Random r = new Random();
                                suggestionsPosition = r.nextInt(myBaseAdapter.getCount() - 0) + 0;
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

                }
            });
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
                        flipContainer.setVisibility(View.VISIBLE);
                        llNoArticles.setVisibility(View.GONE);
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

    @Override
    public void onResume() {
        super.onResume();
    }

    public void onPause() {
        super.onPause();
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
    }

    public void onEventMainThread(String action) {
        if (Constants.OTHERS_MAGAZINE_ACTION.equals(action) || Constants.TOPIC_NOTIFICATION_ACTION.equals(action) || Constants.TOPIC_FOLLOWING_ACTION.equals(action)) {
            loadArticles(null);
        }
    }
}
