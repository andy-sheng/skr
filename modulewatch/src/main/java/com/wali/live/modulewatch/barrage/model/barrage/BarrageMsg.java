package com.wali.live.modulewatch.barrage.model.barrage;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.common.core.myinfo.MyUserInfo;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.log.MyLog;
import com.common.utils.U;
import com.google.protobuf.ByteString;
import com.wali.live.modulewatch.R;
import com.wali.live.modulewatch.watch.model.roominfo.MessageRule;
import com.wali.live.modulewatch.watch.model.roominfo.ViewerModel;
import com.wali.live.proto.LiveMessage.Language;
import com.wali.live.proto.LiveMessage.MedalConfigMessage;
import com.wali.live.proto.LiveMessage.Message;

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
    public final static int SYATEM_NOBEL = 800888;//贵族系统账号


    public static final int INNER_GLOBAL_MEDAL_TYPE = 100;
    public static final int INNER_GLOBAL_SCHEME_TYPE = 101;
    public static final int INNER_GLOBAL_SHARE_JOIN_ROME_TYPE = 400;
    public static final int INNER_GLOBAL_ADMIN_FLY = 500;//弹幕
    public static final int INNER_GLOBAL_PAY_HORN = 501;//喇叭
    public static final int INNER_GLOBAL_FLY_NOTICE = 502;//主播飘屏公告
    public static final int INNER_GLOBAL_VFAN = 600;//宠爱团简要信息

    public static BarrageMsg toBarrageMsg(Message msg) {
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
        if (msg.getGlobalRoomMsgExt() != null) {
            barrageMsg.setGlobalRoomMessageExt(GlobalRoomMessageExt.loadFromPB(msg.getGlobalRoomMsgExt()));
        }
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

        if (msg.hasMultiLanguage() && msg.getMultiLanguage().getLanguageList().size() > 0) {
            barrageMsg.setLocalizedMsg(msg.getMultiLanguage().getLanguageList());
        }

        String fromUserName = msg.getFromUserShowName(); //和from_user_nick_name的区别是不可能系统消息
        if (!TextUtils.isEmpty(fromUserName)) {
            barrageMsg.setSenderName(fromUserName);
        } else if (barrageMsg.getSenderName().equals(U.app().getString(R.string.sys_msg))) {
            barrageMsg.setSenderName(fromUserName);
        }
        if (msg.getFromUser() == SYATEM_NOBEL) {
            // 推荐上热门时会下发贵族客服账号的系统消息　服务端直接下发了uid 这里把uid解析展示相应昵称
            barrageMsg.setSenderName(U.app().getString(R.string.barrage_nobel_nickname));
        }

        if (msg.hasIsFromUserRedname() && msg.getIsFromUserRedname()) {
            barrageMsg.setRedName(msg.getIsFromUserRedname());
        }


        barrageMsg.setVipLevel(msg.getVipLevel());
        barrageMsg.setVipFrozen(msg.getVipDisable());
        barrageMsg.setVipHide(msg.getVipHidden());
        barrageMsg.setFromEffectId(msg.getSpecialEffectId());
        barrageMsg.setNobleLevel(msg.getNobleLevel());

        return barrageMsg;
    }

    public static final int ROOM_TYPE_NORMAL = 0;
    public static final int ROOM_TYPE_PK = 1;

    /**
     * 操作人类型，用于踢人、禁言等消息
     **/
    public static final int OPERATOR_TYPE_ADMIN = 0;       //管理员
    public static final int OPERATOR_TYPE_INSPECTOR = 1;   //巡查员
    public static final int OPERATOR_TYPE_TOP1 = 2;        //热门榜一
    public static final int OPERATOR_TYPE_OWNER = 3;       //房主
    public static final int OPERATOR_TYPE_OSS = 4;         //运营管理端后台

    private long sender;
    private String roomId;
    private long sentTime;
    private long senderMsgId;                              //发送方的消息，可以用来去重
    private int msgType;
    private String senderName;                             //发送者名称
    private int senderLevel;                               //发送者用户级别
    private String body;
    private long anchorId;
    private int certificationType;
    private int resendTimes;                               //用来客户端重发弹幕逻辑调整
    private MsgExt msgExt;
    private int roomType = ROOM_TYPE_NORMAL;
    private String originRoomId;                           //消息来源的roomId
    private boolean isFromPkOpponent = false;
    private String opponentRoomId;
    private long opponentAnchorId;
    private long toUserId;
    private boolean isRedName;                             //是否被社区红名，红名表示是不友好名单，这类用户不显示等级，并且灰色字体显示弹幕
    private GlobalRoomMessageExt globalRoomMessageExt;
    private int vipLevel;                                  //vip等级
    private boolean isVipFrozen;                           //vip是否被冻结
    private boolean isVipHide;                             //vip用户是否设置隐身
    private int nobleLevel;
    private int fromEffectId;                              //发送者的特效id
    private String localizedMsg;                           //本地化信息，解析MultiLanguage得到

    public BarrageMsg() {
        this.senderMsgId = System.currentTimeMillis();
    }

    public void appendCommonInfo() {
        MyUserInfo myUserInfo = MyUserInfoManager.getInstance().getMyUserInfo();
        setVipLevel(myUserInfo.getUserInfo().getVipLevel());
        setVipFrozen(myUserInfo.getUserInfo().getIsVipFrozen());
        setVipHide(myUserInfo.getUserInfo().getIsVipHide());
        setNobleLevel(myUserInfo.getUserInfo().getNobleLevel());
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
                ", globalRoomMessageExt=" + globalRoomMessageExt +
                ", vipLevel=" + vipLevel +
                ", isVipFrozen=" + isVipFrozen +
                ", isVipHide=" + isVipHide +
                ", nobleLevel=" + nobleLevel +
                ", fromEffectId=" + fromEffectId +
                ", localizedMsg='" + localizedMsg + '\'' +
                '}';
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

    @Nullable
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

    public void setOriginRoomId(String originRoomId) {
        this.originRoomId = originRoomId;
    }

    public int getRoomType() {
        return roomType;
    }

    public void setRoomType(int roomType) {
        this.roomType = roomType;
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

    public void setOpponentRoomId(String opponentRoomId) {
        this.opponentRoomId = opponentRoomId;
    }

    public long getOpponentAnchorId() {
        return opponentAnchorId;
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

    public GlobalRoomMessageExt getGlobalRoomMessageExt() {
        return globalRoomMessageExt;
    }

    public void setGlobalRoomMessageExt(GlobalRoomMessageExt globalRoomMessageExt) {
        this.globalRoomMessageExt = globalRoomMessageExt;
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

    public int getNobleLevel() {
        return nobleLevel;
    }

    public void setNobleLevel(int nobleLevel) {
        this.nobleLevel = nobleLevel;
    }

    public int getFromEffectId() {
        return fromEffectId;
    }

    public void setFromEffectId(int fromEffectId) {
        this.fromEffectId = fromEffectId;
    }

    @NonNull
    public String getLocalizedMsg() {
        if (TextUtils.isEmpty(localizedMsg)) {
            return "";
        }
        return localizedMsg;
    }

    private interface LanguageCode {
        int SIMPLIFIED_CHINESE = 0;
        int ENGLISH = 1;
        int TRADITIONAL_CHINESE = 2;
    }

    private void setLocalizedMsg(@NonNull List<Language> languageList) {
        // todo 多语言暂时停掉
//        int desiredLanguage = LocaleUtil.getSelectedLanguageIndex();
//        switch (desiredLanguage) {
//            case LocaleUtil.INDEX_SIMPLIFIED_CHINESE:
//                desiredLanguage = LanguageCode.SIMPLIFIED_CHINESE;
//                break;
//            case LocaleUtil.INDEX_TRADITIONAL_CHINESE:
//                desiredLanguage = LanguageCode.TRADITIONAL_CHINESE;
//                break;
//            case LocaleUtil.INDEX_ENGLISH:
//                desiredLanguage = LanguageCode.ENGLISH;
//                break;
//            default:
//                desiredLanguage = LanguageCode.ENGLISH;
//                break;
//        }
//        String defaultText = null;
//        boolean isObtainDefaultText = false;
//        boolean isObtainSpecificText = false;
//        for (com.wali.live.proto.LiveMessage.Language language : languageList) {
//            if (language.getLanguageCode() == LanguageCode.ENGLISH) {
//                defaultText = language.getText();
//                isObtainDefaultText = true;
//                if (isObtainSpecificText) {
//                    break;
//                }
//            }
//            if (desiredLanguage == language.getLanguageCode()) {
//                localizedMsg = language.getText();
//                isObtainSpecificText = true;
//                if (isObtainDefaultText) {
//                    break;
//                }
//            }
//        }
//        if (TextUtils.isEmpty(localizedMsg) && !TextUtils.isEmpty(defaultText)) {
//            localizedMsg = defaultText;
//        }
    }

    /*
     * 引入clean模式后这个方法要慢慢被废弃掉，因为所有设计到pb的数据操作都应放到data层
     * */
    private void setMsgExt(byte[] data, int msgType) {
        if (data != null) {
            try {
                switch (msgType) {
                    case BarrageMsgType.B_MSG_TYPE_TEXT: {
                        if (data.length <= 0) {
                            break;
                        }
                        com.wali.live.proto.LiveMessage.
                                RoomTxtMessageExt txtMessageExt = com.wali.live.proto.LiveMessage.RoomTxtMessageExt.parseFrom(data);
                        RoomTxtMessageExt ext = new RoomTxtMessageExt();
                        ext.setType(txtMessageExt.getType());
                        ext.setExt(ByteString.copyFrom(txtMessageExt.getExt().toByteArray()));
                        msgExt = ext;
                    }
                    break;
                    case BarrageMsgType.B_MSG_TYPE_CANCEL_FORBIDDEN:
                    case BarrageMsgType.B_MSG_TYPE_FORBIDDEN: {
                        ForbiddenMsgExt ext = new ForbiddenMsgExt();
                        com.wali.live.proto.LiveMessage.ForbiddenMessage f = com.wali.live.proto.LiveMessage.ForbiddenMessage.parseFrom(data);
                        ext.forbiddenUserId = f.getUserId();
                        ext.operatorType = f.getOpType();
                        ext.banNickname = f.getBanNickname();
                        msgExt = ext;
                    }
                    break;
                    case BarrageMsgType.B_MSG_TYPE_LIKE: {
                        LikeMsgExt ext = new LikeMsgExt();
                        com.wali.live.proto.LiveMessage.LikeMessage like = com.wali.live.proto.LiveMessage.LikeMessage.parseFrom(data);
                        ext.id = like.getId();
                        msgExt = ext;
                    }
                    break;
//                    case BarrageMsgType .B_MSG_TYPE_PK_BEGIN: {
//                        PkMessageExt ext = new PkMessageExt();
//                        com.wali.live.proto.LiveMessage.PKBeginMessage pkRoomInfo = com.wali.live.proto.LiveMessage.PKBeginMessage.parseFrom(data);
//                        ext.roomId = pkRoomInfo.getOtherPKInfo().getLiveId();
//                        ext.zuid = pkRoomInfo.getOtherPKInfo().getUuid();
//                        ext.myInitTicket = pkRoomInfo.getThisPKInfo().getPkInitTicket();
//                        ext.otherInitTicket = pkRoomInfo.getOtherPKInfo().getPkInitTicket();
//                        msgExt = ext;
//                    }
//                    break;
//                    case BarrageMsgType.B_MSG_TYPE_PK_END: {
//                        PkMessageExt ext = new PkMessageExt();
//                        com.wali.live.proto.LiveMessage.PKEndMessage pkRoomInfo = com.wali.live.proto.LiveMessage.PKEndMessage.parseFrom(data);
//                        ext.roomId = pkRoomInfo.getOtherPKInfo().getLiveId();
//                        ext.zuid = pkRoomInfo.getOtherPKInfo().getUuid();
//                        msgExt = ext;
//                    }
//                    break;
                    case BarrageMsgType.B_MSG_TYPE_LINE_MIC_BEGIN: {
                        com.wali.live.proto.LiveMic.MicBeginMessage micMsg = com.wali.live.proto.LiveMic.MicBeginMessage.parseFrom(data);
                        msgExt = new BarrageMsgExt.MicBeginInfo().parseFromPB(micMsg);
                    }
                    break;
                    case BarrageMsgType.B_MSG_TYPE_LINE_MIC_END: {
                        com.wali.live.proto.LiveMic.MicEndMessage micMsg = com.wali.live.proto.LiveMic.MicEndMessage.parseFrom(data);
                        msgExt = new BarrageMsgExt.MicEndInfo().parseFromPB(micMsg);

                    }
                    break;
                    case BarrageMsgType.B_MSG_TYPE_JOIN: {
                        JoinRoomMsgExt ext = new JoinRoomMsgExt();
                        com.wali.live.proto.LiveMessage.JoinRoomMessage join = com.wali.live.proto.LiveMessage.JoinRoomMessage.parseFrom(data);
                        {
                            List<ViewerModel> temp = new ArrayList<>();
                            List<com.wali.live.proto.LiveCommon.Viewer> list = join.getViewersList();
                            if (list != null) {
                                for (com.wali.live.proto.LiveCommon.Viewer v : list) {
                                    ViewerModel viewer = new ViewerModel(v);
                                    temp.add(viewer);
                                }
                            }
                            ext.viewerList = temp;
                        }
                        ext.viewerCount = join.getViewerCount();
                        ext.type = join.getType();
                        ext.showVipEnterRoomEffect = join.getShowSpecialEffect();
                        msgExt = ext;
                    }
                    break;
                    case BarrageMsgType.B_MSG_TYPE_LEAVE: {
                        LeaveRoomMsgExt ext = new LeaveRoomMsgExt();
                        com.wali.live.proto.LiveMessage.LeaveRoomMessage leave = com.wali.live.proto.LiveMessage.LeaveRoomMessage.parseFrom(data);
                        {
                            List<ViewerModel> temp = new ArrayList<>();
                            List<com.wali.live.proto.LiveCommon.Viewer> list = leave.getViewersList();
                            if (list != null) {
                                for (com.wali.live.proto.LiveCommon.Viewer v : list) {
                                    ViewerModel viewer = new ViewerModel(v);
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
                        com.wali.live.proto.LiveMessage.ViewerMessage viewerChange = com.wali.live.proto.LiveMessage.ViewerMessage.parseFrom(data);
                        {
                            List<ViewerModel> temp = new ArrayList<>();
                            List<com.wali.live.proto.LiveCommon.Viewer> list = viewerChange.getViewersList();
                            if (list != null) {
                                for (com.wali.live.proto.LiveCommon.Viewer v : list) {
                                    ViewerModel viewer = new ViewerModel(v);
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
                        com.wali.live.proto.LiveMessage.LiveEndMessage liveEndMsgExt = com.wali.live.proto.LiveMessage.LiveEndMessage.parseFrom(data);
                        ext.viewerCount = liveEndMsgExt.getHisViewerCnt();
                        ext.duration = liveEndMsgExt.getDuration();
                        ext.hisBeginLiveCnt = liveEndMsgExt.getHisBeginLiveCnt();
                        ext.newFollowerCnt = liveEndMsgExt.getNewFollowerCnt();
                        msgExt = ext;
                    }
                    break;
                    case BarrageMsgType.B_MSG_TYPE_GLOBAL_SYS_MSG: {
                        GlobalMessageExt globalMessageExt = new GlobalMessageExt();
                        com.wali.live.proto.LiveMessage.GlobalSystemMessageBox globalSystemMessageBox = com.wali.live.proto.LiveMessage.GlobalSystemMessageBox.parseFrom(data);
                        List<SystemMsgModel> temp = new ArrayList<>();
                        List<com.wali.live.proto.LiveMessage.GlobalSystemMessage> globalSystemMessages = globalSystemMessageBox.getGlobalSystemMessageList();
                        if (globalSystemMessages != null) {
                            for (com.wali.live.proto.LiveMessage.GlobalSystemMessage globalSystemMessage : globalSystemMessages) {
                                for (com.wali.live.proto.LiveMessage.SystemMessage sm : globalSystemMessage.getSystemMessageList()) {
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
                        com.wali.live.proto.LiveMessage.RoomSystemMessage roomSystemMessage = com.wali.live.proto.LiveMessage.RoomSystemMessage.parseFrom(data);
                        List<com.wali.live.proto.LiveMessage.SystemMessage> roomSystemMessageList = roomSystemMessage.getSystemMessageList();
                        List<SystemMsgModel> temp = new ArrayList<>();
                        if (roomSystemMessageList != null) {
                            for (com.wali.live.proto.LiveMessage.SystemMessage sm : roomSystemMessageList) {
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
                    case BarrageMsgType.B_MSG_TYPE_ANNOUNCEMENT: {
                        RoomMessageExt roomMessageExt = new RoomMessageExt();
                        com.wali.live.proto.LiveMessage.RoomSystemMessage roomSystemMessage = com.wali.live.proto.LiveMessage.RoomSystemMessage.parseFrom(data);
                        List<com.wali.live.proto.LiveMessage.SystemMessage> roomSystemMessageList = roomSystemMessage.getSystemMessageList();
                        List<SystemMsgModel> temp = new ArrayList<>();
                        if (roomSystemMessageList != null) {
                            for (com.wali.live.proto.LiveMessage.SystemMessage sm : roomSystemMessageList) {
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
                        com.wali.live.proto.LiveMessage.AnchorMessage anchorMessage = com.wali.live.proto.LiveMessage.AnchorMessage.parseFrom(data);
                        if (anchorMessage != null && !TextUtils.isEmpty(anchorMessage.getContent())) {
                            anchorMessageExt.content = anchorMessage.getContent();
                        }
                        msgExt = anchorMessageExt;
                    }
                    break;
                    case BarrageMsgType.B_MSG_TYPE_GIFT: {
                        com.wali.live.proto.LiveMessage.GiftMessage gift = com.wali.live.proto.LiveMessage.GiftMessage.parseFrom(data);
                        msgExt = GiftMsgExt.transformFromPB(gift);
                    }
                    break;
                    case BarrageMsgType.B_MSG_TYPE_PAY_BARRAGE: {
                        com.wali.live.proto.LiveMessage.GiftMessage gift = com.wali.live.proto.LiveMessage.GiftMessage.parseFrom(data);
                        msgExt = GiftMsgExt.transformFromPB(gift);
                    }
                    break;
                    case BarrageMsgType.B_MSG_TYPE_RED_ENVELOPE: {
                        com.wali.live.proto.RedEnvelope.NotifyRedEnvelop redEnvelop = com.wali.live.proto.RedEnvelope.NotifyRedEnvelop.parseFrom(data);
                        msgExt = RedEnvelopMsgExt.transformFromPB(redEnvelop);
                    }
                    break;
                    case BarrageMsgType.B_MSG_TYPE_LIGHT_UP_GIFT: {
                        com.wali.live.proto.LiveMessage.GiftMessage gift = com.wali.live.proto.LiveMessage.GiftMessage.parseFrom(data);
                        msgExt = GiftMsgExt.transformFromPB(gift);
                    }
                    break;
                    case BarrageMsgType.B_MSG_TYPE_ROOM_BACKGROUND_GIFT: {
                        com.wali.live.proto.LiveMessage.GiftMessage gift = com.wali.live.proto.LiveMessage.GiftMessage.parseFrom(data);
                        msgExt = GiftMsgExt.transformFromPB(gift);
                    }
                    break;
                    case BarrageMsgType.B_MSG_TYPE_GLABAL_MSG: {
                        com.wali.live.proto.LiveMessage.GiftMessage gift = com.wali.live.proto.LiveMessage.GiftMessage.parseFrom(data);
                        msgExt = GiftMsgExt.transformFromPB(gift);
                    }
                    break;
                    case BarrageMsgType.B_MSG_TYPE_FREQUENCY_CONTROL: {
                        com.wali.live.proto.LiveMessage.MsgRuleChangeMessage msgRul = com.wali.live.proto.LiveMessage.MsgRuleChangeMessage.parseFrom(data);
                        msgExt = new MsgRuleChangeMessageExt(msgRul);
                    }
                    break;
                    case BarrageMsgType.B_MSG_TYPE_KICK_VIEWER_BARRAGE: {
                        com.wali.live.proto.LiveMessage.KickMessage kickMessage = com.wali.live.proto.LiveMessage.KickMessage.parseFrom(data);
                        msgExt = new KickMessageExt(kickMessage);
                    }
                    break;

                    case BarrageMsgType.B_MSG_TYPE_TAP_TP_SELL: {
                        com.wali.live.proto.LiveMessage.TapToSellPushMsgExt sellMessage = com.wali.live.proto.LiveMessage.TapToSellPushMsgExt.parseFrom(data);
                        msgExt = new SellMessageExt(sellMessage);
                    }
                    break;

                    case BarrageMsgType.B_MSG_TYPE_ATTACHMENT: {
                        com.wali.live.proto.LiveMessage.WidgetMessage attachMessage = com.wali.live.proto.LiveMessage.WidgetMessage.parseFrom(data);
                        msgExt = new attachMessageExt(attachMessage);
                    }
                    break;
                    case BarrageMsgType.B_MSG_TYPE_ANIM: {
                        com.wali.live.proto.LiveMessage.RoomAnimationMessage animationMessage = com.wali.live.proto.LiveMessage.RoomAnimationMessage.parseFrom(data);
                        msgExt = new AnimMsgExt(animationMessage);
                    }
                    break;
                    case BarrageMsgType.B_MSG_TYPE_SELL: {
                        com.wali.live.proto.LiveMessage.PushShoppingInfoMsgExt shoppingInfoMsgExt = com.wali.live.proto.LiveMessage.PushShoppingInfoMsgExt.parseFrom(data);
                        msgExt = new ShoppingInfoMsgExt(shoppingInfoMsgExt);
                    }
                    break;
                    case BarrageMsgType.B_MSG_TYPE_ADD_SHOP: {
                        com.wali.live.proto.LiveMall.ShoppingMsgExt shopMessage = com.wali.live.proto.LiveMall.ShoppingMsgExt.parseFrom(data);
                        msgExt = new ShopMessageExt(shopMessage);
                        break;
                    }
                    case BarrageMsgType.B_MSG_TYPE_LEVEL_UPGRADE_SYS_MSG: {
                        com.wali.live.proto.LiveMessage.UpgradeMessage upgradeMessage = com.wali.live.proto.LiveMessage.UpgradeMessage.parseFrom(data);
                        msgExt = new UpgradeMessage(upgradeMessage);
                    }
                    break;
                    case BarrageMsgType.B_MSG_TYPE_ATTACHMENT_COUNTER: {
                        com.wali.live.proto.LiveMessage.WidgetClickMessage clickMessage = com.wali.live.proto.LiveMessage.WidgetClickMessage.parseFrom(data);
                        msgExt = new WidgetClickMessage(clickMessage);
                    }
                    break;
                    case BarrageMsgType.B_MSG_TYPE_RED_NAME_STATUES: {
                        com.wali.live.proto.LiveMessage.RedNameStatus redNameStatus = com.wali.live.proto.LiveMessage.RedNameStatus.parseFrom(data);
                    }
                    break;
                    case BarrageMsgType.B_MSG_TYPE_LIVE_INFO_CHANGE: {
                        com.wali.live.proto.LiveMessage.LiveInfoChangeMsg liveInfoChangeMsg = com.wali.live.proto.LiveMessage.LiveInfoChangeMsg.parseFrom(data);
                        msgExt = new LiveInfoChangeMsgExt(liveInfoChangeMsg);
                    }
                    break;
                    case BarrageMsgType.B_MSG_TYPE_VIP_LEVEL_CHANGED: {
                        msgExt = VipLevelChangedExt.newInstance(com.wali.live.proto.Vip.VipLevelChangeMsg.parseFrom(data));
                    }
                    break;

                    case BarrageMsgType.B_MSG_TYPE_NEW_PK_SYSTEM: {
                        com.wali.live.proto.LivePK.PKSysMsg pkSysMsg = com.wali.live.proto.LivePK.PKSysMsg.parseFrom(data);
                        msgExt = new BarrageMsgExt.PKSysMsg(pkSysMsg);
                    }
                    break;
                    case BarrageMsgType.B_MSG_TYPE_NEW_PK_END: {
                        com.wali.live.proto.LivePK.PKEndMessage pkMsg = com.wali.live.proto.LivePK.PKEndMessage.parseFrom(data);
                        msgExt = new BarrageMsgExt.PkEndInfo().parseFromPB(pkMsg);
                    }
                    break;
                    case BarrageMsgType.B_MSG_TYPE_NEW_PK_START: {
                        com.wali.live.proto.LivePK.PKBeginMessage pkBeginMessage = com.wali.live.proto.LivePK.PKBeginMessage.parseFrom(data);
                        msgExt = new BarrageMsgExt.PkStartInfo().parseFromPB(pkBeginMessage, getSentTime());
                    }
                    break;
                    case BarrageMsgType.B_MSG_TYPE_NEW_PK_SCORE: {
                        com.wali.live.proto.LivePK.PKScoreChangeMsg pkScoreChangeMsg = com.wali.live.proto.LivePK.PKScoreChangeMsg.parseFrom(data);
                        msgExt = new BarrageMsgExt.PkScoreInfo().parseFromPB(pkScoreChangeMsg);
                    }
                    break;
                    case BarrageMsgType.B_MSG_TYPE_QUESTION:
//                        LiveSummitProto.ContestQuestionMsg contestQuestionMsg = LiveSummitProto.ContestQuestionMsg.parseFrom(data);
//                        msgExt = new ContestQuestionMsgExt(contestQuestionMsg);
                        break;
                    case BarrageMsgType.B_MSG_TYPE_ANSWER:
//                        LiveSummitProto.ContestAnswerMsg contestAnswerMsg = LiveSummitProto.ContestAnswerMsg.parseFrom(data);
//                        msgExt = new ContestAnswerMsgExt(contestAnswerMsg);
                        break;
                    case BarrageMsgType.B_MSG_TYPE_RADIO_ROOMMSG:
                        com.wali.live.proto.RadioSignal.RoomMsg roomMsg = com.wali.live.proto.RadioSignal.RoomMsg.parseFrom(data);
                        msgExt = new RadioRoomMsg(roomMsg);
                        break;
                    case BarrageMsgType.B_MSG_TYPE_RADIO_PUSH:
                        com.wali.live.proto.RadioSignal.DiantaiSignalPush diantaiSignalPush = com.wali.live.proto.RadioSignal.DiantaiSignalPush.parseFrom(data);
                        msgExt = new RadioSignalPush(diantaiSignalPush);
                        break;
                    // todo 先不做
//                    case BarrageMsgType.B_MSG_TYPE_OPEN_TURN_TABLE:
//                        com.wali.live.proto.BigTurnTable.TurntablePush turntablePush = com.wali.live.proto.BigTurnTable.TurntablePush.parseFrom(data);
//                        msgExt = new BarrageMsgExt.TurnTableMessageExt(turntablePush);
//                        break;
                    case BarrageMsgType.B_MSG_TYPE_DISCOUNT_GIFT_EVENT:
                        com.wali.live.proto.LiveMessage.AnchorDiscountAging anchorDiscountAging = com.wali.live.proto.LiveMessage.AnchorDiscountAging.parseFrom(data);
                        if (anchorDiscountAging != null) {
                            msgExt = new AnchorDiscountAgingExt(anchorDiscountAging.getLeftTime());
                        }
                        break;
                }
            } catch (Exception e) {
                MyLog.e(e);
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
        /**
         * 如果只是从服务器接收此Ext，简单返回null就可以了<br>
         * 如果要把这个Ext发送给服务器，则必须返回有意义的值
         *
         * @return
         */
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
        public boolean isPrivilegeGift;
        public int batch_count;//批量送的礼物的个数
        public int popularity;// 人气值
        public long popularityTs;// 人气值时间戳
        public int addPopularity;// 增加的人气值

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

        public boolean isPrivilegeGift() {
            return isPrivilegeGift;
        }

        public void setPrivilegeGift(boolean privilegeGift) {
            isPrivilegeGift = privilegeGift;
        }

        public int getBatch_count() {
            return batch_count;
        }

        public void setBatch_count(int batch_count) {
            this.batch_count = batch_count;
        }

        @Override
        public ByteString toByteString() {
            com.wali.live.proto.LiveMessage.GiftMessage.Builder builder = new com.wali.live.proto.LiveMessage.GiftMessage.Builder();
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
                    ", isPrivilegeGift" + isPrivilegeGift +
                    '}';
        }

        public static GiftMsgExt transformFromPB(com.wali.live.proto.LiveMessage.GiftMessage gift) {
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
                ext.setBatch_count(gift.getBatchCount());
                if (gift.hasOrderId()) {
                    ext.orderId = gift.getOrderId();
                }
                if (gift.hasLiveStreamUrl()) {
                    ext.liveStreamUrl = gift.getLiveStreamUrl();
                }
                ext.popularity = gift.getPopularity();
                ext.popularityTs = gift.getPopularityTimestamp();
                ext.addPopularity = gift.getIncrPopularity();
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
            return ByteString.copyFrom(new com.wali.live.proto.LiveMessage.LikeMessage.Builder().setId(id).build().toByteArray());
        }
    }

    //这种消息，客户端只是接收方， 不会发出
    public static class JoinRoomMsgExt implements MsgExt {
        public static final int TYPE_DEFAULT = 0;
        public static final int TYPE_NEARBY = 1;
        public int viewerCount;
        public int type; // //0：默认 1：通过附近频道进入
        public boolean showVipEnterRoomEffect;

        public List<ViewerModel> viewerList = new ArrayList<>();

        @Override
        public ByteString toByteString() {
            return null;
        }
    }

    public static class RoomTxtMessageExt implements MsgExt {
        public final static int MSG_TYPE_NORMAL = 1;//普通弹幕
        public final static int MSG_SMART_NORMAL = 2;//智能弹幕
        public final static int MSG_TYPE_MANAGER = 3;//管理员弹幕
        public final static int MSG_TYPE_ADMIN_FLY = 4;//飘屏弹幕
        public final static int MSG_TYPE_PAY_HORN = 5;//大喇叭弹幕
        public final static int MSG_TYPE_VFANS = 6;//宠爱团简要信息
        public final static int MSG_TYPE_ANNOUNCEMENT = 7;//主播公告信息

        public int type;
        public ByteString ext;

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public ByteString getExt() {
            return ext;
        }

        public void setExt(ByteString ext) {
            this.ext = ext;
        }

        @Override
        public ByteString toByteString() {
            com.wali.live.proto.LiveMessage.RoomTxtMessageExt.Builder builder = new com.wali.live.proto.LiveMessage.RoomTxtMessageExt.Builder();
            builder.setType(type);
            if (ext != null) {
                builder.setExt(okio.ByteString.of(ext.toByteArray()));
            }
            return ByteString.copyFrom(builder.build().toByteArray());
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
        public long hisBeginLiveCnt;//历史开播次数
        public long duration;//开播时长（ms）
        public long newFollowerCnt;//新增关注数

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
            if (senderName == null || senderName.equals(banNickname) || senderName.equals(U.app().getString(R.string.sys_msg)))
                senderName = "";
            String operator = "";
            //判断当前用户是否是主播
            if (anchorId == uuid) {
                if (operatorType == OPERATOR_TYPE_ADMIN) {
                    operator = U.app().getString(R.string.manager);
                } else if (operatorType == OPERATOR_TYPE_INSPECTOR) {
                    operator = U.app().getString(R.string.sys_manager);
                    senderName = "";
                } else if (operatorType == OPERATOR_TYPE_TOP1) {
                    operator = U.app().getString(R.string.top1);
                } else if (operatorType == OPERATOR_TYPE_OSS) {
                    operator = U.app().getString(R.string.sys_manager);
                    senderName = "";
                }
            }
            if (msgType == BarrageMsgType.B_MSG_TYPE_FORBIDDEN) {
                if (TextUtils.isEmpty(operator)) {
                    message = U.app().getString(R.string.ban_speaker_tips, banNickname);
                } else {
                    message = U.app().getString(R.string.ban_speaker_tips2, banNickname, operator);
                }
            } else if (msgType == BarrageMsgType.B_MSG_TYPE_CANCEL_FORBIDDEN) {
                if (TextUtils.isEmpty(operator)) {
                    message = U.app().getString(R.string.remove_ban_speaker_tips, banNickname);
                } else {
                    message = U.app().getString(R.string.remove_ban_speaker_tips2, banNickname, operator);
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
                        barrageMsg.setGlobalRoomMessageExt(parentMsg.getGlobalRoomMessageExt());
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
        public List<com.wali.live.proto.LiveMessage.ShoppingInfo> shopList;

        public ShoppingInfoMsgExt(com.wali.live.proto.LiveMessage.PushShoppingInfoMsgExt message) {
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
        public com.wali.live.proto.LiveMall.GoodsInfo goodsInfo;

        public ShopMessageExt(com.wali.live.proto.LiveMall.ShoppingMsgExt message) {
            this.shop_type = message.getMsgType();

            try {
                switch (shop_type) {
                    case 2:

                        this.goodsInfo = com.wali.live.proto.LiveMall.GoodsInfoList.parseFrom(message.getMsgContent().toByteArray()).getGoodsInfoList().get(0);
                        break;
                    case 3:
                        this.shop_content = new String(message.getMsgContent().toByteArray(), "UTF-8");
                        break;
                    case 4:
                        this.goodsInfo = com.wali.live.proto.LiveMall.GoodsInfoList.parseFrom(message.getMsgContent().toByteArray()).getGoodsInfoList().get(0);
                        break;
                    case 5:
                        //按钮下线
                        break;
                    default:
                        this.goodsInfo = com.wali.live.proto.LiveMall.GoodsInfo.parseFrom(message.getMsgContent().toByteArray());
                        break;

                }
            } catch (Exception e) {
                MyLog.e(e);
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

        public SellMessageExt(com.wali.live.proto.LiveMessage.TapToSellPushMsgExt message) {
            this.isShop = message.getIsShop();
            this.hideGift = message.getHideGift();
        }

        @Override
        public ByteString toByteString() {
            return null;
        }
    }

    public static class attachMessageExt implements MsgExt {
        public List<com.wali.live.proto.LiveMessage.WidgetMessageItem> widgetList;
        public List<com.wali.live.proto.LiveMessage.NewWidgetMessageItem> newWidgetList;

        public attachMessageExt(com.wali.live.proto.LiveMessage.WidgetMessage message) {
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

        public MsgRuleChangeMessageExt(com.wali.live.proto.LiveMessage.MsgRuleChangeMessage msgRuleChangeMessage) {
            messageRule = new MessageRule(msgRuleChangeMessage.getMsgRule());
        }

        public MessageRule getMessageRule() {
            return messageRule;
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

        public KickMessageExt(com.wali.live.proto.LiveMessage.KickMessage kickMessage) {
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
                        msg = U.app().getString(R.string.admin_kick_viewer_barrage, this.kickedNickname);
                    } else if (operatorType == OPERATOR_TYPE_TOP1) {
                        msg = U.app().getString(R.string.top1_kick_viewer_barrage, this.kickedNickname);
                    } else {
                        msg = U.app().getString(R.string.kick_viewer_brrage, this.kickedNickname);
                    }
                } else if (uuid == kickedId) {
                    msg = U.app().getString(R.string.kick_viewer_brrage, this.kickedNickname);
                } else {
                    msg = U.app().getString(R.string.kick_viewer_brrage, this.kickedNickname);
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


        public AnimMsgExt(com.wali.live.proto.LiveMessage.RoomAnimationMessage roomAnimationMessage) {
            if (roomAnimationMessage != null) {
                animationEffect = roomAnimationMessage.getAnimationEffect();
                animationContent = roomAnimationMessage.getAnimationContent();
                animationType = roomAnimationMessage.getAnimationType();
            }
        }

        @Override
        public ByteString toByteString() {
            com.wali.live.proto.LiveMessage.RoomAnimationMessage roomAnimationMessage = new com.wali.live.proto.LiveMessage.RoomAnimationMessage.Builder().setAnimationContent(animationContent)
                    .setAnimationEffect(animationEffect).setAnimationType(animationType).build();
            return ByteString.copyFrom(roomAnimationMessage.toByteArray());
        }
    }

    public static class UpgradeMessage implements MsgExt {
        public int userLevel;

        public UpgradeMessage(com.wali.live.proto.LiveMessage.UpgradeMessage upgradeMessage) {
            this.userLevel = upgradeMessage.getUserLevel();
        }

        @Override
        public ByteString toByteString() {
            return null;
        }
    }

    public static class RadioRoomMsg implements MsgExt {
        public int type;

        String roomId = "";

        long fromZuId;

        long toZuId;

        int action;// 1:apply push, 2: invite push  3: approve push  4: accept push  5: quit push  6: kick push 7: status update 8: mute 9: unmute 10: cancel apply 11:online status

        public RadioRoomMsg(com.wali.live.proto.RadioSignal.RoomMsg roomMsg) {
            this.type = roomMsg.getInnerType();
            try {
                com.wali.live.proto.RadioSignal.DiantaiRoomMsg diantaiRoomMsg = com.wali.live.proto.RadioSignal.DiantaiRoomMsg.parseFrom(roomMsg.getExtMsg().toByteArray());
                this.roomId = diantaiRoomMsg.getRoomId();
                this.action = diantaiRoomMsg.getAction();
                fromZuId = diantaiRoomMsg.getFromZuid();
                toZuId = diantaiRoomMsg.getToZuid();
            } catch (Exception e) {
                MyLog.e(e);
            }
        }

        public long getToZuId() {
            return toZuId;
        }

        public int getAction() {
            return action;
        }

        public int getType() {
            return type;
        }

        public String getRoomId() {
            return roomId;
        }

        @Override
        public ByteString toByteString() {
            return null;
        }
    }

    public static class RadioSignalPush implements MsgExt {
        int action = -1; // 1:apply push, 2: invite push  3: approve push  4: accept push  5: quit push  6: kick push  7: status update 8: mute 9: unmute 10: cancel apply 11:online status
        int diantai_id; //电台id
        String roomId; //直播房间id
        long from_zuid;
        long to_zuid = 5;
        com.wali.live.proto.RadioSignal.DiantaiUser members;   //电台成员信息，包含自己
        int sync_ts = 7; //服务器时间, 单位:秒

        public RadioSignalPush(com.wali.live.proto.RadioSignal.DiantaiSignalPush diantaiSignalPush) {
            action = diantaiSignalPush.getAction();
            roomId = diantaiSignalPush.getRoomId();
            to_zuid = diantaiSignalPush.getToZuid();
            from_zuid = diantaiSignalPush.getFromZuid();
            diantai_id = diantaiSignalPush.getDiantaiId();
        }

        public long getTo_zuid() {
            return to_zuid;
        }

        public String getRoomId() {
            return roomId;
        }

        public int getAction() {
            return action;
        }

        @Override
        public ByteString toByteString() {
            return null;
        }
    }

    public static class WidgetClickMessage implements MsgExt {
        public String counter;
        public int widgetID;

        public WidgetClickMessage(com.wali.live.proto.LiveMessage.WidgetClickMessage upgradeMessage) {
            this.counter = upgradeMessage.getCounterValue();
            this.widgetID = upgradeMessage.getWidgetID();
        }

        @Override
        public ByteString toByteString() {
            return null;
        }
    }

    public static class LiveInfoChangeMsgExt implements MsgExt {
        public long mod_flag;
        public long zuid;
        public String live_id;
        public boolean enable_viewer_mic;
        public String liveTitle;

        public LiveInfoChangeMsgExt(com.wali.live.proto.LiveMessage.LiveInfoChangeMsg liveInfoChangeMsg) {
            mod_flag = liveInfoChangeMsg.getModFlag();
            zuid = liveInfoChangeMsg.getZuid();
            live_id = liveInfoChangeMsg.getLiveId();
            enable_viewer_mic = liveInfoChangeMsg.getEnableViewerMic();
            liveTitle = liveInfoChangeMsg.getTitle();
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

        public static RedEnvelopMsgExt transformFromPB(com.wali.live.proto.RedEnvelope.NotifyRedEnvelop red) {
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

        @Override
        public ByteString toByteString() {
            com.wali.live.proto.RedEnvelope.NotifyRedEnvelop.Builder builder = new com.wali.live.proto.RedEnvelope.NotifyRedEnvelop.Builder();
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
    }

    public static class VipLevelChangedExt implements MsgExt {
        public final int newVipLevel;
        public final int oldVipLevel;

        private VipLevelChangedExt(int newVipLevel, int oldVipLevel) {
            this.newVipLevel = newVipLevel;
            this.oldVipLevel = oldVipLevel;
        }

        public static VipLevelChangedExt newInstance(com.wali.live.proto.Vip.VipLevelChangeMsg msg) {
            return new VipLevelChangedExt(msg.getNewVipLevel(), msg.getOldVipLevel());
        }

        @Override
        public ByteString toByteString() {
            return null;
        }
    }

    public static class AnchorDiscountAgingExt implements MsgExt {
        public long leftTs;

        public AnchorDiscountAgingExt(long leftTs) {
            this.leftTs = leftTs;
        }

        @Override
        public ByteString toByteString() {
            return null;
        }
    }

    public static class GlobalRoomMessageExt {
        private List<InnerGlobalRoomMessageExt> innerGlobalRoomMessageExtList;

        public static GlobalRoomMessageExt loadFromPB(com.wali.live.proto.LiveMessage.GlobalRoomMessageExt globalRoomMsgExt) {
            if (globalRoomMsgExt == null) {
                return null;
            }
            GlobalRoomMessageExt globalRoomMessageExt = new GlobalRoomMessageExt();
            List<com.wali.live.proto.LiveMessage.InnerGlobalRoomMessageExt> innerGlobalRoomMsgExtList = globalRoomMsgExt.getInnerGlobalRoomMsgExtList();
            if (innerGlobalRoomMsgExtList == null || innerGlobalRoomMsgExtList.isEmpty()) {
                return null;
            }
            globalRoomMessageExt.innerGlobalRoomMessageExtList = new ArrayList<>();
            for (com.wali.live.proto.LiveMessage.InnerGlobalRoomMessageExt msgExt : globalRoomMsgExt.getInnerGlobalRoomMsgExtList()) {
                InnerGlobalRoomMessageExt innerGlobalRoomMessageExt = InnerGlobalRoomMessageExt.loadFromPb(msgExt);
                globalRoomMessageExt.innerGlobalRoomMessageExtList.add(innerGlobalRoomMessageExt);
            }
            return globalRoomMessageExt;
        }

        public List<InnerGlobalRoomMessageExt> getInnerGlobalRoomMessageExtList() {
            return innerGlobalRoomMessageExtList;
        }

        public void setInnerGlobalRoomMessageExtList(List<InnerGlobalRoomMessageExt> innerGlobalRoomMessageExtList) {
            this.innerGlobalRoomMessageExtList = innerGlobalRoomMessageExtList;
        }
    }

    public static class InnerGlobalRoomMessageExt {
        private int type;// 100:勋章配置消息 101:弹幕文案scheme消息
        private BarrageMsgExt.MedalConfigMessage medalConfigMessage;
        private TxtSchemeMessage txtSchemeMessage;
        private ShareJoinRoomMessage shareJoinRoomMessage;
        private VFansMemberBriefInfo vFansMemberBriefInfo; //宠爱团信息

        public static InnerGlobalRoomMessageExt loadFromPb(com.wali.live.proto.LiveMessage.InnerGlobalRoomMessageExt msgExt) {
            InnerGlobalRoomMessageExt innerGlobalRoomMessageExt = new InnerGlobalRoomMessageExt();
            innerGlobalRoomMessageExt.type = msgExt.getType();
            byte[] bytes = msgExt.getExt().toByteArray();
            if (msgExt.getType() == INNER_GLOBAL_MEDAL_TYPE) {
                MedalConfigMessage message = null;
                try {
                    message = MedalConfigMessage.parseFrom(bytes);
                    innerGlobalRoomMessageExt.medalConfigMessage = BarrageMsgExt.MedalConfigMessage.loadFromPB(message);
                } catch (Exception e) {
                    MyLog.e(e);
                }
            } else if (msgExt.getType() == INNER_GLOBAL_SCHEME_TYPE) {
                com.wali.live.proto.LiveMessage.TxtSchemeMessage txtSchemeMessage = null;
                try {
                    txtSchemeMessage = com.wali.live.proto.LiveMessage.TxtSchemeMessage.parseFrom(bytes);
                    innerGlobalRoomMessageExt.txtSchemeMessage = TxtSchemeMessage.loadFromPB(txtSchemeMessage);
                } catch (Exception e) {
                    MyLog.e(e);
                }
            } else if (msgExt.getType() == INNER_GLOBAL_SHARE_JOIN_ROME_TYPE) {
                com.wali.live.proto.LiveMessage.ShareJoinRoomMessage joinRoomMessage = null;
                try {
                    joinRoomMessage = com.wali.live.proto.LiveMessage.ShareJoinRoomMessage.parseFrom(bytes);
                    innerGlobalRoomMessageExt.shareJoinRoomMessage = ShareJoinRoomMessage.loadFromPB(joinRoomMessage);
                } catch (Exception e) {
                    MyLog.e(e);
                }
            } else if (msgExt.getType() == INNER_GLOBAL_ADMIN_FLY) {

            } else if (msgExt.getType() == INNER_GLOBAL_PAY_HORN) {

            } else if (msgExt.getType() == INNER_GLOBAL_VFAN) {
                com.wali.live.proto.LiveMessage.VFansMemberBriefInfo vFansMemberBriefInfoPb = null;
                try {
                    vFansMemberBriefInfoPb = com.wali.live.proto.LiveMessage.VFansMemberBriefInfo.parseFrom(bytes);
                    innerGlobalRoomMessageExt.vFansMemberBriefInfo = VFansMemberBriefInfo.loadFromPB(vFansMemberBriefInfoPb);
                } catch (Exception e) {
                    MyLog.e(e);
                }
            }
            return innerGlobalRoomMessageExt;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public BarrageMsgExt.MedalConfigMessage getMedalConfigMessage() {
            return medalConfigMessage;
        }

        public void setMedalConfigMessage(BarrageMsgExt.MedalConfigMessage medalConfigMessage) {
            this.medalConfigMessage = medalConfigMessage;
        }

        public TxtSchemeMessage getTxtSchemeMessage() {
            return txtSchemeMessage;
        }

        public void setTxtSchemeMessage(TxtSchemeMessage txtSchemeMessage) {
            this.txtSchemeMessage = txtSchemeMessage;
        }

        public ShareJoinRoomMessage getShareJoinRoomMessage() {
            return shareJoinRoomMessage;
        }

        public void setShareJoinRoomMessage(ShareJoinRoomMessage shareJoinRoomMessage) {
            this.shareJoinRoomMessage = shareJoinRoomMessage;
        }

        public VFansMemberBriefInfo getvFansMemberBriefInfo() {
            return vFansMemberBriefInfo;
        }

        public void setvFansMemberBriefInfo(VFansMemberBriefInfo vFansMemberBriefInfo) {
            this.vFansMemberBriefInfo = vFansMemberBriefInfo;
        }
    }

    public static class TxtSchemeMessage {
        private String schemeUrl;

        public static TxtSchemeMessage loadFromPB(com.wali.live.proto.LiveMessage.TxtSchemeMessage config) {
            if (config == null) {
                return null;
            }
            TxtSchemeMessage txtSchemeMessage = new TxtSchemeMessage();
            txtSchemeMessage.schemeUrl = config.getSchemeUrl();
            return txtSchemeMessage;
        }

        public String getSchemeUrl() {
            return schemeUrl;
        }

        public void setSchemeUrl(String schemeUrl) {
            this.schemeUrl = schemeUrl;
        }
    }

    public static class ShareJoinRoomMessage {
        private String content;

        public static ShareJoinRoomMessage loadFromPB(com.wali.live.proto.LiveMessage.ShareJoinRoomMessage message) {
            if (message == null) {
                return null;
            }
            ShareJoinRoomMessage txtSchemeMessage = new ShareJoinRoomMessage();
            txtSchemeMessage.content = message.getContent();
            return txtSchemeMessage;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }

    public static class VFansMemberBriefInfo {
        private int petLevel;
        private String medalValue;
        private boolean isUseMedal;
        private boolean isVipExpire;
        private String barrageColor;

        public static VFansMemberBriefInfo loadFromPB(com.wali.live.proto.LiveMessage.VFansMemberBriefInfo message) {
            if (message == null) {
                return null;
            }
            VFansMemberBriefInfo vFansMemberBriefInfo = new VFansMemberBriefInfo();
            vFansMemberBriefInfo.petLevel = message.getPetLevel();
            vFansMemberBriefInfo.medalValue = message.getMedalValue();
            vFansMemberBriefInfo.isUseMedal = message.getIsUseMedal();
            vFansMemberBriefInfo.isVipExpire = message.getIsVipExpire();
            vFansMemberBriefInfo.barrageColor = message.getBarrageColor();
            return vFansMemberBriefInfo;
        }

        public int getPetLevel() {
            return petLevel;
        }

        public void setPetLevel(int petLevel) {
            this.petLevel = petLevel;
        }

        public String getMedalValue() {
            return medalValue;
        }

        public void setMedalValue(String medalValue) {
            this.medalValue = medalValue;
        }

        public boolean isUseMedal() {
            return isUseMedal;
        }

        public void setUseMedal(boolean useMedal) {
            isUseMedal = useMedal;
        }

        public boolean isVipExpire() {
            return isVipExpire;
        }

        public void setVipExpire(boolean vipExpire) {
            isVipExpire = vipExpire;
        }

        public String getBarrageColor() {
            return barrageColor;
        }

        public void setBarrageColor(String barrageColor) {
            this.barrageColor = barrageColor;
        }
    }
}
