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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.orion.android.common.preferences.PreferenceEndPoint;
import com.yo.android.R;
import com.yo.android.api.YoApi;
import com.yo.android.model.Articles;
import com.yo.android.util.Util;

import javax.inject.Inject;
import javax.inject.Named;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
        final WebView webview = (WebView) findViewById(R.id.webview);
        webview.getSettings().setJavaScriptEnabled(true);

        final Button btnPost = (Button) findViewById(R.id.imv_magazine_post);
        ImageView imvClose = (ImageView) findViewById(R.id.imv_close);

        imvClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        webview.setWebViewClient(new WebViewClient() {


            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                showProgressDialog();
            }

            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Util.hideKeyboard(LoadMagazineActivity.this, etUrl);
                mToastFactory.showToast("Please enter a valid url");
                btnPost.setVisibility(View.INVISIBLE);
                isInvalidUrl = true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                dismissProgressDialog();
                if(!isInvalidUrl) {
                    btnPost.setVisibility(View.VISIBLE);
                }
            }
        });

        etUrl.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE) || (actionId == EditorInfo.IME_ACTION_GO) || (actionId == EditorInfo.IME_ACTION_NEXT)) {
                    url = etUrl.getText().toString();
                    if (!TextUtils.isEmpty(url.trim())) {

                        isInvalidUrl = false;

                        if (!url.contains("http://")) {
                            url = "http://" + url;
                            if (Patterns.WEB_URL.matcher(url).matches()) {
                                webview.loadUrl(url);
                            } else {
                                Util.hideKeyboard(LoadMagazineActivity.this, etUrl);
                                mToastFactory.showToast("Please enter a valid url");
                                btnPost.setVisibility(View.INVISIBLE);
                            }
                        } else {
                            if (Patterns.WEB_URL.matcher(url).matches()) {
                                webview.loadUrl(url);
                            } else {
                                Util.hideKeyboard(LoadMagazineActivity.this, etUrl);
                                mToastFactory.showToast("Please enter a valid url");
                                btnPost.setVisibility(View.INVISIBLE);
                            }
                        }
                    } else {
                        Util.hideKeyboard(LoadMagazineActivity.this, etUrl);
                        mToastFactory.showToast("Please enter a url");
                        btnPost.setVisibility(View.INVISIBLE);
                    }
                }
                return false;
            }
        });

        btnPost.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        String accessToken = preferenceEndPoint.getStringPreference("access_token");
        if (magazineId != null) {
            addStoryToExistingMagazine(accessToken);
        } else {
            createMagazineWithStory(accessToken);
        }
    }

    private void createMagazineWithStory(String accessToken) {
        yoService.postStoryMagazineAPI(accessToken, url, magazineTitle, magazineDesc, magazinePrivacy, magazineId).enqueue(new Callback<Articles>() {
            @Override
            public void onResponse(Call<Articles> call, Response<Articles> response) {
                if (response != null && response.body() != null) {
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
                } else if(response != null && response.errorBody() != null){
                    Util.hideKeyboard(LoadMagazineActivity.this, etUrl);
                    mToastFactory.showToast("Magazine Title is already taken");
                }
            }

            @Override
            public void onFailure(Call<Articles> call, Throwable t) {
                // do nothing
            }
        });
    }

    private void addStoryToExistingMagazine(String accessToken) {
        yoService.addStoryMagazineAPI(accessToken, url, magazineId).enqueue(new Callback<Articles>() {
            @Override
            public void onResponse(Call<Articles> call, Response<Articles> response) {
                if (response.body() != null) {
                    setResult(RESULT_OK);
                    finish();
                } else if (response.errorBody() != null) {
                    Util.hideKeyboard(LoadMagazineActivity.this, etUrl);
                    mToastFactory.showToast("Article already added into current magazine");
                }
            }

            @Override
            public void onFailure(Call<Articles> call, Throwable t) {
                // do nothing
            }
        });
    }
}
