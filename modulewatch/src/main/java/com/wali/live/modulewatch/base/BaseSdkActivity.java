package com.wali.live.modulewatch.base;

import com.common.base.BaseActivity;
import com.wali.live.modulewatch.barrage.collection.InsertSortLinkedList;
import com.wali.live.modulewatch.barrage.manager.LiveRoomChatMsgManager;
import com.wali.live.modulewatch.model.roominfo.RoomBaseDataModel;

/**
 * Created by linjinbin on 16/5/1.
 *
 * @module 基础模块
 */
public abstract class BaseSdkActivity extends BaseActivity {
    /**
     * 本房间相关信息
     */
    protected RoomBaseDataModel mMyRoomData = new RoomBaseDataModel("sdk_Myroominfo");
    /**
     * 房间弹幕管理
     */
    protected LiveRoomChatMsgManager mRoomChatMsgManager = new LiveRoomChatMsgManager(InsertSortLinkedList.DEFAULT_MAX_SIZE);
}
