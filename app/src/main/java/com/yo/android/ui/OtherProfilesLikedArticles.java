package com.yo.android.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.aphidmobile.utils.AphidLog;
import com.aphidmobile.utils.UI;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
//import com.squareup.picasso.Picasso;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yo.android.R;
import com.yo.android.adapters.MagazineArticlesBaseAdapter;
import com.yo.android.api.YoApi;
import com.yo.android.chat.ui.fragments.BaseFragment;
import com.yo.android.flip.MagazineArticleDetailsActivity;
import com.yo.android.helpers.MagazinePreferenceEndPoint;
import com.yo.android.model.Articles;
import com.yo.android.util.AutoReflectWishListActionsListener;
import com.yo.android.util.Constants;
import com.yo.android.util.OtherPeopleMagazineReflectListener;
import com.yo.android.util.Util;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import javax.inject.Inject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import se.emilsjolander.flipview.FlipView;

/**
 * Created by root on 15/7/16.
 */
public class OtherProfilesLikedArticles extends BaseFragment implements OtherPeopleMagazineReflectListener {

    private List<Articles> articlesList = new ArrayList<Articles>();
    private MyBaseAdapter myBaseAdapter;
    private static OtherProfilesLikedArticles listener;
    @Inject
    YoApi.YoService yoService;
    private TextView noArticals;
    private FrameLayout flipContainer;
    private ProgressBar mProgress;
    private boolean isFollowing;
    private TextView tvProgressText;

    public static OtherProfilesLikedArticles getListener() {
        return listener;
    }

    public static void setListener(OtherProfilesLikedArticles listener) {
        OtherProfilesLikedArticles.listener = listener;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        listener = this;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.magazine_flip_fragment, container, false);

        noArticals = (TextView) view.findViewById(R.id.txtEmptyArticals);
        flipContainer = (FrameLayout) view.findViewById(R.id.flipView_container);
        mProgress = (ProgressBar) view.findViewById(R.id.progress);
        FlipView flipView = (FlipView) view.findViewById(R.id.flip_view);
        myBaseAdapter = new MyBaseAdapter(getActivity());
        flipView.setAdapter(myBaseAdapter);

        flipContainer.setVisibility(View.GONE);
        tvProgressText = (TextView) view.findViewById(R.id.tv_progress_text);

