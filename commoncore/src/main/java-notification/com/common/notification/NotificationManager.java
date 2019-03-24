package com.common.notification;

import com.common.log.MyLog;
import com.common.notification.event.FollowNotifyEvent;
import com.common.notification.event.GrabInviteNotifyEvent;
import com.common.utils.U;
import com.zq.live.proto.Notification.ENotificationMsgType;
import com.zq.live.proto.Notification.FollowMsg;
import com.zq.live.proto.Notification.InviteStandMsg;
import com.zq.live.proto.Notification.NotificationMsg;

import org.greenrobot.eventbus.EventBus;

/**
 * 通知管理器
 */
public class NotificationManager {

    public final static String TAG = "NotificationManager";

    public final static String SP_KEY_NEW_FRIEND = "SP_KEY_NEW_FRIEND";  //从外到内 消息3 好友图标2 好友1
    public final static String SP_KEY_NEW_FANS = "SP_KEY_NEW_FANS";    //从外到内 消息3 好友图标2 粉丝1

    private static class NotificationAdapterHolder {
        private static final NotificationManager INSTANCE = new NotificationManager();
    }


    private NotificationManager() {

    }


    public static final NotificationManager getInstance() {
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
        }
    }

    // 处理关注消息
    private void processFollowMsg(BaseNotiInfo baseNotiInfo, FollowMsg followMsg) {
        if (baseNotiInfo != null) {
            FollowNotifyEvent followNotifyEvent = new FollowNotifyEvent(baseNotiInfo, followMsg);
            EventBus.getDefault().post(followNotifyEvent);
            if (followMsg.getIsFriend()) {
                U.getPreferenceUtils().setSettingInt(SP_KEY_NEW_FRIEND, 3);
            }
            if (followMsg.getIsFollow()) {
                U.getPreferenceUtils().setSettingInt(SP_KEY_NEW_FANS, 3);
            }
        }
    }

    // 处理邀请消息
    private void processInviteStandMsg(BaseNotiInfo baseNotiInfo, InviteStandMsg inviteStandMsg) {
        if (baseNotiInfo != null) {
            GrabInviteNotifyEvent grabInviteNotifyEvent = new GrabInviteNotifyEvent(baseNotiInfo, inviteStandMsg);
            EventBus.getDefault().post(grabInviteNotifyEvent);
        }
    }
}
