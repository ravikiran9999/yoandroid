package com.yo.android.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import android.widget.TextView;
import android.widget.Toast;

import com.aphidmobile.utils.AphidLog;
import com.aphidmobile.utils.UI;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
//import com.squareup.picasso.Picasso;
import com.yo.android.R;
import com.yo.android.adapters.MagazineArticlesBaseAdapter;
import com.yo.android.flip.MagazineArticleDetailsActivity;
import com.yo.android.model.Articles;
import com.yo.android.util.Constants;
import com.yo.android.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import se.emilsjolander.flipview.FlipView;

public class WishListActivity extends BaseActivity {

    private List<Articles> articlesList = new ArrayList<Articles>();
    private MyBaseAdapter myBaseAdapter;

    private TextView noArticals;
    private FrameLayout flipContainer;
    private LinearLayout llNoWishlist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wish_list);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String title = "Liked Articles";

        getSupportActionBar().setTitle(title);

        noArticals = (TextView) findViewById(R.id.no_data);
        llNoWishlist = (LinearLayout) findViewById(R.id.ll_no_wishlist);
        flipContainer = (FrameLayout) findViewById(R.id.flipView_container);
        FlipView flipView = (FlipView) findViewById(R.id.flip_view);
        myBaseAdapter = new MyBaseAdapter(this);
        flipView.setAdapter(myBaseAdapter);

        flipContainer.setVisibility(View.GONE);

        articlesList.clear();
        myBaseAdapter.addItems(articlesList);
        showProgressDialog();
        String accessToken = preferenceEndPoint.getStringPreference("access_token");
        yoService.getWishListAPI(accessToken, "true").enqueue(new Callback<List<Articles>>() {
            @Override
            public void onResponse(Call<List<Articles>> call, Response<List<Articles>> response) {
                dismissProgressDialog();
                if (response.body() != null && response.body().size() > 0) {
                    for (int i = 0; i < response.body().size(); i++) {
                        flipContainer.setVisibility(View.VISIBLE);
                        if (noArticals != null) {
                            noArticals.setVisibility(View.GONE);
                            llNoWishlist.setVisibility(View.GONE);
                        }
                        articlesList.add(response.body().get(i));
                    }
                    myBaseAdapter.addItems(articlesList);
                } else {
                    flipContainer.setVisibility(View.GONE);
                    if (noArticals != null) {
                        noArticals.setVisibility(View.GONE);
                        llNoWishlist.setVisibility(View.VISIBLE);
                    }
                }

            }

            @Override
            public void onFailure(Call<List<Articles>> call, Throwable t) {
                dismissProgressDialog();
                flipContainer.setVisibility(View.GONE);
                if (noArticals != null) {
                    noArticals.setVisibility(View.GONE);
                    llNoWishlist.setVisibility(View.VISIBLE);
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
            ViewHolder holder;
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
                                if (MagazineArticlesBaseAdapter.reflectListener != null) {
                                    MagazineArticlesBaseAdapter.reflectListener.updateFollowOrLikesStatus(data, Constants.LIKE_EVENT);
                                }
                                if (!((BaseActivity)context).hasDestroyed()) {
                                    notifyDataSetChanged();
                                }
                                mToastFactory.showToast("You have liked the article " + data.getTitle());
                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                Toast.makeText(context, "Error while liking article " + data.getTitle(), Toast.LENGTH_LONG).show();
                                data.setIsChecked(false);
                                data.setLiked("false");
                                if (!((BaseActivity)context).hasDestroyed()) {
                                    notifyDataSetChanged();
                                }
                            }
                        });
                    } else {
                        String accessToken = preferenceEndPoint.getStringPreference("access_token");
                        showProgressDialog();
                        yoService.unlikeArticlesAPI(data.getId(), accessToken).enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                dismissProgressDialog();
                                data.setIsChecked(false);
                                data.setLiked("false");
                                if (MagazineArticlesBaseAdapter.reflectListener != null) {
                                    MagazineArticlesBaseAdapter.reflectListener.updateFollowOrLikesStatus(data, Constants.LIKE_EVENT);
                                }

                                if(OthersMagazinesDetailActivity.myBaseAdapter != null) {
                                    if (OthersMagazinesDetailActivity.myBaseAdapter.reflectListener != null) {
                                        OthersMagazinesDetailActivity.myBaseAdapter.reflectListener.updateFollowOrLikesStatus(data, Constants.LIKE_EVENT);
                                    }
                                }
                                if (!((BaseActivity)context).hasDestroyed()) {
                                    notifyDataSetChanged();
                                }
                                mToastFactory.showToast("You have unliked the article " + data.getTitle());

                                articlesList.clear();
                                myBaseAdapter.addItems(articlesList);

                                refreshWishList();
                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                Toast.makeText(context, "Error while unliking article " + data.getTitle(), Toast.LENGTH_LONG).show();
                                data.setIsChecked(true);
                                data.setLiked("true");
                                dismissProgressDialog();
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

                    Intent intent = new Intent(WishListActivity.this, CreateMagazineActivity.class);
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
                                if (!((BaseActivity)context).hasDestroyed()) {
                                    notifyDataSetChanged();
                                }

                            }
                        });
                    } else {


                        final AlertDialog.Builder builder = new AlertDialog.Builder(WishListActivity.this);

                        LayoutInflater layoutInflater = LayoutInflater.from(WishListActivity.this);
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
                                        if (!((BaseActivity)context).hasDestroyed()) {
                                            notifyDataSetChanged();
                                        }

                                        articlesList.clear();
                                        myBaseAdapter.addItems(articlesList);

                                        refreshWishList();
                                    }

                                    @Override
                                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                                        ((BaseActivity) context).dismissProgressDialog();
                                        finalHolder.articleFollow.setText("Following");
                                        finalHolder.articleFollow.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_following_tick, 0, 0, 0);
                                        data.setIsFollowing("true");
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
                            startActivityForResult(intent, 90);
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

            if(!isLiked) {
                articlesList.clear();
                myBaseAdapter.addItems(articlesList);
                refreshWishList();
            }

            notifyDataSetChanged();
        }
    }

    private void refreshWishList() {
        String accessToken = preferenceEndPoint.getStringPreference("access_token");
        yoService.getWishListAPI(accessToken, "true").enqueue(new Callback<List<Articles>>() {
            @Override
            public void onResponse(Call<List<Articles>> call, Response<List<Articles>> response) {
                if (response.body() != null && response.body().size() > 0) {
                    for (int i = 0; i < response.body().size(); i++) {
                        if (!hasDestroyed()) {
                            if (noArticals != null) {
                                noArticals.setVisibility(View.GONE);
                                llNoWishlist.setVisibility(View.GONE);
                                flipContainer.setVisibility(View.VISIBLE);
                            }
                            articlesList.add(response.body().get(i));
                        }
                    }
                    myBaseAdapter.addItems(articlesList);
                } else {
                    if (!hasDestroyed()) {
                        if (noArticals != null) {
                            noArticals.setVisibility(View.GONE);
                            flipContainer.setVisibility(View.GONE);
                            llNoWishlist.setVisibility(View.VISIBLE);
                        }
                    }
                }

            }

            @Override
            public void onFailure(Call<List<Articles>> call, Throwable t) {
                if (!hasDestroyed()) {
                    if (noArticals != null) {
                        noArticals.setVisibility(View.GONE);
                        flipContainer.setVisibility(View.GONE);
                        llNoWishlist.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 90 && resultCode == RESULT_OK) {
            if (data != null) {
                Articles topic = data.getParcelableExtra("UpdatedTopic");
                int pos = data.getIntExtra("Pos", 0);
                boolean isTopicFollowing = Boolean.valueOf(topic.getTopicFollowing());
                myBaseAdapter.updateTopic(isTopicFollowing, topic, pos);
            }

        } else if (requestCode == 500 && resultCode == RESULT_OK) {
            if (data != null) {
                Articles articles = data.getParcelableExtra("UpdatedArticle");
                int pos = data.getIntExtra("Pos", 0);
                String articlePlace = data.getStringExtra("ArticlePlace");
                boolean isLiked = Boolean.valueOf(articles.getLiked());
                myBaseAdapter.updateArticle(isLiked, articles, pos, articlePlace);
            }

        }
    }

}