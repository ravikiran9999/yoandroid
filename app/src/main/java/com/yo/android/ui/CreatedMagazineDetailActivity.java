package com.yo.android.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.aphidmobile.flip.FlipViewController;
import com.aphidmobile.utils.AphidLog;
import com.aphidmobile.utils.UI;
import com.orion.android.common.preferences.PreferenceEndPoint;
import com.squareup.picasso.Picasso;
import com.yo.android.R;
import com.yo.android.api.YoApi;
import com.yo.android.flip.MagazineArticleDetailsActivity;
import com.yo.android.model.Articles;
import com.yo.android.model.MagazineArticles;
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

public class CreatedMagazineDetailActivity extends BaseActivity {

    @Inject
    YoApi.YoService yoService;
    @Inject
    @Named("login")
    protected PreferenceEndPoint preferenceEndPoint;
    private FlipViewController flipView;
    private List<Articles> articlesList = new ArrayList<Articles>();
    private MyBaseAdapter myBaseAdapter;
    private TextView noArticals;
    private FrameLayout flipContainer;
    private String magazineTitle;
    private String magazineId;
    private String magazineDesc;
    private String magazinePrivacy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.created_magazines);
        noArticals = (TextView) findViewById(R.id.txtEmptyArticals);
        flipContainer = (FrameLayout) findViewById(R.id.flipView_container);
        flipView = new FlipViewController(this);
        myBaseAdapter = new MyBaseAdapter(this, flipView);
        flipView.setAdapter(myBaseAdapter);
        flipContainer.addView(flipView);

        flipContainer.setVisibility(View.GONE);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        EventBus.getDefault().register(this);

        Intent intent = getIntent();
        /*final String articleId = intent.getStringExtra("ArticleId");
        final String articleTitle = intent.getStringExtra("ArticleTitle");
        final String articleUrl = intent.getStringExtra("ArticleUrl");
        String articleSummary = intent.getStringExtra("ArticleSummary");
        String articleImage = intent.getStringExtra("ArticleImage");*/
        magazineTitle = intent.getStringExtra("MagazineTitle");
        magazineId = intent.getStringExtra("MagazineId");
        magazineDesc = intent.getStringExtra("MagazineDesc");
        magazinePrivacy = intent.getStringExtra("MagazinePrivacy");

        getSupportActionBar().setTitle(magazineTitle);

       /* TextView tvTitle = (TextView) findViewById(R.id.tv_article_title);
        TextView tvSummary = (TextView) findViewById(R.id.tv_article_short_desc);
        ImageView tvImage = (ImageView) findViewById(R.id.photo);
        TextView tvFullStory = (TextView) findViewById(R.id.tv_category_full_story);
        CheckBox magazineLike = (CheckBox) findViewById(R.id.cb_magazine_like);*/



        articlesList.clear();
        String accessToken = preferenceEndPoint.getStringPreference("access_token");
        yoService.getArticlesOfMagazineAPI(magazineId, accessToken).enqueue(new Callback<MagazineArticles>() {
            @Override
            public void onResponse(Call<MagazineArticles> call, Response<MagazineArticles> response) {

                if (response.body().getArticlesList()!= null && response.body().getArticlesList().size() > 0) {
                    for (int i = 0; i < response.body().getArticlesList().size(); i++) {
                        //if (selectedTopic.equalsIgnoreCase(response.body().get(i).getTopicName())) {
                        //articlesList = new ArrayList<Travels.Data>();
                        flipContainer.setVisibility(View.VISIBLE);
                        if (noArticals != null) {
                            noArticals.setVisibility(View.GONE);
                        }
                        articlesList.add(response.body().getArticlesList().get(i));
                        // }
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
            public void onFailure(Call<MagazineArticles> call, Throwable t) {
                flipContainer.setVisibility(View.GONE);
                if (noArticals != null) {
                    noArticals.setVisibility(View.VISIBLE);
                }
            }
        });


        /*tvTitle.setText(articleTitle);
        tvSummary.setText(articleSummary);
        if (articleImage != null) {
            Picasso.with(this)
                .load(articleImage)
                .into(tvImage);
        }
        tvFullStory.setText(articleTitle);
        tvFullStory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CreatedMagazineDetailActivity.this, MagazineArticleDetailsActivity.class);
                intent.putExtra("Title", articleTitle);
                intent.putExtra("Image", articleUrl);
                startActivity(intent);
            }
        });

        magazineLike.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    String accessToken = preferenceEndPoint.getStringPreference("access_token");
                    yoService.likeArticlesAPI(articleId, accessToken).enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                            mToastFactory.showToast("You have liked the article " + articleTitle);

                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            mToastFactory.showToast("Error while liking article " + articleTitle);
                        }
                    });
                } else {
                    String accessToken = preferenceEndPoint.getStringPreference("access_token");
                    yoService.unlikeArticlesAPI(articleId, accessToken).enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                            mToastFactory.showToast("You have unliked the article " + articleTitle);

                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            mToastFactory.showToast("Error while unliking article " + articleTitle);
                        }
                    });
                }
            }
        });*/

    }

    @Override
    public void onResume() {
        super.onResume();
        flipView.onResume();
    }

    public void onPause() {
        super.onPause();
        flipView.onPause();
    }

    private class MyBaseAdapter extends BaseAdapter {

        private FlipViewController controller;

        private Context context;

        private LayoutInflater inflater;

        private Bitmap placeholderBitmap;
        private List<Articles> items;

        private MyBaseAdapter(Context context, FlipViewController controller) {
            inflater = LayoutInflater.from(context);
            this.context = context;
            this.controller = controller;

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
            if (getCount() > position) {
                return items.get(position);
            }
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            View layout = convertView;
            if (layout == null) {
                layout = inflater.inflate(R.layout.activity_created_magazine_detail, null);

                holder = new ViewHolder();

                /*holder.categoryName = UI
                        .<TextView>findViewById(layout, R.id.tv_category_name);*/

                holder.articleTitle = UI.
                        <TextView>findViewById(layout, R.id.tv_article_title);

                holder.articleShortDesc = UI
                        .<TextView>findViewById(layout, R.id.tv_article_short_desc);

                holder.articlePhoto = UI.findViewById(layout, R.id.photo);

                holder.magazineLike = UI.<CheckBox>findViewById(layout, R.id.cb_magazine_like);

                holder.magazineAdd = UI.<ImageView>findViewById(layout, R.id.imv_magazine_add);

                holder.magazineShare = UI.<ImageView>findViewById(layout, R.id.imv_magazine_share);

                layout.setTag(holder);
            } else {
                holder = (ViewHolder) layout.getTag();
            }

            //final Travels.Data data = Travels.getImgDescriptions().get(position);
            final Articles data = getItem(position);
            if (data == null) {
                return layout;
            }
            holder.magazineLike.setTag(position);
            //if (magazineTopicsSelectionFragment.getSelectedTopic().equals(data.getTopicName())) {
            //articlesList = new ArrayList<Travels.Data>();
            //articlesList.add(data);

            /*holder.categoryName
                    .setText(AphidLog.format("%s", topicName));*/

            holder.articleTitle
                    .setText(AphidLog.format("%s", data.getTitle()));

            if (data.getSummary() != null) {
                holder.articleShortDesc
                        .setText(Html.fromHtml(data.getSummary()));
            }
            //TODO:
            holder.magazineLike.setOnCheckedChangeListener(null);
            if (data.getLiked().equals("true")) {
                // holder.magazineLike.setChecked(true);
                data.setIsChecked(true);
            } else {
                //holder.magazineLike.setChecked(false);
                data.setIsChecked(false);
            }

            holder.magazineLike.setChecked(data.isChecked());

            holder.magazineLike.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    int pos = (int) buttonView.getTag();
                    data.setIsChecked(isChecked);
                    if (isChecked) {
                        String accessToken = preferenceEndPoint.getStringPreference("access_token");
                        yoService.likeArticlesAPI(data.getId(), accessToken).enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                                data.setIsChecked(true);
                                data.setLiked("true");
                                notifyDataSetChanged();
                                //if(response.body().getCode().equals(200) && response.body().getResponse().equals("Success")) {
                                mToastFactory.showToast("You have liked the article " + data.getTitle());
                                //  Toast.makeText(context, "You have liked the article " + data.getTitle(), Toast.LENGTH_LONG).show();
                                    /*}
                                    else {
                                        Toast.makeText(context, "Error while liking article " + data.getTitle(), Toast.LENGTH_LONG).show();
                                    }*/
                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                Toast.makeText(context, "Error while liking article " + data.getTitle(), Toast.LENGTH_LONG).show();
                                data.setIsChecked(false);
                                data.setLiked("false");

                                notifyDataSetChanged();
                            }
                        });
                    } else {
                        String accessToken = preferenceEndPoint.getStringPreference("access_token");
                        yoService.unlikeArticlesAPI(data.getId(), accessToken).enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                data.setIsChecked(false);
                                data.setLiked("false");

                                notifyDataSetChanged();
                                //if(response.body().getCode().equals(200) && response.body().getResponse().equals("Success")) {
                                mToastFactory.showToast("You have unliked the article " + data.getTitle());
                                //  Toast.makeText(context, "You have unliked the article " + data.getTitle(), Toast.LENGTH_LONG).show();
                                    /*}
                                    else {
                                        Toast.makeText(context, "Error while unliking article " + data.getTitle(), Toast.LENGTH_LONG).show();
                                    }*/
                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                Toast.makeText(context, "Error while unliking article " + data.getTitle(), Toast.LENGTH_LONG).show();
                                data.setIsChecked(true);
                                data.setLiked("true");
                                notifyDataSetChanged();
                            }
                        });
                    }
                }
            });
            //TODO:


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
                                /*String detailedDesc = Html.fromHtml(data.getDescription()).toString();
                                intent.putExtra("DetailedDesc", detailedDesc);*/
                            intent.putExtra("Image", data.getUrl());
                            context.startActivity(intent);
                        }
                    });


            ImageView photoView = holder.articlePhoto;

            if (data.getImage_filename() != null) {

                Picasso.with(CreatedMagazineDetailActivity.this)
                        .load(data.getImage_filename())
                        .into(photoView);
            }


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
                    Util.shareArticle(v, data.getUrl());
                }
            });


            return layout;
        }


        public void addItems(List<Articles> articlesList) {
            items = new ArrayList<>(articlesList);
            notifyDataSetChanged();
        }
    }

    private static class ViewHolder {
        //private TextView categoryName;

        private TextView articleTitle;

        private TextView articleShortDesc;

        private ImageView articlePhoto;

        private CheckBox magazineLike;

        private ImageView magazineAdd;

        private ImageView magazineShare;
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
                intent.putExtra("MagazineTitle", magazineTitle);
                intent.putExtra("MagazineId", magazineId);
                intent.putExtra("MagazineDesc", magazineDesc);
                intent.putExtra("MagazinePrivacy", magazinePrivacy);
                startActivityForResult(intent, 3);
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
        // check if the request code is same as what is passed  here it is 2
        if (requestCode == 3 && resultCode == RESULT_OK) {
            if(data!= null) {
                String editedTitle = data.getStringExtra("EditedTitle");
                String editedDesc = data.getStringExtra("EditedDesc");

                getSupportActionBar().setTitle(editedTitle);
            }

        }
    }
}
