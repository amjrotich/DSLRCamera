package com.cradleshyft.dslrcamera.view.viewer;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.cradleshyft.dslrcamera.R;

public class VideoActivity extends AppCompatActivity {

    public static final String VIDEO_KEY = "video_key";
    //
    private String videoPath;
    //
    private ExoPlayer simpleExoPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        getExtras();
        init();
        playVideo();
    }

    private void playVideo() {
        this.simpleExoPlayer.setMediaItem(MediaItem.fromUri(videoPath));
        this.simpleExoPlayer.setPlayWhenReady(true);
        this.simpleExoPlayer.seekTo(0, 0);
        this.simpleExoPlayer.prepare();
    }

    private void init() {
        this.simpleExoPlayer = new ExoPlayer.Builder(getBaseContext()).build();
        StyledPlayerView playerView = findViewById(R.id.playerView);
        playerView.setPlayer(simpleExoPlayer);
    }

    private void getExtras() {
        try {
            this.videoPath = getIntent().getExtras().getString(VideoActivity.VIDEO_KEY, "");
        } catch (NullPointerException ignored) {
        }
    }

    @Override
    protected void onPause() {
        pausePlayer();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        releasePlayer();
        super.onDestroy();
    }

    public void releasePlayer() {
        if (this.simpleExoPlayer != null) {
            this.simpleExoPlayer.release();
            this.simpleExoPlayer = null;
        }
    }

    private void pausePlayer() {
        if (simpleExoPlayer != null && simpleExoPlayer.isPlaying()) {
            simpleExoPlayer.pause();
        }
    }
}