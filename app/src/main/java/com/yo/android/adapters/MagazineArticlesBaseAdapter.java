package com.yo.android.adapters;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aphidmobile.utils.AphidLog;
import com.aphidmobile.utils.UI;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.orion.android.common.preferences.PreferenceEndPoint;
import com.orion.android.common.util.ToastFactory;
import com.yo.android.R;
import com.yo.android.api.YoApi;
import com.yo.android.flip.MagazineArticleDetailsActivity;
import com.yo.android.model.Articles;
import com.yo.android.ui.BaseActivity;
import com.yo.android.ui.CreateMagazineActivity;
import com.yo.android.ui.FollowMoreTopicsActivity;
import com.yo.android.ui.OtherProfilesLikedArticles;
import com.yo.android.util.AutoReflectWishListActionsListener;
import com.yo.android.util.Constants;
import com.yo.android.util.MagazineOtherPeopleReflectListener;
import com.yo.android.util.Util;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MagazineArticlesBaseAdapter extends BaseAdapter implements AutoReflectWishListActionsListener, MagazineOtherPeopleReflectListener {

    private Context context;
    private LayoutInflater inflater;
    public static AutoReflectWishListActionsListener reflectListener;
    public static MagazineOtherPeopleReflectListener mListener;


    private List<Articles> items;
    PreferenceEndPoint preferenceEndPoint;
    YoApi.YoService yoService;
    ToastFactory mToastFactory;

    public MagazineArticlesBaseAdapter(Context context,
                                       PreferenceEndPoint preferenceEndPoint,
                                       YoApi.YoService yoService, ToastFactory mToastFactory) {
        inflater = LayoutInflater.from(context);
        this.context = context;
        reflectListener = this;
        mListener = this;
        this.preferenceEndPoint = preferenceEndPoint;
        this.yoService = yoService;
        this.mToastFactory = mToastFactory;
        items = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Articles getItem(int position) {
        if (position >= 0 && getCount() > position) {
            return items.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        View layout = convertView;
        if (layout == null) {
            layout = inflater.inflate(R.layout.magazine_flip_layout, null);

            holder = new ViewHolder();

            holder.articleTitle = UI.
                    <TextView>findViewById(layout, R.id.tv_article_title);

            holder.articleShortDesc = UI
                    .<TextView>findViewById(layout, R.id.tv_article_short_desc);

            holder.articlePhoto = UI.findViewById(layout, R.id.photo);

            holder.magazineLike = UI.<CheckBox>findViewById(layout, R.id.cb_magazine_like);

            holder.magazineAdd = UI.<ImageView>findViewById(layout, R.id.imv_magazine_add);

            holder.magazineShare = UI.<ImageView>findViewById(layout, R.id.imv_magazine_share);

            holder.articleFollow = UI.<Button>findViewById(layout, R.id.imv_magazine_follow);

            layout.setTag(holder);
        } else {
            holder = (ViewHolder) layout.getTag();
        }


        final Articles data = getItem(position);
        if (data == null) {
            return layout;
        }
        holder.magazineLike.setTag(position);

        holder.articleTitle
                .setText(AphidLog.format("%s", data.getTitle()));

        if (data.getSummary() != null) {
            holder.articleShortDesc
                    .setText(Html.fromHtml(data.getSummary()));
        }

        holder.magazineLike.setOnCheckedChangeListener(null);
        if (data.getLiked().equals("true")) {
            data.setIsChecked(true);
        } else {
            data.setIsChecked(false);
        }

        holder.magazineLike.setChecked(data.isChecked());

        holder.magazineLike.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int pos = (int) buttonView.getTag();
                data.setIsChecked(isChecked);
                if (isChecked) {
                    String accessToken = preferenceEndPoint.getStringPreference("access_token");
                    yoService.likeArticlesAPI(data.getId(), accessToken).enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                            data.setIsChecked(true);
                            data.setLiked("true");
                            notifyDataSetChanged();
                            if (OtherProfilesLikedArticles.getListener() != null) {
                                OtherProfilesLikedArticles.getListener().updateOtherPeopleStatus(data, Constants.LIKE_EVENT);
                            }
                            mToastFactory.showToast("You have liked the article " + data.getTitle());

                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            mToastFactory.showToast("Error while liking article " + data.getTitle());
                            data.setIsChecked(false);
                            data.setLiked("false");
                            notifyDataSetChanged();
                        }
                    });
                } else {
                    String accessToken = preferenceEndPoint.getStringPreference("access_token");
                    yoService.unlikeArticlesAPI(data.getId(), accessToken).enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            data.setIsChecked(false);
                            data.setLiked("false");
                            if (OtherProfilesLikedArticles.getListener() != null) {
                                OtherProfilesLikedArticles.getListener().updateOtherPeopleStatus(data, Constants.LIKE_EVENT);
                            }
                            notifyDataSetChanged();

                            mToastFactory.showToast("You have un-liked the article " + data.getTitle());

                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            Toast.makeText(context, "Error while un liking article " + data.getTitle(), Toast.LENGTH_LONG).show();
                            data.setIsChecked(true);
                            data.setLiked("true");
                            notifyDataSetChanged();
                        }
                    });
                }
            }
        });
        UI
                .<TextView>findViewById(layout, R.id.tv_category_full_story)
                .setText(AphidLog.format("%s", data.getTitle()));
        UI
                .<TextView>findViewById(layout, R.id.tv_category_full_story)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, MagazineArticleDetailsActivity.class);
                        intent.putExtra("Title", data.getTitle());
                        intent.putExtra("Image", data.getUrl());
                        context.startActivity(intent);
                    }
                });


        ImageView photoView = holder.articlePhoto;

        if (data.getImage_filename() != null) {
//            Picasso.with(context)
//                    .load(data.getImage_filename())
//                    .fit()
//                    .into(photoView);
            Glide.with(context)
                    .load(data.getImage_filename())
                    .centerCrop()
                    //Image size will be reduced 50%
                    .thumbnail(0.5f)
                    .crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(photoView);
        } else {
            photoView.setImageDrawable(null);
        }

        Button followMoreTopics = (Button) layout.findViewById(R.id.btn_magazine_follow_topics);
        followMoreTopics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, FollowMoreTopicsActivity.class);
                intent.putExtra("From", "Magazines");
                context.startActivity(intent);
            }
        });


        ImageView add = holder.magazineAdd;
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, CreateMagazineActivity.class);
                intent.putExtra(Constants.MAGAZINE_ADD_ARTICLE_ID, data.getId());
                if (context instanceof Activity) {
                    ((Activity) context).startActivityForResult(intent, Constants.ADD_ARTICLES_TO_MAGAZINE);
                }
            }
        });

        ImageView share = holder.magazineShare;
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.shareIntent(v, data.getUrl(), "Sharing Article");
            }
        });

        if (data.getIsFollowing().equals("true")) {
            holder.articleFollow.setText("Following");
            holder.articleFollow.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_following_tick, 0, 0, 0);
        } else {
            holder.articleFollow.setText("Follow");
            holder.articleFollow.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }

        final ViewHolder finalHolder = holder;
        holder.articleFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                followArticle(data, finalHolder);
            }
        });


        return layout;
    }

    private void followArticle(final Articles data, final ViewHolder finalHolder) {
        if (!data.getIsFollowing().equals("true")) {
            ((BaseActivity) context).showProgressDialog();
            String accessToken = preferenceEndPoint.getStringPreference("access_token");
            yoService.followArticleAPI(data.getId(), accessToken).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    ((BaseActivity) context).dismissProgressDialog();
                    finalHolder.articleFollow.setText("Following");
                    finalHolder.articleFollow.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_following_tick, 0, 0, 0);
                    data.setIsFollowing("true");
                    if (OtherProfilesLikedArticles.getListener() != null) {
                        OtherProfilesLikedArticles.getListener().updateOtherPeopleStatus(data, Constants.FOLLOW_EVENT);
                    }
                    notifyDataSetChanged();
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    ((BaseActivity) context).dismissProgressDialog();
                    finalHolder.articleFollow.setText("Follow");
                    finalHolder.articleFollow.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                    data.setIsFollowing("false");
                    notifyDataSetChanged();

                }
            });
        } else {
            showUnFollowConfirmationDialog(data, finalHolder);
        }
    }

    private void showUnFollowConfirmationDialog(final Articles data, final ViewHolder finalHolder) {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
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
                ((BaseActivity) context).showProgressDialog();
                String accessToken = preferenceEndPoint.getStringPreference("access_token");
                yoService.unfollowArticleAPI(data.getId(), accessToken).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        ((BaseActivity) context).dismissProgressDialog();
                        finalHolder.articleFollow.setText("Follow");
                        finalHolder.articleFollow.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                        data.setIsFollowing("false");
                        if (OtherProfilesLikedArticles.getListener() != null) {
                            OtherProfilesLikedArticles.getListener().updateOtherPeopleStatus(data, Constants.FOLLOW_EVENT);
                        }
                        notifyDataSetChanged();
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        ((BaseActivity) context).dismissProgressDialog();
                        finalHolder.articleFollow.setText("Following");
                        finalHolder.articleFollow.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_following_tick, 0, 0, 0);
                        data.setIsFollowing("true");
                        notifyDataSetChanged();
                    }
                });
            }
        });

        dialog.show();
    }


    public void addItems(List<Articles> articlesList) {
        items = new ArrayList<>(articlesList);
        notifyDataSetChanged();
    }

    public void clear() {
        items.clear();
        notifyDataSetChanged();
    }

    @Override
    public void updateFollowOrLikesStatus(Articles data, String type) {
        autoReflectStatus(data, type);
    }

    private void autoReflectStatus(Articles data, String type) {
        if (data != null) {

            if (Constants.FOLLOW_EVENT.equals(type)) {
                for (Articles article : items) {
                    if (data.getId() != null && data.getId().equals(article.getId())) {
                        article.setIsFollowing(data.getIsFollowing());
                        article.setIsFollow(data.isFollow());
                        notifyDataSetChanged();
                        break;
                    }
                }
            } else {
                for (Articles article : items) {
                    if (data.getId() != null && data.getId().equals(article.getId())) {
                        article.setLiked(data.getLiked());
                        article.setIsChecked(data.isChecked());
                        notifyDataSetChanged();
                        break;
                    }

                }
            }
        }
    }

    @Override
    public void updateMagazineStatus(Articles data, String follow) {
        autoReflectStatus(data, follow);
    }

    private static class ViewHolder {

        private TextView articleTitle;

        private TextView articleShortDesc;

        private ImageView articlePhoto;

        private CheckBox magazineLike;

        private ImageView magazineAdd;

        private ImageView magazineShare;

        private Button articleFollow;
    }

}
