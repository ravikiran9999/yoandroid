package com.yo.android.video;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.yo.android.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.yo.android.util.Constants.VIDEO_URL;

public class InAppVideoActivity extends YouTubeBaseActivity implements YouTubePlayer.OnInitializedListener  {

    private static final String TAG = InAppVideoActivity.class.getSimpleName();
    private static final int RECOVERY_DIALOG_REQUEST = 1;
    //https://www.youtube.com/watch?v=<VIDEO_ID>

    private String mVideoUrl;


    public static void start(Activity activity, String youtubeUrl) {
        Intent intent = new Intent(activity, InAppVideoActivity.class);
        intent.putExtra(VIDEO_URL, youtubeUrl);
        activity.startActivityForResult(intent, 500);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        final String API_KEY = getString(R.string.google_api_key);
        mVideoUrl = getIntent().getStringExtra(VIDEO_URL);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Initializing YouTube player view
        YouTubePlayerView youTubePlayerView = (YouTubePlayerView) findViewById(R.id.youtube_player_view);
        youTubePlayerView.initialize(API_KEY, this);
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {

        if (!b) {

            // loadVideo() will auto play video
            youTubePlayer.loadVideo(getVideoId(mVideoUrl));

            youTubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.DEFAULT);
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
