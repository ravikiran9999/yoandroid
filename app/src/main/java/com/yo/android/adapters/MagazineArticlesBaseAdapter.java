package com.yo.android.adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aphidmobile.utils.AphidLog;
import com.aphidmobile.utils.UI;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.orion.android.common.preferences.PreferenceEndPoint;
import com.orion.android.common.util.ToastFactory;
import com.yo.android.R;
import com.yo.android.api.YoApi;
import com.yo.android.flip.MagazineArticleDetailsActivity;
import com.yo.android.flip.MagazineFlipArticlesFragment;
import com.yo.android.helpers.MagazinePreferenceEndPoint;
import com.yo.android.model.Articles;
import com.yo.android.model.Topics;
import com.yo.android.ui.BaseActivity;
import com.yo.android.ui.CreateMagazineActivity;
import com.yo.android.ui.FollowMoreTopicsActivity;
import com.yo.android.ui.OtherProfilesLikedArticles;
import com.yo.android.ui.TopicsDetailActivity;
import com.yo.android.ui.fragments.MagazinesFragment;
import com.yo.android.util.AutoReflectTopicsFollowActionsListener;
import com.yo.android.util.AutoReflectWishListActionsListener;
import com.yo.android.util.Constants;
import com.yo.android.util.MagazineOtherPeopleReflectListener;
import com.yo.android.util.Util;
import com.yo.android.video.InAppVideoActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * The adapter for the Magazine landing screen articles
 */
public class MagazineArticlesBaseAdapter extends BaseAdapter implements AutoReflectWishListActionsListener, MagazineOtherPeopleReflectListener, AutoReflectTopicsFollowActionsListener {

    private Context context;
    private LayoutInflater inflater;
    public static AutoReflectWishListActionsListener reflectListener;
    public static MagazineOtherPeopleReflectListener mListener;
    public static AutoReflectTopicsFollowActionsListener reflectTopicsFollowActionsListener;


    private List<Articles> items;
    PreferenceEndPoint preferenceEndPoint;
    YoApi.YoService yoService;
    ToastFactory mToastFactory;
    private List<Articles> totalItems;
    public Articles secondArticle;
    public Articles thirdArticle;
    private List<Articles> allArticles;
    private MagazineFlipArticlesFragment magazineFlipArticlesFragment;
    private List<Articles> getAllArticles;

    private static AutoReflectWishListActionsListener reflectListenerTemp;

    public MagazineArticlesBaseAdapter(Context context,
                                       PreferenceEndPoint preferenceEndPoint,
                                       YoApi.YoService yoService, ToastFactory mToastFactory, MagazineFlipArticlesFragment magazineFlipArticlesFragment) {
        inflater = LayoutInflater.from(context);
        this.context = context;
        mListener = this;
        this.preferenceEndPoint = preferenceEndPoint;
        this.yoService = yoService;
        this.mToastFactory = mToastFactory;
        items = new ArrayList<>();
        totalItems = new ArrayList<>();
        allArticles = new ArrayList<>();
        getAllArticles = new ArrayList<>();
        this.magazineFlipArticlesFragment = magazineFlipArticlesFragment;
        reflectTopicsFollowActionsListener = this;
        reflectListenerTemp = this;
    }

