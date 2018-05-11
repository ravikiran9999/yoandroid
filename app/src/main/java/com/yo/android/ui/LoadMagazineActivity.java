package com.yo.android.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.yo.android.R;
import com.yo.android.api.ApiCallback;
import com.yo.android.api.YoApi;
import com.yo.android.model.Articles;
import com.yo.android.model.Topics;
import com.yo.android.usecase.StoryUsecase;
import com.yo.android.util.Constants;
import com.yo.android.util.Util;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;


import butterknife.BindView;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * This activity is used to load the new article url and create the new article
 */
public class LoadMagazineActivity extends BaseActivity implements View.OnClickListener {

    private static final String CREATE = "create";
    private static final String ADD = "add";

    @BindView(R.id.et_enter_url)
    EditText etUrl;
    @BindView(R.id.atv_enter_tag)
    AutoCompleteTextView atvMagazineTag;
    @BindView(R.id.imv_magazine_post)
    Button btnPost;
    @BindView(R.id.webview)
    WebView webview;
    @BindView(R.id.imv_close)
    ImageView imvClose;

    @Inject
    StoryUsecase storyUsecase;

    private String magazineId;
    private String url;
    private String magazineTitle;
    private String magazineDesc;
    private String magazinePrivacy;
    private boolean isInvalidUrl;
    private String tag;
    private boolean isPostClicked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_magazine);
        ButterKnife.bind(this);
        Intent intent = getIntent();
        magazineId = intent.getStringExtra("MagazineId");
        magazineTitle = intent.getStringExtra("MagazineTitle");
        magazineDesc = intent.getStringExtra("MagazineDesc");
        magazinePrivacy = intent.getStringExtra("MagazinePrivacy");

        webview.getSettings().setJavaScriptEnabled(true);


        imvClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        getAllTopics();

        webview.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                showProgressDialog();
            }

            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Util.hideKeyboard(LoadMagazineActivity.this, etUrl);
                mToastFactory.showToast(R.string.enter_valid_url);
                isInvalidUrl = true;
                etUrl.post(new Runnable() {
                    public void run() {
                        etUrl.requestFocus();
                    }
                });
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                dismissProgressDialog();
                if (!isInvalidUrl) {
                    //btnPost.setVisibility(View.VISIBLE);
                    btnPost.setText(R.string.post);
                }
            }
        });

        atvMagazineTag.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE) || (actionId == EditorInfo.IME_ACTION_GO) || (actionId == EditorInfo.IME_ACTION_NEXT)) {
                    loadOrPostUrl(webview);
                }
                return false;
            }
        });

        btnPost.setOnClickListener(this);
    }

    /**
     * Loads the url depending
     *
     * @param webview The Webview
     */
    private void loadOrPostUrl(WebView webview) {
        url = etUrl.getText().toString();
        tag = atvMagazineTag.getText().toString();
        if (!TextUtils.isEmpty(url.trim()) && !TextUtils.isEmpty(tag.trim())) { // Url is not empty then load the url

            isInvalidUrl = false;

            if (!url.contains("http://")) {
                if (!url.contains("https://")) {
                    url = "http://" + url;
                }
                if (Patterns.WEB_URL.matcher(url).matches()) {
                    webview.loadUrl(url); // Load the url
                } else {
                    Util.hideKeyboard(LoadMagazineActivity.this, etUrl);
                    mToastFactory.showToast(R.string.enter_valid_url);
                    etUrl.post(new Runnable() {
                        public void run() {
                            etUrl.requestFocus();
                        }
                    });
                }
            } else {
                if (Patterns.WEB_URL.matcher(url).matches()) {
                    webview.loadUrl(url);
                } else {
                    Util.hideKeyboard(LoadMagazineActivity.this, etUrl);
                    mToastFactory.showToast(R.string.enter_valid_url);
                    etUrl.post(new Runnable() {
                        public void run() {
                            etUrl.requestFocus();
                        }
                    });
                }
            }
        } else if (TextUtils.isEmpty(url.trim())) { // If the url is empty
            Util.hideKeyboard(LoadMagazineActivity.this, etUrl);
            mToastFactory.showToast(R.string.enter_url);
            etUrl.post(new Runnable() {
                public void run() {
                    etUrl.requestFocus();
                }
            });
        } else { // If the tag is empty
            Util.hideKeyboard(LoadMagazineActivity.this, atvMagazineTag);
            mToastFactory.showToast(R.string.enter_tag);
            atvMagazineTag.post(new Runnable() {
                public void run() {
                    atvMagazineTag.requestFocus();
                }
            });
        }
    }

    @Override
    public void onClick(View v) {

        if (((Button) v).getText().equals("Load")) { // Button text is Load
            loadOrPostUrl(webview);
        } else { // Button text is Post

            if (!TextUtils.isEmpty(url.trim()) && !TextUtils.isEmpty(tag.trim())) { // Url is not empty then post the url
                if (magazineId != null) {
                    magazineWithStory("", ADD, url, magazineTitle, magazineDesc, magazinePrivacy, magazineId, tag); // Add story to existing magazine
                } else {
                    magazineWithStory(CREATE, "", url, "", "", "", magazineId, tag); // Create a new magazine with the new story
                }
            } else if (TextUtils.isEmpty(url.trim())) { // Url is empty
                Util.hideKeyboard(LoadMagazineActivity.this, etUrl);
                mToastFactory.showToast(R.string.enter_url);
                etUrl.post(new Runnable() {
                    public void run() {
                        etUrl.requestFocus();
                    }
                });
            } else { // Tag is empty
                Util.hideKeyboard(LoadMagazineActivity.this, atvMagazineTag);
                mToastFactory.showToast(R.string.enter_tag);
                atvMagazineTag.post(new Runnable() {
                    public void run() {
                        atvMagazineTag.requestFocus();
                    }
                });
            }
        }
    }

    /**
     * Creates the story in the new magazine
     */
    private void magazineWithStory(final String create, final String add, String url, final String magazineTitle, final String magazineDesc, final String magazinePrivacy, String magazineId, String tag) {
        if (!isPostClicked) {
            isPostClicked = true;
            storyUsecase.magazineStory(url, magazineTitle, magazineDesc, magazinePrivacy, magazineId, tag, new ApiCallback<Articles>() {
                @Override
                public void onResult(Articles result) {
                    EventBus.getDefault().post(Constants.REFRESH_TOPICS_ACTION);
                    setResult(RESULT_OK);
                    finish();
                    if (create != null) {
                        Intent intent = new Intent(LoadMagazineActivity.this, CreatedMagazineDetailActivity.class);
                        intent.putExtra("MagazineTitle", magazineTitle);
                        if (result != null) {
                            intent.putExtra("MagazineId", result.getMagzine_id());
                        }
                        intent.putExtra("MagazineDesc", magazineDesc);
                        intent.putExtra("MagazinePrivacy", magazinePrivacy);
                        startActivity(intent);
                    }
                }

                @Override
                public void onFailure(String message) {
                    if (TextUtils.isEmpty(message)) {
                        Util.hideKeyboard(LoadMagazineActivity.this, etUrl);
                        if (create != null) {
                            mToastFactory.showToast(R.string.title_already_exists);
                        } else if (add != null) {
                            mToastFactory.showToast(R.string.article_already_added);
                        }
                    }
                    isPostClicked = false;
                }
            });
        }
    }

    /**
     * Gets all the magazine topics
     */
    private void getAllTopics() {
        String accessToken = preferenceEndPoint.getStringPreference(YoApi.ACCESS_TOKEN);

        yoService.tagsAPI(accessToken).enqueue(new Callback<List<Topics>>() {
            @Override
            public void onResponse(Call<List<Topics>> call, Response<List<Topics>> response) {
                try {
                    dismissProgressDialog();
                    if (this == null || response == null || response.body() == null) {
                        return;
                    }

                    List<String> topicNamesList = new ArrayList<String>();

                    for (int i = 0; i < response.body().size(); i++) {
                        topicNamesList.add(response.body().get(i).getName());
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<String>
                            (LoadMagazineActivity.this, R.layout.textviewitem, topicNamesList);
                    atvMagazineTag.setThreshold(1);//will start working from first character
                    atvMagazineTag.setAdapter(adapter);//setting the adapter data into the AutoCompleteTextView
                } finally {
                    if (response != null && response.body() != null) {
                        response.body().clear();
                        response = null;
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Topics>> call, Throwable t) {
                dismissProgressDialog();
            }
        });
    }

}
