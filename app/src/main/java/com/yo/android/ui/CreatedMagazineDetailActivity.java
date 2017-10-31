package com.yo.android.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.R;
import com.yo.android.api.YoApi;
import com.yo.android.flip.MagazineArticleDetailsActivity;
import com.yo.android.model.Articles;
import com.yo.android.model.MagazineArticles;
import com.yo.android.util.Constants;
import com.yo.android.util.Util;

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
 * The activity which displays on creating a new article in our magazine
 */
public class CreatedMagazineDetailActivity extends BaseActivity {

    @Inject
    YoApi.YoService yoService;
    @Inject
    @Named("login")
    protected PreferenceEndPoint preferenceEndPoint;
    private List<Articles> articlesList = new ArrayList<Articles>();
    private MyBaseAdapter myBaseAdapter;
    private TextView noArticals;
    private FrameLayout flipContainer;
    private String magazineTitle;
    private String magazineId;
    private String magazineDesc;
    private String magazinePrivacy;
    private String editedTitle;
    private String editedDesc;

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
        magazineTitle = intent.getStringExtra("MagazineTitle");
        magazineId = intent.getStringExtra("MagazineId");
        magazineDesc = intent.getStringExtra("MagazineDesc");
        magazinePrivacy = intent.getStringExtra("MagazinePrivacy");

        getSupportActionBar().setTitle(magazineTitle);

