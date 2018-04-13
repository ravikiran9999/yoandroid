package com.yo.android.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.aphidmobile.utils.AphidLog;
import com.aphidmobile.utils.UI;
import com.orion.android.common.preferences.PreferenceEndPoint;
import com.orion.android.common.util.ToastFactory;
import com.yo.android.BuildConfig;
import com.yo.android.R;
import com.yo.android.api.ApiCallback;
import com.yo.android.api.YoApi;
import com.yo.android.flip.MagazineFlipArticlesFragment;
import com.yo.android.model.Articles;
import com.yo.android.model.Categories;
import com.yo.android.model.Topics;
import com.yo.android.ui.BaseActivity;
import com.yo.android.ui.NewFollowMoreTopicsActivity;
import com.yo.android.ui.fragments.MagazinesFragment;
import com.yo.android.usecase.AddTopicsUsecase;
import com.yo.android.usecase.MagazinesServicesUsecase;
import com.yo.android.util.AutoReflectTopicsFollowActionsListener;
import com.yo.android.util.AutoReflectWishListActionsListener;
import com.yo.android.util.MagazineOtherPeopleReflectListener;
import com.yo.android.util.Util;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;

/**
 * The adapter for the Magazine landing screen articles
 */
public class MagazineArticlesBaseAdapter extends BaseAdapter implements AutoReflectWishListActionsListener, MagazineOtherPeopleReflectListener, AutoReflectTopicsFollowActionsListener, NewSuggestionsAdapter.TopicSelectionListener {

    private static final String TAG = MagazineArticlesBaseAdapter.class.getSimpleName();

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
    MagazinesServicesUsecase mMagazinesServicesUsecase;

    private static AutoReflectWishListActionsListener reflectListenerTemp;

    public MagazineArticlesBaseAdapter(Context context,
                                       PreferenceEndPoint preferenceEndPoint,
                                       YoApi.YoService yoService, ToastFactory mToastFactory, MagazineFlipArticlesFragment magazineFlipArticlesFragment, AddTopicsUsecase addTopicsUsecase, MagazinesServicesUsecase magazinesServicesUsecase) {
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
        mMagazinesServicesUsecase = magazinesServicesUsecase;
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
                /*if (!BuildConfig.NEW_FOLLOW_MORE_TOPICS) {
                    layout = inflater.inflate(R.layout.landing_suggestions_page, null);
                } else {*/
                    layout = inflater.inflate(R.layout.new_landing_suggestions_page, null);
                //}
            } else {
                // Inflate the layout with single article
                layout = inflater.inflate(R.layout.magazine_flip_layout, null);
            }

            holder = new ViewHolder();

            holder.articleTitle = UI.
                    findViewById(layout, R.id.tv_article_title);

            holder.articleShortDesc = UI
                    .findViewById(layout, R.id.tv_article_short_desc);

            holder.articlePhoto = UI.findViewById(layout, R.id.photo);

            holder.magazineLike = UI.findViewById(layout, R.id.cb_magazine_like);

            holder.magazineAdd = UI.findViewById(layout, R.id.imv_magazine_add);

            holder.magazineShare = UI.findViewById(layout, R.id.imv_magazine_share);

            holder.articleTitleTop = UI.
                    findViewById(layout, R.id.tv_article_title_top);

            holder.articlePhotoTop = UI.findViewById(layout, R.id.photo_top);

            holder.magazineLikeTop = UI.findViewById(layout, R.id.cb_magazine_like_top);

            holder.magazineAddTop = UI.findViewById(layout, R.id.imv_magazine_add_top);

            holder.magazineShareTop = UI.findViewById(layout, R.id.imv_magazine_share_top);

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

            /*if (!BuildConfig.NEW_FOLLOW_MORE_TOPICS) {
                holder.lvSuggestions = (ListView) layout.findViewById(R.id.lv_suggestions);
            } else {*/
                holder.rvSuggestions = (RecyclerView) layout.findViewById(R.id.rv_suggestions);
                holder.doneButton = (Button) layout.findViewById(R.id.btn_done);
                holder.noSuggestionsTextView = (TextView) layout.findViewById(R.id.no_suggestions);
            //}
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

