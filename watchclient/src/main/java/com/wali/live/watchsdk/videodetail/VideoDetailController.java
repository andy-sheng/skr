package com.wali.live.watchsdk.videodetail;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.mi.live.data.room.model.RoomBaseDataModel;
import com.wali.live.common.barrage.manager.LiveRoomChatMsgManager;
import com.wali.live.componentwrapper.BaseSdkController;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.component.view.VideoDetailPlayerView;
import com.wali.live.watchsdk.videodetail.presenter.VideoDetailPlayerPresenter;

/**
 * Created by yangli on 2017/5/26.
 */
public class VideoDetailController extends BaseSdkController {
    private static final String TAG = "VideoDetailController";

    protected RoomBaseDataModel mMyRoomData;
    protected LiveRoomChatMsgManager mRoomChatMsgManager; // 房间弹幕管理

    protected VideoDetailPlayerView mPlayerView;
    protected VideoDetailPlayerPresenter mPlayerPresenter;

    @Nullable
    @Override
    protected String getTAG() {
        return TAG;
    }

    public VideoDetailController(
            @NonNull RoomBaseDataModel myRoomData,
            @NonNull LiveRoomChatMsgManager roomChatMsgManager) {
        mMyRoomData = myRoomData;
        mRoomChatMsgManager = roomChatMsgManager;
    }

    public void setupController(Context context) {
        if (mPlayerView == null) {
            mPlayerView = new VideoDetailPlayerView(context);
            mPlayerView.setId(R.id.video_view);
            mPlayerView.setMyRoomData(mMyRoomData);
            mPlayerPresenter = new VideoDetailPlayerPresenter(this, mMyRoomData, (Activity) context);
            mPlayerPresenter.setComponentView(mPlayerView.getViewProxy());
            mPlayerView.setPresenter(mPlayerPresenter);
        }
        mPlayerPresenter.startPresenter();
    }

    @Override
    public void release() {
        super.release();
        mPlayerPresenter.destroy();
    }
}
