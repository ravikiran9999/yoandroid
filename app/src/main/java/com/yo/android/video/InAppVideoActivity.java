package com.yo.android.video;

import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.yo.android.R;

public class InAppVideoActivity extends YouTubeBaseActivity implements YouTubePlayer.OnInitializedListener  {

    private static final int RECOVERY_DIALOG_REQUEST = 1;
    public static final String API_KEY = "AIzaSyA8dZKgumHN0Xy6DYl3LQ0SKTtDMvphLR4";

    //https://www.youtube.com/watch?v=<VIDEO_ID>
    public static final String VIDEO_ID = "-m3V8w_7vhk";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        // Initializing YouTube player view
        YouTubePlayerView youTubePlayerView = (YouTubePlayerView) findViewById(R.id.youtube_player_view);
        youTubePlayerView.initialize(API_KEY, this);
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {

        if (!b) {

            // loadVideo() will auto play video
            // Use cueVideo() method, if you don't want to play it automatically
            youTubePlayer.loadVideo(VIDEO_ID);

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
}
