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

import com.aphidmobile.flip.FlipViewController;
import com.orion.android.common.util.ConnectivityHelper;
import com.yo.android.R;
import com.yo.android.adapters.MagazineArticlesBaseAdapter;
import com.yo.android.api.YoApi;
import com.yo.android.chat.ui.fragments.BaseFragment;
import com.yo.android.model.Articles;
import com.yo.android.ui.FollowMoreTopicsActivity;
import com.yo.android.ui.fragments.MagazinesFragment;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import se.emilsjolander.flipview.FlipView;

/**
 * Created by creatives on 6/30/2016.
 */
public class MagazineFlipArticlesFragment extends BaseFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    //private FlipViewController flipView;
    private static MagazineTopicsSelectionFragment magazineTopicsSelectionFragment;
    private MagazineArticlesBaseAdapter myBaseAdapter;
    @Inject
    YoApi.YoService yoService;
    private String topicName;
    private LinearLayout llNoArticles;
    private FrameLayout flipContainer;
    private ProgressBar mProgress;
    private Button followMoreTopics;
    private boolean isFollowing;

    @Inject
    ConnectivityHelper mHelper;
    private FrameLayout articlesRootLayout;
    private TextView networkFailureText;

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
        //flipView = new FlipViewController(getActivity());
        FlipView flipView = (FlipView) view.findViewById(R.id.flip_view);
        myBaseAdapter = new MagazineArticlesBaseAdapter(getActivity(), preferenceEndPoint, yoService, mToastFactory);
        flipView.setAdapter(myBaseAdapter);
        //flipView.setAdapter(myBaseAdapter);
        //flipContainer.addView(flipView);
        followMoreTopics = (Button) view.findViewById(R.id.btn_magazine_follow_topics);
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (!TextUtils.isEmpty(preferenceEndPoint.getStringPreference("magazine_tags"))) {
            String[] prefTags = TextUtils.split(preferenceEndPoint.getStringPreference("magazine_tags"), ",");
            if (prefTags != null) {
                List<String> tagIds = Arrays.asList(prefTags);
                loadArticles(null);
            }
        } else {
            mProgress.setVisibility(View.GONE);
            flipContainer.setVisibility(View.GONE);
            llNoArticles.setVisibility(View.VISIBLE);
        }

        followMoreTopics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), FollowMoreTopicsActivity.class);
                intent.putExtra("From", "Magazines");
                startActivity(intent);
            }
        });

    }

    public void loadArticles(List<String> tagIds) {

        if (!mHelper.isConnected()) {
            myBaseAdapter.clear();
            if (articlesRootLayout.getChildCount() > 0) {
                articlesRootLayout.setVisibility(View.GONE);
                networkFailureText.setText(getActivity().getResources().getString(R.string.unable_to_fetch));
                networkFailureText.setVisibility(View.VISIBLE);
            }
            return;
        } else {
            articlesRootLayout.setVisibility(View.VISIBLE);
            networkFailureText.setVisibility(View.GONE);
        }

        if (mProgress != null) {
            mProgress.setVisibility(View.VISIBLE);
        }
        String accessToken = preferenceEndPoint.getStringPreference("access_token");
        if (tagIds != null) {
            yoService.getArticlesAPI(accessToken, tagIds).enqueue(callback);
        } else {
            yoService.getUserArticlesAPI(accessToken).enqueue(callback);
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
                if (llNoArticles != null) {
                    llNoArticles.setVisibility(View.GONE);
                    flipContainer.setVisibility(View.VISIBLE);
                }
            } else {
                if (llNoArticles != null) {
                    flipContainer.setVisibility(View.GONE);
                    //flipView.refreshAllPages();
                    llNoArticles.setVisibility(View.VISIBLE);
                }
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
                    if (llNoArticles != null) {
                        networkFailureText.setVisibility(View.GONE);
                        llNoArticles.setVisibility(View.VISIBLE);
                        flipContainer.setVisibility(View.GONE);
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
    }

    @Override
    public void onResume() {
        super.onResume();
        //flipView.onResume();
    }

    public void onPause() {
        super.onPause();
        //flipView.onPause();
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("magazine_tags")) {
            if (!TextUtils.isEmpty(preferenceEndPoint.getStringPreference("magazine_tags"))) {
                String[] prefTags = TextUtils.split(preferenceEndPoint.getStringPreference("magazine_tags"), ",");
                if (prefTags != null) {
                    List<String> tagIds = Arrays.asList(prefTags);
                    loadArticles(null);
                }
            } else {
                myBaseAdapter.addItems(new ArrayList<Articles>());
                if (llNoArticles != null) {
                    llNoArticles.setVisibility(View.VISIBLE);
                    flipContainer.setVisibility(View.GONE);
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
                List<String> tagIds = Arrays.asList(prefTags);
                loadArticles(null);
            }
        }
    }


}
