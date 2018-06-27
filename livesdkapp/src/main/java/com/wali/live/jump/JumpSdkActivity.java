package com.wali.live.jump;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.base.activity.BaseSdkActivity;
import com.base.log.MyLog;
import com.base.utils.CommonUtils;
import com.base.utils.callback.ICommonCallBack;
import com.mi.live.data.account.channel.HostChannelManager;
import com.mi.live.data.location.Location;
import com.mi.liveassistant.R;
import com.wali.live.common.statistics.StatisticsAlmightyWorker;
import com.wali.live.livesdk.live.LiveSdkActivity;
import com.wali.live.statistics.StatisticsKey;
import com.wali.live.watchsdk.channel.ChannelListSdkActivity;
import com.wali.live.watchsdk.contest.ContestPrepareActivity;
import com.wali.live.watchsdk.contest.ContestWatchActivity;
import com.wali.live.watchsdk.cta.CTANotifyFragment;
import com.wali.live.watchsdk.income.income.UserIncomeActivity;
import com.wali.live.watchsdk.ipc.service.MiLiveSdkBinder;
import com.wali.live.watchsdk.watch.VideoDetailSdkActivity;
import com.wali.live.watchsdk.watch.WatchSdkActivity;
import com.wali.live.watchsdk.watch.model.RoomInfo;

import java.util.ArrayList;

/**
 * Created by lan on 17/2/21.
 */
public class JumpSdkActivity extends BaseSdkActivity {
    private static final String ACTION_OPEN_WATCH = "open_watch";
    private static final String ACTION_OPEN_WATCH_ROOM = "open_watch_room";
    private static final String ACTION_OPEN_WATCH_ROOM_LIST = "open_watch_room_list";
    private static final String ACTION_OPEN_REPLAY = "open_replay";
    private static final String ACTION_OPEN_NORMAL_LIVE = "open_normal_live";
    private static final String ACTION_OPEN_GAME_LIVE = "open_game_live";
    private static final String ACTION_OPEN_CONTEST_PREPARE = "open_contest_prepare";
    private static final String ACTION_OPEN_CONTEST_WATCH = "open_contest_watch";
    private static final String ACTION_OPEN_WITHDRAW = "open_withdraw";
    private static final String ACTION_OPEN_CHANNEL_LIST = "open_channel_list";

    private static final String EXTRA_CHANNEL_ID = "extra_channel_id";
    private static final String EXTRA_PACKAGE_NAME = "extra_package_name";
    private static final String EXTRA_CHANNEL_SECRET = "extra_channel_secret";

    private static final String EXTRA_PLAYER_ID = "extra_player_id";
    private static final String EXTRA_LIVE_ID = "extra_live_id";
    private static final String EXTRA_VIDEO_URL = "extra_video_url";
    private static final String EXTRA_LIVE_TYPE = "extra_live_type";
    private static final String EXTRA_GAME_ID = "extra_game_id";
    private static final String EXTRA_ENABLE_SHARE = "extra_enable_share";
    private static final String EXTRA_ENABLE_FOLLOW = "extra_enable_follow";

    private static final String EXTRA_LOCATION = "extra_location";

    private static final String EXTRA_WATCH_ROOM = "extra_watch_room";
    private static final String EXTRA_WATCH_ROOM_LIST = "extra_watch_room_list";
    private static final String EXTRA_WATCH_ROOM_POSITION = "extra_watch_room_position";

    private boolean hasFinishFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        overridePendingTransition(R.anim.slide_bottom_in, 0);
        setTranslucentStatus(this, true);
        setStatusColor(this, true);

        if (CommonUtils.isNeedShowCtaDialog()) {
            CTANotifyFragment.openFragment(this, android.R.id.content, new CTANotifyFragment.CTANotifyButtonClickListener() {

                @Override
                public void onClickCancelButton() {
                    finish();
                }

                @Override
                public void onClickConfirmButton() {
                    processIntent();
                }
            });
        } else {
            processIntent();
        }

