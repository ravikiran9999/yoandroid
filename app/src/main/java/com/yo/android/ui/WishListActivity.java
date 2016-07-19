package com.yo.android.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
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
import com.squareup.picasso.Picasso;
import com.yo.android.R;
import com.yo.android.api.YoApi;
import com.yo.android.flip.MagazineArticleDetailsActivity;
import com.yo.android.flip.MagazineTopicsSelectionFragment;
import com.yo.android.model.Articles;
import com.yo.android.util.Constants;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WishListActivity extends BaseActivity {

    private FlipViewController flipView;
    private List<Articles> articlesList = new ArrayList<Articles>();
    private MyBaseAdapter myBaseAdapter;
    @Inject
    YoApi.YoService yoService;
    private String topicName;
    private TextView noArticals;
    private FrameLayout flipContainer;
    private ProgressBar mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.magazine_flip_fragment);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String title = "Wish List";

        getSupportActionBar().setTitle(title);

        noArticals = (TextView) findViewById(R.id.txtEmptyArticals);
        flipContainer = (FrameLayout) findViewById(R.id.flipView_container);
        mProgress = (ProgressBar) findViewById(R.id.progress);
        flipView = new FlipViewController(this);
        myBaseAdapter = new MyBaseAdapter(this, flipView);
        flipView.setAdapter(myBaseAdapter);
        flipContainer.addView(flipView);

        flipContainer.setVisibility(View.GONE);

        articlesList.clear();
        myBaseAdapter.addItems(articlesList);
        if (mProgress != null) {
            mProgress.setVisibility(View.VISIBLE);
        }
        String accessToken = preferenceEndPoint.getStringPreference("access_token");
        yoService.getWishListAPI(accessToken, "true").enqueue(new Callback<List<Articles>>() {
            @Override
            public void onResponse(Call<List<Articles>> call, Response<List<Articles>> response) {
                if (mProgress != null) {
                    mProgress.setVisibility(View.GONE);
                }
                if (response.body().size() > 0) {
                    for (int i = 0; i < response.body().size(); i++) {
                        flipContainer.setVisibility(View.VISIBLE);
                        if (noArticals != null) {
                            noArticals.setVisibility(View.GONE);
                        }
                        //if (selectedTopic.equalsIgnoreCase(response.body().get(i).getTopicName())) {
                        //articlesList = new ArrayList<Travels.Data>();
                        articlesList.add(response.body().get(i));
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
            public void onFailure(Call<List<Articles>> call, Throwable t) {
                if (mProgress != null) {
                    mProgress.setVisibility(View.GONE);
                }
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
                layout = inflater.inflate(R.layout.magazine_flip_layout, null);

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
                //Use an async task to load the bitmap
                /*boolean needReload = true;
                AsyncImageTask previousTask = AsyncDrawable.getTask(photoView);
                if (previousTask != null) {
                    //check if the convertView happens to be previously used
                    if (previousTask.getPageIndex() == position && previousTask.getImageName()
                            .equals(data.getImage_filename())) {
                        needReload = false;
                    } else {
                        previousTask.cancel(true);
                    }
                }

                if (needReload) {
                    AsyncImageTask
                            task =
                            new AsyncImageTask(layout.getContext().getAssets(), photoView, controller, position,
                                    data.getImage_filename());
                    photoView
                            .setImageDrawable(new AsyncDrawable(context.getResources(), placeholderBitmap, task));

                    task.execute();
                }*/

                //photoView.loadUrl(data.getImage_filename());
                Picasso.with(WishListActivity.this)
                        .load(data.getImage_filename())
                        .into(photoView);
            }
            //}

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
    }

}