package com.yo.android.flip;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.yo.android.R;
import com.yo.android.adapters.MagazineArticlesBaseAdapter;
import com.yo.android.api.YoApi;
import com.yo.android.model.Articles;
import com.yo.android.ui.BaseActivity;
import com.yo.android.ui.CreateMagazineActivity;
import com.yo.android.util.Constants;
import com.yo.android.util.Util;
import com.yo.dialer.DialerConfig;
import com.yo.dialer.YoSipService;
import com.yo.dialer.googlesheet.UploadCallDetails;
import com.yo.dialer.googlesheet.UploadModel;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * The activity used to display the article content in a WebView
 */
public class MagazineArticleDetailsActivity extends BaseActivity {
    private Articles data;
    private int position;
    private String articlePlacement;

    @BindView(R.id.cb_magazine_like)
    CheckBox magazineLike;
    @BindView(R.id.imv_magazine_add)
    ImageView magazineAdd;
    @BindView(R.id.imv_magazine_share)
    ImageView magazineShare;
    @BindView(R.id.webview_progressbar)
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_magazine_article_details);
        ButterKnife.bind(this);
        enableBack();

        Intent intent = getIntent();
        String title = intent.getStringExtra("Title");
        String image = intent.getStringExtra("Image");
        data = intent.getParcelableExtra("Article");
        position = intent.getIntExtra("Position", 0);
        if (intent.hasExtra("ArticlePlacement")) {
            articlePlacement = intent.getStringExtra("ArticlePlacement");
        } else {
            articlePlacement = "";
        }

        setTitleHideIcon(title);

        WebView webview = (WebView) findViewById(R.id.webview);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setAllowContentAccess(true);
        webview.getSettings().setBuiltInZoomControls(true);
        webview.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        webview.getSettings().setLoadWithOverviewMode(true);
        webview.getSettings().setUseWideViewPort(true);
        webview.getSettings().setDomStorageEnabled(true);

        webview.setWebViewClient(new WebViewClient() {
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                // do nothing
            }
        });

        webview.loadUrl(image);
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

        magazineLike.setOnCheckedChangeListener(null);
        if (data != null) {
            if ("true".equals(data.getLiked())) {
                data.setIsChecked(true);
            } else {
                data.setIsChecked(false);
            }

            magazineLike.setChecked(data.isChecked());

            magazineLike.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    data.setIsChecked(isChecked);
                    if (isChecked) {
                        showProgressDialog();
                        String accessToken = preferenceEndPoint.getStringPreference(YoApi.ACCESS_TOKEN);
                        yoService.likeArticlesAPI(data.getId(), accessToken).enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                try {
                                    dismissProgressDialog();

                                    data.setIsChecked(true);
                                    data.setLiked("true");
                                    MagazineArticlesBaseAdapter.initListener();
                                    if (MagazineArticlesBaseAdapter.reflectListener != null) {
                                        MagazineArticlesBaseAdapter.reflectListener.updateFollowOrLikesStatus(data, Constants.LIKE_EVENT);
                                    }
                                    mToastFactory.showToast(R.string.liked_article + data.getTitle());
                                } finally {
                                    if (response != null && response.body() != null) {
                                        response.body().close();
                                    }
                                }
                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                dismissProgressDialog();
                                Toast.makeText(MagazineArticleDetailsActivity.this, R.string.like_article_error + data.getTitle(), Toast.LENGTH_LONG).show();
                                data.setIsChecked(false);
                                data.setLiked("false");
                            }
                        });
                    } else {
                        showProgressDialog();
                        String accessToken = preferenceEndPoint.getStringPreference(YoApi.ACCESS_TOKEN);
                        yoService.unlikeArticlesAPI(data.getId(), accessToken).enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                try {
                                    dismissProgressDialog();
                                    data.setIsChecked(false);
                                    data.setLiked("false");
                                    MagazineArticlesBaseAdapter.initListener();
                                    if (MagazineArticlesBaseAdapter.reflectListener != null) {
                                        MagazineArticlesBaseAdapter.reflectListener.updateFollowOrLikesStatus(data, Constants.LIKE_EVENT);
                                    }
                                    mToastFactory.showToast(R.string.unlike_article + data.getTitle());
                                } finally {
                                    if (response != null && response.body() != null) {
                                        response.body().close();
                                    }
                                }
                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                dismissProgressDialog();
                                Toast.makeText(MagazineArticleDetailsActivity.this, R.string.unlike_article_error + data.getTitle(), Toast.LENGTH_LONG).show();
                                data.setIsChecked(true);
                                data.setLiked("true");
                            }
                        });
                    }
                }
            });

            if (magazineAdd != null) {
                ImageView add = magazineAdd;
                add.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(MagazineArticleDetailsActivity.this, CreateMagazineActivity.class);
                        intent.putExtra(Constants.MAGAZINE_ADD_ARTICLE_ID, data.getId());
                        startActivityForResult(intent, Constants.ADD_ARTICLES_TO_MAGAZINE);
                    }
                });
            }

            if (magazineShare != null) {
                ImageView share = magazineShare;
                share.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (data.getImage_filename() != null) {
                            new Util.ImageLoaderTask(v, data).execute(data.getImage_filename());
                        } else {
                            String summary = Html.fromHtml(data.getSummary()).toString();
                            Util.shareNewIntent(v, data.getGenerated_url(), "Article: " + data.getTitle(), summary, null);
                        }
                    }
                });
            }
        }

        // Capture user id and article title
        Map<String, String> articleReadParams = new HashMap<String, String>();
        String userId = preferenceEndPoint.getStringPreference(Constants.USER_ID);
        //param keys and values have to be of String type
        articleReadParams.put("UserId", userId);
        articleReadParams.put("ArticleTitle", title);

        FlurryAgent.logEvent("Reading article", articleReadParams);
        if (DialerConfig.UPLOAD_REPORTS_GOOGLE_SHEET) {
            UploadModel model = new UploadModel(preferenceEndPoint);
            if (data != null) {
                // Topic Name
                model.setNotificationType(data.getTopicName());
            }
            // Article Title
            model.setNotificationDetails(title);
            //String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
            model.setCaller(preferenceEndPoint.getStringPreference(Constants.VOX_USER_NAME));
            Calendar c = Calendar.getInstance();
            String formattedDate = YoSipService.df.format(c.getTime());
            model.setDate(formattedDate);
            Date d = new Date();
            String currentDateTimeString = YoSipService.sdf.format(d);
            model.setTime(currentDateTimeString);

            try {
                UploadCallDetails.postDataFromApi(model, "Magazines");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent();
            if (data != null) {
                intent.putExtra("UpdatedArticle", data);
            }
            intent.putExtra("Pos", position);
            intent.putExtra("ArticlePlace", articlePlacement);
            setResult(RESULT_OK, intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

    @Override
    public void onBackPressed() {

        Intent intent = new Intent();
        if (data != null) {
            intent.putExtra("UpdatedArticle", data);
        }
        intent.putExtra("Pos", position);
        intent.putExtra("ArticlePlace", articlePlacement);
        setResult(RESULT_OK, intent);
        finish();

        super.onBackPressed();
    }

}
