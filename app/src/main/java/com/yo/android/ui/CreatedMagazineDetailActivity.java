package com.yo.android.ui;

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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aphidmobile.flip.FlipViewController;
import com.aphidmobile.utils.AphidLog;
import com.aphidmobile.utils.UI;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.orion.android.common.preferences.PreferenceEndPoint;
//import com.squareup.picasso.Picasso;
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
import se.emilsjolander.flipview.FlipView;

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

    private void loadArticles() {
        articlesList.clear();
        String accessToken = preferenceEndPoint.getStringPreference("access_token");
        yoService.getArticlesOfMagazineAPI(magazineId, accessToken).enqueue(new Callback<MagazineArticles>() {
            @Override
            public void onResponse(Call<MagazineArticles> call, final Response<MagazineArticles> response) {
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

        private Bitmap placeholderBitmap;
        private List<Articles> items;

        private MyBaseAdapter(Context context) {
            inflater = LayoutInflater.from(context);
            this.context = context;

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
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            View layout = convertView;
            if (layout == null) {
                layout = inflater.inflate(R.layout.activity_created_magazine_detail, null);

                holder = new ViewHolder();

                holder.articleTitle = UI.
                        <TextView>findViewById(layout, R.id.tv_article_title);

                holder.articleShortDesc = UI
                        .<TextView>findViewById(layout, R.id.tv_article_short_desc);

                holder.articlePhoto = UI.findViewById(layout, R.id.photo);

                holder.magazineLike = UI.<CheckBox>findViewById(layout, R.id.cb_magazine_like);

                holder.magazineAdd = UI.<ImageView>findViewById(layout, R.id.imv_magazine_add);

                holder.magazineShare = UI.<ImageView>findViewById(layout, R.id.imv_magazine_share);

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
                        yoService.unlikeArticlesAPI(data.getId(), accessToken).enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                data.setIsChecked(false);
                                data.setLiked("false");
                                if (!((BaseActivity)context).hasDestroyed()) {
                                    notifyDataSetChanged();
                                }
                                mToastFactory.showToast("You have unliked the article " + data.getTitle());
                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
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
                            context.startActivity(intent);
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
                    context.startActivity(intent);
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
                        Util.shareNewIntent(v, data.getGenerated_url(), "Article: " + data.getTitle(), data.getSummary(), null);
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
                        context.startActivity(intent);
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
                            intent.putExtra("TopicId", data.getTopicId());
                            intent.putExtra("TopicName", data.getTopicName());
                            intent.putExtra("TopicFollowing", data.getTopicFollowing());
                            context.startActivity(intent);
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
    }

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

        }
    }
}
