package com.mi.live.data.push.model;

import android.text.TextUtils;

import com.base.global.GlobalData;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.R;
import com.mi.live.data.query.model.MessageRule;
import com.mi.live.data.query.model.SystemMsgModel;
import com.mi.live.data.query.model.ViewerModel;
import com.wali.live.proto.LiveCommonProto;
import com.wali.live.proto.LiveMallProto;
import com.wali.live.proto.LiveMessageProto;
import com.wali.live.proto.LiveMicProto;
import com.wali.live.proto.LivePKProto;
import com.wali.live.proto.RedEnvelProto;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * @module com.wali.live.message.data
 * <p>
 * Created by MK on 16/2/23.
 * 弹幕消息对象
 * @moudle 弹幕
 */
public class BarrageMsg implements Comparable<BarrageMsg> {

    public static BarrageMsg toBarrageMsg(LiveMessageProto.Message msg) {
        BarrageMsg barrageMsg = new BarrageMsg();
        barrageMsg.setSender(msg.getFromUser());
        barrageMsg.setRoomId(msg.getRoomId());
        if (!TextUtils.isEmpty(msg.getMsgBody())) {
            barrageMsg.setBody(msg.getMsgBody());
        }
        barrageMsg.setSenderMsgId(msg.getCid());
        barrageMsg.setSenderLevel(msg.getFromUserLevel());
        barrageMsg.setSenderName(msg.getFromUserNickName());
        barrageMsg.setMsgType(msg.getMsgType());
        barrageMsg.setSentTime(msg.getTimestamp());
        barrageMsg.setCertificationType(msg.getCertificationType());
        barrageMsg.setMsgExt(msg.getMsgExt().toByteArray(), msg.getMsgType());
        if (msg.hasToUser()) {
            barrageMsg.setToUserId(msg.getToUser());
        }
        //pk相关
        barrageMsg.setRoomType(msg.getRoomType());
        barrageMsg.setOriginRoomId(msg.getOriginRoomId());
        barrageMsg.setAnchorId(msg.getToUser());
        int loc = Arrays.binarySearch(BarrageMsgType.types, msg.getMsgType());
        if (loc < 0 && !TextUtils.isEmpty(msg.getSupportTxt())) {
            barrageMsg.setBody(msg.getSupportTxt());
        }

        String fromUserName = msg.getFromUserShowName(); //和from_user_nick_name的区别是不可能系统消息
        if (!TextUtils.isEmpty(fromUserName)) {
            barrageMsg.setSenderName(fromUserName);
        } else if (barrageMsg.getSenderName().equals(com.base.global.GlobalData.app().getString(R.string.sys_msg))) {
            barrageMsg.setSenderName(fromUserName);
        }

        if (msg.hasIsFromUserRedname() && msg.getIsFromUserRedname()) {
            barrageMsg.setRedName(msg.getIsFromUserRedname());
        }
        if (msg.getGlobalRoomMsgExt() != null) {
            barrageMsg.setGlobalRoomMsgExt(GlobalRoomMsgExt.loadFromPB(msg.getGlobalRoomMsgExt()));
        }
        barrageMsg.setVipFrozen(msg.getVipDisable());
        barrageMsg.setVipLevel(msg.getVipLevel());
        barrageMsg.setVipHide(msg.getVipHidden());
        return barrageMsg;
    }

    public static final int ROOM_TYPE_NORMAL = 0;
    public static final int ROOM_TYPE_PK = 1;

    /**
     * 操作人类型，用于踢人、禁言等消息
     **/
    public static final int OPERATOR_TYPE_ADMIN = 0;             //管理员
    public static final int OPERATOR_TYPE_INSPECTOR = 1;         //巡查员
    public static final int OPERATOR_TYPE_TOP1 = 2;              //热门榜一
    public static final int OPERATOR_TYPE_OWNER = 3;             //房主
    public static final int OPERATOR_TYPE_OSS = 4;               //运营管理端后台

    private long sender;
    private String roomId;
    private long sentTime;
    private long senderMsgId;                          //发送方的消息，可以用来去重
    private int msgType;
    private String senderName;                         //发送者名称
    private int senderLevel;                           //发送者用户级别
    private String body;
    private long anchorId;
    private int certificationType;
    private int resendTimes;                           //用来客户端重发弹幕逻辑调整
    private MsgExt msgExt;
    private int roomType = ROOM_TYPE_NORMAL;
    private String originRoomId;                       // 消息来源的roomId
    private boolean isFromPkOpponent = false;
    private String opponentRoomId;
    private long opponentAnchorId;
    private long toUserId;
    private boolean isRedName;                         // 是否被社区红名，红名表示是不友好名单，这类用户不显示等级，并且灰色字体显示弹幕
    private GlobalRoomMsgExt globalRoomMsgExt;         // 所有类型弹幕扩展字段(针对多种类型的弹幕)
    private int vipLevel;                              //vip等级
    private boolean isVipFrozen;                       //vip是否被冻结
    private boolean isVipHide;                         //vip用户是否设置隐身

    public BarrageMsg() {
        this.senderMsgId = System.currentTimeMillis();
    }

    public long getToUserId() {
        return toUserId;
    }

    public void setToUserId(long toUserId) {
        this.toUserId = toUserId;
    }

    public long getSender() {
        return sender;
    }

    public void setSender(long sender) {
        this.sender = sender;
    }

    public String getSenderName() {
//        if (TextUtils.isEmpty(senderName)) {
//            return String.valueOf(sender);
//        }
        return senderName;
    }

