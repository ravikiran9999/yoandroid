package com.yo.android.adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.orion.android.common.preferences.PreferenceEndPoint;
//import com.squareup.picasso.Picasso;
import com.yo.android.R;
import com.yo.android.api.YoApi;
import com.yo.android.di.Injector;
import com.yo.android.helpers.FindPeopleViewHolder;
import com.yo.android.helpers.InviteFriendsViewHolder;
import com.yo.android.helpers.Settings;
import com.yo.android.model.Contact;
import com.yo.android.model.FindPeople;
import com.yo.android.photo.TextDrawable;
import com.yo.android.photo.util.ColorGenerator;
import com.yo.android.ui.BaseActivity;
import com.yo.android.ui.FollowingsActivity;
import com.yo.android.ui.OtherProfilesLikedArticles;
import com.yo.android.util.Constants;
import com.yo.android.util.YODialogs;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Named;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

/**
 * Created by MYPC on 7/17/2016.
 */

/**
 * This adapter is used to show the list of Yo App users
 */
public class FindPeopleAdapter extends AbstractBaseAdapter<FindPeople, FindPeopleViewHolder> {

    private Context context;
    @Inject
    YoApi.YoService yoService;

    @Inject
    @Named("login")
    protected PreferenceEndPoint preferenceEndPoint;
    private boolean isFollowingUser;

    private ColorGenerator mColorGenerator = ColorGenerator.MATERIAL;
    private TextDrawable.IBuilder mDrawableBuilder;

    public FindPeopleAdapter(Context context) {
        super(context);
        this.context = context;
        Injector.obtain(context.getApplicationContext()).inject(this);
        mDrawableBuilder = TextDrawable.builder()
                .round();
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
        if (!TextUtils.isEmpty(item.getAvatar())) {
            RequestOptions requestOptions = new RequestOptions()
                    .fitCenter()
                    .placeholder(R.drawable.dynamic_profile)
                    .dontAnimate()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .error(R.drawable.dynamic_profile);

            Glide.with(mContext)
                    .load(item.getAvatar())
                    .apply(requestOptions)
                    //.transition(withCrossFade())
                    .into(holder.getImvFindPeoplePic());
        } else if (Settings.isTitlePicEnabled) { // Showing the first character of the name as the profile pic
            if (item.getFirst_name() != null && item.getFirst_name().length() >= 1) {
                String title = String.valueOf(item.getFirst_name().charAt(0)).toUpperCase();
                Pattern p = Pattern.compile("^[a-zA-Z]");
                Matcher m = p.matcher(title);
                boolean b = m.matches();
                if (b) {
                    Drawable drawable = mDrawableBuilder.build(title, mColorGenerator.getColor(item.getPhone_no()));
                    Glide.with(context).clear(holder.getImvFindPeoplePic());
                    holder.getImvFindPeoplePic().setImageDrawable(drawable);
                } else {
                    loadAvatarImage(holder, item);
                }
            }
        } else {
            loadAvatarImage(holder, item);
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
                boolean renewalStatus = preferenceEndPoint.getBooleanPreference(Constants.MAGAZINE_LOCK, false);
                if (!renewalStatus) {
                    if (!"true".equals(item.getIsFollowing())) { // Follow the Yo app user
                        ((BaseActivity) context).showProgressDialog();
                        String accessToken = preferenceEndPoint.getStringPreference("access_token");
                        yoService.followUsersAPI(accessToken, item.getId()).enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                try {
                                    ((BaseActivity) context).dismissProgressDialog();
                                    holder.getBtnFindPeopleFollow().setText("Following");
                                    holder.getBtnFindPeopleFollow().setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_following_tick, 0, 0, 0);
                                    item.setIsFollowing("true");
                                    item.setFollowersCount(item.getFollowersCount() + 1);
                                    isFollowingUser = true;
                                } finally {
                                    if(response != null && response.body() != null) {
                                        response.body().close();
                                    }
                                }
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
                    } else { // Unfollow the Yo app user
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
                                            try {
                                                ((BaseActivity) context).dismissProgressDialog();
                                                if (context instanceof FollowingsActivity) {
                                                    removeItem(item);
                                                    if (getOriginalListCount() == 0) {
                                                        ((FollowingsActivity) context).showEmptyDataScreen();
                                                    }
                                                } else {
                                                    // do nothing
                                                }
                                                holder.getBtnFindPeopleFollow().setText("Follow");
                                                holder.getBtnFindPeopleFollow().setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                                                item.setIsFollowing("false");
                                                item.setFollowersCount(item.getFollowersCount() - 1);
                                                isFollowingUser = false;
                                            } finally {
                                                if(response != null && response.body() != null) {
                                                    response.body().close();
                                                }
                                            }
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
                } else {
                    YODialogs.renewMagazine((Activity) context, null, R.string.renewal_message, preferenceEndPoint);
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

    /**
     * Loads the default avatar image
     * @param holder The FindPeopleViewHolder
     * @param item The FindPeople object
     */
    private void loadAvatarImage(FindPeopleViewHolder holder, FindPeople item) {
        Drawable tempImage = mContext.getResources().getDrawable(R.drawable.dynamic_profile);
        LayerDrawable bgDrawable = (LayerDrawable) tempImage;
        final GradientDrawable shape = (GradientDrawable) bgDrawable.findDrawableByLayerId(R.id.shape_id);
        if (Settings.isTitlePicEnabled) {
            Glide.with(context).clear(holder.getImvFindPeoplePic());
            shape.setColor(mColorGenerator.getColor(item.getPhone_no()));
        }
        if (holder.getImvFindPeoplePic().getTag(Settings.imageTag) == null) {
            holder.getImvFindPeoplePic().setTag(Settings.imageTag, tempImage);
        }
        holder.getImvFindPeoplePic().setImageDrawable((Drawable) holder.getImvFindPeoplePic().getTag(Settings.imageTag));
    }

    private boolean containsValue(String str, String key) {
        return str != null && str.toLowerCase().contains(key);
    }
}