        mMagazinesServicesUsecase.handleArticleLike(position, holder.magazineLike, data, this, context, mToastFactory);

        mMagazinesServicesUsecase.handleArticleLike(position, holder.fullImageMagazineLike, data, this, context, mToastFactory);

        mMagazinesServicesUsecase.handleArticleImage(position, holder, holder.articlePhoto, data, context);

/*<<<<<<< HEAD
        if (holder.articlePhoto != null) {
            final ImageView photoView = holder.articlePhoto;

            photoView.setImageResource(R.drawable.magazine_backdrop);
            if (data.getS3_image_filename() != null) {
                mMagazinesServicesUsecase.handleImageLoading(holder, context, data, photoView);
            } else {
                photoView.setImageResource(R.drawable.magazine_backdrop);
            }
=======*/
        mMagazinesServicesUsecase.handleArticleAdd(holder.magazineAdd, data, context);
//>>>>>>> 4056593f7dcc1b6568a3d53e115d921d4309687c

        mMagazinesServicesUsecase.handleArticleShare(holder.magazineShare, data);

        mMagazinesServicesUsecase.handleArticleAdd(holder.fullImageMagazineAdd, data, context);

        mMagazinesServicesUsecase.handleArticleShare(holder.fullImageMagazineShare, data);

        LinearLayout llArticleInfo = (LinearLayout) layout.findViewById(R.id.ll_article_info);
        if (llArticleInfo != null) {
            llArticleInfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mMagazinesServicesUsecase.navigateToArticleWebView(context, data, position);
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
                        mMagazinesServicesUsecase.navigateToTopicDetails(magazineFlipArticlesFragment, context, data, position);
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
            mMagazinesServicesUsecase.populateEmptyLeftArticle(holder);
        }

        if (allArticles.size() >= 3) {
            Articles thirdData = thirdArticle;
            populateRightArticle(holder, thirdData, position);
        } else {
            mMagazinesServicesUsecase.populateEmptyRightArticle(holder);
        }
        /*if (!BuildConfig.NEW_FOLLOW_MORE_TOPICS) {
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
        } else {*/

