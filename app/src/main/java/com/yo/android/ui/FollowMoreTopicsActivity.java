package com.yo.android.ui;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.SearchView;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cunoraz.tagview.Tag;
import com.cunoraz.tagview.TagView;
import com.cunoraz.tagview.Utils;
import com.google.gson.Gson;
import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.R;
import com.yo.android.api.YoApi;
import com.yo.android.helpers.TagLoader;
import com.yo.android.helpers.TagSelected;
import com.yo.android.model.Articles;
import com.yo.android.model.Categories;
import com.yo.android.model.Topics;
import com.yo.android.sectionheaders.CategorizedList;
import com.yo.android.sectionheaders.Section;
import com.yo.android.util.Constants;
import com.yo.android.util.Util;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.Bind;
import butterknife.ButterKnife;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FollowMoreTopicsActivity extends BaseActivity {

    private static final int OPEN_ADD_BALANCE_RESULT = 1000;
    private static int TAB_GREY;
    private static int WHITE;
    private static int COLOR_PRIMARY;

    @Inject
    YoApi.YoService yoService;
    @Inject
    @Named("login")
    protected PreferenceEndPoint preferenceEndPoint;


    @Bind(R.id.no_search_results)
    TextView noSearchResults;
    @Bind(R.id.btn_done)
    Button done;
    @Bind(R.id.hello_interests)
    TextView tvHelloInterests;
    @Bind(R.id.pick_topics)
    TextView tvPickTopics;
    @Bind(R.id.listView)
    ListView listView;
    @Bind(R.id.tagsparent)
    LinearLayout tagsParentLayout;
    @Bind(R.id.btn_skip)
    Button skip;
    @Bind(R.id.bottom)
    LinearLayout bottomLayout;

    private List<String> followedTopicsIdsList;
    private int lineMargin;
    private int tagMargin;
    private int textPaddingLeft;
    private int textPaddingRight;
    private int textPaddingTop;
    private int texPaddingBottom;

    private String from;
    private List<Topics> topicsList;
    private List<String> addedTopics;
    public ArrayList<Tag> initialTags;
    private ArrayList<Tag> searchTags;
    private boolean isInvalidSearch;
    private CategorizedList categorisedList;
    private List<Categories> serverTopics;
    private TagView tagViewAdapter;
    private LayoutInflater layoutInflater;
    private ArrayList<Tag> worldpopulationlist = null;
    private ArrayList<Tag> arraylist = null;
    private boolean isSkipClicked;


    public interface TagsLoader {
        void loaded();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow_more_topics);
        ButterKnife.bind(this);
        String title = "Follow more topics";

        getSupportActionBar().setTitle(title);

        Intent intent = getIntent();
        from = intent.getStringExtra("From");
        layoutInflater = LayoutInflater.from(this);

        boolean backBtn = true;
        if ("UpdateProfileActivity".equals(from) || preferenceEndPoint.getBooleanPreference(Constants.ENABLE_FOLLOW_TOPICS_SCREEN)) {
            backBtn = false;
        }
        getSupportActionBar().setHomeButtonEnabled(backBtn);
        getSupportActionBar().setDisplayHomeAsUpEnabled(backBtn);

        View customView = getLayoutInflater().inflate(R.layout.section_list_item, null);
        tagViewAdapter = (TagView) customView.findViewById(R.id.tag_group);

        initialTags = new ArrayList<>();
        topicsList = new ArrayList<Topics>();
        addedTopics = new ArrayList<String>();

        TAB_GREY = getResources().getColor(R.color.tab_grey);
        WHITE = getResources().getColor(R.color.white);
        COLOR_PRIMARY = getResources().getColor(R.color.colorPrimary);

        if (preferenceEndPoint.getBooleanPreference(Constants.ENABLE_FOLLOW_TOPICS_SCREEN)) {
            skip.setVisibility(View.VISIBLE);
        } else {
            skip.setVisibility(View.GONE);
        }

        String accessToken = preferenceEndPoint.getStringPreference("access_token");
        showProgressDialog();
        serverTopics = new ArrayList<>();

        yoService.categoriesAPI(accessToken).enqueue(new Callback<List<Categories>>() {
            @Override
            public void onResponse(Call<List<Categories>> call, Response<List<Categories>> response) {
                if (response == null || response.body() == null) {
                    dismissProgressDialog();
                    return;
                }

                serverTopics = response.body();

                categorisedList = new CategorizedList(FollowMoreTopicsActivity.this, listView, initialTags, serverTopics);
                new TagLoader(FollowMoreTopicsActivity.this, new TagsLoader() {
                    @Override
                    public void loaded() {
                        worldpopulationlist = new ArrayList<Tag>(initialTags);
                        arraylist = new ArrayList<Tag>(initialTags);
                    }
                }, serverTopics, tagViewAdapter, initialTags, categorisedList).execute();

            }

            @Override
            public void onFailure(Call<List<Categories>> call, Throwable t) {
                dismissProgressDialog();
            }
        });

        followedTopicsIdsList = new ArrayList<String>();
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performDoneAction(followedTopicsIdsList);
                done.setEnabled(false);
                isSkipClicked = false;
            }
        });

        //set delete listener
        tagViewAdapter.setOnTagDeleteListener(new TagView.OnTagDeleteListener() {
            @Override
            public void onTagDeleted(final TagView view, final Tag tag, final int position) {
                // do nothing
            }
        });

        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FollowMoreTopicsActivity.this, TabsHeaderActivity.class);
                intent.putExtra(Constants.OPEN_ADD_BALANCE, true);
                startActivityForResult(intent, OPEN_ADD_BALANCE_RESULT);
                skip.setEnabled(false);
                isSkipClicked = true;
            }
        });


        TypedArray typeArray = obtainStyledAttributes(null, com.cunoraz.tagview.R.styleable.TagView, 0, 0);
        this.lineMargin = (int) typeArray.getDimension(com.cunoraz.tagview.R.styleable.TagView_lineMargin, Utils.dipToPx(this, com.cunoraz.tagview.Constants.DEFAULT_LINE_MARGIN));
        this.tagMargin = (int) typeArray.getDimension(com.cunoraz.tagview.R.styleable.TagView_tagMargin, Utils.dipToPx(this, com.cunoraz.tagview.Constants.DEFAULT_TAG_MARGIN));
        this.textPaddingLeft = (int) typeArray.getDimension(com.cunoraz.tagview.R.styleable.TagView_textPaddingLeft, Utils.dipToPx(this, com.cunoraz.tagview.Constants.DEFAULT_TAG_TEXT_PADDING_LEFT));
        this.textPaddingRight = (int) typeArray.getDimension(com.cunoraz.tagview.R.styleable.TagView_textPaddingRight, Utils.dipToPx(this, com.cunoraz.tagview.Constants.DEFAULT_TAG_TEXT_PADDING_RIGHT));
        this.textPaddingTop = (int) typeArray.getDimension(com.cunoraz.tagview.R.styleable.TagView_textPaddingTop, Utils.dipToPx(this, com.cunoraz.tagview.Constants.DEFAULT_TAG_TEXT_PADDING_TOP));
        this.texPaddingBottom = (int) typeArray.getDimension(com.cunoraz.tagview.R.styleable.TagView_textPaddingBottom, Utils.dipToPx(this, com.cunoraz.tagview.Constants.DEFAULT_TAG_TEXT_PADDING_BOTTOM));
        typeArray.recycle();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {

        View v = getCurrentFocus();
        boolean ret = super.dispatchTouchEvent(event);

        if (v instanceof SearchView.SearchAutoComplete) {
            View w = getCurrentFocus();
            int scrcoords[] = new int[2];
            w.getLocationOnScreen(scrcoords);
            float x = event.getRawX() + w.getLeft() - scrcoords[0];
            float y = event.getRawY() + w.getTop() - scrcoords[1];

            Log.d("FollowMoreTopics", "Touch event " + event.getRawX() + "," + event.getRawY() + " " + x + "," + y + " rect " + w.getLeft() + "," + w.getTop() + "," + w.getRight() + "," + w.getBottom() + " coords " + scrcoords[0] + "," + scrcoords[1]);
            if (event.getAction() == MotionEvent.ACTION_UP && (x < w.getLeft() || x >= w.getRight() || y < w.getTop() || y > w.getBottom())) {

                if (this != null) {
                    Util.hideKeyboard(FollowMoreTopicsActivity.this, getCurrentFocus());
                }
            }
        }
        return ret;
    }

    private void performDoneAction(final List<String> followedTopicsIdsList) {

        if (preferenceEndPoint.getBooleanPreference(Constants.ENABLE_FOLLOW_TOPICS_SCREEN)) {
            Intent intent = new Intent(FollowMoreTopicsActivity.this, TabsHeaderActivity.class);
            intent.putExtra(Constants.OPEN_ADD_BALANCE, true);
            startActivityForResult(intent, OPEN_ADD_BALANCE_RESULT);
        } else {
            if (searchTags != null && !searchTags.isEmpty()) {
                for (int i = 0; i < initialTags.size(); i++) {
                    for (int j = 0; j < searchTags.size(); j++) {
                        TagSelected initialSel = (TagSelected) initialTags.get(i);
                        TagSelected searchSel = (TagSelected) searchTags.get(j);
                        if (initialSel.getTagId().equals(searchSel.getTagId())) {
                            initialTags.remove(initialSel);
                            initialTags.add(searchSel);
                        }
                    }
                }
            }

            tagViewAdapter.addTags(initialTags);
            for (int k = 0; k < tagViewAdapter.getTags().size(); k++) {
                TagSelected t = (TagSelected) tagViewAdapter.getTags().get(k);
                if (t.getSelected()) {

                    followedTopicsIdsList.add(String.valueOf(t.getTagId()));

                }

            }
            showProgressDialog();
            String accessToken = preferenceEndPoint.getStringPreference("access_token");
            yoService.addTopicsAPI(accessToken, followedTopicsIdsList).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    dismissProgressDialog();
                    if ("Magazines".equals(from)) {
                        Intent myCollectionsIntent = new Intent(FollowMoreTopicsActivity.this, MyCollections.class);
                        startActivity(myCollectionsIntent);
                        finish();
                    } else if (preferenceEndPoint.getBooleanPreference(Constants.ENABLE_FOLLOW_TOPICS_SCREEN)) {
                        //TODO:Disalbe flag for Follow more
                        preferenceEndPoint.saveBooleanPreference(Constants.ENABLE_FOLLOW_TOPICS_SCREEN, false);
                        String accessToken = preferenceEndPoint.getStringPreference("access_token");
                        yoService.getArticlesWithPaginationAPI(accessToken, 1, 200);
                    } else {
                        Intent intent = new Intent();
                        setResult(2, intent);
                        finish();
                    }

                    preferenceEndPoint.saveStringPreference("magazine_tags", TextUtils.join(",", followedTopicsIdsList));
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    dismissProgressDialog();
                    Toast.makeText(FollowMoreTopicsActivity.this, "Error while adding topics", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    @NonNull
    public TagSelected prepareTag(Topics topics) {
        TagSelected tag = new TagSelected(topics.getName());
        tag.radius = 1f;
        tag.setTagId(topics.getId());
        tag.layoutBorderColor = getResources().getColor(R.color.tab_grey);
        tag.layoutBorderSize = 1f;
        tag.layoutColor = getResources().getColor(android.R.color.white);
        tag.tagTextColor = getResources().getColor(R.color.tab_grey);
        tag.tagTextSize = 12;

        if (topics.isSelected()) {
            tag.setSelected(true);
            tag.layoutColor = getResources().getColor(R.color.colorPrimary);
            tag.tagTextColor = getResources().getColor(android.R.color.white);
            tag.setSelected(true);
            addedTopics.add(tag.text);
        } else {
            tag.layoutColor = getResources().getColor(android.R.color.white);
            tag.tagTextColor = getResources().getColor(R.color.tab_grey);
            tag.setSelected(false);
        }
        return tag;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search, menu);
        prepareTagSearch(this, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public void prepareTagSearch(Activity activity, Menu menu) {
        final SearchManager searchManager =
                (SearchManager) activity.getSystemService(Context.SEARCH_SERVICE);
        MenuItem searchMenuItem;
        SearchView searchView;

        searchMenuItem = menu.findItem(R.id.menu_search);
        searchView =
                (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setQueryHint(Html.fromHtml("<font color = #88FFFFFF>" + "Search...." + "</font>"));
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(activity.getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            public static final String TAG = "PrepareSearch in Topics";

            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.i(TAG, "onQueryTextSubmit: " + query);
                if (this != null) {
                    Util.hideKeyboard(FollowMoreTopicsActivity.this, getCurrentFocus());
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.i(TAG, "onQueryTextChange: " + newText);
                try {
                    setTags(newText);
                } catch (Exception e) {

                }
                return true;
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                if (this != null) {
                    Util.hideKeyboard(FollowMoreTopicsActivity.this, getCurrentFocus());
                }
                return true;
            }
        });
    }

    //Started new search
    private boolean isTestSearch = true;
    private static TagView totalTagsInView;

    private void filterTags(CharSequence searchText) {
        noSearchResults.setVisibility(View.GONE);
        tvHelloInterests.setVisibility(View.GONE);
        tvPickTopics.setVisibility(View.GONE);
        listView.setVisibility(View.GONE);
        tagsParentLayout.setVisibility(View.VISIBLE);
        bottomLayout.setVisibility(View.GONE);

        if (TextUtils.isEmpty(searchText.toString().trim())) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            showProgressDialog();
            listView.setAdapter(null);
            categorisedList = new CategorizedList(FollowMoreTopicsActivity.this, listView, initialTags, serverTopics);
            new TagLoader(FollowMoreTopicsActivity.this, new TagsLoader() {
                @Override
                public void loaded() {
                    worldpopulationlist = new ArrayList<Tag>(initialTags);
                    arraylist = new ArrayList<Tag>(initialTags);
                }
            }, serverTopics, tagViewAdapter, initialTags, categorisedList).execute();
            listView.setVisibility(View.VISIBLE);
            bottomLayout.setVisibility(View.VISIBLE);
            tagsParentLayout.setVisibility(View.GONE);
            tvHelloInterests.setVisibility(View.VISIBLE);
            tvPickTopics.setVisibility(View.VISIBLE);
        } else {
            listView.setVisibility(View.GONE);
            bottomLayout.setVisibility(View.GONE);
            tvHelloInterests.setVisibility(View.GONE);
            tvPickTopics.setVisibility(View.GONE);
            tagsParentLayout.setVisibility(View.VISIBLE);
            searchText = searchText.toString().toLowerCase(Locale.getDefault());
            final CharSequence searchTextTemp = searchText;
            new AsyncTask<CharSequence, Void, ArrayList<Tag>>() {

                @Override
                protected ArrayList<Tag> doInBackground(CharSequence... search) {
                    worldpopulationlist.clear();
                    for (Categories category : serverTopics) {
                        String categoryName = category.getName();
                        if (categoryName.toLowerCase(Locale.getDefault()).contains(searchTextTemp)) {
                            for (Topics topic : category.getTags()) {
                                for (Tag tag : arraylist) {
                                    if (tag.getText().equals(topic.getName())) {
                                        worldpopulationlist.add(tag);
                                    }
                                }
                            }

                        }
                    }

                    for (Tag wp : arraylist) {
                        if (wp.getText().toLowerCase(Locale.getDefault()).contains(search[0])) {
                            worldpopulationlist.add(wp);
                        }
                    }
                    LinkedHashSet<Tag> hashSet = new LinkedHashSet<>();
                    hashSet.addAll(worldpopulationlist);
                    worldpopulationlist = new ArrayList<Tag>(hashSet);
                    return worldpopulationlist;
                }

                @Override
                protected void onPostExecute(ArrayList<Tag> aVoid) {
                    super.onPostExecute(aVoid);
                    tagsParentLayout.removeAllViews();
                    tagsParentLayout.addView(createTags(worldpopulationlist));
                    if (worldpopulationlist.size() == 0) {
                        noSearchResults.setVisibility(View.VISIBLE);
                        tvHelloInterests.setVisibility(View.GONE);
                        bottomLayout.setVisibility(View.GONE);
                        tvPickTopics.setVisibility(View.GONE);
                        isInvalidSearch = true;
                    } else {
                        noSearchResults.setVisibility(View.GONE);
                        bottomLayout.setVisibility(View.VISIBLE);
                        tvHelloInterests.setVisibility(View.VISIBLE);
                        tvPickTopics.setVisibility(View.VISIBLE);
                        isInvalidSearch = false;
                    }
                    onTagSearchClickEvent();
                }
            }.execute(searchText);
        }
    }


    private TagView createTags(List<Tag> tags) {
        TagView view = (TagView) layoutInflater.inflate(R.layout.section_list_item, null);
        TagView tv = (TagView) view.findViewById(R.id.tag_group);
        tv.addTags(tags);
        return view;
    }
    //Ended new search

    private void setTags(CharSequence cs) {

        if (isTestSearch) {
            Log.e("Tag", "search test : " + isTestSearch);
            filterTags(cs);
        } else {
            if (TextUtils.isEmpty(cs.toString().trim())) {
                Log.e("Tag", "search test : " + isTestSearch + " if");
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                noSearchResults.setVisibility(View.GONE);
                tvHelloInterests.setVisibility(View.VISIBLE);
                tvPickTopics.setVisibility(View.VISIBLE);
                listView.setVisibility(View.VISIBLE);
                for (Categories categories : serverTopics) {
                    topicsList.addAll(categories.getTags());
                }
                return;
            } else {
                Log.e("Tag", "search test : " + isTestSearch + " else");
            }


            String text = cs.toString();
            searchTags = new ArrayList<Tag>();
            for (Tag tag : initialTags) {
                if (TextUtils.isEmpty(text) || tag.text.toLowerCase().contains(text.toLowerCase())) {
                    searchTags.add(tag);
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OPEN_ADD_BALANCE_RESULT && resultCode == RESULT_OK) {
            if (!isSkipClicked) {
                if (searchTags != null && !searchTags.isEmpty()) {
                    for (int i = 0; i < initialTags.size(); i++) {
                        for (int j = 0; j < searchTags.size(); j++) {
                            TagSelected initialSel = (TagSelected) initialTags.get(i);
                            TagSelected searchSel = (TagSelected) searchTags.get(j);
                            if (initialSel.getTagId().equals(searchSel.getTagId())) {
                                initialTags.remove(initialSel);
                                initialTags.add(searchSel);
                            }
                        }
                    }
                }
                if (tagViewAdapter.getVisibility() == View.VISIBLE) {
                    tagViewAdapter.addTags(initialTags);
                    for (int k = 0; k < tagViewAdapter.getTags().size(); k++) {
                        TagSelected t = (TagSelected) tagViewAdapter.getTags().get(k);
                        if (t.getSelected()) {
                            followedTopicsIdsList.add(String.valueOf(t.getTagId()));
                        }
                    }
                }
            }
            navigation();

        } else if (requestCode == OPEN_ADD_BALANCE_RESULT && resultCode == RESULT_CANCELED) {
            done.setEnabled(true);
            skip.setEnabled(true);
            isSkipClicked = false;
        }
    }

    private Callback<List<Articles>> callback = new Callback<List<Articles>>() {
        @Override
        public void onResponse(Call<List<Articles>> call, Response<List<Articles>> response) {

            if (response.body() != null && !response.body().isEmpty()) {
                if (!TextUtils.isEmpty(preferenceEndPoint.getStringPreference("cached_magazines"))) {
                    preferenceEndPoint.removePreference("cached_magazines");
                }
                preferenceEndPoint.saveStringPreference("cached_magazines", new Gson().toJson(response.body()));
                navigation();

            } else {
                navigation();
            }

        }

        @Override
        public void onFailure(Call<List<Articles>> call, Throwable t) {
            if (t instanceof UnknownHostException) {
                mLog.e("Magazine", "Please check network settings");
            }
        }

    };

    private void navigation() {
        Intent myCollectionsIntent = new Intent(FollowMoreTopicsActivity.this, BottomTabsActivity.class);
        myCollectionsIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        ArrayList<String> tagIds = new ArrayList<String>(followedTopicsIdsList);
        myCollectionsIntent.putStringArrayListExtra("tagIds", tagIds);
        startActivity(myCollectionsIntent);
        finish();
    }

    public void onClickingTag(Tag mTag, int position) {
        Log.d("FollowMoreTopics", "Tag " + mTag + " position " + position);
        try {
            TagSelected tag = (TagSelected) mTag;
            tag.toggleSelection();

            String tagId1 = tag.getTagId();
            List<Section> listItemsWithHeaders = categorisedList.getCategoryAdapter().listItemsWithHeaders;
            TagView tView = null;
            for (int i = 0; i < listItemsWithHeaders.size(); i++) {
                if (listItemsWithHeaders.get(i).getLayoutId() == R.layout.section_list_item) {
                    List<Tag> tagsList = listItemsWithHeaders.get(i).getSectionItem().getCategoryItem().getTags();
                    for (int j = 0; j < tagsList.size(); j++) {
                        TagSelected initialSel = (TagSelected) tagsList.get(j);
                        if (initialSel.getTagId().equals(tagId1)) {
                            tView = categorisedList.getCategoryAdapter().getItem(i).getSectionItem().getCategoryItem();
                        }
                    }
                }
            }

            if (tView != null) {
                Log.d("FollowMoreTopics", "TagView is " + tView);
                if (!tag.getSelected()) {
                    addedTopics.remove(tag);
                    String tagId = tag.getTagId();
                    for (Categories categories : serverTopics) {
                        topicsList.addAll(categories.getTags());
                    }
                    for (Topics topics : topicsList) {
                        if (topics.getId().equals(tagId)) {
                            topics.setSelected(false);
                            break;
                        }
                    }

                    Tag unselectedTag = tView.getTags().get(position);
                    unselectedTag.layoutBorderColor = TAB_GREY;
                    unselectedTag.layoutColor = WHITE;
                    unselectedTag.tagTextColor = TAB_GREY;
                    unselectedTag.tagTextSize = 12;
                    tView.updateTag(unselectedTag);


                } else {
                    addedTopics.add(tag.text);
                    String tagId = tag.getTagId();
                    for (Categories categories : serverTopics) {
                        topicsList.addAll(categories.getTags());
                    }
                    for (Topics topics : topicsList) {
                        if (topics.getId().equals(tagId)) {
                            topics.setSelected(true);
                            break;
                        }
                    }
                    Tag selectedTag = tView.getTags().get(position);
                    selectedTag.layoutBorderColor = WHITE;
                    selectedTag.layoutColor = COLOR_PRIMARY;
                    selectedTag.tagTextColor = WHITE;
                    selectedTag.tagTextSize = 12;
                    tView.updateTag(selectedTag);
                }
            }

            Tag tagDummy = new Tag("Android");
            tagDummy.radius = 1f;
            tagDummy.layoutBorderColor = TAB_GREY;
            tagDummy.layoutBorderSize = 1f;
            tagDummy.layoutColor = WHITE;
            tagDummy.tagTextColor = TAB_GREY;
            tagDummy.tagTextSize = 12;
        } catch (Exception e) {
            mLog.e("TAGS", "Exception" + e);
        }
    }

    private void onTagSearchClickEvent() {
        if (tagsParentLayout.getChildCount() > 0 && tagsParentLayout.getChildAt(0) instanceof TagView) {
            Log.d("FollowMoreTopics", "getChildCount() " + tagsParentLayout.getChildCount() + " getChildAt(0) " + tagsParentLayout.getChildAt(0));
            ((TagView) tagsParentLayout.getChildAt(0)).setOnTagClickListener(new TagView.OnTagClickListener() {
                @Override
                public void onTagClick(Tag mTag, int position) {
                    try {
                        TagSelected tag = (TagSelected) mTag;
                        tag.toggleSelection();
                        if (!tag.getSelected()) {
                            addedTopics.remove(tag);
                            String tagId = tag.getTagId();

                            for (Topics topics : topicsList) {
                                if (topics.getId().equals(tagId)) {
                                    topics.setSelected(false);
                                    break;
                                }
                            }

                            Tag unselectedTag = ((TagView) tagsParentLayout.getChildAt(0)).getTags().get(position);
                            unselectedTag.layoutBorderColor = TAB_GREY;
                            unselectedTag.layoutColor = WHITE;
                            unselectedTag.tagTextColor = TAB_GREY;
                            unselectedTag.tagTextSize = 12;
                            ((TagView) tagsParentLayout.getChildAt(0)).updateTag(unselectedTag);
                        } else {
                            addedTopics.add(tag.text);
                            String tagId = tag.getTagId();

                            for (Topics topics : topicsList) {
                                if (topics.getId().equals(tagId)) {
                                    topics.setSelected(true);
                                    break;
                                }
                            }
                            Tag selectedTag = ((TagView) tagsParentLayout.getChildAt(0)).getTags().get(position);
                            selectedTag.layoutBorderColor = WHITE;
                            selectedTag.layoutColor = COLOR_PRIMARY;
                            selectedTag.tagTextColor = WHITE;
                            selectedTag.tagTextSize = 12;
                            ((TagView) tagsParentLayout.getChildAt(0)).updateTag(selectedTag);

                        }

                        Tag tagDummy = new Tag("Android");
                        tagDummy.radius = 1f;
                        tagDummy.layoutBorderColor = TAB_GREY;
                        tagDummy.layoutBorderSize = 1f;
                        tagDummy.layoutColor = WHITE;
                        tagDummy.tagTextColor = TAB_GREY;
                        tagDummy.tagTextSize = 12;

                        String tagId = tag.getTagId();
                        boolean isBreak = false;
                        for (int i = 0; i < listView.getAdapter().getCount(); i++) {
                            Section section = (Section) listView.getAdapter().getItem(i);
                            if (section.getLayoutId() == R.layout.section_list_item) {
                                TagView tagView = section.getSectionItem().getCategoryItem();
                                List<Tag> tagsList = tagView.getTags();
                                for (int j = 0; j < tagsList.size(); j++) {
                                    TagSelected initialSel = (TagSelected) tagsList.get(j);
                                    if (initialSel.getTagId().equals(tagId)) {
                                        onUpdatingClickedTag(tag, i);
                                        isBreak = true;
                                        break;
                                    }
                                }
                                if (isBreak) {
                                    break;
                                }
                            }
                        }

                    } catch (Exception e) {
                        mLog.e("TAGS", "Exception" + e);
                    }

                }
            });
        }
    }

    private void onUpdatingClickedTag(Tag mTag, int position) {
        Log.d("FollowMoreTopics", "Tag " + mTag + " position " + position);
        try {
            TagSelected tag = (TagSelected) mTag;
            TagView tView = categorisedList.getCategoryAdapter().getItem(position).getSectionItem().getCategoryItem();
            Log.d("FollowMoreTopics", "TagView is " + tView);
            if (!tag.getSelected()) {
                addedTopics.remove(tag);
                String tagId = tag.getTagId();
                for (Categories categories : serverTopics) {
                    topicsList.addAll(categories.getTags());
                }
                for (Topics topics : topicsList) {
                    if (topics.getId().equals(tagId)) {
                        topics.setSelected(false);
                        break;
                    }
                }

                for (int j = 0; j < tView.getTags().size(); j++) {
                    TagSelected initialSel = (TagSelected) tView.getTags().get(j);
                    if (initialSel.getTagId().equals(tagId)) {
                        Tag unselectedTag = tView.getTags().get(j);
                        unselectedTag.layoutBorderColor = TAB_GREY;
                        unselectedTag.layoutColor = WHITE;
                        unselectedTag.tagTextColor = TAB_GREY;
                        unselectedTag.tagTextSize = 12;
                        tView.updateTag(unselectedTag);
                        break;
                    }
                }
            } else {
                addedTopics.add(tag.text);
                String tagId = tag.getTagId();
                for (Categories categories : serverTopics) {
                    topicsList.addAll(categories.getTags());
                }
                for (Topics topics : topicsList) {
                    if (topics.getId().equals(tagId)) {
                        topics.setSelected(true);
                        break;
                    }
                }
                for (int j = 0; j < tView.getTags().size(); j++) {
                    TagSelected initialSel = (TagSelected) tView.getTags().get(j);
                    if (initialSel.getTagId().equals(tagId)) {
                        Tag selectedTag = tView.getTags().get(j);
                        selectedTag.layoutBorderColor = WHITE;
                        selectedTag.layoutColor = COLOR_PRIMARY;
                        selectedTag.tagTextColor = WHITE;
                        selectedTag.tagTextSize = 12;
                        tView.updateTag(selectedTag);
                        break;
                    }
                }
            }

            Tag tagDummy = new Tag("Android");
            tagDummy.radius = 1f;
            tagDummy.layoutBorderColor = TAB_GREY;
            tagDummy.layoutBorderSize = 1f;
            tagDummy.layoutColor = WHITE;
            tagDummy.tagTextColor = TAB_GREY;
            tagDummy.tagTextSize = 12;
        } catch (Exception e) {
            mLog.e("TAGS", "Exception" + e);
        }
    }
}