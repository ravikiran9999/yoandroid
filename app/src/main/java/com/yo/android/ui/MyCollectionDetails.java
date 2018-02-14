package com.yo.android.ui;

import android.app.AlertDialog;
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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.R;
import com.yo.android.adapters.MagazineArticlesBaseAdapter;
import com.yo.android.api.YoApi;
import com.yo.android.helpers.MagazinePreferenceEndPoint;
import com.yo.android.model.Articles;
import com.yo.android.usecase.MagazinesServicesUsecase;
import com.yo.android.usecase.MyCollectionsDetailsUsecase;
import com.yo.android.util.Constants;

import java.lang.reflect.Type;
import java.util.ArrayList;
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

/**
 * This activity is used to display the articles of a followed topic
 */
public class MyCollectionDetails extends BaseActivity implements FlipView.OnFlipListener {

    @Inject
    YoApi.YoService yoService;

    @Inject
    @Named("login")
    protected PreferenceEndPoint preferenceEndPoint;
    public List<Articles> articlesList = new ArrayList<Articles>();
    private MyBaseAdapter myBaseAdapter;
    private String type;
    private String topicId;
    public LinkedHashSet<Articles> articlesHashSet = new LinkedHashSet<>();
    private static int currentFlippedPosition;
    private List<String> readArticleIds;
    private LinkedHashSet<List<String>> articlesIdsHashSet = new LinkedHashSet<>();
    public TextView tvNoArticles;
    public FlipView flipView;
    private List<Articles> cachedArticlesList = new ArrayList<>();
    private Context context;
    @Inject
    MagazinesServicesUsecase magazinesServicesUsecase;
    @Inject
    MyCollectionsDetailsUsecase myCollectionsDetailsUsecase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_collection_details);
        context = this;
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
            List<String> tagIds = new ArrayList<String>();
            tagIds.add(topicId);

            myCollectionsDetailsUsecase.displayUnreadCachedMagazines(this, topicId, myBaseAdapter);

            /*if (cachedArticlesList.size() == 0) {
                tvNoArticles.setVisibility(View.VISIBLE);
                flipView.setVisibility(View.GONE);
            } else {
                tvNoArticles.setVisibility(View.GONE);
                flipView.setVisibility(View.VISIBLE);
            }*/

            showProgressDialog();

            List<String> existingArticleIds = checkCachedMagazines();
            myCollectionsDetailsUsecase.getRemainingArticlesInTopics(existingArticleIds, topicId, this, myBaseAdapter);
        } else {

            myCollectionsDetailsUsecase.displayUnreadCachedMagazines(this, topicId, myBaseAdapter);
            /*if (cachedArticlesList.size() == 0) {
                tvNoArticles.setVisibility(View.VISIBLE);
                flipView.setVisibility(View.GONE);
            } else {
                tvNoArticles.setVisibility(View.GONE);
                flipView.setVisibility(View.VISIBLE);
            }*/

            showProgressDialog();
            List<String> existingArticleIds = checkCachedMagazines();
            myCollectionsDetailsUsecase.getRemainingArticlesInMagazine(existingArticleIds, this, topicName, myBaseAdapter);
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
        public View getView(final int position, final View convertView, ViewGroup parent) {
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
                textView.setText(AphidLog.format("%s", data.getTitle()));

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
                        myCollectionsDetailsUsecase.likeMyCollectionArticles(data, context, myBaseAdapter);
                    } else {
                        myCollectionsDetailsUsecase.unlikeMyCollectionArticles(data, context, myBaseAdapter);
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
                        myCollectionsDetailsUsecase.likeMyCollectionArticles(data, context, myBaseAdapter);
                    } else {
                        myCollectionsDetailsUsecase.unlikeMyCollectionArticles(data, context, myBaseAdapter);
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
                        .load(data.getS3_image_filename())
                        .thumbnail(0.1f)
                        .placeholder(R.drawable.magazine_backdrop)
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .dontAnimate()
                        .into(photoView);

                if(articleTitle != null) {
                    articleTitle.setText(AphidLog.format("%s", data.getTitle()));
                }

                if(textView1 != null) {
                    textView1.setText(Html.fromHtml(data.getSummary()));
                }

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

            final ViewHolder finalHolder = holder;

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
         * Adds the articles to the list
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
         * Updates the list of articles
         *
         * @param isLiked      isLiked article or not
         * @param articles     The Articles object
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
                            List<String> topicIds = new ArrayList<String>();
                            topicIds.add(topicId);
                            yoService.removeTopicsAPI(accessToken, topicIds).enqueue(new Callback<ResponseBody>() {
                                @Override
                                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                    handleUnfollowTopicOrMagazine(response);
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
                            yoService.unfollowMagazineAPI(topicId, accessToken).enqueue(new Callback<ResponseBody>() {
                                @Override
                                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                    handleUnfollowTopicOrMagazine(response);
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

    private void handleUnfollowTopicOrMagazine(Response<ResponseBody> response) {
        try {
            dismissProgressDialog();

            if (MagazineArticlesBaseAdapter.reflectTopicsFollowActionsListener != null) {
                MagazineArticlesBaseAdapter.reflectTopicsFollowActionsListener.updateUnfollowTopicStatus(topicId, Constants.FOLLOW_TOPIC_EVENT);
            }

            Intent intent = new Intent();
            setResult(6, intent);
            finish();
        } finally {
            if(response != null && response.body() != null) {
                response.body().close();
            }
        }
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

    /**
     * Removes the read articles
     */
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
            String readCachedIds1 = MagazinePreferenceEndPoint.getInstance().getPref(context, userId).getString("read_article_ids", "");
            List<String> cachedReadList1 = new ArrayList<>();
            if (!TextUtils.isEmpty(readCachedIds1)) {
                Type type2 = new TypeToken<List<String>>() {
                }.getType();
                String cachedIds1 = readCachedIds1;
                cachedReadList1 = new Gson().fromJson(cachedIds1, type2);
            }
            articlesList.addAll(cachedReadList1);
            editor.putString("read_article_ids", new Gson().toJson(new LinkedHashSet<String>(articlesList)));
            editor.commit();

            List<Articles> cachedMagazinesList = magazinesServicesUsecase.getCachedMagazinesList(this);

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

            magazinesServicesUsecase.saveCachedMagazinesList(cachedMagazinesList, this);
        }

    }

    /**
     * Checks the cached magazines list with the topicId of the selected topic
     *
     * @return
     */
    private List<String> checkCachedMagazines() {
        List<String> existingArticleIds = new ArrayList<>();

        List<Articles> cachedMagazinesList = magazinesServicesUsecase.getCachedMagazinesList(this);

        if (cachedMagazinesList != null) {
            for (Articles article : cachedMagazinesList) {
                if (article.getTopicId().equals(topicId)) {
                    existingArticleIds.add(article.getId());
                }
            }
        }

        return existingArticleIds;
    }
}