            if (allArticles.size() >= 4 && MagazinesFragment.newCategoriesList.size() > 0) {
                if (holder.rvSuggestions != null) {
                    ArrayList<Categories> addFourCategoriesList = new ArrayList<>();
                    for (Categories categories : MagazinesFragment.newCategoriesList) {
                        if (addFourCategoriesList.size() < 4 && categories.getTags() != null && categories.getTags().size() > 0) {
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
        //}
        if (holder.tvFollowMoreTopics != null) {
            holder.tvFollowMoreTopics.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent;
                    /*if (!BuildConfig.NEW_FOLLOW_MORE_TOPICS) {
                        intent = new Intent(context, FollowMoreTopicsActivity.class);
                    } else {*/
                        intent = new Intent(context, NewFollowMoreTopicsActivity.class);
                    //}
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
       mMagazinesServicesUsecase.autoReflectStatus(data, type, allArticles, context, this);
    }

    @Override
    public void updateMagazineStatus(Articles data, String follow) {
        mMagazinesServicesUsecase.autoReflectStatus(data, follow, allArticles, context, this);
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

        if (holder.articleTitleTop != null) {
            holder.articleTitleTop
                    .setText(AphidLog.format("%s", data.getTitle()));
        }

        mMagazinesServicesUsecase.handleArticleLike(position, holder.magazineLikeTop, data, this, context, mToastFactory);

        mMagazinesServicesUsecase.handleArticleImage(position, holder, holder.articlePhotoTop, data, context);

        mMagazinesServicesUsecase.handleArticleAdd(holder.magazineAddTop, data, context);

        mMagazinesServicesUsecase.handleArticleShare(holder.magazineShareTop, data);

        Button followMoreTopics = (Button) layout.findViewById(R.id.btn_magazine_follow_topics_top);
        if (followMoreTopics != null) {
            followMoreTopics.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent;
                    /*if (!BuildConfig.NEW_FOLLOW_MORE_TOPICS) {
                        intent = new Intent(context, FollowMoreTopicsActivity.class);
                    } else {*/
                        intent = new Intent(context, NewFollowMoreTopicsActivity.class);
                    //}
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
                        mMagazinesServicesUsecase.navigateToTopicDetails(magazineFlipArticlesFragment, context, data, position);
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
            //holder.magazineLikeLeft.setTag(position);
        }

        if (holder.articleTitleLeft != null) {
            holder.articleTitleLeft.setVisibility(View.VISIBLE);
            holder.articleTitleLeft
                    .setText(AphidLog.format("%s", data.getTitle()));
            holder.articleTitleLeft.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mMagazinesServicesUsecase.navigateFromLeftRightArticleToWebView(magazineFlipArticlesFragment, context, data, position, "left");
                }
            });
        }

        mMagazinesServicesUsecase.handleArticleLike(position, holder.magazineLikeLeft, data, this, context, mToastFactory);

        if (holder.articlePhotoLeft != null) {
            final ImageView photoView = holder.articlePhotoLeft;
            photoView.setVisibility(View.VISIBLE);
            photoView.setImageResource(R.drawable.magazine_backdrop);
            if (data.getImage_filename() != null) {
                mMagazinesServicesUsecase.loadImageFromS3(context, data, photoView);
            } else {
                photoView.setImageResource(R.drawable.magazine_backdrop);
            }

            photoView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mMagazinesServicesUsecase.navigateFromLeftRightArticleToWebView(magazineFlipArticlesFragment, context, data, position, "left");
                }
            });
        }

        if (holder.magazineAddLeft != null) {
            ImageView add = holder.magazineAddLeft;
            add.setVisibility(View.VISIBLE);
            add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mMagazinesServicesUsecase.onAddClick(context, data);
                }
            });
        }

        if (holder.magazineShareLeft != null) {
            ImageView share = holder.magazineShareLeft;
            share.setVisibility(View.VISIBLE);
            share.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mMagazinesServicesUsecase.onShareClick(v, data);
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
                        mMagazinesServicesUsecase.navigateFromLeftRightToTopicsDetail(magazineFlipArticlesFragment, context, data, position, "left");
                    }
                });
            } else {
                holder.tvTopicNameLeft.setVisibility(View.GONE);
            }
        }

        mMagazinesServicesUsecase.displayLeftRightSummaryBasedOnDensity(holder.articleSummaryLeft, data, context);

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
            //holder.magazineLikeRight.setTag(position);
        }

        if (holder.articleTitleRight != null) {
            holder.articleTitleRight.setVisibility(View.VISIBLE);
            holder.articleTitleRight
                    .setText(AphidLog.format("%s", data.getTitle()));
            holder.articleTitleRight.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mMagazinesServicesUsecase.navigateFromLeftRightArticleToWebView(magazineFlipArticlesFragment, context, data, position, "right");
                }
            });
        }

        mMagazinesServicesUsecase.handleArticleLike(position, holder.magazineLikeRight, data, this, context, mToastFactory);

        if (holder.articlePhotoRight != null) {
            final ImageView photoView = holder.articlePhotoRight;
            photoView.setVisibility(View.VISIBLE);
            photoView.setImageResource(R.drawable.magazine_backdrop);
            if (data.getImage_filename() != null) {
                mMagazinesServicesUsecase.loadImageFromS3(context, data, photoView);
            } else {
                photoView.setImageResource(R.drawable.magazine_backdrop);
            }

            photoView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mMagazinesServicesUsecase.navigateFromLeftRightArticleToWebView(magazineFlipArticlesFragment, context, data, position, "right");
                }
            });
        }

        if (holder.magazineAddRight != null) {
            ImageView add = holder.magazineAddRight;
            add.setVisibility(View.VISIBLE);
            add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mMagazinesServicesUsecase.onAddClick(context, data);
                }
            });
        }

        if (holder.magazineShareRight != null) {
            ImageView share = holder.magazineShareRight;
            share.setVisibility(View.VISIBLE);
            share.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mMagazinesServicesUsecase.onShareClick(v, data);
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
                        mMagazinesServicesUsecase.navigateFromLeftRightToTopicsDetail(magazineFlipArticlesFragment, context, data, position, "right");
                    }
                });
            } else {
                holder.tvTopicNameRight.setVisibility(View.GONE);
            }
        }

        mMagazinesServicesUsecase.displayLeftRightSummaryBasedOnDensity(holder.articleSummaryRight, data, context);

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

                    List<Articles> cachedMagazinesList = mMagazinesServicesUsecase.getCachedMagazinesList(context);

                    if (cachedMagazinesList != null) {
                        List<Articles> tempList = cachedMagazinesList;
                        for (int i = 0; i < cachedMagazinesList.size(); i++) {
                            if (data.getTopicId().equals(tempList.get(i).getTopicId())) {
                                tempList.get(i).setTopicFollowing(data.getTopicFollowing());
                            }
                        }
                        cachedMagazinesList = tempList;

                        mMagazinesServicesUsecase.saveCachedMagazinesList(cachedMagazinesList, context);
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
                if (article != null) {
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
                mMagazinesServicesUsecase.loadArticles(null, false, context, magazineFlipArticlesFragment);
            }

            List<Articles> cachedMagazinesList = mMagazinesServicesUsecase.getCachedMagazinesList(context);

            if (cachedMagazinesList != null) {
                List<Articles> tempList = new ArrayList<>();
                for (int i = 0; i < cachedMagazinesList.size(); i++) {
                    if (topicId.equals(cachedMagazinesList.get(i).getTopicId())) {
                        cachedMagazinesList.get(i).setTopicFollowing("false");
                        tempList.add(cachedMagazinesList.get(i));
                    }
                }
                cachedMagazinesList.removeAll(tempList);

                mMagazinesServicesUsecase.saveCachedMagazinesList(cachedMagazinesList, context);
            }
        }

    }

    /**
     * The View Holder class
     */
    public static class ViewHolder {

        public TextView articleTitle;

        public TextView articleShortDesc;

        private ImageView articlePhoto;

        private CheckBox magazineLike;

        private ImageView magazineAdd;

        private ImageView magazineShare;

        private TextView articleTitleTop;

        private ImageView articlePhotoTop;

        private CheckBox magazineLikeTop;

        private ImageView magazineAddTop;

        private ImageView magazineShareTop;

        public TextView articleTitleLeft;

        public ImageView articlePhotoLeft;

        public CheckBox magazineLikeLeft;

        public ImageView magazineAddLeft;

        public ImageView magazineShareLeft;

        public Button articleFollowLeft;

        public TextView articleTitleRight;

        public ImageView articlePhotoRight;

        public CheckBox magazineLikeRight;

        public ImageView magazineAddRight;

        public ImageView magazineShareRight;

        public Button articleFollowRight;

        private ListView lvSuggestions;

        private RecyclerView rvSuggestions;

        private TextView noSuggestionsTextView;

        private TextView tvFollowMoreTopics;

        private TextView tvTopicName;

        private TextView tvTopicNameTop;

        public TextView tvTopicNameLeft;

        public TextView tvTopicNameRight;

        private TextView articleSummaryLeft;

        private TextView articleSummaryRight;

        public TextView fullImageTitle;

        public ImageView blackMask;

        public RelativeLayout rlFullImageOptions;

        private CheckBox fullImageMagazineLike;

        private ImageView fullImageMagazineAdd;

        private ImageView fullImageMagazineShare;

        private Button doneButton;
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
            mAddTopicsUsecase.addTopics(followedTopicsIdsList, "random_topics", new ApiCallback<List<Categories>>() {
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