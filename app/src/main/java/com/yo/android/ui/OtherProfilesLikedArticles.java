package com.yo.android.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.aphidmobile.utils.AphidLog;
import com.aphidmobile.utils.UI;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yo.android.R;
import com.yo.android.adapters.MagazineArticlesBaseAdapter;
import com.yo.android.api.YoApi;
import com.yo.android.chat.ui.fragments.BaseFragment;
import com.yo.android.flip.MagazineArticleDetailsActivity;
import com.yo.android.model.Articles;
import com.yo.android.usecase.MagazinesServicesUsecase;
import com.yo.android.usecase.OthersProfileLikedArticlesUsecase;
import com.yo.android.util.AutoReflectWishListActionsListener;
import com.yo.android.util.Constants;
import com.yo.android.util.OtherPeopleMagazineReflectListener;
import com.yo.android.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import javax.inject.Inject;


import butterknife.BindView;
import butterknife.ButterKnife;
import se.emilsjolander.flipview.FlipView;

/**
 * Created by root on 15/7/16.
 */

/**
 * This class is used to display the Other Yo app users Liked Articles
 */
public class OtherProfilesLikedArticles extends BaseFragment implements OtherPeopleMagazineReflectListener {

    @Inject
    YoApi.YoService yoService;
    @BindView(R.id.refreshContainer)
    SwipeRefreshLayout swipeRefreshContainer;
    @BindView(R.id.txtEmptyArticals)
    public TextView noArticals;
    @BindView(R.id.flipView_container)
    public FrameLayout flipContainer;
    @BindView(R.id.progress)
    public ProgressBar mProgress;
    @BindView(R.id.tv_progress_text)
    public TextView tvProgressText;
    @BindView(R.id.flip_view)
    public FlipView flipView;

    public List<Articles> articlesList = new ArrayList<Articles>();
    public MyBaseAdapter myBaseAdapter;
    private static OtherProfilesLikedArticles listener;
    private Activity activity;
    @Inject
    OthersProfileLikedArticlesUsecase othersProfileLikedArticlesUsecase;
    @Inject
    MagazinesServicesUsecase magazinesServicesUsecase;

    public static OtherProfilesLikedArticles getListener() {
        return listener;
    }

