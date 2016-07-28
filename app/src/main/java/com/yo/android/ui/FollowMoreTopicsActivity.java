package com.yo.android.ui;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.SearchView;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.cunoraz.tagview.OnTagClickListener;
import com.cunoraz.tagview.OnTagDeleteListener;
import com.cunoraz.tagview.Tag;
import com.cunoraz.tagview.TagView;
import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.R;
import com.yo.android.api.YoApi;
import com.yo.android.model.Topics;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow_more_topics);

        String title = "Follow more topics";

        getSupportActionBar().setTitle(title);

        final Intent intent = getIntent();
        if (intent.hasExtra("From") && !TextUtils.isEmpty("from")) {
            from = intent.getStringExtra("From");
        }

        boolean backBtn = true;
        if (from.equals("UpdateProfileActivity")) {
            backBtn = false;
        }
        getSupportActionBar().setHomeButtonEnabled(backBtn);
        getSupportActionBar().setDisplayHomeAsUpEnabled(backBtn);

        tagGroup = (TagView) findViewById(R.id.tag_group);
        Button done = (Button) findViewById(R.id.btn_done);

        initialTags = new ArrayList<>();
        topicsList = new ArrayList<Topics>();
        addedTopics = new ArrayList<String>();

        String accessToken = preferenceEndPoint.getStringPreference("access_token");
        showProgressDialog();
        yoService.tagsAPI(accessToken).enqueue(new Callback<List<Topics>>() {
            @Override
            public void onResponse(Call<List<Topics>> call, Response<List<Topics>> response) {
                if (response == null || response.body() == null) {
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
        tagGroup.setOnTagClickListener(new OnTagClickListener() {
            @Override
            public void onTagClick(Tag mTag, int position) {
                try {
                    TagSelected tag = (TagSelected) mTag;
                    tag.toggleSelection();
                    if (!tag.getSelected()) {
                        addedTopics.remove(tag);
                        tagGroup.getTags().get(position).layoutBorderColor = getResources().getColor(R.color.tab_grey);
                        tagGroup.getTags().get(position).layoutColor = getResources().getColor(R.color.white);
                        tagGroup.getTags().get(position).tagTextColor = getResources().getColor(R.color.tab_grey);
                    } else {
                        addedTopics.add(tag.text);
                        tagGroup.getTags().get(position).layoutBorderColor = getResources().getColor(R.color.white);
                        tagGroup.getTags().get(position).layoutColor = getResources().getColor(R.color.colorPrimary);
                        tagGroup.getTags().get(position).tagTextColor = getResources().getColor(R.color.white);
                    }


                    Tag tagDummy = new Tag("Android");
                    tagDummy.radius = 1f;
                    tagDummy.layoutBorderColor = getResources().getColor(R.color.tab_grey);
                    tagDummy.layoutBorderSize = 1f;
                    tagDummy.layoutColor = getResources().getColor(android.R.color.white);
                    tagDummy.tagTextColor = getResources().getColor(R.color.tab_grey);

                    tagGroup.addTag(tagDummy);
                    tagGroup.remove(tagGroup.getTags().size() - 1);
                } catch (Exception e) {
                    mLog.e("TAGS", "Exception" + e);
                }

            }
        });

        final List<String> followedTopicsIdsList = new ArrayList<String>();
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (searchTags != null) {
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
                        if (from.equals("Magazines")) {
                            Intent myCollectionsIntent = new Intent(FollowMoreTopicsActivity.this, MyCollections.class);
                            startActivity(myCollectionsIntent);
                            finish();
                        } else if (from.equals("UpdateProfileActivity")) {
                            Intent myCollectionsIntent = new Intent(FollowMoreTopicsActivity.this, BottomTabsActivity.class);
                            myCollectionsIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            ArrayList<String> tagIds = new ArrayList<String>(followedTopicsIdsList);
                            myCollectionsIntent.putStringArrayListExtra("tagIds", tagIds);
                            startActivity(myCollectionsIntent);
                            finish();
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
        });

        //set delete listener
        tagGroup.setOnTagDeleteListener(new OnTagDeleteListener() {
            @Override
            public void onTagDeleted(final TagView view, final Tag tag, final int position) {
            }
        });
    }

    private class TagLoader extends AsyncTask<Void, Void, ArrayList<Tag>> {

        @NonNull
        private final List<Topics> dummyTopicsList;
        private TagView tagGroup;

        public TagLoader(List<Topics> topics, TagView tagGroup) {
            this.dummyTopicsList = topics;
            this.tagGroup = tagGroup;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected ArrayList<Tag> doInBackground(Void... params) {
            topicsList.addAll(dummyTopicsList);
            for (Topics topic : topicsList) {
                final TagSelected tag = prepareTag(topic);
                initialTags.add(tag);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tagGroup.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                tagGroup.addTag(tag);
                            }
                        }, 1000);
                    }
                });
            }
            return initialTags;
        }

        @Override
        protected void onPostExecute(final ArrayList<Tag> tagSelected) {
            super.onPostExecute(tagSelected);
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

        // if (i % 2 == 0) // you can set deletable or not
        //     tag.isDeletable = true;
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
        searchView.setQueryHint(Html.fromHtml("<font color = #FFFFFF>" + "Search...." + "</font>"));
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(activity.getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            public static final String TAG = "PrepareSearch in Topics";

            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.i(TAG, "onQueryTextSubmit: " + query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.i(TAG, "onQueryTextChange: " + newText);
                setTags(newText);
                return true;
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                return true;
            }
        });
    }

    private void setTags(CharSequence cs) {
        if (TextUtils.isEmpty(cs.toString())) {
            return;
        }
        tagGroup.getTags().clear();
        tagGroup.removeAllViews();
        String text = cs.toString();
        for (Tag tag : initialTags) {
            if (tag.text.toLowerCase().contains(text.toLowerCase())) {
                tagGroup.addTag(tag);
            }
        }
    }
}
