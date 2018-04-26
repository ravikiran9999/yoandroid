package com.yo.android.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.yo.android.R;
import com.yo.android.adapters.TabsPagerAdapter;
import com.yo.android.api.YoApi;
import com.yo.android.util.Constants;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * This activity contains the Other Profiles Magazines, Followers and Liked Articles tabs
 */
public class OthersProfileActivity extends BaseActivity {

    @Bind(R.id.tabs)
    TabLayout tabLayout;
    @Bind(R.id.viewpager)
    ViewPager viewPager;
    @Bind(R.id.back)
    ImageView backbtn;
    @Bind(R.id.follow_btn)
    Button btnFolow;
    @Bind(R.id.follower_name)
    TextView tvName;
    @Bind(R.id.picture)
    CircleImageView picture;

    TabsPagerAdapter mAdapter;
    private List<ProfileTabsData> dataList;
    String userId;
    private boolean isFollowingUser;
    private int magazinesCount;
    private int followersCount;
    private int likedArticlesCount;


    private static Fragment currentFragment;

    public static Fragment getCurrentFragment() {
        return currentFragment;
    }

    public static void setCurrentFragment(Fragment currentFragment) {
        OthersProfileActivity.currentFragment = currentFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_layout);
        ButterKnife.bind(this);

        mAdapter = new TabsPagerAdapter(getSupportFragmentManager());
        mAdapter.addFragment(new OthersProfileMagazines(), null);
        mAdapter.addFragment(new OtherProfilesFollowers(), null);
        mAdapter.addFragment(new OtherProfilesLikedArticles(), null);
        viewPager.setAdapter(mAdapter);

        magazinesCount = getIntent().getIntExtra("MagazinesCount", 0);
        followersCount = getIntent().getIntExtra("FollowersCount", 0);
        likedArticlesCount = getIntent().getIntExtra("LikedArticlesCount", 0);

        tabLayout.setupWithViewPager(viewPager);
        dataList = createTabsList();
        updateTitles();


        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("FollowState", btnFolow.getText());
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        viewPager.setCurrentItem(1);
        viewPager.setCurrentItem(0);

        userId = getIntent().getStringExtra(Constants.USER_ID);
        String name = getIntent().getStringExtra("PersonName");
        String pic = getIntent().getStringExtra("PersonPic");
        String isFollowing = getIntent().getStringExtra("PersonIsFollowing");

        if (!TextUtils.isEmpty(pic)) {
            RequestOptions requestOptions = new RequestOptions()
                    .dontAnimate()
                    .fitCenter();
            Glide.with(this).load(pic)
                    .apply(requestOptions)
                    .into(picture);
        }

        tvName.setText(name);

