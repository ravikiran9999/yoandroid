package com.yo.android.adapters;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.orion.android.common.preferences.PreferenceEndPoint;
//import com.squareup.picasso.Picasso;
import com.yo.android.R;
import com.yo.android.api.YoApi;
import com.yo.android.di.Injector;
import com.yo.android.helpers.FindPeopleViewHolder;
import com.yo.android.model.FindPeople;
import com.yo.android.ui.BaseActivity;
import com.yo.android.ui.FollowingsActivity;
import com.yo.android.ui.OtherProfilesLikedArticles;
import com.yo.android.util.Constants;

import javax.inject.Inject;
import javax.inject.Named;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by MYPC on 7/17/2016.
 */
public class FindPeopleAdapter extends AbstractBaseAdapter<FindPeople, FindPeopleViewHolder> {

    private Context context;
    @Inject
    YoApi.YoService yoService;

    @Inject
    @Named("login")
    protected PreferenceEndPoint preferenceEndPoint;
    private boolean isFollowingUser;

    public FindPeopleAdapter(Context context) {
        super(context);
        this.context = context;
        Injector.obtain(context.getApplicationContext()).inject(this);
    }

    @Override
    public int getLayoutId() {
        return R.layout.find_people_list_item;
    }

    @Override
    public boolean isEnabled(int position) {
        if ("true".equals(getItem(position).getSelf())) {
            return false;
        }
        return super.isEnabled(position);
    }

    @Override
    public FindPeopleViewHolder getViewHolder(View convertView) {
        return new FindPeopleViewHolder(convertView);
    }

    @Override
    public void bindView(final int position, final FindPeopleViewHolder holder, final FindPeople item) {

        if (item.getAvatar() == null || TextUtils.isEmpty(item.getAvatar())) {
            Glide.with(context).load(R.drawable.dynamic_profile)
                    .dontAnimate()
                    .placeholder(R.drawable.dynamic_profile)
                    .error(R.drawable.dynamic_profile)
                    .fitCenter()
                    .into(holder.getImvFindPeoplePic());
        } else {
            Glide.with(context).load(item.getAvatar())
                    .dontAnimate()
                    .placeholder(R.drawable.dynamic_profile)
                    .error(R.drawable.dynamic_profile)
                    .fitCenter()
                    .into(holder.getImvFindPeoplePic());
        }
        if (!TextUtils.isEmpty(item.getFirst_name())) {
            holder.getTvFindPeopleName().setText(item.getFirst_name() + " " + item.getLast_name());
        } else {
            holder.getTvFindPeopleName().setText("Unknown");
        }
        holder.getTvFindPeopleDesc().setText(item.getDescription());
        if ("true".equals(item.getIsFollowing())) {
            holder.getBtnFindPeopleFollow().setText("Following");
            holder.getBtnFindPeopleFollow().setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_following_tick, 0, 0, 0);
            isFollowingUser = true;
        } else {
            holder.getBtnFindPeopleFollow().setText("Follow");
            holder.getBtnFindPeopleFollow().setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            isFollowingUser = false;
        }
        holder.getBtnFindPeopleFollow().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!"true".equals(item.getIsFollowing())) {
                    ((BaseActivity) context).showProgressDialog();
                    String accessToken = preferenceEndPoint.getStringPreference("access_token");
                    yoService.followUsersAPI(accessToken, item.getId()).enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            ((BaseActivity) context).dismissProgressDialog();
                            holder.getBtnFindPeopleFollow().setText("Following");
                            holder.getBtnFindPeopleFollow().setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_following_tick, 0, 0, 0);
                            item.setIsFollowing("true");
                            item.setFollowersCount(item.getFollowersCount() + 1);
                            isFollowingUser = true;
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            ((BaseActivity) context).dismissProgressDialog();
                            holder.getBtnFindPeopleFollow().setText("Follow");
                            holder.getBtnFindPeopleFollow().setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                            item.setIsFollowing("false");
                            isFollowingUser = false;
                        }
                    });
                } else {
                    if (context != null) {

                        final AlertDialog.Builder builder = new AlertDialog.Builder(context);

                        LayoutInflater layoutInflater = LayoutInflater.from(context);
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
                                ((BaseActivity) context).showProgressDialog();
                                String accessToken = preferenceEndPoint.getStringPreference("access_token");
                                yoService.unfollowUsersAPI(accessToken, item.getId()).enqueue(new Callback<ResponseBody>() {
                                    @Override
                                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                        ((BaseActivity) context).dismissProgressDialog();
                                        if ((BaseActivity) context instanceof FollowingsActivity) {
                                            removeItem(item);
                                        } else {
                                            // do nothing
                                        }
                                        holder.getBtnFindPeopleFollow().setText("Follow");
                                        holder.getBtnFindPeopleFollow().setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                                        item.setIsFollowing("false");
                                        item.setFollowersCount(item.getFollowersCount() - 1);
                                        isFollowingUser = false;
                                    }

                                    @Override
                                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                                        ((BaseActivity) context).dismissProgressDialog();
                                        holder.getBtnFindPeopleFollow().setText("Following");
                                        holder.getBtnFindPeopleFollow().setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_following_tick, 0, 0, 0);
                                        item.setIsFollowing("true");
                                        isFollowingUser = true;
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
            }
        });

        if ("true".equals(item.getSelf())) {
            holder.getBtnFindPeopleFollow().setVisibility(View.GONE);
        } else {
            holder.getBtnFindPeopleFollow().setVisibility(View.VISIBLE);
        }

    }

    @Override
    protected boolean hasData(FindPeople findPeople, String key) {
        if (findPeople.getFirst_name() != null && findPeople.getLast_name() != null) {
            if (containsValue(findPeople.getFirst_name().toLowerCase(), key)
                    || containsValue(findPeople.getLast_name().toLowerCase(), key)) {
                return true;
            }
        }
        return super.hasData(findPeople, key);
    }

    private boolean containsValue(String str, String key) {
        return str != null && str.toLowerCase().contains(key);
    }
}
