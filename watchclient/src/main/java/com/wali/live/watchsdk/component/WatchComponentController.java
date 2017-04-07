package com.wali.live.watchsdk.component;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.mi.live.data.room.model.RoomBaseDataModel;
import com.wali.live.common.barrage.manager.LiveRoomChatMsgManager;
import com.wali.live.component.ComponentController;

/**
 * Created by yangli on 2017/2/18.
 *
 * @module 组件控制器, 游戏直播
 */
public class WatchComponentController extends ComponentController {
    private static final String TAG = "WatchComponentController";

    @NonNull
    RoomBaseDataModel mMyRoomData;

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


    @Nullable
    @Override
    protected String getTAG() {
        return TAG;
    }
}