        getWindow().getDecorView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                realFinish();
            }
        });
    }

    @Override
    protected void onStart() {
        MyLog.d(TAG, "onStart");
        super.onStart();
        if(hasFinishFlag){
            realFinish();
        }
    }

    @Override
    protected void onResume() {
        MyLog.d(TAG, "onResume");
        super.onResume();
        if(hasFinishFlag){
            realFinish();
        }
    }

    @Override
    protected void onStop() {
        MyLog.d(TAG, "onStop");

        super.onStop();
    }

    @Override
    protected void onPause() {
        MyLog.d(TAG, "onPause");

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        MyLog.d(TAG, "onDestroy");

        super.onDestroy();
    }

    protected void processIntent() {
        final Intent intent = getIntent();
        if (intent == null) {
            MyLog.w(TAG, "processIntent intent is null");
            finish();
            return;
        }
        String action = intent.getAction();

        final int channelId = intent.getIntExtra(EXTRA_CHANNEL_ID, 0);
        String packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME);
        String channelSecret = intent.getStringExtra(EXTRA_CHANNEL_SECRET);

        final boolean enableShare = intent.getBooleanExtra(EXTRA_ENABLE_SHARE, false);
        final boolean enableFollow = intent.getBooleanExtra(EXTRA_ENABLE_FOLLOW, true);
        HostChannelManager.getInstance().put(channelId, HostChannelManager.KEY_SHARE_ENABLE, enableShare);
        HostChannelManager.getInstance().put(channelId, HostChannelManager.KEY_FOLLOW_ENABLE, enableFollow);

        MyLog.d(TAG, action + " enableShare=" + enableShare);
        switch (action) {
            case ACTION_OPEN_WATCH: {
                final long playerId = intent.getLongExtra(EXTRA_PLAYER_ID, 0);
                final String liveId = intent.getStringExtra(EXTRA_LIVE_ID);
                final String videoUrl = intent.getStringExtra(EXTRA_VIDEO_URL);
                final int liveType = intent.getIntExtra(EXTRA_LIVE_TYPE, 0);
                final String gameId = intent.getStringExtra(EXTRA_GAME_ID);

                MiLiveSdkBinder.getInstance().openWatch(this, channelId, packageName, channelSecret,
                        new ICommonCallBack() {
                            @Override
                            public void process(Object objects) {
                                RoomInfo roomInfo = RoomInfo.Builder.newInstance(playerId, liveId, videoUrl)
                                        .setLiveType(liveType)
                                        .setGameId(gameId)
                                        .setEnableShare(enableShare)
                                        .setEnableRelationChain(enableFollow)
                                        .build();
                                WatchSdkActivity.openActivity(JumpSdkActivity.this, roomInfo);
                            }
                        }, true);
                break;
            }
            case ACTION_OPEN_WATCH_ROOM: {
                final RoomInfo roomInfo = intent.getParcelableExtra(EXTRA_WATCH_ROOM);
                MiLiveSdkBinder.getInstance().openWatch(this, channelId, packageName, channelSecret,
                        new ICommonCallBack() {
                            @Override
                            public void process(Object objects) {
                                if (roomInfo != null) {
                                    roomInfo.setEnableShare(enableShare);
                                    roomInfo.setEnableRelationChain(enableFollow);
                                }
                                WatchSdkActivity.openActivity(JumpSdkActivity.this, roomInfo);
                            }
                        }, true);
                break;
            }
            case ACTION_OPEN_WATCH_ROOM_LIST: {
                final ArrayList<RoomInfo> list = intent.getParcelableArrayListExtra(EXTRA_WATCH_ROOM_LIST);
                final int position = intent.getIntExtra(EXTRA_WATCH_ROOM_POSITION, 0);

                MiLiveSdkBinder.getInstance().openWatch(this, channelId, packageName, channelSecret,
                        new ICommonCallBack() {
                            @Override
                            public void process(Object objects) {
                                for (RoomInfo roomInfo : list) {
                                    if (roomInfo != null) {
                                        roomInfo.setEnableShare(enableShare);
                                        roomInfo.setEnableRelationChain(enableFollow);
                                    }
                                }
                                WatchSdkActivity.openActivity(JumpSdkActivity.this, list, position);
                            }
                        }, true);
                break;
            }
            case ACTION_OPEN_REPLAY: {
                final long playerId = intent.getLongExtra(EXTRA_PLAYER_ID, 0);
                final String liveId = intent.getStringExtra(EXTRA_LIVE_ID);
                final String videoUrl = intent.getStringExtra(EXTRA_VIDEO_URL);
                final int liveType = intent.getIntExtra(EXTRA_LIVE_TYPE, 0);
                final String gameId = intent.getStringExtra(EXTRA_GAME_ID);

                MiLiveSdkBinder.getInstance().openReplay(this, channelId, packageName, channelSecret,
                        new ICommonCallBack() {
                            @Override
                            public void process(Object objects) {
                                reportReplay(channelId);

                                RoomInfo roomInfo = RoomInfo.Builder.newInstance(playerId, liveId, videoUrl)
                                        .setLiveType(liveType)
                                        .setGameId(gameId)
                                        .setEnableShare(enableShare)
                                        .setEnableRelationChain(enableFollow)
                                        .build();
                                VideoDetailSdkActivity.openActivity(JumpSdkActivity.this, roomInfo);
                            }
                        }, true);
                break;
            }
            case ACTION_OPEN_NORMAL_LIVE: {
                final Location location = intent.getParcelableExtra(EXTRA_LOCATION);
                MiLiveSdkBinder.getInstance().openNormalLive(this, channelId, packageName, channelSecret,
                        new ICommonCallBack() {
                            @Override
                            public void process(Object objects) {
                                reportLive(channelId);
                                LiveSdkActivity.openActivity(JumpSdkActivity.this, location, enableShare, false);
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
                                reportLive(channelId);
                                LiveSdkActivity.openActivity(JumpSdkActivity.this, location, enableShare, true);
                            }
                        }, true);
                break;
            }
            case ACTION_OPEN_CONTEST_PREPARE: {
                MiLiveSdkBinder.getInstance().checkActivitySecure(this, channelId, packageName, channelSecret,
                        new ICommonCallBack() {
                            @Override
                            public void process(Object objects) {
                                ContestPrepareActivity.open(JumpSdkActivity.this, 0);
                            }
                        }, true, ACTION_OPEN_CONTEST_PREPARE);
                break;
            }
            case ACTION_OPEN_CONTEST_WATCH: {
                final long playerId = intent.getLongExtra(EXTRA_PLAYER_ID, 0);
                final String liveId = intent.getStringExtra(EXTRA_LIVE_ID);
                final String videoUrl = intent.getStringExtra(EXTRA_VIDEO_URL);

                MiLiveSdkBinder.getInstance().checkActivitySecure(this, channelId, packageName, channelSecret,
                        new ICommonCallBack() {
                            @Override
                            public void process(Object objects) {
                                ContestWatchActivity.open(JumpSdkActivity.this, playerId, liveId, videoUrl);
                            }
                        }, true, ACTION_OPEN_CONTEST_WATCH);
                break;
            }
            case ACTION_OPEN_WITHDRAW: {
                MiLiveSdkBinder.getInstance().checkActivitySecure(this, channelId, packageName, channelSecret,
                        new ICommonCallBack() {
                            @Override
                            public void process(Object objects) {
                                UserIncomeActivity.openActivity(JumpSdkActivity.this);
                            }
                        }, true, ACTION_OPEN_WITHDRAW);
                break;
            }
            case ACTION_OPEN_CHANNEL_LIST: {
                MiLiveSdkBinder.getInstance().checkActivitySecure(this, channelId, packageName, channelSecret,
                        new ICommonCallBack() {
                            @Override
                            public void process(Object objects) {
                                ChannelListSdkActivity.openActivity(JumpSdkActivity.this);
                            }
                        }, true, ACTION_OPEN_CHANNEL_LIST);
                break;
            }
            default: {
                finish();
                break;
            }
        }
    }

    private void reportReplay(int channelId) {
        try {
            String key = String.format(StatisticsKey.KEY_REPLAY_COUNT, channelId);
            MyLog.w(TAG, "reportReplay key=" + key);
            StatisticsAlmightyWorker.getsInstance().recordImmediatelyDefault(key, 1);
        } catch (Exception e) {
            MyLog.e(TAG, "reportReplay e", e);
        }
    }

    private void reportLive(int channelId) {
        try {
            String key = String.format(StatisticsKey.KEY_LIVE_COUNT, channelId);
            MyLog.w(TAG, "reportLive key=" + key);
            StatisticsAlmightyWorker.getsInstance().recordImmediatelyDefault(key, 1);
        } catch (Exception e) {
            MyLog.e(TAG, "reportLive e", e);
        }
    }

    /**
     * 这里不能马上把 activity 销毁，因为会导致有jump启动的Activity A 退出动画无效
     * 所以只能延迟finish
     */
    @Override
    public void finish() {
        MyLog.w(TAG, "finish");
        hasFinishFlag = true;
    }

    public void realFinish(){
        super.finish();
        // 给音乐用的
        overridePendingTransition(0, R.anim.slide_bottom_out);
    }

    @Override
    public void onBackPressed() {
        realFinish();
    }
}
