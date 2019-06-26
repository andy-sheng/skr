package com.common.notification;

import com.common.log.MyLog;
import com.common.notification.event.CombineRoomInviteInCreateRoomNotifyEvent;
import com.common.notification.event.CombineRoomSendInviteUserNotifyEvent;
import com.common.notification.event.CombineRoomSyncInviteUserNotifyEvent;
import com.common.notification.event.DoubleStartCombineRoomByMatchPushEvent;
import com.common.notification.event.FollowNotifyEvent;
import com.common.notification.event.GrabInviteNotifyEvent;
import com.common.notification.event.StartCombineRoomByCreateNotifyEvent;
import com.common.notification.event.SysWarnNotifyEvent;
import com.zq.live.proto.Notification.CombineRoomEnterMsg;
import com.zq.live.proto.Notification.CombineRoomInviteMsg;
import com.zq.live.proto.Notification.ECombineRoomEnterType;
import com.zq.live.proto.Notification.EInviteType;
import com.zq.live.proto.Notification.ENotificationMsgType;
import com.zq.live.proto.Notification.FollowMsg;
import com.zq.live.proto.Notification.InviteStandMsg;
import com.zq.live.proto.Notification.NotificationMsg;
import com.zq.live.proto.Notification.SysWarningMsg;

import org.greenrobot.eventbus.EventBus;

/**
 * 通知管理器
 */
public class NotificationPushManager {

    public final static String TAG = "NotificationManager";

    private static class NotificationAdapterHolder {
        private static final NotificationPushManager INSTANCE = new NotificationPushManager();
    }


    private NotificationPushManager() {

    }


    public static final NotificationPushManager getInstance() {
        return NotificationAdapterHolder.INSTANCE;
    }

    /**
     * 处理通知消息
     */
    public void processNotificationMsg(NotificationMsg msg) {
        MyLog.d(TAG, "processRoomMsg" + " messageType=" + msg.getMsgType());

        BaseNotiInfo baseNotiInfo = BaseNotiInfo.parse(msg);
        if (msg.getMsgType() == ENotificationMsgType.NM_FOLLOW) {
            processFollowMsg(baseNotiInfo, msg.getFollowMsg());
        } else if (msg.getMsgType() == ENotificationMsgType.NM_INVITE_STAND) {
            processInviteStandMsg(baseNotiInfo, msg.getInviteStandMsg());
        } else if (msg.getMsgType() == ENotificationMsgType.NM_SYS_WARNING_MSG) {
            processSysWarnMsg(baseNotiInfo, msg.getSysWarningMsg());
        } else if (msg.getMsgType() == ENotificationMsgType.NM_CR_INVITE) {
            processInviteMsg(baseNotiInfo, msg.getInviteMsg());
        } else if (msg.getMsgType() == ENotificationMsgType.NM_CR_ENTER) {
            processEnterRoomMsg(baseNotiInfo, msg.getEnterMsg());
        }
    }

    // 处理关注消息
    private void processFollowMsg(BaseNotiInfo baseNotiInfo, FollowMsg followMsg) {
        if (baseNotiInfo != null) {
            FollowNotifyEvent followNotifyEvent = new FollowNotifyEvent(baseNotiInfo, followMsg);
            EventBus.getDefault().post(followNotifyEvent);
        }
    }

    // 处理邀请消息
    private void processInviteStandMsg(BaseNotiInfo baseNotiInfo, InviteStandMsg inviteStandMsg) {
        if (baseNotiInfo != null) {
            GrabInviteNotifyEvent grabInviteNotifyEvent = new GrabInviteNotifyEvent(baseNotiInfo, inviteStandMsg);
            EventBus.getDefault().post(grabInviteNotifyEvent);
        }
    }

