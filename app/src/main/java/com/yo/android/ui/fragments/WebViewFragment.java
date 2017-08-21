package com.yo.android.ui.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;


import com.yo.android.R;
import com.yo.android.chat.ui.fragments.BaseFragment;
import com.yo.android.util.Util;

import butterknife.Bind;
import butterknife.ButterKnife;

public abstract class WebViewFragment extends BaseFragment
         {

    @Bind(R.id.webview_stub) ViewStub webViewStub;
    WebView webView;

    protected String urlToLoad;
    protected String domainUrl;

    protected abstract void initToolbar();
    protected abstract void onWebViewInflated();
    protected abstract void onPageStartedCallback();
    protected abstract void onPageFinishedCallback();
    protected abstract boolean isCacheSettingEnabled();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_webview, container, false);
        ButterKnife.bind(this, rootView);

        initToolbar();
        initView();

        return rootView;
    }

    private void initView() {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                webView = (WebView) webViewStub.inflate();
                onWebViewInflated();
                initWebView();
            }
        });
    }

    private void initWebView() {
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);

                onPageStartedCallback();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                onPageFinishedCallback();
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (domainUrl.equals(Uri.parse(url).getHost())) {
                    return false;
                }

                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);

                return true;
            }
        });

        setupWebSettings();

        webView.loadUrl(urlToLoad);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebSettings() {
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);

        if (isCacheSettingEnabled()) {
            webSettings.setAppCachePath(getActivity().getApplicationContext().getCacheDir().getAbsolutePath());
            webSettings.setAllowFileAccess(true);
            webSettings.setAppCacheEnabled(true);
            if (Util.isOnline(getActivity())) {
                webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
            }
            else {
                webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
            }
        }
    }

    protected void webViewBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        }
    }

    protected void webViewForwardPressed() {
        if (webView.canGoForward()) {
            webView.goForward();
        }
    }

    protected boolean canGoBack() {
        return webView.canGoBack();
    }

    protected boolean canGoForward() {
        return webView.canGoForward();
    }

    protected void loadUrl(String url) {
        webView.loadUrl(url);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (webView != null) {
            webView.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (webView != null) {
            webView.onPause();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (webView != null) {
            webView.destroy();
        }
    }

}