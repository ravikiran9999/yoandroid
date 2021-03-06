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

import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.R;
import com.yo.android.api.YoApi;
import com.yo.android.model.Articles;
import com.yo.android.model.Topics;
import com.yo.android.util.Constants;
import com.yo.android.util.Util;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import de.greenrobot.event.EventBus;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * This activity is used to load the new article url and create the new article
 */
public class LoadMagazineActivity extends BaseActivity implements View.OnClickListener {

    @Inject
    YoApi.YoService yoService;
    @Inject
    @Named("login")
    protected PreferenceEndPoint preferenceEndPoint;
    private String magazineId;
    private String url;
    private String magazineTitle;
    private String magazineDesc;
    private String magazinePrivacy;
    private boolean isInvalidUrl;
    private EditText etUrl;
    private AutoCompleteTextView atvMagazineTag;
    private String tag;
    private Button btnPost;
    private boolean isPostClicked;
    private WebView webview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_magazine);

        Intent intent = getIntent();
        magazineId = intent.getStringExtra("MagazineId");
        magazineTitle = intent.getStringExtra("MagazineTitle");
        magazineDesc = intent.getStringExtra("MagazineDesc");
        magazinePrivacy = intent.getStringExtra("MagazinePrivacy");

        etUrl = (EditText) findViewById(R.id.et_enter_url);
        atvMagazineTag = (AutoCompleteTextView) findViewById(R.id.atv_enter_tag);
        webview = (WebView) findViewById(R.id.webview);
        webview.getSettings().setJavaScriptEnabled(true);

        btnPost = (Button) findViewById(R.id.imv_magazine_post);
        ImageView imvClose = (ImageView) findViewById(R.id.imv_close);

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
                mToastFactory.showToast("Please enter a valid url");
                //btnPost.setVisibility(View.INVISIBLE);
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
                    btnPost.setText("Post");
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
                    mToastFactory.showToast("Please enter a valid url");
                    //btnPost.setVisibility(View.INVISIBLE);
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
                    mToastFactory.showToast("Please enter a valid url");
                    //btnPost.setVisibility(View.INVISIBLE);
                    etUrl.post(new Runnable() {
                        public void run() {
                            etUrl.requestFocus();
                        }
                    });
                }
            }
        } else if (TextUtils.isEmpty(url.trim())) { // If the url is empty
            Util.hideKeyboard(LoadMagazineActivity.this, etUrl);
            mToastFactory.showToast("Please enter a url");
            //btnPost.setVisibility(View.INVISIBLE);
            etUrl.post(new Runnable() {
                public void run() {
                    etUrl.requestFocus();
                }
            });
        } else { // If the tag is empty
            Util.hideKeyboard(LoadMagazineActivity.this, atvMagazineTag);
            mToastFactory.showToast("Please enter a tag");
            //btnPost.setVisibility(View.INVISIBLE);
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

            String accessToken = preferenceEndPoint.getStringPreference("access_token");
            if (!TextUtils.isEmpty(url.trim()) && !TextUtils.isEmpty(tag.trim())) { // Url is not empty then post the url
                if (magazineId != null) {
                    addStoryToExistingMagazine(accessToken); // Add story to existing magazine
                } else {
                    createMagazineWithStory(accessToken); // Create a new magazine with the new story
                }
            } else if (TextUtils.isEmpty(url.trim())) { // Url is empty
                Util.hideKeyboard(LoadMagazineActivity.this, etUrl);
                mToastFactory.showToast("Please enter a url");
                //btnPost.setVisibility(View.INVISIBLE);
                etUrl.post(new Runnable() {
                    public void run() {
                        etUrl.requestFocus();
                    }
                });
            } else { // Tag is empty
                Util.hideKeyboard(LoadMagazineActivity.this, atvMagazineTag);
                mToastFactory.showToast("Please enter a tag");
                //btnPost.setVisibility(View.INVISIBLE);
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
     *
     * @param accessToken
     */
    private void createMagazineWithStory(String accessToken) {
        if (!isPostClicked) {
            isPostClicked = true;
            yoService.postStoryMagazineAPI(accessToken, url, magazineTitle, magazineDesc, magazinePrivacy, magazineId, tag).enqueue(new Callback<Articles>() {
                @Override
                public void onResponse(Call<Articles> call, Response<Articles> response) {
                    try {
                        if (response != null && response.body() != null) {
                            EventBus.getDefault().post(Constants.REFRESH_TOPICS_ACTION);
                            setResult(RESULT_OK);
                            finish();
                            Intent intent = new Intent(LoadMagazineActivity.this, CreatedMagazineDetailActivity.class);
                            intent.putExtra("MagazineTitle", magazineTitle);
                            if (response.body() != null) {
                                intent.putExtra("MagazineId", response.body().getMagzine_id());
                            }
                            intent.putExtra("MagazineDesc", magazineDesc);
                            intent.putExtra("MagazinePrivacy", magazinePrivacy);
                            startActivity(intent);
                        } else if (response != null && response.errorBody() != null) {
                            Util.hideKeyboard(LoadMagazineActivity.this, etUrl);
                            mToastFactory.showToast("Magazine Title is already taken");
                            isPostClicked = false;
                        }
                    }finally {
                        if(response != null && response.body() != null) {
                            try {
                                response = null;
                            }catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

                @Override
                public void onFailure(Call<Articles> call, Throwable t) {
                    isPostClicked = false;
                }
            });
        }
    }

    /**
     * Adds a story to an existing magazine
     *
     * @param accessToken
     */
    private void addStoryToExistingMagazine(String accessToken) {
        if (!isPostClicked) {
            isPostClicked = true;
            yoService.addStoryMagazineAPI(accessToken, url, magazineId, tag).enqueue(new Callback<Articles>() {
                @Override
                public void onResponse(Call<Articles> call, Response<Articles> response) {
                    try {
                        if (response.body() != null) {
                            EventBus.getDefault().post(Constants.REFRESH_TOPICS_ACTION);
                            setResult(RESULT_OK);
                            finish();
                        } else if (response.errorBody() != null) {
                            Util.hideKeyboard(LoadMagazineActivity.this, etUrl);
                            mToastFactory.showToast("Article already added into current magazine");
                            isPostClicked = false;
                        }
                    } finally {
                        if(response != null && response.body() != null) {
                            try {
                                response = null;
                            }catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

                @Override
                public void onFailure(Call<Articles> call, Throwable t) {
                    isPostClicked = false;
                }
            });
        }
    }

    /**
     * Gets all the magazine topics
     */
    private void getAllTopics() {
        String accessToken = preferenceEndPoint.getStringPreference("access_token");

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
