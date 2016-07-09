package com.yo.android.flip;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.aphidmobile.flip.FlipViewController;
import com.aphidmobile.utils.AphidLog;
import com.aphidmobile.utils.IO;
import com.aphidmobile.utils.UI;
import com.yo.android.R;
import com.yo.android.api.YoApi;
import com.yo.android.chat.ui.fragments.BaseFragment;
import com.yo.android.model.Articles;
import com.yo.android.ui.FollowMoreTopics;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.inject.Inject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by creatives on 6/30/2016.
 */
public class MagazineFlipArticlesFragment extends BaseFragment {

    private FlipViewController flipView;
    private static MagazineTopicsSelectionFragment magazineTopicsSelectionFragment;
    //private static List<Travels.Data> articlesList = new ArrayList<Travels.Data>();
    private List<Articles> articlesList = new ArrayList<Articles>();
    private MyReceiver myReceiver;
    private MyBaseAdapter myBaseAdapter;
    @Inject
    YoApi.YoService yoService;
    private String topicName;

    @SuppressLint("ValidFragment")
    public MagazineFlipArticlesFragment(MagazineTopicsSelectionFragment fragment) {
        // Required empty public constructor
        magazineTopicsSelectionFragment = fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IntentFilter filter = new IntentFilter("com.yo.magazine.SendBroadcast");
        myReceiver = new MyReceiver();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(myReceiver, filter);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        flipView = new FlipViewController(getActivity());
        myBaseAdapter = new MyBaseAdapter(getActivity(), flipView);
        flipView.setAdapter(myBaseAdapter);
        return flipView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //loadArticles(magazineTopicsSelectionFragment.getSelectedTopic());
    }

    public void loadArticles(String selectedTopic, String topicId) {
        topicName = selectedTopic;
       /* articlesList.clear();
        for (int i = 0; i < Travels.getImgDescriptions().size(); i++) {
            //if (magazineTopicsSelectionFragment.getSelectedTopic().equals(Travels.getImgDescriptions().get(i).getTopicName())) {
            if (selectedTopic.equalsIgnoreCase(Travels.getImgDescriptions().get(i).getTopicName())) {
                //articlesList = new ArrayList<Travels.Data>();
                articlesList.add(Travels.getImgDescriptions().get(i));
            }
        }
        myBaseAdapter.addItems(articlesList);*/

        articlesList.clear();
        String accessToken = preferenceEndPoint.getStringPreference("access_token");
        yoService.getArticlesAPI(accessToken, topicId).enqueue(new Callback<List<Articles>>() {
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
                Toast.makeText(getActivity(), "Error retrieving Articles", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void onDestroyView() {
        super.onDestroyView();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(myReceiver);
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

                holder.categoryName = UI
                        .<TextView>findViewById(layout, R.id.tv_category_name);

                holder.articleTitle = UI.
                        <TextView>findViewById(layout, R.id.tv_article_title);

                holder.articleShortDesc = UI
                        .<TextView>findViewById(layout, R.id.tv_article_short_desc);

                holder.articlePhoto = UI.findViewById(layout, R.id.photo);

                holder.magazineLike = UI.<CheckBox>findViewById(layout, R.id.cb_magazine_like);

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

            holder.categoryName
                    .setText(AphidLog.format("%s", topicName));

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


            WebView photoView = holder.articlePhoto;

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

                photoView.loadUrl(data.getImage_filename());
            }
            //}

            Button followMoreTopics = (Button)layout.findViewById(R.id.btn_magazine_follow_topics);
            followMoreTopics.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), FollowMoreTopics.class);
                    startActivity(intent);
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
        private TextView categoryName;

        private TextView articleTitle;

        private TextView articleShortDesc;

        private WebView articlePhoto;

        private CheckBox magazineLike;
    }

    /**
     * Borrowed from the official BitmapFun tutorial: http://developer.android.com/training/displaying-bitmaps/index.html
     */
    private static final class AsyncDrawable extends BitmapDrawable {

        private final WeakReference<AsyncImageTask> taskRef;

        public AsyncDrawable(Resources res, Bitmap bitmap, AsyncImageTask task) {
            super(res, bitmap);
            this.taskRef = new WeakReference<AsyncImageTask>(task);
        }

        public static AsyncImageTask getTask(ImageView imageView) {
            Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                return ((AsyncDrawable) drawable).taskRef.get();
            }

            return null;
        }
    }

    private static final class AsyncImageTask extends AsyncTask<Void, Void, Bitmap> {

        private static final Random RANDOM = new Random();

        private final AssetManager assetManager;

        private final WeakReference<ImageView> imageViewRef;
        private final WeakReference<FlipViewController> controllerRef;
        private final int pageIndex;
        private final String imageName;

        public AsyncImageTask(AssetManager assetManager, ImageView imageView,
                              FlipViewController controller, int pageIndex, String imageName) {
            this.assetManager = assetManager;
            imageViewRef = new WeakReference<>(imageView);
            controllerRef = new WeakReference<>(controller);
            this.pageIndex = pageIndex;
            this.imageName = imageName;
        }

        public int getPageIndex() {
            return pageIndex;
        }

        public String getImageName() {
            return imageName;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            try {
                //wait for a random time
                Thread.sleep(500 + RANDOM.nextInt(2000));
            } catch (InterruptedException e) {
                Log.e("TAG", "doInBackground", e);
            }

            return IO.readBitmap(assetManager, imageName);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled()) {
                return;
            }

            ImageView imageView = imageViewRef.get();
            //the imageView can be reused for another page, so it's necessary to check its consistence
            if (imageView != null && AsyncDrawable.getTask(imageView) == this) {
                imageView.setImageBitmap(bitmap);
                FlipViewController controller = controllerRef.get();
                if (controller != null) {
                    controller.refreshPage(pageIndex);
                }
            }
        }
    }

    public class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String selectedTopic = intent.getStringExtra("SelectedTopic");
            String topicId = intent.getStringExtra("TopicId");
            loadArticles(selectedTopic, topicId);
        }
    }


}
