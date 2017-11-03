package com.yo.android.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
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
import android.widget.Toast;

import com.aphidmobile.utils.AphidLog;
import com.aphidmobile.utils.UI;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.R;
import com.yo.android.adapters.MagazineArticlesBaseAdapter;
import com.yo.android.api.YoApi;
import com.yo.android.flip.MagazineArticleDetailsActivity;
import com.yo.android.model.Articles;
import com.yo.android.util.Constants;
import com.yo.android.util.Util;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

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
    private List<Articles> articlesList = new ArrayList<Articles>();
    private MyBaseAdapter myBaseAdapter;
    private boolean isFollowingTopic;
    private Articles topic;
    private int position;
    private String articlePlacement;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topics_detail);

        FlipView flipView = (FlipView) findViewById(R.id.flip_view);
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

        String accessToken = preferenceEndPoint.getStringPreference("access_token");
        List<String> tagIds = new ArrayList<String>();
        tagIds.add(topic.getTopicId());
        showProgressDialog();
        yoService.getArticlesAPI(accessToken, tagIds).enqueue(new Callback<List<Articles>>() {
            @Override
            public void onResponse(Call<List<Articles>> call, Response<List<Articles>> response) {
                dismissProgressDialog();
                if (response.body() != null && response.body().size() > 0) {
                    for (int i = 0; i < response.body().size(); i++) {
                        articlesList.add(response.body().get(i));
                    }
                    myBaseAdapter.addItems(articlesList);
                } else {
                    mToastFactory.showToast("No Articles");
                }
            }

            @Override
            public void onFailure(Call<List<Articles>> call, Throwable t) {
                dismissProgressDialog();
                Toast.makeText(TopicsDetailActivity.this, "Error retrieving Articles", Toast.LENGTH_LONG).show();
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

                holder.articleFollow = UI.findViewById(layout, R.id.imv_magazine_follow);

                holder.tvTopicName = UI.findViewById(layout, R.id.imv_magazine_topic);

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

            photoView.setImageResource(R.drawable.img_placeholder);

            if (data.getImage_filename() != null) {
                new NewImageRenderTask(context, data.getImage_filename(), photoView).execute();
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

                    Intent intent = new Intent(TopicsDetailActivity.this, CreateMagazineActivity.class);
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

        /**
         * Shows the unfollow confirmation dialog
         *
         * @param data        The Articles object
         * @param finalHolder The View holder object
         */
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

        private Button articleFollow;

        private TextView tvTopicName;
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
                    final List<String> followedTopicsIdsList = new ArrayList<String>();
                    if (!TextUtils.isEmpty(preferenceEndPoint.getStringPreference(Constants.MAGAZINE_TAGS))) {
                        String[] prefTags = TextUtils.split(preferenceEndPoint.getStringPreference(Constants.MAGAZINE_TAGS), ",");
                        for (int i = 0; i < prefTags.length; i++) {
                            followedTopicsIdsList.add(prefTags[i]);
                        }
                    }
                    followedTopicsIdsList.add(String.valueOf(topic.getTopicId()));
                    yoService.addTopicsAPI(accessToken, followedTopicsIdsList).enqueue(new Callback<ResponseBody>() {
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
                            List<String> topicIds = new ArrayList<String>();
                            topicIds.add(topic.getTopicId());
                            yoService.removeTopicsAPI(accessToken, topicIds).enqueue(new Callback<ResponseBody>() {
                                @Override
                                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                    dismissProgressDialog();
                                    menuItem.setIcon(null);
                                    menuItem.setTitle("Follow");
                                    isFollowingTopic = false;
                                    topic.setTopicFollowing("false");

                                    if (MagazineArticlesBaseAdapter.reflectTopicsFollowActionsListener != null) {
                                        MagazineArticlesBaseAdapter.reflectTopicsFollowActionsListener.updateFollowTopicStatus(topic, Constants.FOLLOW_TOPIC_EVENT);
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
                Intent intent = new Intent();
                intent.putExtra("UpdatedTopic", topic);
                intent.putExtra("Pos", position);
                intent.putExtra("ArticlePlace", articlePlacement);
                setResult(RESULT_OK, intent);
                super.onOptionsItemSelected(item);
                break;
            default:
                super.onOptionsItemSelected(item);
                break;

        }
        return true;
    }

    @Override
    public void onBackPressed() {

        Intent intent = new Intent();
        intent.putExtra("UpdatedTopic", topic);
        intent.putExtra("Pos", position);
        intent.putExtra("ArticlePlace", articlePlacement);
        setResult(RESULT_OK, intent);
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
}