    @Override
    public String toString() {
        return "BarrageMsg{" +
                "sender=" + sender +
                ", roomId='" + roomId + '\'' +
                ", sentTime=" + sentTime +
                ", senderMsgId=" + senderMsgId +
                ", msgType=" + msgType +
                ", senderName='" + senderName + '\'' +
                ", senderLevel=" + senderLevel +
                ", body='" + body + '\'' +
                ", anchorId=" + anchorId +
                ", certificationType=" + certificationType +
                ", resendTimes=" + resendTimes +
                ", msgExt=" + msgExt +
                ", roomType=" + roomType +
                ", originRoomId='" + originRoomId + '\'' +
                ", isFromPkOpponent=" + isFromPkOpponent +
                ", opponentRoomId='" + opponentRoomId + '\'' +
                ", opponentAnchorId=" + opponentAnchorId +
                ", toUserId=" + toUserId +
                ", isRedName=" + isRedName +
                ", globalRoomMsgExt=" + globalRoomMsgExt +
                ", vipLevel=" + vipLevel +
                ", isVipFrozen=" + isVipFrozen +
                ", isVipHide=" + isVipHide +
                '}';
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
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

    public long getSenderMsgId() {
        return senderMsgId;
    }

    public void setSenderMsgId(long senderMsgId) {
        this.senderMsgId = senderMsgId;
    }

    public int getMsgType() {
        return msgType;
    }

    public void setMsgType(int msgType) {
        this.msgType = msgType;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public int getSenderLevel() {
        return senderLevel;
    }

    public void setSenderLevel(int level) {
        this.senderLevel = level;
    }

    public long getAnchorId() {
        return anchorId;
    }

    public void setAnchorId(long anchorId) {
        this.anchorId = anchorId;
    }

    public int getCertificationType() {
        return certificationType;
    }

    public void setCertificationType(int certificationType) {
        this.certificationType = certificationType;
    }

    public int getResendTimes() {
        return resendTimes;
    }

    public void setResendTimes(int resendTimes) {
        this.resendTimes = resendTimes;
    }

    public String getOriginRoomId() {
        return originRoomId;
    }

    public int getRoomType() {
        return roomType;
    }

    public void setRoomType(int roomType) {
        this.roomType = roomType;
    }

    public void setOriginRoomId(String originRoomId) {
        this.originRoomId = originRoomId;
    }

    public boolean isFromPkOpponent() {
        return isFromPkOpponent;
    }

    public void setIsFromPkOpponent(boolean isFromPkOpponent) {
        this.isFromPkOpponent = isFromPkOpponent;
    }

    public String getOpponentRoomId() {
        return opponentRoomId;
    }

    public long getOpponentAnchorId() {
        return opponentAnchorId;
    }

    public void setOpponentRoomId(String opponentRoomId) {
        this.opponentRoomId = opponentRoomId;
    }

    public void setOpponentAnchorId(long opponentAnchorId) {
        this.opponentAnchorId = opponentAnchorId;
    }

    public boolean isRedName() {
        return isRedName;
    }

    public void setRedName(boolean redName) {
        isRedName = redName;
    }

    public MsgExt getMsgExt() {
        return msgExt;
    }

    public void setMsgExt(MsgExt msgExt) {
        this.msgExt = msgExt;
    }

    public GlobalRoomMsgExt getGlobalRoomMsgExt() {
        return globalRoomMsgExt;
    }

    public void setGlobalRoomMsgExt(GlobalRoomMsgExt globalRoomMsgExt) {
        this.globalRoomMsgExt = globalRoomMsgExt;
    }

    public int getVipLevel() {
        return vipLevel;
    }

    public void setVipLevel(int vipLevel) {
        this.vipLevel = vipLevel;
    }

    public boolean isVipFrozen() {
        return isVipFrozen;
    }

    public void setVipFrozen(boolean vipFrozen) {
        isVipFrozen = vipFrozen;
    }

    public boolean isVipHide() {
        return isVipHide;
    }

    public void setVipHide(boolean vipHide) {
        isVipHide = vipHide;
    }

    /*
            * 引入clean模式后这个方法要慢慢被废弃掉，因为所有设计到pb的数据操作都应放到data层
            * */
    public void setMsgExt(byte[] data, int msgType) {
        if (data != null) {
            try {
                switch (msgType) {
                    case BarrageMsgType.B_MSG_TYPE_CANCEL_FORBIDDEN:
                    case BarrageMsgType.B_MSG_TYPE_FORBIDDEN: {
                        ForbiddenMsgExt ext = new ForbiddenMsgExt();
                        LiveMessageProto.ForbiddenMessage f = LiveMessageProto.ForbiddenMessage.parseFrom(data);
                        ext.forbiddenUserId = f.getUserId();
                        ext.operatorType = f.getOpType();
                        ext.banNickname = f.getBanNickname();
                        msgExt = ext;
                    }
                    break;
                    case BarrageMsgType.B_MSG_TYPE_LIKE: {
                        LikeMsgExt ext = new LikeMsgExt();
                        LiveMessageProto.LikeMessage like = LiveMessageProto.LikeMessage.parseFrom(data);
                        ext.id = like.getId();
                        msgExt = ext;
                    }
                    break;
                    case BarrageMsgType.B_MSG_TYPE_PK_BEGIN: {
                        PkMessageExt ext = new PkMessageExt();
                        LiveMessageProto.PKBeginMessage pkRoomInfo = LiveMessageProto.PKBeginMessage.parseFrom(data);
                        ext.roomId = pkRoomInfo.getOtherPKInfo().getLiveId();
                        ext.zuid = pkRoomInfo.getOtherPKInfo().getUuid();
                        ext.myInitTicket = pkRoomInfo.getThisPKInfo().getPkInitTicket();
                        ext.otherInitTicket = pkRoomInfo.getOtherPKInfo().getPkInitTicket();
                        msgExt = ext;
                    }
                    break;
                    case BarrageMsgType.B_MSG_TYPE_PK_END: {
                        PkMessageExt ext = new PkMessageExt();
                        LiveMessageProto.PKEndMessage pkRoomInfo = LiveMessageProto.PKEndMessage.parseFrom(data);
                        ext.roomId = pkRoomInfo.getOtherPKInfo().getLiveId();
                        ext.zuid = pkRoomInfo.getOtherPKInfo().getUuid();
                        msgExt = ext;
                    }
                    break;
                    case BarrageMsgType.B_MSG_TYPE_JOIN: {
                        JoinRoomMsgExt ext = new JoinRoomMsgExt();
                        LiveMessageProto.JoinRoomMessage join = LiveMessageProto.JoinRoomMessage.parseFrom(data);
                        {
                            List temp = new ArrayList<ViewerModel>();
                            List<LiveCommonProto.Viewer> list = join.getViewersList();
                            if (list != null) {
                                for (LiveCommonProto.Viewer v : list) {
                                    ViewerModel viewer = new ViewerModel(v.getUuid(), v.getLevel(), v.getAvatar(), v.getCertificationType(), v.getRedName());
                                    temp.add(viewer);
                                }
                            }
                            ext.viewerList = temp;
                        }
                        ext.viewerCount = join.getViewerCount();
                        msgExt = ext;
                    }
                    break;
                    case BarrageMsgType.B_MSG_TYPE_LEAVE: {
                        LeaveRoomMsgExt ext = new LeaveRoomMsgExt();
                        LiveMessageProto.LeaveRoomMessage leave = LiveMessageProto.LeaveRoomMessage.parseFrom(data);
                        {
                            List temp = new ArrayList<ViewerModel>();
                            List<LiveCommonProto.Viewer> list = leave.getViewersList();
                            if (list != null) {
                                for (LiveCommonProto.Viewer v : list) {
                                    ViewerModel viewer = new ViewerModel(v.getUuid(), v.getLevel(), v.getAvatar(), v.getCertificationType(), v.getRedName());
                                    temp.add(viewer);
                                }
                            }
                            ext.viewerList = temp;
                        }
                        ext.viewerCount = leave.getViewerCount();
                        msgExt = ext;
                    }
                    break;
                    case BarrageMsgType.B_MSG_TYPE_VIEWER_CHANGE: {
                        ViewerChangeMsgExt ext = new ViewerChangeMsgExt();
                        LiveMessageProto.ViewerMessage viewerChange = LiveMessageProto.ViewerMessage.parseFrom(data);
                        {
                            List temp = new ArrayList<ViewerModel>();
                            List<LiveCommonProto.Viewer> list = viewerChange.getViewersList();
                            if (list != null) {
                                for (LiveCommonProto.Viewer v : list) {
                                    ViewerModel viewer = new ViewerModel(v.getUuid(), v.getLevel(), v.getAvatar(), v.getCertificationType(), v.getRedName());
                                    temp.add(viewer);
                                }
                            }
                            ext.viewerList = temp;
                        }
                        ext.viewerCount = viewerChange.getViewerCount();
                        msgExt = ext;
                    }
                    break;
                    case BarrageMsgType.B_MSG_TYPE_LIVE_START:
                        msgExt = new LiveStartMsgExt();
                        break;
                    case BarrageMsgType.B_MSG_TYPE_LIVE_END: {
                        LiveEndMsgExt ext = new LiveEndMsgExt();
                        LiveMessageProto.LiveEndMessage liveEndMsgExt = LiveMessageProto.LiveEndMessage.parseFrom(data);
                        ext.viewerCount = liveEndMsgExt.getHisViewerCnt();
                        msgExt = ext;
                    }
                    break;
                    case BarrageMsgType.B_MSG_TYPE_GLOBAL_SYS_MSG: {
                        GlobalMessageExt globalMessageExt = new GlobalMessageExt();
                        LiveMessageProto.GlobalSystemMessageBox globalSystemMessageBox = LiveMessageProto.GlobalSystemMessageBox.parseFrom(data);
                        List<SystemMsgModel> temp = new ArrayList();
                        List<LiveMessageProto.GlobalSystemMessage> globalSystemMessages = globalSystemMessageBox.getGlobalSystemMessageList();
                        if (globalSystemMessages != null) {
                            for (LiveMessageProto.GlobalSystemMessage globalSystemMessage : globalSystemMessages) {
                                for (LiveMessageProto.SystemMessage sm : globalSystemMessage.getSystemMessageList()) {
                                    SystemMsgModel systemMsg = new SystemMsgModel();
                                    systemMsg.setFromUser(sm.getFromUser());
                                    systemMsg.setContent(sm.getContent());
                                    systemMsg.setStartTime(sm.getStartTime());
                                    systemMsg.setEndTime(sm.getEndTime());
                                    temp.add(systemMsg);
                                }
                            }
                        }
                        globalMessageExt.systemMessageList = temp;
                        msgExt = globalMessageExt;
                    }
                    break;
                    case BarrageMsgType.B_MSG_TYPE_ROOM_SYS_MSG: {
                        RoomMessageExt roomMessageExt = new RoomMessageExt();
                        LiveMessageProto.RoomSystemMessage roomSystemMessage = LiveMessageProto.RoomSystemMessage.parseFrom(data);
                        List<LiveMessageProto.SystemMessage> roomSystemMessageList = roomSystemMessage.getSystemMessageList();
                        List<SystemMsgModel> temp = new ArrayList();
                        if (roomSystemMessageList != null) {
                            for (LiveMessageProto.SystemMessage sm : roomSystemMessageList) {
                                SystemMsgModel systemMsg = new SystemMsgModel();
                                systemMsg.setFromUser(sm.getFromUser());
                                systemMsg.setContent(sm.getContent());
                                systemMsg.setStartTime(sm.getStartTime());
                                systemMsg.setEndTime(sm.getEndTime());
                                temp.add(systemMsg);
                            }
                        }
                        roomMessageExt.roomSystemMessageList = temp;
                        msgExt = roomMessageExt;
                    }
                    break;
                    case BarrageMsgType.B_MSG_TYPE_LIVE_OWNER_MSG: {
                        AnchorMessageExt anchorMessageExt = new AnchorMessageExt();
                        LiveMessageProto.AnchorMessage anchorMessage = LiveMessageProto.AnchorMessage.parseFrom(data);
                        if (anchorMessage != null && !TextUtils.isEmpty(anchorMessage.getContent())) {
                            anchorMessageExt.content = anchorMessage.getContent();
                        }
                        msgExt = anchorMessageExt;
                    }
                    break;
                    case BarrageMsgType.B_MSG_TYPE_GIFT: {
                        LiveMessageProto.GiftMessage gift = LiveMessageProto.GiftMessage.parseFrom(data);
                        msgExt = GiftMsgExt.transformFromPB(gift);
                    }
                    break;
                    case BarrageMsgType.B_MSG_TYPE_PAY_BARRAGE: {
                        LiveMessageProto.GiftMessage gift = LiveMessageProto.GiftMessage.parseFrom(data);
                        msgExt = GiftMsgExt.transformFromPB(gift);
                    }
                    break;
                    case BarrageMsgType.B_MSG_TYPE_RED_ENVELOPE: {
                        RedEnvelProto.NotifyRedEnvelop redEnvelop = RedEnvelProto.NotifyRedEnvelop.parseFrom(data);
                        msgExt = RedEnvelopMsgExt.transformFromPB(redEnvelop);
                    }
                    break;
                    case BarrageMsgType.B_MSG_TYPE_LIGHT_UP_GIFT: {
                        LiveMessageProto.GiftMessage gift = LiveMessageProto.GiftMessage.parseFrom(data);
                        msgExt = GiftMsgExt.transformFromPB(gift);
                    }
                    break;
                    case BarrageMsgType.B_MSG_TYPE_ROOM_BACKGROUND_GIFT: {
                        LiveMessageProto.GiftMessage gift = LiveMessageProto.GiftMessage.parseFrom(data);
                        msgExt = GiftMsgExt.transformFromPB(gift);
                    }
                    break;
                    case BarrageMsgType.B_MSG_TYPE_GLABAL_MSG: {
                        LiveMessageProto.GiftMessage gift = LiveMessageProto.GiftMessage.parseFrom(data);
                        msgExt = GiftMsgExt.transformFromPB(gift);
                    }
                    break;
                    case BarrageMsgType.B_MSG_TYPE_FREQUENCY_CONTROL: {
                        LiveMessageProto.MsgRuleChangeMessage msgRul = LiveMessageProto.MsgRuleChangeMessage.parseFrom(data);
                        msgExt = new MsgRuleChangeMessageExt(msgRul);
                    }
                    break;
                    case BarrageMsgType.B_MSG_TYPE_KICK_VIEWER_BARRAGE: {
                        LiveMessageProto.KickMessage kickMessage = LiveMessageProto.KickMessage.parseFrom(data);
                        msgExt = new KickMessageExt(kickMessage);
                    }
                    break;

                    case BarrageMsgType.B_MSG_TYPE_TAP_TP_SELL: {
                        LiveMessageProto.TapToSellPushMsgExt sellMessage = LiveMessageProto.TapToSellPushMsgExt.parseFrom(data);
                        msgExt = new SellMessageExt(sellMessage);
                    }
                    break;

                    case BarrageMsgType.B_MSG_TYPE_ATTACHMENT: {
                        LiveMessageProto.WidgetMessage attachMessage = LiveMessageProto.WidgetMessage.parseFrom(data);
                        msgExt = new attachMessageExt(attachMessage);
                    }
                    break;
                    case BarrageMsgType.B_MSG_TYPE_ANIM: {
                        LiveMessageProto.RoomAnimationMessage animationMessage = LiveMessageProto.RoomAnimationMessage.parseFrom(data);
                        msgExt = new AnimMsgExt(animationMessage);
                    }
                    break;
                    case BarrageMsgType.B_MSG_TYPE_SELL: {
                        LiveMessageProto.PushShoppingInfoMsgExt shoppingInfoMsgExt = LiveMessageProto.PushShoppingInfoMsgExt.parseFrom(data);
                        msgExt = new ShoppingInfoMsgExt(shoppingInfoMsgExt);
                    }
                    break;
                    case BarrageMsgType.B_MSG_TYPE_ADD_SHOP: {
                        LiveMallProto.ShoppingMsgExt shopMessage = LiveMallProto.ShoppingMsgExt.parseFrom(data);
                        msgExt = new ShopMessageExt(shopMessage);
                        break;
                    }
                    case BarrageMsgType.B_MSG_TYPE_LEVEL_UPGRADE_SYS_MSG: {
                        LiveMessageProto.UpgradeMessage upgradeMessage = LiveMessageProto.UpgradeMessage.parseFrom(data);
                        msgExt = new UpgradeMessage(upgradeMessage);
                    }
                    break;
                    case BarrageMsgType.B_MSG_TYPE_ATTACHMENT_COUNTER: {
                        LiveMessageProto.WidgetClickMessage clickMessage = LiveMessageProto.WidgetClickMessage.parseFrom(data);
                        msgExt = new WidgetClickMessage(clickMessage);
                    }
                    break;
                    case BarrageMsgType.B_MSG_TYPE_RED_NAME_STATUES: {
                        LiveMessageProto.RedNameStatus redNameStatus = LiveMessageProto.RedNameStatus.parseFrom(data);
                    }
                    break;

                    case BarrageMsgType.B_MSG_TYPE_LINE_MIC_BEGIN: {
                        LiveMicProto.MicBeginMessage micMsg = LiveMicProto.MicBeginMessage.parseFrom(data);
                        msgExt = new BarrageMsgExt.MicBeginInfo().parseFromPB(micMsg);
                        break;
                    }
                    case BarrageMsgType.B_MSG_TYPE_LINE_MIC_END: {
                        LiveMicProto.MicEndMessage micMsg = LiveMicProto.MicEndMessage.parseFrom(data);
                        msgExt = new BarrageMsgExt.MicEndInfo().parseFromPB(micMsg);
                        break;
                    }
                    case BarrageMsgType.B_MSG_TYPE_NEW_PK_SCORE: {
                        LivePKProto.PKScoreChangeMsg pkMsg = LivePKProto.PKScoreChangeMsg.parseFrom(data);
                        msgExt = new BarrageMsgExt.PkScoreInfo().parseFromPB(pkMsg);
                        break;
                    }
                    case BarrageMsgType.B_MSG_TYPE_NEW_PK_START: {
                        LivePKProto.PKBeginMessage pkMsg = LivePKProto.PKBeginMessage.parseFrom(data);
                        msgExt = new BarrageMsgExt.PkStartInfo().parseFromPB(pkMsg, getSentTime());
                        break;
                    }
                    case BarrageMsgType.B_MSG_TYPE_NEW_PK_END: {
                        LivePKProto.PKEndMessage pkMsg = LivePKProto.PKEndMessage.parseFrom(data);
                        msgExt = new BarrageMsgExt.PkEndInfo().parseFromPB(pkMsg);
                        break;
                    }
                }
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int compareTo(BarrageMsg another) {
        if (another == null) {
            return -1;
        } else {
            if (this.sentTime < another.sentTime) {
                return -1;
            } else if (this.sentTime > another.sentTime) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (this == o) {
            return true;
        }
        BarrageMsg that = (BarrageMsg) o;
        return sender == that.sender && senderMsgId == that.senderMsgId;
    }

    @Override
    public int hashCode() {
        int result = 17;
        int elementHash = (int) (sender ^ (sender >>> 32));
        result = 31 * result + elementHash;
        elementHash = (int) (senderMsgId ^ (senderMsgId >>> 32));
        result = 31 * result + elementHash;
        return result;
    }

    public interface MsgExt {
        ByteString toByteString();
    }

    public static class GiftMsgExt implements MsgExt {
        public int giftId;// 礼物id
        public String giftName;// 礼物名称
        public int giftCount;// 礼物个数
        public int zhuboAsset;// 主播当前的收益资产
        public long zhuboAssetTs;// 主播当前资产时间戳
        public long continueId; // 连送Id
        public String msgBody; // 弹幕消息内容
        public long avatarTimestamp; // 头像时间戳
        public String redEnvelopeId; // 红包id
        public String orderId;
        public String liveStreamUrl; //全局大礼物推流地址

        public String getGiftName() {
            return giftName;
        }

        public void setGiftName(String giftName) {
            this.giftName = giftName;
        }

        public int getGiftCount() {
            return giftCount;
        }

        public void setGiftCount(int giftCount) {
            this.giftCount = giftCount;
        }

        public int getZhuboAsset() {
            return zhuboAsset;
        }

        public void setZhuboAsset(int zhuboAsset) {
            this.zhuboAsset = zhuboAsset;
        }

        public long getZhuboAssetTs() {
            return zhuboAssetTs;
        }

        public void setZhuboAssetTs(long zhuboAssetTs) {
            this.zhuboAssetTs = zhuboAssetTs;
        }

        public long getContinueId() {
            return continueId;
        }

        public void setContinueId(long continueId) {
            this.continueId = continueId;
        }

        public String getMsgBody() {
            return msgBody;
        }

        public void setMsgBody(String msgBody) {
            this.msgBody = msgBody;
        }

        public String getRedEnvelopeId() {
            return redEnvelopeId;
        }

        public void setRedEnvelopeId(String redEnvelopeId) {
            this.redEnvelopeId = redEnvelopeId;
        }

        public String getLiveStreamUrl() {
            return liveStreamUrl;
        }

        public void setLiveStreamUrl(String liveStreamUrl) {
            this.liveStreamUrl = liveStreamUrl;
        }

        public long getAvatarTimestamp() {
            return avatarTimestamp;
        }

        public void setAvatarTimestamp(long avatarTimestamp) {
            this.avatarTimestamp = avatarTimestamp;
        }

        @Override
        public ByteString toByteString() {
            LiveMessageProto.GiftMessage.Builder builder = LiveMessageProto.GiftMessage.newBuilder();
            builder.setGiftCount(giftCount);
            builder.setGiftId(giftId);
            if (!TextUtils.isEmpty(giftName)) {
                builder.setGiftName(giftName);
            }
            if (!TextUtils.isEmpty(msgBody)) {
                builder.setMsgBody(msgBody);
            }
            return ByteString.copyFrom(builder.build().toByteArray());
        }

        @Override
        public String toString() {
            return "GiftMsgExt{" +
                    "giftId=" + giftId +
                    ", giftName='" + giftName + '\'' +
                    ", giftCount=" + giftCount +
                    ", zhuboAsset=" + zhuboAsset +
                    ", zhuboAssetTs=" + zhuboAssetTs +
                    ", continueId=" + continueId +
                    '}';
        }

        public static GiftMsgExt transformFromPB(LiveMessageProto.GiftMessage gift) {
            GiftMsgExt ext = new GiftMsgExt();
            if (gift != null) {
                ext.giftName = gift.getGiftName();
                ext.giftCount = gift.getGiftCount();
                ext.giftId = gift.getGiftId();
                ext.zhuboAsset = gift.getZhuboAsset();
                ext.zhuboAssetTs = gift.getZhuboAssetTimestamp();
                ext.continueId = gift.getContinueId();
                ext.msgBody = gift.getMsgBody();
                ext.avatarTimestamp = gift.getAvatarTimestamp();
                if (gift.hasOrderId()) {
                    ext.orderId = gift.getOrderId();
                }
                if (gift.hasLiveStreamUrl()) {
                    ext.liveStreamUrl = gift.getLiveStreamUrl();
                }
            }
            return ext;
        }
    }

    public static class LikeMsgExt implements MsgExt {
        // 1,2,3,4,5从1起步
        public int id; //表示赞消息的 星星外观
        public String bitmapPath;

        @Override
        public ByteString toByteString() {
            return ByteString.copyFrom(LiveMessageProto.LikeMessage.newBuilder().setId(id).build().toByteArray());
        }
    }

    //这种消息，客户端只是接收方， 不会发出
    public static class JoinRoomMsgExt implements MsgExt {
        public int viewerCount;

        public List<ViewerModel> viewerList = new ArrayList<>();

        @Override
        public ByteString toByteString() {
            return null;
        }
    }

    //这种消息，客户端只是接收方， 不会发出
    public static class LeaveRoomMsgExt implements MsgExt {
        public int viewerCount;

        public List<ViewerModel> viewerList = new ArrayList<>();

        @Override
        public ByteString toByteString() {
            return null;
        }
    }

    //这种消息，客户端只是接收方， 不会发出
    public static class ViewerChangeMsgExt implements MsgExt {
        public int viewerCount;

        public List<ViewerModel> viewerList = new ArrayList<>();

        @Override
        public ByteString toByteString() {
            return null;
        }
    }

    //这种消息，客户端只是接收方， 不会发出
    public static class LiveStartMsgExt implements MsgExt {
        @Override
        public ByteString toByteString() {
            return null;
        }
    }

    //这种消息，客户端只是接收方， 不会发出
    public static class LiveEndMsgExt implements MsgExt {
        public int viewerCount;

        @Override
        public ByteString toByteString() {
            return null;
        }
    }

    //这种消息，客户端只是接收方， 不会发出
    public static class ForbiddenMsgExt implements MsgExt {
        public long forbiddenUserId;

        public int operatorType;

        public String banNickname;

        @Override
        public ByteString toByteString() {
            return null;
        }

        /**
         * 获取禁言弹幕消息
         *
         * @param anchorId
         * @param uuid
         * @param msgType    消息类型，区分禁言和解禁
         * @param senderName 发消息人的名称
         * @return
         */
        public String getBanMessage(long anchorId, long uuid, int msgType, String senderName) {
            String message = "";
            if (TextUtils.isEmpty(banNickname))
                return message;
            if (senderName == null || senderName.equals(banNickname) || senderName.equals(com.base.global.GlobalData.app().getString(R.string.sys_msg)))
                senderName = "";
            String operator = "";
            //判断当前用户是否是主播
            if (anchorId == uuid) {
                if (operatorType == OPERATOR_TYPE_ADMIN) {
                    operator = com.base.global.GlobalData.app().getString(R.string.manager);
                } else if (operatorType == OPERATOR_TYPE_INSPECTOR) {
                    operator = com.base.global.GlobalData.app().getString(R.string.sys_manager);
                    senderName = "";
                } else if (operatorType == OPERATOR_TYPE_TOP1) {
                    operator = com.base.global.GlobalData.app().getString(R.string.top1);
                } else if (operatorType == OPERATOR_TYPE_OSS) {
                    operator = com.base.global.GlobalData.app().getString(R.string.sys_manager);
                    senderName = "";
                }
            }
            if (msgType == BarrageMsgType.B_MSG_TYPE_FORBIDDEN) {
                if (TextUtils.isEmpty(operator)) {
                    message = com.base.global.GlobalData.app().getString(R.string.ban_speaker_tips, banNickname);
                } else {
                    message = com.base.global.GlobalData.app().getString(R.string.ban_speaker_tips2, banNickname, operator, senderName);
                }
            } else if (msgType == BarrageMsgType.B_MSG_TYPE_CANCEL_FORBIDDEN) {
                if (TextUtils.isEmpty(operator)) {
                    message = com.base.global.GlobalData.app().getString(R.string.remove_ban_speaker_tips, banNickname);
                } else {
                    message = com.base.global.GlobalData.app().getString(R.string.remove_ban_speaker_tips2, banNickname, operator, senderName);
                }
            }
            return message;
        }
    }

    public static class GlobalMessageExt implements MsgExt {
        public List<SystemMsgModel> systemMessageList = new ArrayList<>();

        @Override
        public ByteString toByteString() {
            return null;
        }

        public List<BarrageMsg> getSysBarrageMsg(BarrageMsg parentMsg) {
            List<BarrageMsg> barrageMsgs = new ArrayList<>();
            if (systemMessageList.size() > 0 && parentMsg != null) {
                int i = 0;
                for (SystemMsgModel systemMessage : systemMessageList) {
                    if (System.currentTimeMillis() > systemMessage.getStartTime() && System.currentTimeMillis() < systemMessage.getEndTime()) {
                        BarrageMsg barrageMsg = new BarrageMsg();
                        barrageMsg.setBody(systemMessage.getContent());
                        barrageMsg.setSender(systemMessage.getFromUser());
                        barrageMsg.setRoomId(parentMsg.roomId);
                        barrageMsg.setSenderMsgId(parentMsg.getSentTime() + i);
                        barrageMsg.setSenderLevel(parentMsg.getSenderLevel());
                        barrageMsg.setSenderName(parentMsg.getSenderName());
                        barrageMsg.setMsgType(parentMsg.getMsgType());
                        barrageMsg.setSentTime(parentMsg.getSentTime());
                        barrageMsg.setIsFromPkOpponent(parentMsg.isFromPkOpponent());
                        barrageMsg.setGlobalRoomMsgExt(parentMsg.getGlobalRoomMsgExt());
                        barrageMsgs.add(barrageMsg);
                    }
                    i++;
                }
            }
            return barrageMsgs;
        }
    }

    public static class RoomMessageExt implements MsgExt {

        public List<SystemMsgModel> roomSystemMessageList = new ArrayList<>();

        @Override
        public ByteString toByteString() {
            return null;
        }

        public List<BarrageMsg> getRoomBarrageMsg(BarrageMsg parentMsg) {
            List<BarrageMsg> barrageMsgs = new ArrayList<>();
            if (parentMsg != null && roomSystemMessageList.size() > 0) {
                int i = 0;
                for (SystemMsgModel systemMessage : roomSystemMessageList) {
                    if (System.currentTimeMillis() > systemMessage.getStartTime() && System.currentTimeMillis() < systemMessage.getEndTime()) {
                        BarrageMsg barrageMsg = new BarrageMsg();
                        barrageMsg.setBody(systemMessage.getContent());
                        barrageMsg.setSender(systemMessage.getFromUser());
                        barrageMsg.setRoomId(parentMsg.roomId);
                        barrageMsg.setSenderMsgId(parentMsg.getSentTime() + i);
                        barrageMsg.setSenderLevel(parentMsg.getSenderLevel());
                        barrageMsg.setSenderName(parentMsg.getSenderName());
                        barrageMsg.setMsgType(parentMsg.getMsgType());
                        barrageMsg.setSentTime(parentMsg.getSentTime());
                        barrageMsg.setIsFromPkOpponent(parentMsg.isFromPkOpponent());
                        barrageMsgs.add(barrageMsg);

                    }
                    i++;
                }
            }
            return barrageMsgs;
        }
    }


    public static class ShoppingInfoMsgExt implements MsgExt {
        public List<LiveMessageProto.ShoppingInfo> shopList;

        public ShoppingInfoMsgExt(LiveMessageProto.PushShoppingInfoMsgExt message) {
            this.shopList = message.getShoppingInfoList();
        }

        @Override
        public ByteString toByteString() {
            return null;
        }
    }

    public static class ShopMessageExt implements MsgExt {
        public int shop_type;
        public String shop_content;
        public LiveMallProto.GoodsInfo goodsInfo;

        public ShopMessageExt(LiveMallProto.ShoppingMsgExt message) {
            this.shop_type = message.getMsgType();

            try {
                switch (shop_type) {
                    case 2:
                        this.goodsInfo = LiveMallProto.GoodsInfoList.parseFrom(message.getMsgContent()).getGoodsInfo(0);
                        break;
                    case 3:
                        this.shop_content = new String(message.getMsgContent().toByteArray(), "UTF-8");
                        break;
                    case 4:
                        this.goodsInfo = LiveMallProto.GoodsInfoList.parseFrom(message.getMsgContent()).getGoodsInfo(0);
                        break;
                    case 5:
                        //按钮下线
                        break;
                    default:
                        this.goodsInfo = LiveMallProto.GoodsInfo.parseFrom(message.getMsgContent());
                        break;

                }
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        @Override
        public ByteString toByteString() {
            return null;
        }
    }

    public static class SellMessageExt implements MsgExt {
        public boolean isShop;
        public boolean hideGift;

        public SellMessageExt(LiveMessageProto.TapToSellPushMsgExt message) {
            this.isShop = message.getIsShop();
            this.hideGift = message.getHideGift();
        }

        @Override
        public ByteString toByteString() {
            return null;
        }
    }

    public static class attachMessageExt implements MsgExt {
        public List<LiveMessageProto.WidgetMessageItem> widgetList;
        public List<LiveMessageProto.NewWidgetMessageItem> newWidgetList;

        public attachMessageExt(LiveMessageProto.WidgetMessage message) {
            this.widgetList = message.getMsgItemList();
            this.newWidgetList = message.getNewWidgetItemList();
        }

        @Override
        public ByteString toByteString() {
            return null;
        }
    }

    public static class AnchorMessageExt implements MsgExt {
        public String content;

        @Override
        public ByteString toByteString() {
            return null;
        }
    }

    public static class PkMessageExt implements MsgExt {
        public String roomId;
        public long zuid;
        public int myInitTicket;
        public int otherInitTicket;

        @Override
        public ByteString toByteString() {
            return null;
        }
    }

    public static class MsgRuleChangeMessageExt implements MsgExt {
        private MessageRule messageRule;

        public MessageRule getMessageRule() {
            return messageRule;
        }

        public MsgRuleChangeMessageExt(LiveMessageProto.MsgRuleChangeMessage msgRuleChangeMessage) {
            messageRule = new MessageRule(msgRuleChangeMessage.getMsgRule());
        }

        @Override
        public ByteString toByteString() {
            return null;
        }

        @Override
        public String toString() {
            return messageRule.toString();
        }
    }

    public static class KickMessageExt implements MsgExt {

        public static final int OPERATION_TYPE_THIS_ROOM = 0;
        public static final int OPERATION_TYPE_BLOCK = 1;

        private long zuid; // 主播id
        private String liveid; // 直播id
        private long operatorId; //操作人id
        private int operatorType; //操作人类型: 0:主播, 1:管理员, 2:榜一
        private long kickedId; //被踢用户id
        private int operationType; //操作类型: 0:本场拉黑，1:永久拉黑
        private String kickedNickname;

        public KickMessageExt(LiveMessageProto.KickMessage kickMessage) {
            this.zuid = kickMessage.getZuid();
            this.liveid = kickMessage.getLiveid();
            this.operatorId = kickMessage.getOpId();
            this.operatorType = kickMessage.getOpType();
            this.kickedId = kickMessage.getKickedId();
            this.operationType = kickMessage.getOperationType();
            this.kickedNickname = kickMessage.getKickedNickname();
        }

        public String buildUpSysMessage(long anchorId, long uuid) {
            String msg = null;
            if (!TextUtils.isEmpty(this.kickedNickname)) {
                if (anchorId == uuid) {
                    if (operatorType == OPERATOR_TYPE_ADMIN) {
                        msg = GlobalData.app().getString(R.string.admin_kick_viewer_barrage, this.kickedNickname);
                    } else if (operatorType == OPERATOR_TYPE_TOP1) {
                        msg = GlobalData.app().getString(R.string.top1_kick_viewer_barrage, this.kickedNickname);
                    } else {
                        msg = GlobalData.app().getString(R.string.kick_viewer_brrage, this.kickedNickname);
                    }
                } else if (uuid == kickedId) {
                    msg = GlobalData.app().getString(R.string.kick_viewer_brrage, this.kickedNickname);
                } else {
                    msg = GlobalData.app().getString(R.string.kick_viewer_brrage, this.kickedNickname);
                }
            }
            return msg;
        }

        @Override
        public ByteString toByteString() {
            return null;
        }

        public long getZuid() {
            return zuid;
        }

        public String getLiveid() {
            return liveid;
        }

        public long getOperatorId() {
            return operatorId;
        }

        public int getOperatorType() {
            return operatorType;
        }

        public long getKickedId() {
            return kickedId;
        }

        public int getOperationType() {
            return operationType;
        }

        @Override
        public String toString() {
            return "KickMessage zuid=" + zuid + " liveid=" + liveid + " operatorId=" + operatorId + " operatorType=" + operatorType + " kickedId=" + kickedId + " operationType=" + operationType + " kickedNickname=" + kickedNickname;
        }
    }


    /**
     * 进入房间　或者是等级升级的动画
     */
    public static class AnimMsgExt implements MsgExt {

        public static final int ENTER_ROOM_ANIMATION_TYPE = 0; //播放进入房间动画
        public static final int LEVEL_UPGREAD_ANIMATION_TYPE = 1; //等级升级动画

        public int animationEffect;
        public String animationContent;
        public int animationType;

        public AnimMsgExt(int animationEffect, String animationContent, int animationType) {
            this.animationContent = animationContent;
            this.animationEffect = animationEffect;
            this.animationType = animationType;
        }


        public AnimMsgExt(LiveMessageProto.RoomAnimationMessage roomAnimationMessage) {
            if (roomAnimationMessage != null) {
                animationEffect = roomAnimationMessage.getAnimationEffect();
                animationContent = roomAnimationMessage.getAnimationContent();
                animationType = roomAnimationMessage.getAnimationType();
            }
        }

        @Override
        public ByteString toByteString() {
            LiveMessageProto.RoomAnimationMessage roomAnimationMessage = LiveMessageProto.RoomAnimationMessage.newBuilder().setAnimationContent(animationContent)
                    .setAnimationEffect(animationEffect).setAnimationType(animationType).build();
            return roomAnimationMessage.toByteString();
        }
    }

    public static class UpgradeMessage implements MsgExt {
        public int userLevel;

        public UpgradeMessage(LiveMessageProto.UpgradeMessage upgradeMessage) {
            this.userLevel = upgradeMessage.getUserLevel();
        }

        @Override
        public ByteString toByteString() {
            return null;
        }
    }

    public static class WidgetClickMessage implements MsgExt {
        public String counter;
        public int widgetID;

        public WidgetClickMessage(LiveMessageProto.WidgetClickMessage upgradeMessage) {
            this.counter = upgradeMessage.getCounterValue();
            this.widgetID = upgradeMessage.getWidgetID();
        }

        @Override
        public ByteString toByteString() {
            return null;
        }
    }

    public static class RedEnvelopMsgExt implements MsgExt {
        public long userId;// 用户id
        public String roomId;// 房间id
        public String redEnvolopId;// 红包id
        public String msg;// 红包宣言
        public String nickName;//用户昵称
        public int level;//用户等级
        public long avatar;//头像
        public int gemCnt;
        public int type;//红包等级

        @Override
        public ByteString toByteString() {
            RedEnvelProto.NotifyRedEnvelop.Builder builder = RedEnvelProto.NotifyRedEnvelop.newBuilder();
            builder.setMsg(msg);
            builder.setRedEnvelopId(redEnvolopId);
            builder.setUserId(userId);
            builder.setRoomId(roomId);
            builder.setAvatar(avatar);
            builder.setLevel(level);
            builder.setNickname(nickName);
            builder.setGemCnt(gemCnt);
            builder.setEnvelopLevel(type);
            return ByteString.copyFrom(builder.build().toByteArray());
        }

        @Override
        public String toString() {
            return "RedEnvelopMsgExt{" +
                    "userId=" + userId +
                    ", roomId='" + roomId + '\'' +
                    ", redEnvolopId=" + redEnvolopId +
                    ", msg=" + msg +
                    ", nickName=" + nickName +
                    ", level=" + level +
                    ", avatar=" + avatar +
                    ", gemCnt=" + gemCnt +
                    '}';
        }

        public static RedEnvelopMsgExt transformFromPB(RedEnvelProto.NotifyRedEnvelop red) {
            RedEnvelopMsgExt ext = new RedEnvelopMsgExt();
            if (red != null) {
                ext.userId = red.getUserId();
                ext.roomId = red.getRoomId();
                ext.nickName = red.getNickname();
                ext.level = red.getLevel();
                ext.avatar = red.getAvatar();
                ext.msg = red.getMsg();
                ext.redEnvolopId = red.getRedEnvelopId();
                ext.gemCnt = red.getGemCnt();
                ext.type = red.getEnvelopLevel();
            }
            return ext;
        }
    }
}