    public static void setListener(OtherProfilesLikedArticles listener) {
        OtherProfilesLikedArticles.listener = listener;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (Activity) context;
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
        ButterKnife.bind(this, view);
        myBaseAdapter = new MyBaseAdapter(activity);
        flipView.setAdapter(myBaseAdapter);
        swipeRefreshContainer.setEnabled(false);
        swipeRefreshContainer.setRefreshing(false);
        flipContainer.setVisibility(View.GONE);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String userID = getActivity().getIntent().getStringExtra(Constants.USER_ID);
        othersProfileLikedArticlesUsecase.loadLikedArticles(userID, this);
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
                        findViewById(layout, R.id.tv_article_title);

                holder.articleShortDesc = UI
                        .findViewById(layout, R.id.tv_article_short_desc);

                holder.articlePhoto = UI.findViewById(layout, R.id.photo);

                holder.magazineLike = UI.findViewById(layout, R.id.cb_magazine_like);

                holder.magazineAdd = UI.findViewById(layout, R.id.imv_magazine_add);

                holder.magazineShare = UI.findViewById(layout, R.id.imv_magazine_share);

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

            /*if (holder.articleTitle != null) {
                holder.fullImageTitle.setVisibility(View.GONE);
                holder.blackMask.setVisibility(View.GONE);
                holder.rlFullImageOptions.setVisibility(View.GONE);
                holder.articleTitle
                        .setText(AphidLog.format("%s", data.getTitle()));

                final TextView textView = holder.articleTitle;

                ViewTreeObserver vto = textView.getViewTreeObserver();
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
                });
            }*/

            if (holder.articleTitle != null) {
                holder.articleTitle.setText(AphidLog.format("%s", data.getTitle()));
            }
            if (holder.articleShortDesc != null) {
                holder.articleShortDesc.setText(Html.fromHtml(data.getSummary()));
            }

            /*if (holder.articleShortDesc != null) {
                if (data.getSummary() != null && holder.articleShortDesc != null) {
                    holder.articleShortDesc.setMaxLines(1000);
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
                    });
                }
            }*/

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
                        othersProfileLikedArticlesUsecase.likeOthersProfileLikedArticles(data, context, myBaseAdapter);
                    } else {
                        othersProfileLikedArticlesUsecase.unlikeOthersProfileLikedArticles(data, context, myBaseAdapter);
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
                        othersProfileLikedArticlesUsecase.likeOthersProfileLikedArticles(data, context, myBaseAdapter);
                    } else {
                        othersProfileLikedArticlesUsecase.unlikeOthersProfileLikedArticles(data, context, myBaseAdapter);
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
                            navigateToArticleWebView(data, position);
                        }
                    });


            final ImageView photoView = holder.articlePhoto;

            photoView.setImageResource(R.drawable.magazine_backdrop);

            if (data.getImage_filename() != null) {
                final TextView fullImageTitle = holder.fullImageTitle;
                final TextView articleTitle = holder.articleTitle;
                final ImageView blackMask = holder.blackMask;
                final RelativeLayout rlFullImageOptions = holder.rlFullImageOptions;
                final TextView textView1 = holder.articleShortDesc;
                RequestOptions requestOptions = new RequestOptions()
                        .placeholder(R.drawable.magazine_backdrop)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .dontAnimate();
                Glide.with(context)
                        .load(data.getS3_image_filename())
                        //.asBitmap()
                        .apply(requestOptions)
                        .into(photoView);
                        /*.into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                int screenHeight = DeviceDimensionsHelper.getDisplayHeight(context);
                                if (resource != null) {
                                    Bitmap bmp = BitmapScaler.scaleToFitHeight(resource, screenHeight);
                                    Glide.with(context)
                                            .load(data.getImage_filename())
                                            .override(bmp.getWidth(), bmp.getHeight())
                                            .placeholder(R.drawable.magazine_backdrop)
                                            .crossFade()
                                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                                            .dontAnimate()
                                            .into(photoView);
                                    if (fullImageTitle != null && articleTitle != null && blackMask != null && rlFullImageOptions != null) {
                                        fullImageTitle.setVisibility(View.VISIBLE);
                                        fullImageTitle.setText(articleTitle.getText().toString());
                                        blackMask.setVisibility(View.VISIBLE);
                                        rlFullImageOptions.setVisibility(View.VISIBLE);

                                    }

                                }
                            }
                        });*/
            } else {
                photoView.setImageResource(R.drawable.magazine_backdrop);
            }

            /*Log.d("OthersProfileLiked", "The photoView.getDrawable() is " + photoView.getDrawable());

            if(photoView.getDrawable() != null) {
                int newHeight = ((BaseActivity) context).getWindowManager().getDefaultDisplay().getHeight() / 4;
                int orgWidth = photoView.getDrawable().getIntrinsicWidth();
                int orgHeight = photoView.getDrawable().getIntrinsicHeight();

                int newWidth = (int) Math.floor((orgWidth * newHeight) / orgHeight);

                Log.d("OthersProfileLiked", "The new width is " + newWidth + "  new height is " + newHeight);

                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                        newWidth, newHeight);
                params.addRule(RelativeLayout.CENTER_HORIZONTAL);
                photoView.setLayoutParams(params);
            }*/

            photoView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    navigateToArticleWebView(data, position);
                }
            });

            Button followMoreTopics = (Button) layout.findViewById(R.id.btn_magazine_follow_topics);
            followMoreTopics.setVisibility(View.GONE);


            ImageView add = holder.magazineAdd;
            add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    magazinesServicesUsecase.onAddClick(context, data);
                }
            });

            ImageView share = holder.magazineShare;
            share.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    magazinesServicesUsecase.onShareClick(v, data);
                }
            });

            LinearLayout llArticleInfo = (LinearLayout) layout.findViewById(R.id.ll_article_info);
            if (llArticleInfo != null) {
                llArticleInfo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        navigateToArticleWebView(data, position);
                    }
                });
            }

            ImageView add1 = holder.fullImageMagazineAdd;
            add1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    magazinesServicesUsecase.onAddClick(context, data);
                }
            });

            ImageView share1 = holder.fullImageMagazineShare;
            share1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    magazinesServicesUsecase.onShareClick(v, data);
                }
            });

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
                            startActivityForResult(intent, 80);
                        }
                    });
                } else {
                    holder.tvTopicName.setVisibility(View.GONE);
                }
            }


            return layout;
        }

        /**
         * Navigates to the article WebView
         * @param data The Articles object
         * @param position The position
         */
        private void navigateToArticleWebView(Articles data, int position) {
            Intent intent = new Intent(context, MagazineArticleDetailsActivity.class);
            intent.putExtra("Title", data.getTitle());
            intent.putExtra("Image", data.getUrl());
            intent.putExtra("Article", data);
            intent.putExtra("Position", position);
            startActivityForResult(intent, 500);
        }

        /**
         * Adds the articles to the list
         *
         * @param articlesList The list of articles
         */
        public void addItems(List<Articles> articlesList) {
            items = new ArrayList<>(articlesList);
            if (!((BaseActivity) context).hasDestroyed()) {
                notifyDataSetChanged();
            }
        }

        @Override
        public void updateFollowOrLikesStatus(Articles data, String type) {
            autoReflectFollowOrLikes(data, type);
        }

        /**
         * Performs operations after following or liking an article
         *
         * @param data The articles list
         * @param type The type of operation whether it is follow or like
         */
        private void autoReflectFollowOrLikes(Articles data, String type) {
            if (data != null) {

                if (Constants.FOLLOW_EVENT.equals(type)) {
                    for (Articles article : items) {
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
                    for (Articles article : items) {
                        if (data.getId() != null && data.getId().equals(article.getId())) {
                            article.setLiked(data.getLiked());
                            article.setIsChecked(data.isChecked());
                            if (!((BaseActivity) context).hasDestroyed()) {
                                notifyDataSetChanged();
                            }

                            List<Articles> cachedMagazinesList = magazinesServicesUsecase.getCachedMagazinesList(getActivity());
                            if (cachedMagazinesList != null) {
                                List<Articles> tempList = cachedMagazinesList;
                                for (int i = 0; i < cachedMagazinesList.size(); i++) {
                                    if (data.getId().equals(tempList.get(i).getId())) {
                                        tempList.get(i).setLiked(data.getLiked());
                                    }
                                }

                                cachedMagazinesList = tempList;

                                magazinesServicesUsecase.saveCachedMagazinesList(cachedMagazinesList, getActivity());
                            }
                            break;
                        }
                    }
                }
            }
        }

        /**
         * Updates the topic following
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
         * @param articlePlace The article placement
         */
        public void updateArticle(boolean isLiked, Articles articles, int position, String articlePlace) {
            items.remove(position);
            items.add(position, articles);

            notifyDataSetChanged();
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

        private TextView tvTopicName;

        private TextView fullImageTitle;

        private ImageView blackMask;

        private RelativeLayout rlFullImageOptions;

        private CheckBox fullImageMagazineLike;

        private ImageView fullImageMagazineAdd;

        private ImageView fullImageMagazineShare;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 80 && resultCode == activity.RESULT_OK) {
            if (data != null) {
                Articles topic = data.getParcelableExtra("UpdatedTopic");
                int pos = data.getIntExtra("Pos", 0);
                boolean isTopicFollowing = Boolean.valueOf(topic.getTopicFollowing());
                myBaseAdapter.updateTopic(isTopicFollowing, topic, pos);
            }

        } else if (requestCode == 500 && resultCode == activity.RESULT_OK) {
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