        return view;

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String userID = getActivity().getIntent().getStringExtra(Constants.USER_ID);
        loadLikedArticles(userID);
    }

    private void loadLikedArticles(String userID) {
        articlesList.clear();
        myBaseAdapter.addItems(articlesList);
        mProgress.setVisibility(View.VISIBLE);
        String accessToken = preferenceEndPoint.getStringPreference("access_token");
        yoService.getOtherProfilesLikedArticlesAPI(accessToken, userID).enqueue(new Callback<List<Articles>>() {
            @Override
            public void onResponse(Call<List<Articles>> call, Response<List<Articles>> response) {
                if (mProgress != null) {
                    mProgress.setVisibility(View.GONE);
                }
                tvProgressText.setVisibility(View.GONE);
                if (!response.body().isEmpty()) {
                    for (int i = 0; i < response.body().size(); i++) {
                        flipContainer.setVisibility(View.VISIBLE);
                        if (noArticals != null) {
                            noArticals.setVisibility(View.GONE);
                        }
                        articlesList.add(response.body().get(i));
                    }
                    myBaseAdapter.addItems(articlesList);
                } else {
                    flipContainer.setVisibility(View.GONE);
                    if (noArticals != null) {
                        noArticals.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Articles>> call, Throwable t) {
                if (mProgress != null) {
                    mProgress.setVisibility(View.GONE);
                }
                tvProgressText.setVisibility(View.GONE);
                flipContainer.setVisibility(View.GONE);
                if (noArticals != null) {
                    noArticals.setVisibility(View.VISIBLE);
                }
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void onPause() {
        super.onPause();
    }

    @Override
    public void updateOtherPeopleStatus(Articles data, String follow) {
        if (myBaseAdapter != null) {
            myBaseAdapter.autoReflectFollowOrLikes(data, follow);
        }
    }

    public class MyBaseAdapter extends BaseAdapter implements AutoReflectWishListActionsListener {


        private Context context;

        private LayoutInflater inflater;

        private Bitmap placeholderBitmap;
        private List<Articles> items;


        private MyBaseAdapter(Context context) {
            inflater = LayoutInflater.from(context);
            this.context = context;
            MagazineArticlesBaseAdapter.reflectListener = this;
            //Use a system resource as the placeholder
            placeholderBitmap =
                    BitmapFactory.decodeResource(context.getResources(), android.R.drawable.dark_header);
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
            ViewHolder holder;
            View layout = convertView;
            if (layout == null) {
                layout = inflater.inflate(R.layout.others_liked_magazine, null);

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
                                if (!((BaseActivity)context).hasDestroyed()) {
                                    notifyDataSetChanged();
                                }
                                if (MagazineArticlesBaseAdapter.reflectListener != null) {
                                    MagazineArticlesBaseAdapter.reflectListener.updateFollowOrLikesStatus(data, Constants.LIKE_EVENT);
                                }
                                if (MagazineArticlesBaseAdapter.mListener != null) {
                                    MagazineArticlesBaseAdapter.mListener.updateMagazineStatus(data, Constants.LIKE_EVENT);
                                }
                                mToastFactory.showToast("You have liked the article " + data.getTitle());
                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                dismissProgressDialog();
                                Toast.makeText(context, "Error while liking article " + data.getTitle(), Toast.LENGTH_LONG).show();
                                data.setIsChecked(false);
                                data.setLiked("false");
                                if (!((BaseActivity)context).hasDestroyed()) {
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
                                if (!((BaseActivity)context).hasDestroyed()) {
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
                                if (!((BaseActivity)context).hasDestroyed()) {
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

                    Intent intent = new Intent(getActivity(), CreateMagazineActivity.class);
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
                        String summary = Html.fromHtml(data.getSummary()).toString();
                        Util.shareNewIntent(v, data.getGenerated_url(), "Article: " + data.getTitle(), summary, null);
                    }
                }
            });
            if ("true".equals(data.getIsFollowing())) {
                holder.articleFollow.setText("Following");
                holder.articleFollow.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_following_tick, 0, 0, 0);
                isFollowing = true;
            } else {
                holder.articleFollow.setText("Follow");
                holder.articleFollow.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                isFollowing = false;
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
                                isFollowing = true;
                                if (!((BaseActivity)context).hasDestroyed()) {
                                    notifyDataSetChanged();
                                }
                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                ((BaseActivity) context).dismissProgressDialog();
                                finalHolder.articleFollow.setText("Follow");
                                finalHolder.articleFollow.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                                data.setIsFollowing("false");
                                isFollowing = false;
                                if (!((BaseActivity)context).hasDestroyed()) {
                                    notifyDataSetChanged();
                                }

                            }
                        });
                    } else {


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
                                showProgressDialog();
                                String accessToken = preferenceEndPoint.getStringPreference("access_token");
                                ((BaseActivity) context).showProgressDialog();
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
                                            MagazineArticlesBaseAdapter.mListener.updateMagazineStatus(data, Constants.FOLLOW_EVENT);
                                        }
                                        isFollowing = false;
                                        if (!((BaseActivity)context).hasDestroyed()) {
                                            notifyDataSetChanged();
                                        }

                                    }

                                    @Override
                                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                                        ((BaseActivity) context).dismissProgressDialog();
                                        finalHolder.articleFollow.setText("Following");
                                        finalHolder.articleFollow.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_following_tick, 0, 0, 0);
                                        data.setIsFollowing("true");
                                        isFollowing = true;
                                        if (!((BaseActivity)context).hasDestroyed()) {
                                            notifyDataSetChanged();
                                        }

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

            LinearLayout llArticleInfo = (LinearLayout)layout.findViewById(R.id.ll_article_info);
            if(llArticleInfo != null) {
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

            if(holder.tvTopicName != null) {
                if(!TextUtils.isEmpty(data.getTopicName())) {
                    holder.tvTopicName.setVisibility(View.VISIBLE);
                    holder.tvTopicName.setText(data.getTopicName());
                    holder.tvTopicName.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(context, TopicsDetailActivity.class);
                            /*intent.putExtra("TopicId", data.getTopicId());
                            intent.putExtra("TopicName", data.getTopicName());
                            intent.putExtra("TopicFollowing", data.getTopicFollowing());*/
                            intent.putExtra("Topic", data);
                            intent.putExtra("Position", position);
                            startActivityForResult(intent, 80);
                        }
                    });
                } else {
                    holder.tvTopicName.setVisibility(View.GONE);
                }
            }


            return layout;
        }


        public void addItems(List<Articles> articlesList) {
            items = new ArrayList<>(articlesList);
            if (!((BaseActivity)context).hasDestroyed()) {
                notifyDataSetChanged();
            }
        }

        @Override
        public void updateFollowOrLikesStatus(Articles data, String type) {
            autoReflectFollowOrLikes(data, type);
        }

        private void autoReflectFollowOrLikes(Articles data, String type) {
            if (data != null) {

                if (Constants.FOLLOW_EVENT.equals(type)) {
                    for (Articles article : items) {
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
                    for (Articles article : items) {
                        if (data.getId() != null && data.getId().equals(article.getId())) {
                            article.setLiked(data.getLiked());
                            article.setIsChecked(data.isChecked());
                            if (!((BaseActivity)context).hasDestroyed()) {
                                notifyDataSetChanged();
                            }
                            /*Type type1 = new TypeToken<List<Articles>>() {
                            }.getType();
                            String userId = preferenceEndPoint.getStringPreference(Constants.USER_ID);
                            String cachedMagazines = MagazinePreferenceEndPoint.getInstance().getPref(context, userId).getString("cached_magazines", "");
                            List<Articles> cachedMagazinesList = new Gson().fromJson(cachedMagazines, type1);*/
                            List<Articles> cachedMagazinesList = getCachedMagazinesList();
                            if(cachedMagazinesList != null) {
                                List<Articles> tempList = cachedMagazinesList;
                                for (int i = 0; i < cachedMagazinesList.size(); i++) {
                                    if (data.getId().equals(tempList.get(i).getId())) {
                                        tempList.get(i).setLiked(data.getLiked());
                                    }
                                }

                                cachedMagazinesList = tempList;

                                //preferenceEndPoint.saveStringPreference("cached_magazines", new Gson().toJson(cachedMagazinesList));
                                /*SharedPreferences.Editor editor = MagazinePreferenceEndPoint.getInstance().get(context, userId);
                                editor.putString("cached_magazines", new Gson().toJson(cachedMagazinesList));
                                editor.commit();*/

                                saveCachedMagazinesList(cachedMagazinesList);
                            }
                            break;
                        }
                    }
                }
            }
        }

        public void updateTopic(boolean isFollowing, Articles topic, int position) {
            items.remove(position);
            items.add(position, topic);

            for (ListIterator<Articles> it = items.listIterator(); it.hasNext();) {
                Articles top = it.next();
                if(!TextUtils.isEmpty(top.getTopicName()) && top.getTopicName().equals(topic.getTopicName())) {
                    if(isFollowing) {
                        top.setTopicFollowing("true");
                    } else {
                        top.setTopicFollowing("false");
                    }
                }

            }
            notifyDataSetChanged();
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 80 && resultCode == getActivity().RESULT_OK) {
            if (data != null) {
                Articles topic = data.getParcelableExtra("UpdatedTopic");
                int pos = data.getIntExtra("Pos", 0);
                boolean isTopicFollowing = Boolean.valueOf(topic.getTopicFollowing());
                myBaseAdapter.updateTopic(isTopicFollowing, topic, pos);
            }

        } else if (requestCode == 500 && resultCode == getActivity().RESULT_OK) {
            if (data != null) {
                Articles articles = data.getParcelableExtra("UpdatedArticle");
                int pos = data.getIntExtra("Pos", 0);
                String articlePlace = data.getStringExtra("ArticlePlace");
                boolean isLiked = Boolean.valueOf(articles.getLiked());
                myBaseAdapter.updateArticle(isLiked, articles, pos, articlePlace);
            }

        }
    }

    private List<Articles> getCachedMagazinesList() {
        Type type1 = new TypeToken<List<Articles>>() {
        }.getType();
        String userId = preferenceEndPoint.getStringPreference(Constants.USER_ID);

        String sharedFollowedCachedMagazines = MagazinePreferenceEndPoint.getInstance().getPref(getActivity(), userId).getString("followed_cached_magazines", "");
        String sharedRandomCachedMagazines = MagazinePreferenceEndPoint.getInstance().getPref(getActivity(), userId).getString("random_cached_magazines", "");

        List<Articles> cachedMagazinesList = new ArrayList<>();
        if(!TextUtils.isEmpty(sharedFollowedCachedMagazines)){
            String cachedMagazines = sharedFollowedCachedMagazines;
            List<Articles> cachedFollowedMagazinesList = new Gson().fromJson(cachedMagazines, type1);
            cachedMagazinesList.addAll(cachedFollowedMagazinesList);
        }
        if(!TextUtils.isEmpty(sharedRandomCachedMagazines)) {
            String cachedMagazines = sharedRandomCachedMagazines;
            List<Articles> cachedRandomMagazinesList = new Gson().fromJson(cachedMagazines, type1);
            cachedMagazinesList.addAll(cachedRandomMagazinesList);
        }

        return cachedMagazinesList;
    }

    private void saveCachedMagazinesList(List<Articles> cachedMagazinesList) {
        List<Articles> followedTopicArticles = new ArrayList<>();
        List<Articles> randomTopicArticles = new ArrayList<>();
        for(Articles articles: cachedMagazinesList) {
            if("true".equals(articles.getTopicFollowing())) {
                followedTopicArticles.add(articles);
            } else {
                randomTopicArticles.add(articles);
            }
        }

        String userId = preferenceEndPoint.getStringPreference(Constants.USER_ID);
        //preferenceEndPoint.saveStringPreference("cached_magazines", new Gson().toJson(cachedMagazinesList));
        SharedPreferences.Editor editor = MagazinePreferenceEndPoint.getInstance().get(getActivity(), userId);
        //editor.putString("cached_magazines", new Gson().toJson(cachedMagazinesList));
        editor.putString("followed_cached_magazines", new Gson().toJson(followedTopicArticles));
        editor.putString("random_cached_magazines", new Gson().toJson(randomTopicArticles));
        editor.commit();
    }
}
