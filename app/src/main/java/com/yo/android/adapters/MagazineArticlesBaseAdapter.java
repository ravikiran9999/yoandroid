package com.yo.android.adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Layout;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aphidmobile.utils.AphidLog;
import com.aphidmobile.utils.UI;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.orion.android.common.preferences.PreferenceEndPoint;
import com.orion.android.common.util.ToastFactory;
import com.yo.android.BuildConfig;
import com.yo.android.R;
import com.yo.android.api.ApiCallback;
import com.yo.android.api.YoApi;
import com.yo.android.flip.MagazineArticleDetailsActivity;
import com.yo.android.flip.MagazineFlipArticlesFragment;
import com.yo.android.helpers.MagazinePreferenceEndPoint;
import com.yo.android.model.Articles;
import com.yo.android.model.Categories;
import com.yo.android.model.Topics;
import com.yo.android.ui.BaseActivity;
import com.yo.android.ui.BitmapScaler;
import com.yo.android.ui.CreateMagazineActivity;
import com.yo.android.ui.DeviceDimensionsHelper;
import com.yo.android.ui.FollowMoreTopicsActivity;
import com.yo.android.ui.NewFollowMoreTopicsActivity;
import com.yo.android.ui.OtherProfilesLikedArticles;
import com.yo.android.ui.TopicsDetailActivity;
import com.yo.android.ui.fragments.MagazinesFragment;
import com.yo.android.usecase.AddTopicsUsecase;
import com.yo.android.util.AutoReflectTopicsFollowActionsListener;
import com.yo.android.util.AutoReflectWishListActionsListener;
import com.yo.android.util.Constants;
import com.yo.android.util.MagazineOtherPeopleReflectListener;
import com.yo.android.util.Util;
import com.yo.android.video.InAppVideoActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;

import javax.inject.Inject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * The adapter for the Magazine landing screen articles
 */
public class MagazineArticlesBaseAdapter extends BaseAdapter implements AutoReflectWishListActionsListener, MagazineOtherPeopleReflectListener, AutoReflectTopicsFollowActionsListener, NewSuggestionsAdapter.TopicSelectionListener {

    private Context context;
    private LayoutInflater inflater;
    public static AutoReflectWishListActionsListener reflectListener;
    public static MagazineOtherPeopleReflectListener mListener;
    public static AutoReflectTopicsFollowActionsListener reflectTopicsFollowActionsListener;


    private List<Articles> items;
    PreferenceEndPoint preferenceEndPoint;
    YoApi.YoService yoService;
    ToastFactory mToastFactory;
    private List<Articles> totalItems;
    public Articles secondArticle;
    public Articles thirdArticle;
    private List<Articles> allArticles;
    private MagazineFlipArticlesFragment magazineFlipArticlesFragment;
    private List<Articles> getAllArticles;
    private NewSuggestionsAdapter newSuggestionsAdapter;
    AddTopicsUsecase mAddTopicsUsecase;

    private static AutoReflectWishListActionsListener reflectListenerTemp;


    public MagazineArticlesBaseAdapter(Context context,
                                       PreferenceEndPoint preferenceEndPoint,
                                       YoApi.YoService yoService, ToastFactory mToastFactory, MagazineFlipArticlesFragment magazineFlipArticlesFragment, AddTopicsUsecase addTopicsUsecase) {
        inflater = LayoutInflater.from(context);
        this.context = context;
        mListener = this;
        this.preferenceEndPoint = preferenceEndPoint;
        this.yoService = yoService;
        this.mToastFactory = mToastFactory;
        items = new ArrayList<>();
        totalItems = new ArrayList<>();
        allArticles = new ArrayList<>();
        getAllArticles = new ArrayList<>();
        this.magazineFlipArticlesFragment = magazineFlipArticlesFragment;
        reflectTopicsFollowActionsListener = this;
        reflectListenerTemp = this;
        mAddTopicsUsecase = addTopicsUsecase;
    }

    public static void initListener() {
        reflectListener = reflectListenerTemp;
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

        int type = getItemViewType(position);
        if (layout == null) {
            if (type == 0) {
                // Inflate the layout with multiple articles
                layout = inflater.inflate(R.layout.magazine_landing_layout, null);
            } else if (type == 2) {
                // Inflate the layout with suggestions page
                if (!BuildConfig.NEW_FOLLOW_MORE_TOPICS) {
                    layout = inflater.inflate(R.layout.landing_suggestions_page, null);
                } else {
                    layout = inflater.inflate(R.layout.new_landing_suggestions_page, null);
                }
            } else {
                // Inflate the layout with single article
                layout = inflater.inflate(R.layout.magazine_flip_layout, null);
            }

            holder = new ViewHolder();

            holder.topLayout = UI.
                    findViewById(layout, R.id.rl_left);

            holder.middleLayout = UI.
                    findViewById(layout, R.id.ll_bottom_layout);
            holder.bottomLayout = UI.
                    findViewById(layout, R.id.ll_article_info);

            holder.articleTitle = UI.
                    findViewById(layout, R.id.tv_article_title);

            holder.articleShortDesc = UI
                    .findViewById(layout, R.id.tv_article_short_desc);

            holder.articlePhoto = UI.findViewById(layout, R.id.photo);

            holder.magazineLike = UI.findViewById(layout, R.id.cb_magazine_like);

            holder.magazineAdd = UI.findViewById(layout, R.id.imv_magazine_add);

            holder.magazineShare = UI.findViewById(layout, R.id.imv_magazine_share);

            holder.articleFollow = UI.findViewById(layout, R.id.imv_magazine_follow);

            holder.articleTitleTop = UI.
                    findViewById(layout, R.id.tv_article_title_top);

            holder.articlePhotoTop = UI.findViewById(layout, R.id.photo_top);

            holder.magazineLikeTop = UI.findViewById(layout, R.id.cb_magazine_like_top);

            holder.magazineAddTop = UI.findViewById(layout, R.id.imv_magazine_add_top);

            holder.magazineShareTop = UI.findViewById(layout, R.id.imv_magazine_share_top);

            holder.articleFollowTop = UI.findViewById(layout, R.id.imv_magazine_follow_top);

            holder.articleTitleLeft = UI.
                    findViewById(layout, R.id.tv_article_title_left);

            holder.articlePhotoLeft = UI.findViewById(layout, R.id.photo_left);

            holder.magazineLikeLeft = UI.findViewById(layout, R.id.cb_magazine_like_left);

            holder.magazineAddLeft = UI.findViewById(layout, R.id.imv_magazine_add_left);

            holder.magazineShareLeft = UI.findViewById(layout, R.id.imv_magazine_share_left);

            holder.articleFollowLeft = UI.findViewById(layout, R.id.imv_magazine_follow_left);

            holder.articleTitleRight = UI.
                    findViewById(layout, R.id.tv_article_title_right);

            holder.articlePhotoRight = UI.findViewById(layout, R.id.photo_right);

            holder.magazineLikeRight = UI.findViewById(layout, R.id.cb_magazine_like_right);

            holder.magazineAddRight = UI.findViewById(layout, R.id.imv_magazine_add_right);

            holder.magazineShareRight = UI.findViewById(layout, R.id.imv_magazine_share_right);

            holder.articleFollowRight = UI.findViewById(layout, R.id.imv_magazine_follow_right);

            //holder.lvSuggestions = UI.findViewById(layout, R.id.lv_suggestions);
            if (!BuildConfig.NEW_FOLLOW_MORE_TOPICS) {
                holder.lvSuggestions = (ListView) layout.findViewById(R.id.lv_suggestions);
            } else {
                holder.rvSuggestions = (RecyclerView) layout.findViewById(R.id.rv_suggestions);
                holder.doneButton = (Button) layout.findViewById(R.id.btn_done);
                holder.noSuggestionsTextView = (TextView) layout.findViewById(R.id.no_suggestions);
            }
            holder.tvFollowMoreTopics = UI.findViewById(layout, R.id.tv_follow_more_topics);

            holder.tvTopicName = UI.findViewById(layout, R.id.imv_magazine_topic);

            holder.tvTopicNameTop = UI.findViewById(layout, R.id.imv_magazine_topic_top);

            holder.tvTopicNameLeft = UI.findViewById(layout, R.id.imv_magazine_topic_left);

            holder.tvTopicNameRight = UI.findViewById(layout, R.id.imv_magazine_topic_right);

            holder.articleSummaryLeft = UI.findViewById(layout, R.id.tv_article_short_desc_summary);

            holder.articleSummaryRight = UI.findViewById(layout, R.id.tv_article_short_desc_summary_right);

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

        if (holder.magazineLike != null) {
            holder.magazineLike.setTag(position);
        }

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

/*                ViewTreeObserver vto = holder.articleShortDesc.getViewTreeObserver();
                textView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                    @Override
                    public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                               int oldLeft, int oldTop, int oldRight, int oldBottom) {
                        textView.removeOnLayoutChangeListener(this);
                        float lineHeight = textView.getLineHeight();
                        int maxLines = (int) (textView.getHeight() / lineHeight);
                        if (textView.getLineCount() != maxLines) {
                            textView.setLines(maxLines);
                            textView.setEllipsize(TextUtils.TruncateAt.END);
                            // Re-assign text to ensure ellipsize is performed correctly.
                            textView.setText(Html.fromHtml(data.getSummary()));
                        }
                    }
                });*/
                /*vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        Log.d("BaseAdapter", "The short desc line count is " + shortDesc.getLineCount());
                        //calculateHeight(shortDesc);
                        *//*final Layout layout = shortDesc.getLayout();
                        String contentToBeWrite = "";
                        int start = 0;

                        int height = shortDesc.getHeight();
                        int scrollY = shortDesc.getScrollY();
                        int firstVisibleLineNumber = layout.getLineForVertical(scrollY);
                        int lastVisibleLineNumber = layout.getLineForVertical(scrollY + height);

                        String content = shortDesc.getText().toString();
                        Log.d("BaseAdapter", "lastVisibleLineNumber " + lastVisibleLineNumber);
                        for (int i = 0; i < lastVisibleLineNumber-1; i++) {
                            int end = layout.getLineEnd(i);

                            contentToBeWrite = contentToBeWrite + content.substring(start, end);
                            start = end + 1;
                            Log.d("BaseAdapter", "contentToBeWrite " + contentToBeWrite);
                        }
                        shortDesc.setText(contentToBeWrite);*//*

                        //shortDesc.setMaxLines(lastVisibleLineNumber);

                        *//*if(lastVisibleLineNumber != 0 && shortDesc.getLineCount()>lastVisibleLineNumber){
                            shortDesc.setLines(lastVisibleLineNumber);
                        }*//*
                    }
                });*/
                //doEllipsize(holder.articleShortDesc);
            }
        }

