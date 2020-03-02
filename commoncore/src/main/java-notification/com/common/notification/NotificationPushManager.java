package com.common.notification;

import com.common.log.MyLog;
import com.common.notification.event.CNRelayEnterFromOuterInviteNotifyEvent;
import com.common.notification.event.CNRelayEnterFromRedpacketNotifyEvent;
import com.common.notification.event.CNRelayEnterFromRoomInviteNotifyEvent;
import com.common.notification.event.CRInviteInCreateRoomNotifyEvent;
import com.common.notification.event.CRRefuseInviteNotifyEvent;
import com.common.notification.event.CRSendInviteUserNotifyEvent;
import com.common.notification.event.CRStartByCreateNotifyEvent;
import com.common.notification.event.CRStartByMatchPushEvent;
import com.common.notification.event.CRSyncInviteUserNotifyEvent;
import com.common.notification.event.EStandFullStarEvent;
import com.common.notification.event.FeedCommentAddNotifyEvent;
import com.common.notification.event.FeedCommentLikeNotifyEvent;
import com.common.notification.event.FeedLikeNotifyEvent;
import com.common.notification.event.FollowNotifyEvent;
import com.common.notification.event.GrabInviteNotifyEvent;
import com.common.notification.event.MicRoomInviteEvent;
import com.common.notification.event.PostsCommentAddEvent;
import com.common.notification.event.PostsCommentLikeEvent;
import com.common.notification.event.PostsLikeEvent;
import com.common.notification.event.SysWarnNotifyEvent;
import com.zq.live.proto.Notification.CombineRoomEnterMsg;
import com.zq.live.proto.Notification.CombineRoomInviteMsg;
import com.zq.live.proto.Notification.CombineRoomInviteV2Msg;
import com.zq.live.proto.Notification.CombineRoomRefuseMsg;
import com.zq.live.proto.Notification.ECombineRoomEnterType;
import com.zq.live.proto.Notification.EInviteType;
import com.zq.live.proto.Notification.ENotificationMsgType;
import com.zq.live.proto.Notification.ERInviteType;
import com.zq.live.proto.Notification.FeedCommentAddMsg;
import com.zq.live.proto.Notification.FeedCommentLikeMsg;
import com.zq.live.proto.Notification.FeedLikeMsg;
import com.zq.live.proto.Notification.FollowMsg;
import com.zq.live.proto.Notification.InviteMicMsg;
import com.zq.live.proto.Notification.InviteStandMsg;
import com.zq.live.proto.Notification.NotificationMsg;
import com.zq.live.proto.Notification.PostsCommentAddMsg;
import com.zq.live.proto.Notification.PostsCommentLikeMsg;
import com.zq.live.proto.Notification.PostsLikeMsg;
import com.zq.live.proto.Notification.SysWarningMsg;
import com.zq.live.proto.broadcast.ERoomBroadcastMsgType;
import com.zq.live.proto.broadcast.RoomBroadcastMsg;
import com.zq.live.proto.broadcast.StandFullStar;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

/**
 * 通知管理器
 */
public class NotificationPushManager {

    public final String TAG = "NotificationManager";

    private static class NotificationAdapterHolder {
        private static final NotificationPushManager INSTANCE = new NotificationPushManager();
    }


    private NotificationPushManager() {

    }


    public static final NotificationPushManager getInstance() {
        return NotificationAdapterHolder.INSTANCE;
    }

    ArrayList<Long> broadcastFilterList = new ArrayList<>(10);