    public static void initListener() {
        reflectListener = reflectListenerTemp;
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        View layout = convertView;
        int type = getItemViewType(position);
        if (layout == null) {
            if (type == 0) {
                // Inflate the layout with multiple articles
                layout = inflater.inflate(R.layout.magazine_landing_layout, null);
            } else if (type == 2) {
                // Inflate the layout with suggestions page
                layout = inflater.inflate(R.layout.landing_suggestions_page, null);
            } else {
                // Inflate the layout with single article
                layout = inflater.inflate(R.layout.magazine_flip_layout, null);
            }

            holder = new ViewHolder();

            holder.articleTitle = UI.
                    findViewById(layout, R.id.tv_article_title);

            holder.articleShortDesc = UI
                    .findViewById(layout, R.id.tv_article_short_desc);

            holder.articlePhoto = UI.findViewById(layout, R.id.photo);

            holder.magazineLike = UI.findViewById(layout, R.id.cb_magazine_like);

            holder.magazineAdd = UI.findViewById(layout, R.id.imv_magazine_add);

            holder.magazineShare = UI.findViewById(layout, R.id.imv_magazine_share);

            holder.articleFollow = UI.findViewById(layout, R.id.imv_magazine_follow);

            holder.articleTitleTop = UI.
                    findViewById(layout, R.id.tv_article_title_top);

            holder.articlePhotoTop = UI.findViewById(layout, R.id.photo_top);

            holder.magazineLikeTop = UI.findViewById(layout, R.id.cb_magazine_like_top);

            holder.magazineAddTop = UI.findViewById(layout, R.id.imv_magazine_add_top);

            holder.magazineShareTop = UI.findViewById(layout, R.id.imv_magazine_share_top);

            holder.articleFollowTop = UI.findViewById(layout, R.id.imv_magazine_follow_top);

            holder.articleTitleLeft = UI.
                    findViewById(layout, R.id.tv_article_title_left);

            holder.articlePhotoLeft = UI.findViewById(layout, R.id.photo_left);

            holder.magazineLikeLeft = UI.findViewById(layout, R.id.cb_magazine_like_left);

            holder.magazineAddLeft = UI.findViewById(layout, R.id.imv_magazine_add_left);

            holder.magazineShareLeft = UI.findViewById(layout, R.id.imv_magazine_share_left);

            holder.articleFollowLeft = UI.findViewById(layout, R.id.imv_magazine_follow_left);

            holder.articleTitleRight = UI.
                    findViewById(layout, R.id.tv_article_title_right);

            holder.articlePhotoRight = UI.findViewById(layout, R.id.photo_right);

            holder.magazineLikeRight = UI.findViewById(layout, R.id.cb_magazine_like_right);

            holder.magazineAddRight = UI.findViewById(layout, R.id.imv_magazine_add_right);

            holder.magazineShareRight = UI.findViewById(layout, R.id.imv_magazine_share_right);

            holder.articleFollowRight = UI.findViewById(layout, R.id.imv_magazine_follow_right);

            //holder.lvSuggestions = UI.findViewById(layout, R.id.lv_suggestions);
            holder.lvSuggestions = (ListView) layout.findViewById(R.id.lv_suggestions);

            holder.tvFollowMoreTopics = UI.findViewById(layout, R.id.tv_follow_more_topics);

            holder.tvTopicName = UI.findViewById(layout, R.id.imv_magazine_topic);

            holder.tvTopicNameTop = UI.findViewById(layout, R.id.imv_magazine_topic_top);

            holder.tvTopicNameLeft = UI.findViewById(layout, R.id.imv_magazine_topic_left);

            holder.tvTopicNameRight = UI.findViewById(layout, R.id.imv_magazine_topic_right);

            holder.articleSummaryLeft = UI.findViewById(layout, R.id.tv_article_short_desc_summary);

            holder.articleSummaryRight = UI.findViewById(layout, R.id.tv_article_short_desc_summary_right);

            layout.setTag(holder);
        } else {
            holder = (ViewHolder) layout.getTag();
        }

        final Articles data = getItem(position);
        if (data == null) {
            return layout;
        }

        if (holder.magazineLike != null) {
            holder.magazineLike.setTag(position);
        }

        if (holder.articleTitle != null) {
            holder.articleTitle
                    .setText(AphidLog.format("%s", data.getTitle()));
        }

        if (holder.articleShortDesc != null) {
            if (data.getSummary() != null && holder.articleShortDesc != null) {
                holder.articleShortDesc
                        .setText(Html.fromHtml(data.getSummary()));
            }
        }

        if (holder.magazineLike != null) {
            holder.magazineLike.setOnCheckedChangeListener(null);
            if (Boolean.valueOf(data.getLiked())) {
                data.setIsChecked(true);
            } else {
                data.setIsChecked(false);
            }

            holder.magazineLike.setText("");
            holder.magazineLike.setChecked(Boolean.valueOf(data.getLiked()));

            holder.magazineLike.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Log.d("MagazineBaseAdapter", "Title and liked... " + data.getTitle() + " " + Boolean.valueOf(data.getLiked()));

                    if (isChecked) {
                        ((BaseActivity) context).showProgressDialog();
                        String accessToken = preferenceEndPoint.getStringPreference("access_token");
                        yoService.likeArticlesAPI(data.getId(), accessToken).enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                                ((BaseActivity) context).dismissProgressDialog();
                                data.setIsChecked(true);
                                data.setLiked("true");
                                if (!((BaseActivity) context).hasDestroyed()) {
                                    notifyDataSetChanged();
                                }
                                if (OtherProfilesLikedArticles.getListener() != null) {
                                    OtherProfilesLikedArticles.getListener().updateOtherPeopleStatus(data, Constants.LIKE_EVENT);
                                }
                                mToastFactory.showToast("You have liked the article " + data.getTitle());

                                List<Articles> cachedMagazinesList = getCachedMagazinesList();
                                if (cachedMagazinesList != null) {
                                    List<Articles> tempList = cachedMagazinesList;
                                    for (int i = 0; i < cachedMagazinesList.size(); i++) {
                                        if (data.getId().equals(tempList.get(i).getId())) {
                                            tempList.get(i).setLiked("true");
                                        }
                                    }

                                    cachedMagazinesList = tempList;

                                    saveCachedMagazinesList(cachedMagazinesList);
                                }

                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                ((BaseActivity) context).dismissProgressDialog();
                                mToastFactory.showToast("Error while liking article " + data.getTitle());
                                data.setIsChecked(false);
                                data.setLiked("false");
                                if (!((BaseActivity) context).hasDestroyed()) {
                                    notifyDataSetChanged();
                                }

                                List<Articles> cachedMagazinesList = getCachedMagazinesList();
                                if (cachedMagazinesList != null) {
                                    List<Articles> tempList = cachedMagazinesList;
                                    for (int i = 0; i < cachedMagazinesList.size(); i++) {
                                        if (data.getId().equals(tempList.get(i).getId())) {
                                            tempList.get(i).setLiked("false");
                                        }
                                    }

                                    cachedMagazinesList = tempList;

                                    saveCachedMagazinesList(cachedMagazinesList);
                                }
                            }
                        });
                    } else {
                        ((BaseActivity) context).showProgressDialog();
                        String accessToken = preferenceEndPoint.getStringPreference("access_token");
                        yoService.unlikeArticlesAPI(data.getId(), accessToken).enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                ((BaseActivity) context).dismissProgressDialog();
                                data.setIsChecked(false);
                                data.setLiked("false");
                                if (OtherProfilesLikedArticles.getListener() != null) {
                                    OtherProfilesLikedArticles.getListener().updateOtherPeopleStatus(data, Constants.LIKE_EVENT);
                                }
                                if (!((BaseActivity) context).hasDestroyed()) {
                                    notifyDataSetChanged();
                                }

                                mToastFactory.showToast("You have un-liked the article " + data.getTitle());

                                List<Articles> cachedMagazinesList = getCachedMagazinesList();
                                if (cachedMagazinesList != null) {
                                    List<Articles> tempList = cachedMagazinesList;
                                    for (int i = 0; i < cachedMagazinesList.size(); i++) {
                                        if (data.getId().equals(tempList.get(i).getId())) {
                                            tempList.get(i).setLiked("false");
                                        }
                                    }

                                    cachedMagazinesList = tempList;

                                    saveCachedMagazinesList(cachedMagazinesList);
                                }

                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                ((BaseActivity) context).dismissProgressDialog();
                                Toast.makeText(context, "Error while un liking article " + data.getTitle(), Toast.LENGTH_LONG).show();
                                data.setIsChecked(true);
                                data.setLiked("true");
                                if (!((BaseActivity) context).hasDestroyed()) {
                                    notifyDataSetChanged();
                                }

                                List<Articles> cachedMagazinesList = getCachedMagazinesList();
                                if (cachedMagazinesList != null) {
                                    List<Articles> tempList = cachedMagazinesList;
                                    for (int i = 0; i < cachedMagazinesList.size(); i++) {
                                        if (data.getId().equals(tempList.get(i).getId())) {
                                            tempList.get(i).setLiked("true");
                                        }
                                    }

                                    cachedMagazinesList = tempList;

                                    saveCachedMagazinesList(cachedMagazinesList);
                                }
                            }
                        });
                    }
                }
            });
        }
        if (UI
                .<TextView>findViewById(layout, R.id.tv_category_full_story)
                != null) {
            UI
                    .<TextView>findViewById(layout, R.id.tv_category_full_story)
                    .setText(AphidLog.format("%s", data.getTitle()));
        }

        if (UI
                .<TextView>findViewById(layout, R.id.tv_category_full_story) != null) {
            UI
                    .<TextView>findViewById(layout, R.id.tv_category_full_story)
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(context, MagazineArticleDetailsActivity.class);
                            intent.putExtra("Title", data.getTitle());
                            intent.putExtra("Image", data.getUrl());
                            intent.putExtra("Article", data);
                            intent.putExtra("Position", position);
                            magazineFlipArticlesFragment.startActivityForResult(intent, 500);
                        }
                    });
        }


        if (holder.articlePhoto != null) {
            ImageView photoView = holder.articlePhoto;

            /*RelativeLayout rl = (UI.findViewById(layout, R.id.rl_top));
            final float scale = context.getResources().getDisplayMetrics().density;
            int height;
            if (scale == 4.0) {
                height = 400;
            } else if (scale == 3.5) {
                height = 350;
            } else if (scale == 3.0) {
                height = 300;
            } else if (scale == 2.0) {
                height = 250;
            } else {
                height = 450;
            }
            int pixels = (int) (height * scale + 0.5f);
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, pixels);
            rl.setLayoutParams(layoutParams);*/

            photoView.setImageResource(R.drawable.img_placeholder);

            if (data.getImage_filename() != null) {
                if (!((BaseActivity) context).hasDestroyed()) {
                    Glide.with(context)
                            .load(data.getImage_filename())
                            .listener(new RequestListener<String, GlideDrawable>() {
                                @Override
                                public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                                    if(e!= null) {
                                        mToastFactory.newToast(e.getMessage(), Toast.LENGTH_SHORT);
                                    }
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                    return false;
                                }
                            })
                            .placeholder(R.drawable.img_placeholder)
                            //Image size will be reduced 50%
                            .thumbnail(0.5f)
                            .crossFade()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .dontAnimate()
                            .into(photoView);
                }
            } else {
                photoView.setImageResource(R.drawable.img_placeholder);
            }

            Log.d("ArticlesBaseAdapter", "The photoView.getDrawable() is " + photoView.getDrawable());

            if(photoView.getDrawable() != null) {
                int newHeight = ((BaseActivity) context).getWindowManager().getDefaultDisplay().getHeight() / 3;
                int orgWidth = photoView.getDrawable().getIntrinsicWidth();
                int orgHeight = photoView.getDrawable().getIntrinsicHeight();

                int newWidth = (int) Math.floor((orgWidth * newHeight) / orgHeight);

                Log.d("ArticlesBaseAdapter", "The new width is " + newWidth + "  new height is " + newHeight);

                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                        newWidth, newHeight);
                params.addRule(RelativeLayout.CENTER_HORIZONTAL);
                photoView.setLayoutParams(params);
            }

            photoView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String videoUrl = data.getVideo_url();
                    if (videoUrl != null && !TextUtils.isEmpty(videoUrl)) {
                        InAppVideoActivity.start((Activity) context, videoUrl, data.getTitle());
                    } else {
                        Intent intent = new Intent(context, MagazineArticleDetailsActivity.class);
                        intent.putExtra("Title", data.getTitle());
                        intent.putExtra("Image", data.getUrl());
                        intent.putExtra("Article", data);
                        intent.putExtra("Position", position);
                        magazineFlipArticlesFragment.startActivityForResult(intent, 500);
                    }
                }
            });
        }

        Button followMoreTopics = (Button) layout.findViewById(R.id.btn_magazine_follow_topics);
        if (followMoreTopics != null) {
            followMoreTopics.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, FollowMoreTopicsActivity.class);
                    intent.putExtra("From", "Magazines");
                    context.startActivity(intent);
                }
            });
        }

        if (holder.magazineAdd != null) {
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

        if (holder.magazineShare != null) {
            ImageView share = holder.magazineShare;
            share.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (data.getImage_filename() != null) {
                        new Util.ImageLoaderTask(v, data).execute(data.getImage_filename());
                    } else {
                        String summary = Html.fromHtml(data.getSummary()).toString();
                        Util.shareNewIntent(v, data.getGenerated_url(), "Article: " + data.getTitle(), summary, null);
                    }
                }
            });
        }

        if (holder.articleFollow != null) {
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

        LinearLayout llArticleInfo = (LinearLayout) layout.findViewById(R.id.ll_article_info);
        if (llArticleInfo != null) {
            llArticleInfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, MagazineArticleDetailsActivity.class);
                    intent.putExtra("Title", data.getTitle());
                    intent.putExtra("Image", data.getUrl());
                    intent.putExtra("Article", data);
                    intent.putExtra("Position", position);
                    magazineFlipArticlesFragment.startActivityForResult(intent, 500);
                }
            });
        }

        if (holder.tvTopicName != null) {
            if (!TextUtils.isEmpty(data.getTopicName())) {
                holder.tvTopicName.setVisibility(View.VISIBLE);
                holder.tvTopicName.setText(data.getTopicName());
                holder.tvTopicName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, TopicsDetailActivity.class);
                        intent.putExtra("Topic", data);
                        intent.putExtra("Position", position);
                        magazineFlipArticlesFragment.startActivityForResult(intent, 60);
                    }
                });
            } else {
                holder.tvTopicName.setVisibility(View.GONE);
            }
        }

        if (allArticles.size() >= 1) {
            Articles firstData = getItem(0);
            populateTopArticle(layout, holder, firstData, position);
        }

        if (allArticles.size() >= 2) {
            Articles secondData = secondArticle;
            populateLeftArticle(holder, secondData, position);
        } else {
            populateEmptyLeftArticle(holder);
        }

        if (allArticles.size() >= 3) {
            Articles thirdData = thirdArticle;
            populateRightArticle(holder, thirdData, position);
        } else {
            populateEmptyRightArticle(holder);
        }

        if (allArticles.size() >= 4 && MagazinesFragment.unSelectedTopics.size() > 0) {
            if (holder.lvSuggestions != null) {
                SuggestionsAdapter suggestionsAdapter = new SuggestionsAdapter(context, magazineFlipArticlesFragment);
                holder.lvSuggestions.setAdapter(suggestionsAdapter);
                int n = 5;
                if (MagazinesFragment.unSelectedTopics.size() >= n) {
                    List<Topics> subList = new ArrayList<>(MagazinesFragment.unSelectedTopics.subList(0, n));
                    suggestionsAdapter.addItems(subList);
                } else {
                    int count = MagazinesFragment.unSelectedTopics.size();
                    if (count > 0) {
                        List<Topics> subList = new ArrayList<>(MagazinesFragment.unSelectedTopics.subList(0, count));
                        suggestionsAdapter.addItems(subList);
                    }
                }
            }
        }

        if (holder.tvFollowMoreTopics != null) {
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

    /**
     * Used to follow an article
     *
     * @param data        The Articles object
     * @param finalHolder The view holder object
     * @param follow      The follow button
     */
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
                    if (!((BaseActivity) context).hasDestroyed()) {
                        notifyDataSetChanged();
                    }
                    Type type = new TypeToken<List<Articles>>() {
                    }.getType();
                    String cachedMagazines = preferenceEndPoint.getStringPreference("cached_magazines", null);
                    List<Articles> cachedMagazinesList = new Gson().fromJson(cachedMagazines, type);
                    if (cachedMagazinesList != null) {
                        for (int i = 0; i < cachedMagazinesList.size(); i++) {
                            if (data.getId().equals(cachedMagazinesList.get(i).getId())) {
                                cachedMagazinesList.get(i).setIsFollowing("true");
                            }
                        }

                        preferenceEndPoint.saveStringPreference("cached_magazines", new Gson().toJson(cachedMagazinesList));
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    ((BaseActivity) context).dismissProgressDialog();
                    follow.setText("Follow");
                    follow.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                    data.setIsFollowing("false");
                    if (!((BaseActivity) context).hasDestroyed()) {
                        notifyDataSetChanged();
                    }
                    Type type = new TypeToken<List<Articles>>() {
                    }.getType();
                    String cachedMagazines = preferenceEndPoint.getStringPreference("cached_magazines", null);
                    List<Articles> cachedMagazinesList = new Gson().fromJson(cachedMagazines, type);
                    if (cachedMagazinesList != null) {
                        for (int i = 0; i < cachedMagazinesList.size(); i++) {
                            if (data.getId().equals(cachedMagazinesList.get(i).getId())) {
                                cachedMagazinesList.get(i).setIsFollowing("false");
                            }
                        }

                        preferenceEndPoint.saveStringPreference("cached_magazines", new Gson().toJson(cachedMagazinesList));
                    }

                }
            });
        } else {
            showUnFollowConfirmationDialog(data, finalHolder, follow);
        }
    }

    /**
     * Shows unfollow confirmation dialog
     *
     * @param data        The articles object
     * @param finalHolder The view holder object
     * @param follow      The follow button
     */
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
                                                                                            if (!((BaseActivity) context).hasDestroyed()) {
                                                                                                notifyDataSetChanged();
                                                                                            }
                                                                                            Type type = new TypeToken<List<Articles>>() {
                                                                                            }.getType();
                                                                                            String cachedMagazines = preferenceEndPoint.getStringPreference("cached_magazines", null);
                                                                                            List<Articles> cachedMagazinesList = new Gson().fromJson(cachedMagazines, type);
                                                                                            if (cachedMagazinesList != null) {
                                                                                                for (int i = 0; i < cachedMagazinesList.size(); i++) {
                                                                                                    if (data.getId().equals(cachedMagazinesList.get(i).getId())) {
                                                                                                        cachedMagazinesList.get(i).setIsFollowing("false");
                                                                                                    }
                                                                                                }

                                                                                                preferenceEndPoint.saveStringPreference("cached_magazines", new Gson().toJson(cachedMagazinesList));
                                                                                            }
                                                                                        }

                                                                                        @Override
                                                                                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                                                                                            ((BaseActivity) context).dismissProgressDialog();
                                                                                            follow.setText("Following");
                                                                                            follow.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_following_tick, 0, 0, 0);
                                                                                            data.setIsFollowing("true");
                                                                                            if (!((BaseActivity) context).hasDestroyed()) {
                                                                                                notifyDataSetChanged();
                                                                                            }
                                                                                            Type type = new TypeToken<List<Articles>>() {
                                                                                            }.getType();
                                                                                            String cachedMagazines = preferenceEndPoint.getStringPreference("cached_magazines", null);
                                                                                            List<Articles> cachedMagazinesList = new Gson().fromJson(cachedMagazines, type);
                                                                                            if (cachedMagazinesList != null) {
                                                                                                for (int i = 0; i < cachedMagazinesList.size(); i++) {
                                                                                                    if (data.getId().equals(cachedMagazinesList.get(i).getId())) {
                                                                                                        cachedMagazinesList.get(i).setIsFollowing("true");
                                                                                                    }
                                                                                                }

                                                                                                preferenceEndPoint.saveStringPreference("cached_magazines", new Gson().toJson(cachedMagazinesList));
                                                                                            }
                                                                                        }
                                                                                    }

                    );
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


    /**
     * Adds the items to the list
     *
     * @param articlesList The list of articles
     */
    public void addItems(List<Articles> articlesList) {
        allArticles = new ArrayList<>(articlesList);
        totalItems = new ArrayList<>(articlesList);
        if (!magazineFlipArticlesFragment.isSearch) {
            if (totalItems.size() > 1) {
                secondArticle = totalItems.get(1);
            }
            if (totalItems.size() > 2) {
                thirdArticle = totalItems.get(2);
            }
            if (totalItems.size() > 1) {
                totalItems.remove(1);
            }
            if (totalItems.size() > 1) {
                totalItems.remove(1);
            }
        }
        items = new ArrayList<>(totalItems);
        if (!((BaseActivity) context).hasDestroyed()) {
            notifyDataSetChanged();
        }
    }

    /**
     * Adds all the items to the list
     *
     * @param list The articles list
     */
    public void addItemsAll(List<Articles> list) {
        items.addAll(list);
        if (!((BaseActivity) context).hasDestroyed()) {
            notifyDataSetChanged();
        }
    }

    /**
     * Clears the articles list
     */
    public void clear() {
        items.clear();
        if (!((BaseActivity) context).hasDestroyed()) {
            notifyDataSetChanged();
        }
    }

    @Override
    public void updateFollowOrLikesStatus(Articles data, String type) {
        autoReflectStatus(data, type);
    }

    /**
     * Updates the Follow and Like status of the articles
     *
     * @param data The articles object
     * @param type Whether it is Follow or Like
     */
    private void autoReflectStatus(Articles data, String type) {
        if (data != null) {

            if (Constants.FOLLOW_EVENT.equals(type)) {
                for (Articles article : allArticles) {
                    if (data.getId() != null && data.getId().equals(article.getId())) {
                        article.setIsFollowing(data.getIsFollowing());
                        article.setIsFollow(data.isFollow());
                        if (!((BaseActivity) context).hasDestroyed()) {
                            notifyDataSetChanged();
                        }
                        break;
                    }
                }
            } else {
                allArticles = getAllItems();
                for (Articles article : allArticles) {
                    if (data.getId() != null && data.getId().equals(article.getId())) {
                        article.setLiked(data.getLiked());
                        article.setIsChecked(data.isChecked());
                        if (!((BaseActivity) context).hasDestroyed()) {
                            notifyDataSetChanged();
                        }

                        List<Articles> cachedMagazinesList = getCachedMagazinesList();

                        if (cachedMagazinesList != null) {
                            List<Articles> tempList = cachedMagazinesList;
                            for (int i = 0; i < cachedMagazinesList.size(); i++) {
                                if (data.getId().equals(tempList.get(i).getId())) {
                                    tempList.get(i).setLiked(data.getLiked());
                                }
                            }
                            cachedMagazinesList = tempList;

                            saveCachedMagazinesList(cachedMagazinesList);
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
        if (position == 0 && !magazineFlipArticlesFragment.isSearch) {
            return 0;
        } else if (position == MagazineFlipArticlesFragment.suggestionsPosition) {
            return 2;
        } else if (magazineFlipArticlesFragment.isSearch) {
            return 1;
        } else {
            return 1;
        }
    }

    /**
     * Populates the top article
     *
     * @param layout   The view object
     * @param holder   The view holder object
     * @param data     The articles object
     * @param position The position
     */
    private void populateTopArticle(View layout, ViewHolder holder, final Articles data, final int position) {
        // Log.d("ArticlesBaseAdapter", "In populateTopArticle");
        if (holder.magazineLikeTop != null) {
            holder.magazineLikeTop.setTag(position);
        }

        if (holder.articleTitleTop != null) {
            holder.articleTitleTop
                    .setText(AphidLog.format("%s", data.getTitle()));
        }

        if (holder.magazineLikeTop != null) {
            holder.magazineLikeTop.setOnCheckedChangeListener(null);
            if (Boolean.valueOf(data.getLiked())) {
                data.setIsChecked(true);
            } else {
                data.setIsChecked(false);
            }

            holder.magazineLikeTop.setText("");
            holder.magazineLikeTop.setChecked(Boolean.valueOf(data.getLiked()));

            holder.magazineLikeTop.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    if (isChecked) {
                        ((BaseActivity) context).showProgressDialog();
                        String accessToken = preferenceEndPoint.getStringPreference("access_token");
                        yoService.likeArticlesAPI(data.getId(), accessToken).enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                ((BaseActivity) context).dismissProgressDialog();

                                data.setIsChecked(true);
                                data.setLiked("true");
                                if (!((BaseActivity) context).hasDestroyed()) {
                                    notifyDataSetChanged();
                                }
                                if (OtherProfilesLikedArticles.getListener() != null) {
                                    OtherProfilesLikedArticles.getListener().updateOtherPeopleStatus(data, Constants.LIKE_EVENT);
                                }
                                mToastFactory.showToast("You have liked the article " + data.getTitle());

                                List<Articles> cachedMagazinesList = getCachedMagazinesList();
                                if (cachedMagazinesList != null) {
                                    List<Articles> tempList = cachedMagazinesList;
                                    for (int i = 0; i < cachedMagazinesList.size(); i++) {
                                        if (data.getId().equals(tempList.get(i).getId())) {
                                            tempList.get(i).setLiked("true");
                                        }
                                    }

                                    cachedMagazinesList = tempList;

                                    saveCachedMagazinesList(cachedMagazinesList);
                                }

                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                ((BaseActivity) context).dismissProgressDialog();
                                mToastFactory.showToast("Error while liking article " + data.getTitle());
                                data.setIsChecked(false);
                                data.setLiked("false");
                                if (!((BaseActivity) context).hasDestroyed()) {
                                    notifyDataSetChanged();
                                }

                                List<Articles> cachedMagazinesList = getCachedMagazinesList();
                                if (cachedMagazinesList != null) {
                                    List<Articles> tempList = cachedMagazinesList;
                                    for (int i = 0; i < cachedMagazinesList.size(); i++) {
                                        if (data.getId().equals(tempList.get(i).getId())) {
                                            tempList.get(i).setLiked("false");
                                        }
                                    }

                                    cachedMagazinesList = tempList;

                                    saveCachedMagazinesList(cachedMagazinesList);
                                }
                            }
                        });
                    } else {
                        ((BaseActivity) context).showProgressDialog();
                        String accessToken = preferenceEndPoint.getStringPreference("access_token");
                        yoService.unlikeArticlesAPI(data.getId(), accessToken).enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                ((BaseActivity) context).dismissProgressDialog();
                                data.setIsChecked(false);
                                data.setLiked("false");
                                if (OtherProfilesLikedArticles.getListener() != null) {
                                    OtherProfilesLikedArticles.getListener().updateOtherPeopleStatus(data, Constants.LIKE_EVENT);
                                }
                                if (!((BaseActivity) context).hasDestroyed()) {
                                    notifyDataSetChanged();
                                }

                                mToastFactory.showToast("You have un-liked the article " + data.getTitle());

                                List<Articles> cachedMagazinesList = getCachedMagazinesList();
                                if (cachedMagazinesList != null) {
                                    List<Articles> tempList = cachedMagazinesList;
                                    for (int i = 0; i < cachedMagazinesList.size(); i++) {
                                        if (data.getId().equals(tempList.get(i).getId())) {
                                            tempList.get(i).setLiked("false");
                                        }
                                    }

                                    cachedMagazinesList = tempList;

                                    saveCachedMagazinesList(cachedMagazinesList);
                                }

                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                ((BaseActivity) context).dismissProgressDialog();
                                Toast.makeText(context, "Error while un liking article " + data.getTitle(), Toast.LENGTH_LONG).show();
                                data.setIsChecked(true);
                                data.setLiked("true");
                                if (!((BaseActivity) context).hasDestroyed()) {
                                    notifyDataSetChanged();
                                }

                                List<Articles> cachedMagazinesList = getCachedMagazinesList();
                                if (cachedMagazinesList != null) {
                                    List<Articles> tempList = cachedMagazinesList;
                                    for (int i = 0; i < cachedMagazinesList.size(); i++) {
                                        if (data.getId().equals(tempList.get(i).getId())) {
                                            tempList.get(i).setLiked("true");
                                        }
                                    }

                                    cachedMagazinesList = tempList;

                                    saveCachedMagazinesList(cachedMagazinesList);
                                }
                            }
                        });
                    }
                }
            });
        }

        if (holder.articlePhotoTop != null) {
            ImageView photoView = holder.articlePhotoTop;

            if (data.getImage_filename() != null) {
                if (!((BaseActivity) context).hasDestroyed()) {
                    Glide.with(context)
                            .load(data.getImage_filename())
                            .placeholder(R.drawable.img_placeholder)
                            //Image size will be reduced 50%
                            .thumbnail(0.5f)
                            .crossFade()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .dontAnimate()
                            .into(photoView);
                }
            } else {
                photoView.setImageResource(R.drawable.img_placeholder);
            }

            photoView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, MagazineArticleDetailsActivity.class);
                    intent.putExtra("Title", data.getTitle());
                    intent.putExtra("Image", data.getUrl());
                    intent.putExtra("Article", data);
                    intent.putExtra("Position", position);
                    magazineFlipArticlesFragment.startActivityForResult(intent, 500);
                }
            });
        }

        if (holder.magazineAddTop != null) {
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

        if (holder.magazineShareTop != null) {
            ImageView share = holder.magazineShareTop;
            share.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (data.getImage_filename() != null) {
                        new Util.ImageLoaderTask(v, data).execute(data.getImage_filename());
                    } else {
                        String summary = Html.fromHtml(data.getSummary()).toString();
                        Util.shareNewIntent(v, data.getGenerated_url(), "Article: " + data.getTitle(), summary, null);
                    }
                }
            });
        }

        if (holder.articleFollowTop != null) {
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
        if (followMoreTopics != null) {
            followMoreTopics.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, FollowMoreTopicsActivity.class);
                    intent.putExtra("From", "Magazines");
                    context.startActivity(intent);
                }
            });
        }

        if (holder.tvTopicNameTop != null) {
            if (!TextUtils.isEmpty(data.getTopicName())) {
                holder.tvTopicNameTop.setVisibility(View.VISIBLE);
                holder.tvTopicNameTop.setText(data.getTopicName());
                holder.tvTopicNameTop.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, TopicsDetailActivity.class);
                        intent.putExtra("Topic", data);
                        intent.putExtra("Position", position);
                        magazineFlipArticlesFragment.startActivityForResult(intent, 60);
                    }
                });
            } else {
                holder.tvTopicNameTop.setVisibility(View.GONE);
            }
        }

    }

    /**
     * Populates the left article
     *
     * @param holder   The view holder object
     * @param data     The articles object
     * @param position The position
     */
    private void populateLeftArticle(ViewHolder holder, final Articles data, final int position) {
        //Log.d("ArticlesBaseAdapter", "In populateLeftArticle");
        if (holder.magazineLikeLeft != null) {
            holder.magazineLikeLeft.setVisibility(View.VISIBLE);
            holder.magazineLikeLeft.setTag(position);
        }

        if (holder.articleTitleLeft != null) {
            holder.articleTitleLeft.setVisibility(View.VISIBLE);
            holder.articleTitleLeft
                    .setText(AphidLog.format("%s", data.getTitle()));
            holder.articleTitleLeft.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, MagazineArticleDetailsActivity.class);
                    intent.putExtra("Title", data.getTitle());
                    intent.putExtra("Image", data.getUrl());
                    intent.putExtra("Article", data);
                    intent.putExtra("Position", position);
                    intent.putExtra("ArticlePlacement", "left");
                    magazineFlipArticlesFragment.startActivityForResult(intent, 500);
                }
            });
        }

        if (holder.magazineLikeLeft != null) {
            holder.magazineLikeLeft.setOnCheckedChangeListener(null);
            if (Boolean.valueOf(data.getLiked())) {
                data.setIsChecked(true);
            } else {
                data.setIsChecked(false);
            }

            holder.magazineLikeLeft.setText("");
            holder.magazineLikeLeft.setChecked(Boolean.valueOf(data.getLiked()));

            holder.magazineLikeLeft.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    if (isChecked) {
                        ((BaseActivity) context).showProgressDialog();
                        String accessToken = preferenceEndPoint.getStringPreference("access_token");
                        yoService.likeArticlesAPI(data.getId(), accessToken).enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                likeUnlikeErrorMessage(response);
                                ((BaseActivity) context).dismissProgressDialog();
                                data.setIsChecked(true);
                                data.setLiked("true");
                                if (!((BaseActivity) context).hasDestroyed()) {
                                    notifyDataSetChanged();
                                }
                                if (OtherProfilesLikedArticles.getListener() != null) {
                                    OtherProfilesLikedArticles.getListener().updateOtherPeopleStatus(data, Constants.LIKE_EVENT);
                                }
                                mToastFactory.showToast("You have liked the article " + data.getTitle());

                                List<Articles> cachedMagazinesList = getCachedMagazinesList();
                                if (cachedMagazinesList != null) {
                                    List<Articles> tempList = cachedMagazinesList;
                                    for (int i = 0; i < cachedMagazinesList.size(); i++) {
                                        if (data.getId().equals(tempList.get(i).getId())) {
                                            tempList.get(i).setLiked("true");
                                        }
                                    }

                                    cachedMagazinesList = tempList;

                                    saveCachedMagazinesList(cachedMagazinesList);
                                }

                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                ((BaseActivity) context).dismissProgressDialog();
                                mToastFactory.showToast("Error while liking article " + data.getTitle());
                                data.setIsChecked(false);
                                data.setLiked("false");
                                if (!((BaseActivity) context).hasDestroyed()) {
                                    notifyDataSetChanged();
                                }

                                List<Articles> cachedMagazinesList = getCachedMagazinesList();
                                if (cachedMagazinesList != null) {
                                    List<Articles> tempList = cachedMagazinesList;
                                    for (int i = 0; i < cachedMagazinesList.size(); i++) {
                                        if (data.getId().equals(tempList.get(i).getId())) {
                                            tempList.get(i).setLiked("false");
                                        }
                                    }

                                    cachedMagazinesList = tempList;

                                    saveCachedMagazinesList(cachedMagazinesList);
                                }
                            }
                        });
                    } else {
                        ((BaseActivity) context).showProgressDialog();
                        String accessToken = preferenceEndPoint.getStringPreference("access_token");
                        yoService.unlikeArticlesAPI(data.getId(), accessToken).enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                likeUnlikeErrorMessage(response);
                                ((BaseActivity) context).dismissProgressDialog();
                                data.setIsChecked(false);
                                data.setLiked("false");
                                if (OtherProfilesLikedArticles.getListener() != null) {
                                    OtherProfilesLikedArticles.getListener().updateOtherPeopleStatus(data, Constants.LIKE_EVENT);
                                }
                                if (!((BaseActivity) context).hasDestroyed()) {
                                    notifyDataSetChanged();
                                }

                                mToastFactory.showToast("You have un-liked the article " + data.getTitle());

                                List<Articles> cachedMagazinesList = getCachedMagazinesList();
                                if (cachedMagazinesList != null) {
                                    List<Articles> tempList = cachedMagazinesList;
                                    for (int i = 0; i < cachedMagazinesList.size(); i++) {
                                        if (data.getId().equals(tempList.get(i).getId())) {
                                            tempList.get(i).setLiked("false");
                                        }
                                    }

                                    cachedMagazinesList = tempList;

                                    saveCachedMagazinesList(cachedMagazinesList);
                                }

                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                ((BaseActivity) context).dismissProgressDialog();
                                Toast.makeText(context, "Error while un liking article " + data.getTitle(), Toast.LENGTH_LONG).show();
                                data.setIsChecked(true);
                                data.setLiked("true");
                                if (!((BaseActivity) context).hasDestroyed()) {
                                    notifyDataSetChanged();
                                }

                                List<Articles> cachedMagazinesList = getCachedMagazinesList();
                                if (cachedMagazinesList != null) {
                                    List<Articles> tempList = cachedMagazinesList;
                                    for (int i = 0; i < cachedMagazinesList.size(); i++) {
                                        if (data.getId().equals(tempList.get(i).getId())) {
                                            tempList.get(i).setLiked("true");
                                        }
                                    }

                                    cachedMagazinesList = tempList;

                                    saveCachedMagazinesList(cachedMagazinesList);
                                }
                            }
                        });
                    }
                }
            });
        }

        if (holder.articlePhotoLeft != null) {
            ImageView photoView = holder.articlePhotoLeft;
            photoView.setVisibility(View.VISIBLE);

            if (data.getImage_filename() != null) {
                if (!((BaseActivity) context).hasDestroyed()) {
                    Glide.with(context)
                            .load(data.getImage_filename())
                            .placeholder(R.drawable.img_placeholder)
                            .crossFade()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .dontAnimate()
                            .error(R.drawable.img_placeholder)
                            .into(photoView);
                }
            } else {
                photoView.setImageResource(R.drawable.img_placeholder);
            }

            photoView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, MagazineArticleDetailsActivity.class);
                    intent.putExtra("Title", data.getTitle());
                    intent.putExtra("Image", data.getUrl());
                    intent.putExtra("Article", data);
                    intent.putExtra("Position", position);
                    intent.putExtra("ArticlePlacement", "left");
                    magazineFlipArticlesFragment.startActivityForResult(intent, 500);
                }
            });
        }

        if (holder.magazineAddLeft != null) {
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

        if (holder.magazineShareLeft != null) {
            ImageView share = holder.magazineShareLeft;
            share.setVisibility(View.VISIBLE);
            share.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (data.getImage_filename() != null) {
                        new Util.ImageLoaderTask(v, data).execute(data.getImage_filename());
                    } else {
                        String summary = Html.fromHtml(data.getSummary()).toString();
                        Util.shareNewIntent(v, data.getGenerated_url(), "Article: " + data.getTitle(), summary, null);
                    }
                }
            });
        }

        if (holder.articleFollowLeft != null) {

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

        if (holder.tvTopicNameLeft != null) {
            if (!TextUtils.isEmpty(data.getTopicName())) {
                holder.tvTopicNameLeft.setVisibility(View.VISIBLE);
                holder.tvTopicNameLeft.setText(data.getTopicName());
                holder.tvTopicNameLeft.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, TopicsDetailActivity.class);
                        intent.putExtra("Topic", data);
                        intent.putExtra("Position", position);
                        intent.putExtra("ArticlePlacement", "left");
                        magazineFlipArticlesFragment.startActivityForResult(intent, 60);
                    }
                });
            } else {
                holder.tvTopicNameLeft.setVisibility(View.GONE);
            }
        }

        if (holder.articleSummaryLeft != null) {

            float density = context.getResources().getDisplayMetrics().density;

            if (density == 4.0) {
                holder.articleSummaryLeft.setVisibility(View.VISIBLE);
            } else if (density == 3.5) {
                holder.articleSummaryLeft.setVisibility(View.VISIBLE);
            } else if (density == 3.0) {
                holder.articleSummaryLeft.setVisibility(View.VISIBLE);
            } else if (density == 2.0) {
                holder.articleSummaryLeft.setVisibility(View.VISIBLE);
            } else {
                holder.articleSummaryLeft.setVisibility(View.GONE);
            }

            if (data.getSummary() != null && holder.articleSummaryLeft != null) {
                holder.articleSummaryLeft
                        .setText(Html.fromHtml(data.getSummary()));
            }
        }


    }

    /**
     * Populates the right article
     *
     * @param holder   The view holder object
     * @param data     The articles object
     * @param position The position
     */
    private void populateRightArticle(ViewHolder holder, final Articles data, final int position) {
        //  Log.d("ArticlesBaseAdapter", "In populateRightArticle");
        if (holder.magazineLikeRight != null) {
            holder.magazineLikeRight.setVisibility(View.VISIBLE);
            holder.magazineLikeRight.setTag(position);
        }

        if (holder.articleTitleRight != null) {
            holder.articleTitleRight.setVisibility(View.VISIBLE);
            holder.articleTitleRight
                    .setText(AphidLog.format("%s", data.getTitle()));
            holder.articleTitleRight.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, MagazineArticleDetailsActivity.class);
                    intent.putExtra("Title", data.getTitle());
                    intent.putExtra("Image", data.getUrl());
                    intent.putExtra("Article", data);
                    intent.putExtra("Position", position);
                    intent.putExtra("ArticlePlacement", "right");
                    magazineFlipArticlesFragment.startActivityForResult(intent, 500);
                }
            });
        }

        if (holder.magazineLikeRight != null) {
            holder.magazineLikeRight.setOnCheckedChangeListener(null);
            if (Boolean.valueOf(data.getLiked())) {
                data.setIsChecked(true);
            } else {
                data.setIsChecked(false);
            }

            holder.magazineLikeRight.setText("");
            holder.magazineLikeRight.setChecked(Boolean.valueOf(data.getLiked()));

            holder.magazineLikeRight.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    if (isChecked) {
                        ((BaseActivity) context).showProgressDialog();
                        String accessToken = preferenceEndPoint.getStringPreference("access_token");
                        yoService.likeArticlesAPI(data.getId(), accessToken).enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                ((BaseActivity) context).dismissProgressDialog();
                                data.setIsChecked(true);
                                data.setLiked("true");
                                if (!((BaseActivity) context).hasDestroyed()) {
                                    notifyDataSetChanged();
                                }
                                if (OtherProfilesLikedArticles.getListener() != null) {
                                    OtherProfilesLikedArticles.getListener().updateOtherPeopleStatus(data, Constants.LIKE_EVENT);
                                }
                                mToastFactory.showToast("You have liked the article " + data.getTitle());

                                List<Articles> cachedMagazinesList = getCachedMagazinesList();
                                if (cachedMagazinesList != null) {
                                    List<Articles> tempList = cachedMagazinesList;
                                    for (int i = 0; i < cachedMagazinesList.size(); i++) {
                                        if (data.getId().equals(tempList.get(i).getId())) {
                                            tempList.get(i).setLiked("true");
                                        }
                                    }

                                    cachedMagazinesList = tempList;

                                    saveCachedMagazinesList(cachedMagazinesList);
                                }

                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                ((BaseActivity) context).dismissProgressDialog();
                                mToastFactory.showToast("Error while liking article " + data.getTitle());
                                data.setIsChecked(false);
                                data.setLiked("false");
                                if (!((BaseActivity) context).hasDestroyed()) {
                                    notifyDataSetChanged();
                                }

                                List<Articles> cachedMagazinesList = getCachedMagazinesList();
                                if (cachedMagazinesList != null) {
                                    List<Articles> tempList = cachedMagazinesList;
                                    for (int i = 0; i < cachedMagazinesList.size(); i++) {
                                        if (data.getId().equals(tempList.get(i).getId())) {
                                            tempList.get(i).setLiked("false");
                                        }
                                    }

                                    cachedMagazinesList = tempList;

                                    saveCachedMagazinesList(cachedMagazinesList);
                                }
                            }
                        });
                    } else {
                        ((BaseActivity) context).showProgressDialog();
                        String accessToken = preferenceEndPoint.getStringPreference("access_token");
                        yoService.unlikeArticlesAPI(data.getId(), accessToken).enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                ((BaseActivity) context).dismissProgressDialog();
                                data.setIsChecked(false);
                                data.setLiked("false");
                                if (OtherProfilesLikedArticles.getListener() != null) {
                                    OtherProfilesLikedArticles.getListener().updateOtherPeopleStatus(data, Constants.LIKE_EVENT);
                                }
                                if (!((BaseActivity) context).hasDestroyed()) {
                                    notifyDataSetChanged();
                                }

                                mToastFactory.showToast("You have un-liked the article " + data.getTitle());

                                List<Articles> cachedMagazinesList = getCachedMagazinesList();
                                if (cachedMagazinesList != null) {
                                    List<Articles> tempList = cachedMagazinesList;
                                    for (int i = 0; i < cachedMagazinesList.size(); i++) {
                                        if (data.getId().equals(tempList.get(i).getId())) {
                                            tempList.get(i).setLiked("false");
                                        }
                                    }

                                    cachedMagazinesList = tempList;

                                    saveCachedMagazinesList(cachedMagazinesList);
                                }

                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                ((BaseActivity) context).dismissProgressDialog();
                                Toast.makeText(context, "Error while un liking article " + data.getTitle(), Toast.LENGTH_LONG).show();
                                data.setIsChecked(true);
                                data.setLiked("true");
                                if (!((BaseActivity) context).hasDestroyed()) {
                                    notifyDataSetChanged();
                                }

                                List<Articles> cachedMagazinesList = getCachedMagazinesList();
                                if (cachedMagazinesList != null) {
                                    List<Articles> tempList = cachedMagazinesList;
                                    for (int i = 0; i < cachedMagazinesList.size(); i++) {
                                        if (data.getId().equals(tempList.get(i).getId())) {
                                            tempList.get(i).setLiked("true");
                                        }
                                    }

                                    cachedMagazinesList = tempList;

                                    saveCachedMagazinesList(cachedMagazinesList);
                                }
                            }
                        });
                    }
                }
            });
        }

        if (holder.articlePhotoRight != null) {
            ImageView photoView = holder.articlePhotoRight;
            photoView.setVisibility(View.VISIBLE);

            if (data.getImage_filename() != null) {
                if (!((BaseActivity) context).hasDestroyed()) {
                    Glide.with(context)
                            .load(data.getImage_filename())
                            .placeholder(R.drawable.img_placeholder)
                            .crossFade()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .dontAnimate()
                            .error(R.drawable.img_placeholder)
                            .into(photoView);
                }
            } else {
                photoView.setImageResource(R.drawable.img_placeholder);
            }

            photoView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, MagazineArticleDetailsActivity.class);
                    intent.putExtra("Title", data.getTitle());
                    intent.putExtra("Image", data.getUrl());
                    intent.putExtra("Article", data);
                    intent.putExtra("Position", position);
                    intent.putExtra("ArticlePlacement", "right");
                    magazineFlipArticlesFragment.startActivityForResult(intent, 500);
                }
            });
        }

        if (holder.magazineAddRight != null) {
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

        if (holder.magazineShareRight != null) {
            ImageView share = holder.magazineShareRight;
            share.setVisibility(View.VISIBLE);
            share.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (data.getImage_filename() != null) {
                        new Util.ImageLoaderTask(v, data).execute(data.getImage_filename());
                    } else {
                        String summary = Html.fromHtml(data.getSummary()).toString();
                        Util.shareNewIntent(v, data.getGenerated_url(), "Article: " + data.getTitle(), summary, null);
                    }
                }
            });
        }

        if (holder.articleFollowRight != null) {

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

        if (holder.tvTopicNameRight != null) {
            if (!TextUtils.isEmpty(data.getTopicName())) {
                holder.tvTopicNameRight.setVisibility(View.VISIBLE);
                holder.tvTopicNameRight.setText(data.getTopicName());
                holder.tvTopicNameRight.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, TopicsDetailActivity.class);
                        intent.putExtra("Topic", data);
                        intent.putExtra("Position", position);
                        intent.putExtra("ArticlePlacement", "right");
                        magazineFlipArticlesFragment.startActivityForResult(intent, 60);
                    }
                });
            } else {
                holder.tvTopicNameRight.setVisibility(View.GONE);
            }
        }

        if (holder.articleSummaryRight != null) {

            float density = context.getResources().getDisplayMetrics().density;

            if (density == 4.0) {
                holder.articleSummaryRight.setVisibility(View.VISIBLE);
            } else if (density == 3.5) {
                holder.articleSummaryRight.setVisibility(View.VISIBLE);
            } else if (density == 3.0) {
                holder.articleSummaryRight.setVisibility(View.VISIBLE);
            } else if (density == 2.0) {
                holder.articleSummaryRight.setVisibility(View.VISIBLE);
            } else {
                holder.articleSummaryRight.setVisibility(View.GONE);
            }

            if (data.getSummary() != null && holder.articleSummaryRight != null) {
                holder.articleSummaryRight
                        .setText(Html.fromHtml(data.getSummary()));
            }
        }

    }

    /**
     * Populates the empty left article
     *
     * @param holder The view holder object
     */
    private void populateEmptyLeftArticle(ViewHolder holder) {
        if (holder.magazineLikeLeft != null) {
            holder.magazineLikeLeft.setVisibility(View.GONE);
        }

        if (holder.articleTitleLeft != null) {
            holder.articleTitleLeft.setVisibility(View.GONE);
        }

        if (holder.articlePhotoLeft != null) {
            ImageView photoView = holder.articlePhotoLeft;
            photoView.setVisibility(View.GONE);
        }

        if (holder.magazineAddLeft != null) {
            ImageView add = holder.magazineAddLeft;
            add.setVisibility(View.GONE);
        }

        if (holder.magazineShareLeft != null) {
            ImageView share = holder.magazineShareLeft;
            share.setVisibility(View.GONE);
        }

        if (holder.articleFollowLeft != null) {
            holder.articleFollowLeft.setVisibility(View.GONE);
        }

        if (holder.tvTopicNameLeft != null) {
            holder.tvTopicNameLeft.setVisibility(View.GONE);
        }

    }

    /**
     * Populates the empty right article
     *
     * @param holder The view holder object
     */
    private void populateEmptyRightArticle(ViewHolder holder) {
        if (holder.magazineLikeRight != null) {
            holder.magazineLikeRight.setVisibility(View.GONE);
        }

        if (holder.articleTitleRight != null) {
            holder.articleTitleRight.setVisibility(View.GONE);
        }

        if (holder.articlePhotoRight != null) {
            ImageView photoView = holder.articlePhotoRight;
            photoView.setVisibility(View.GONE);
        }

        if (holder.magazineAddRight != null) {
            ImageView add = holder.magazineAddRight;
            add.setVisibility(View.GONE);
        }

        if (holder.magazineShareRight != null) {
            ImageView share = holder.magazineShareRight;
            share.setVisibility(View.GONE);
        }

        if (holder.articleFollowRight != null) {
            holder.articleFollowRight.setVisibility(View.GONE);
        }

        if (holder.tvTopicNameRight != null) {
            holder.tvTopicNameRight.setVisibility(View.GONE);
        }

    }

    /**
     * Updates the topic follow status
     *
     * @param data   The articles object
     * @param follow The follow string
     */
    @Override
    public void updateFollowTopicStatus(Articles data, String follow) {
        if (data != null) {
            for (Articles article : allArticles) {

                if (data.getTopicId() != null && data.getTopicId().equals(article.getTopicId())) {
                    article.setTopicFollowing(data.getTopicFollowing());
                    if (!((BaseActivity) context).hasDestroyed()) {
                        notifyDataSetChanged();
                    }

                    List<Articles> cachedMagazinesList = getCachedMagazinesList();

                    if (cachedMagazinesList != null) {
                        List<Articles> tempList = cachedMagazinesList;
                        for (int i = 0; i < cachedMagazinesList.size(); i++) {
                            if (data.getTopicId().equals(tempList.get(i).getTopicId())) {
                                tempList.get(i).setTopicFollowing(data.getTopicFollowing());
                            }
                        }
                        cachedMagazinesList = tempList;

                        saveCachedMagazinesList(cachedMagazinesList);
                    }
                }

            }
        }

    }

    /**
     * The View Holder class
     */
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

        private TextView tvTopicName;

        private TextView tvTopicNameTop;

        private TextView tvTopicNameLeft;

        private TextView tvTopicNameRight;

        private TextView articleSummaryLeft;

        private TextView articleSummaryRight;
    }

    /**
     * Updates the topic follow
     *
     * @param isFollowing  isFollowing or not
     * @param topic        The articles object
     * @param position     The position
     * @param articlePlace The article's placement
     */
    public void updateTopic(boolean isFollowing, Articles topic, int position, String articlePlace) {

        if (TextUtils.isEmpty(articlePlace)) {
            items.remove(position);
            items.add(position, topic);
        } else if ("left".equals(articlePlace)) {
            secondArticle = topic;
        } else if ("right".equals(articlePlace)) {
            thirdArticle = topic;
        }

        for (ListIterator<Articles> it = items.listIterator(); it.hasNext(); ) {
            Articles top = it.next();
            if (!TextUtils.isEmpty(top.getTopicName()) && top.getTopicName().equals(topic.getTopicName())) {
                if (isFollowing) {
                    top.setTopicFollowing("true");
                } else {
                    top.setTopicFollowing("false");
                }
            }

        }
        notifyDataSetChanged();
    }

    /**
     * Updates the articles
     *
     * @param isLiked      isLiked article or not
     * @param articles     The Articles object
     * @param position     The position
     * @param articlePlace The article placement
     */
    public void updateArticle(boolean isLiked, Articles articles, int position, String articlePlace) {
        Log.d("ArticlesBaseAdapter", "The position in updateArticle " + position);
        if (TextUtils.isEmpty(articlePlace)) {
            items.remove(position);
            items.add(position, articles);
        } else if ("left".equals(articlePlace)) {
            secondArticle = articles;
        } else if ("right".equals(articlePlace)) {
            thirdArticle = articles;
        }

        Log.d("ArticlesBaseAdapter", "The items in updateArticle after add " + items.get(position).getTitle() + " position " + position + " liked " + items.get(position).getLiked());

        notifyDataSetChanged();
    }

    /**
     * Gets all the articles
     *
     * @return The articles list
     */
    public List<Articles> getAllItems() {
        getAllArticles = new ArrayList<>(items);
        /*if (getAllArticles.size() >= 2) {
            getAllArticles.add(1, secondArticle);
        }
        if (getAllArticles.size() >= 3) {
            getAllArticles.add(2, thirdArticle);
        }*/
        getAllArticles.add(secondArticle);
        getAllArticles.add(thirdArticle);
        return getAllArticles;
    }

    /**
     * Removes the articles from the list
     *
     * @param articlesList The articles list which needs to be removed
     */
    public void removeItems(List<Articles> articlesList) {
        items.removeAll(articlesList);
        notifyDataSetChanged();
    }

    /**
     * Gets the cached magazines list
     *
     * @return The list of articles
     */
    private List<Articles> getCachedMagazinesList() {
        Type type1 = new TypeToken<List<Articles>>() {
        }.getType();
        String userId = preferenceEndPoint.getStringPreference(Constants.USER_ID);
        List<Articles> cachedMagazinesList = new ArrayList<>();
        if (context != null) {
            String sharedFollowedCachedMagazines = MagazinePreferenceEndPoint.getInstance().getPref(context, userId).getString("followed_cached_magazines", "");
            String sharedRandomCachedMagazines = MagazinePreferenceEndPoint.getInstance().getPref(context, userId).getString("random_cached_magazines", "");

            if (!TextUtils.isEmpty(sharedFollowedCachedMagazines)) {
                String cachedMagazines = sharedFollowedCachedMagazines;
                List<Articles> cachedFollowedMagazinesList = new Gson().fromJson(cachedMagazines, type1);
                cachedMagazinesList.addAll(cachedFollowedMagazinesList);
            }
            if (!TextUtils.isEmpty(sharedRandomCachedMagazines)) {
                String cachedMagazines = sharedRandomCachedMagazines;
                List<Articles> cachedRandomMagazinesList = new Gson().fromJson(cachedMagazines, type1);
                cachedMagazinesList.addAll(cachedRandomMagazinesList);
            }
        }

        return cachedMagazinesList;
    }

    /**
     * Saves the cached magazines list
     *
     * @param cachedMagazinesList The list of articles to be cached
     */
    private void saveCachedMagazinesList(List<Articles> cachedMagazinesList) {
        List<Articles> followedTopicArticles = new ArrayList<>();
        List<Articles> randomTopicArticles = new ArrayList<>();
        for (Articles articles : cachedMagazinesList) {
            if ("true".equals(articles.getTopicFollowing())) {
                followedTopicArticles.add(articles);
            } else {
                randomTopicArticles.add(articles);
            }
        }

        String userId = preferenceEndPoint.getStringPreference(Constants.USER_ID);
        if (context != null) {
            SharedPreferences.Editor editor = MagazinePreferenceEndPoint.getInstance().get(context, userId);
            editor.putString("followed_cached_magazines", new Gson().toJson(new LinkedHashSet<Articles>(followedTopicArticles)));
            editor.putString("random_cached_magazines", new Gson().toJson(new LinkedHashSet<Articles>(randomTopicArticles)));
            editor.commit();
        }
    }

    private void likeUnlikeErrorMessage(Response<ResponseBody> response) {
        try {
            int code = (new JSONObject(response.body().string())).getInt("code");
            if (code == 422) {
                mToastFactory.showToast(R.string.like_unlike_error_message);
                return;
            }

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}