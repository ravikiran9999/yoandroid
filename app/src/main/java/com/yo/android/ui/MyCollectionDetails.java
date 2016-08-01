package com.yo.android.ui;

import android.app.Dialog;
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
import android.view.Window;
import android.webkit.WebView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyCollectionDetails extends BaseActivity {

    @Inject
    YoApi.YoService yoService;

    @Inject
    @Named("login")
    protected PreferenceEndPoint preferenceEndPoint;
    private FlipViewController flipView;
    private List<Articles> articlesList = new ArrayList<Articles>();
    private MyBaseAdapter myBaseAdapter;
    private String type;
    private String topicId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        flipView = new FlipViewController(this);
        myBaseAdapter = new MyBaseAdapter(this, flipView);
        flipView.setAdapter(myBaseAdapter);

        setContentView(flipView);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        topicId = intent.getStringExtra("TopicId");
        String topicName = intent.getStringExtra("TopicName");
        type = intent.getStringExtra("Type");

        String title = topicName;

        getSupportActionBar().setTitle(title);

        articlesList.clear();

        if(type.equals("Tag")) {
            String accessToken = preferenceEndPoint.getStringPreference("access_token");
            List<String> tagIds = new ArrayList<String>();
            tagIds.add(topicId);
            yoService.getArticlesAPI(accessToken, tagIds).enqueue(new Callback<List<Articles>>() {
                @Override
                public void onResponse(Call<List<Articles>> call, Response<List<Articles>> response) {
                    if (response.body().size() > 0) {
                        for (int i = 0; i < response.body().size(); i++) {
                            //if (selectedTopic.equalsIgnoreCase(response.body().get(i).getTopicName())) {
                            //articlesList = new ArrayList<Travels.Data>();
                            articlesList.add(response.body().get(i));
                            // }
                        }
                        myBaseAdapter.addItems(articlesList);
                    } else {
                        mToastFactory.showToast("No Articles");
                    }
                }

                @Override
                public void onFailure(Call<List<Articles>> call, Throwable t) {
                    Toast.makeText(MyCollectionDetails.this, "Error retrieving Articles", Toast.LENGTH_LONG).show();
                }
            });
        }
        else {
            String accessToken = preferenceEndPoint.getStringPreference("access_token");
            yoService.getArticlesOfMagazineAPI(topicId, accessToken).enqueue(new Callback<MagazineArticles>() {
                @Override
                public void onResponse(Call<MagazineArticles> call, final Response<MagazineArticles> response) {
                    final String id = response.body().getId();
                    if (response.body().getArticlesList()!= null && response.body().getArticlesList().size() > 0) {
                        for (int i = 0; i < response.body().getArticlesList().size(); i++) {
                            //if (selectedTopic.equalsIgnoreCase(response.body().get(i).getTopicName())) {
                            //articlesList = new ArrayList<Travels.Data>();

                            articlesList.add(response.body().getArticlesList().get(i));
                            // }
                        }
                        myBaseAdapter.addItems(articlesList);
                    }

                }

                @Override
                public void onFailure(Call<MagazineArticles> call, Throwable t) {

                }
            });
        }
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
            if (position>=0 && getCount() > position) {
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

                holder.magazineShare = UI.<ImageView>findViewById(layout, R.id.imv_magazine_share);

                holder.articleFollow = UI.<Button>findViewById(layout, R.id.imv_magazine_follow);

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
                                notifyDataSetChanged();
                            }
                        });
                    } else {
                        String accessToken = preferenceEndPoint.getStringPreference("access_token");
                        yoService.unlikeArticlesAPI(data.getId(), accessToken).enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
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
                Picasso.with(MyCollectionDetails.this)
                        .load(data.getImage_filename())
                        .fit()
                        .into(photoView);
            }
            //}

            Button followMoreTopics = (Button)layout.findViewById(R.id.btn_magazine_follow_topics);
            followMoreTopics.setVisibility(View.GONE);
           /* followMoreTopics.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), FollowMoreTopicsActivity.class);
                    startActivity(intent);
                }
            });*/

            ImageView add = holder.magazineAdd;
            add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(MyCollectionDetails.this, CreateMagazineActivity.class);
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

            /*holder.articleFollow.setEnabled(false);
            holder.articleFollow.setBackgroundColor(context.getResources().getColor(R.color.grey_divider));*/
            holder.articleFollow.setVisibility(View.GONE);

            /*if(data.getIsFollowing().equals("true")) {
                holder.articleFollow.setText("Following");
                holder.articleFollow.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_following_tick, 0, 0, 0);
            }
            else {
                holder.articleFollow.setText("Follow");
                holder.articleFollow.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            }

            final ViewHolder finalHolder = holder;
            holder.articleFollow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((BaseActivity)context).showProgressDialog();
                    String accessToken = preferenceEndPoint.getStringPreference("access_token");
                    yoService.followArticleAPI(data.getId(), accessToken).enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            ((BaseActivity) context).dismissProgressDialog();
                            finalHolder.articleFollow.setText("Following");
                            finalHolder.articleFollow.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_following_tick, 0, 0, 0);
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            ((BaseActivity) context).dismissProgressDialog();
                            finalHolder.articleFollow.setText("Follow");
                            finalHolder.articleFollow.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);

                        }
                    });
                }
            });*/


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

        private Button articleFollow;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_my_collections_detail, menu);
        menu.getItem(0).setTitle("Following");

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        final MenuItem menuItem = item;
        switch (item.getItemId()) {
            case R.id.menu_follow_magazine:
                if (type.equals("Tag")) {
                    final Dialog dialog = new Dialog(MyCollectionDetails.this);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setContentView(R.layout.unfollow_alert_dialog);

                    dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                    Button btnCancel = (Button) dialog.findViewById(R.id.btn_cancel);

                    Button btnUnfollow = (Button) dialog.findViewById(R.id.btn_unfollow);

                    btnCancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });

                    btnUnfollow.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                            String accessToken = preferenceEndPoint.getStringPreference("access_token");
                            List<String> topicIds = new ArrayList<String>();
                            topicIds.add(topicId);
                            yoService.removeTopicsAPI(accessToken, topicIds).enqueue(new Callback<ResponseBody>() {
                                @Override
                                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                    menuItem.setTitle("Follow");
                                    Intent intent = new Intent();
                                    setResult(6, intent);
                                    finish();

                                }

                                @Override
                                public void onFailure(Call<ResponseBody> call, Throwable t) {
                                    menuItem.setTitle("Following");
                                }
                            });
                        }
                    });

                    dialog.show();
                } else {
                    final Dialog dialog = new Dialog(MyCollectionDetails.this);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setContentView(R.layout.unfollow_alert_dialog);

                    dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                    Button btnCancel = (Button) dialog.findViewById(R.id.btn_cancel);

                    Button btnUnfollow = (Button) dialog.findViewById(R.id.btn_unfollow);

                    btnCancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });

                    btnUnfollow.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                            showProgressDialog();
                            String accessToken = preferenceEndPoint.getStringPreference("access_token");
                            yoService.unfollowMagazineAPI(topicId, accessToken).enqueue(new Callback<ResponseBody>() {
                                @Override
                                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                    dismissProgressDialog();
                                    menuItem.setTitle("Follow");
                                    Intent intent = new Intent();
                                    setResult(6, intent);
                                    finish();
                                }

                                @Override
                                public void onFailure(Call<ResponseBody> call, Throwable t) {
                                    dismissProgressDialog();
                                    menuItem.setTitle("Following");
                                    //menuItem.setIcon(R.drawable.ic_magazine_following);

                                }
                            });
                        }
                    });

                    dialog.show();
                }

        }
        return true;
    }
}
