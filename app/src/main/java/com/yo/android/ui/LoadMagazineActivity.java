package com.yo.android.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_magazine);

        Intent intent = getIntent();
        magazineId = intent.getStringExtra("MagazineId");

        final EditText etUrl = (EditText) findViewById(R.id.et_enter_url);
        final WebView webview = (WebView) findViewById(R.id.webview);
        webview.getSettings().setJavaScriptEnabled(true);

        Button btnPost = (Button) findViewById(R.id.imv_magazine_post);

        webview.setWebViewClient(new WebViewClient() {
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {

            }
        });

        etUrl.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE) || (actionId == EditorInfo.IME_ACTION_GO) || (actionId == EditorInfo.IME_ACTION_NEXT)) {
                    url = etUrl.getText().toString();
                    webview.loadUrl(url);
                }
                return false;
            }
        });

        btnPost.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        if(!TextUtils.isEmpty(url)) {
            String accessToken = preferenceEndPoint.getStringPreference("access_token");
            yoService.addStoryMagazineAPI(accessToken, url, magazineId).enqueue(new Callback<Articles>() {
                @Override
                public void onResponse(Call<Articles> call, Response<Articles> response) {

                }

                @Override
                public void onFailure(Call<Articles> call, Throwable t) {

                }
            });
        }
        else {
            mToastFactory.showToast("Please enter a url");
        }
    }
}
