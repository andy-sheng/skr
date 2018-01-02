package com.wali.live.watchsdk.watch.presenter.push;

import android.support.annotation.NonNull;

import com.base.log.MyLog;
import com.mi.live.data.account.MyUserInfoManager;
import com.mi.live.data.push.IPushMsgProcessor;
import com.mi.live.data.push.model.BarrageMsg;
import com.mi.live.data.push.model.BarrageMsgType;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.wali.live.watchsdk.component.presenter.BarrageControlAnimPresenter;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by zyh on 2017/12/31.
 *
 * @module 需要动画展示的消息：
 */

public class AnimMsgPresenter implements IPushMsgProcessor {
    @NonNull
    private RoomBaseDataModel mMyRoomData;

    public AnimMsgPresenter(@NonNull RoomBaseDataModel myRoomData) {
        mMyRoomData = myRoomData;
    }

    private boolean isSameRoom(@NonNull RoomBaseDataModel roomBaseDataModel) {
        return mMyRoomData.getRoomId() == roomBaseDataModel.getRoomId();
    }

    @Override
    public void process(BarrageMsg msg, @NonNull RoomBaseDataModel roomBaseDataModel) {
        if (msg == null || !isSameRoom(roomBaseDataModel)) {
            return;
        }
        MyLog.w("AnimMsgPresenter", "BarrageAnim process msg =" + msg.toString());
        switch (msg.getMsgType()) {
            case BarrageMsgType.B_MSG_TYPE_JOIN:
            case BarrageMsgType.B_MSG_TYPE_ANIM:
                EventBus.getDefault().post(new BarrageControlAnimPresenter.AnimMsgEvent(msg));
                break;
            case BarrageMsgType.B_MSG_TYPE_VIP_LEVEL_CHANGED:
                MyUserInfoManager.getInstance().setVipLevel(msg.getVipLevel());
                EventBus.getDefault().post(new BarrageControlAnimPresenter.AnimMsgEvent(msg));
                break;
        }
    }

    @Override
    public int[] getAcceptMsgType() {
        return new int[]{
                BarrageMsgType.B_MSG_TYPE_JOIN,
                BarrageMsgType.B_MSG_TYPE_ANIM,
                BarrageMsgType.B_MSG_TYPE_VIP_LEVEL_CHANGED
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
