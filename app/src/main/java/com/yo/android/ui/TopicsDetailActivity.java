package com.yo.android.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.aphidmobile.utils.AphidLog;
import com.aphidmobile.utils.UI;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.R;
import com.yo.android.adapters.MagazineArticlesBaseAdapter;
import com.yo.android.api.YoApi;
import com.yo.android.model.Articles;
import com.yo.android.usecase.MagazinesServicesUsecase;
import com.yo.android.usecase.TopicDetailsUsecase;
import com.yo.android.util.Constants;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import se.emilsjolander.flipview.FlipView;

/**
 * This activity is used to display the articles of a topic
 */
public class TopicsDetailActivity extends BaseActivity {

    @Inject
    YoApi.YoService yoService;
    @Inject
    @Named("login")
    protected PreferenceEndPoint preferenceEndPoint;
    @Bind(R.id.tv_no_articles)
    public TextView tvNoArticles;
    @Bind(R.id.flip_view)
    public FlipView flipView;
    public List<Articles> articlesList = new ArrayList<Articles>();
    public MyBaseAdapter myBaseAdapter;
    private boolean isFollowingTopic;
    public Articles topic;
    private int position;
    private String articlePlacement;
    Handler handler;
    @Inject
    TopicDetailsUsecase topicDetailsUsecase;
    @Inject
    MagazinesServicesUsecase magazinesServicesUsecase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topics_detail);
        ButterKnife.bind(this);
        myBaseAdapter = new MyBaseAdapter(this);
        flipView.setAdapter(myBaseAdapter);


        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        topic = intent.getParcelableExtra("Topic");
        position = intent.getIntExtra("Position", 0);
        if (intent.hasExtra("ArticlePlacement")) {
            articlePlacement = intent.getStringExtra("ArticlePlacement");
        } else {
            articlePlacement = "";
        }

        String title = topic.getTopicName();

        getSupportActionBar().setTitle(title);

        articlesList.clear();
        topicDetailsUsecase.getArticlesOfTopic(this);

        // clear glide for every 10 seconds
        /*handler = new Handler();
        handler.postDelayed(new Runnable() {

            @Override
            public void run() {
                clearGlideMemory(TopicsDetailActivity.this);
            }
        }, 10000);*/
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void onPause() {
        super.onPause();
        if(handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }

    public class MyBaseAdapter extends BaseAdapter {


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

            if (holder.articleTitle != null) {
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
                        topicDetailsUsecase.likeMyCollectionArticles(data, context, myBaseAdapter);
                    } else {
                        topicDetailsUsecase.unlikeMyCollectionArticles(data, context, myBaseAdapter);
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
                        topicDetailsUsecase.likeMyCollectionArticles(data, context, myBaseAdapter);
                    } else {
                        topicDetailsUsecase.unlikeMyCollectionArticles(data, context, myBaseAdapter);
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
                Glide.with(context)
                        //.load(data.getImage_filename())
                        .load(data.getS3_image_filename())
                        .placeholder(R.drawable.magazine_backdrop)
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .dontAnimate()
                        .into(photoView);

                /*if (articleTitle != null) {
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
                            } else if (maxLines == -1 && articleTitle.getHeight() > 0) {
                                //Log.d("BaseAdapter", "Max lines inside else if" + maxLines);
                                articleTitle.setMaxLines(1);
                                articleTitle.setEllipsize(TextUtils.TruncateAt.END);
                                // Re-assign text to ensure ellipsize is performed correctly.
                                articleTitle.setText(AphidLog.format("%s", data.getTitle()));
                            } else if (maxLines == -1 && articleTitle.getHeight() == 0) {
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
                }*/

                articleTitle.setText(AphidLog.format("%s", data.getTitle()));

                textView1.setText(Html.fromHtml(data.getSummary()));

            } else {
                photoView.setImageResource(R.drawable.magazine_backdrop);
            }

            photoView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    magazinesServicesUsecase.navigateToArticleWebView(context, data, position);
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

            LinearLayout llArticleInfo = (LinearLayout) layout.findViewById(R.id.ll_article_info);
            if (llArticleInfo != null) {
                llArticleInfo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        magazinesServicesUsecase.navigateToArticleWebView(context, data, position);
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

        /**
         * Adds articles to the list
         *
         * @param articlesList
         */
        public void addItems(List<Articles> articlesList) {
            items = new ArrayList<>(articlesList);
            if (!((BaseActivity) context).hasDestroyed()) {
                notifyDataSetChanged();
            }
        }

        /**
         * Updates the article
         *
         * @param isLiked      isLiked or not
         * @param articles     The articles object
         * @param position     The position
         * @param articlePlace The article's placement
         */
        public void updateArticle(boolean isLiked, Articles articles, int position, String articlePlace) {
            items.remove(position);
            items.add(position, articles);

            notifyDataSetChanged();
        }
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

        if ("true".equals(topic.getTopicFollowing())) {
            menu.getItem(0).setTitle("");
            menu.getItem(0).setIcon(R.drawable.ic_mycollections_tick);
            isFollowingTopic = true;
        } else {
            menu.getItem(0).setIcon(null);
            menu.getItem(0).setTitle("Follow");
            isFollowingTopic = false;
        }


        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        final MenuItem menuItem = item;
        switch (item.getItemId()) {
            case R.id.menu_follow_magazine:
                super.onOptionsItemSelected(item);
                if (!Boolean.valueOf(topic.getTopicFollowing())) {
                    showProgressDialog();

                    String accessToken = preferenceEndPoint.getStringPreference("access_token");
                    final List<String> followedTopicsIdsList = new ArrayList<>();
                    if (!TextUtils.isEmpty(preferenceEndPoint.getStringPreference(Constants.MAGAZINE_TAGS))) {
                        String[] prefTags = TextUtils.split(preferenceEndPoint.getStringPreference(Constants.MAGAZINE_TAGS), ",");
                        followedTopicsIdsList.addAll(Arrays.asList(prefTags));
                    }
                    followedTopicsIdsList.add(String.valueOf(topic.getTopicId()));
                    yoService.addTopicsAPI(accessToken, followedTopicsIdsList, "").enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            dismissProgressDialog();
                            menuItem.setTitle("");
                            menuItem.setIcon(R.drawable.ic_mycollections_tick);
                            isFollowingTopic = true;
                            topic.setTopicFollowing("true");

                            EventBus.getDefault().post(topic.getTopicId());
                            if (MagazineArticlesBaseAdapter.reflectTopicsFollowActionsListener != null) {
                                MagazineArticlesBaseAdapter.reflectTopicsFollowActionsListener.updateFollowTopicStatus(topic, Constants.FOLLOW_TOPIC_EVENT);
                            }

                            preferenceEndPoint.saveStringPreference(Constants.MAGAZINE_TAGS, TextUtils.join(",", followedTopicsIdsList));
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            dismissProgressDialog();
                            menuItem.setIcon(null);
                            menuItem.setTitle("Follow");
                            isFollowingTopic = false;
                            topic.setTopicFollowing("false");
                        }
                    });
                } else {

                    final AlertDialog.Builder builder = new AlertDialog.Builder(TopicsDetailActivity.this);

                    LayoutInflater layoutInflater = LayoutInflater.from(TopicsDetailActivity.this);
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
                            final List<String> topicIds = new ArrayList<>();
                            topicIds.add(topic.getTopicId());
                            yoService.removeTopicsAPI(accessToken, topicIds).enqueue(new Callback<ResponseBody>() {
                                @Override
                                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                    try {
                                        dismissProgressDialog();
                                        menuItem.setIcon(null);
                                        menuItem.setTitle("Follow");
                                        isFollowingTopic = false;
                                        topic.setTopicFollowing("false");

                                        if (MagazineArticlesBaseAdapter.reflectTopicsFollowActionsListener != null) {
                                            for (int i = 0; i < topicIds.size(); i++) {
                                                MagazineArticlesBaseAdapter.reflectTopicsFollowActionsListener.updateUnfollowTopicStatus(topicIds.get(i), Constants.FOLLOW_TOPIC_EVENT);
                                            }
                                        }
                                    } finally {
                                        if (response != null && response.body() != null) {
                                            response.body().close();
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<ResponseBody> call, Throwable t) {
                                    dismissProgressDialog();
                                    menuItem.setTitle("");
                                    menuItem.setIcon(R.drawable.ic_mycollections_tick);
                                    isFollowingTopic = true;
                                    topic.setTopicFollowing("true");
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
                updateTopicOnBackPress();
                super.onOptionsItemSelected(item);
                break;
            default:
                super.onOptionsItemSelected(item);
                break;

        }
        return true;
    }

    private void updateTopicOnBackPress() {
        Intent intent = new Intent();
        intent.putExtra("UpdatedTopic", topic);
        intent.putExtra("Pos", position);
        intent.putExtra("ArticlePlace", articlePlacement);
        setResult(RESULT_OK, intent);
    }

    @Override
    public void onBackPressed() {
        updateTopicOnBackPress();
        finish();
        super.onBackPressed();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clearGlideMemory(this);
    }
}