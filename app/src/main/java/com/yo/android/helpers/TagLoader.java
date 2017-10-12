package com.yo.android.helpers;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;

import com.cunoraz.tagview.Tag;
import com.cunoraz.tagview.TagView;
import com.yo.android.R;
import com.yo.android.model.Categories;
import com.yo.android.model.Collections;
import com.yo.android.model.Topics;
import com.yo.android.sectionheaders.CategorizedList;
import com.yo.android.sectionheaders.CategoryAdapter;
import com.yo.android.ui.FollowMoreTopicsActivity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * Created by creatives on 12/21/2016.
 */
public class TagLoader extends AsyncTask<Void, TagSelected, HashMap<String, ArrayList<Tag>>> {

    @NonNull
    private final List<Categories> dummyTopicsList;
    private TagView tagGroup;
    private ArrayList<Tag> initialTags;
    private List<Categories> topicsList;
    private Context context;
    private CategorizedList categorisedList;
    private LayoutInflater layoutInflater;
    private HashMap<String, ArrayList<Tag>> categoriesHashMap;
    FollowMoreTopicsActivity.TagsLoader tagsLoader;

    public TagLoader(Context context, FollowMoreTopicsActivity.TagsLoader tagsLoader, List<Categories> topics, TagView tagGroup, ArrayList<Tag> initialTags, CategorizedList categorisedList) {
        this.dummyTopicsList = new ArrayList<>(topics);
        this.tagGroup = tagGroup;
        this.initialTags = initialTags;
        this.tagsLoader = tagsLoader;
        this.context = context;
        this.categorisedList = categorisedList;
        this.layoutInflater = LayoutInflater.from(context);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        topicsList = new ArrayList<Categories>();
        tagGroup.setVisibility(View.GONE);
        initialTags.clear();
        categoriesHashMap = new HashMap<>();
    }

    @Override
    protected HashMap<String, ArrayList<Tag>> doInBackground(Void... params) {
        topicsList.clear();
        topicsList.addAll(dummyTopicsList);

        synchronized (categoriesHashMap) {
            for (Categories categories : topicsList) {
                initialTags = new ArrayList<>();
                for (Topics topics : categories.getTags()) {
                    final TagSelected tag = ((FollowMoreTopicsActivity) context).prepareTag(topics);
                    initialTags.add(tag);
                    ((FollowMoreTopicsActivity) context).initialTags.add(tag);
                }
                //ArrayList<Tag> cacheTags = new ArrayList<>(initialTags);
                java.util.Collections.sort(initialTags, new Comparator<Tag>() {
                    @Override
                    public int compare(Tag lhs, Tag rhs) {
                        return lhs.getText().toLowerCase().compareTo(rhs.getText().toLowerCase());
                    }
                });

                categoriesHashMap.put(categories.getName(), initialTags);
            }
        }
        return categoriesHashMap;
    }

    @Override
    protected void onProgressUpdate(TagSelected... values) {
        super.onProgressUpdate(values);
        if (tagGroup != null) {
            tagGroup.addTag(values[0]);
        }
    }

    @Override
    protected void onPostExecute(final HashMap<String, ArrayList<Tag>> tagSelected) {
        super.onPostExecute(tagSelected);

        tagsLoader.loaded();
        if (tagGroup != null) {
            tagGroup.setVisibility(View.VISIBLE);
        }

        for (Categories categories : topicsList) {
            categorisedList.CreateSectionItems(createTag(tagSelected.get(categories.getName())), categories.getName());
        }

        CategoryAdapter categoryAdapter = categorisedList.LoadCategoryAdapter();

        Handler mHandler = new Handler();
        long DURATION = 5000L;
        mHandler.postDelayed(runnable, DURATION);
    }

    private TagView createTag(List<Tag> tags) {
        TagView view = (TagView) layoutInflater.inflate(R.layout.section_list_item, null);
        TagView tv = (TagView) view.findViewById(R.id.tag_group);
        tv.addTags(tags);
        return view;
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            ((FollowMoreTopicsActivity) context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((FollowMoreTopicsActivity) context).dismissProgressDialog();
                }
            });

        }
    };
}
