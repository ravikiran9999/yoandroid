package com.yo.android.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.aphidmobile.utils.AphidLog;
import com.aphidmobile.utils.UI;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.R;
import com.yo.android.adapters.MagazineArticlesBaseAdapter;
import com.yo.android.api.YoApi;
import com.yo.android.model.Articles;
import com.yo.android.model.OwnMagazine;
import com.yo.android.usecase.MagazinesServicesUsecase;
import com.yo.android.usecase.OthersMagazineDetailsUsecase;
import com.yo.android.util.AutoReflectWishListActionsListener;
import com.yo.android.util.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import javax.inject.Inject;
import javax.inject.Named;

import de.greenrobot.event.EventBus;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import se.emilsjolander.flipview.FlipView;

/**
 * This activity is used to see the articles of other's magazine
 */
public class OthersMagazinesDetailActivity extends BaseActivity {

    @Inject
    YoApi.YoService yoService;
    @Inject
    @Named("login")
    protected PreferenceEndPoint preferenceEndPoint;
    public List<Articles> articlesList = new ArrayList<Articles>();
    public static MyBaseAdapter myBaseAdapter;
    public TextView noArticals;
    public FrameLayout flipContainer;
    private boolean isFollowingMagazine;
    public OwnMagazine ownMagazine;
    private int position;
    public boolean isMagazineDeleted;
    @Inject
    OthersMagazineDetailsUsecase othersMagazineDetailsUsecase;
    @Inject
    MagazinesServicesUsecase magazinesServicesUsecase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.created_magazines);
        noArticals = (TextView) findViewById(R.id.txtEmptyArticals);
        flipContainer = (FrameLayout) findViewById(R.id.flipView_container);
        FlipView flipView = (FlipView) findViewById(R.id.flip_view);
        myBaseAdapter = new MyBaseAdapter(this);
        flipView.setAdapter(myBaseAdapter);

        flipContainer.setVisibility(View.GONE);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        EventBus.getDefault().register(this);

        Intent intent = getIntent();
        ownMagazine = intent.getParcelableExtra("OwnMagazine");
        position = intent.getIntExtra("Position", 0);

        getSupportActionBar().setTitle(ownMagazine.getName());

        noArticals.setText(getString(R.string.others_no_article_added));

        articlesList.clear();

        othersMagazineDetailsUsecase.getArticlesOfMagazine(this);

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void onPause() {
        super.onPause();
    }

    public class MyBaseAdapter extends BaseAdapter implements AutoReflectWishListActionsListener {

        private Context context;

        private LayoutInflater inflater;

        private Bitmap placeholderBitmap;
        private List<Articles> items;

        public AutoReflectWishListActionsListener reflectListener;

        private MyBaseAdapter(Context context) {
            inflater = LayoutInflater.from(context);
            this.context = context;
            reflectListener = this;

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
            ViewHolder holder = null;
            View layout = convertView;
            if (layout == null) {
                layout = inflater.inflate(R.layout.activity_others_magazines_detail, null);

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
            }

            if (holder.articleShortDesc != null) {
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
                        othersMagazineDetailsUsecase.likeOthersMagazineArticles(data, context, myBaseAdapter);
                    } else {
                        othersMagazineDetailsUsecase.unlikeOthersMagazineArticles(data, context, myBaseAdapter);
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
                        othersMagazineDetailsUsecase.likeOthersMagazineArticles(data, context, myBaseAdapter);
                    } else {
                        othersMagazineDetailsUsecase.unlikeOthersMagazineArticles(data, context, myBaseAdapter);
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
                            magazinesServicesUsecase.navigateToArticleWebView(context, data, position);
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
                articleTitle.setText(AphidLog.format("%s", data.getTitle()));
                textView1.setText(Html.fromHtml(data.getSummary()));
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
                    magazinesServicesUsecase.navigateToArticleWebView(context, data, position);
                }
            });

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
                        magazinesServicesUsecase.navigateToArticleWebView(context, data, position);
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
                            startActivityForResult(intent, 70);
                        }
                    });
                } else {
                    holder.tvTopicName.setVisibility(View.GONE);
                }
            }

            return layout;
        }

        /**
         * Adds articles to the list
         * @param articlesList The articles list
         */
        public void addItems(List<Articles> articlesList) {
            items = new ArrayList<>(articlesList);
            if (!hasDestroyed()) {
                notifyDataSetChanged();
            }
        }

        @Override
        public void updateFollowOrLikesStatus(Articles data, String type) {
            if (data != null) {

                if (Constants.FOLLOW_EVENT.equals(type)) {
                    for (Articles article : items) {
                        if (data.getId() != null && data.getId().equals(article.getId())) {
                            article.setIsFollowing(data.getIsFollowing());
                            article.setIsFollow(data.isFollow());
                            if (!hasDestroyed()) {
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
                            if (!hasDestroyed()) {
                                notifyDataSetChanged();
                            }
                            break;
                        }

                    }
                }
            }
        }

        /**
         * Updates the topic following
         * @param isFollowing isFollowing or not
         * @param topic The articles object
         * @param position The position
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
         * @param isLiked isLiked or not
         * @param articles The articles object
         * @param position The position
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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_my_collections_detail, menu);

        if ("true".equals(ownMagazine.getIsFollowing())) {
            menu.getItem(0).setTitle("");
            menu.getItem(0).setIcon(R.drawable.ic_mycollections_tick);
            isFollowingMagazine = true;
        } else {
            menu.getItem(0).setIcon(null);
            menu.getItem(0).setTitle("Follow");
            isFollowingMagazine = false;
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        final MenuItem menuItem = item;
        switch (item.getItemId()) {
            case R.id.menu_follow_magazine:
                super.onOptionsItemSelected(item);
                if (!Boolean.valueOf(ownMagazine.getIsFollowing())) {
                    showProgressDialog();
                    String accessToken = preferenceEndPoint.getStringPreference("access_token");
                    yoService.followMagazineAPI(ownMagazine.getId(), accessToken).enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            try {
                                dismissProgressDialog();
                                menuItem.setTitle("");
                                menuItem.setIcon(R.drawable.ic_mycollections_tick);
                                isFollowingMagazine = true;
                                ownMagazine.setIsFollowing("true");
                                EventBus.getDefault().post(Constants.OTHERS_MAGAZINE_ACTION);
                            } finally {
                                if(response != null && response.body() != null) {
                                    response.body().close();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            dismissProgressDialog();
                            menuItem.setIcon(null);
                            menuItem.setTitle("Follow");
                            ownMagazine.setIsFollowing("false");
                            isFollowingMagazine = false;
                        }
                    });
                } else {


                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);

                    LayoutInflater layoutInflater = LayoutInflater.from(this);
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
                            yoService.unfollowMagazineAPI(ownMagazine.getId(), accessToken).enqueue(new Callback<ResponseBody>() {
                                @Override
                                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                    try {
                                        dismissProgressDialog();
                                        menuItem.setIcon(null);
                                        menuItem.setTitle("Follow");
                                        isFollowingMagazine = false;
                                        ownMagazine.setIsFollowing("false");
                                        //EventBus.getDefault().post(Constants.OTHERS_MAGAZINE_ACTION);
                                        if (MagazineArticlesBaseAdapter.reflectTopicsFollowActionsListener != null) {
                                            MagazineArticlesBaseAdapter.reflectTopicsFollowActionsListener.updateUnfollowTopicStatus(ownMagazine.getId(), Constants.FOLLOW_TOPIC_EVENT);
                                        }
                                    } finally {
                                        if(response != null && response.body() != null) {
                                            response.body().close();
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<ResponseBody> call, Throwable t) {
                                    dismissProgressDialog();
                                    menuItem.setTitle("");
                                    menuItem.setIcon(R.drawable.ic_mycollections_tick);
                                    isFollowingMagazine = true;
                                    ownMagazine.setIsFollowing("true");

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
                updateMagazineOnBackPress();
                super.onOptionsItemSelected(item);
                break;
            default:
                super.onOptionsItemSelected(item);
                break;
        }
        return true;
    }

    private void updateMagazineOnBackPress() {
        Intent intent = new Intent();
        intent.putExtra("Magazine", ownMagazine);
        intent.putExtra("Pos", position);
        intent.putExtra("MagazineDeleted", isMagazineDeleted);
        setResult(RESULT_OK, intent);
    }

    public void onEventMainThread(String action) {
        if (Constants.DELETE_MAGAZINE_ACTION.equals(action)) {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 3 && resultCode == RESULT_OK) {
            if (data != null) {
                String editedTitle = data.getStringExtra("EditedTitle");
                String editedDesc = data.getStringExtra("EditedDesc");

                getSupportActionBar().setTitle(editedTitle);
            }

        } else if (requestCode == 70 && resultCode == RESULT_OK) {
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

    @Override
    public void onBackPressed() {
        updateMagazineOnBackPress();
        finish();
        super.onBackPressed();
    }
}