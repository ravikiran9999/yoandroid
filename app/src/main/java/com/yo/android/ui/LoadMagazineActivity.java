package com.yo.android.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
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
import com.yo.android.model.OwnMagazine;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_magazine);

        Intent intent = getIntent();
        magazineId = intent.getStringExtra("MagazineId");
        magazineTitle = intent.getStringExtra("MagazineTitle");

        final EditText etUrl = (EditText) findViewById(R.id.et_enter_url);
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
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {

            }
        });

        etUrl.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE) || (actionId == EditorInfo.IME_ACTION_GO) || (actionId == EditorInfo.IME_ACTION_NEXT)) {
                    url = etUrl.getText().toString();
                    if(!TextUtils.isEmpty(url.trim())) {

                        if(!url.contains("http://")) {
                            url = "http://" + url;
                            if (Patterns.WEB_URL.matcher(url).matches()) {
                                webview.loadUrl(url);
                                btnPost.setVisibility(View.VISIBLE);
                            } else {
                                mToastFactory.showToast("Please enter a valid url");
                            }
                        }
                        else {
                            if (Patterns.WEB_URL.matcher(url).matches()) {
                                webview.loadUrl(url);
                                btnPost.setVisibility(View.VISIBLE);
                            } else {
                                mToastFactory.showToast("Please enter a valid url");
                            }
                        }
                    }
                    else {
                        mToastFactory.showToast("Please enter a url");
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
                yoService.addStoryMagazineAPI(accessToken, url, magazineId).enqueue(new Callback<Articles>() {
                    @Override
                    public void onResponse(Call<Articles> call, Response<Articles> response) {
                        Intent intent = new Intent(LoadMagazineActivity.this, CreatedMagazineDetailActivity.class);
                       /* intent.putExtra("ArticleId", response.body().getId());
                        intent.putExtra("ArticleTitle", response.body().getTitle());
                        intent.putExtra("ArticleUrl", response.body().getUrl());
                        intent.putExtra("ArticleSummary", response.body().getSummary());
                        intent.putExtra("ArticleImage", response.body().getImage_filename());*/
                        intent.putExtra("MagazineTitle", magazineTitle);
                        intent.putExtra("MagazineId", magazineId);
                        intent.putExtra("MagazineDesc", response.body().getSummary());
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onFailure(Call<Articles> call, Throwable t) {

                    }
                });
    }
}
