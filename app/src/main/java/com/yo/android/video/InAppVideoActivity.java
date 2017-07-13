package com.yo.android.video;

import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveVideoTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.PlaybackControlView;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.util.Util;
import com.yo.android.R;
import com.yo.android.app.BaseApp;
import com.yo.android.ui.BaseActivity;

public class InAppVideoActivity extends BaseActivity implements ExoPlayer.EventListener, PlaybackControlView.VisibilityListener {

    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();
    public static final String EXTENSION_EXTRA = "extension";
    private DataSource.Factory mediaDataSourceFactory;
    private DefaultTrackSelector trackSelector;
    private TrackSelectionHelper trackSelectionHelper;
    private SimpleExoPlayer player;

    private boolean shouldAutoPlay;
    private int resumeWindow;
    private long resumePosition;
    private Handler mainHandler;
    private boolean needRetrySource;
    Uri uri;
    SimpleExoPlayerView simpleExoPlayerView;
    RelativeLayout loading;

    private Runnable updatePlayer = new Runnable() {
        @Override
        public void run() {
            if (player != null) {
                switch (player.getPlaybackState()) {
                    case ExoPlayer.STATE_BUFFERING:
                        loading.setVisibility(View.VISIBLE);
                        break;

                    case ExoPlayer.STATE_IDLE:
                        loading.setVisibility(View.GONE);
                        break;
                    case ExoPlayer.STATE_READY:
                        loading.setVisibility(View.GONE);
                        break;

                    default:
                        break;
                }
            }
            mainHandler.postDelayed(updatePlayer, 100);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        shouldAutoPlay = true;
        mediaDataSourceFactory = buildDataSourceFactory(true);
        mainHandler = new Handler();
        setContentView(R.layout.activity_video);
        loading = (RelativeLayout) findViewById(R.id.loading);

        //uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.videoplayback);
        uri = Uri.parse("https://storage.googleapis.com/exoplayer-test-media-1/gen-3/screens/dash-vod-single-segment/ve ideo-137.mp4");
        simpleExoPlayerView = (SimpleExoPlayerView) findViewById(R.id.player_view);
        simpleExoPlayerView.setControllerVisibilityListener(this);
        simpleExoPlayerView.requestFocus();

    }

    @Override
    public void onStart() {
        super.onStart();
        if (Util.SDK_INT > 23) {
            initializePlayer();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if ((Util.SDK_INT <= 23 || player == null)) {
            initializePlayer();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT <= 23) {
            releasePlayer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT > 23) {
            releasePlayer();
        }
    }

    @Override
    public void onVisibilityChange(int visibility) {

    }

    private void initializePlayer() {
        Intent intent = getIntent();
        boolean needNewPlayer = player == null;
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveVideoTrackSelection.Factory(BANDWIDTH_METER);
        trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
        trackSelectionHelper = new TrackSelectionHelper(trackSelector, videoTrackSelectionFactory);
        player = ExoPlayerFactory.newSimpleInstance(this, trackSelector, new DefaultLoadControl(),
                null, 0);
        player.addListener(this);

        simpleExoPlayerView.setPlayer(player);
        player.setPlayWhenReady(shouldAutoPlay);

        if (needNewPlayer || needRetrySource) {
            String action = intent.getAction();
            Uri[] uris;
            String[] extensions;
            //if (ACTION_VIEW.equals(action)) {
            uris = new Uri[]{uri};
            extensions = new String[]{intent.getStringExtra(EXTENSION_EXTRA)};
            /*} else {
                //showToast(getString(R.string.unexpected_intent_action, action));
                return;
            }*/
            if (Util.maybeRequestReadExternalStoragePermission(this, uris)) {
                // The player will be reinitialized if the permission is granted.
                return;
            }
            MediaSource[] mediaSources = new MediaSource[uris.length];
            for (int i = 0; i < uris.length; i++) {
                mediaSources[i] = buildMediaSource(uri, extensions[i]);
            }
            MediaSource mediaSource = mediaSources.length == 1 ? mediaSources[0]
                    : new ConcatenatingMediaSource(mediaSources);
            boolean haveResumePosition = resumeWindow != C.INDEX_UNSET;
            /*if (haveResumePosition) {
                player.seekTo(resumeWindow, resumePosition);
            }*/
            player.prepare(mediaSource, !haveResumePosition, false);
            needRetrySource = false;
            //updateButtonVisibilities();
            loading.setVisibility(View.VISIBLE);
            mainHandler.postDelayed(updatePlayer, 100);
        }
    }

    private MediaSource buildMediaSource(Uri uri, String overrideExtension) {
        int type = TextUtils.isEmpty(overrideExtension) ? Util.inferContentType(uri.toString())
                : Util.inferContentType("." + overrideExtension);
        switch (type) {
            case C.TYPE_OTHER:
                return new ExtractorMediaSource(uri, mediaDataSourceFactory, new DefaultExtractorsFactory(),
                        mainHandler, null);
            default: {
                throw new IllegalStateException("Unsupported type: " + type);
            }
        }
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {

    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    @Override
    public void onLoadingChanged(boolean isLoading) {

    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {

    }

    @Override
    public void onPositionDiscontinuity() {

    }

    private void releasePlayer() {
        if (player != null) {
            //debugViewHelper.stop();
            //debugViewHelper = null;
            shouldAutoPlay = player.getPlayWhenReady();
            //updateResumePosition();
            player.release();
            player = null;
            trackSelector = null;
            trackSelectionHelper = null;
            //eventLogger = null;
        }
    }

    private DataSource.Factory buildDataSourceFactory(boolean useBandwidthMeter) {
        return ((BaseApp) getApplication())
                .buildDataSourceFactory(useBandwidthMeter ? BANDWIDTH_METER : null);
    }
}
