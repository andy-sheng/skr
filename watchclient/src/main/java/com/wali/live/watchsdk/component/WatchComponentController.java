package com.wali.live.watchsdk.component;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.base.log.MyLog;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.wali.live.common.barrage.manager.LiveRoomChatMsgManager;
import com.wali.live.componentwrapper.BaseSdkController;
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
    RoomBaseDataModel mMyRoomData;

    ArrayList<RoomInfo> mRoomInfoList;
    int mRoomInfoPosition;

    /**
     * 房间弹幕管理
     */
    LiveRoomChatMsgManager mRoomChatMsgManager;

    private boolean mSwitchNext;

    @Nullable
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
}
