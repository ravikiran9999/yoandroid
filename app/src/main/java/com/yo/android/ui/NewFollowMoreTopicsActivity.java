
package com.yo.android.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cunoraz.tagview.Tag;
import com.google.gson.Gson;
import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.R;
import com.yo.android.adapters.CategoriesAdapter;
import com.yo.android.api.YoApi;
import com.yo.android.helpers.TagSelected;
import com.yo.android.model.Articles;
import com.yo.android.model.Categories;
import com.yo.android.model.Topics;
import com.yo.android.sectionheaders.NewCategorizedList;
import com.yo.android.ui.followmoretopics.CategoriesAccordionSection;
import com.yo.android.util.Constants;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import butterknife.Bind;
import butterknife.ButterKnife;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewFollowMoreTopicsActivity extends BaseActivity {

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
    @Bind(R.id.recyclerView)
    RecyclerView recyclerView;
    @Bind(R.id.tagsparent)
    LinearLayout tagsParentLayout;
    @Bind(R.id.btn_skip)
    Button skip;
    @Bind(R.id.bottom)
    LinearLayout bottomLayout;
    @Bind(R.id.no_categories)
    TextView noCategories;

    private List<String> followedTopicsIdsList;

    private String from;
    private ArrayList<String> selectedTopics;
    private List<Topics> topicsList;
    private List<String> addedTopics;
    public ArrayList<Tag> initialTags;
    private ArrayList<Tag> searchTags;
    private List<Categories> serverTopics;
    private boolean isSkipClicked;
    private Context context;
    private ArrayList<Object> mData;
    private CategoriesAdapter categoriesAdapter;

    public interface TagsLoader {
        void loaded();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow_more_topics);
        ButterKnife.bind(this);
        context = this;

        setTitleHideIcon(R.string.follow_more_topics);

        Intent intent = getIntent();
        from = intent.getStringExtra("From");

        boolean backBtn = true;
        if ("UpdateProfileActivity".equals(from) || preferenceEndPoint.getBooleanPreference(Constants.ENABLE_FOLLOW_TOPICS_SCREEN)) {
            backBtn = false;
        }

        getSupportActionBar().setHomeButtonEnabled(backBtn);
        getSupportActionBar().setDisplayHomeAsUpEnabled(backBtn);

        View customView = getLayoutInflater().inflate(R.layout.section_list_item, null);

        initialTags = new ArrayList<>();
        topicsList = new ArrayList<>();
        addedTopics = new ArrayList<>();
        selectedTopics = new ArrayList<>();

        TAB_GREY = getResources().getColor(R.color.tab_grey);
        WHITE = getResources().getColor(R.color.white);
        COLOR_PRIMARY = getResources().getColor(R.color.colorPrimary);

        if (preferenceEndPoint.getBooleanPreference(Constants.ENABLE_FOLLOW_TOPICS_SCREEN)) {
            skip.setVisibility(View.VISIBLE);
        } else {
            skip.setVisibility(View.GONE);
        }

        String accessToken = preferenceEndPoint.getStringPreference(YoApi.ACCESS_TOKEN);
        showProgressDialog();
        serverTopics = new ArrayList<>();

        yoService.categoriesAPI(accessToken, true).enqueue(new Callback<List<Categories>>() {
            @Override
            public void onResponse(Call<List<Categories>> call, Response<List<Categories>> response) {
                try {
                    if (response == null || response.body() == null) {
                        dismissProgressDialog();
                        return;
                    }

                    serverTopics = response.body();
                    mData = new ArrayList<>();
                    for (Categories categories : serverTopics) {
                        mData.add(new CategoriesAccordionSection(categories.getId(), categories.getName(), categories.isLanguage_specific(), categories.getTags(), false));
                    }

                    loadCategoriesAdapter(mData);
                    dismissProgressDialog();

                } finally {
                    if (response != null && response.body() != null) {
                        try {
                            response.body().clear();
                            response = null;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

            }

            @Override
            public void onFailure(Call<List<Categories>> call, Throwable t) {
                dismissProgressDialog();
                noCategories.setText(R.string.socket_time_out);
                noCategories.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
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

        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                 Not to show add balance screen after selecting topics for new user
                */

                /*Intent intent = new Intent(context, TabsHeaderActivity.class);
                intent.putExtra(Constants.OPEN_ADD_BALANCE, true);
                startActivityForResult(intent, OPEN_ADD_BALANCE_RESULT);*/

                skip.setEnabled(false);
                isSkipClicked = true;
                navigation();
            }
        });
    }


    /**
     * Action done on clicking the Done button
     *
     * @param followedTopicsIdsList The topics list
     */
    private void performDoneAction(final List<String> followedTopicsIdsList) {

        /*
          Not to show add balance screen after selecting topics for new user
         */

        /*if (preferenceEndPoint.getBooleanPreference(Constants.ENABLE_FOLLOW_TOPICS_SCREEN)) {
            Intent intent = new Intent(context, TabsHeaderActivity.class);
            intent.putExtra(Constants.OPEN_ADD_BALANCE, true);
            startActivityForResult(intent, OPEN_ADD_BALANCE_RESULT);

        } else {*/
        if (mHelper != null && mHelper.isConnected()) {
            for (Object object : getCategoriesAdapter().getData()) {
                if (object instanceof CategoriesAccordionSection) {
                    for (Topics topics : ((CategoriesAccordionSection) object).getTags()) {
                        if (topics.isSelected()) {
                            followedTopicsIdsList.add(topics.getId());
                        }

                    }
                }
            }
        }

        showProgressDialog();
        String accessToken = preferenceEndPoint.getStringPreference(YoApi.ACCESS_TOKEN);
        yoService.addTopicsAPI(accessToken, followedTopicsIdsList, "").enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                dismissProgressDialog();
                if ("Magazines".equals(from)) {
                    Intent myCollectionsIntent = new Intent(context, MyCollections.class);
                    startActivity(myCollectionsIntent);
                    finish();
                } else if (preferenceEndPoint.getBooleanPreference(Constants.ENABLE_FOLLOW_TOPICS_SCREEN)) {
                    //TODO:Disalbe flag for Follow more
                    preferenceEndPoint.saveBooleanPreference(Constants.ENABLE_FOLLOW_TOPICS_SCREEN, false);
                    String accessToken = preferenceEndPoint.getStringPreference(YoApi.ACCESS_TOKEN);
                    // discussed with ingrid, and commenting it, as we are not using it
                    //yoService.getArticlesWithPaginationAPI(accessToken, 1, 200);
                    navigation();
                } else {
                    Intent intent = new Intent();
                    setResult(2, intent);
                    finish();
                }

                preferenceEndPoint.saveStringPreference(Constants.MAGAZINE_TAGS, TextUtils.join(",", followedTopicsIdsList));
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                dismissProgressDialog();
                Toast.makeText(context, "Error while adding topics", Toast.LENGTH_LONG).show();
            }
        });
        //}
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
        // hide search icon
        /*MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search, menu);
        prepareTagSearch(this, menu);*/
        return super.onCreateOptionsMenu(menu);
    }

    /*
      No need to come back from add balance screen
    */

    /*@Override
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
    }*/

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

    /**
     * Navigates to the landing screen
     */
    private void navigation() {
        Intent myCollectionsIntent = new Intent(context, BottomTabsActivity.class);
        myCollectionsIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        ArrayList<String> tagIds = new ArrayList<>(followedTopicsIdsList);
        myCollectionsIntent.putStringArrayListExtra("tagIds", tagIds);
        startActivity(myCollectionsIntent);
        finish();
    }

    public void loadCategoriesAdapter(ArrayList<Object> data) {
        categoriesAdapter = new CategoriesAdapter(context, selectedTopics);
        categoriesAdapter.setData(data);
        categoriesAdapter.notifyDataSetChanged();
        recyclerView.setAdapter(categoriesAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

    }

    public CategoriesAdapter getCategoriesAdapter() {
        return categoriesAdapter;
    }
}
