package com.mi.liveassistant.barrage.data;

import android.text.TextUtils;

import com.mi.liveassistant.account.UserAccountManager;
import com.mi.liveassistant.barrage.model.BarrageMsg;
import com.mi.liveassistant.barrage.model.BarrageMsgType;

/**
 * Created by wuxiaoshan on 17-5-3.
 */
public class Message implements Comparable<Message> {
    private int msgType;
    private long senderMsgId; //发送方的消息，可以用来去重
    private long sender;
    private String roomId;
    private long sentTime;
    private String senderName;//发送者名称
    private int senderLevel; //发送者用户级别
    private String body;
    private long anchorId;
    private int roomType;
    private long toUserId;

    private MessageExt messageExt;

    public MessageExt getMessageExt() {
        return messageExt;
    }

    public void setMessageExt(MessageExt messageExt) {
        this.messageExt = messageExt;
    }

    public long getSenderMsgId() {
        return senderMsgId;
    }

    public void setSenderMsgId(long senderMsgId) {
        this.senderMsgId = senderMsgId;
    }

    public long getSender() {
        return sender;
    }

    public void setSender(long sender) {
        this.sender = sender;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public long getSentTime() {
        return sentTime;
    }

    public void setSentTime(long sentTime) {
        this.sentTime = sentTime;
    }

    public int getMsgType() {
        return msgType;
    }

    public void setMsgType(int msgType) {
        this.msgType = msgType;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public int getSenderLevel() {
        return senderLevel;
    }

    public void setSenderLevel(int senderLevel) {
        this.senderLevel = senderLevel;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public long getAnchorId() {
        return anchorId;
    }

    public void setAnchorId(long anchorId) {
        this.anchorId = anchorId;
    }

    public int getRoomType() {
        return roomType;
    }

    public void setRoomType(int roomType) {
        this.roomType = roomType;
    }

    public long getToUserId() {
        return toUserId;
    }

    public void setToUserId(long toUserId) {
        this.toUserId = toUserId;
    }

    @Override
    public int compareTo(Message otherMsg) {
        if (otherMsg == null) {
            return -1;
        } else {
            if (this.sentTime < otherMsg.sentTime) {
                return -1;
            } else if (this.sentTime > otherMsg.sentTime) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    public static Message loadFromBarrage(BarrageMsg msg) {
        Message liveComment = new Message();
        liveComment.setSender(msg.getSender());
        liveComment.setMsgType(msg.getMsgType());
        liveComment.setSenderLevel(msg.getSenderLevel());
        String name = msg.getSenderName();
        if (TextUtils.isEmpty(name)) {
            liveComment.setSenderName(String.valueOf(liveComment.getSender()));
        } else {
            liveComment.setSenderName(name);
        }
        liveComment.setSentTime(msg.getSentTime());
        liveComment.setSenderMsgId(msg.getSenderMsgId());

        switch (liveComment.getMsgType()) {
            //以下是　系统消息类 　不显示名字和级别
            case BarrageMsgType.B_MSG_TYPE_FORBIDDEN:
            case BarrageMsgType.B_MSG_TYPE_CANCEL_FORBIDDEN: {
                BarrageMsg.ForbiddenMsgExt msgExt = (BarrageMsg.ForbiddenMsgExt) msg.getMsgExt();
//                MyLog.w("ForbiddenMsg"+msg.toString());
                String message = msgExt.getBanMessage(msg.getAnchorId(), UserAccountManager.getInstance().getUuidAsLong(), msg.getMsgType(), msg.getSenderName());
                liveComment.setBody(message);
                liveComment.setSenderLevel(0);
                liveComment.setSenderName(null);
                liveComment.setMessageExt(new MessageExt.ForbiddenMessageExt(msgExt));
            }
            break;
            case BarrageMsgType.B_MSG_TYPE_SET_MANAGER: {
                liveComment.setBody("主播设置你为管理员");
                liveComment.setSenderLevel(0);
                liveComment.setSenderName(null);
            }
            break;
            case BarrageMsgType.B_MSG_TYPE_CANCEL_MANAGER: {
                liveComment.setBody("主播取消了你的管理员");
                liveComment.setSenderLevel(0);
                liveComment.setSenderName(null);
            }
            break;
            case BarrageMsgType.B_MSG_TYPE_FREQUENCY_CONTROL:
                BarrageMsg.MsgRuleChangeMessageExt msgRuleChangeMessageExt = (BarrageMsg.MsgRuleChangeMessageExt) msg.getMsgExt();
                if (msgRuleChangeMessageExt != null) {
                    liveComment.setMessageExt(new MessageExt.FrequencyControlMessageExt(msgRuleChangeMessageExt.getMessageRule()));
                }
                break;
            case BarrageMsgType.B_MSG_TYPE_KICK_VIEWER:
                BarrageMsg.KickMessageExt kickMessageExt = (BarrageMsg.KickMessageExt) msg.getMsgExt();
                if (kickMessageExt != null) {
                    liveComment.setMessageExt(new MessageExt.KickMessageExt(kickMessageExt));
                }
                break;
            case BarrageMsgType.B_MSG_TYPE_COMMEN_SYS_MSG:
            case BarrageMsgType.B_MSG_TYPE_GLOBAL_SYS_MSG:
            case BarrageMsgType.B_MSG_TYPE_ROOM_SYS_MSG:
                liveComment.setMsgType(MessageType.MSG_TYPE_ROOM_SYS_MSG);
                liveComment.setBody(msg.getBody());
                break;
            case BarrageMsgType.B_MSG_TYPE_TOP_GET:
            case BarrageMsgType.B_MSG_TYPE_TOP_LOSE:
            case BarrageMsgType.B_MSG_TYPE_LIVE_END:
                if (msg.getMsgExt() != null && msg.getMsgExt() instanceof BarrageMsg.LiveEndMsgExt) {
                    BarrageMsg.LiveEndMsgExt liveEndMsgExt = (BarrageMsg.LiveEndMsgExt) msg.getMsgExt();
                    liveComment.setMessageExt(new MessageExt.LiveEndMessageExt(liveEndMsgExt));
                }
                liveComment.setBody(msg.getBody());
                break;
            case BarrageMsgType.B_MSG_TYPE_ANCHOR_LEAVE:
                liveComment.setBody(msg.getBody());
                break;
            case BarrageMsgType.B_MSG_TYPE_ANCHOR_JOIN:
                liveComment.setBody(msg.getBody());
                break;
            case BarrageMsgType.B_MSG_TYPE_VIEWER_CHANGE:
                if (msg.getMsgExt() != null && msg.getMsgExt() instanceof BarrageMsg.ViewerChangeMsgExt) {
                    BarrageMsg.ViewerChangeMsgExt viewerChangeMsgExt = (BarrageMsg.ViewerChangeMsgExt) msg.getMsgExt();
                    liveComment.setMessageExt(new MessageExt.ViewChangeMessageExt(viewerChangeMsgExt));
                }
                liveComment.setBody(msg.getBody());
                break;
            case BarrageMsgType.B_MSG_TYPE_LIVE_OWNER_MSG:
            case BarrageMsgType.B_MSG_TYPE_LINE_VIEWER_BACK:
            case BarrageMsgType.B_MSG_TYPE_LINE_VIEWER_LEAVE: {
                liveComment.setBody(msg.getBody());
            }
            break;
            //用户行为类 显示名字和级别
            case BarrageMsgType.B_MSG_TYPE_ROOM_FOUCES_ANCHOR: {
                if (!TextUtils.isEmpty(name)) {
                    liveComment.setBody("关注了主播");
                } else {
                    liveComment.setSenderName(null);
                    liveComment.setBody(msg.getBody());
                }
            }
            break;
            case BarrageMsgType.B_MSG_TYPE_ANIM:
            case BarrageMsgType.B_MSG_TYPE_KICK_VIEWER_BARRAGE:
                break;
            case BarrageMsgType.B_MSG_TYPE_SHARE: {
                if (!TextUtils.isEmpty(name)) {
                    liveComment.setBody("分享了直播");
                } else {
                    liveComment.setBody(msg.getBody());
                }
            }
            break;
            case BarrageMsgType.B_MSG_TYPE_LEAVE: {
                if (msg.getMsgExt() != null && msg.getMsgExt() instanceof BarrageMsg.LeaveRoomMsgExt) {
                    BarrageMsg.LeaveRoomMsgExt leaveRoomMsgExt = (BarrageMsg.LeaveRoomMsgExt) msg.getMsgExt();
                    liveComment.setMessageExt(new MessageExt.LeaveRoomMessageExt(leaveRoomMsgExt));
                }
                if (!TextUtils.isEmpty(name)) {
                    liveComment.setBody("离开房间");
                } else {
                    liveComment.setBody(msg.getBody());
                }
            }
            break;
            case BarrageMsgType.B_MSG_TYPE_JOIN: {
                if (msg.getMsgExt() != null && msg.getMsgExt() instanceof BarrageMsg.JoinRoomMsgExt) {
                    BarrageMsg.JoinRoomMsgExt joinRoomMsgExt = (BarrageMsg.JoinRoomMsgExt) msg.getMsgExt();
                    liveComment.setMessageExt(new MessageExt.JoinRoomMessageExt(joinRoomMsgExt));
                }
                if (!TextUtils.isEmpty(name)) {
                    liveComment.setBody("进入房间");
                } else {
                    liveComment.setBody(msg.getBody());
                }
            }
            break;
            case BarrageMsgType.B_MSG_TYPE_LIKE: {
                if (!TextUtils.isEmpty(name)) {
                    liveComment.setBody("点亮了");
                } else {
                    liveComment.setSenderName(null);
                    liveComment.setBody(msg.getBody());
                }
            }
            break;
            case BarrageMsgType.B_MSG_TYPE_GIFT:
            case BarrageMsgType.B_MSG_TYPE_PAY_BARRAGE:
            case BarrageMsgType.B_MSG_TYPE_ROOM_BACKGROUND_GIFT:
            case BarrageMsgType.B_MSG_TYPE_LIGHT_UP_GIFT:
                BarrageMsg.GiftMsgExt giftMsgExt = (BarrageMsg.GiftMsgExt) msg.getMsgExt();
                liveComment.setMessageExt(new MessageExt.GiftMessageExt(giftMsgExt));
                liveComment.setBody(msg.getBody());
                break;
            case BarrageMsgType.B_MSG_TYPE_RED_ENVELOPE:
            case BarrageMsgType.B_MSG_TYPE_GLABAL_MSG:
                liveComment.setBody(msg.getBody());
                break;
            default: {
                liveComment.setBody(msg.getBody());
            }
            break;
        }
        return liveComment;
    }
}
