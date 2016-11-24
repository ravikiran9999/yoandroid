package com.yo.android.ui;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v7.widget.SearchView;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cunoraz.tagview.Tag;
import com.cunoraz.tagview.TagView;
import com.cunoraz.tagview.Utils;
import com.google.gson.Gson;
import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.R;
import com.yo.android.api.YoApi;
import com.yo.android.inapp.UnManageInAppPurchaseActivity;
import com.yo.android.model.Articles;
import com.yo.android.model.Topics;
import com.yo.android.util.Constants;
import com.yo.android.util.Util;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FollowMoreTopicsActivity extends BaseActivity {

    @Inject
    YoApi.YoService yoService;

    @Inject
    @Named("login")
    protected PreferenceEndPoint preferenceEndPoint;
    private String from;
    private TagView tagGroup;
    private List<Topics> topicsList;
    private List<String> addedTopics;
    private ArrayList<Tag> initialTags;
    private ArrayList<Tag> searchTags;
    private boolean isInvalidSearch;
    private TextView noSearchResults;
    private List<String> followedTopicsIdsList;
    private Button done;
    private static final int OPEN_ADD_BALANCE_RESULT = 1000;
    private TextView tvHelloInterests;
    private TextView tvPickTopics;
    private static int TAB_GREY;
    private static int WHITE;
    private static int COLOR_PRIMARY;


    private int lineMargin;
    private int tagMargin;
    private int textPaddingLeft;
    private int textPaddingRight;
    private int textPaddingTop;
    private int texPaddingBottom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow_more_topics);

        String title = "Follow more topics";

        getSupportActionBar().setTitle(title);

        Intent intent = getIntent();
        from = intent.getStringExtra("From");

        boolean backBtn = true;
        if ("UpdateProfileActivity".equals(from) || preferenceEndPoint.getBooleanPreference(Constants.ENABLE_FOLLOW_TOPICS_SCREEN)) {
            backBtn = false;
        }
        getSupportActionBar().setHomeButtonEnabled(backBtn);
        getSupportActionBar().setDisplayHomeAsUpEnabled(backBtn);

        tagGroup = (TagView) findViewById(R.id.tag_group);
        done = (Button) findViewById(R.id.btn_done);
        noSearchResults = (TextView) findViewById(R.id.no_search_results);
        tvHelloInterests = (TextView) findViewById(R.id.hello_interests);
        tvPickTopics = (TextView) findViewById(R.id.pick_topics);

        initialTags = new ArrayList<>();
        topicsList = new ArrayList<Topics>();
        addedTopics = new ArrayList<String>();

        TAB_GREY = getResources().getColor(R.color.tab_grey);
        WHITE = getResources().getColor(R.color.white);
        COLOR_PRIMARY = getResources().getColor(R.color.colorPrimary);

        String accessToken = preferenceEndPoint.getStringPreference("access_token");
        showProgressDialog();
        yoService.tagsAPI(accessToken).enqueue(new Callback<List<Topics>>() {
            @Override
            public void onResponse(Call<List<Topics>> call, Response<List<Topics>> response) {
                if (response == null || response.body() == null) {
                    dismissProgressDialog();
                    return;
                }
                new TagLoader(response.body(), tagGroup).execute();

            }

            @Override
            public void onFailure(Call<List<Topics>> call, Throwable t) {
                dismissProgressDialog();
            }
        });

        //set click listener
        tagGroup.setOnTagClickListener(new TagView.OnTagClickListener() {
            @Override
            public void onTagClick(Tag mTag, int position) {
                try {
                    TagSelected tag = (TagSelected) mTag;
                    tag.toggleSelection();
                    if (!tag.getSelected()) {
                        addedTopics.remove(tag);
                        String tagId = tag.getTagId();
                        /*for (int i = 0; i < topicsList.size(); i++) {
                            if (topicsList.get(i).getId().equals(tagId)) {
                                topicsList.get(i).setSelected(false);
                            }
                        }*/
                        for (Topics topics : topicsList) {
                            if (topics.getId().equals(tagId)) {
                                topics.setSelected(false);
                                break;
                            }
                        }

                        Tag unselectedTag = tagGroup.getTags().get(position);
                        unselectedTag.layoutBorderColor = TAB_GREY;
                        unselectedTag.layoutColor = WHITE;
                        unselectedTag.tagTextColor = TAB_GREY;
                        tagGroup.updateTag(unselectedTag);


                        //tagGroup.getTags().get(position).layoutColorPress = getResources().getColor(R.color.colorPrimary);
                    } else {
                        addedTopics.add(tag.text);
                        String tagId = tag.getTagId();
                        /*for (int i = 0; i < topicsList.size(); i++) {
                            if (topicsList.get(i).getId().equals(tagId)) {
                                topicsList.get(i).setSelected(true);
                            }
                        }*/
                        for (Topics topics : topicsList) {
                            if (topics.getId().equals(tagId)) {
                                topics.setSelected(true);
                                break;
                            }
                        }
                        Tag selectedTag = tagGroup.getTags().get(position);
                        selectedTag.layoutBorderColor = WHITE;
                        selectedTag.layoutColor = COLOR_PRIMARY;
                        selectedTag.tagTextColor = WHITE;
                        tagGroup.updateTag(selectedTag);

                        //tagGroup.getTags().get(position).layoutColorPress = getResources().getColor(R.color.colorPrimary);
                    }

                    //tagGroup.setWillNotDraw(false);
                    //tagGroup.invalidate();

                    Tag tagDummy = new Tag("Android");
                    tagDummy.radius = 1f;
                    tagDummy.layoutBorderColor = TAB_GREY;
                    tagDummy.layoutBorderSize = 1f;
                    tagDummy.layoutColor = WHITE;
                    tagDummy.tagTextColor = TAB_GREY;
                    //tagDummy.layoutColorPress = getResources().getColor(R.color.colorPrimary);

                    //tagGroup.drawTags();
                    // tagGroup.updateTag(tagDummy);
                    // tagGroup.remove(tagGroup.getTags().size() - 1);
                } catch (Exception e) {
                    mLog.e("TAGS", "Exception" + e);
                }

            }
        });

        followedTopicsIdsList = new ArrayList<String>();
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performDoneAction(followedTopicsIdsList);
                done.setEnabled(false);
            }
        });

        //set delete listener
        tagGroup.setOnTagDeleteListener(new TagView.OnTagDeleteListener() {
            @Override
            public void onTagDeleted(final TagView view, final Tag tag, final int position) {
                // do nothing
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

            /*final Intent intent = new Intent(this, UnManageInAppPurchaseActivity.class);
            intent.putExtra("sku", "com.yo.products.credit.FIVE");
            intent.putExtra("price", 5f);
            final String userId = preferenceEndPoint.getStringPreference(Constants.USER_ID);
            intent.putExtra(Constants.USER_ID, userId);
            startActivityForResult(intent, 14);*/
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
            tagGroup.addTags(initialTags);
            for (int k = 0; k < tagGroup.getTags().size(); k++) {
                TagSelected t = (TagSelected) tagGroup.getTags().get(k);
                if (t.getSelected()) {

                    followedTopicsIdsList.add(String.valueOf(t.getTagId()));

                }

            }

            String accessToken = preferenceEndPoint.getStringPreference("access_token");
            yoService.addTopicsAPI(accessToken, followedTopicsIdsList).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if ("Magazines".equals(from)) {
                        Intent myCollectionsIntent = new Intent(FollowMoreTopicsActivity.this, MyCollections.class);
                        startActivity(myCollectionsIntent);
                        finish();
                    } else if (preferenceEndPoint.getBooleanPreference(Constants.ENABLE_FOLLOW_TOPICS_SCREEN)) {
                        //TODO:Disalbe flag for Follow more
                        preferenceEndPoint.saveBooleanPreference(Constants.ENABLE_FOLLOW_TOPICS_SCREEN, false);
                        String accessToken = preferenceEndPoint.getStringPreference("access_token");
                        yoService.getAllArticlesAPI(accessToken).enqueue(callback);
                        /*Intent myCollectionsIntent = new Intent(FollowMoreTopicsActivity.this, BottomTabsActivity.class);
                        myCollectionsIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        ArrayList<String> tagIds = new ArrayList<String>(followedTopicsIdsList);
                        myCollectionsIntent.putStringArrayListExtra("tagIds", tagIds);
                        startActivity(myCollectionsIntent);
                        finish();*/
                    } else {
                        Intent intent = new Intent();
                        setResult(2, intent);
                        finish();
                    }

                    preferenceEndPoint.saveStringPreference("magazine_tags", TextUtils.join(",", followedTopicsIdsList));
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast.makeText(FollowMoreTopicsActivity.this, "Error while adding topics", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private class TagLoader extends AsyncTask<Void, TagSelected, ArrayList<Tag>> {

        @NonNull
        private final List<Topics> dummyTopicsList;
        private TagView tagGroup;

        public TagLoader(List<Topics> topics, TagView tagGroup) {
            this.dummyTopicsList = new ArrayList<>(topics);
            this.tagGroup = tagGroup;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressDialog();
            topicsList = new ArrayList<Topics>();
            tagGroup.setVisibility(View.GONE);
            initialTags.clear();
        }

        @Override
        protected ArrayList<Tag> doInBackground(Void... params) {
            topicsList.clear();
            topicsList.addAll(dummyTopicsList);
            synchronized (initialTags) {
                for (Topics topic : topicsList) {
                    final TagSelected tag = prepareTag(topic);
                    initialTags.add(tag);
                }
            }
            return initialTags;
        }

        @Override
        protected void onProgressUpdate(TagSelected... values) {
            super.onProgressUpdate(values);
            if (tagGroup != null) {
                tagGroup.addTag(values[0]);
            }
        }

        @Override
        protected void onPostExecute(final ArrayList<Tag> tagSelected) {
            super.onPostExecute(tagSelected);
            tagGroup.addTags(tagSelected);
            if (tagGroup != null) {
                tagGroup.setVisibility(View.VISIBLE);
            }
            dismissProgressDialog();
        }

    }

    @NonNull
    private TagSelected prepareTag(Topics topics) {
        TagSelected tag = new TagSelected(topics.getName());
        tag.radius = 1f;
        tag.setTagId(topics.getId());
        tag.layoutBorderColor = getResources().getColor(R.color.tab_grey);
        tag.layoutBorderSize = 1f;
        tag.layoutColor = getResources().getColor(android.R.color.white);
        tag.tagTextColor = getResources().getColor(R.color.tab_grey);
        //tag.layoutColorPress = getResources().getColor(R.color.colorPrimary);

        if (topics.isSelected()) {
            tag.setSelected(true);
            tag.layoutColor = getResources().getColor(R.color.colorPrimary);
            tag.tagTextColor = getResources().getColor(android.R.color.white);
            //tag.layoutColorPress = getResources().getColor(R.color.colorPrimary);
            tag.setSelected(true);
            addedTopics.add(tag.text);
        } else {
            tag.layoutColor = getResources().getColor(android.R.color.white);
            tag.tagTextColor = getResources().getColor(R.color.tab_grey);
            //tag.layoutColorPress = getResources().getColor(R.color.colorPrimary);
            tag.setSelected(false);
        }
        return tag;
    }

    private class TagSelected extends Tag {

        private boolean selected;
        private String tagId;

        public TagSelected(String text) {
            super(text);
        }

        public boolean getSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        public void toggleSelection() {
            this.selected = !selected;
        }

        public String getTagId() {
            return tagId;
        }

        public void setTagId(String tagId) {
            this.tagId = tagId;
        }
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
                    //Crash Happened ::    File: ArrayList.java   Class:java.util.ArrayList$ArrayListIterator   Method:next   Line:573
                    // File: FollowMoreTopicsActivity.java   Class:com.yo.android.ui.FollowMoreTopicsActivity   Method:setTags   Line:419
                    setTags(newText);
                   // setNewTags(newText);
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

    private void setNewTags(CharSequence cs) {
        if (TextUtils.isEmpty(cs.toString().trim())) {
            //Util.hideKeyboard(this, getCurrentFocus());
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            noSearchResults.setVisibility(View.GONE);
            tvHelloInterests.setVisibility(View.VISIBLE);
            tvPickTopics.setVisibility(View.VISIBLE);
            return;
        }
        String text = cs.toString();
        List<Tag> listTags = tagGroup.getTags();
        for (Tag tag : listTags) {
            TextView tagView = (TextView) tag.getTagView().findViewById(com.cunoraz.tagview.R.id.tv_tag_item_contain);
            String data = tagView.getText().toString().toUpperCase();
            if(!data.contains(text.toUpperCase())){
                tagGroup.removeView(tag.getTagView());
                tagGroup.invalidate();
            }
        }
        listTags = tagGroup.getTags();
        for (Tag tag : listTags) {
            TextView tagView = (TextView) tag.getTagView().findViewById(com.cunoraz.tagview.R.id.tv_tag_item_contain);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) tagView.getLayoutParams();
            params.setMargins(textPaddingLeft, textPaddingTop, textPaddingRight, texPaddingBottom);
            tagView.setLayoutParams(params);
            tagView.invalidate();
            tagGroup.invalidate();
        }

    }

    private void setTags(CharSequence cs) {
        tagGroup.getTags().clear();
        tagGroup.removeAllViews();

        if (TextUtils.isEmpty(cs.toString().trim())) {
            //Util.hideKeyboard(this, getCurrentFocus());
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            noSearchResults.setVisibility(View.GONE);
            tvHelloInterests.setVisibility(View.VISIBLE);
            tvPickTopics.setVisibility(View.VISIBLE);
            new TagLoader(topicsList, tagGroup).execute();
            return;
        }

        String text = cs.toString();
        searchTags = new ArrayList<Tag>();
        for (Tag tag : initialTags) {
            if (TextUtils.isEmpty(text) || tag.text.toLowerCase().contains(text.toLowerCase())) {
                tagGroup.addTag(tag);
                searchTags.add(tag);
            }
        }
        if (tagGroup.getTags().size() == 0) {
            noSearchResults.setVisibility(View.VISIBLE);
            tvHelloInterests.setVisibility(View.GONE);
            tvPickTopics.setVisibility(View.GONE);
            isInvalidSearch = true;
        } else {
            noSearchResults.setVisibility(View.GONE);
            tvHelloInterests.setVisibility(View.VISIBLE);
            tvPickTopics.setVisibility(View.VISIBLE);
            isInvalidSearch = false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OPEN_ADD_BALANCE_RESULT && resultCode == RESULT_OK) {
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
            tagGroup.addTags(initialTags);
            for (int k = 0; k < tagGroup.getTags().size(); k++) {
                TagSelected t = (TagSelected) tagGroup.getTags().get(k);
                if (t.getSelected()) {
                    followedTopicsIdsList.add(String.valueOf(t.getTagId()));
                }
            }

            navigation();

            /*String accessToken = preferenceEndPoint.getStringPreference("access_token");
            yoService.addTopicsAPI(accessToken, followedTopicsIdsList).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if ("Magazines".equals(from)) {
                        Intent myCollectionsIntent = new Intent(FollowMoreTopicsActivity.this, MyCollections.class);
                        startActivity(myCollectionsIntent);
                        finish();
                    } else if (preferenceEndPoint.getBooleanPreference(Constants.ENABLE_FOLLOW_TOPICS_SCREEN)) {
                        //TODO:Disalbe flag for Follow more
                        preferenceEndPoint.saveBooleanPreference(Constants.ENABLE_FOLLOW_TOPICS_SCREEN, false);
                        *//*String accessToken = preferenceEndPoint.getStringPreference("access_token");
                        yoService.getAllArticlesAPI(accessToken).enqueue(callback);*//*
                        navigation();

                        *//*Intent myCollectionsIntent = new Intent(FollowMoreTopicsActivity.this, BottomTabsActivity.class);
                        myCollectionsIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        ArrayList<String> tagIds = new ArrayList<String>(followedTopicsIdsList);
                        myCollectionsIntent.putStringArrayListExtra("tagIds", tagIds);
                        startActivity(myCollectionsIntent);
                        finish();*//*
                    } else {
                        Intent intent = new Intent();
                        setResult(2, intent);
                        finish();
                    }

                    preferenceEndPoint.saveStringPreference("magazine_tags", TextUtils.join(",", followedTopicsIdsList));
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast.makeText(FollowMoreTopicsActivity.this, "Error while adding topics", Toast.LENGTH_LONG).show();
                }
            });*/

        } else if (requestCode == OPEN_ADD_BALANCE_RESULT && resultCode == RESULT_CANCELED) {
            done.setEnabled(true);
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

    //==
    private void newOpenCamera() {
        if (mThread == null) {
            mThread = new CameraHandlerThread();
        }

        synchronized (mThread) {
            mThread.openCamera();
        }
    }

    private CameraHandlerThread mThread = null;

    private static class CameraHandlerThread extends HandlerThread {
        Handler mHandler = null;

        CameraHandlerThread() {
            super("CameraHandlerThread");
            start();
            mHandler = new Handler(getLooper());
        }

        synchronized void notifyCameraOpened() {
            notify();
        }

        void openCamera() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    oldOpenCamera();
                    notifyCameraOpened();
                }
            });
            try {
                wait();
            } catch (InterruptedException e) {
                Log.w("", "wait was interrupted");
            }
        }

        private void oldOpenCamera() {
            try {
                // do nothing
            } catch (RuntimeException e) {
                Log.e("", "failed to open front camera");
                // do nothing
            }
        }
    }

    private class TagDummyLoader extends AsyncTask<Void, Tag, Tag> {

        @NonNull
        private final Tag tagDummy;
        private TagView tagGroup;

        public TagDummyLoader(Tag tag, TagView tagGroup) {
            this.tagDummy = tag;
            this.tagGroup = tagGroup;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressDialog();
            /*topicsList = new ArrayList<Topics>();
            tagGroup.setVisibility(View.GONE);
            initialTags.clear();*/
        }

        @Override
        protected Tag doInBackground(Void... params) {
            /*topicsList.clear();
            topicsList.addAll(dummyTopicsList);
            synchronized (initialTags) {
                for (Topics topic : topicsList) {
                    final TagSelected tag = prepareTag(topic);
                    initialTags.add(tag);
                }
            }
            return initialTags;*/

            /*final Tag tagDummy = new Tag("Android");
            tagDummy.radius = 1f;
            tagDummy.layoutBorderColor = TAB_GREY;
            tagDummy.layoutBorderSize = 1f;
            tagDummy.layoutColor = WHITE;
            tagDummy.tagTextColor = TAB_GREY;
            //tagDummy.layoutColorPress = getResources().getColor(R.color.colorPrimary);

            runOnUiThread(new Runnable() {
                public void run() {
                    tagGroup.addTag(tagDummy);
                    tagGroup.remove(tagGroup.getTags().size() - 1);

                }
            });*/
            final Tag tagDummy = new Tag("Android");
            tagDummy.radius = 1f;
            tagDummy.layoutBorderColor = TAB_GREY;
            tagDummy.layoutBorderSize = 1f;
            tagDummy.layoutColor = WHITE;
            tagDummy.tagTextColor = TAB_GREY;
            publishProgress(tagDummy);

            return tagDummy;
        }

        @Override
        protected void onProgressUpdate(Tag... values) {
            super.onProgressUpdate(values);
            /*if (tagGroup != null) {
                tagGroup.addTag(values[0]);
            }*/

            tagGroup.addTag(tagDummy);
            tagGroup.remove(tagGroup.getTags().size() - 1);

        }

    }

    private void addTopics() {
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
        tagGroup.addTags(initialTags);
        for (int k = 0; k < tagGroup.getTags().size(); k++) {
            TagSelected t = (TagSelected) tagGroup.getTags().get(k);
            if (t.getSelected()) {
                followedTopicsIdsList.add(String.valueOf(t.getTagId()));
            }
        }

        String accessToken = preferenceEndPoint.getStringPreference("access_token");
        yoService.addTopicsAPI(accessToken, followedTopicsIdsList).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if ("Magazines".equals(from)) {
                    Intent myCollectionsIntent = new Intent(FollowMoreTopicsActivity.this, MyCollections.class);
                    startActivity(myCollectionsIntent);
                    finish();
                } else if (preferenceEndPoint.getBooleanPreference(Constants.ENABLE_FOLLOW_TOPICS_SCREEN)) {
                    //TODO:Disalbe flag for Follow more
                    preferenceEndPoint.saveBooleanPreference(Constants.ENABLE_FOLLOW_TOPICS_SCREEN, false);
                   /* String accessToken = preferenceEndPoint.getStringPreference("access_token");
                    yoService.getAllArticlesAPI(accessToken).enqueue(callback);*/
                    Intent intent = new Intent(FollowMoreTopicsActivity.this, TabsHeaderActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra(Constants.OPEN_ADD_BALANCE, true);
                    ArrayList<String> tagIds = new ArrayList<String>(followedTopicsIdsList);
                    intent.putStringArrayListExtra("tagIds", tagIds);
                    startActivityForResult(intent, OPEN_ADD_BALANCE_RESULT);
                    //startActivity(intent);

                        /*Intent myCollectionsIntent = new Intent(FollowMoreTopicsActivity.this, BottomTabsActivity.class);
                        myCollectionsIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        ArrayList<String> tagIds = new ArrayList<String>(followedTopicsIdsList);
                        myCollectionsIntent.putStringArrayListExtra("tagIds", tagIds);
                        startActivity(myCollectionsIntent);
                        finish();*/
                } else {
                    Intent intent = new Intent();
                    setResult(2, intent);
                    finish();
                }

                preferenceEndPoint.saveStringPreference("magazine_tags", TextUtils.join(",", followedTopicsIdsList));
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(FollowMoreTopicsActivity.this, "Error while adding topics", Toast.LENGTH_LONG).show();
            }
        });
    }


}