    /**
     * 处理广播消息
     *
     * @param msg
     */
    public void processBroadcastMsg(RoomBroadcastMsg msg) {
        MyLog.d(TAG, "processBroadcastMsg" + " msg=" + msg);
        if (msg.getMsgType() == ERoomBroadcastMsgType.RBRT_STAND_FULL_STAR) {
            Long ts = msg.getTimeMs();
            if (broadcastFilterList.contains(ts)) {
                MyLog.d(TAG, "processBroadcastMsg 这条是重复的广播消息，过滤掉");
            } else {
                broadcastFilterList.add(ts);
                if (broadcastFilterList.size() > 10) {
                    broadcastFilterList.remove(0);
                }
                StandFullStar pb = msg.getStandFullStar();
                EventBus.getDefault().post(new EStandFullStarEvent(pb));
            }
        } else if (msg.getMsgType() == ERoomBroadcastMsgType.RBRT_PRESENT_GIFT) {
            EventBus.getDefault().post(msg.getPresentGift());
        }
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
        } else if (msg.getMsgType() == ENotificationMsgType.NM_CR_INVITEV2) {
            processInviteMsgV2(baseNotiInfo, msg.getInviteV2Msg());
        } else if (msg.getMsgType() == ENotificationMsgType.NM_CR_ENTER) {
            processEnterRoomMsg(baseNotiInfo, msg.getEnterMsg());
        } else if (msg.getMsgType() == ENotificationMsgType.NM_CR_REFUSE) {
            processRefuseDoubleRoomMsg(baseNotiInfo, msg.getRefuseMsg());
        } else if (msg.getMsgType() == ENotificationMsgType.NM_FD_LIKE) {
            processFeedLikeMsg(baseNotiInfo, msg.getFeedLikeMsg());
        } else if (msg.getMsgType() == ENotificationMsgType.NM_FD_COMMENT_LIKE) {
            processFeedCommentLikeMsg(baseNotiInfo, msg.getFeedCommentLikeMsg());
        } else if (msg.getMsgType() == ENotificationMsgType.NM_FD_COMMENT_ADD) {
            processFeedCommentAddMsg(baseNotiInfo, msg.getFeedCommentAddMsg());
        } else if (msg.getMsgType() == ENotificationMsgType.NM_PS_LIKE) {
            processPostsLikeMsg(baseNotiInfo, msg.getPostsLikeMsg());
        } else if (msg.getMsgType() == ENotificationMsgType.NM_PS_COMMENT_LIKE) {
            processPostsCommentLikeMsg(baseNotiInfo, msg.getPostsCommentLikeMsg());
        } else if (msg.getMsgType() == ENotificationMsgType.NM_PS_COMMENT_ADD) {
            processPostsCommentAddMsg(baseNotiInfo, msg.getPostsCommentAddMsg());
        } else if (msg.getMsgType() == ENotificationMsgType.NM_INVITE_MIC) {
            processMicInviteMsg(baseNotiInfo, msg.getInviteMicMsg());
        } else if (msg.getMsgType() == ENotificationMsgType.NM_SPFOLLOW_NEWPOST) {
            EventBus.getDefault().post(msg.getSpFollowNewPostMsg());
        } else if (msg.getMsgType() == ENotificationMsgType.NM_SPFOLLOW_UPDATEALBUM) {
            EventBus.getDefault().post(msg.getSpFollowUpdateAlbumMsg());
        } else if (msg.getMsgType() == ENotificationMsgType.NM_GIFT_RECEIVES) {
            EventBus.getDefault().post(msg.getGiftReceivesMsg());
        } else if (msg.getMsgType() == ENotificationMsgType.NM_INVITE_PARTY) {
            EventBus.getDefault().post(msg.getInvitePartyMsg());
        } else if (msg.getMsgType() == ENotificationMsgType.NM_CLUBINFO_CHANGE) {
            EventBus.getDefault().post(msg.getClubInfoChangeMsg());
        } else if (msg.getMsgType() == ENotificationMsgType.NM_RELAY_ENTER) {
            if (msg.getRelayRoomEnterMsg().getInviteType() == ERInviteType.RIT_OUT_COMBINE_ROOM) {
                EventBus.getDefault().post(new CNRelayEnterFromOuterInviteNotifyEvent(msg.getRelayRoomEnterMsg()));
            } else if (msg.getRelayRoomEnterMsg().getInviteType() == ERInviteType.RIT_IN_COMBINE_ROOM) {
                EventBus.getDefault().post(new CNRelayEnterFromRoomInviteNotifyEvent(msg.getRelayRoomEnterMsg()));
            } else if (msg.getRelayRoomEnterMsg().getInviteType() == ERInviteType.RIT_REDPACKET_INVITE) {
                EventBus.getDefault().post(new CNRelayEnterFromRedpacketNotifyEvent(msg.getRelayRoomEnterMsg()));
            }
        } else if (msg.getMsgType() == ENotificationMsgType.NM_RELAY_REFUSE) {
            EventBus.getDefault().post(msg.getRelayRoomRefuseMsg());
        } else if (msg.getMsgType() == ENotificationMsgType.NM_RELAY_INVITE) {
            EventBus.getDefault().post(msg.getRelayRoomInviteMsg());
        }
    }

    private void processPostsLikeMsg(BaseNotiInfo baseNotiInfo, PostsLikeMsg postsLikeMsg) {
        if (postsLikeMsg != null) {
            PostsLikeEvent postsLikeEvent = new PostsLikeEvent(baseNotiInfo, postsLikeMsg);
            EventBus.getDefault().post(postsLikeEvent);
        } else {
            MyLog.e(TAG, "processPostsLikeMsg postsLikeMsg=null");
        }
    }

    private void processPostsCommentLikeMsg(BaseNotiInfo baseNotiInfo, PostsCommentLikeMsg postsCommentLikeMsg) {
        if (postsCommentLikeMsg != null) {
            PostsCommentLikeEvent feedCommentAddNotifyEvent = new PostsCommentLikeEvent(baseNotiInfo, postsCommentLikeMsg);
            EventBus.getDefault().post(feedCommentAddNotifyEvent);
        } else {
            MyLog.e(TAG, "processPostsCommentLikeMsg postsCommentLikeMsg=null");
        }
    }

    private void processMicInviteMsg(BaseNotiInfo baseNotiInfo, InviteMicMsg inviteMicMsg) {
        if (inviteMicMsg != null) {
            MicRoomInviteEvent micRoomInviteEvent = new MicRoomInviteEvent(baseNotiInfo, inviteMicMsg);
            EventBus.getDefault().post(micRoomInviteEvent);
        } else {
            MyLog.e(TAG, "processMicInviteMsg postsCommentAddMsg=null");
        }
    }

    private void processPostsCommentAddMsg(BaseNotiInfo baseNotiInfo, PostsCommentAddMsg postsCommentAddMsg) {
        if (postsCommentAddMsg != null) {
            PostsCommentAddEvent feedCommentAddNotifyEvent = new PostsCommentAddEvent(baseNotiInfo, postsCommentAddMsg);
            EventBus.getDefault().post(feedCommentAddNotifyEvent);
        } else {
            MyLog.e(TAG, "processPostsCommentAddMsg postsCommentAddMsg=null");
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
    private void processInviteMsg(BaseNotiInfo baseNotiInfo, CombineRoomInviteMsg combineRoomInviteMsg) {
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

    /**
     * 邀请解析，所有邀请都包括
     */
    private void processInviteMsgV2(BaseNotiInfo baseNotiInfo, CombineRoomInviteV2Msg combineRoomInviteMsg) {
        if (combineRoomInviteMsg != null) {
            if (combineRoomInviteMsg.getInviteType() == EInviteType.IT_OUT_COMBINE_ROOM) {
                //房间外，目前是一场到底里面
                CRSendInviteUserNotifyEvent combineRoomSendInviteUserEvent = new CRSendInviteUserNotifyEvent(baseNotiInfo, combineRoomInviteMsg);
                EventBus.getDefault().post(combineRoomSendInviteUserEvent);
            } else if (combineRoomInviteMsg.getInviteType() == EInviteType.IT_IN_COMBINE_ROOM) {
                //双人房房间里
                EventBus.getDefault().post(new CRInviteInCreateRoomNotifyEvent(baseNotiInfo, combineRoomInviteMsg));
            } else {
                MyLog.e(TAG, "processInviteMsgV2 unknown type=" + combineRoomInviteMsg.getInviteType());
            }
        } else {
            MyLog.e(TAG, "processInviteMsgV2 combineRoomInviteMsg=null");
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

    private void processFeedLikeMsg(BaseNotiInfo baseNotiInfo, FeedLikeMsg feedLikeMsg) {
        if (feedLikeMsg != null) {
            FeedLikeNotifyEvent feedLikeNotifyEvent = new FeedLikeNotifyEvent(baseNotiInfo, feedLikeMsg);
            EventBus.getDefault().post(feedLikeNotifyEvent);
        } else {
            MyLog.e(TAG, "processFeedLikeMsg feedLikeMsg=null");
        }
    }

    private void processFeedCommentLikeMsg(BaseNotiInfo baseNotiInfo, FeedCommentLikeMsg feedCommentLikeMsg) {
        if (feedCommentLikeMsg != null) {
            FeedCommentLikeNotifyEvent feedCommentLikeNotifyEvent = new FeedCommentLikeNotifyEvent(baseNotiInfo, feedCommentLikeMsg);
            EventBus.getDefault().post(feedCommentLikeNotifyEvent);
        } else {
            MyLog.e(TAG, "processFeedCommentLikeMsg feedCommentLikeMsg=null");
        }
    }

    private void processFeedCommentAddMsg(BaseNotiInfo baseNotiInfo, FeedCommentAddMsg feedCommentAddMsg) {
        if (feedCommentAddMsg != null) {
            FeedCommentAddNotifyEvent feedCommentAddNotifyEvent = new FeedCommentAddNotifyEvent(baseNotiInfo, feedCommentAddMsg);
            EventBus.getDefault().post(feedCommentAddNotifyEvent);
        } else {
            MyLog.e(TAG, "processFeedCommentAddMsg feedCommentAddMsg=null");
        }
    }
}
