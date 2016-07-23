package com.yo.android.ui;

import android.app.Dialog;
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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
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

/**
 * Created by root on 15/7/16.
 */
public class OthersProfileActivity extends BaseActivity {
    public static TabLayout tabLayout;
    private ViewPager viewPager;
    private ImageView backbtn;
    TabsPagerAdapter mAdapter;
    private List<ProfileTabsData> dataList;
    String userId;
    private boolean isFollowingUser;

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
        mAdapter.addFragment(new OtherProfilesLinedArticles(), null);
        viewPager.setAdapter(mAdapter);
        CircleImageView picture = (CircleImageView) findViewById(R.id.picture);
        TextView tvName = (TextView) findViewById(R.id.follower_name);
        final Button btnFolow = (Button) findViewById(R.id.follow_btn);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        dataList = createTabsList();
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


        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        viewPager.setCurrentItem(2);
        viewPager.setCurrentItem(1);
        viewPager.setCurrentItem(0);

        userId = getIntent().getStringExtra(Constants.USER_ID);
        String name = getIntent().getStringExtra("PersonName");
        String pic = getIntent().getStringExtra("PersonPic");
        String isFollowing = getIntent().getStringExtra("PersonIsFollowing");

        if(!TextUtils.isEmpty(pic)) {
            Picasso.with(this)
                    .load(pic)
                    .fit()
                    .into(picture);
        }
        else {
            Picasso.with(this)
                    .load(R.drawable.ic_contacts)
                    .fit()
                    .into(picture);
        }

        tvName.setText(name);

        if(isFollowing.equals("true")) {
            btnFolow.setText("Following");
            btnFolow.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_following_tick, 0, 0, 0);
            isFollowingUser = true;
        }
        else {
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

                    final Dialog dialog = new Dialog(OthersProfileActivity.this);
                    dialog.setContentView(R.layout.unfollow_alert_dialog);

                    dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                    Button btnCancel = (Button) dialog.findViewById(R.id.btn_cancel);

                    Button btnUnfollow = (Button) dialog.findViewById(R.id.btn_unfollow);

                    btnCancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });

                    btnUnfollow.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                            showProgressDialog();
                            String accessToken = preferenceEndPoint.getStringPreference("access_token");
                            yoService.unfollowUsersAPI(accessToken, userId).enqueue(new Callback<ResponseBody>() {
                                @Override
                                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                    dismissProgressDialog();
                                    btnFolow.setText("Follow");
                                    btnFolow.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
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

                    dialog.show();
                }
            }
        });

    }

    public View setTabs(final String title, final int count, final boolean isLast) {
        final View view = LayoutInflater.from(this).inflate(R.layout.profile_tab_holder, null);
        // We need to manually set the LayoutParams here because we don't have a view root
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        ((TextView) view.findViewById(R.id.count)).setText(String.valueOf(count));
        ((TextView) view.findViewById(R.id.tab_name)).setText(title);
        if (isLast) {
            ((View) view.findViewById(R.id.divider)).setVisibility(View.GONE);
        }

        return view;
    }

    protected List<ProfileTabsData> createTabsList() {
        List<ProfileTabsData> list = new ArrayList<>();
        list.add(new ProfileTabsData("Magazines", 0));
        list.add(new ProfileTabsData("Followers", 0));
        list.add(new ProfileTabsData("Liked Articles", 0));

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
    }
}
