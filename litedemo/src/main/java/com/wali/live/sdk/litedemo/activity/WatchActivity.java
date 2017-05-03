package com.wali.live.sdk.litedemo.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.mi.liveassistant.room.manager.watch.WatchManager;
import com.mi.liveassistant.room.manager.watch.callback.IWatchCallback;
import com.wali.live.sdk.litedemo.R;
import com.wali.live.sdk.litedemo.base.activity.RxActivity;
import com.wali.live.sdk.litedemo.utils.ToastUtils;

/**
 * Created by lan on 17/5/3.
 */
public class WatchActivity extends RxActivity {
    public static final String EXTRA_PLAYER_ID = "player_id";
    public static final String EXTRA_LIVE_ID = "live_id";

    private WatchManager mWatchManager;

    private RelativeLayout mSurfaceContainer;

    private long mPlayerId;
    private String mLiveId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch);

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        initData();
        initView();
        initManager();
    }

    private void initData() {
        Intent data = getIntent();
        if (data == null) {
            finish();
            return;
        }
        mPlayerId = data.getLongExtra(EXTRA_PLAYER_ID, 0);
        mLiveId = data.getStringExtra(EXTRA_LIVE_ID);
    }

    private void initView() {
        mSurfaceContainer = $(R.id.surface_container);
    }

    private void initManager() {
        mWatchManager = new WatchManager();
        mWatchManager.setContainerView(mSurfaceContainer);
        mWatchManager.enterLive(mPlayerId, mLiveId, new IWatchCallback() {
            @Override
            public void notifyFail(int errCode) {
                ToastUtils.showToast("enter live fail=" + errCode);
            }

            @Override
            public void notifySuccess() {
                ToastUtils.showToast("enter live success");
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWatchManager.leaveLive();
    }

    public static void openActivity(Activity activity, long playerId, String liveId) {
        Intent intent = new Intent(activity, WatchActivity.class);
        intent.putExtra(EXTRA_PLAYER_ID, playerId);
        intent.putExtra(EXTRA_LIVE_ID, liveId);
        activity.startActivity(intent);
    }
}
