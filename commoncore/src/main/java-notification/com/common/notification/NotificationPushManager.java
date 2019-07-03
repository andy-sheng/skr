package com.common.notification;

import com.common.log.MyLog;
import com.common.notification.event.CRInviteInCreateRoomNotifyEvent;
import com.common.notification.event.CRRefuseInviteNotifyEvent;
import com.common.notification.event.CRSendInviteUserNotifyEvent;
import com.common.notification.event.CRStartByCreateNotifyEvent;
import com.common.notification.event.CRStartByMatchPushEvent;
import com.common.notification.event.CRSyncInviteUserNotifyEvent;
import com.common.notification.event.FollowNotifyEvent;
import com.common.notification.event.GrabInviteNotifyEvent;
import com.common.notification.event.SysWarnNotifyEvent;
import com.zq.live.proto.Notification.CombineRoomEnterMsg;
import com.zq.live.proto.Notification.CombineRoomInviteMsg;
import com.zq.live.proto.Notification.CombineRoomInviteV2Msg;
import com.zq.live.proto.Notification.CombineRoomRefuseMsg;
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
        } else if (msg.getMsgType() == ENotificationMsgType.NM_CR_INVITEV2) {
            processInviteMsg(baseNotiInfo, msg.getInviteV2Msg());
        } else if (msg.getMsgType() == ENotificationMsgType.NM_CR_ENTER) {
            processEnterRoomMsg(baseNotiInfo, msg.getEnterMsg());
        } else if (msg.getMsgType() == ENotificationMsgType.NM_CR_REFUSE) {
            processRefuseDoubleRoomMsg(baseNotiInfo, msg.getRefuseMsg());
        }
    }

    // 处理双人房拒绝消息
    private void processRefuseDoubleRoomMsg(BaseNotiInfo baseNotiInfo, CombineRoomRefuseMsg refuseMsg) {
        if (baseNotiInfo != null) {
            CRRefuseInviteNotifyEvent crRefuseInviteNotifyEvent = new CRRefuseInviteNotifyEvent(baseNotiInfo, refuseMsg);
            EventBus.getDefault().post(crRefuseInviteNotifyEvent);
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

    /**
     * 邀请解析，所有邀请都包括
     */
    private void processInviteMsg(BaseNotiInfo baseNotiInfo, CombineRoomInviteV2Msg combineRoomInviteMsg) {
        if (combineRoomInviteMsg != null) {
            if (combineRoomInviteMsg.getInviteType() == EInviteType.IT_OUT_COMBINE_ROOM) {
                //房间外，目前是一场到底里面
                CRSendInviteUserNotifyEvent combineRoomSendInviteUserEvent = new CRSendInviteUserNotifyEvent(baseNotiInfo, combineRoomInviteMsg);
                EventBus.getDefault().post(combineRoomSendInviteUserEvent);
            } else if (combineRoomInviteMsg.getInviteType() == EInviteType.IT_IN_COMBINE_ROOM) {
                //双人房房间里
                EventBus.getDefault().post(new CRInviteInCreateRoomNotifyEvent(baseNotiInfo, combineRoomInviteMsg));
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
                CRSyncInviteUserNotifyEvent combineRoomSyncInviteUserEvent = new CRSyncInviteUserNotifyEvent(baseNotiInfo, combineRoomEnterMsg);
                EventBus.getDefault().post(combineRoomSyncInviteUserEvent);
            } else if (combineRoomEnterMsg.getEnterType() == ECombineRoomEnterType.CRET_MATCH) {
                EventBus.getDefault().post(new CRStartByMatchPushEvent(baseNotiInfo, combineRoomEnterMsg));
            } else if (combineRoomEnterMsg.getEnterType() == ECombineRoomEnterType.CRET_CREATE) {
                EventBus.getDefault().post(new CRStartByCreateNotifyEvent(baseNotiInfo, combineRoomEnterMsg));
            } else {
                MyLog.e(TAG, "processEnterRoomMsg unknown type=" + combineRoomEnterMsg.getEnterType());
            }
        } else {
            MyLog.e(TAG, "processEnterRoomMsg combineRoomEnterMsg=null");
        }
    }
}