        if (isFollowing.equals("true")) {
            btnFolow.setText(R.string.following);
            btnFolow.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_following_tick, 0, 0, 0);
            isFollowingUser = true;
        } else {
            btnFolow.setText(R.string.follow);
            btnFolow.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            isFollowingUser = false;
        }

        btnFolow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isFollowingUser) {
                    showProgressDialog();
                    String accessToken = preferenceEndPoint.getStringPreference(YoApi.ACCESS_TOKEN);
                    yoService.followUsersAPI(accessToken, userId).enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            try {
                                dismissProgressDialog();
                                btnFolow.setText(R.string.following);
                                btnFolow.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_following_tick, 0, 0, 0);
                                followersCount = followersCount + 1;
                                dataList.get(1).setCount(followersCount);
                                ((TextView) tabLayout.getTabAt(1).getCustomView().findViewById(R.id.count)).setText(String.valueOf(followersCount));
                                if (mAdapter.getCount() >= 2) {
                                    if (mAdapter.getItem(1) instanceof OtherProfilesFollowers) {
                                        ((OtherProfilesFollowers) mAdapter.getItem(1)).update();
                                    }
                                }
                                isFollowingUser = true;
                            } finally {
                                if(response != null && response.body() != null) {
                                    response.body().close();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            dismissProgressDialog();
                            btnFolow.setText(R.string.follow);
                            btnFolow.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                            isFollowingUser = false;
                        }
                    });
                } else {


                    final AlertDialog.Builder builder = new AlertDialog.Builder(OthersProfileActivity.this);

                    LayoutInflater layoutInflater = LayoutInflater.from(OthersProfileActivity.this);
                    final View view = layoutInflater.inflate(R.layout.unfollow_alert_dialog, null);
                    builder.setView(view);

                    Button yesBtn = ButterKnife.findById(view, R.id.yes_btn);
                    Button noBtn = ButterKnife.findById(view, R.id.no_btn);


                    final AlertDialog alertDialog = builder.create();
                    alertDialog.setCancelable(false);
                    alertDialog.show();

                    yesBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            alertDialog.dismiss();
                            showProgressDialog();
                            String accessToken = preferenceEndPoint.getStringPreference(YoApi.ACCESS_TOKEN);
                            yoService.unfollowUsersAPI(accessToken, userId).enqueue(new Callback<ResponseBody>() {
                                @Override
                                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                    try {
                                        dismissProgressDialog();
                                        btnFolow.setText(R.string.follow);
                                        btnFolow.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                                        followersCount = followersCount - 1;
                                        dataList.get(1).setCount(followersCount);
                                        ((TextView) tabLayout.getTabAt(1).getCustomView().findViewById(R.id.count)).setText(String.valueOf(followersCount));
                                        if (mAdapter.getCount() >= 2) {
                                            if (mAdapter.getItem(1) instanceof OtherProfilesFollowers) {
                                                ((OtherProfilesFollowers) mAdapter.getItem(1)).update();
                                            }
                                        }
                                        isFollowingUser = false;
                                    } finally {
                                        if(response != null && response.body() != null) {
                                            response.body().close();
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<ResponseBody> call, Throwable t) {
                                    dismissProgressDialog();
                                    btnFolow.setText(R.string.following);
                                    btnFolow.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_following_tick, 0, 0, 0);
                                    isFollowingUser = false;
                                }
                            });
                        }
                    });


                    noBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            alertDialog.dismiss();
                        }
                    });
                }
            }
        });

    }

    /**
     * Updates the titles of the tabs
     */
    private void updateTitles() {
        int index = 0;
        for (ProfileTabsData data : dataList) {
            final TabLayout.Tab tab = tabLayout.getTabAt(index);
            if (index == 2) {
                tab.setCustomView(setTabs(data.getTitle(), data.getCount(), true));
            } else {
                tab.setCustomView(setTabs(data.getTitle(), data.getCount(), false));
            }
            index++;
        }
    }

    /**
     * Sets the tabs title and count
     * @param title The title of the tab
     * @param count The count
     * @param isLast isLast tab or not
     * @return
     */
    public View setTabs(final String title, final int count, final boolean isLast) {
        final View view = LayoutInflater.from(this).inflate(R.layout.profile_tab_holder, null);
        // We need to manually set the LayoutParams here because we don't have a view root
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        TextView countText = ButterKnife.findById(view, R.id.count);
        TextView tabTitle = ButterKnife.findById(view,R.id.tab_name);
        countText.setText(String.valueOf(count));
        tabTitle.setText(title);

        if (isLast) {
            view.findViewById(R.id.divider).setVisibility(View.GONE);
        }

        return view;
    }

    /**
     * The tabs in the Other's Profile screen
     * @return
     */
    protected List<ProfileTabsData> createTabsList() {
        List<ProfileTabsData> list = new ArrayList<>();
        list.add(new ProfileTabsData("Magazines", magazinesCount));
        list.add(new ProfileTabsData("Followers", followersCount));
        list.add(new ProfileTabsData("Liked Articles", likedArticlesCount));

        return list;
    }

    /**
     * The ProfileTabsData class
     */
    public class ProfileTabsData {

        private String title;
        private int count;

        public ProfileTabsData(String title, int count) {
            this.title = title;
            this.count = count;
        }

        public String getTitle() {
            return title;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("FollowState", btnFolow.getText());
        setResult(RESULT_OK, intent);
        finish();
        super.onBackPressed();
    }

    /**
     * Updates the magazines count
     */
    public void updateMagazinesCount() {
        magazinesCount = magazinesCount - 1;
        dataList.get(0).setCount(magazinesCount);
        ((TextView) tabLayout.getTabAt(0).getCustomView().findViewById(R.id.count)).setText(String.valueOf(magazinesCount));
    }
}
