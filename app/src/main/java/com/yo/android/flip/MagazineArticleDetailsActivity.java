package com.yo.android.flip;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.yo.android.R;
import com.yo.android.ui.BaseActivity;

public class MagazineArticleDetailsActivity extends BaseActivity {
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_magazine_article_details);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        String title = intent.getStringExtra("Title");
        //String detailedDesc = intent.getStringExtra("DetailedDesc");
        String image = intent.getStringExtra("Image");

        getSupportActionBar().setTitle(title);
       /* UI
                .<TextView>findViewById(this, R.id.tv_article_long_desc)
                .setText(detailedDesc);*/
        /*ImageView photoView = UI.findViewById(this, R.id.photo);
        // load image
        try {
            // get input stream
            InputStream ims = getAssets().open(image);
            // load image as Drawable
            Drawable d = Drawable.createFromStream(ims, null);
            // set image to ImageView
            photoView.setImageDrawable(d);
        } catch (IOException ex) {
            return;
        }*/

        WebView webview = (WebView) findViewById(R.id.webview);
        webview.getSettings().setJavaScriptEnabled(true);

        webview.setWebViewClient(new WebViewClient() {
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                //Toast.makeText(activity, description, Toast.LENGTH_SHORT).show();
            }
        });

        webview.loadUrl(image);
        progressBar = (ProgressBar) findViewById(R.id.webview_progressbar);
        webview.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                if (newProgress >= 100) {
                    progressBar.setVisibility(View.GONE);
                } else {
                    progressBar.setProgress(newProgress);
                    progressBar.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
