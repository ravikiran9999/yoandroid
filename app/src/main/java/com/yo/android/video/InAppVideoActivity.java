package com.yo.android.video;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.yo.android.R;
import com.yo.android.util.Constants;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.Bind;
import butterknife.ButterKnife;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static com.yo.android.util.Constants.VIDEO_URL;

public class InAppVideoActivity extends YouTubeFailureRecoveryActivity implements
        YouTubePlayer.OnFullscreenListener {

    private static final String TAG = InAppVideoActivity.class.getSimpleName();
    private static final int RECOVERY_DIALOG_REQUEST = 1;

    @Bind(R.id.layout)
    LinearLayout baseLayout;
    @Bind(R.id.player)
    YouTubePlayerView playerView;

    //https://www.youtube.com/watch?v=<VIDEO_ID>
    private String mVideoUrl;

    private boolean fullscreen;

    public static void start(Activity activity, String youtubeUrl, String title) {
        Intent intent = new Intent(activity, InAppVideoActivity.class);
        intent.putExtra(Constants.TITLE, title);
        intent.putExtra(VIDEO_URL, youtubeUrl);
        activity.startActivityForResult(intent, 500);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        ButterKnife.bind(this);

        playerView.initialize(DeveloperKey.DEVELOPER_KEY, this);
        mVideoUrl = getIntent().getStringExtra(VIDEO_URL);
        initActionbar();
        doLayout();

    }

    private void initActionbar() {
        ActionBar actionBar = getActionBar();
        if(actionBar != null) {
            actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimary)));
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getIntent().getStringExtra(Constants.TITLE));
        }
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {

        // Specify that we want to handle fullscreen behavior ourselves.
        youTubePlayer.addFullscreenControlFlag(YouTubePlayer.FULLSCREEN_FLAG_CUSTOM_LAYOUT);
        youTubePlayer.setOnFullscreenListener(this);

        if (!b) {
            // loadVideo() will auto play video
            youTubePlayer.cueVideo(getVideoId(mVideoUrl));
        }
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {

        if (youTubeInitializationResult.isUserRecoverableError()) {
            youTubeInitializationResult.getErrorDialog(this, RECOVERY_DIALOG_REQUEST).show();
        } else {

            Toast.makeText(this, youTubeInitializationResult.toString(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected YouTubePlayer.Provider getYouTubePlayerProvider() {
        return playerView;
    }

    private void doLayout() {
        LinearLayout.LayoutParams playerParams =
                (LinearLayout.LayoutParams) playerView.getLayoutParams();
        if (fullscreen) {
            playerParams.width = LinearLayout.LayoutParams.MATCH_PARENT;
            playerParams.height = LinearLayout.LayoutParams.MATCH_PARENT;
        } else {
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                playerParams.width = MATCH_PARENT;
                playerParams.height = WRAP_CONTENT;
                playerParams.weight = 1;
                baseLayout.setOrientation(LinearLayout.HORIZONTAL);
            } else {
                playerParams.width = MATCH_PARENT;
                playerParams.height = WRAP_CONTENT;
                playerParams.weight = 0;
                baseLayout.setOrientation(LinearLayout.VERTICAL);
            }
        }
    }

    @Override
    public void onFullscreen(boolean isFullscreen) {
        fullscreen = isFullscreen;
        doLayout();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        doLayout();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            setResult(RESULT_OK);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private String getVideoId(String youtubeUrl) {
        String pattern = "(?<=watch\\?v=|/videos/|embed\\/)[^#\\&\\?]*";

        Pattern compiledPattern = Pattern.compile(pattern);
        Matcher matcher = compiledPattern.matcher(youtubeUrl);

        if(matcher.find()){
            return matcher.group();
        }
        return "";
    }
}
