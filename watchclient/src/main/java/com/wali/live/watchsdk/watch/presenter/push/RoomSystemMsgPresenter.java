package com.wali.live.watchsdk.watch.presenter.push;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.push.IPushMsgProcessor;
import com.mi.live.data.push.model.BarrageMsg;
import com.mi.live.data.push.model.BarrageMsgType;
import com.mi.live.data.query.model.MessageRule;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.wali.live.common.barrage.manager.LiveRoomChatMsgManager;
import com.wali.live.event.EventClass;
import com.wali.live.watchsdk.R;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * @module 房间系统消息
 * <p>
 * Created by wuxiaoshan on 17-3-15.
 */
public class RoomSystemMsgPresenter implements IPushMsgProcessor {

    private static final String TAG = RoomSystemMsgPresenter.class.getSimpleName();

    LiveRoomChatMsgManager mRoomChatMsgManager;

    public RoomSystemMsgPresenter(LiveRoomChatMsgManager mRoomChatMsgManager) {
        this.mRoomChatMsgManager = mRoomChatMsgManager;
    }

    @Override
    public void process(BarrageMsg msg, RoomBaseDataModel roomBaseDataModel) {
        if (mRoomChatMsgManager.isHideSysMsg()) {
            return;
        }
        if (msg.getMsgType() == BarrageMsgType.B_MSG_TYPE_GLOBAL_SYS_MSG || msg.getMsgType() == BarrageMsgType.B_MSG_TYPE_COMMEN_SYS_MSG) {
            //处理全局系统消息
            BarrageMsg.GlobalMessageExt globalMessageExt = (BarrageMsg.GlobalMessageExt) msg.getMsgExt();
            if (globalMessageExt != null) {
                List<BarrageMsg> barrageMsgs = globalMessageExt.getSysBarrageMsg(msg);
                mRoomChatMsgManager.bulkAddChatMsg(barrageMsgs, true);
            }
        } else if (msg.getMsgType() == BarrageMsgType.B_MSG_TYPE_ROOM_SYS_MSG) {
            //处理房间消息
            BarrageMsg.RoomMessageExt roomMessageExt = (BarrageMsg.RoomMessageExt) msg.getMsgExt();
            if (roomMessageExt != null) {
                List<BarrageMsg> barrageMsgs = roomMessageExt.getRoomBarrageMsg(msg);
                mRoomChatMsgManager.bulkAddChatMsg(barrageMsgs, true);
            }
        } else if (msg.getMsgType() == BarrageMsgType.B_MSG_TYPE_ROOM_FOUCES_ANCHOR) {
            mRoomChatMsgManager.addChatMsg(msg, true);
        } else if (msg.getMsgType() == BarrageMsgType.B_MSG_TYPE_FREQUENCY_CONTROL) {
            //处理房间里发送弹幕限制消息
            if (roomBaseDataModel != null && roomBaseDataModel.getRoomId().equals(msg.getRoomId())) {
                BarrageMsg.MsgRuleChangeMessageExt messageExt = (BarrageMsg.MsgRuleChangeMessageExt) msg.getMsgExt();
                if (messageExt != null) {
                    sendMsgRuleSysMsg(messageExt.getMessageRule(), roomBaseDataModel);
                    int oriSpeakPeriod = 0;
                    if (roomBaseDataModel.getMsgRule() != null && roomBaseDataModel.getMsgRule().getSpeakPeriod() > 0) {
                        oriSpeakPeriod = roomBaseDataModel.getMsgRule().getSpeakPeriod();
                    }
                    EventClass.MsgRuleChangedEvent event = new EventClass.MsgRuleChangedEvent(roomBaseDataModel.getRoomId(), messageExt.getMessageRule().getSpeakPeriod(), oriSpeakPeriod, messageExt.getMessageRule().isUnrepeatable());
                    roomBaseDataModel.setMsgRule(messageExt.getMessageRule());
                    EventBus.getDefault().post(event);
                    MyLog.w("receive barrage frequency control msg:" + messageExt.toString());
                }
            }
        } else if (msg.getMsgType() == BarrageMsgType.B_MSG_TYPE_KICK_VIEWER) {
            MyLog.w(TAG, "viewer kicked," + msg.toString());
            if (msg.getRoomId().equals(roomBaseDataModel.getRoomId()) && msg.getToUserId() == UserAccountManager.getInstance().getUuidAsLong()) {
                Observable.just(0)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<Integer>() {
                            @Override
                            public void call(Integer integer) {
                                EventBus.getDefault().post(new EventClass.KickEvent());
                            }
                        });
            }
        }
    }

    /**
     * 发送消息频率控制更改的系统消息
     *
     * @param msgRule
     * @param roomBaseDataModel
     */
    private void sendMsgRuleSysMsg(MessageRule msgRule, RoomBaseDataModel roomBaseDataModel) {
        try {
            if (msgRule.getSpeakPeriod() != 0 && (roomBaseDataModel.getMsgRule() == null || roomBaseDataModel.getMsgRule().getSpeakPeriod() == 0)) {
                mRoomChatMsgManager.sendLocalSystemMsg(GlobalData.app().getString(R.string.sys_msg), GlobalData.app().getString(R.string.barrage_frequency_control_tips), roomBaseDataModel.getRoomId(), roomBaseDataModel.getUid());
            } else if (msgRule.getSpeakPeriod() == 0 && (roomBaseDataModel.getMsgRule() != null && roomBaseDataModel.getMsgRule().getSpeakPeriod() > 0)) {
                mRoomChatMsgManager.sendLocalSystemMsg(GlobalData.app().getString(R.string.sys_msg), GlobalData.app().getString(R.string.barrage_frequency_control_remove_tips), roomBaseDataModel.getRoomId(), roomBaseDataModel.getUid());
            }
            if (roomBaseDataModel.getMsgRule() == null || msgRule.isUnrepeatable() != roomBaseDataModel.getMsgRule().isUnrepeatable()) {
                if (msgRule.isUnrepeatable()) {
                    mRoomChatMsgManager.sendLocalSystemMsg(GlobalData.app().getString(R.string.sys_msg), GlobalData.app().getString(R.string.barrage_not_repeat), roomBaseDataModel.getRoomId(), roomBaseDataModel.getUid());
                } else if (roomBaseDataModel.getMsgRule() != null) {
                    mRoomChatMsgManager.sendLocalSystemMsg(GlobalData.app().getString(R.string.sys_msg), GlobalData.app().getString(R.string.barrage_not_repeat_remove), roomBaseDataModel.getRoomId(), roomBaseDataModel.getUid());
                }
            }
        } catch (Exception e) {
            MyLog.e("sendMessage room message rule error", e);
        }
    }

    @Override
    public int[] getAcceptMsgType() {
        return new int[]{
                BarrageMsgType.B_MSG_TYPE_ROOM_SYS_MSG,
                BarrageMsgType.B_MSG_TYPE_GLOBAL_SYS_MSG,
                BarrageMsgType.B_MSG_TYPE_ROOM_FOUCES_ANCHOR,
                BarrageMsgType.B_MSG_TYPE_FREQUENCY_CONTROL,
                BarrageMsgType.B_MSG_TYPE_COMMEN_SYS_MSG,
                BarrageMsgType.B_MSG_TYPE_KICK_VIEWER
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
        mRoomChatMsgManager = null;
    }
}