    // 处理系统警告消息
    private void processSysWarnMsg(BaseNotiInfo baseNotiInfo, SysWarningMsg sysWarningMsg) {
        if (baseNotiInfo != null) {
            SysWarnNotifyEvent sysWarnNotifyEvent = new SysWarnNotifyEvent(baseNotiInfo, sysWarningMsg.getTitle(), sysWarningMsg.getContent());
            EventBus.getDefault().post(sysWarnNotifyEvent);
        }
    }

//    //被别人邀请去双人房
//    private void processInviteToDoubleRoomMsg(BaseNotiInfo baseNotiInfo, CombineRoomSendInviteUserMsg combineRoomSendInviteUserMsg) {
//        if (combineRoomSendInviteUserMsg != null) {
//            CombineRoomSendInviteUserNotifyEvent combineRoomSendInviteUserEvent = new CombineRoomSendInviteUserNotifyEvent(baseNotiInfo, combineRoomSendInviteUserMsg);
//            EventBus.getDefault().post(combineRoomSendInviteUserEvent);
//        }
//    }
//
//    //被我邀请的人进入了房间的push
//    private void processAcceptInviteMsg(BaseNotiInfo baseNotiInfo, CombineRoomSyncInviteUserMsg combineRoomSyncInviteUserMsg) {
//        if (combineRoomSyncInviteUserMsg != null) {
//            CombineRoomSyncInviteUserNotifyEvent combineRoomSyncInviteUserEvent = new CombineRoomSyncInviteUserNotifyEvent(baseNotiInfo, combineRoomSyncInviteUserMsg);
//            EventBus.getDefault().post(combineRoomSyncInviteUserEvent);
//        }
//    }
//
//    private void processStartCombineRoomMsg(BaseNotiInfo basePushInfo, StartCombineRoomByMatchMsg startCombineRoomByMatchMsg) {
//        if (startCombineRoomByMatchMsg != null) {
//            EventBus.getDefault().post(new DoubleStartCombineRoomByMatchPushEvent(basePushInfo, startCombineRoomByMatchMsg));
//        } else {
//            MyLog.e(TAG, "processStartCombineRoomMsg" + " Msg=null");
//        }
//    }
//
//    /**
//     * 在双人房里有人邀请后被邀请方收到的push
//     *
//     * @param basePushInfo
//     * @param combineRoomInviteInCreateRoomMsg
//     */
//    private void processCreateRoomInviteMsg(BaseNotiInfo basePushInfo, CombineRoomInviteInCreateRoomMsg combineRoomInviteInCreateRoomMsg) {
//        if (combineRoomInviteInCreateRoomMsg != null) {
//            EventBus.getDefault().post(new CombineRoomInviteInCreateRoomNotifyEvent(basePushInfo, combineRoomInviteInCreateRoomMsg));
//        } else {
//            MyLog.e(TAG, "processCreateRoomInviteMsg" + " Msg=null");
//        }
//    }
//
//    private void processStartCombineRoomByCreateMsg(BaseNotiInfo basePushInfo, StartCombineRoomByCreateMsg startCombineRoomByCreateMsg) {
//        if (startCombineRoomByCreateMsg != null) {
//            EventBus.getDefault().post(new StartCombineRoomByCreateNotifyEvent(basePushInfo, startCombineRoomByCreateMsg));
//        } else {
//            MyLog.e(TAG, "processStartCombineRoomByCreateMsg" + " Msg=null");
//        }
//    }

    /**
     * 邀请解析，所有邀请都包括
     */
    private void processInviteMsg(BaseNotiInfo baseNotiInfo, CombineRoomInviteMsg combineRoomInviteMsg) {
        if (combineRoomInviteMsg != null) {
            if (combineRoomInviteMsg.getInviteType() == EInviteType.IT_OUT_COMBINE_ROOM) {
                //房间外，目前是一场到底里面
                CombineRoomSendInviteUserNotifyEvent combineRoomSendInviteUserEvent = new CombineRoomSendInviteUserNotifyEvent(baseNotiInfo, combineRoomInviteMsg);
                EventBus.getDefault().post(combineRoomSendInviteUserEvent);
            } else if (combineRoomInviteMsg.getInviteType() == EInviteType.IT_IN_COMBINE_ROOM) {
                //双人房房间里
                EventBus.getDefault().post(new CombineRoomInviteInCreateRoomNotifyEvent(baseNotiInfo, combineRoomInviteMsg));
            } else {
                MyLog.e(TAG, "processInviteMsg unknown type=" + combineRoomInviteMsg.getInviteType());
            }
        } else {
            MyLog.e(TAG, "processInviteMsg combineRoomInviteMsg=null");
        }
    }

    private void processEnterRoomMsg(BaseNotiInfo baseNotiInfo, CombineRoomEnterMsg combineRoomEnterMsg) {
        if (combineRoomEnterMsg != null) {
            if (combineRoomEnterMsg.getEnterType() == ECombineRoomEnterType.CRET_INVITE) {
                CombineRoomSyncInviteUserNotifyEvent combineRoomSyncInviteUserEvent = new CombineRoomSyncInviteUserNotifyEvent(baseNotiInfo, combineRoomEnterMsg);
                EventBus.getDefault().post(combineRoomSyncInviteUserEvent);
            } else if (combineRoomEnterMsg.getEnterType() == ECombineRoomEnterType.CRET_MATCH) {
                EventBus.getDefault().post(new DoubleStartCombineRoomByMatchPushEvent(baseNotiInfo, combineRoomEnterMsg));
            } else if (combineRoomEnterMsg.getEnterType() == ECombineRoomEnterType.CRET_CREATE) {
                EventBus.getDefault().post(new StartCombineRoomByCreateNotifyEvent(baseNotiInfo, combineRoomEnterMsg));
            } else {
                MyLog.e(TAG, "processEnterRoomMsg unknown type=" + combineRoomEnterMsg.getEnterType());
            }
        } else {
            MyLog.e(TAG, "processEnterRoomMsg combineRoomEnterMsg=null");
        }
    }
}
