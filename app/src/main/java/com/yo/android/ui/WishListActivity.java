package com.yo.android.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aphidmobile.utils.AphidLog;
import com.aphidmobile.utils.UI;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.yo.android.R;
import com.yo.android.adapters.MagazineArticlesBaseAdapter;
import com.yo.android.flip.MagazineArticleDetailsActivity;
import com.yo.android.model.Articles;
import com.yo.android.util.Constants;
import com.yo.android.util.Util;
import com.yo.android.video.InAppVideoActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import se.emilsjolander.flipview.FlipView;

/**
 * This activity is used to display the liked articles of the users
 */
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
                    try {
                        for (int i = 0; i < response.body().size(); i++) {
                            flipContainer.setVisibility(View.VISIBLE);
                            if (noArticals != null) {
                                noArticals.setVisibility(View.GONE);
                                llNoWishlist.setVisibility(View.GONE);
                            }
                            if (!"...".equalsIgnoreCase(response.body().get(i).getSummary())) {
                                articlesList.add(response.body().get(i));
                            }
                        }
                        myBaseAdapter.addItems(articlesList);
                    } finally {
                        if (response != null && response.body() != null) {
                            try {
                                response.body().clear();
                                response = null;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
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
                        findViewById(layout, R.id.tv_article_title);

                holder.articleShortDesc = UI
                        .findViewById(layout, R.id.tv_article_short_desc);

                holder.articlePhoto = UI.findViewById(layout, R.id.photo);

                holder.magazineLike = UI.findViewById(layout, R.id.cb_magazine_like);

                holder.magazineAdd = UI.findViewById(layout, R.id.imv_magazine_add);

                holder.magazineShare = UI.findViewById(layout, R.id.imv_magazine_share);

                holder.articleFollow = UI.findViewById(layout, R.id.imv_magazine_follow);

                holder.tvTopicName = UI.findViewById(layout, R.id.imv_magazine_topic);

                holder.fullImageTitle = UI.findViewById(layout, R.id.tv_full_image_title_top);

                holder.blackMask = UI.findViewById(layout, R.id.imv_black_mask);

                holder.rlFullImageOptions = UI.findViewById(layout, R.id.rl_full_image_options);

                holder.fullImageMagazineLike = UI.findViewById(layout, R.id.cb_full_image_magazine_like_top);

                holder.fullImageMagazineAdd = UI.findViewById(layout, R.id.imv_full_image_magazine_add_top);

                holder.fullImageMagazineShare = UI.findViewById(layout, R.id.imv_full_image_magazine_share_top);

                layout.setTag(holder);
            } else {
                holder = (ViewHolder) layout.getTag();
            }

            final Articles data = getItem(position);
            if (data == null) {
                return layout;
            }
            holder.magazineLike.setTag(position);

            if (holder.articleTitle != null) {
                holder.fullImageTitle.setVisibility(View.GONE);
                holder.blackMask.setVisibility(View.GONE);
                holder.rlFullImageOptions.setVisibility(View.GONE);
                holder.articleTitle
                        .setText(AphidLog.format("%s", data.getTitle()));

                final TextView textView = holder.articleTitle;
                if (textView != null) {
                    textView.setText(AphidLog.format("%s", data.getTitle()));
                }

                /*ViewTreeObserver vto = textView.getViewTreeObserver();
                vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    private int maxLines = -1;

                    @Override
                    public void onGlobalLayout() {
                        //Log.d("BaseAdapter", "First Title " + data.getTitle() + " max lines " + maxLines + " textView.getHeight() " + textView.getHeight() + " textView.getLineHeight() " + textView.getLineHeight());
                        if (maxLines < 0 && textView.getHeight() > 0 && textView.getLineHeight() > 0) {
                            int height = textView.getHeight();
                            int lineHeight = textView.getLineHeight();
                            maxLines = height / lineHeight;
                            textView.setMaxLines(maxLines);
                            textView.setEllipsize(TextUtils.TruncateAt.END);
                            // Re-assign text to ensure ellipsize is performed correctly.
                            textView.setText(AphidLog.format("%s", data.getTitle()));
                        }
                    }
                });*/
            }

            if (holder.articleShortDesc != null) {
                if (data.getSummary() != null && holder.articleShortDesc != null) {
                    final TextView textView = holder.articleShortDesc;
                    holder.articleShortDesc.setText(Html.fromHtml(data.getSummary()));

                    /*holder.articleShortDesc.setMaxLines(1000);
                    holder.articleShortDesc
                            .setText(Html.fromHtml(data.getSummary()));
                    //Log.d("BaseAdapter", "The text size is " + holder.articleShortDesc.getTextSize());
                    final TextView textView = holder.articleShortDesc;

                    ViewTreeObserver vto = textView.getViewTreeObserver();
                    vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        private int maxLines = -1;

                        @Override
                        public void onGlobalLayout() {
                            if (maxLines < 0 && textView.getHeight() > 0 && textView.getLineHeight() > 0) {
                                int height = textView.getHeight();
                                int lineHeight = textView.getLineHeight();
                                maxLines = height / lineHeight;
                                textView.setMaxLines(maxLines);
                                textView.setEllipsize(TextUtils.TruncateAt.END);
                                // Re-assign text to ensure ellipsize is performed correctly.
                                textView.setText(Html.fromHtml(data.getSummary()));
                            }
                        }
                    });*/
                }
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

                    if (isChecked) {
                        String accessToken = preferenceEndPoint.getStringPreference("access_token");
                        yoService.likeArticlesAPI(data.getId(), accessToken).enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                                data.setIsChecked(true);
                                data.setLiked("true");
                                MagazineArticlesBaseAdapter.initListener();
                                if (MagazineArticlesBaseAdapter.reflectListener != null) {
                                    MagazineArticlesBaseAdapter.reflectListener.updateFollowOrLikesStatus(data, Constants.LIKE_EVENT);
                                }
                                if (!((BaseActivity) context).hasDestroyed()) {
                                    notifyDataSetChanged();
                                }
                                mToastFactory.showToast("You have liked the article " + data.getTitle());
                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                Toast.makeText(context, "Error while liking article " + data.getTitle(), Toast.LENGTH_LONG).show();
                                data.setIsChecked(false);
                                data.setLiked("false");
                                if (!((BaseActivity) context).hasDestroyed()) {
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
                                MagazineArticlesBaseAdapter.initListener();
                                if (MagazineArticlesBaseAdapter.reflectListener != null) {
                                    MagazineArticlesBaseAdapter.reflectListener.updateFollowOrLikesStatus(data, Constants.LIKE_EVENT);
                                }

                                if (OthersMagazinesDetailActivity.myBaseAdapter != null) {
                                    if (OthersMagazinesDetailActivity.myBaseAdapter.reflectListener != null) {
                                        OthersMagazinesDetailActivity.myBaseAdapter.reflectListener.updateFollowOrLikesStatus(data, Constants.LIKE_EVENT);
                                    }
                                }
                                if (!((BaseActivity) context).hasDestroyed()) {
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
                                if (!((BaseActivity) context).hasDestroyed()) {
                                    notifyDataSetChanged();
                                }
                            }
                        });
                    }
                }
            });

            holder.fullImageMagazineLike.setOnCheckedChangeListener(null);
            if (Boolean.valueOf(data.getLiked())) {
                data.setIsChecked(true);
            } else {
                data.setIsChecked(false);
            }

            holder.fullImageMagazineLike.setText("");
            holder.fullImageMagazineLike.setChecked(Boolean.valueOf(data.getLiked()));

            holder.fullImageMagazineLike.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    if (isChecked) {
                        String accessToken = preferenceEndPoint.getStringPreference("access_token");
                        yoService.likeArticlesAPI(data.getId(), accessToken).enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                                data.setIsChecked(true);
                                data.setLiked("true");
                                MagazineArticlesBaseAdapter.initListener();
                                if (MagazineArticlesBaseAdapter.reflectListener != null) {
                                    MagazineArticlesBaseAdapter.reflectListener.updateFollowOrLikesStatus(data, Constants.LIKE_EVENT);
                                }
                                if (!((BaseActivity) context).hasDestroyed()) {
                                    notifyDataSetChanged();
                                }
                                mToastFactory.showToast("You have liked the article " + data.getTitle());
                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                Toast.makeText(context, "Error while liking article " + data.getTitle(), Toast.LENGTH_LONG).show();
                                data.setIsChecked(false);
                                data.setLiked("false");
                                if (!((BaseActivity) context).hasDestroyed()) {
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
                                MagazineArticlesBaseAdapter.initListener();
                                if (MagazineArticlesBaseAdapter.reflectListener != null) {
                                    MagazineArticlesBaseAdapter.reflectListener.updateFollowOrLikesStatus(data, Constants.LIKE_EVENT);
                                }

                                if (OthersMagazinesDetailActivity.myBaseAdapter != null) {
                                    if (OthersMagazinesDetailActivity.myBaseAdapter.reflectListener != null) {
                                        OthersMagazinesDetailActivity.myBaseAdapter.reflectListener.updateFollowOrLikesStatus(data, Constants.LIKE_EVENT);
                                    }
                                }
                                if (!((BaseActivity) context).hasDestroyed()) {
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


            final ImageView photoView = holder.articlePhoto;

            photoView.setImageResource(R.drawable.magazine_backdrop);
            if (data.getImage_filename() != null) {
                //new NewImageRenderTask(context, data.getImage_filename(), photoView).execute();
                final TextView fullImageTitle = holder.fullImageTitle;
                final TextView articleTitle = holder.articleTitle;
                final ImageView blackMask = holder.blackMask;
                final RelativeLayout rlFullImageOptions = holder.rlFullImageOptions;
                final TextView textView1 = holder.articleShortDesc;
                Glide.with(context)
                        .load(data.getImage_filename())
                        .asBitmap()
                        .placeholder(R.drawable.magazine_backdrop)
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .dontAnimate()
                        .into(photoView);

                if (articleTitle != null) {
                    articleTitle.setText(AphidLog.format("%s", data.getTitle()));
                }

                if (textView1 != null && data.getSummary() != null) {
                    textView1.setText(Html.fromHtml(data.getSummary()));
                }
                        /*.into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                int screenWidth = DeviceDimensionsHelper.getDisplayWidth(context);
                                Bitmap bmp = null;
                                if (resource != null) {
                                    try {
                                    bmp = BitmapScaler.scaleToFitWidth(resource, screenWidth);
                                    Glide.with(context)
                                            .load(data.getImage_filename())
                                            .override(bmp.getWidth(), bmp.getHeight())
                                            .placeholder(R.drawable.magazine_backdrop)
                                            .crossFade()
                                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                                            .dontAnimate()
                                            .into(photoView);

                                    int screenHeight = DeviceDimensionsHelper.getDisplayHeight(context);
                                    //Log.d("BaseAdapter", "screenHeight " + screenHeight);
                                       *//*int spaceForImage = screenHeight - 120;
                                       Log.d("BaseAdapter", "spaceForImage" + spaceForImage);*//*
                                    //Log.d("BaseAdapter", "bmp.getHeight()" + bmp.getHeight());
                                    int total = bmp.getHeight() + 50;
                                    //if(bmp.getHeight() >= spaceForImage-30) {
                                    //Log.d("BaseAdapter", "total" + total);
                                    if (screenHeight - total <= 250) {

                                        Log.d("BaseAdapter", "Full screen image");
                                        if (fullImageTitle != null && articleTitle != null && blackMask != null && rlFullImageOptions != null) {
                                            fullImageTitle.setVisibility(View.VISIBLE);
                                            fullImageTitle.setText(articleTitle.getText().toString());
                                            blackMask.setVisibility(View.VISIBLE);
                                            rlFullImageOptions.setVisibility(View.VISIBLE);

                                        }
                                    } else {
                                        fullImageTitle.setVisibility(View.GONE);
                                        blackMask.setVisibility(View.GONE);
                                        rlFullImageOptions.setVisibility(View.GONE);
                                        articleTitle
                                                .setText(AphidLog.format("%s", data.getTitle()));
                                        textView1.setMaxLines(1000);
                                        textView1
                                                .setText(Html.fromHtml(data.getSummary()));
                                    }
                                    }finally {
                                        if(bmp != null) {
                                            bmp.recycle();
                                            bmp = null;
                                        }
                                    }
                                    if(articleTitle != null) {
                                        ViewTreeObserver vto1 = articleTitle.getViewTreeObserver();
                                        vto1.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                                            private int maxLines = -1;

                                            @Override
                                            public void onGlobalLayout() {
                                                //Log.d("BaseAdapter", "Second Title " + data.getTitle() + " max lines " + maxLines + " textView.getHeight() " + textView.getHeight() + " textView.getLineHeight() " + textView.getLineHeight());
                                                if (maxLines < 0 && articleTitle.getHeight() > 0 && articleTitle.getLineHeight() > 0) {
                                                    //Log.d("BaseAdapter", "Max lines inside if" + maxLines);
                                                    int height = articleTitle.getHeight();
                                                    int lineHeight = articleTitle.getLineHeight();
                                                    maxLines = height / lineHeight;
                                                    articleTitle.setMaxLines(maxLines);
                                                    articleTitle.setEllipsize(TextUtils.TruncateAt.END);
                                                    // Re-assign text to ensure ellipsize is performed correctly.
                                                    articleTitle.setText(AphidLog.format("%s", data.getTitle()));
                                                } else if(maxLines == -1 && articleTitle.getHeight() > 0) {
                                                    //Log.d("BaseAdapter", "Max lines inside else if" + maxLines);
                                                    articleTitle.setMaxLines(1);
                                                    articleTitle.setEllipsize(TextUtils.TruncateAt.END);
                                                    // Re-assign text to ensure ellipsize is performed correctly.
                                                    articleTitle.setText(AphidLog.format("%s", data.getTitle()));
                                                } else if(maxLines == -1 && articleTitle.getHeight() == 0) {
                                                    // Log.d("BaseAdapter", "Full screen image after options cut or not shown");
                                                    if (fullImageTitle != null && articleTitle != null && blackMask != null && rlFullImageOptions != null) {
                                                        fullImageTitle.setVisibility(View.VISIBLE);
                                                        fullImageTitle.setText(articleTitle.getText().toString());
                                                        blackMask.setVisibility(View.VISIBLE);
                                                        rlFullImageOptions.setVisibility(View.VISIBLE);

                                                    }
                                                }
                                            }
                                        });
                                    }

                                    if(textView1 != null) {
                                        ViewTreeObserver vto = textView1.getViewTreeObserver();
                                        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                                            private int maxLines = -1;

                                            @Override
                                            public void onGlobalLayout() {
                                                if (maxLines < 0 && textView1.getHeight() > 0 && textView1.getLineHeight() > 0) {
                                                    int height = textView1.getHeight();
                                                    int lineHeight = textView1.getLineHeight();
                                                    maxLines = height / lineHeight;
                                                    textView1.setMaxLines(maxLines);
                                                    textView1.setEllipsize(TextUtils.TruncateAt.END);
                                                    // Re-assign text to ensure ellipsize is performed correctly.
                                                    textView1.setText(Html.fromHtml(data.getSummary()));
                                                }
                                            }
                                        });
                                    }
                                }
                            }
                        });*/
            } else {
                photoView.setImageResource(R.drawable.magazine_backdrop);
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
                        startActivityForResult(intent, 500);
                    }
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
                        String summary = Html.fromHtml(data.getSummary()).toString();
                        Util.shareNewIntent(v, data.getGenerated_url(), "Article: " + data.getTitle(), summary, null);
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
                                try {
                                    ((BaseActivity) context).dismissProgressDialog();
                                    finalHolder.articleFollow.setText("Following");
                                    finalHolder.articleFollow.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_following_tick, 0, 0, 0);
                                    data.setIsFollowing("true");
                                    if (MagazineArticlesBaseAdapter.reflectListener != null) {
                                        MagazineArticlesBaseAdapter.reflectListener.updateFollowOrLikesStatus(data, Constants.FOLLOW_EVENT);
                                    }
                                    if (!((BaseActivity) context).hasDestroyed()) {
                                        notifyDataSetChanged();
                                    }
                                } finally {
                                    if (response != null && response.body() != null) {
                                        response.body().close();
                                    }
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
                                        try {
                                            ((BaseActivity) context).dismissProgressDialog();
                                            finalHolder.articleFollow.setText("Follow");
                                            finalHolder.articleFollow.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                                            data.setIsFollowing("false");
                                            if (MagazineArticlesBaseAdapter.reflectListener != null) {
                                                MagazineArticlesBaseAdapter.reflectListener.updateFollowOrLikesStatus(data, Constants.FOLLOW_EVENT);
                                            }
                                            if (!((BaseActivity) context).hasDestroyed()) {
                                                notifyDataSetChanged();
                                            }

                                            articlesList.clear();
                                            myBaseAdapter.addItems(articlesList);

                                            refreshWishList();
                                        } finally {
                                            if (response != null && response.body() != null) {
                                                response.body().close();
                                            }
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

            if (holder.fullImageMagazineAdd != null) {
                ImageView add1 = holder.fullImageMagazineAdd;
                add1.setOnClickListener(new View.OnClickListener() {
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

            if (holder.fullImageMagazineShare != null) {
                ImageView share1 = holder.fullImageMagazineShare;
                share1.setOnClickListener(new View.OnClickListener() {
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
                    holder.tvTopicName.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(context, TopicsDetailActivity.class);
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

        /**
         * Adds items to the list
         *
         * @param articlesList The articles list
         */
        public void addItems(List<Articles> articlesList) {
            items = new ArrayList<>(articlesList);
            if (!((BaseActivity) context).hasDestroyed()) {
                notifyDataSetChanged();
            }
        }

        /**
         * Updates the topic with the topic following
         *
         * @param isFollowing isFollowing or not
         * @param topic       The articles object
         * @param position    The position
         */
        public void updateTopic(boolean isFollowing, Articles topic, int position) {
            items.remove(position);
            items.add(position, topic);

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
         * @param isLiked      isLiked or not
         * @param articles     The articles object
         * @param position     The position
         * @param articlePlace The articles placement
         */
        public void updateArticle(boolean isLiked, Articles articles, int position, String articlePlace) {
            items.remove(position);
            items.add(position, articles);

            if (!isLiked) {
                articlesList.clear();
                myBaseAdapter.addItems(articlesList);
                refreshWishList();
            }

            notifyDataSetChanged();
        }
    }

    /**
     * Refreshes the liked articles list
     */
    private void refreshWishList() {
        String accessToken = preferenceEndPoint.getStringPreference("access_token");
        yoService.getWishListAPI(accessToken, "true").enqueue(new Callback<List<Articles>>() {
            @Override
            public void onResponse(Call<List<Articles>> call, Response<List<Articles>> response) {
                if (response.body() != null && response.body().size() > 0) {
                    try {
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
                    } finally {
                        if (response != null && response.body() != null) {
                            try {
                                response.body().clear();
                                response = null;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
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

    /**
     * The ViewHolder class
     */
    private static class ViewHolder {

        private TextView articleTitle;

        private TextView articleShortDesc;

        private ImageView articlePhoto;

        private CheckBox magazineLike;

        private ImageView magazineAdd;

        private ImageView magazineShare;

        private Button articleFollow;

        private TextView tvTopicName;

        private TextView fullImageTitle;

        private ImageView blackMask;

        private RelativeLayout rlFullImageOptions;

        private CheckBox fullImageMagazineLike;

        private ImageView fullImageMagazineAdd;

        private ImageView fullImageMagazineShare;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 90 && resultCode == RESULT_OK) { // On coming back from the Topics Details screen
            if (data != null) {
                Articles topic = data.getParcelableExtra("UpdatedTopic");
                int pos = data.getIntExtra("Pos", 0);
                boolean isTopicFollowing = Boolean.valueOf(topic.getTopicFollowing());
                myBaseAdapter.updateTopic(isTopicFollowing, topic, pos);
            }

        } else if (requestCode == 500 && resultCode == RESULT_OK) { // On coming back from the Magazine Article Webview Detail screen
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