package com.wali.live.jump;

import android.content.Intent;
import android.os.Bundle;

import com.base.activity.BaseSdkActivity;
import com.base.log.MyLog;
import com.base.utils.callback.ICommonCallBack;
import com.mi.live.data.location.Location;
import com.wali.live.TestSdkActivity;
import com.wali.live.livesdk.live.LiveSdkActivity;
import com.wali.live.watchsdk.ipc.service.MiLiveSdkBinder;

/**
 * Created by lan on 17/2/21.
 */
public class JumpSdkActivity extends BaseSdkActivity {
    private static final String ACTION_OPEN_WATCH = "open_watch";
    private static final String ACTION_OPEN_REPLAY = "open_replay";
    private static final String ACTION_OPEN_NORMAL_LIVE = "open_normal_live";
    private static final String ACTION_OPEN_GAME_LIVE = "open_game_live";

    private static final String EXTRA_CHANNEL_ID = "extra_channel_id";
    private static final String EXTRA_PACKAGE_NAME = "extra_package_name";
    private static final String EXTRA_CHANNEL_SECRET = "extra_channel_secret";

    private static final String EXTRA_PLAYER_ID = "extra_player_id";
    private static final String EXTRA_LIVE_ID = "extra_live_id";
    private static final String EXTRA_VIDEO_URL = "extra_video_url";
    private static final String EXTRA_LIVE_TYPE = "extra_live_type";

    private static final String EXTRA_LOCATION = "extra_location";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTranslucentStatus(this, true);
        setStatusColor(this, true);

        processIntent();
    }

    private void processIntent() {
        Intent intent = getIntent();
        if (intent == null) {
            MyLog.w(TAG, "processIntent intent is null");
            finish();
            return;
        }
        String action = intent.getAction();

        int channelId = intent.getIntExtra(EXTRA_CHANNEL_ID, 0);
        String packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME);
        String channelSecret = intent.getStringExtra(EXTRA_CHANNEL_SECRET);
        switch (action) {
            case ACTION_OPEN_WATCH: {
                long playerId = intent.getLongExtra(EXTRA_PLAYER_ID, 0);
                String liveId = intent.getStringExtra(EXTRA_LIVE_ID);
                String videoUrl = intent.getStringExtra(EXTRA_VIDEO_URL);
                int liveType = intent.getIntExtra(EXTRA_LIVE_TYPE, 0);
                MiLiveSdkBinder.getInstance().openWatch(this, channelId, packageName, channelSecret,
                        playerId, liveId, videoUrl, liveType, true);
                break;
            }
            case ACTION_OPEN_REPLAY: {
                long playerId = intent.getLongExtra(EXTRA_PLAYER_ID, 0);
                String liveId = intent.getStringExtra(EXTRA_LIVE_ID);
                String videoUrl = intent.getStringExtra(EXTRA_VIDEO_URL);
                int liveType = intent.getIntExtra(EXTRA_LIVE_TYPE, 0);
                MiLiveSdkBinder.getInstance().openReplay(this, channelId, packageName, channelSecret,
                        playerId, liveId, videoUrl, liveType, true);
                break;
            }
            case ACTION_OPEN_NORMAL_LIVE: {
                final Location location = intent.getParcelableExtra(EXTRA_LOCATION);
                MiLiveSdkBinder.getInstance().openNormalLive(this, channelId, packageName, channelSecret,
                        new ICommonCallBack() {
                            @Override
                            public void process(Object objects) {
                                LiveSdkActivity.openActivity(JumpSdkActivity.this, location, false);
                            }
                        }, true);
                break;
            }
            case ACTION_OPEN_GAME_LIVE: {
                final Location location = intent.getParcelableExtra(EXTRA_LOCATION);
                MiLiveSdkBinder.getInstance().openGameLive(this, channelId, packageName, channelSecret,
                        new ICommonCallBack() {
                            @Override
                            public void process(Object objects) {
                                LiveSdkActivity.openActivity(JumpSdkActivity.this, location, true);
                            }
                        }, true);
                break;
            }
            default: {
                finish();
                break;
            }
        }
    }

    private void openRandomLive() {
        Intent intent = new Intent(this, TestSdkActivity.class);
        startActivity(intent);
        finish();
    }
}