        loadArticles();

    }

    /**
     * Gets the articles of a magazine
     */
    private void loadArticles() {
        articlesList.clear();
        showProgressDialog();
        String accessToken = preferenceEndPoint.getStringPreference("access_token");
        yoService.getArticlesOfMagazineAPI(magazineId, accessToken).enqueue(new Callback<MagazineArticles>() {
            @Override
            public void onResponse(Call<MagazineArticles> call, final Response<MagazineArticles> response) {
                dismissProgressDialog();
                if (response.body() != null) {
                    final String id = response.body().getId();
                    if (response.body().getArticlesList() != null && response.body().getArticlesList().size() > 0) {
                        for (int i = 0; i < response.body().getArticlesList().size(); i++) {
                            flipContainer.setVisibility(View.VISIBLE);
                            if (noArticals != null) {
                                noArticals.setVisibility(View.GONE);
                            }
                            articlesList.add(response.body().getArticlesList().get(i));
                        }
                        myBaseAdapter.addItems(articlesList);
                    } else {
                        flipContainer.setVisibility(View.GONE);
                        if (noArticals != null) {
                            noArticals.setVisibility(View.VISIBLE);

                            noArticals.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(CreatedMagazineDetailActivity.this, LoadMagazineActivity.class);
                                    intent.putExtra("MagazineId", id);
                                    intent.putExtra("MagazineTitle", magazineTitle);
                                    intent.putExtra("MagazineDesc", magazineDesc);
                                    intent.putExtra("MagazinePrivacy", magazinePrivacy);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                        }

                    }

                }
            }

            @Override
            public void onFailure(Call<MagazineArticles> call, Throwable t) {
                dismissProgressDialog();
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
                layout = inflater.inflate(R.layout.activity_created_magazine_detail, null);

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
                                if (!((BaseActivity)context).hasDestroyed()) {
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

            photoView.setImageResource(R.drawable.img_placeholder);

            if (data.getImage_filename() != null) {
                Glide.with(context)
                        .load(data.getImage_filename())
                        .placeholder(R.drawable.img_placeholder)
                        //Image size will be reduced 50%
                        .thumbnail(0.5f)
                        .crossFade()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .dontAnimate()
                        .into(photoView);
            } else {
                photoView.setImageResource(R.drawable.img_placeholder);
            }

            Log.d("CreatedMagazineDetail", "The photoView.getDrawable() is " + photoView.getDrawable());

            if(photoView.getDrawable() != null) {
                int newHeight = getWindowManager().getDefaultDisplay().getHeight() / 2;
                int orgWidth = photoView.getDrawable().getIntrinsicWidth();
                int orgHeight = photoView.getDrawable().getIntrinsicHeight();

                int newWidth = (int) Math.floor((orgWidth * newHeight) / orgHeight);

                Log.d("CreatedMagazineDetail", "The new width is " + newWidth + "  new height is " + newHeight);

                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                        newWidth, newHeight);
                params.addRule(RelativeLayout.CENTER_HORIZONTAL);
                photoView.setLayoutParams(params);
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


            ImageView add = holder.magazineAdd;
            add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(CreatedMagazineDetailActivity.this, CreateMagazineActivity.class);
                    intent.putExtra(Constants.MAGAZINE_ADD_ARTICLE_ID, data.getId());
                    startActivityForResult(intent, Constants.ADD_ARTICLES_TO_MAGAZINE);
                }
            });

            ImageView share = holder.magazineShare;
            share.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(data.getImage_filename() !=null) {
                        new Util.ImageLoaderTask(v, data).execute(data.getImage_filename());
                    }
                    else {
                        String summary = Html.fromHtml(data.getSummary()).toString();
                        Util.shareNewIntent(v, data.getGenerated_url(), "Article: " + data.getTitle(), summary, null);
                    }
                }
            });

            LinearLayout llArticleInfo = (LinearLayout)layout.findViewById(R.id.ll_article_info);
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

            if(holder.tvTopicName != null) {
                if(!TextUtils.isEmpty(data.getTopicName())) {
                    holder.tvTopicName.setVisibility(View.VISIBLE);
                    holder.tvTopicName.setText(data.getTopicName());
                    holder.tvTopicName.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(context, TopicsDetailActivity.class);
                            intent.putExtra("Topic", data);
                            intent.putExtra("Position", position);
                            startActivityForResult(intent, 100);
                        }
                    });
                } else {
                    holder.tvTopicName.setVisibility(View.GONE);
                }
            }

            return layout;
        }


        /**
         * Adds the articles to the list
         * @param articlesList The articles list to be added
         */
        public void addItems(List<Articles> articlesList) {
            items = new ArrayList<>(articlesList);
            if (!((BaseActivity)context).hasDestroyed()) {
                notifyDataSetChanged();
            }
        }

        /**
         * Updates the topic following to true or false
         * @param isFollowing isFollowing or not
         * @param topic The articles object
         * @param position The position
         */
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

        /**
         * Updates the article
         * @param isLiked isLiked true or false
         * @param articles The articles object
         * @param position The position
         * @param articlePlace The articles placement
         */
        public void updateArticle(boolean isLiked, Articles articles, int position, String articlePlace) {
                items.remove(position);
                items.add(position, articles);

            notifyDataSetChanged();
        }
    }

    /**
     * The view holder class
     */
    private static class ViewHolder {

        private TextView articleTitle;

        private TextView articleShortDesc;

        private ImageView articlePhoto;

        private CheckBox magazineLike;

        private ImageView magazineAdd;

        private ImageView magazineShare;

        private TextView tvTopicName;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_created_magazine, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.menu_edit:
                Intent intent = new Intent(CreatedMagazineDetailActivity.this, EditMagazineActivity.class);
                if (editedTitle != null) {
                    intent.putExtra("MagazineTitle", editedTitle);
                } else {
                    intent.putExtra("MagazineTitle", magazineTitle);
                }
                intent.putExtra("MagazineId", magazineId);
                if (editedDesc != null) {
                    intent.putExtra("MagazineDesc", editedDesc);
                } else {
                    intent.putExtra("MagazineDesc", magazineDesc);
                }
                intent.putExtra("MagazinePrivacy", magazinePrivacy);
                startActivityForResult(intent, 3);
                break;
            case R.id.menu_add_story:
                intent = new Intent(CreatedMagazineDetailActivity.this, LoadMagazineActivity.class);
                intent.putExtra("MagazineId", magazineId);
                intent.putExtra("MagazineTitle", magazineTitle);
                intent.putExtra("MagazineDesc", magazineDesc);
                intent.putExtra("MagazinePrivacy", magazinePrivacy);
                startActivityForResult(intent, 2);

                break;
            default:
                break;
        }
        return true;
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
        if (requestCode == 2 && resultCode == RESULT_OK) {
            loadArticles();
        } else if (requestCode == 3 && resultCode == RESULT_OK) {
            if (data != null) {
                editedTitle = data.getStringExtra("EditedTitle");
                editedDesc = data.getStringExtra("EditedDesc");

                getSupportActionBar().setTitle(editedTitle);
            }

        } else if (requestCode == 100 && resultCode == RESULT_OK) {
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
