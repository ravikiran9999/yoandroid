package com.yo.android.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

//import com.squareup.picasso.Picasso;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.yo.android.R;
import com.yo.android.adapters.TabsPagerAdapter;
import com.yo.android.util.Constants;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OthersProfileActivity extends BaseActivity {
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private ImageView backbtn;
    TabsPagerAdapter mAdapter;
    private List<ProfileTabsData> dataList;
    String userId;
    private boolean isFollowingUser;
    private int magazinesCount;
    private int followersCount;
    private int likedArticlesCount;
    private Button btnFolow;

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
        backbtn = (ImageView) findViewById(R.id.back);
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        mAdapter = new TabsPagerAdapter(getSupportFragmentManager());
        mAdapter.addFragment(new OthersProfileMagazines(), null);
        mAdapter.addFragment(new OtherProfilesFollowers(), null);
        mAdapter.addFragment(new OtherProfilesLikedArticles(), null);
        viewPager.setAdapter(mAdapter);
        CircleImageView picture = (CircleImageView) findViewById(R.id.picture);
        TextView tvName = (TextView) findViewById(R.id.follower_name);
        btnFolow = (Button) findViewById(R.id.follow_btn);

        magazinesCount = getIntent().getIntExtra("MagazinesCount", 0);
        followersCount = getIntent().getIntExtra("FollowersCount", 0);
        likedArticlesCount = getIntent().getIntExtra("LikedArticlesCount", 0);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
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
            Glide.with(this).load(pic)
                    .dontAnimate()
                    .fitCenter()
                    .into(picture);
        }

        tvName.setText(name);

        if (isFollowing.equals("true")) {
            btnFolow.setText("Following");
            btnFolow.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_following_tick, 0, 0, 0);
            isFollowingUser = true;
        } else {
            btnFolow.setText("Follow");
            btnFolow.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            isFollowingUser = false;
        }

        btnFolow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isFollowingUser) {
                    showProgressDialog();
                    String accessToken = preferenceEndPoint.getStringPreference("access_token");
                    yoService.followUsersAPI(accessToken, userId).enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            dismissProgressDialog();
                            btnFolow.setText("Following");
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
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            dismissProgressDialog();
                            btnFolow.setText("Follow");
                            btnFolow.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                            isFollowingUser = false;
                        }
                    });
                } else {


                    final AlertDialog.Builder builder = new AlertDialog.Builder(OthersProfileActivity.this);

                    LayoutInflater layoutInflater = LayoutInflater.from(OthersProfileActivity.this);
                    final View view = layoutInflater.inflate(R.layout.unfollow_alert_dialog, null);
                    builder.setView(view);

                    Button yesBtn = (Button) view.findViewById(R.id.yes_btn);
                    Button noBtn = (Button) view.findViewById(R.id.no_btn);


                    final AlertDialog alertDialog = builder.create();
                    alertDialog.setCancelable(false);
                    alertDialog.show();

                    yesBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            alertDialog.dismiss();
                            showProgressDialog();
                            String accessToken = preferenceEndPoint.getStringPreference("access_token");
                            yoService.unfollowUsersAPI(accessToken, userId).enqueue(new Callback<ResponseBody>() {
                                @Override
                                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                    dismissProgressDialog();
                                    btnFolow.setText("Follow");
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
                                }

                                @Override
                                public void onFailure(Call<ResponseBody> call, Throwable t) {
                                    dismissProgressDialog();
                                    btnFolow.setText("Following");
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

    public View setTabs(final String title, final int count, final boolean isLast) {
        final View view = LayoutInflater.from(this).inflate(R.layout.profile_tab_holder, null);
        // We need to manually set the LayoutParams here because we don't have a view root
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        ((TextView) view.findViewById(R.id.count)).setText(String.valueOf(count));
        ((TextView) view.findViewById(R.id.tab_name)).setText(title);
        if (isLast) {
            view.findViewById(R.id.divider).setVisibility(View.GONE);
        }

        return view;
    }

    protected List<ProfileTabsData> createTabsList() {
        List<ProfileTabsData> list = new ArrayList<>();
        list.add(new ProfileTabsData("Magazines", magazinesCount));
        list.add(new ProfileTabsData("Followers", followersCount));
        list.add(new ProfileTabsData("Liked Articles", likedArticlesCount));

        return list;
    }


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

    public void updateMagazinesCount() {
        magazinesCount = magazinesCount - 1;
        dataList.get(0).setCount(magazinesCount);
        ((TextView) tabLayout.getTabAt(0).getCustomView().findViewById(R.id.count)).setText(String.valueOf(magazinesCount));
    }
}
