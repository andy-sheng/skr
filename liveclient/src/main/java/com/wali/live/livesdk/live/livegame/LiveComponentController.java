package com.wali.live.livesdk.live.livegame;

import com.mi.live.data.push.collection.InsertSortLinkedList;
import com.wali.live.common.barrage.manager.LiveRoomChatMsgManager;
import com.wali.live.livesdk.live.component.ComponentController;
import com.wali.live.livesdk.live.presenter.GameLivePresenter;

/**
 * Created by yangli on 2017/2/18.
 *
 * @module 组件控制器, 游戏直播
 */
public class LiveComponentController extends ComponentController {
    private static final String TAG = "LiveComponentController";

    /**
     * 房间弹幕管理
     */
    LiveRoomChatMsgManager mRoomChatMsgManager =
            new LiveRoomChatMsgManager(InsertSortLinkedList.DEFAULT_MAX_SIZE);

    GameLivePresenter mGameLivePresenter;
}
