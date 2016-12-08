package com.yo.android.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.orion.android.common.preferences.PreferenceEndPoint;
//import com.squareup.picasso.Picasso;
import com.yo.android.R;
import com.yo.android.adapters.MagazineArticlesBaseAdapter;
import com.yo.android.api.YoApi;
import com.yo.android.flip.MagazineArticleDetailsActivity;
import com.yo.android.helpers.MagazinePreferenceEndPoint;
import com.yo.android.model.Articles;
import com.yo.android.model.MagazineArticles;
import com.yo.android.util.ArticlesComparator;
import com.yo.android.util.Constants;
import com.yo.android.util.Util;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import se.emilsjolander.flipview.FlipView;

public class MyCollectionDetails extends BaseActivity implements FlipView.OnFlipListener {

    @Inject
    YoApi.YoService yoService;

    @Inject
    @Named("login")
    protected PreferenceEndPoint preferenceEndPoint;
    private List<Articles> articlesList = new ArrayList<Articles>();
    private MyBaseAdapter myBaseAdapter;
    private String type;
    private String topicId;
    private LinkedHashSet<Articles> articlesHashSet = new LinkedHashSet<>();
    private static int currentFlippedPosition;
    private List<String> readArticleIds;
    private LinkedHashSet<List<String>> articlesIdsHashSet = new LinkedHashSet<>();
    private TextView tvNoArticles;
    private FlipView flipView;
    private List<Articles> cachedArticlesList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_collection_details);

        flipView = (FlipView) findViewById(R.id.flip_view);
        tvNoArticles = (TextView) findViewById(R.id.tv_no_articles);
        myBaseAdapter = new MyBaseAdapter(this);
        flipView.setAdapter(myBaseAdapter);


        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        flipView.setOnFlipListener(this);

        Intent intent = getIntent();
        topicId = intent.getStringExtra("TopicId");
        String topicName = intent.getStringExtra("TopicName");
        type = intent.getStringExtra("Type");

        String title = topicName;

        getSupportActionBar().setTitle(title);

        articlesList.clear();

        readArticleIds = new ArrayList<>();
        currentFlippedPosition = 0;
        tvNoArticles.setVisibility(View.GONE);
        flipView.setVisibility(View.VISIBLE);

        if ("Tag".equals(type)) {
            String accessToken = preferenceEndPoint.getStringPreference("access_token");
            List<String> tagIds = new ArrayList<String>();
            tagIds.add(topicId);

            String userId = preferenceEndPoint.getStringPreference(Constants.USER_ID);
            String sharedCachedMagazines = MagazinePreferenceEndPoint.getInstance().getPref(MyCollectionDetails.this, userId).getString("cached_magazines", "");
            List<Articles> cachedMagazinesList = new ArrayList<Articles>();
            List<Articles> cachedTopicMagazinesList = new ArrayList<Articles>();
            if (!TextUtils.isEmpty(sharedCachedMagazines)) {
                Type type = new TypeToken<List<Articles>>() {
                }.getType();
                String cachedMagazines = sharedCachedMagazines;
                cachedMagazinesList = new Gson().fromJson(cachedMagazines, type);
                for (Articles article : cachedMagazinesList) {
                    if (article.getTopicId().equals(topicId)) {
                        cachedTopicMagazinesList.add(article);
                    }
                }
            }
            cachedArticlesList.addAll(cachedTopicMagazinesList);
            List<Articles> tempArticlesList = new ArrayList<Articles>(cachedArticlesList);
            String readCachedIds = MagazinePreferenceEndPoint.getInstance().getPref(MyCollectionDetails.this, userId).getString("read_article_ids", "");
            if (!TextUtils.isEmpty(readCachedIds)) {
                Type type1 = new TypeToken<List<String>>() {
                }.getType();
                String cachedIds = readCachedIds;
                List<String> cachedReadList = new Gson().fromJson(cachedIds, type1);


                for (Articles article : cachedArticlesList) {
                    for (String artId : cachedReadList) {
                        if (article.getId().equals(artId)) {
                            tempArticlesList.remove(article);
                            break;
                        }
                    }
                }
            }
            cachedArticlesList = tempArticlesList;
            List<Articles> emptyUpdatedArticles = new ArrayList<>();
            List<Articles> notEmptyUpdatedArticles = new ArrayList<>();
            for(Articles updatedArticles: cachedArticlesList) {
                if(!TextUtils.isEmpty(updatedArticles.getUpdated())) {
                    notEmptyUpdatedArticles.add(updatedArticles);
                } else {
                    emptyUpdatedArticles.add(updatedArticles);
                }
            }
            Collections.sort(notEmptyUpdatedArticles, new ArticlesComparator());
            Collections.reverse(notEmptyUpdatedArticles);
            notEmptyUpdatedArticles.addAll(emptyUpdatedArticles);
            cachedArticlesList = notEmptyUpdatedArticles;
            myBaseAdapter.addItems(cachedArticlesList);
            if(cachedArticlesList.size()==0) {
                tvNoArticles.setVisibility(View.VISIBLE);
                flipView.setVisibility(View.GONE);
            } else {
                tvNoArticles.setVisibility(View.GONE);
                flipView.setVisibility(View.VISIBLE);
            }
            showProgressDialog();

            List<String> existingArticleIds = checkCachedMagazines();
                getRemainingArticlesInTopics(existingArticleIds);

/*            yoService.getArticlesAPI(accessToken, tagIds).enqueue(new Callback<List<Articles>>() {
                @Override
                public void onResponse(Call<List<Articles>> call, Response<List<Articles>> response) {
                    dismissProgressDialog();
                    if (response.body().size() > 0) {
                        for (int i = 0; i < response.body().size(); i++) {
                            //articlesList.add(response.body().get(i));
                            articlesHashSet.add(response.body().get(i));
                        }
                        articlesList = new ArrayList<Articles>(articlesHashSet);
                        List<Articles> tempArticlesList = new ArrayList<Articles>(articlesList);
                        String userId = preferenceEndPoint.getStringPreference(Constants.USER_ID);
                        String readCachedIds = MagazinePreferenceEndPoint.getInstance().getPref(MyCollectionDetails.this, userId).getString("read_article_ids", "");
                        if (!TextUtils.isEmpty(readCachedIds)) {
                            Type type1 = new TypeToken<List<String>>() {
                            }.getType();
                            String cachedIds = readCachedIds;
                            List<String> cachedReadList = new Gson().fromJson(cachedIds, type1);


                            for (Articles article : articlesList) {
                                for (String artId : cachedReadList) {
                                    if (article.getId().equals(artId)) {
                                        tempArticlesList.remove(article);
                                        break;
                                    }
                                }
                            }
                        }
                        articlesList = tempArticlesList;
                        myBaseAdapter.addItems(articlesList);
                        if(articlesList.size()==0) {
                            tvNoArticles.setVisibility(View.VISIBLE);
                            flipView.setVisibility(View.GONE);
                        } else {
                            tvNoArticles.setVisibility(View.GONE);
                            flipView.setVisibility(View.VISIBLE);
                        }
                    } else {
                        mToastFactory.showToast("No Articles");
                    }
                }

                @Override
                public void onFailure(Call<List<Articles>> call, Throwable t) {
                    dismissProgressDialog();
                    Toast.makeText(MyCollectionDetails.this, "Error retrieving Articles", Toast.LENGTH_LONG).show();
                }
            });*/
        } else {

            String userId = preferenceEndPoint.getStringPreference(Constants.USER_ID);
            String sharedCachedMagazines = MagazinePreferenceEndPoint.getInstance().getPref(MyCollectionDetails.this, userId).getString("cached_magazines", "");
            List<Articles> cachedMagazinesList = new ArrayList<Articles>();
            List<Articles> cachedTopicMagazinesList = new ArrayList<Articles>();
            if (!TextUtils.isEmpty(sharedCachedMagazines)) {
                Type type = new TypeToken<List<Articles>>() {
                }.getType();
                String cachedMagazines = sharedCachedMagazines;
                cachedMagazinesList = new Gson().fromJson(cachedMagazines, type);
                for (Articles article : cachedMagazinesList) {
                    if (article.getTopicId().equals(topicId)) {
                        cachedTopicMagazinesList.add(article);
                    }
                }
            }
            cachedArticlesList.addAll(cachedTopicMagazinesList);
            List<Articles> tempArticlesList = new ArrayList<Articles>(cachedArticlesList);
            String readCachedIds = MagazinePreferenceEndPoint.getInstance().getPref(MyCollectionDetails.this, userId).getString("read_article_ids", "");
            if (!TextUtils.isEmpty(readCachedIds)) {
                Type type1 = new TypeToken<List<String>>() {
                }.getType();
                String cachedIds = readCachedIds;
                List<String> cachedReadList = new Gson().fromJson(cachedIds, type1);

                for (Articles article : cachedArticlesList) {
                    for (String artId : cachedReadList) {
                        if (article.getId().equals(artId)) {
                            tempArticlesList.remove(article);
                            break;
                        }
                    }
                }
            }
            cachedArticlesList = tempArticlesList;
            List<Articles> emptyUpdatedArticles = new ArrayList<>();
            List<Articles> notEmptyUpdatedArticles = new ArrayList<>();
            for(Articles updatedArticles: cachedArticlesList) {
                if(!TextUtils.isEmpty(updatedArticles.getUpdated())) {
                    notEmptyUpdatedArticles.add(updatedArticles);
                } else {
                    emptyUpdatedArticles.add(updatedArticles);
                }
            }
            Collections.sort(notEmptyUpdatedArticles, new ArticlesComparator());
            Collections.reverse(notEmptyUpdatedArticles);
            notEmptyUpdatedArticles.addAll(emptyUpdatedArticles);
            cachedArticlesList = notEmptyUpdatedArticles;
            myBaseAdapter.addItems(cachedArticlesList);
            if(cachedArticlesList.size()==0) {
                tvNoArticles.setVisibility(View.VISIBLE);
                flipView.setVisibility(View.GONE);
            } else {
                tvNoArticles.setVisibility(View.GONE);
                flipView.setVisibility(View.VISIBLE);
            }

            showProgressDialog();
            List<String> existingArticleIds = checkCachedMagazines();
            getRemainingArticlesInMagazine(existingArticleIds);
/*            String accessToken = preferenceEndPoint.getStringPreference("access_token");
            yoService.getArticlesOfMagazineAPI(topicId, accessToken).enqueue(new Callback<MagazineArticles>() {
                @Override
                public void onResponse(Call<MagazineArticles> call, final Response<MagazineArticles> response) {
                    dismissProgressDialog();
                    if (response.body().getArticlesList() != null && response.body().getArticlesList().size() > 0) {
                        for (int i = 0; i < response.body().getArticlesList().size(); i++) {
                            //articlesList.add(response.body().getArticlesList().get(i));
                            articlesHashSet.add(response.body().getArticlesList().get(i));
                        }
                        articlesList = new ArrayList<Articles>(articlesHashSet);
                        List<Articles> tempArticlesList = new ArrayList<Articles>(articlesList);
                        String userId = preferenceEndPoint.getStringPreference(Constants.USER_ID);
                        String readCachedIds = MagazinePreferenceEndPoint.getInstance().getPref(MyCollectionDetails.this, userId).getString("read_article_ids", "");
                        if (!TextUtils.isEmpty(readCachedIds)) {
                            Type type1 = new TypeToken<List<String>>() {
                            }.getType();
                            String cachedIds = readCachedIds;
                            List<String> cachedReadList = new Gson().fromJson(cachedIds, type1);


                            for (Articles article : articlesList) {
                                for (String artId : cachedReadList) {
                                    if (article.getId().equals(artId)) {
                                        tempArticlesList.remove(article);
                                        break;
                                    }
                                }
                            }
                        }
                        articlesList = tempArticlesList;
                        myBaseAdapter.addItems(articlesList);
                        if(articlesList.size()==0) {
                            tvNoArticles.setVisibility(View.VISIBLE);
                            flipView.setVisibility(View.GONE);
                        } else {
                            tvNoArticles.setVisibility(View.GONE);
                            flipView.setVisibility(View.VISIBLE);
                        }
                    }

                }

                @Override
                public void onFailure(Call<MagazineArticles> call, Throwable t) {
                    dismissProgressDialog();
                    tvNoArticles.setVisibility(View.VISIBLE);
                    flipView.setVisibility(View.GONE);
                }
            });*/
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void onPause() {
        super.onPause();
    }

    @Override
    public void onFlippedToPage(FlipView v, int position, long id) {
        currentFlippedPosition = position;
    }

    @Override
    public void onBackPressed() {
        removeReadArticles();
        super.onBackPressed();
    }

    private class MyBaseAdapter extends BaseAdapter {


        private Context context;

        private LayoutInflater inflater;

        private List<Articles> items;

        private MyBaseAdapter(Context context) {
            inflater = LayoutInflater.from(context);
            this.context = context;
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
        public View getView(final int position, View convertView, ViewGroup parent) {
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

                holder.tvTopicName = UI.<TextView>findViewById(layout, R.id.imv_magazine_topic);

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
                    //data.setIsChecked(isChecked);
                    if (isChecked) {
                        showProgressDialog();
                        String accessToken = preferenceEndPoint.getStringPreference("access_token");
                        yoService.likeArticlesAPI(data.getId(), accessToken).enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                dismissProgressDialog();
                                data.setIsChecked(true);
                                data.setLiked("true");
                                if (MagazineArticlesBaseAdapter.reflectListener != null) {
                                    MagazineArticlesBaseAdapter.reflectListener.updateFollowOrLikesStatus(data, Constants.LIKE_EVENT);
                                }
                                if (MagazineArticlesBaseAdapter.mListener != null) {
                                    MagazineArticlesBaseAdapter.mListener.updateMagazineStatus(data, Constants.LIKE_EVENT);
                                }
                                if (!((BaseActivity) context).hasDestroyed()) {
                                    notifyDataSetChanged();
                                }
                                mToastFactory.showToast("You have liked the article " + data.getTitle());
                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                dismissProgressDialog();
                                Toast.makeText(context, "Error while liking article " + data.getTitle(), Toast.LENGTH_LONG).show();
                                data.setIsChecked(false);
                                data.setLiked("false");
                                if (!((BaseActivity) context).hasDestroyed()) {
                                    notifyDataSetChanged();
                                }
                            }
                        });
                    } else {
                        showProgressDialog();
                        String accessToken = preferenceEndPoint.getStringPreference("access_token");
                        yoService.unlikeArticlesAPI(data.getId(), accessToken).enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                dismissProgressDialog();
                                data.setIsChecked(false);
                                data.setLiked("false");
                                if (MagazineArticlesBaseAdapter.reflectListener != null) {
                                    MagazineArticlesBaseAdapter.reflectListener.updateFollowOrLikesStatus(data, Constants.LIKE_EVENT);
                                }
                                if (MagazineArticlesBaseAdapter.mListener != null) {
                                    MagazineArticlesBaseAdapter.mListener.updateMagazineStatus(data, Constants.LIKE_EVENT);
                                }
                                if (!((BaseActivity) context).hasDestroyed()) {
                                    notifyDataSetChanged();
                                }
                                mToastFactory.showToast("You have unliked the article " + data.getTitle());
                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                dismissProgressDialog();
                                Toast.makeText(context, "Error while unliking article " + data.getTitle(), Toast.LENGTH_LONG).show();
                                data.setIsChecked(true);
                                data.setLiked("true");
                                if (!((BaseActivity) context).hasDestroyed()) {
                                    notifyDataSetChanged();
                                }
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
                            intent.putExtra("Article", data);
                            intent.putExtra("Position", position);
                            startActivityForResult(intent, 500);
                        }
                    });


            ImageView photoView = holder.articlePhoto;

            if (data.getImage_filename() != null) {
                Glide.with(context)
                        .load(data.getImage_filename())
                        .placeholder(R.drawable.img_placeholder)
                                //.centerCrop()
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
                    intent.putExtra("Article", data);
                    intent.putExtra("Position", position);
                    startActivityForResult(intent, 500);
                }
            });

            Button followMoreTopics = (Button) layout.findViewById(R.id.btn_magazine_follow_topics);
            followMoreTopics.setVisibility(View.GONE);

            ImageView add = holder.magazineAdd;
            add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(MyCollectionDetails.this, CreateMagazineActivity.class);
                    intent.putExtra(Constants.MAGAZINE_ADD_ARTICLE_ID, data.getId());
                    startActivityForResult(intent, Constants.ADD_ARTICLES_TO_MAGAZINE);
                }
            });

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
                    if (!"true".equals(data.getIsFollowing())) {
                        ((BaseActivity) context).showProgressDialog();
                        String accessToken = preferenceEndPoint.getStringPreference("access_token");
                        yoService.followArticleAPI(data.getId(), accessToken).enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                ((BaseActivity) context).dismissProgressDialog();
                                finalHolder.articleFollow.setText("Following");
                                finalHolder.articleFollow.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_following_tick, 0, 0, 0);
                                data.setIsFollowing("true");
                                if (MagazineArticlesBaseAdapter.reflectListener != null) {
                                    MagazineArticlesBaseAdapter.reflectListener.updateFollowOrLikesStatus(data, Constants.FOLLOW_EVENT);
                                }
                                if (MagazineArticlesBaseAdapter.mListener != null) {
                                    MagazineArticlesBaseAdapter.mListener.updateMagazineStatus(data, Constants.FOLLOW_EVENT);
                                }
                                if (!((BaseActivity) context).hasDestroyed()) {
                                    notifyDataSetChanged();
                                }
                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                ((BaseActivity) context).dismissProgressDialog();
                                finalHolder.articleFollow.setText("Follow");
                                finalHolder.articleFollow.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                                data.setIsFollowing("false");
                                if (!((BaseActivity) context).hasDestroyed()) {
                                    notifyDataSetChanged();
                                }

                            }
                        });
                    } else {
                        showUnFollowConfirmationDialog(data, finalHolder);
                    }
                }
            });

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
                        startActivityForResult(intent, 500);
                    }
                });
            }

            if (holder.tvTopicName != null) {
                if (!TextUtils.isEmpty(data.getTopicName())) {
                    holder.tvTopicName.setVisibility(View.VISIBLE);
                    holder.tvTopicName.setText(data.getTopicName());
                } else {
                    holder.tvTopicName.setVisibility(View.GONE);
                }
            }


            return layout;
        }

        private void showUnFollowConfirmationDialog(final Articles data, final ViewHolder finalHolder) {


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
                                                                                                finalHolder.articleFollow.setText("Follow");
                                                                                                finalHolder.articleFollow.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                                                                                                data.setIsFollowing("false");
                                                                                                if (MagazineArticlesBaseAdapter.reflectListener != null) {
                                                                                                    MagazineArticlesBaseAdapter.reflectListener.updateFollowOrLikesStatus(data, Constants.FOLLOW_EVENT);
                                                                                                }
                                                                                                if (MagazineArticlesBaseAdapter.mListener != null) {
                                                                                                    MagazineArticlesBaseAdapter.mListener.updateMagazineStatus(data, Constants.LIKE_EVENT);
                                                                                                }
                                                                                                if (!((BaseActivity) context).hasDestroyed()) {
                                                                                                    notifyDataSetChanged();
                                                                                                }
                                                                                            }

                                                                                            @Override
                                                                                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                                                                                ((BaseActivity) context).dismissProgressDialog();
                                                                                                finalHolder.articleFollow.setText("Following");
                                                                                                finalHolder.articleFollow.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_following_tick, 0, 0, 0);
                                                                                                data.setIsFollowing("true");
                                                                                                if (!((BaseActivity) context).hasDestroyed()) {
                                                                                                    notifyDataSetChanged();
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


        public void addItems(List<Articles> articlesList) {
            items = new ArrayList<>(articlesList);
            if (!((BaseActivity) context).hasDestroyed()) {
                notifyDataSetChanged();
            }
        }

        public void updateArticle(boolean isLiked, Articles articles, int position, String articlePlace) {
            items.remove(position);
            items.add(position, articles);

            notifyDataSetChanged();
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

        private TextView tvTopicName;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_my_collections_detail, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        final MenuItem menuItem = item;
        switch (item.getItemId()) {
            case R.id.menu_follow_magazine:
                if ("Tag".equals(type)) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(MyCollectionDetails.this);

                    LayoutInflater layoutInflater = LayoutInflater.from(MyCollectionDetails.this);
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
                            List<String> topicIds = new ArrayList<String>();
                            topicIds.add(topicId);
                            yoService.removeTopicsAPI(accessToken, topicIds).enqueue(new Callback<ResponseBody>() {
                                @Override
                                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                    Intent intent = new Intent();
                                    setResult(6, intent);
                                    finish();

                                }

                                @Override
                                public void onFailure(Call<ResponseBody> call, Throwable t) {
                                    menuItem.setIcon(R.drawable.ic_mycollections_tick);
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

                } else {

                    final AlertDialog.Builder builder = new AlertDialog.Builder(MyCollectionDetails.this);

                    LayoutInflater layoutInflater = LayoutInflater.from(MyCollectionDetails.this);
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
                            yoService.unfollowMagazineAPI(topicId, accessToken).enqueue(new Callback<ResponseBody>() {
                                @Override
                                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                    dismissProgressDialog();
                                    Intent intent = new Intent();
                                    setResult(6, intent);
                                    finish();
                                }

                                @Override
                                public void onFailure(Call<ResponseBody> call, Throwable t) {
                                    dismissProgressDialog();
                                    menuItem.setIcon(R.drawable.ic_mycollections_tick);

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
                break;
            case android.R.id.home:
                removeReadArticles();
                super.onOptionsItemSelected(item);
                break;
            default:
                break;

        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 500 && resultCode == RESULT_OK) {
            if (data != null) {
                Articles articles = data.getParcelableExtra("UpdatedArticle");
                int pos = data.getIntExtra("Pos", 0);
                String articlePlace = data.getStringExtra("ArticlePlace");
                boolean isLiked = Boolean.valueOf(articles.getLiked());
                myBaseAdapter.updateArticle(isLiked, articles, pos, articlePlace);
            }

        }
    }

    public void removeReadArticles() {

        Log.d("FlipArticlesFragment", "currentFlippedPosition outside loop " + currentFlippedPosition);
        if (myBaseAdapter.getCount() > 0) {
            for (int i = 0; i <= currentFlippedPosition; i++) {
                String articleId = myBaseAdapter.getItem(i).getId();
                Log.d("FlipArticlesFragment", "Article Id is " + articleId + "currentFlippedPosition " + currentFlippedPosition + " Article Name is " + myBaseAdapter.getItem(i).getTitle() + " Articles size " + myBaseAdapter.getCount());

                readArticleIds.add(articleId);
            }


            articlesIdsHashSet.add(readArticleIds);
            Iterator<List<String>> itr = articlesIdsHashSet.iterator();
            List<String> tempList = new ArrayList<String>();
            while (itr.hasNext()) {
                tempList = itr.next();
            }
            List<String> articlesList = new ArrayList<String>(tempList);

            String userId = preferenceEndPoint.getStringPreference(Constants.USER_ID);
            SharedPreferences.Editor editor = MagazinePreferenceEndPoint.getInstance().get(this, userId);
            String readCachedIds1 = MagazinePreferenceEndPoint.getInstance().getPref(MyCollectionDetails.this, userId).getString("read_article_ids", "");
            List<String> cachedReadList1 = new ArrayList<>();
            if (!TextUtils.isEmpty(readCachedIds1)) {
                Type type2 = new TypeToken<List<String>>() {
                }.getType();
                String cachedIds1 = readCachedIds1;
                cachedReadList1 = new Gson().fromJson(cachedIds1, type2);
            }
            articlesList.addAll(cachedReadList1);
            editor.putString("read_article_ids", new Gson().toJson(articlesList));
            editor.commit();


            String sharedCachedMagazines = MagazinePreferenceEndPoint.getInstance().getPref(this, userId).getString("cached_magazines", "");
            Type type = new TypeToken<List<Articles>>() {
            }.getType();
            String cachedMagazines = sharedCachedMagazines;
            List<Articles> cachedMagazinesList = new Gson().fromJson(cachedMagazines, type);
            //if(currentFlippedPosition >0) {
           /* cachedMagazinesList.remove(myBaseAdapter.secondArticle);
            cachedMagazinesList.remove(myBaseAdapter.thirdArticle);*/
            //}
            /*for (int i = 0; i <= currentFlippedPosition; i++) {
                cachedMagazinesList.remove(i);
                Log.d("FlipArticlesFragment", "Cached Article Name is " + cachedMagazinesList.get(i).getTitle() + " Cached Articles size " + cachedMagazinesList.size());
            }*/
            List<Articles> tempArticlesList = new ArrayList<Articles>(cachedMagazinesList);
            String readCachedIds = MagazinePreferenceEndPoint.getInstance().getPref(this, userId).getString("read_article_ids", "");
            if (!TextUtils.isEmpty(readCachedIds)) {
                Type type1 = new TypeToken<List<String>>() {
                }.getType();
                String cachedIds = readCachedIds;
                List<String> cachedReadList = new Gson().fromJson(cachedIds, type1);
                for (Articles article : cachedMagazinesList) {
                    for (String artId : cachedReadList) {
                        if (article.getId().equals(artId)) {
                            tempArticlesList.remove(article);
                            break;
                        }
                    }
                }
                cachedMagazinesList = tempArticlesList;
            }
            /*String readCachedIds = MagazinePreferenceEndPoint.getInstance().getPref(this, userId).getString("read_article_ids", "");
            Type type1 = new TypeToken<List<String>>() {
            }.getType();
            String cachedIds = readCachedIds;
            List<String> cachedReadList = new Gson().fromJson(cachedIds, type1);
            for (int i = 0; i < cachedMagazinesList.size(); i++) {
                for (int j = 0; j < cachedReadList.size(); j++) {
                    if (cachedMagazinesList.size() > 0 && i < cachedMagazinesList.size() && cachedMagazinesList.get(i).getId().equals(cachedReadList.get(j)))
                        cachedMagazinesList.remove(i);
                    Log.d("FlipArticlesFragment", "Cached Article Name is " + cachedMagazinesList.get(i).getTitle() + " Cached Articles size " + cachedMagazinesList.size());
                }
            }*/
        /*cachedMagazinesList.remove(myBaseAdapter.getItem(0));
        cachedMagazinesList.remove(myBaseAdapter.secondArticle);
        cachedMagazinesList.remove(myBaseAdapter.thirdArticle);
        cachedMagazinesList.remove(myBaseAdapter.getItem(position));*/

            editor.putString("cached_magazines", new Gson().toJson(cachedMagazinesList));
            editor.commit();
        }

    }

    private List<String> checkCachedMagazines() {
        String userId = preferenceEndPoint.getStringPreference(Constants.USER_ID);
        String sharedCachedMagazines = MagazinePreferenceEndPoint.getInstance().getPref(this, userId).getString("cached_magazines", "");
        List<String> existingArticleIds = new ArrayList<>();
        if (!TextUtils.isEmpty(sharedCachedMagazines)) {
            Type type = new TypeToken<List<Articles>>() {
            }.getType();
            String cachedMagazines = sharedCachedMagazines;
            List<Articles> cachedMagazinesList = new Gson().fromJson(cachedMagazines, type);
            for (Articles article : cachedMagazinesList) {
                    if (article.getTopicId().equals(topicId)) {
                     existingArticleIds.add(article.getId());
                    }
            }
        }

        return existingArticleIds;
    }

    private void getRemainingArticlesInTopics(List<String> existingArticles) {
        String accessToken = preferenceEndPoint.getStringPreference("access_token");
        yoService.getRemainingArticlesInTopicAPI(accessToken, topicId, existingArticles).enqueue(new Callback<List<Articles>>() {
            @Override
            public void onResponse(Call<List<Articles>> call, Response<List<Articles>> response) {
                dismissProgressDialog();
                if (response.body() != null && response.body().size() > 0) {
                    for (int i = 0; i < response.body().size(); i++) {
                        //articlesList.add(response.body().get(i));
                        articlesHashSet.add(response.body().get(i));
                    }
                    articlesList = new ArrayList<Articles>(articlesHashSet);
                   /* String userId = preferenceEndPoint.getStringPreference(Constants.USER_ID);
                    String sharedCachedMagazines = MagazinePreferenceEndPoint.getInstance().getPref(MyCollectionDetails.this, userId).getString("cached_magazines", "");
                    List<Articles> cachedMagazinesList = new ArrayList<Articles>();
                    List<Articles> cachedTopicMagazinesList = new ArrayList<Articles>();
                    if (!TextUtils.isEmpty(sharedCachedMagazines)) {
                        Type type = new TypeToken<List<Articles>>() {
                        }.getType();
                        String cachedMagazines = sharedCachedMagazines;
                        cachedMagazinesList = new Gson().fromJson(cachedMagazines, type);
                        for (Articles article : cachedMagazinesList) {
                            if (article.getTopicId().equals(topicId)) {
                                cachedTopicMagazinesList.add(article);
                            }
                        }
                    }
                    articlesList.addAll(cachedTopicMagazinesList);*/
                    articlesList.addAll(cachedArticlesList);
                    List<Articles> tempArticlesList = new ArrayList<Articles>(articlesList);
                    String userId = preferenceEndPoint.getStringPreference(Constants.USER_ID);
                    String readCachedIds = MagazinePreferenceEndPoint.getInstance().getPref(MyCollectionDetails.this, userId).getString("read_article_ids", "");
                    if (!TextUtils.isEmpty(readCachedIds)) {
                        Type type1 = new TypeToken<List<String>>() {
                        }.getType();
                        String cachedIds = readCachedIds;
                        List<String> cachedReadList = new Gson().fromJson(cachedIds, type1);


                        for (Articles article : articlesList) {
                            for (String artId : cachedReadList) {
                                if (article.getId().equals(artId)) {
                                    tempArticlesList.remove(article);
                                    break;
                                }
                            }
                        }
                    }
                    articlesList = tempArticlesList;

                    List<Articles> emptyUpdatedArticles = new ArrayList<>();
                    List<Articles> notEmptyUpdatedArticles = new ArrayList<>();
                    for(Articles updatedArticles: articlesList) {
                        if(!TextUtils.isEmpty(updatedArticles.getUpdated())) {
                            notEmptyUpdatedArticles.add(updatedArticles);
                        } else {
                            emptyUpdatedArticles.add(updatedArticles);
                        }
                    }
                    Collections.sort(notEmptyUpdatedArticles, new ArticlesComparator());
                    Collections.reverse(notEmptyUpdatedArticles);
                    notEmptyUpdatedArticles.addAll(emptyUpdatedArticles);
                    articlesList = notEmptyUpdatedArticles;

                    for(Articles a: articlesList) {
                        Log.d("MyCollectionDetails", "The sorted list is " + a.getId() + " updated " + a.getUpdated());
                    }
                    myBaseAdapter.addItems(articlesList);
                    if(articlesList.size()==0) {
                        tvNoArticles.setVisibility(View.VISIBLE);
                        flipView.setVisibility(View.GONE);
                    } else {
                        tvNoArticles.setVisibility(View.GONE);
                        flipView.setVisibility(View.VISIBLE);
                    }
                } else {
                    //mToastFactory.showToast("No Articles");
                    if(cachedArticlesList.size() == 0) {
                        tvNoArticles.setVisibility(View.VISIBLE);
                        flipView.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Articles>> call, Throwable t) {
               dismissProgressDialog();
                //Toast.makeText(MyCollectionDetails.this, "Error retrieving Articles", Toast.LENGTH_LONG).show();
                if(cachedArticlesList.size() == 0) {
                    tvNoArticles.setVisibility(View.VISIBLE);
                    flipView.setVisibility(View.GONE);
                }
            }
        });
    }

    private void getRemainingArticlesInMagazine(List<String> existingArticles) {
        String accessToken = preferenceEndPoint.getStringPreference("access_token");
        yoService.getRemainingArticlesInMagAPI(accessToken, topicId, existingArticles).enqueue(new Callback<MagazineArticles>() {
            @Override
            public void onResponse(Call<MagazineArticles> call, Response<MagazineArticles> response) {
                dismissProgressDialog();
                if (response.body().getArticlesList() != null && response.body().getArticlesList().size() > 0) {
                    for (int i = 0; i < response.body().getArticlesList().size(); i++) {
                        //articlesList.add(response.body().getArticlesList().get(i));
                        articlesHashSet.add(response.body().getArticlesList().get(i));
                    }
                    articlesList = new ArrayList<Articles>(articlesHashSet);
/*                    String userId = preferenceEndPoint.getStringPreference(Constants.USER_ID);
                    String sharedCachedMagazines = MagazinePreferenceEndPoint.getInstance().getPref(MyCollectionDetails.this, userId).getString("cached_magazines", "");
                    List<Articles> cachedMagazinesList = new ArrayList<Articles>();
                    if (!TextUtils.isEmpty(sharedCachedMagazines)) {
                        Type type = new TypeToken<List<Articles>>() {
                        }.getType();
                        String cachedMagazines = sharedCachedMagazines;
                        cachedMagazinesList = new Gson().fromJson(cachedMagazines, type);
                    }
                    articlesList.addAll(cachedMagazinesList);*/
                    articlesList.addAll(cachedArticlesList);
                    List<Articles> tempArticlesList = new ArrayList<Articles>(articlesList);
                    String userId = preferenceEndPoint.getStringPreference(Constants.USER_ID);
                    String readCachedIds = MagazinePreferenceEndPoint.getInstance().getPref(MyCollectionDetails.this, userId).getString("read_article_ids", "");
                    if (!TextUtils.isEmpty(readCachedIds)) {
                        Type type1 = new TypeToken<List<String>>() {
                        }.getType();
                        String cachedIds = readCachedIds;
                        List<String> cachedReadList = new Gson().fromJson(cachedIds, type1);


                        for (Articles article : articlesList) {
                            for (String artId : cachedReadList) {
                                if (article.getId().equals(artId)) {
                                    tempArticlesList.remove(article);
                                    break;
                                }
                            }
                        }
                    }
                    articlesList = tempArticlesList;
                    List<Articles> emptyUpdatedArticles = new ArrayList<>();
                    List<Articles> notEmptyUpdatedArticles = new ArrayList<>();
                    for(Articles updatedArticles: articlesList) {
                        if(!TextUtils.isEmpty(updatedArticles.getUpdated())) {
                            notEmptyUpdatedArticles.add(updatedArticles);
                        } else {
                            emptyUpdatedArticles.add(updatedArticles);
                        }
                    }
                    Collections.sort(notEmptyUpdatedArticles, new ArticlesComparator());
                    Collections.reverse(notEmptyUpdatedArticles);
                    notEmptyUpdatedArticles.addAll(emptyUpdatedArticles);
                    articlesList = notEmptyUpdatedArticles;
                    for(Articles a: articlesList) {
                        Log.d("MyCollectionDetails", "The sorted list is " + a.getId() + " updated " + a.getUpdated());
                    }
                    myBaseAdapter.addItems(articlesList);
                    if(articlesList.size()==0) {
                        tvNoArticles.setVisibility(View.VISIBLE);
                        flipView.setVisibility(View.GONE);
                    } else {
                        tvNoArticles.setVisibility(View.GONE);
                        flipView.setVisibility(View.VISIBLE);
                    }
                } else {
                    if(cachedArticlesList.size() == 0) {
                        tvNoArticles.setVisibility(View.VISIBLE);
                        flipView.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onFailure(Call<MagazineArticles> call, Throwable t) {
                dismissProgressDialog();
                if(cachedArticlesList.size() == 0) {
                    tvNoArticles.setVisibility(View.VISIBLE);
                    flipView.setVisibility(View.GONE);
                }
            }
        });
    }
}