        if (holder.magazineLike != null) {
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
                    Log.d("MagazineBaseAdapter", "Title and liked... " + data.getTitle() + " " + Boolean.valueOf(data.getLiked()));

                    if (isChecked) {
                        ((BaseActivity) context).showProgressDialog();
                        String accessToken = preferenceEndPoint.getStringPreference("access_token");
                        yoService.likeArticlesAPI(data.getId(), accessToken).enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                                ((BaseActivity) context).dismissProgressDialog();
                                data.setIsChecked(true);
                                data.setLiked("true");
                                if (!((BaseActivity) context).hasDestroyed()) {
                                    notifyDataSetChanged();
                                }
                                if (OtherProfilesLikedArticles.getListener() != null) {
                                    OtherProfilesLikedArticles.getListener().updateOtherPeopleStatus(data, Constants.LIKE_EVENT);
                                }
                                mToastFactory.showToast("You have liked the article " + data.getTitle());

                                List<Articles> cachedMagazinesList = getCachedMagazinesList();
                                if (cachedMagazinesList != null) {
                                    List<Articles> tempList = cachedMagazinesList;
                                    for (int i = 0; i < cachedMagazinesList.size(); i++) {
                                        if (data.getId().equals(tempList.get(i).getId())) {
                                            tempList.get(i).setLiked("true");
                                        }
                                    }

                                    cachedMagazinesList = tempList;

                                    saveCachedMagazinesList(cachedMagazinesList);
                                }

                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                ((BaseActivity) context).dismissProgressDialog();
                                mToastFactory.showToast("Error while liking article " + data.getTitle());
                                data.setIsChecked(false);
                                data.setLiked("false");
                                if (!((BaseActivity) context).hasDestroyed()) {
                                    notifyDataSetChanged();
                                }

                                List<Articles> cachedMagazinesList = getCachedMagazinesList();
                                if (cachedMagazinesList != null) {
                                    List<Articles> tempList = cachedMagazinesList;
                                    for (int i = 0; i < cachedMagazinesList.size(); i++) {
                                        if (data.getId().equals(tempList.get(i).getId())) {
                                            tempList.get(i).setLiked("false");
                                        }
                                    }

                                    cachedMagazinesList = tempList;

                                    saveCachedMagazinesList(cachedMagazinesList);
                                }
                            }
                        });
                    } else {
                        ((BaseActivity) context).showProgressDialog();
                        String accessToken = preferenceEndPoint.getStringPreference("access_token");
                        yoService.unlikeArticlesAPI(data.getId(), accessToken).enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                ((BaseActivity) context).dismissProgressDialog();
                                data.setIsChecked(false);
                                data.setLiked("false");
                                if (OtherProfilesLikedArticles.getListener() != null) {
                                    OtherProfilesLikedArticles.getListener().updateOtherPeopleStatus(data, Constants.LIKE_EVENT);
                                }
                                if (!((BaseActivity) context).hasDestroyed()) {
                                    notifyDataSetChanged();
                                }

                                mToastFactory.showToast("You have un-liked the article " + data.getTitle());

                                List<Articles> cachedMagazinesList = getCachedMagazinesList();
                                if (cachedMagazinesList != null) {
                                    List<Articles> tempList = cachedMagazinesList;
                                    for (int i = 0; i < cachedMagazinesList.size(); i++) {
                                        if (data.getId().equals(tempList.get(i).getId())) {
                                            tempList.get(i).setLiked("false");
                                        }
                                    }

                                    cachedMagazinesList = tempList;

                                    saveCachedMagazinesList(cachedMagazinesList);
                                }

                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                ((BaseActivity) context).dismissProgressDialog();
                                Toast.makeText(context, "Error while un liking article " + data.getTitle(), Toast.LENGTH_LONG).show();
                                data.setIsChecked(true);
                                data.setLiked("true");
                                if (!((BaseActivity) context).hasDestroyed()) {
                                    notifyDataSetChanged();
                                }

                                List<Articles> cachedMagazinesList = getCachedMagazinesList();
                                if (cachedMagazinesList != null) {
                                    List<Articles> tempList = cachedMagazinesList;
                                    for (int i = 0; i < cachedMagazinesList.size(); i++) {
                                        if (data.getId().equals(tempList.get(i).getId())) {
                                            tempList.get(i).setLiked("true");
                                        }
                                    }

                                    cachedMagazinesList = tempList;

                                    saveCachedMagazinesList(cachedMagazinesList);
                                }
                            }
                        });
                    }
                }
            });
        }

        if (holder.fullImageMagazineLike != null) {
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
                    Log.d("MagazineBaseAdapter", "Title and liked... " + data.getTitle() + " " + Boolean.valueOf(data.getLiked()));

                    if (isChecked) {
                        ((BaseActivity) context).showProgressDialog();
                        String accessToken = preferenceEndPoint.getStringPreference("access_token");
                        yoService.likeArticlesAPI(data.getId(), accessToken).enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                                ((BaseActivity) context).dismissProgressDialog();
                                data.setIsChecked(true);
                                data.setLiked("true");
                                if (!((BaseActivity) context).hasDestroyed()) {
                                    notifyDataSetChanged();
                                }
                                if (OtherProfilesLikedArticles.getListener() != null) {
                                    OtherProfilesLikedArticles.getListener().updateOtherPeopleStatus(data, Constants.LIKE_EVENT);
                                }
                                mToastFactory.showToast("You have liked the article " + data.getTitle());

                                List<Articles> cachedMagazinesList = getCachedMagazinesList();
                                if (cachedMagazinesList != null) {
                                    List<Articles> tempList = cachedMagazinesList;
                                    for (int i = 0; i < cachedMagazinesList.size(); i++) {
                                        if (data.getId().equals(tempList.get(i).getId())) {
                                            tempList.get(i).setLiked("true");
                                        }
                                    }

                                    cachedMagazinesList = tempList;

                                    saveCachedMagazinesList(cachedMagazinesList);
                                }

                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                ((BaseActivity) context).dismissProgressDialog();
                                mToastFactory.showToast("Error while liking article " + data.getTitle());
                                data.setIsChecked(false);
                                data.setLiked("false");
                                if (!((BaseActivity) context).hasDestroyed()) {
                                    notifyDataSetChanged();
                                }

                                List<Articles> cachedMagazinesList = getCachedMagazinesList();
                                if (cachedMagazinesList != null) {
                                    List<Articles> tempList = cachedMagazinesList;
                                    for (int i = 0; i < cachedMagazinesList.size(); i++) {
                                        if (data.getId().equals(tempList.get(i).getId())) {
                                            tempList.get(i).setLiked("false");
                                        }
                                    }

                                    cachedMagazinesList = tempList;

                                    saveCachedMagazinesList(cachedMagazinesList);
                                }
                            }
                        });
                    } else {
                        ((BaseActivity) context).showProgressDialog();
                        String accessToken = preferenceEndPoint.getStringPreference("access_token");
                        yoService.unlikeArticlesAPI(data.getId(), accessToken).enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                ((BaseActivity) context).dismissProgressDialog();
                                data.setIsChecked(false);
                                data.setLiked("false");
                                if (OtherProfilesLikedArticles.getListener() != null) {
                                    OtherProfilesLikedArticles.getListener().updateOtherPeopleStatus(data, Constants.LIKE_EVENT);
                                }
                                if (!((BaseActivity) context).hasDestroyed()) {
                                    notifyDataSetChanged();
                                }

                                mToastFactory.showToast("You have un-liked the article " + data.getTitle());

                                List<Articles> cachedMagazinesList = getCachedMagazinesList();
                                if (cachedMagazinesList != null) {
                                    List<Articles> tempList = cachedMagazinesList;
                                    for (int i = 0; i < cachedMagazinesList.size(); i++) {
                                        if (data.getId().equals(tempList.get(i).getId())) {
                                            tempList.get(i).setLiked("false");
                                        }
                                    }

                                    cachedMagazinesList = tempList;

                                    saveCachedMagazinesList(cachedMagazinesList);
                                }

                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                ((BaseActivity) context).dismissProgressDialog();
                                Toast.makeText(context, "Error while un liking article " + data.getTitle(), Toast.LENGTH_LONG).show();
                                data.setIsChecked(true);
                                data.setLiked("true");
                                if (!((BaseActivity) context).hasDestroyed()) {
                                    notifyDataSetChanged();
                                }

                                List<Articles> cachedMagazinesList = getCachedMagazinesList();
                                if (cachedMagazinesList != null) {
                                    List<Articles> tempList = cachedMagazinesList;
                                    for (int i = 0; i < cachedMagazinesList.size(); i++) {
                                        if (data.getId().equals(tempList.get(i).getId())) {
                                            tempList.get(i).setLiked("true");
                                        }
                                    }

                                    cachedMagazinesList = tempList;

                                    saveCachedMagazinesList(cachedMagazinesList);
                                }
                            }
                        });
                    }
                }
            });
        }

        if (UI
                .<TextView>findViewById(layout, R.id.tv_category_full_story)
                != null) {
            UI
                    .<TextView>findViewById(layout, R.id.tv_category_full_story)
                    .setText(AphidLog.format("%s", data.getTitle()));
        }

        if (UI
                .<TextView>findViewById(layout, R.id.tv_category_full_story) != null) {
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
                            magazineFlipArticlesFragment.startActivityForResult(intent, 500);
                        }
                    });
        }


        if (holder.articlePhoto != null) {
            final ImageView photoView = holder.articlePhoto;

            photoView.setImageResource(R.drawable.magazine_backdrop);
            if (data.getImage_filename() != null) {
                if (!((BaseActivity) context).hasDestroyed()) {
                    //new NewImageRenderTask(context, data.getImage_filename(), photoView).execute();
                    final TextView fullImageTitle = holder.fullImageTitle;
                    final TextView articleTitle = holder.articleTitle;
                    final ImageView blackMask = holder.blackMask;
                    final RelativeLayout rlFullImageOptions = holder.rlFullImageOptions;
                    final TextView textView = holder.articleShortDesc;
                    /*if("597695a01645e9120c620243".equals(data.getId())) {
                        data.setImage_filename("http://yowatsup.com/newfilename.jpg");
                    }*/

                    Glide.with(context)
                            .load(data.getImage_filename())
                            .asBitmap()
                            .placeholder(R.drawable.magazine_backdrop)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .dontAnimate()
                            .into(new SimpleTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                    int screenWidth = DeviceDimensionsHelper.getDisplayWidth(context);
                                    if (resource != null) {
                                        Bitmap bmp = BitmapScaler.scaleToFitWidth(resource, screenWidth);
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
                                       /*int spaceForImage = screenHeight - 120;
                                       Log.d("BaseAdapter", "spaceForImage" + spaceForImage);*/
                                        //Log.d("BaseAdapter", "bmp.getHeight()" + bmp.getHeight());
                                        int total = bmp.getHeight() + 120;
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
                                        }

                                        if (articleTitle != null) {
                                            ViewTreeObserver vto1 = articleTitle.getViewTreeObserver();
                                            vto1.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                                                private int maxLines = -1;

                                                @Override
                                                public void onGlobalLayout() {
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
                                        }

                                        if (textView != null) {
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
                                }
                            });
                }
            } else {
                photoView.setImageResource(R.drawable.magazine_backdrop);
            }

            photoView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String videoUrl = data.getVideo_url();
                    if (videoUrl != null && !TextUtils.isEmpty(videoUrl)) {
                        InAppVideoActivity.start((Activity) context, videoUrl, data.getTitle());
                    } else {
                        Intent intent = new Intent(context, MagazineArticleDetailsActivity.class);
                        intent.putExtra("Title", data.getTitle());
                        intent.putExtra("Image", data.getUrl());
                        intent.putExtra("Article", data);
                        intent.putExtra("Position", position);
                        magazineFlipArticlesFragment.startActivityForResult(intent, 500);
                    }
                }
            });
        }

        Button followMoreTopics = (Button) layout.findViewById(R.id.btn_magazine_follow_topics);
        if (followMoreTopics != null) {
            followMoreTopics.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent;
                    if (!BuildConfig.NEW_FOLLOW_MORE_TOPICS) {
                        intent = new Intent(context, FollowMoreTopicsActivity.class);
                    } else {
                        intent = new Intent(context, NewFollowMoreTopicsActivity.class);
                    }
                    intent.putExtra("From", "Magazines");
                    context.startActivity(intent);
                }
            });
        }

        if (holder.magazineAdd != null) {
            ImageView add = holder.magazineAdd;
            add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, CreateMagazineActivity.class);
                    intent.putExtra(Constants.MAGAZINE_ADD_ARTICLE_ID, data.getId());
                    if (context instanceof Activity) {
                        ((Activity) context).startActivityForResult(intent, Constants.ADD_ARTICLES_TO_MAGAZINE);
                    }
                }
            });
        }

        if (holder.magazineShare != null) {
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
        }

        if (holder.fullImageMagazineAdd != null) {
            ImageView add = holder.fullImageMagazineAdd;
            add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, CreateMagazineActivity.class);
                    intent.putExtra(Constants.MAGAZINE_ADD_ARTICLE_ID, data.getId());
                    if (context instanceof Activity) {
                        ((Activity) context).startActivityForResult(intent, Constants.ADD_ARTICLES_TO_MAGAZINE);
                    }
                }
            });
        }

        if (holder.fullImageMagazineShare != null) {
            ImageView share = holder.fullImageMagazineShare;
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
        }

        if (holder.articleFollow != null) {
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
                    followArticle(data, finalHolder, finalHolder.articleFollow);
                }
            });
        }

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
                    magazineFlipArticlesFragment.startActivityForResult(intent, 500);
                }
            });
        }

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
                        magazineFlipArticlesFragment.startActivityForResult(intent, 60);
                    }
                });
            } else {
                holder.tvTopicName.setVisibility(View.GONE);
            }
        }

        if (allArticles.size() >= 1) {
            Articles firstData = getItem(0);
            populateTopArticle(layout, holder, firstData, position);
        }

        if (allArticles.size() >= 2) {
            Articles secondData = secondArticle;
            populateLeftArticle(holder, secondData, position);
        } else {
            populateEmptyLeftArticle(holder);
        }

        if (allArticles.size() >= 3) {
            Articles thirdData = thirdArticle;
            populateRightArticle(holder, thirdData, position);
        } else {
            populateEmptyRightArticle(holder);
        }
        if (!BuildConfig.NEW_FOLLOW_MORE_TOPICS) {
            if (allArticles.size() >= 4 && MagazinesFragment.unSelectedTopics.size() > 0) {
                if (holder.lvSuggestions != null) {
                    SuggestionsAdapter suggestionsAdapter = new SuggestionsAdapter(context, magazineFlipArticlesFragment);
                    holder.lvSuggestions.setAdapter(suggestionsAdapter);
                    int n = 5;
                    if (MagazinesFragment.unSelectedTopics.size() >= n) {
                        List<Topics> subList = new ArrayList<>(MagazinesFragment.unSelectedTopics.subList(0, n));
                        suggestionsAdapter.addItems(subList);
                    } else {
                        int count = MagazinesFragment.unSelectedTopics.size();
                        if (count > 0) {
                            List<Topics> subList = new ArrayList<>(MagazinesFragment.unSelectedTopics.subList(0, count));
                            suggestionsAdapter.addItems(subList);
                        }
                    }
                }
            }
        } else {

            if (allArticles.size() >= 4 && MagazinesFragment.newCategoriesList.size() > 0) {
                if (holder.rvSuggestions != null) {
                    ArrayList<Categories> addFourCategoriesList = new ArrayList<>();
                    for (Categories categories : MagazinesFragment.newCategoriesList) {
                        if (addFourCategoriesList.size() <= 4 && categories.getTags() != null && categories.getTags().size() > 0) {
                            if (!categories.getTags().get(0).isSelected()) {
                                addFourCategoriesList.add(categories);
                            }
                        }
                    }

                    if (addFourCategoriesList != null && addFourCategoriesList.size() > 0) {

                        newSuggestionsAdapter = new NewSuggestionsAdapter(context, magazineFlipArticlesFragment, addFourCategoriesList);
                        newSuggestionsAdapter.notifyDataSetChanged();
                        newSuggestionsAdapter.setTopicsItemListener(this);
                        holder.rvSuggestions.setAdapter(newSuggestionsAdapter);
                        holder.rvSuggestions.setNestedScrollingEnabled(false);
                        holder.rvSuggestions.setLayoutManager(new GridLayoutManager(context, 2));
                        holder.rvSuggestions.setVisibility(View.VISIBLE);
                        holder.noSuggestionsTextView.setVisibility(View.GONE);
                    } else {
                        holder.noSuggestionsTextView.setVisibility(View.VISIBLE);
                        holder.noSuggestionsTextView.setText(context.getString(R.string.no_topics_available));
                    }

                }
            }
        }
        if (holder.tvFollowMoreTopics != null) {
            holder.tvFollowMoreTopics.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent;
                    if (!BuildConfig.NEW_FOLLOW_MORE_TOPICS) {
                        intent = new Intent(context, FollowMoreTopicsActivity.class);
                    } else {
                        intent = new Intent(context, NewFollowMoreTopicsActivity.class);
                    }
                    intent.putExtra("From", "Magazines");
                    context.startActivity(intent);
                }
            });
        }
        final RecyclerView mRVSuggestions = holder.rvSuggestions;
        final TextView noSuggestions = holder.noSuggestionsTextView;
        if (holder.doneButton != null) {
            holder.doneButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedTopics(mRVSuggestions, noSuggestions);
                }
            });
        }
        return layout;
    }

    private void calculateHeight(TextView shortDesc) {
        int height = shortDesc.getHeight();
        int scrollY = shortDesc.getScrollY();
        Layout layout = shortDesc.getLayout();

        int firstVisibleLineNumber = layout.getLineForVertical(scrollY);
        int lastVisibleLineNumber = layout.getLineForVertical(scrollY + height);
        Log.d("BaseAdapter", "The lastVisibleLineNumber is " + lastVisibleLineNumber);

        /*int start = shortDesc.getLayout().getLineStart(lastVisibleLineNumber - 1); //start position
        int end = shortDesc.getLayout().getLineEnd(lastVisibleLineNumber - 1); //last visible position*/

        String displayedText = shortDesc.getText().toString().substring(0, layout.getLineEnd(lastVisibleLineNumber - 1));
        Log.d("BaseAdapter", "The displayedText is " + displayedText);
        shortDesc.setText(displayedText + "..." + "\n");
    }

    /**
     * Used to follow an article
     *
     * @param data        The Articles object
     * @param finalHolder The view holder object
     * @param follow      The follow button
     */
    private void followArticle(final Articles data, final ViewHolder finalHolder, final Button follow) {
        if (!"true".equals(data.getIsFollowing())) {
            ((BaseActivity) context).showProgressDialog();
            String accessToken = preferenceEndPoint.getStringPreference("access_token");
            yoService.followArticleAPI(data.getId(), accessToken).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    ((BaseActivity) context).dismissProgressDialog();
                    follow.setText("Following");
                    follow.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_following_tick, 0, 0, 0);
                    data.setIsFollowing("true");
                    if (OtherProfilesLikedArticles.getListener() != null) {
                        OtherProfilesLikedArticles.getListener().updateOtherPeopleStatus(data, Constants.FOLLOW_EVENT);
                    }
                    if (!((BaseActivity) context).hasDestroyed()) {
                        notifyDataSetChanged();
                    }
                    Type type = new TypeToken<List<Articles>>() {
                    }.getType();
                    String cachedMagazines = preferenceEndPoint.getStringPreference("cached_magazines", null);
                    List<Articles> cachedMagazinesList = new Gson().fromJson(cachedMagazines, type);
                    if (cachedMagazinesList != null) {
                        for (int i = 0; i < cachedMagazinesList.size(); i++) {
                            if (data.getId().equals(cachedMagazinesList.get(i).getId())) {
                                cachedMagazinesList.get(i).setIsFollowing("true");
                            }
                        }

                        preferenceEndPoint.saveStringPreference("cached_magazines", new Gson().toJson(cachedMagazinesList));
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    ((BaseActivity) context).dismissProgressDialog();
                    follow.setText("Follow");
                    follow.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                    data.setIsFollowing("false");
                    if (!((BaseActivity) context).hasDestroyed()) {
                        notifyDataSetChanged();
                    }
                    Type type = new TypeToken<List<Articles>>() {
                    }.getType();
                    String cachedMagazines = preferenceEndPoint.getStringPreference("cached_magazines", null);
                    List<Articles> cachedMagazinesList = new Gson().fromJson(cachedMagazines, type);
                    if (cachedMagazinesList != null) {
                        for (int i = 0; i < cachedMagazinesList.size(); i++) {
                            if (data.getId().equals(cachedMagazinesList.get(i).getId())) {
                                cachedMagazinesList.get(i).setIsFollowing("false");
                            }
                        }

                        preferenceEndPoint.saveStringPreference("cached_magazines", new Gson().toJson(cachedMagazinesList));
                    }

                }
            });
        } else {
            showUnFollowConfirmationDialog(data, finalHolder, follow);
        }
    }

    /**
     * Shows unfollow confirmation dialog
     *
     * @param data        The articles object
     * @param finalHolder The view holder object
     * @param follow      The follow button
     */
    private void showUnFollowConfirmationDialog(final Articles data, final ViewHolder finalHolder, final Button follow) {


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
                                                                                            follow.setText("Follow");
                                                                                            follow.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                                                                                            data.setIsFollowing("false");
                                                                                            if (OtherProfilesLikedArticles.getListener() != null) {
                                                                                                OtherProfilesLikedArticles.getListener().updateOtherPeopleStatus(data, Constants.FOLLOW_EVENT);
                                                                                            }
                                                                                            if (!((BaseActivity) context).hasDestroyed()) {
                                                                                                notifyDataSetChanged();
                                                                                            }
                                                                                            Type type = new TypeToken<List<Articles>>() {
                                                                                            }.getType();
                                                                                            String cachedMagazines = preferenceEndPoint.getStringPreference("cached_magazines", null);
                                                                                            List<Articles> cachedMagazinesList = new Gson().fromJson(cachedMagazines, type);
                                                                                            if (cachedMagazinesList != null) {
                                                                                                for (int i = 0; i < cachedMagazinesList.size(); i++) {
                                                                                                    if (data.getId().equals(cachedMagazinesList.get(i).getId())) {
                                                                                                        cachedMagazinesList.get(i).setIsFollowing("false");
                                                                                                    }
                                                                                                }

                                                                                                preferenceEndPoint.saveStringPreference("cached_magazines", new Gson().toJson(cachedMagazinesList));
                                                                                            }
                                                                                        }

                                                                                        @Override
                                                                                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                                                                                            ((BaseActivity) context).dismissProgressDialog();
                                                                                            follow.setText("Following");
                                                                                            follow.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_following_tick, 0, 0, 0);
                                                                                            data.setIsFollowing("true");
                                                                                            if (!((BaseActivity) context).hasDestroyed()) {
                                                                                                notifyDataSetChanged();
                                                                                            }
                                                                                            Type type = new TypeToken<List<Articles>>() {
                                                                                            }.getType();
                                                                                            String cachedMagazines = preferenceEndPoint.getStringPreference("cached_magazines", null);
                                                                                            List<Articles> cachedMagazinesList = new Gson().fromJson(cachedMagazines, type);
                                                                                            if (cachedMagazinesList != null) {
                                                                                                for (int i = 0; i < cachedMagazinesList.size(); i++) {
                                                                                                    if (data.getId().equals(cachedMagazinesList.get(i).getId())) {
                                                                                                        cachedMagazinesList.get(i).setIsFollowing("true");
                                                                                                    }
                                                                                                }

                                                                                                preferenceEndPoint.saveStringPreference("cached_magazines", new Gson().toJson(cachedMagazinesList));
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
     * Adds the items to the list
     *
     * @param articlesList The list of articles
     */
    public void addItems(List<Articles> articlesList) {
        allArticles = new ArrayList<>(articlesList);
        totalItems = new ArrayList<>(articlesList);
        if (!magazineFlipArticlesFragment.isSearch) {
            if (totalItems.size() > 1) {
                secondArticle = totalItems.get(1);
            }
            if (totalItems.size() > 2) {
                thirdArticle = totalItems.get(2);
            }
            if (totalItems.size() > 1) {
                totalItems.remove(1);
            }
            if (totalItems.size() > 1) {
                totalItems.remove(1);
            }
        }
        items = new ArrayList<>(totalItems);
        if (!((BaseActivity) context).hasDestroyed()) {
            notifyDataSetChanged();
        }
    }

    /**
     * Adds all the items to the list
     *
     * @param list The articles list
     */
    public void addItemsAll(List<Articles> list) {
        items.addAll(list);
        if (!((BaseActivity) context).hasDestroyed()) {
            notifyDataSetChanged();
        }
    }

    /**
     * Clears the articles list
     */
    public void clear() {
        items.clear();
        if (!((BaseActivity) context).hasDestroyed()) {
            notifyDataSetChanged();
        }
    }

    @Override
    public void updateFollowOrLikesStatus(Articles data, String type) {
        autoReflectStatus(data, type);
    }

    /**
     * Updates the Follow and Like status of the articles
     *
     * @param data The articles object
     * @param type Whether it is Follow or Like
     */
    private void autoReflectStatus(Articles data, String type) {
        if (data != null) {

            if (Constants.FOLLOW_EVENT.equals(type)) {
                for (Articles article : allArticles) {
                    if (data.getId() != null && data.getId().equals(article.getId())) {
                        article.setIsFollowing(data.getIsFollowing());
                        article.setIsFollow(data.isFollow());
                        if (!((BaseActivity) context).hasDestroyed()) {
                            notifyDataSetChanged();
                        }
                        break;
                    }
                }
            } else {
                allArticles = getAllItems();
                for (Articles article : allArticles) {
                    if (data.getId() != null && data.getId().equals(article.getId())) {
                        article.setLiked(data.getLiked());
                        article.setIsChecked(data.isChecked());
                        if (!((BaseActivity) context).hasDestroyed()) {
                            notifyDataSetChanged();
                        }

                        List<Articles> cachedMagazinesList = getCachedMagazinesList();

                        if (cachedMagazinesList != null) {
                            List<Articles> tempList = cachedMagazinesList;
                            for (int i = 0; i < cachedMagazinesList.size(); i++) {
                                if (data.getId().equals(tempList.get(i).getId())) {
                                    tempList.get(i).setLiked(data.getLiked());
                                }
                            }
                            cachedMagazinesList = tempList;

                            saveCachedMagazinesList(cachedMagazinesList);
                        }
                        break;
                    }

                }
            }
        }
    }

    @Override
    public void updateMagazineStatus(Articles data, String follow) {
        autoReflectStatus(data, follow);
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0 && !magazineFlipArticlesFragment.isSearch) {
            return 0;
        } else if (position == MagazineFlipArticlesFragment.suggestionsPosition && !magazineFlipArticlesFragment.isSearch) {
            return 2;
        } else if (magazineFlipArticlesFragment.isSearch) {
            return 1;
        } else {
            return 1;
        }
    }

    /**
     * Populates the top article
     *
     * @param layout   The view object
     * @param holder   The view holder object
     * @param data     The articles object
     * @param position The position
     */
    private void populateTopArticle(View layout, ViewHolder holder, final Articles data, final int position) {
        // Log.d("ArticlesBaseAdapter", "In populateTopArticle");
        if (holder.magazineLikeTop != null) {
            holder.magazineLikeTop.setTag(position);
        }

        if (holder.articleTitleTop != null) {
            holder.articleTitleTop
                    .setText(AphidLog.format("%s", data.getTitle()));
        }

        if (holder.magazineLikeTop != null) {
            holder.magazineLikeTop.setOnCheckedChangeListener(null);
            if (Boolean.valueOf(data.getLiked())) {
                data.setIsChecked(true);
            } else {
                data.setIsChecked(false);
            }

            holder.magazineLikeTop.setText("");
            holder.magazineLikeTop.setChecked(Boolean.valueOf(data.getLiked()));

            holder.magazineLikeTop.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    if (isChecked) {
                        ((BaseActivity) context).showProgressDialog();
                        String accessToken = preferenceEndPoint.getStringPreference("access_token");
                        yoService.likeArticlesAPI(data.getId(), accessToken).enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                ((BaseActivity) context).dismissProgressDialog();

                                data.setIsChecked(true);
                                data.setLiked("true");
                                if (!((BaseActivity) context).hasDestroyed()) {
                                    notifyDataSetChanged();
                                }
                                if (OtherProfilesLikedArticles.getListener() != null) {
                                    OtherProfilesLikedArticles.getListener().updateOtherPeopleStatus(data, Constants.LIKE_EVENT);
                                }
                                mToastFactory.showToast("You have liked the article " + data.getTitle());

                                List<Articles> cachedMagazinesList = getCachedMagazinesList();
                                if (cachedMagazinesList != null) {
                                    List<Articles> tempList = cachedMagazinesList;
                                    for (int i = 0; i < cachedMagazinesList.size(); i++) {
                                        if (data.getId().equals(tempList.get(i).getId())) {
                                            tempList.get(i).setLiked("true");
                                        }
                                    }

                                    cachedMagazinesList = tempList;

                                    saveCachedMagazinesList(cachedMagazinesList);
                                }

                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                ((BaseActivity) context).dismissProgressDialog();
                                mToastFactory.showToast("Error while liking article " + data.getTitle());
                                data.setIsChecked(false);
                                data.setLiked("false");
                                if (!((BaseActivity) context).hasDestroyed()) {
                                    notifyDataSetChanged();
                                }

                                List<Articles> cachedMagazinesList = getCachedMagazinesList();
                                if (cachedMagazinesList != null) {
                                    List<Articles> tempList = cachedMagazinesList;
                                    for (int i = 0; i < cachedMagazinesList.size(); i++) {
                                        if (data.getId().equals(tempList.get(i).getId())) {
                                            tempList.get(i).setLiked("false");
                                        }
                                    }

                                    cachedMagazinesList = tempList;

                                    saveCachedMagazinesList(cachedMagazinesList);
                                }
                            }
                        });
                    } else {
                        ((BaseActivity) context).showProgressDialog();
                        String accessToken = preferenceEndPoint.getStringPreference("access_token");
                        yoService.unlikeArticlesAPI(data.getId(), accessToken).enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                ((BaseActivity) context).dismissProgressDialog();
                                data.setIsChecked(false);
                                data.setLiked("false");
                                if (OtherProfilesLikedArticles.getListener() != null) {
                                    OtherProfilesLikedArticles.getListener().updateOtherPeopleStatus(data, Constants.LIKE_EVENT);
                                }
                                if (!((BaseActivity) context).hasDestroyed()) {
                                    notifyDataSetChanged();
                                }

                                mToastFactory.showToast("You have un-liked the article " + data.getTitle());

                                List<Articles> cachedMagazinesList = getCachedMagazinesList();
                                if (cachedMagazinesList != null) {
                                    List<Articles> tempList = cachedMagazinesList;
                                    for (int i = 0; i < cachedMagazinesList.size(); i++) {
                                        if (data.getId().equals(tempList.get(i).getId())) {
                                            tempList.get(i).setLiked("false");
                                        }
                                    }

                                    cachedMagazinesList = tempList;

                                    saveCachedMagazinesList(cachedMagazinesList);
                                }

                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                ((BaseActivity) context).dismissProgressDialog();
                                Toast.makeText(context, "Error while un liking article " + data.getTitle(), Toast.LENGTH_LONG).show();
                                data.setIsChecked(true);
                                data.setLiked("true");
                                if (!((BaseActivity) context).hasDestroyed()) {
                                    notifyDataSetChanged();
                                }

                                List<Articles> cachedMagazinesList = getCachedMagazinesList();
                                if (cachedMagazinesList != null) {
                                    List<Articles> tempList = cachedMagazinesList;
                                    for (int i = 0; i < cachedMagazinesList.size(); i++) {
                                        if (data.getId().equals(tempList.get(i).getId())) {
                                            tempList.get(i).setLiked("true");
                                        }
                                    }

                                    cachedMagazinesList = tempList;

                                    saveCachedMagazinesList(cachedMagazinesList);
                                }
                            }
                        });
                    }
                }
            });
        }

        if (holder.articlePhotoTop != null) {
            final ImageView photoView = holder.articlePhotoTop;
            photoView.setImageResource(R.drawable.magazine_backdrop);
            if (data.getImage_filename() != null) {
                if (!((BaseActivity) context).hasDestroyed()) {
                    //new NewImageRenderTask(context, data.getImage_filename(), photoView).execute();
                    Glide.with(context)
                            .load(data.getImage_filename())
                            .asBitmap()
                            .placeholder(R.drawable.magazine_backdrop)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .dontAnimate()
                            .into(new SimpleTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                    int screenWidth = DeviceDimensionsHelper.getDisplayWidth(context);
                                    if (resource != null) {
                                        Bitmap bmp = BitmapScaler.scaleToFitWidth(resource, screenWidth);
                                        Glide.with(context)
                                                .load(data.getImage_filename())
                                                .override(bmp.getWidth(), bmp.getHeight())
                                                .placeholder(R.drawable.magazine_backdrop)
                                                .crossFade()
                                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                                .dontAnimate()
                                                .into(photoView);
                                    }
                                }
                            });
                }
            } else {
                photoView.setImageResource(R.drawable.magazine_backdrop);
            }

            photoView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, MagazineArticleDetailsActivity.class);
                    intent.putExtra("Title", data.getTitle());
                    intent.putExtra("Image", data.getUrl());
                    intent.putExtra("Article", data);
                    intent.putExtra("Position", position);
                    magazineFlipArticlesFragment.startActivityForResult(intent, 500);
                }
            });
        }

        if (holder.magazineAddTop != null) {
            ImageView add = holder.magazineAddTop;
            add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, CreateMagazineActivity.class);
                    intent.putExtra(Constants.MAGAZINE_ADD_ARTICLE_ID, data.getId());
                    if (context instanceof Activity) {
                        ((Activity) context).startActivityForResult(intent, Constants.ADD_ARTICLES_TO_MAGAZINE);
                    }
                }
            });
        }

        if (holder.magazineShareTop != null) {
            ImageView share = holder.magazineShareTop;
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
        }

        if (holder.articleFollowTop != null) {
            if ("true".equals(data.getIsFollowing())) {
                holder.articleFollowTop.setText("Following");
                holder.articleFollowTop.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_following_tick, 0, 0, 0);
            } else {
                holder.articleFollowTop.setText("Follow");
                holder.articleFollowTop.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            }

            final ViewHolder finalHolder = holder;
            holder.articleFollowTop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    followArticle(data, finalHolder, finalHolder.articleFollowTop);
                }
            });
        }

        Button followMoreTopics = (Button) layout.findViewById(R.id.btn_magazine_follow_topics_top);
        if (followMoreTopics != null) {
            followMoreTopics.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent;
                    if (!BuildConfig.NEW_FOLLOW_MORE_TOPICS) {
                        intent = new Intent(context, FollowMoreTopicsActivity.class);
                    } else {
                        intent = new Intent(context, NewFollowMoreTopicsActivity.class);
                    }
                    intent.putExtra("From", "Magazines");
                    context.startActivity(intent);
                }
            });
        }

        if (holder.tvTopicNameTop != null) {
            if (!TextUtils.isEmpty(data.getTopicName())) {
                holder.tvTopicNameTop.setVisibility(View.VISIBLE);
                holder.tvTopicNameTop.setText(data.getTopicName());
                holder.tvTopicNameTop.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, TopicsDetailActivity.class);
                        intent.putExtra("Topic", data);
                        intent.putExtra("Position", position);
                        magazineFlipArticlesFragment.startActivityForResult(intent, 60);
                    }
                });
            } else {
                holder.tvTopicNameTop.setVisibility(View.GONE);
            }
        }

    }

    /**
     * Populates the left article
     *
     * @param holder   The view holder object
     * @param data     The articles object
     * @param position The position
     */
    private void populateLeftArticle(ViewHolder holder, final Articles data, final int position) {
        //Log.d("ArticlesBaseAdapter", "In populateLeftArticle");
        if (holder.magazineLikeLeft != null) {
            holder.magazineLikeLeft.setVisibility(View.VISIBLE);
            holder.magazineLikeLeft.setTag(position);
        }

        if (holder.articleTitleLeft != null) {
            holder.articleTitleLeft.setVisibility(View.VISIBLE);
            holder.articleTitleLeft
                    .setText(AphidLog.format("%s", data.getTitle()));
            holder.articleTitleLeft.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, MagazineArticleDetailsActivity.class);
                    intent.putExtra("Title", data.getTitle());
                    intent.putExtra("Image", data.getUrl());
                    intent.putExtra("Article", data);
                    intent.putExtra("Position", position);
                    intent.putExtra("ArticlePlacement", "left");
                    magazineFlipArticlesFragment.startActivityForResult(intent, 500);
                }
            });
        }

        if (holder.magazineLikeLeft != null) {
            holder.magazineLikeLeft.setOnCheckedChangeListener(null);
            if (Boolean.valueOf(data.getLiked())) {
                data.setIsChecked(true);
            } else {
                data.setIsChecked(false);
            }

            holder.magazineLikeLeft.setText("");
            holder.magazineLikeLeft.setChecked(Boolean.valueOf(data.getLiked()));

            holder.magazineLikeLeft.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    if (isChecked) {
                        ((BaseActivity) context).showProgressDialog();
                        String accessToken = preferenceEndPoint.getStringPreference("access_token");
                        yoService.likeArticlesAPI(data.getId(), accessToken).enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                likeUnlikeErrorMessage(response);
                                ((BaseActivity) context).dismissProgressDialog();
                                data.setIsChecked(true);
                                data.setLiked("true");
                                if (!((BaseActivity) context).hasDestroyed()) {
                                    notifyDataSetChanged();
                                }
                                if (OtherProfilesLikedArticles.getListener() != null) {
                                    OtherProfilesLikedArticles.getListener().updateOtherPeopleStatus(data, Constants.LIKE_EVENT);
                                }
                                mToastFactory.showToast("You have liked the article " + data.getTitle());

                                List<Articles> cachedMagazinesList = getCachedMagazinesList();
                                if (cachedMagazinesList != null) {
                                    List<Articles> tempList = cachedMagazinesList;
                                    for (int i = 0; i < cachedMagazinesList.size(); i++) {
                                        if (data.getId().equals(tempList.get(i).getId())) {
                                            tempList.get(i).setLiked("true");
                                        }
                                    }

                                    cachedMagazinesList = tempList;

                                    saveCachedMagazinesList(cachedMagazinesList);
                                }

                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                ((BaseActivity) context).dismissProgressDialog();
                                mToastFactory.showToast("Error while liking article " + data.getTitle());
                                data.setIsChecked(false);
                                data.setLiked("false");
                                if (!((BaseActivity) context).hasDestroyed()) {
                                    notifyDataSetChanged();
                                }

                                List<Articles> cachedMagazinesList = getCachedMagazinesList();
                                if (cachedMagazinesList != null) {
                                    List<Articles> tempList = cachedMagazinesList;
                                    for (int i = 0; i < cachedMagazinesList.size(); i++) {
                                        if (data.getId().equals(tempList.get(i).getId())) {
                                            tempList.get(i).setLiked("false");
                                        }
                                    }

                                    cachedMagazinesList = tempList;

                                    saveCachedMagazinesList(cachedMagazinesList);
                                }
                            }
                        });
                    } else {
                        ((BaseActivity) context).showProgressDialog();
                        String accessToken = preferenceEndPoint.getStringPreference("access_token");
                        yoService.unlikeArticlesAPI(data.getId(), accessToken).enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                likeUnlikeErrorMessage(response);
                                ((BaseActivity) context).dismissProgressDialog();
                                data.setIsChecked(false);
                                data.setLiked("false");
                                if (OtherProfilesLikedArticles.getListener() != null) {
                                    OtherProfilesLikedArticles.getListener().updateOtherPeopleStatus(data, Constants.LIKE_EVENT);
                                }
                                if (!((BaseActivity) context).hasDestroyed()) {
                                    notifyDataSetChanged();
                                }

                                mToastFactory.showToast("You have un-liked the article " + data.getTitle());

                                List<Articles> cachedMagazinesList = getCachedMagazinesList();
                                if (cachedMagazinesList != null) {
                                    List<Articles> tempList = cachedMagazinesList;
                                    for (int i = 0; i < cachedMagazinesList.size(); i++) {
                                        if (data.getId().equals(tempList.get(i).getId())) {
                                            tempList.get(i).setLiked("false");
                                        }
                                    }

                                    cachedMagazinesList = tempList;

                                    saveCachedMagazinesList(cachedMagazinesList);
                                }

                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                ((BaseActivity) context).dismissProgressDialog();
                                Toast.makeText(context, "Error while un liking article " + data.getTitle(), Toast.LENGTH_LONG).show();
                                data.setIsChecked(true);
                                data.setLiked("true");
                                if (!((BaseActivity) context).hasDestroyed()) {
                                    notifyDataSetChanged();
                                }

                                List<Articles> cachedMagazinesList = getCachedMagazinesList();
                                if (cachedMagazinesList != null) {
                                    List<Articles> tempList = cachedMagazinesList;
                                    for (int i = 0; i < cachedMagazinesList.size(); i++) {
                                        if (data.getId().equals(tempList.get(i).getId())) {
                                            tempList.get(i).setLiked("true");
                                        }
                                    }

                                    cachedMagazinesList = tempList;

                                    saveCachedMagazinesList(cachedMagazinesList);
                                }
                            }
                        });
                    }
                }
            });
        }

        if (holder.articlePhotoLeft != null) {
            final ImageView photoView = holder.articlePhotoLeft;
            photoView.setVisibility(View.VISIBLE);
            photoView.setImageResource(R.drawable.magazine_backdrop);
            if (data.getImage_filename() != null) {
                if (!((BaseActivity) context).hasDestroyed()) {
                    //new NewImageRenderTask(context, data.getImage_filename(), photoView).execute();
                    Glide.with(context)
                            .load(data.getImage_filename())
                            .asBitmap()
                            .placeholder(R.drawable.magazine_backdrop)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .dontAnimate()
                            .into(new SimpleTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                    int screenWidth = DeviceDimensionsHelper.getDisplayWidth(context);
                                    if (resource != null) {
                                        Bitmap bmp = BitmapScaler.scaleToFitWidth(resource, screenWidth);
                                        Glide.with(context)
                                                .load(data.getImage_filename())
                                                .override(bmp.getWidth(), bmp.getHeight())
                                                .placeholder(R.drawable.magazine_backdrop)
                                                .crossFade()
                                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                                .dontAnimate()
                                                .into(photoView);
                                    }
                                }
                            });
                }
            } else {
                photoView.setImageResource(R.drawable.magazine_backdrop);
            }

            photoView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, MagazineArticleDetailsActivity.class);
                    intent.putExtra("Title", data.getTitle());
                    intent.putExtra("Image", data.getUrl());
                    intent.putExtra("Article", data);
                    intent.putExtra("Position", position);
                    intent.putExtra("ArticlePlacement", "left");
                    magazineFlipArticlesFragment.startActivityForResult(intent, 500);
                }
            });
        }

        if (holder.magazineAddLeft != null) {
            ImageView add = holder.magazineAddLeft;
            add.setVisibility(View.VISIBLE);
            add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, CreateMagazineActivity.class);
                    intent.putExtra(Constants.MAGAZINE_ADD_ARTICLE_ID, data.getId());
                    if (context instanceof Activity) {
                        ((Activity) context).startActivityForResult(intent, Constants.ADD_ARTICLES_TO_MAGAZINE);
                    }
                }
            });
        }

        if (holder.magazineShareLeft != null) {
            ImageView share = holder.magazineShareLeft;
            share.setVisibility(View.VISIBLE);
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
        }

        if (holder.articleFollowLeft != null) {

            if ("true".equals(data.getIsFollowing())) {
                holder.articleFollowLeft.setText("Following");
                holder.articleFollowLeft.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_following_tick, 0, 0, 0);
            } else {
                holder.articleFollowLeft.setText("Follow");
                holder.articleFollowLeft.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            }

            final ViewHolder finalHolder = holder;
            holder.articleFollowLeft.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    followArticle(data, finalHolder, finalHolder.articleFollowLeft);
                }
            });
        }

        if (holder.tvTopicNameLeft != null) {
            if (!TextUtils.isEmpty(data.getTopicName())) {
                holder.tvTopicNameLeft.setVisibility(View.VISIBLE);
                holder.tvTopicNameLeft.setText(data.getTopicName());
                holder.tvTopicNameLeft.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, TopicsDetailActivity.class);
                        intent.putExtra("Topic", data);
                        intent.putExtra("Position", position);
                        intent.putExtra("ArticlePlacement", "left");
                        magazineFlipArticlesFragment.startActivityForResult(intent, 60);
                    }
                });
            } else {
                holder.tvTopicNameLeft.setVisibility(View.GONE);
            }
        }

        if (holder.articleSummaryLeft != null) {

            float density = context.getResources().getDisplayMetrics().density;

            if (density == 4.0) {
                holder.articleSummaryLeft.setVisibility(View.VISIBLE);
            } else if (density == 3.5) {
                holder.articleSummaryLeft.setVisibility(View.VISIBLE);
            } else if (density == 3.0) {
                holder.articleSummaryLeft.setVisibility(View.VISIBLE);
            } else if (density == 2.0) {
                holder.articleSummaryLeft.setVisibility(View.VISIBLE);
            } else {
                holder.articleSummaryLeft.setVisibility(View.GONE);
            }

            if (data.getSummary() != null && holder.articleSummaryLeft != null) {
                holder.articleSummaryLeft
                        .setText(Html.fromHtml(data.getSummary()));
            }
        }


    }

    /**
     * Populates the right article
     *
     * @param holder   The view holder object
     * @param data     The articles object
     * @param position The position
     */
    private void populateRightArticle(ViewHolder holder, final Articles data, final int position) {
        //  Log.d("ArticlesBaseAdapter", "In populateRightArticle");
        if (holder.magazineLikeRight != null) {
            holder.magazineLikeRight.setVisibility(View.VISIBLE);
            holder.magazineLikeRight.setTag(position);
        }

        if (holder.articleTitleRight != null) {
            holder.articleTitleRight.setVisibility(View.VISIBLE);
            holder.articleTitleRight
                    .setText(AphidLog.format("%s", data.getTitle()));
            holder.articleTitleRight.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, MagazineArticleDetailsActivity.class);
                    intent.putExtra("Title", data.getTitle());
                    intent.putExtra("Image", data.getUrl());
                    intent.putExtra("Article", data);
                    intent.putExtra("Position", position);
                    intent.putExtra("ArticlePlacement", "right");
                    magazineFlipArticlesFragment.startActivityForResult(intent, 500);
                }
            });
        }

        if (holder.magazineLikeRight != null) {
            holder.magazineLikeRight.setOnCheckedChangeListener(null);
            if (Boolean.valueOf(data.getLiked())) {
                data.setIsChecked(true);
            } else {
                data.setIsChecked(false);
            }

            holder.magazineLikeRight.setText("");
            holder.magazineLikeRight.setChecked(Boolean.valueOf(data.getLiked()));

            holder.magazineLikeRight.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    if (isChecked) {
                        ((BaseActivity) context).showProgressDialog();
                        String accessToken = preferenceEndPoint.getStringPreference("access_token");
                        yoService.likeArticlesAPI(data.getId(), accessToken).enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                ((BaseActivity) context).dismissProgressDialog();
                                data.setIsChecked(true);
                                data.setLiked("true");
                                if (!((BaseActivity) context).hasDestroyed()) {
                                    notifyDataSetChanged();
                                }
                                if (OtherProfilesLikedArticles.getListener() != null) {
                                    OtherProfilesLikedArticles.getListener().updateOtherPeopleStatus(data, Constants.LIKE_EVENT);
                                }
                                mToastFactory.showToast("You have liked the article " + data.getTitle());

                                List<Articles> cachedMagazinesList = getCachedMagazinesList();
                                if (cachedMagazinesList != null) {
                                    List<Articles> tempList = cachedMagazinesList;
                                    for (int i = 0; i < cachedMagazinesList.size(); i++) {
                                        if (data.getId().equals(tempList.get(i).getId())) {
                                            tempList.get(i).setLiked("true");
                                        }
                                    }

                                    cachedMagazinesList = tempList;

                                    saveCachedMagazinesList(cachedMagazinesList);
                                }

                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                ((BaseActivity) context).dismissProgressDialog();
                                mToastFactory.showToast("Error while liking article " + data.getTitle());
                                data.setIsChecked(false);
                                data.setLiked("false");
                                if (!((BaseActivity) context).hasDestroyed()) {
                                    notifyDataSetChanged();
                                }

                                List<Articles> cachedMagazinesList = getCachedMagazinesList();
                                if (cachedMagazinesList != null) {
                                    List<Articles> tempList = cachedMagazinesList;
                                    for (int i = 0; i < cachedMagazinesList.size(); i++) {
                                        if (data.getId().equals(tempList.get(i).getId())) {
                                            tempList.get(i).setLiked("false");
                                        }
                                    }

                                    cachedMagazinesList = tempList;

                                    saveCachedMagazinesList(cachedMagazinesList);
                                }
                            }
                        });
                    } else {
                        ((BaseActivity) context).showProgressDialog();
                        String accessToken = preferenceEndPoint.getStringPreference("access_token");
                        yoService.unlikeArticlesAPI(data.getId(), accessToken).enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                ((BaseActivity) context).dismissProgressDialog();
                                data.setIsChecked(false);
                                data.setLiked("false");
                                if (OtherProfilesLikedArticles.getListener() != null) {
                                    OtherProfilesLikedArticles.getListener().updateOtherPeopleStatus(data, Constants.LIKE_EVENT);
                                }
                                if (!((BaseActivity) context).hasDestroyed()) {
                                    notifyDataSetChanged();
                                }

                                mToastFactory.showToast("You have un-liked the article " + data.getTitle());

                                List<Articles> cachedMagazinesList = getCachedMagazinesList();
                                if (cachedMagazinesList != null) {
                                    List<Articles> tempList = cachedMagazinesList;
                                    for (int i = 0; i < cachedMagazinesList.size(); i++) {
                                        if (data.getId().equals(tempList.get(i).getId())) {
                                            tempList.get(i).setLiked("false");
                                        }
                                    }

                                    cachedMagazinesList = tempList;

                                    saveCachedMagazinesList(cachedMagazinesList);
                                }

                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                ((BaseActivity) context).dismissProgressDialog();
                                Toast.makeText(context, "Error while un liking article " + data.getTitle(), Toast.LENGTH_LONG).show();
                                data.setIsChecked(true);
                                data.setLiked("true");
                                if (!((BaseActivity) context).hasDestroyed()) {
                                    notifyDataSetChanged();
                                }

                                List<Articles> cachedMagazinesList = getCachedMagazinesList();
                                if (cachedMagazinesList != null) {
                                    List<Articles> tempList = cachedMagazinesList;
                                    for (int i = 0; i < cachedMagazinesList.size(); i++) {
                                        if (data.getId().equals(tempList.get(i).getId())) {
                                            tempList.get(i).setLiked("true");
                                        }
                                    }

                                    cachedMagazinesList = tempList;

                                    saveCachedMagazinesList(cachedMagazinesList);
                                }
                            }
                        });
                    }
                }
            });
        }

        if (holder.articlePhotoRight != null) {
            final ImageView photoView = holder.articlePhotoRight;
            photoView.setVisibility(View.VISIBLE);
            photoView.setImageResource(R.drawable.magazine_backdrop);
            if (data.getImage_filename() != null) {
                if (!((BaseActivity) context).hasDestroyed()) {
                    //new NewImageRenderTask(context, data.getImage_filename(), photoView).execute();
                    Glide.with(context)
                            .load(data.getImage_filename())
                            .asBitmap()
                            .placeholder(R.drawable.magazine_backdrop)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .dontAnimate()
                            .into(new SimpleTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                    int screenWidth = DeviceDimensionsHelper.getDisplayWidth(context);
                                    if (resource != null) {
                                        Bitmap bmp = BitmapScaler.scaleToFitWidth(resource, screenWidth);
                                        Glide.with(context)
                                                .load(data.getImage_filename())
                                                .override(bmp.getWidth(), bmp.getHeight())
                                                .placeholder(R.drawable.magazine_backdrop)
                                                .crossFade()
                                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                                .dontAnimate()
                                                .into(photoView);
                                    }
                                }
                            });
                }
            } else {
                photoView.setImageResource(R.drawable.magazine_backdrop);
            }

            photoView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, MagazineArticleDetailsActivity.class);
                    intent.putExtra("Title", data.getTitle());
                    intent.putExtra("Image", data.getUrl());
                    intent.putExtra("Article", data);
                    intent.putExtra("Position", position);
                    intent.putExtra("ArticlePlacement", "right");
                    magazineFlipArticlesFragment.startActivityForResult(intent, 500);
                }
            });
        }

        if (holder.magazineAddRight != null) {
            ImageView add = holder.magazineAddRight;
            add.setVisibility(View.VISIBLE);
            add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, CreateMagazineActivity.class);
                    intent.putExtra(Constants.MAGAZINE_ADD_ARTICLE_ID, data.getId());
                    if (context instanceof Activity) {
                        ((Activity) context).startActivityForResult(intent, Constants.ADD_ARTICLES_TO_MAGAZINE);
                    }
                }
            });
        }

        if (holder.magazineShareRight != null) {
            ImageView share = holder.magazineShareRight;
            share.setVisibility(View.VISIBLE);
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
        }

        if (holder.articleFollowRight != null) {

            if ("true".equals(data.getIsFollowing())) {
                holder.articleFollowRight.setText("Following");
                holder.articleFollowRight.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_following_tick, 0, 0, 0);
            } else {
                holder.articleFollowRight.setText("Follow");
                holder.articleFollowRight.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            }

            final ViewHolder finalHolder = holder;
            holder.articleFollowRight.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    followArticle(data, finalHolder, finalHolder.articleFollowRight);
                }
            });
        }

        if (holder.tvTopicNameRight != null) {
            if (!TextUtils.isEmpty(data.getTopicName())) {
                holder.tvTopicNameRight.setVisibility(View.VISIBLE);
                holder.tvTopicNameRight.setText(data.getTopicName());
                holder.tvTopicNameRight.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, TopicsDetailActivity.class);
                        intent.putExtra("Topic", data);
                        intent.putExtra("Position", position);
                        intent.putExtra("ArticlePlacement", "right");
                        magazineFlipArticlesFragment.startActivityForResult(intent, 60);
                    }
                });
            } else {
                holder.tvTopicNameRight.setVisibility(View.GONE);
            }
        }

        if (holder.articleSummaryRight != null) {

            float density = context.getResources().getDisplayMetrics().density;

            if (density == 4.0) {
                holder.articleSummaryRight.setVisibility(View.VISIBLE);
            } else if (density == 3.5) {
                holder.articleSummaryRight.setVisibility(View.VISIBLE);
            } else if (density == 3.0) {
                holder.articleSummaryRight.setVisibility(View.VISIBLE);
            } else if (density == 2.0) {
                holder.articleSummaryRight.setVisibility(View.VISIBLE);
            } else {
                holder.articleSummaryRight.setVisibility(View.GONE);
            }

            if (data.getSummary() != null && holder.articleSummaryRight != null) {
                holder.articleSummaryRight
                        .setText(Html.fromHtml(data.getSummary()));
            }
        }

    }

    /**
     * Populates the empty left article
     *
     * @param holder The view holder object
     */
    private void populateEmptyLeftArticle(ViewHolder holder) {
        if (holder.magazineLikeLeft != null) {
            holder.magazineLikeLeft.setVisibility(View.GONE);
        }

        if (holder.articleTitleLeft != null) {
            holder.articleTitleLeft.setVisibility(View.GONE);
        }

        if (holder.articlePhotoLeft != null) {
            ImageView photoView = holder.articlePhotoLeft;
            photoView.setVisibility(View.GONE);
        }

        if (holder.magazineAddLeft != null) {
            ImageView add = holder.magazineAddLeft;
            add.setVisibility(View.GONE);
        }

        if (holder.magazineShareLeft != null) {
            ImageView share = holder.magazineShareLeft;
            share.setVisibility(View.GONE);
        }

        if (holder.articleFollowLeft != null) {
            holder.articleFollowLeft.setVisibility(View.GONE);
        }

        if (holder.tvTopicNameLeft != null) {
            holder.tvTopicNameLeft.setVisibility(View.GONE);
        }

    }

    /**
     * Populates the empty right article
     *
     * @param holder The view holder object
     */
    private void populateEmptyRightArticle(ViewHolder holder) {
        if (holder.magazineLikeRight != null) {
            holder.magazineLikeRight.setVisibility(View.GONE);
        }

        if (holder.articleTitleRight != null) {
            holder.articleTitleRight.setVisibility(View.GONE);
        }

        if (holder.articlePhotoRight != null) {
            ImageView photoView = holder.articlePhotoRight;
            photoView.setVisibility(View.GONE);
        }

        if (holder.magazineAddRight != null) {
            ImageView add = holder.magazineAddRight;
            add.setVisibility(View.GONE);
        }

        if (holder.magazineShareRight != null) {
            ImageView share = holder.magazineShareRight;
            share.setVisibility(View.GONE);
        }

        if (holder.articleFollowRight != null) {
            holder.articleFollowRight.setVisibility(View.GONE);
        }

        if (holder.tvTopicNameRight != null) {
            holder.tvTopicNameRight.setVisibility(View.GONE);
        }

    }

    /**
     * Updates the topic follow status
     *
     * @param data   The articles object
     * @param follow The follow string
     */
    @Override
    public void updateFollowTopicStatus(Articles data, String follow) {
        if (data != null) {
            for (Articles article : allArticles) {

                if (data.getTopicId() != null && data.getTopicId().equals(article.getTopicId())) {
                    article.setTopicFollowing(data.getTopicFollowing());
                    if (!((BaseActivity) context).hasDestroyed()) {
                        notifyDataSetChanged();
                    }

                    List<Articles> cachedMagazinesList = getCachedMagazinesList();

                    if (cachedMagazinesList != null) {
                        List<Articles> tempList = cachedMagazinesList;
                        for (int i = 0; i < cachedMagazinesList.size(); i++) {
                            if (data.getTopicId().equals(tempList.get(i).getTopicId())) {
                                tempList.get(i).setTopicFollowing(data.getTopicFollowing());
                            }
                        }
                        cachedMagazinesList = tempList;

                        saveCachedMagazinesList(cachedMagazinesList);
                    }
                }

            }
        }

    }

    @Override
    public void updateUnfollowTopicStatus(String topicId, String follow) {
        if (!TextUtils.isEmpty(topicId)) {

            List<Articles> followedTopicArticlesList = new ArrayList<Articles>();

            allArticles = getAllItems();

            for (Articles article : allArticles) {
                if(article != null) {
                    if (topicId.equals(article.getTopicId())) {
                        article.setTopicFollowing("false");
                        followedTopicArticlesList.add(article);
                    }
                }
            }

            removeItems(followedTopicArticlesList);

            List<Articles> finalArticles = getAllItems();
            allArticles = new ArrayList<Articles>(new LinkedHashSet<Articles>(finalArticles));

            if (topicId.equals(secondArticle.getTopicId())) {
                allArticles.remove(secondArticle);
            }

            if (topicId.equals(thirdArticle.getTopicId())) {
                allArticles.remove(thirdArticle);
            }

            if (allArticles.size() >= 2) {
                secondArticle = allArticles.get(1);
            }

            if (allArticles.size() >= 3) {
                thirdArticle = allArticles.get(2);
            }

            if (!((BaseActivity) context).hasDestroyed()) {
                notifyDataSetChanged();
            }

            if (getCount() > 0) {
                magazineFlipArticlesFragment.flipView.flipTo(0);
            }

            if (allArticles.size() == 0) {
                magazineFlipArticlesFragment.loadArticles(null, false);
            }

            List<Articles> cachedMagazinesList = getCachedMagazinesList();

            if (cachedMagazinesList != null) {
                List<Articles> tempList = new ArrayList<>();
                for (int i = 0; i < cachedMagazinesList.size(); i++) {
                    if (topicId.equals(cachedMagazinesList.get(i).getTopicId())) {
                        cachedMagazinesList.get(i).setTopicFollowing("false");
                        tempList.add(cachedMagazinesList.get(i));
                    }
                }
                cachedMagazinesList.removeAll(tempList);

                saveCachedMagazinesList(cachedMagazinesList);
            }
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

        private Button articleFollow;

        private TextView articleTitleTop;

        private ImageView articlePhotoTop;

        private CheckBox magazineLikeTop;

        private ImageView magazineAddTop;

        private ImageView magazineShareTop;

        private Button articleFollowTop;

        private TextView articleTitleLeft;

        private ImageView articlePhotoLeft;

        private CheckBox magazineLikeLeft;

        private ImageView magazineAddLeft;

        private ImageView magazineShareLeft;

        private Button articleFollowLeft;

        private TextView articleTitleRight;

        private ImageView articlePhotoRight;

        private CheckBox magazineLikeRight;

        private ImageView magazineAddRight;

        private ImageView magazineShareRight;

        private Button articleFollowRight;

        private ListView lvSuggestions;

        private RecyclerView rvSuggestions;

        private TextView noSuggestionsTextView;

        private TextView tvFollowMoreTopics;

        private TextView tvTopicName;

        private TextView tvTopicNameTop;

        private TextView tvTopicNameLeft;

        private TextView tvTopicNameRight;

        private TextView articleSummaryLeft;

        private TextView articleSummaryRight;

        private TextView fullImageTitle;

        private ImageView blackMask;

        private RelativeLayout rlFullImageOptions;

        private CheckBox fullImageMagazineLike;

        private ImageView fullImageMagazineAdd;

        private ImageView fullImageMagazineShare;

        private Button doneButton;


        private RelativeLayout topLayout;
        private LinearLayout middleLayout; //like, add and share
        private LinearLayout bottomLayout;

    }

    /**
     * Updates the topic follow
     *
     * @param isFollowing  isFollowing or not
     * @param topic        The articles object
     * @param position     The position
     * @param articlePlace The article's placement
     */
    public void updateTopic(boolean isFollowing, Articles topic, int position, String articlePlace) {

        if (TextUtils.isEmpty(articlePlace)) {
            items.remove(position);
            items.add(position, topic);
        } else if ("left".equals(articlePlace)) {
            secondArticle = topic;
        } else if ("right".equals(articlePlace)) {
            thirdArticle = topic;
        }

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
     *
     * @param isLiked      isLiked article or not
     * @param articles     The Articles object
     * @param position     The position
     * @param articlePlace The article placement
     */
    public void updateArticle(boolean isLiked, Articles articles, int position, String articlePlace) {
        Log.d("ArticlesBaseAdapter", "The position in updateArticle " + position);
        if (TextUtils.isEmpty(articlePlace)) {
            items.remove(position);
            items.add(position, articles);
        } else if ("left".equals(articlePlace)) {
            secondArticle = articles;
        } else if ("right".equals(articlePlace)) {
            thirdArticle = articles;
        }

        Log.d("ArticlesBaseAdapter", "The items in updateArticle after add " + items.get(position).getTitle() + " position " + position + " liked " + items.get(position).getLiked());

        notifyDataSetChanged();
    }

    /**
     * Gets all the articles
     *
     * @return The articles list
     */
    public List<Articles> getAllItems() {
        getAllArticles = new ArrayList<>(items);
        /*if (getAllArticles.size() >= 2) {
            getAllArticles.add(1, secondArticle);
        }
        if (getAllArticles.size() >= 3) {
            getAllArticles.add(2, thirdArticle);
        }*/
        getAllArticles.add(secondArticle);
        getAllArticles.add(thirdArticle);
        return getAllArticles;
    }

    /**
     * Removes the articles from the list
     *
     * @param articlesList The articles list which needs to be removed
     */
    public void removeItems(List<Articles> articlesList) {
        items.removeAll(articlesList);
        notifyDataSetChanged();
    }

    /**
     * Gets the cached magazines list
     *
     * @return The list of articles
     */
    private List<Articles> getCachedMagazinesList() {
        Type type1 = new TypeToken<List<Articles>>() {
        }.getType();
        String userId = preferenceEndPoint.getStringPreference(Constants.USER_ID);
        List<Articles> cachedMagazinesList = new ArrayList<>();
        if (context != null) {
            String sharedFollowedCachedMagazines = MagazinePreferenceEndPoint.getInstance().getPref(context, userId).getString("followed_cached_magazines", "");
            String sharedRandomCachedMagazines = MagazinePreferenceEndPoint.getInstance().getPref(context, userId).getString("random_cached_magazines", "");

            if (!TextUtils.isEmpty(sharedFollowedCachedMagazines)) {
                String cachedMagazines = sharedFollowedCachedMagazines;
                List<Articles> cachedFollowedMagazinesList = new Gson().fromJson(cachedMagazines, type1);
                cachedMagazinesList.addAll(cachedFollowedMagazinesList);
            }
            if (!TextUtils.isEmpty(sharedRandomCachedMagazines)) {
                String cachedMagazines = sharedRandomCachedMagazines;
                List<Articles> cachedRandomMagazinesList = new Gson().fromJson(cachedMagazines, type1);
                cachedMagazinesList.addAll(cachedRandomMagazinesList);
            }
        }

        return cachedMagazinesList;
    }

    /**
     * Saves the cached magazines list
     *
     * @param cachedMagazinesList The list of articles to be cached
     */
    private void saveCachedMagazinesList(List<Articles> cachedMagazinesList) {
        List<Articles> followedTopicArticles = new ArrayList<>();
        List<Articles> randomTopicArticles = new ArrayList<>();
        for (Articles articles : cachedMagazinesList) {
            if ("true".equals(articles.getTopicFollowing())) {
                followedTopicArticles.add(articles);
            } else {
                randomTopicArticles.add(articles);
            }
        }

        String userId = preferenceEndPoint.getStringPreference(Constants.USER_ID);
        if (context != null) {
            SharedPreferences.Editor editor = MagazinePreferenceEndPoint.getInstance().get(context, userId);
            editor.putString("followed_cached_magazines", new Gson().toJson(new LinkedHashSet<Articles>(followedTopicArticles)));
            editor.putString("random_cached_magazines", new Gson().toJson(new LinkedHashSet<Articles>(randomTopicArticles)));
            editor.commit();
        }
    }

    private void likeUnlikeErrorMessage(Response<ResponseBody> response) {
        try {
            int code = (new JSONObject(response.body().string())).getInt("code");
            if (code == 422) {
                mToastFactory.showToast(R.string.like_unlike_error_message);
                return;
            }

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void doEllipsize(final TextView tv) {
        ViewTreeObserver vto = tv.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @SuppressWarnings("deprecation")
            @Override
            public void onGlobalLayout() {

                ViewTreeObserver obs = tv.getViewTreeObserver();
                obs.removeGlobalOnLayoutListener(this);

                //int visibleLines = (tv.getHeight() - tv.getPaddingTop() - tv.getPaddingBottom())/tv.getLineHeight();
                int height = tv.getHeight();
                int scrollY = tv.getScrollY();
                Layout layout = tv.getLayout();

                int firstVisibleLineNumber = layout.getLineForVertical(scrollY);
                int lastVisibleLineNumber = layout.getLineForVertical(scrollY + height);

                for (int i = 0; i < lastVisibleLineNumber - 1; i++) {
                    int lineEnd = layout.getLineEnd(i);

                }
                /*if (tv.getLineCount() > lastVisibleLineNumber) {
                    tv.setMaxLines(lastVisibleLineNumber);
                    tv.setEllipsize(TextUtils.TruncateAt.END);
                }*/

            }
        });
    }

    @Override
    public void onItemSelected(Topics topics) {
        if (!topics.isSelected()) {
            topics.setSelected(true);
        } else {
            topics.setSelected(false);
        }
        newSuggestionsAdapter.notifyDataSetChanged();
    }

    private void selectedTopics(final RecyclerView mRecyclerView, final TextView mNoSuggestions) {

        List<String> followedTopicsIdsList = new ArrayList();
        for (Categories categories : newSuggestionsAdapter.getmData()) {
            for (Topics topics : categories.getTags()) {
                if (topics.isSelected()) {
                    followedTopicsIdsList.add(topics.getId());
                }

            }
        }

        if (followedTopicsIdsList.size() > 0) {
            ((BaseActivity) context).showProgressDialog();
            mAddTopicsUsecase.addTopics(followedTopicsIdsList, new ApiCallback<List<Categories>>() {
                @Override
                public void onResult(List<Categories> result) {
                    newSuggestionsAdapter.getmData().clear();
                    newSuggestionsAdapter.setmData(new ArrayList<Categories>(result));
                    newSuggestionsAdapter.notifyDataSetChanged();
                    ((BaseActivity) context).dismissProgressDialog();
                }

                @Override
                public void onFailure(String message) {
                    ((BaseActivity) context).dismissProgressDialog();
                    if (mRecyclerView != null && mNoSuggestions != null) {
                        newSuggestionsAdapter.getmData().clear();
                        mRecyclerView.setVisibility(View.GONE);
                        mNoSuggestions.setVisibility(View.VISIBLE);
                        mNoSuggestions.setText(message);
                    }
                }
            });
        } else {
            mToastFactory.newToast(context.getString(R.string.no_topics_selected), Toast.LENGTH_SHORT);
        }
    }
}