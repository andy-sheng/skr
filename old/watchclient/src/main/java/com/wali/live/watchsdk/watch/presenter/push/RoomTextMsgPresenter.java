package com.wali.live.watchsdk.watch.presenter.push;

import com.mi.live.data.push.IPushMsgProcessor;
import com.mi.live.data.push.model.BarrageMsg;
import com.mi.live.data.push.model.BarrageMsgType;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.wali.live.common.barrage.manager.LiveRoomChatMsgManager;

/**
 * 弹幕push处理
 * Created by chengsimin on 16/7/5.
 */
public class RoomTextMsgPresenter implements IPushMsgProcessor {
    LiveRoomChatMsgManager mRoomChatMsgManager;

    /**
     * @param mRoomChatMsgManager
     */
    public RoomTextMsgPresenter(LiveRoomChatMsgManager mRoomChatMsgManager) {
        this.mRoomChatMsgManager = mRoomChatMsgManager;
    }

    @Override
    public void process(BarrageMsg msg, RoomBaseDataModel roomBaseDataModel) {
        if (mRoomChatMsgManager.isHideChatMsg()) {
            return;
        }
        if (msg.getMsgType() == BarrageMsgType.B_MSG_TYPE_TEXT
                || msg.getMsgType() == BarrageMsgType.B_MSG_TYPE_ANIM
                || msg.getMsgType() == BarrageMsgType.B_MSG_TYPE_SHARE) {

            mRoomChatMsgManager.addChatMsg(msg, true);
        }
    }


    @Override
    public int[] getAcceptMsgType() {
        return new int[]{
                BarrageMsgType.B_MSG_TYPE_TEXT
                , BarrageMsgType.B_MSG_TYPE_ANIM
                , BarrageMsgType.B_MSG_TYPE_SHARE
        };
    }

    @Override
    public void start() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void destroy() {
    }

}
