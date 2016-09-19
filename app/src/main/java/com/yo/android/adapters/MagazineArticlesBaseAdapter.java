package com.yo.android.adapters;

import android.app.Activity;
import android.app.AlertDialog;
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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.aphidmobile.utils.AphidLog;
import com.aphidmobile.utils.UI;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.orion.android.common.preferences.PreferenceEndPoint;
import com.orion.android.common.util.ToastFactory;
import com.yo.android.R;
import com.yo.android.api.YoApi;
import com.yo.android.calllogs.CallLog;
import com.yo.android.flip.MagazineArticleDetailsActivity;
import com.yo.android.flip.MagazineFlipArticlesFragment;
import com.yo.android.model.Articles;
import com.yo.android.model.Topics;
import com.yo.android.ui.BaseActivity;
import com.yo.android.ui.CreateMagazineActivity;
import com.yo.android.ui.FollowMoreTopicsActivity;
import com.yo.android.ui.OtherProfilesLikedArticles;
import com.yo.android.ui.fragments.MagazinesFragment;
import com.yo.android.util.AutoReflectWishListActionsListener;
import com.yo.android.util.Constants;
import com.yo.android.util.MagazineOtherPeopleReflectListener;
import com.yo.android.util.Util;

import java.lang.reflect.Type;
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
    private List<Articles> totalItems;
    private Articles secondArticle;
    private Articles thirdArticle;
    private List<Articles> allArticles;

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
        totalItems = new ArrayList<>();
        allArticles = new ArrayList<>();
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
        int type = getItemViewType(position);
        if (layout == null) {
            //layout = inflater.inflate(R.layout.magazine_flip_layout, null);

            if (type == 0) {
                 // Inflate the layout with multiple articles
                layout = inflater.inflate(R.layout.magazine_landing_layout, null);
            } else if(type == 2) {
                // Inflate the layout with suggestions page
                layout = inflater.inflate(R.layout.landing_suggestions_page, null);
            } else {
                // Inflate the layout with single article
                layout = inflater.inflate(R.layout.magazine_flip_layout, null);
            }

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

            holder.articleTitleTop = UI.
                    <TextView>findViewById(layout, R.id.tv_article_title_top);

            holder.articlePhotoTop = UI.findViewById(layout, R.id.photo_top);

            holder.magazineLikeTop = UI.<CheckBox>findViewById(layout, R.id.cb_magazine_like_top);

            holder.magazineAddTop = UI.<ImageView>findViewById(layout, R.id.imv_magazine_add_top);

            holder.magazineShareTop = UI.<ImageView>findViewById(layout, R.id.imv_magazine_share_top);

            holder.articleFollowTop = UI.<Button>findViewById(layout, R.id.imv_magazine_follow_top);

            holder.articleTitleLeft = UI.
                    <TextView>findViewById(layout, R.id.tv_article_title_left);

            holder.articlePhotoLeft = UI.findViewById(layout, R.id.photo_left);

            holder.magazineLikeLeft = UI.<CheckBox>findViewById(layout, R.id.cb_magazine_like_left);

            holder.magazineAddLeft = UI.<ImageView>findViewById(layout, R.id.imv_magazine_add_left);

            holder.magazineShareLeft = UI.<ImageView>findViewById(layout, R.id.imv_magazine_share_left);

            holder.articleFollowLeft = UI.<Button>findViewById(layout, R.id.imv_magazine_follow_left);

            holder.articleTitleRight = UI.
                    <TextView>findViewById(layout, R.id.tv_article_title_right);

            holder.articlePhotoRight = UI.findViewById(layout, R.id.photo_right);

            holder.magazineLikeRight = UI.<CheckBox>findViewById(layout, R.id.cb_magazine_like_right);

            holder.magazineAddRight = UI.<ImageView>findViewById(layout, R.id.imv_magazine_add_right);

            holder.magazineShareRight = UI.<ImageView>findViewById(layout, R.id.imv_magazine_share_right);

            holder.articleFollowRight = UI.<Button>findViewById(layout, R.id.imv_magazine_follow_right);

            holder.lvSuggestions = UI.<ListView>findViewById(layout, R.id.lv_suggestions);

            holder.tvFollowMoreTopics = UI.<TextView>findViewById(layout, R.id.tv_follow_more_topics);

            layout.setTag(holder);
        } else {
            holder = (ViewHolder) layout.getTag();
        }

        final Articles data = getItem(position);
        if (data == null) {
            return layout;
        }
        if(holder.magazineLike != null) {
            holder.magazineLike.setTag(position);
        }

        if(holder.articleTitle != null) {
            holder.articleTitle
                    .setText(AphidLog.format("%s", data.getTitle()));
        }

        if(holder.articleShortDesc != null) {
            if (data.getSummary() != null && holder.articleShortDesc != null) {
                holder.articleShortDesc
                        .setText(Html.fromHtml(data.getSummary()));
            }
        }

        if(holder.magazineLike != null) {
            holder.magazineLike.setOnCheckedChangeListener(null);
            if ("true".equals(data.getLiked())) {
                data.setIsChecked(true);
            } else {
                data.setIsChecked(false);
            }

            holder.magazineLike.setChecked(data.isChecked());

            holder.magazineLike.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
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
                                Type type = new TypeToken<List<Articles>>() {
                                }.getType();
                                String cachedMagazines = preferenceEndPoint.getStringPreference("cached_magazines", null);
                                List<Articles> cachedMagazinesList = new Gson().fromJson(cachedMagazines, type);
                                for(int i=0; i<cachedMagazinesList.size(); i++) {
                                    if(data.getId().equals(cachedMagazinesList.get(i).getId())) {
                                        cachedMagazinesList.get(i).setLiked("true");
                                    }
                                }
                                preferenceEndPoint.saveStringPreference("cached_magazines", new Gson().toJson(cachedMagazinesList));

                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                mToastFactory.showToast("Error while liking article " + data.getTitle());
                                data.setIsChecked(false);
                                data.setLiked("false");
                                notifyDataSetChanged();
                                Type type = new TypeToken<List<Articles>>() {
                                }.getType();
                                String cachedMagazines = preferenceEndPoint.getStringPreference("cached_magazines", null);
                                List<Articles> cachedMagazinesList = new Gson().fromJson(cachedMagazines, type);
                                for(int i=0; i<cachedMagazinesList.size(); i++) {
                                    if(data.getId().equals(cachedMagazinesList.get(i).getId())) {
                                        cachedMagazinesList.get(i).setLiked("false");
                                    }
                                }
                                preferenceEndPoint.saveStringPreference("cached_magazines", new Gson().toJson(cachedMagazinesList));
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
                                Type type = new TypeToken<List<Articles>>() {
                                }.getType();
                                String cachedMagazines = preferenceEndPoint.getStringPreference("cached_magazines", null);
                                List<Articles> cachedMagazinesList = new Gson().fromJson(cachedMagazines, type);
                                for(int i=0; i<cachedMagazinesList.size(); i++) {
                                    if(data.getId().equals(cachedMagazinesList.get(i).getId())) {
                                        cachedMagazinesList.get(i).setLiked("false");
                                    }
                                }
                                preferenceEndPoint.saveStringPreference("cached_magazines", new Gson().toJson(cachedMagazinesList));

                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                Toast.makeText(context, "Error while un liking article " + data.getTitle(), Toast.LENGTH_LONG).show();
                                data.setIsChecked(true);
                                data.setLiked("true");
                                notifyDataSetChanged();
                                Type type = new TypeToken<List<Articles>>() {
                                }.getType();
                                String cachedMagazines = preferenceEndPoint.getStringPreference("cached_magazines", null);
                                List<Articles> cachedMagazinesList = new Gson().fromJson(cachedMagazines, type);
                                for(int i=0; i<cachedMagazinesList.size(); i++) {
                                    if(data.getId().equals(cachedMagazinesList.get(i).getId())) {
                                        cachedMagazinesList.get(i).setLiked("true");
                                    }
                                }
                                preferenceEndPoint.saveStringPreference("cached_magazines", new Gson().toJson(cachedMagazinesList));
                            }
                        });
                    }
                }
            });
        }
        if(UI
                .<TextView>findViewById(layout, R.id.tv_category_full_story)
                != null) {
            UI
                    .<TextView>findViewById(layout, R.id.tv_category_full_story)
                    .setText(AphidLog.format("%s", data.getTitle()));
        }

        if(UI
                .<TextView>findViewById(layout, R.id.tv_category_full_story) != null) {
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
        }


        if(holder.articlePhoto != null) {
            ImageView photoView = holder.articlePhoto;

            if (data.getImage_filename() != null) {
                Glide.with(context)
                        .load(data.getImage_filename())
                        .placeholder(R.drawable.img_placeholder)
                        .centerCrop()
                                //Image size will be reduced 50%
                        .thumbnail(0.5f)
                        .crossFade()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .dontAnimate()
                        .into(photoView);
            } else {
                photoView.setImageResource(R.drawable.img_placeholder);
            }

            photoView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, MagazineArticleDetailsActivity.class);
                    intent.putExtra("Title", data.getTitle());
                    intent.putExtra("Image", data.getUrl());
                    context.startActivity(intent);
                }
            });
        }

        Button followMoreTopics = (Button) layout.findViewById(R.id.btn_magazine_follow_topics);
        if(followMoreTopics != null) {
            followMoreTopics.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, FollowMoreTopicsActivity.class);
                    intent.putExtra("From", "Magazines");
                    context.startActivity(intent);
                }
            });
        }

        if(holder.magazineAdd != null) {
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
        }

        if(holder.magazineShare != null) {
            ImageView share = holder.magazineShare;
            share.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (data.getImage_filename() != null) {
                        new Util.ImageLoaderTask(v, data).execute(data.getImage_filename());
                    } else {
                        Util.shareNewIntent(v, data.getGenerated_url(), "Article: " + data.getTitle(), data.getSummary(), null);
                    }
                }
            });
        }

        if(holder.articleFollow != null) {
            if ("true".equals(data.getIsFollowing())) {
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
                    followArticle(data, finalHolder, finalHolder.articleFollow);
                }
            });
        }

        LinearLayout llArticleInfo = (LinearLayout)layout.findViewById(R.id.ll_article_info);
        if(llArticleInfo != null) {
            llArticleInfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, MagazineArticleDetailsActivity.class);
                    intent.putExtra("Title", data.getTitle());
                    intent.putExtra("Image", data.getUrl());
                    context.startActivity(intent);
                }
            });
        }

        if(allArticles.size()>=1) {
            Articles firstData = getItem(0);
            populateTopArticle(layout, holder, firstData, position);
        }

        if(allArticles.size()>=2) {
            Articles secondData = secondArticle;
            populateLeftArticle(holder, secondData, position);
        }
        else {
            populateEmptyLeftArticle(holder);
        }

        if(allArticles.size()>=3) {
            Articles thirdData = thirdArticle;
            populateRightArticle(holder, thirdData, position);
        }
        else {
            populateEmptyRightArticle(holder);
        }

        if(allArticles.size()>=4 && MagazinesFragment.unSelectedTopics.size()>0) {
            if(holder.lvSuggestions != null) {
                SuggestionsAdapter suggestionsAdapter = new SuggestionsAdapter(context);
                holder.lvSuggestions.setAdapter(suggestionsAdapter);
                int n = 5;
                if(MagazinesFragment.unSelectedTopics.size()>=n) {
                    List<Topics> subList = new ArrayList<>(MagazinesFragment.unSelectedTopics.subList(0, n));
                    suggestionsAdapter.addItems(subList);
                } else {
                    int count = MagazinesFragment.unSelectedTopics.size();
                    if(count>0) {
                        List<Topics> subList = new ArrayList<>(MagazinesFragment.unSelectedTopics.subList(0, count));
                        suggestionsAdapter.addItems(subList);
                    }
                }
            }
        }

        if(holder.tvFollowMoreTopics != null) {
            holder.tvFollowMoreTopics.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, FollowMoreTopicsActivity.class);
                    intent.putExtra("From", "Magazines");
                    context.startActivity(intent);
                }
            });
        }
        return layout;
    }

    private void followArticle(final Articles data, final ViewHolder finalHolder, final Button follow) {
        if (!"true".equals(data.getIsFollowing())) {
            ((BaseActivity) context).showProgressDialog();
            String accessToken = preferenceEndPoint.getStringPreference("access_token");
            yoService.followArticleAPI(data.getId(), accessToken).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    ((BaseActivity) context).dismissProgressDialog();
                    follow.setText("Following");
                    follow.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_following_tick, 0, 0, 0);
                    data.setIsFollowing("true");
                    if (OtherProfilesLikedArticles.getListener() != null) {
                        OtherProfilesLikedArticles.getListener().updateOtherPeopleStatus(data, Constants.FOLLOW_EVENT);
                    }
                    notifyDataSetChanged();
                    Type type = new TypeToken<List<Articles>>() {
                    }.getType();
                    String cachedMagazines = preferenceEndPoint.getStringPreference("cached_magazines", null);
                    List<Articles> cachedMagazinesList = new Gson().fromJson(cachedMagazines, type);
                    for(int i=0; i<cachedMagazinesList.size(); i++) {
                        if(data.getId().equals(cachedMagazinesList.get(i).getId())) {
                            cachedMagazinesList.get(i).setIsFollowing("true");
                        }
                    }
                    preferenceEndPoint.saveStringPreference("cached_magazines", new Gson().toJson(cachedMagazinesList));
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    ((BaseActivity) context).dismissProgressDialog();
                    follow.setText("Follow");
                    follow.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                    data.setIsFollowing("false");
                    notifyDataSetChanged();
                    Type type = new TypeToken<List<Articles>>() {
                    }.getType();
                    String cachedMagazines = preferenceEndPoint.getStringPreference("cached_magazines", null);
                    List<Articles> cachedMagazinesList = new Gson().fromJson(cachedMagazines, type);
                    for(int i=0; i<cachedMagazinesList.size(); i++) {
                        if(data.getId().equals(cachedMagazinesList.get(i).getId())) {
                            cachedMagazinesList.get(i).setIsFollowing("false");
                        }
                    }
                    preferenceEndPoint.saveStringPreference("cached_magazines", new Gson().toJson(cachedMagazinesList));

                }
            });
        } else {
            showUnFollowConfirmationDialog(data, finalHolder, follow);
        }
    }

    private void showUnFollowConfirmationDialog(final Articles data, final ViewHolder finalHolder, final Button follow) {


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
                    yoService.unfollowArticleAPI(data.getId(), accessToken).enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            ((BaseActivity) context).dismissProgressDialog();
                            follow.setText("Follow");
                            follow.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                            data.setIsFollowing("false");
                            if (OtherProfilesLikedArticles.getListener() != null) {
                                OtherProfilesLikedArticles.getListener().updateOtherPeopleStatus(data, Constants.FOLLOW_EVENT);
                            }
                            notifyDataSetChanged();
                            Type type = new TypeToken<List<Articles>>() {
                            }.getType();
                            String cachedMagazines = preferenceEndPoint.getStringPreference("cached_magazines", null);
                            List<Articles> cachedMagazinesList = new Gson().fromJson(cachedMagazines, type);
                            for(int i=0; i<cachedMagazinesList.size(); i++) {
                                if(data.getId().equals(cachedMagazinesList.get(i).getId())) {
                                    cachedMagazinesList.get(i).setIsFollowing("false");
                                }
                            }
                            preferenceEndPoint.saveStringPreference("cached_magazines", new Gson().toJson(cachedMagazinesList));
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            ((BaseActivity) context).dismissProgressDialog();
                            follow.setText("Following");
                            follow.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_following_tick, 0, 0, 0);
                            data.setIsFollowing("true");
                            notifyDataSetChanged();
                            Type type = new TypeToken<List<Articles>>() {
                            }.getType();
                            String cachedMagazines = preferenceEndPoint.getStringPreference("cached_magazines", null);
                            List<Articles> cachedMagazinesList = new Gson().fromJson(cachedMagazines, type);
                            for(int i=0; i<cachedMagazinesList.size(); i++) {
                                if(data.getId().equals(cachedMagazinesList.get(i).getId())) {
                                    cachedMagazinesList.get(i).setIsFollowing("true");
                                }
                            }
                            preferenceEndPoint.saveStringPreference("cached_magazines", new Gson().toJson(cachedMagazinesList));
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


    public void addItems(List<Articles> articlesList) {
        allArticles = new ArrayList<>(articlesList);
        totalItems = new ArrayList<>(articlesList);
        if(totalItems.size()>1) {
            secondArticle = totalItems.get(1);
        }
        if(totalItems.size()>2) {
            thirdArticle = totalItems.get(2);
        }
        if(totalItems.size()>1) {
            totalItems.remove(1);
        }
        if(totalItems.size()>1) {
            totalItems.remove(1);
        }
        items = new ArrayList<>(totalItems);
        if (!((BaseActivity)context).hasDestroyed()) {
            notifyDataSetChanged();
        }
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
                for (Articles article : allArticles) {
                    if (data.getId() != null && data.getId().equals(article.getId())) {
                        article.setIsFollowing(data.getIsFollowing());
                        article.setIsFollow(data.isFollow());
                        if (!((BaseActivity)context).hasDestroyed()) {
                            notifyDataSetChanged();
                        }
                        break;
                    }
                }
            } else {
                for (Articles article : allArticles) {
                    if (data.getId() != null && data.getId().equals(article.getId())) {
                        article.setLiked(data.getLiked());
                        article.setIsChecked(data.isChecked());
                        if (!((BaseActivity)context).hasDestroyed()) {
                            notifyDataSetChanged();
                        }
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

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    @Override
    public int getItemViewType(int position) {
        //return (position == 0) ? 0 : 1;
        if(position == 0) {
            return  0;
        } else if(position == MagazineFlipArticlesFragment.suggestionsPosition) {
            return 2;
        } else {
            return 1;
        }
    }

    private void populateTopArticle(View layout, ViewHolder holder, final Articles data, int position) {
        if(holder.magazineLikeTop != null) {
            holder.magazineLikeTop.setTag(position);
        }

        if(holder.articleTitleTop != null) {
            holder.articleTitleTop
                    .setText(AphidLog.format("%s", data.getTitle()));
        }

        if(holder.magazineLikeTop != null) {
            holder.magazineLikeTop.setOnCheckedChangeListener(null);
            if ("true".equals(data.getLiked())) {
                data.setIsChecked(true);
            } else {
                data.setIsChecked(false);
            }

            holder.magazineLikeTop.setChecked(data.isChecked());

            holder.magazineLikeTop.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
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
                                Type type = new TypeToken<List<Articles>>() {
                                }.getType();
                                String cachedMagazines = preferenceEndPoint.getStringPreference("cached_magazines", null);
                                List<Articles> cachedMagazinesList = new Gson().fromJson(cachedMagazines, type);
                                for (int i = 0; i < cachedMagazinesList.size(); i++) {
                                    if (data.getId().equals(cachedMagazinesList.get(i).getId())) {
                                        cachedMagazinesList.get(i).setLiked("true");
                                    }
                                }
                                preferenceEndPoint.saveStringPreference("cached_magazines", new Gson().toJson(cachedMagazinesList));

                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                mToastFactory.showToast("Error while liking article " + data.getTitle());
                                data.setIsChecked(false);
                                data.setLiked("false");
                                notifyDataSetChanged();
                                Type type = new TypeToken<List<Articles>>() {
                                }.getType();
                                String cachedMagazines = preferenceEndPoint.getStringPreference("cached_magazines", null);
                                List<Articles> cachedMagazinesList = new Gson().fromJson(cachedMagazines, type);
                                for (int i = 0; i < cachedMagazinesList.size(); i++) {
                                    if (data.getId().equals(cachedMagazinesList.get(i).getId())) {
                                        cachedMagazinesList.get(i).setLiked("false");
                                    }
                                }
                                preferenceEndPoint.saveStringPreference("cached_magazines", new Gson().toJson(cachedMagazinesList));
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
                                Type type = new TypeToken<List<Articles>>() {
                                }.getType();
                                String cachedMagazines = preferenceEndPoint.getStringPreference("cached_magazines", null);
                                List<Articles> cachedMagazinesList = new Gson().fromJson(cachedMagazines, type);
                                for (int i = 0; i < cachedMagazinesList.size(); i++) {
                                    if (data.getId().equals(cachedMagazinesList.get(i).getId())) {
                                        cachedMagazinesList.get(i).setLiked("false");
                                    }
                                }
                                preferenceEndPoint.saveStringPreference("cached_magazines", new Gson().toJson(cachedMagazinesList));

                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                Toast.makeText(context, "Error while un liking article " + data.getTitle(), Toast.LENGTH_LONG).show();
                                data.setIsChecked(true);
                                data.setLiked("true");
                                notifyDataSetChanged();
                                Type type = new TypeToken<List<Articles>>() {
                                }.getType();
                                String cachedMagazines = preferenceEndPoint.getStringPreference("cached_magazines", null);
                                List<Articles> cachedMagazinesList = new Gson().fromJson(cachedMagazines, type);
                                for (int i = 0; i < cachedMagazinesList.size(); i++) {
                                    if (data.getId().equals(cachedMagazinesList.get(i).getId())) {
                                        cachedMagazinesList.get(i).setLiked("true");
                                    }
                                }
                                preferenceEndPoint.saveStringPreference("cached_magazines", new Gson().toJson(cachedMagazinesList));
                            }
                        });
                    }
                }
            });
        }

        if(holder.articlePhotoTop != null) {
            ImageView photoView = holder.articlePhotoTop;

            if (data.getImage_filename() != null) {
                Glide.with(context)
                        .load(data.getImage_filename())
                        .placeholder(R.drawable.img_placeholder)
                        .centerCrop()
                                //Image size will be reduced 50%
                        .thumbnail(0.5f)
                        .crossFade()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .dontAnimate()
                        .into(photoView);
            } else {
                photoView.setImageResource(R.drawable.img_placeholder);
            }

            photoView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, MagazineArticleDetailsActivity.class);
                    intent.putExtra("Title", data.getTitle());
                    intent.putExtra("Image", data.getUrl());
                    context.startActivity(intent);
                }
            });
        }

        if(holder.magazineAddTop != null) {
            ImageView add = holder.magazineAddTop;
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
        }

        if(holder.magazineShareTop != null) {
            ImageView share = holder.magazineShareTop;
            share.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (data.getImage_filename() != null) {
                        new Util.ImageLoaderTask(v, data).execute(data.getImage_filename());
                    } else {
                        Util.shareNewIntent(v, data.getGenerated_url(), "Article: " + data.getTitle(), data.getSummary(), null);
                    }
                }
            });
        }

        if(holder.articleFollowTop != null) {
            if ("true".equals(data.getIsFollowing())) {
                holder.articleFollowTop.setText("Following");
                holder.articleFollowTop.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_following_tick, 0, 0, 0);
            } else {
                holder.articleFollowTop.setText("Follow");
                holder.articleFollowTop.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            }

            final ViewHolder finalHolder = holder;
            holder.articleFollowTop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    followArticle(data, finalHolder, finalHolder.articleFollowTop);
                }
            });
        }

        Button followMoreTopics = (Button) layout.findViewById(R.id.btn_magazine_follow_topics_top);
        if(followMoreTopics != null) {
            followMoreTopics.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, FollowMoreTopicsActivity.class);
                    intent.putExtra("From", "Magazines");
                    context.startActivity(intent);
                }
            });
        }

    }

    private void populateLeftArticle(ViewHolder holder, final Articles data, int position) {
        if(holder.magazineLikeLeft != null) {
            holder.magazineLikeLeft.setVisibility(View.VISIBLE);
            holder.magazineLikeLeft.setTag(position);
        }

        if(holder.articleTitleLeft != null) {
            holder.articleTitleLeft.setVisibility(View.VISIBLE);
            holder.articleTitleLeft
                    .setText(AphidLog.format("%s", data.getTitle()));
            holder.articleTitleLeft.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, MagazineArticleDetailsActivity.class);
                    intent.putExtra("Title", data.getTitle());
                    intent.putExtra("Image", data.getUrl());
                    context.startActivity(intent);
                }
            });
        }

        if(holder.magazineLikeLeft != null) {
            holder.magazineLikeLeft.setOnCheckedChangeListener(null);
            if ("true".equals(data.getLiked())) {
                data.setIsChecked(true);
            } else {
                data.setIsChecked(false);
            }

            holder.magazineLikeLeft.setChecked(data.isChecked());

            holder.magazineLikeLeft.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
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
                                Type type = new TypeToken<List<Articles>>() {
                                }.getType();
                                String cachedMagazines = preferenceEndPoint.getStringPreference("cached_magazines", null);
                                List<Articles> cachedMagazinesList = new Gson().fromJson(cachedMagazines, type);
                                for(int i=0; i<cachedMagazinesList.size(); i++) {
                                    if(data.getId().equals(cachedMagazinesList.get(i).getId())) {
                                        cachedMagazinesList.get(i).setLiked("true");
                                    }
                                }
                                preferenceEndPoint.saveStringPreference("cached_magazines", new Gson().toJson(cachedMagazinesList));

                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                mToastFactory.showToast("Error while liking article " + data.getTitle());
                                data.setIsChecked(false);
                                data.setLiked("false");
                                notifyDataSetChanged();
                                Type type = new TypeToken<List<Articles>>() {
                                }.getType();
                                String cachedMagazines = preferenceEndPoint.getStringPreference("cached_magazines", null);
                                List<Articles> cachedMagazinesList = new Gson().fromJson(cachedMagazines, type);
                                for(int i=0; i<cachedMagazinesList.size(); i++) {
                                    if(data.getId().equals(cachedMagazinesList.get(i).getId())) {
                                        cachedMagazinesList.get(i).setLiked("false");
                                    }
                                }
                                preferenceEndPoint.saveStringPreference("cached_magazines", new Gson().toJson(cachedMagazinesList));
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
                                Type type = new TypeToken<List<Articles>>() {
                                }.getType();
                                String cachedMagazines = preferenceEndPoint.getStringPreference("cached_magazines", null);
                                List<Articles> cachedMagazinesList = new Gson().fromJson(cachedMagazines, type);
                                for(int i=0; i<cachedMagazinesList.size(); i++) {
                                    if(data.getId().equals(cachedMagazinesList.get(i).getId())) {
                                        cachedMagazinesList.get(i).setLiked("false");
                                    }
                                }
                                preferenceEndPoint.saveStringPreference("cached_magazines", new Gson().toJson(cachedMagazinesList));

                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                Toast.makeText(context, "Error while un liking article " + data.getTitle(), Toast.LENGTH_LONG).show();
                                data.setIsChecked(true);
                                data.setLiked("true");
                                notifyDataSetChanged();
                                Type type = new TypeToken<List<Articles>>() {
                                }.getType();
                                String cachedMagazines = preferenceEndPoint.getStringPreference("cached_magazines", null);
                                List<Articles> cachedMagazinesList = new Gson().fromJson(cachedMagazines, type);
                                for(int i=0; i<cachedMagazinesList.size(); i++) {
                                    if(data.getId().equals(cachedMagazinesList.get(i).getId())) {
                                        cachedMagazinesList.get(i).setLiked("true");
                                    }
                                }
                                preferenceEndPoint.saveStringPreference("cached_magazines", new Gson().toJson(cachedMagazinesList));
                            }
                        });
                    }
                }
            });
        }

        if(holder.articlePhotoLeft != null) {
            ImageView photoView = holder.articlePhotoLeft;
            photoView.setVisibility(View.VISIBLE);

            if (data.getImage_filename() != null) {
                Glide.with(context)
                        .load(data.getImage_filename())
                        .placeholder(R.drawable.img_placeholder)
                        .crossFade()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .dontAnimate()
                        .error(R.drawable.img_placeholder)
                        .into(photoView);
            } else {
                photoView.setImageResource(R.drawable.img_placeholder);
            }

            photoView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, MagazineArticleDetailsActivity.class);
                    intent.putExtra("Title", data.getTitle());
                    intent.putExtra("Image", data.getUrl());
                    context.startActivity(intent);
                }
            });
        }

        if(holder.magazineAddLeft != null) {
            ImageView add = holder.magazineAddLeft;
            add.setVisibility(View.VISIBLE);
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
        }

        if(holder.magazineShareLeft != null) {
            ImageView share = holder.magazineShareLeft;
            share.setVisibility(View.VISIBLE);
            share.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (data.getImage_filename() != null) {
                        new Util.ImageLoaderTask(v, data).execute(data.getImage_filename());
                    } else {
                        Util.shareNewIntent(v, data.getGenerated_url(), "Article: " + data.getTitle(), data.getSummary(), null);
                    }
                }
            });
        }

        if(holder.articleFollowLeft != null) {
            holder.articleFollowLeft.setVisibility(View.VISIBLE);
            if ("true".equals(data.getIsFollowing())) {
                holder.articleFollowLeft.setText("Following");
                holder.articleFollowLeft.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_following_tick, 0, 0, 0);
            } else {
                holder.articleFollowLeft.setText("Follow");
                holder.articleFollowLeft.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            }

            final ViewHolder finalHolder = holder;
            holder.articleFollowLeft.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    followArticle(data, finalHolder, finalHolder.articleFollowLeft);
                }
            });
        }

    }

    private void populateRightArticle(ViewHolder holder, final Articles data, int position) {
        if(holder.magazineLikeRight != null) {
            holder.magazineLikeRight.setVisibility(View.VISIBLE);
            holder.magazineLikeRight.setTag(position);
        }

        if(holder.articleTitleRight != null) {
            holder.articleTitleRight.setVisibility(View.VISIBLE);
            holder.articleTitleRight
                    .setText(AphidLog.format("%s", data.getTitle()));
            holder.articleTitleRight.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, MagazineArticleDetailsActivity.class);
                    intent.putExtra("Title", data.getTitle());
                    intent.putExtra("Image", data.getUrl());
                    context.startActivity(intent);
                }
            });
        }

        if(holder.magazineLikeRight != null) {
            holder.magazineLikeRight.setOnCheckedChangeListener(null);
            if ("true".equals(data.getLiked())) {
                data.setIsChecked(true);
            } else {
                data.setIsChecked(false);
            }

            holder.magazineLikeRight.setChecked(data.isChecked());

            holder.magazineLikeRight.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
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
                                Type type = new TypeToken<List<Articles>>() {
                                }.getType();
                                String cachedMagazines = preferenceEndPoint.getStringPreference("cached_magazines", null);
                                List<Articles> cachedMagazinesList = new Gson().fromJson(cachedMagazines, type);
                                for(int i=0; i<cachedMagazinesList.size(); i++) {
                                    if(data.getId().equals(cachedMagazinesList.get(i).getId())) {
                                        cachedMagazinesList.get(i).setLiked("true");
                                    }
                                }
                                preferenceEndPoint.saveStringPreference("cached_magazines", new Gson().toJson(cachedMagazinesList));

                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                mToastFactory.showToast("Error while liking article " + data.getTitle());
                                data.setIsChecked(false);
                                data.setLiked("false");
                                notifyDataSetChanged();
                                Type type = new TypeToken<List<Articles>>() {
                                }.getType();
                                String cachedMagazines = preferenceEndPoint.getStringPreference("cached_magazines", null);
                                List<Articles> cachedMagazinesList = new Gson().fromJson(cachedMagazines, type);
                                for(int i=0; i<cachedMagazinesList.size(); i++) {
                                    if(data.getId().equals(cachedMagazinesList.get(i).getId())) {
                                        cachedMagazinesList.get(i).setLiked("false");
                                    }
                                }
                                preferenceEndPoint.saveStringPreference("cached_magazines", new Gson().toJson(cachedMagazinesList));
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
                                Type type = new TypeToken<List<Articles>>() {
                                }.getType();
                                String cachedMagazines = preferenceEndPoint.getStringPreference("cached_magazines", null);
                                List<Articles> cachedMagazinesList = new Gson().fromJson(cachedMagazines, type);
                                for(int i=0; i<cachedMagazinesList.size(); i++) {
                                    if(data.getId().equals(cachedMagazinesList.get(i).getId())) {
                                        cachedMagazinesList.get(i).setLiked("false");
                                    }
                                }
                                preferenceEndPoint.saveStringPreference("cached_magazines", new Gson().toJson(cachedMagazinesList));

                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                Toast.makeText(context, "Error while un liking article " + data.getTitle(), Toast.LENGTH_LONG).show();
                                data.setIsChecked(true);
                                data.setLiked("true");
                                notifyDataSetChanged();
                                Type type = new TypeToken<List<Articles>>() {
                                }.getType();
                                String cachedMagazines = preferenceEndPoint.getStringPreference("cached_magazines", null);
                                List<Articles> cachedMagazinesList = new Gson().fromJson(cachedMagazines, type);
                                for(int i=0; i<cachedMagazinesList.size(); i++) {
                                    if(data.getId().equals(cachedMagazinesList.get(i).getId())) {
                                        cachedMagazinesList.get(i).setLiked("true");
                                    }
                                }
                                preferenceEndPoint.saveStringPreference("cached_magazines", new Gson().toJson(cachedMagazinesList));
                            }
                        });
                    }
                }
            });
        }

        if(holder.articlePhotoRight != null) {
            ImageView photoView = holder.articlePhotoRight;
            photoView.setVisibility(View.VISIBLE);

            if (data.getImage_filename() != null) {
                Glide.with(context)
                        .load(data.getImage_filename())
                        .placeholder(R.drawable.img_placeholder)
                        .crossFade()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .dontAnimate()
                        .error(R.drawable.img_placeholder)
                        .into(photoView);
            } else {
                photoView.setImageResource(R.drawable.img_placeholder);
            }

            photoView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, MagazineArticleDetailsActivity.class);
                    intent.putExtra("Title", data.getTitle());
                    intent.putExtra("Image", data.getUrl());
                    context.startActivity(intent);
                }
            });
        }

        if(holder.magazineAddRight != null) {
            ImageView add = holder.magazineAddRight;
            add.setVisibility(View.VISIBLE);
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
        }

        if(holder.magazineShareRight != null) {
            ImageView share = holder.magazineShareRight;
            share.setVisibility(View.VISIBLE);
            share.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (data.getImage_filename() != null) {
                        new Util.ImageLoaderTask(v, data).execute(data.getImage_filename());
                    } else {
                        Util.shareNewIntent(v, data.getGenerated_url(), "Article: " + data.getTitle(), data.getSummary(), null);
                    }
                }
            });
        }

        if(holder.articleFollowRight != null) {
            holder.articleFollowRight.setVisibility(View.VISIBLE);
            if ("true".equals(data.getIsFollowing())) {
                holder.articleFollowRight.setText("Following");
                holder.articleFollowRight.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_following_tick, 0, 0, 0);
            } else {
                holder.articleFollowRight.setText("Follow");
                holder.articleFollowRight.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            }

            final ViewHolder finalHolder = holder;
            holder.articleFollowRight.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    followArticle(data, finalHolder, finalHolder.articleFollowRight);
                }
            });
        }

    }

    private void populateEmptyLeftArticle(ViewHolder holder) {
        if(holder.magazineLikeLeft != null) {
            holder.magazineLikeLeft.setVisibility(View.GONE);
        }

        if(holder.articleTitleLeft != null) {
            holder.articleTitleLeft.setVisibility(View.GONE);
        }

        if(holder.articlePhotoLeft != null) {
            ImageView photoView = holder.articlePhotoLeft;
            photoView.setVisibility(View.GONE);
        }

        if(holder.magazineAddLeft != null) {
            ImageView add = holder.magazineAddLeft;
           add.setVisibility(View.GONE);
        }

        if(holder.magazineShareLeft != null) {
            ImageView share = holder.magazineShareLeft;
            share.setVisibility(View.GONE);
        }

        if(holder.articleFollowLeft != null) {
            holder.articleFollowLeft.setVisibility(View.GONE);
        }

    }

    private void populateEmptyRightArticle(ViewHolder holder) {
        if(holder.magazineLikeRight != null) {
            holder.magazineLikeRight.setVisibility(View.GONE);
        }

        if(holder.articleTitleRight != null) {
            holder.articleTitleRight.setVisibility(View.GONE);
        }

        if(holder.articlePhotoRight != null) {
            ImageView photoView = holder.articlePhotoRight;
            photoView.setVisibility(View.GONE);
        }

        if(holder.magazineAddRight != null) {
            ImageView add = holder.magazineAddRight;
            add.setVisibility(View.GONE);
        }

        if(holder.magazineShareRight != null) {
            ImageView share = holder.magazineShareRight;
            share.setVisibility(View.GONE);
        }

        if(holder.articleFollowRight != null) {
            holder.articleFollowRight.setVisibility(View.GONE);
        }

    }

    private static class ViewHolder {

        private TextView articleTitle;

        private TextView articleShortDesc;

        private ImageView articlePhoto;

        private CheckBox magazineLike;

        private ImageView magazineAdd;

        private ImageView magazineShare;

        private Button articleFollow;

        private TextView articleTitleTop;

        private ImageView articlePhotoTop;

        private CheckBox magazineLikeTop;

        private ImageView magazineAddTop;

        private ImageView magazineShareTop;

        private Button articleFollowTop;

        private TextView articleTitleLeft;

        private ImageView articlePhotoLeft;

        private CheckBox magazineLikeLeft;

        private ImageView magazineAddLeft;

        private ImageView magazineShareLeft;

        private Button articleFollowLeft;

        private TextView articleTitleRight;

        private ImageView articlePhotoRight;

        private CheckBox magazineLikeRight;

        private ImageView magazineAddRight;

        private ImageView magazineShareRight;

        private Button articleFollowRight;

        private ListView lvSuggestions;

        private TextView tvFollowMoreTopics;
    }

}
