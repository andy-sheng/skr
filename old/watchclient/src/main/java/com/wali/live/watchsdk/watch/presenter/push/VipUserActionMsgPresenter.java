package com.wali.live.watchsdk.watch.presenter.push;

import android.support.annotation.NonNull;
import com.mi.live.data.account.MyUserInfoManager;
import com.mi.live.data.event.VipUpdateEvent;
import com.mi.live.data.push.IPushMsgProcessor;
import com.mi.live.data.push.model.BarrageMsg;
import com.mi.live.data.push.model.BarrageMsgType;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.wali.live.common.barrage.manager.LiveRoomChatMsgManager;
import com.wali.live.watchsdk.eventbus.EventClass;

import org.greenrobot.eventbus.EventBus;

/**
 * VIP用户动作消息处理器，响应VIP用户进入房间等动作
 * Created by anping on 16-7-29.
 */
public class VipUserActionMsgPresenter implements IPushMsgProcessor {

    private volatile LiveRoomChatMsgManager mLiveRoomChatMsgManager;

    public VipUserActionMsgPresenter(@NonNull LiveRoomChatMsgManager chatMsgManager) {
        this.mLiveRoomChatMsgManager = chatMsgManager;
    }

    @Override
    public void process(BarrageMsg msg, RoomBaseDataModel roomBaseDataModel) {
        if (msg == null) {
            return;
        }
        switch (msg.getMsgType()) {
            case BarrageMsgType.B_MSG_TYPE_VIP_LEVEL_CHANGED: {
                BarrageMsg.VipLevelChangedExt msgExt = (BarrageMsg.VipLevelChangedExt) msg.getMsgExt();
                MyUserInfoManager.getInstance().setVipInfo(msgExt.newVipLevel);

                EventBus.getDefault().post(new VipUpdateEvent());
            }
            break;
            case BarrageMsgType.B_MSG_TYPE_JOIN: {
                EventBus.getDefault().post(new EventClass.HighLevelUserActionEvent(msg));
            }
            break;
            default:
                break;
        }
    }

    @Override
    public int[] getAcceptMsgType() {
        return new int[]{BarrageMsgType.B_MSG_TYPE_JOIN, BarrageMsgType.B_MSG_TYPE_VIP_LEVEL_CHANGED,};
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
