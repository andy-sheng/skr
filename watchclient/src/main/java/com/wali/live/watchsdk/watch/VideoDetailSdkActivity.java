package com.wali.live.watchsdk.watch;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.WindowManager;

import com.base.log.MyLog;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.base.BaseComponentSdkActivity;
import com.wali.live.watchsdk.videodetail.VideoDetailController;
import com.wali.live.watchsdk.videodetail.VideoDetailView;
import com.wali.live.watchsdk.watch.model.RoomInfo;

/**
 * Created by yangli on 2017/5/26.
 */
public class VideoDetailSdkActivity extends BaseComponentSdkActivity {

    private VideoDetailController mComponentController;
    private VideoDetailView mSdkView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if (isMIUIV6()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        } else {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_detail_layout);
        initData();
        initView();
    }

    @Override
    public boolean isKeyboardResize() {
        return false;
    }

    @Override
    protected void trySendDataWithServerOnce() {
    }

    @Override
    protected void tryClearData() {
    }

    @Override
    public void onKickEvent(String msg) {
    }

    private void initData() {
        Intent data = getIntent();
        if (data == null) {
            return;
        }
        mRoomInfo = (RoomInfo) data.getParcelableExtra(EXTRA_ROOM_INFO);
        if (mRoomInfo == null) {
            MyLog.e(TAG, "mRoomInfo is null");
            finish();
            return;
        }
        // 填充 mMyRoomData
        mMyRoomData.setRoomId(mRoomInfo.getLiveId());
        mMyRoomData.setUid(mRoomInfo.getPlayerId());
        mMyRoomData.setVideoUrl(mRoomInfo.getVideoUrl());
        mMyRoomData.setLiveType(mRoomInfo.getLiveType());
    }

    private void initView() {
        mComponentController = new VideoDetailController(mMyRoomData);
        mSdkView = new VideoDetailView(this, mComponentController);
        mSdkView.setupSdkView();
    }

    public static void openActivity(@NonNull Activity activity, RoomInfo roomInfo) {
        Intent intent = new Intent(activity, VideoDetailSdkActivity.class);
        intent.putExtra(EXTRA_ROOM_INFO, roomInfo);
        activity.startActivity(intent);
    }
}
