package com.yo.android.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.cunoraz.tagview.OnTagClickListener;
import com.cunoraz.tagview.OnTagDeleteListener;
import com.cunoraz.tagview.Tag;
import com.cunoraz.tagview.TagView;
import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.R;
import com.yo.android.api.YoApi;
import com.yo.android.model.Topics;

import org.w3c.dom.Text;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow_more_topics);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String title = "Follow more topics";

        getSupportActionBar().setTitle(title);

        final Intent intent = getIntent();
        if(intent.hasExtra("From") && !TextUtils.isEmpty("from")) {
            from = intent.getStringExtra("From");
        }

        final TagView tagGroup = (TagView) findViewById(R.id.tag_group);
        Button done = (Button) findViewById(R.id.btn_done);

        final ArrayList<Tag> tags = new ArrayList<>();
        final List<Topics> topicsList = new ArrayList<Topics>();
        final List<String> addedTopics = new ArrayList<String>();

        String accessToken = preferenceEndPoint.getStringPreference("access_token");
        showProgressDialog();
        yoService.tagsAPI(accessToken).enqueue(new Callback<List<Topics>>() {
            @Override
            public void onResponse(Call<List<Topics>> call, Response<List<Topics>> response) {
                dismissProgressDialog();
                if (response == null || response.body() == null) {
                    return;
                }
                topicsList.addAll(response.body());
                for (int i = 0; i < topicsList.size(); i++) {
                    //if (topicsList.get(i).getName().toLowerCase().startsWith(text.toLowerCase())) {
                    Tag tag = new Tag(topicsList.get(i).getName());
                    tag.radius = 1f;
                    tag.layoutBorderColor = getResources().getColor(R.color.tab_grey);
                    tag.layoutBorderSize = 1f;
                    tag.layoutColor = getResources().getColor(android.R.color.white);
                    tag.tagTextColor = getResources().getColor(R.color.tab_grey);
                    // if (i % 2 == 0) // you can set deletable or not
                    //     tag.isDeletable = true;

                    if(topicsList.get(i).getSelected().equals("true")) {
                        tag.layoutColor = getResources().getColor(R.color.colorPrimary);
                        tag.tagTextColor = getResources().getColor(android.R.color.white);
                        addedTopics.add(tag.text);
                    }
                    else {
                        tag.layoutColor = getResources().getColor(android.R.color.white);
                        tag.tagTextColor = getResources().getColor(R.color.tab_grey);
                    }
                    tags.add(tag);
                    //}
                }
                tagGroup.addTags(tags);

            }

            @Override
            public void onFailure(Call<List<Topics>> call, Throwable t) {
                dismissProgressDialog();
            }
        });

        //set click listener
        tagGroup.setOnTagClickListener(new OnTagClickListener() {
            @Override
            public void onTagClick(Tag tag, int position) {
                try {
                    if (tag.tagTextColor == getResources().getColor(R.color.white)) {
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
                for (int i = 0; i < addedTopics.size(); i++) {
                    for (int j = 0; j < topicsList.size(); j++) {
                        if (addedTopics.get(i).equals(topicsList.get(j).getName()))
                            followedTopicsIdsList.add(topicsList.get(j).getId());
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
                        } else {
                            Intent intent = new Intent();
                            setResult(2, intent);
                            finish();
                        }
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
}
