package com.common.notification;

import com.common.log.MyLog;
import com.common.notification.event.CombineRoomSendInviteUserNotifyEvent;
import com.common.notification.event.CombineRoomSyncInviteUserNotifyEvent;
import com.common.notification.event.DoubleStartCombineRoomByMatchPushEvent;
import com.common.notification.event.FollowNotifyEvent;
import com.common.notification.event.GrabInviteNotifyEvent;
import com.common.notification.event.SysWarnNotifyEvent;
import com.zq.live.proto.Notification.CombineRoomSendInviteUserMsg;
import com.zq.live.proto.Notification.CombineRoomSyncInviteUserMsg;
import com.zq.live.proto.Notification.ENotificationMsgType;
import com.zq.live.proto.Notification.FollowMsg;
import com.zq.live.proto.Notification.InviteStandMsg;
import com.zq.live.proto.Notification.NotificationMsg;
import com.zq.live.proto.Notification.StartCombineRoomByMatchMsg;
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
        } else if (msg.getMsgType() == ENotificationMsgType.NM_CR_SEND_INVITE_USER) {
            processInviteToDoubleRoomMsg(baseNotiInfo, msg.getSendInviteUserMsg());
        } else if (msg.getMsgType() == ENotificationMsgType.NM_CR_SYNC_INVITE_USER) {
            processAcceptInviteMsg(baseNotiInfo, msg.getSyncInviteUserMsg());
        } else if (msg.getMsgType() == ENotificationMsgType.NM_START_CR_BY_MATCH) {
            processStartCombineRoomMsg(baseNotiInfo, msg.getStartCombineRoomByMatchMsg());
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

    //被别人邀请去双人房
    private void processInviteToDoubleRoomMsg(BaseNotiInfo baseNotiInfo, CombineRoomSendInviteUserMsg combineRoomSendInviteUserMsg) {
        if (combineRoomSendInviteUserMsg != null) {
            CombineRoomSendInviteUserNotifyEvent combineRoomSendInviteUserEvent = new CombineRoomSendInviteUserNotifyEvent(baseNotiInfo, combineRoomSendInviteUserMsg);
            EventBus.getDefault().post(combineRoomSendInviteUserEvent);
        }
    }

    //被我邀请的人进入了房间的push
    private void processAcceptInviteMsg(BaseNotiInfo baseNotiInfo, CombineRoomSyncInviteUserMsg combineRoomSyncInviteUserMsg) {
        if (combineRoomSyncInviteUserMsg != null) {
            CombineRoomSyncInviteUserNotifyEvent combineRoomSyncInviteUserEvent = new CombineRoomSyncInviteUserNotifyEvent(baseNotiInfo, combineRoomSyncInviteUserMsg);
            EventBus.getDefault().post(combineRoomSyncInviteUserEvent);
        }
    }

    private void processStartCombineRoomMsg(BaseNotiInfo basePushInfo, StartCombineRoomByMatchMsg startCombineRoomByMatchMsg) {
        if (startCombineRoomByMatchMsg != null) {
            EventBus.getDefault().post(new DoubleStartCombineRoomByMatchPushEvent(basePushInfo, startCombineRoomByMatchMsg));
        } else {
            MyLog.e(TAG, "processStartCombineRoomMsg" + " Msg=null");
        }
    }
}
