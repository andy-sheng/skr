package com.wali.live.watchsdk.component;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.mi.live.data.room.model.RoomBaseDataModel;
import com.wali.live.common.barrage.manager.LiveRoomChatMsgManager;
import com.wali.live.component.ComponentController;
import com.wali.live.watchsdk.watch.model.RoomInfo;

import java.util.List;

/**
 * Created by yangli on 2017/2/18.
 *
 * @module 组件控制器, 游戏直播
 */
public class WatchComponentController extends ComponentController {
    private static final String TAG = "WatchComponentController";

    @NonNull
    RoomBaseDataModel mMyRoomData;

    List<RoomInfo> mRoomInfoList;
    int mRoomInfoPosition;

    /**
     * 房间弹幕管理
     */
    LiveRoomChatMsgManager mRoomChatMsgManager;

    public WatchComponentController(
            @NonNull RoomBaseDataModel myRoomData,
            @NonNull LiveRoomChatMsgManager roomChatMsgManager) {
        mMyRoomData = myRoomData;
        mRoomChatMsgManager = roomChatMsgManager;
    }

    public void setVerticalList(List<RoomInfo> list, int position) {
        mRoomInfoList = list;
        mRoomInfoPosition = position;
    }

    public void switchToNextPosition() {
        mRoomInfoPosition += 1;
        if (mRoomInfoPosition >= mRoomInfoList.size()) {
            mRoomInfoPosition -= mRoomInfoList.size();
        }
    }

    public void switchToLastPosition() {
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

    @Nullable
    @Override
    protected String getTAG() {
        return TAG;
    }
}
