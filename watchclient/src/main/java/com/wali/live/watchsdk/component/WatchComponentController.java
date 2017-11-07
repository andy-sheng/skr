package com.wali.live.watchsdk.component;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.milink.MiLinkClientAdapter;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.mi.live.engine.player.engine.GalileoPlayer;
import com.thornbirds.component.IEventController;
import com.thornbirds.component.Params;
import com.wali.live.common.barrage.manager.LiveRoomChatMsgManager;
import com.wali.live.component.BaseSdkController;
import com.wali.live.watchsdk.videodetail.data.PullStreamerPresenter;
import com.wali.live.watchsdk.watch.WatchSdkActivity;
import com.wali.live.watchsdk.watch.model.RoomInfo;

import java.util.ArrayList;

/**
 * Created by yangli on 2017/2/18.
 *
 * @module 组件控制器, 游戏直播
 */
public class WatchComponentController extends BaseSdkController {
    private static final String TAG = "WatchComponentController";

    @NonNull
    protected RoomBaseDataModel mMyRoomData;

    protected ArrayList<RoomInfo> mRoomInfoList;
    protected int mRoomInfoPosition;

    /**
     * 房间弹幕管理
     */
    protected LiveRoomChatMsgManager mRoomChatMsgManager;

    private boolean mSwitchNext;

    protected PullStreamerPresenter mStreamerPresenter;

    @Override
    protected String getTAG() {
        return TAG;
    }

    public WatchComponentController(
            @NonNull RoomBaseDataModel myRoomData,
            @NonNull LiveRoomChatMsgManager roomChatMsgManager) {
        mMyRoomData = myRoomData;
        mRoomChatMsgManager = roomChatMsgManager;
    }

    public void setupController(Context context) {
        mStreamerPresenter = new PullStreamerPresenter(new EventPlayerCallback(this));
        mStreamerPresenter.setIsRealTime(true);
        GalileoPlayer player = new GalileoPlayer(GlobalData.app(), UserAccountManager.getInstance().getUuid(),
                MiLinkClientAdapter.getsInstance().getClientIp());
        player.setCallback(mStreamerPresenter.getInnerPlayerCallback());
        mStreamerPresenter.setStreamer(player);
    }

    @Override
    public void release() {
        super.release();
        mStreamerPresenter.stopWatch();
        mStreamerPresenter.destroy();
    }

    public void setVerticalList(ArrayList<RoomInfo> list, int position) {
        mRoomInfoList = list;
        mRoomInfoPosition = position;
    }

    public void switchToNextPosition() {
        mSwitchNext = true;
        mRoomInfoPosition += 1;
        if (mRoomInfoPosition >= mRoomInfoList.size()) {
            mRoomInfoPosition -= mRoomInfoList.size();
        }
    }

    public void switchToLastPosition() {
        mSwitchNext = false;
        mRoomInfoPosition -= 1;
        if (mRoomInfoPosition < 0) {
            mRoomInfoPosition += mRoomInfoList.size();
        }
    }

    public void switchRoom() {
        mMyRoomData.reset();

        RoomInfo roomInfo = mRoomInfoList.get(mRoomInfoPosition);
        mMyRoomData.setRoomId(roomInfo.getLiveId());
        mMyRoomData.setUid(roomInfo.getPlayerId());
        mMyRoomData.setVideoUrl(roomInfo.getVideoUrl());
        mMyRoomData.setLiveType(roomInfo.getLiveType());
        mMyRoomData.setGameId(roomInfo.getGameId());
        mMyRoomData.setEnableShare(roomInfo.isEnableShare());
    }

    public boolean removeCurrentRoom() {
        if (mRoomInfoList != null && mRoomInfoList.size() > 1) {
            MyLog.d(TAG, "mRoomInfoList remove before=" + mRoomInfoList.size());
            mRoomInfoList.remove(mRoomInfoPosition);
            MyLog.d(TAG, "mRoomInfoList remove after=" + mRoomInfoList.size());
            if (mSwitchNext) {
                if (mRoomInfoPosition >= mRoomInfoList.size()) {
                    mRoomInfoPosition -= mRoomInfoList.size();
                }
            } else {
                mRoomInfoPosition -= 1;
                if (mRoomInfoPosition < 0) {
                    mRoomInfoPosition += mRoomInfoList.size();
                }
            }
            MyLog.w(TAG, "removeCurrentRoom roomPosition=" + mRoomInfoPosition + "; roomList size=" + mRoomInfoList.size());
            return true;
        }
        return false;
    }

    public void enterRoomList(Activity activity) {
        MyLog.w(TAG, "enterRoomList roomList size=" + mRoomInfoList.size());
        if (mRoomInfoList.size() > 1) {
            WatchSdkActivity.openActivity(activity, mRoomInfoList, mRoomInfoPosition);
        } else if (mRoomInfoList.size() == 1) {
            WatchSdkActivity.openActivity(activity, mRoomInfoList.get(0));
        }
    }

    public static class EventPlayerCallback extends PullStreamerPresenter.PlayerCallbackWrapper {

        private IEventController controller;

        public EventPlayerCallback(@NonNull IEventController controller) {
            this.controller = controller;
        }

        @Override
        public void onPrepared() {
            controller.postEvent(MSG_PLAYER_READY);
        }

        @Override
        public void onCompletion() {
            controller.postEvent(MSG_PLAYER_COMPLETED);
        }

        @Override
        public void onSeekComplete() {
            controller.postEvent(MSG_SEEK_COMPLETED);
        }

        @Override
        public void onVideoSizeChanged(int width, int height) {
            controller.postEvent(MSG_VIDEO_SIZE_CHANGED, new Params().putItem(width).putItem(height));
        }

        @Override
        public void onError(int what, int extra) {
            controller.postEvent(MSG_PLAYER_ERROR);
        }

        @Override
        public void onShowLoading() {
            controller.postEvent(MSG_PLAYER_SHOW_LOADING);
        }

        @Override
        public void onHideLoading() {
            controller.postEvent(MSG_PLAYER_HIDE_LOADING);
        }

        @Override
        public void onUpdateProgress() {
            controller.postEvent(MSG_UPDATE_PLAY_PROGRESS);
        }
    }
}
