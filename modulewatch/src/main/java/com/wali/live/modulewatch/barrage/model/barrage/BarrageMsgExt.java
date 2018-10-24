package com.wali.live.modulewatch.barrage.model.barrage;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.common.core.userinfo.UserInfo;
import com.google.protobuf.ByteString;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yangli on 2017/9/15.
 */
public class BarrageMsgExt {

    interface IProtoBarrage<EXT extends BarrageMsg.MsgExt, PROTO> extends BarrageMsg.MsgExt {
        EXT parseFromPB(PROTO msg);
    }

    public static abstract class MicInfo {
        protected static final int MIC_TYPE_NORMAL = 0;
        protected static final int MIC_TYPE_ANCHOR = 1;

        public String roomId;    // 房间号
        public long zuid;        // 主播用户id
        public long micuid;      // 对应的用户id
        public String micLiveId; // 主播-主播连麦时，对方的房间ID
        protected int lineType;  // 0:主播与观众连麦 1：主播与主播连麦

        public boolean isMicAnchor() {
            return lineType == MIC_TYPE_ANCHOR;
        }

        public boolean isMicNormal() {
            return lineType == MIC_TYPE_NORMAL;
        }
    }

    /**
     * 连麦开始信息
     */
    public static class MicBeginInfo extends MicInfo implements IProtoBarrage<MicBeginInfo,
            com.wali.live.proto.LiveMic.MicBeginMessage> {
        public float scaleX; // 子视图左上角X在实际推流视频中的比例
        public float scaleY; // 子视图左上角Y在实际推流视频中的比例
        public float scaleW; // 子视图宽度在实际推流视频中的比例
        public float scaleH; // 子视图高度左上角在推流视频中的比例

        @Override
        public MicBeginInfo parseFromPB(com.wali.live.proto.LiveMic.MicBeginMessage msg) {
            if (msg == null) {
                return null;
            }
            roomId = msg.getLiveId();
            zuid = msg.getZuid();
            lineType = msg.getType();
            final com.wali.live.proto.LiveMic.MicInfo micInfo = msg.getMicInfo();
            micuid = micInfo.getMicuid();
            micLiveId = micInfo.getMicLiveid();
            if (micInfo.hasSubViewPos()) {
                final com.wali.live.proto.LiveMic.MicSubViewPos viewPos = micInfo.getSubViewPos();
                scaleX = viewPos.getTopXScale();
                scaleY = viewPos.getTopYScale();
                scaleW = viewPos.getWidthScale();
                scaleH = viewPos.getHeightScale();
            }
            return this;
        }

        public MicBeginInfo parseFromPB(com.wali.live.proto.LiveCommon.MicInfo micInfo) {
            if (micInfo == null) {
                return null;
            }
            micuid = micInfo.getMicuid();
            lineType = micInfo.getType();
            micLiveId = micInfo.getMicLiveid();
            if (micInfo.hasSubViewPos()) {
                scaleX = micInfo.getSubViewPos().getTopXScale();
                scaleY = micInfo.getSubViewPos().getTopYScale();
                scaleW = micInfo.getSubViewPos().getWidthScale();
                scaleH = micInfo.getSubViewPos().getHeightScale();
            }
            return this;
        }

        @Override
        public ByteString toByteString() {
            return null;
        }
    }

    /**
     * 连麦结束信息
     */
    public static class MicEndInfo extends MicInfo implements IProtoBarrage<MicEndInfo,
            com.wali.live.proto.LiveMic.MicEndMessage> {

        @Override
        public MicEndInfo parseFromPB(com.wali.live.proto.LiveMic.MicEndMessage msg) {
            if (msg == null) {
                return null;
            }
            roomId = msg.getLiveId();
            zuid = msg.getZuid();
            lineType = msg.getType();
            final com.wali.live.proto.LiveMic.MicInfo micInfo = msg.getMicInfo();
            micuid = micInfo.getMicuid();
            micLiveId = micInfo.getMicLiveid();
            return this;
        }

        @Override
        public ByteString toByteString() {
            return null;
        }
    }

    /**
     * PK分数信息
     */
    public static class PkScoreInfo implements IProtoBarrage<PkScoreInfo,
            com.wali.live.proto.LivePK.PKScoreChangeMsg> {
        public long uuid1;
        public long uuid2;
        public long score1;
        public long score2;
        public String pkType;
        protected String timeName;
        protected long beginTs;

        protected final PkScoreInfo parseFromInfo(@NonNull com.wali.live.proto.LivePK.NewPKInfo msg) {
            final com.wali.live.proto.LivePK.PKInfoItem item1 = msg.getFirst(), item2 = msg.getSecond();
            uuid1 = item1.getUuid();
            score1 = item1.getScore();
            uuid2 = item2.getUuid();
            score2 = item2.getScore();
            final com.wali.live.proto.LivePK.PKSetting setting = msg.getSetting();
            pkType = setting.getContent().getName();
            timeName = setting.getDuration().getName();
            beginTs = msg.getBeginTs();
            return this;
        }

        @Override
        public PkScoreInfo parseFromPB(com.wali.live.proto.LivePK.PKScoreChangeMsg msg) {
            if (msg == null || !msg.hasPkInfo()) {
                return null;
            }
            return parseFromInfo(msg.getPkInfo());
        }

        @Override
        public ByteString toByteString() {
            return null;
        }
    }

    /**
     * PK开始信息
     */
    public static class PkStartInfo extends PkScoreInfo {
        public int startRemainTime;
        public int pkRemainTime;

        public PkStartInfo parseFromScoreInfo(@NonNull PkScoreInfo pkScoreInfo, long currServerTs) {
            uuid1 = pkScoreInfo.uuid1;
            score1 = pkScoreInfo.score1;
            uuid2 = pkScoreInfo.uuid2;
            score2 = pkScoreInfo.score2;
            pkType = pkScoreInfo.pkType;
            timeName = pkScoreInfo.timeName;
            beginTs = pkScoreInfo.beginTs;
            calcRemainTime(currServerTs);
            return this;
        }

        public PkStartInfo parseFromInfo(com.wali.live.proto.LivePK.NewPKInfo msg, long currServerTs) {
            if (msg == null) {
                return null;
            }
            parseFromInfo(msg);
            calcRemainTime(currServerTs);
            return this;
        }

        public PkStartInfo parseFromPB(com.wali.live.proto.LivePK.PKBeginMessage msg, long currServerTs) {
            if (msg == null || !msg.hasPkInfo()) {
                return null;
            }
            parseFromInfo(msg.getPkInfo());
            calcRemainTime(currServerTs);
            return this;
        }

        private void calcRemainTime(long currServerTs) {
            pkRemainTime = 180;
            if (!TextUtils.isEmpty(timeName)) {
                int pkTime = 0;
                // 从timeName开始的数字中提取时常
                for (int i = 0, size = timeName.length(); i < size; ++i) {
                    char c = timeName.charAt(i);
                    if (c < '0' || c > '9') {
                        break;
                    }
                    pkTime = 10 * pkTime + (c - '0');
                }
                if (pkTime > 0) {
                    pkRemainTime = pkTime * 60;
                }
            }
            if (currServerTs == 0) {
                startRemainTime = 10;
            } else if (currServerTs < beginTs + 10000) { // 10s开始倒计时还未结束
                startRemainTime = (int) (10000 - currServerTs + beginTs) / 1000;
            } else { // 10s开始倒计时已结束，计算PK进度剩余时间
                startRemainTime = 0;
                pkRemainTime -= (int) (currServerTs - beginTs - 10000) / 1000;
                pkRemainTime = Math.max(pkRemainTime, 0);
            }
        }
    }

    public static class PKNoHeartBeatMsg implements BarrageMsg.MsgExt {
        public com.wali.live.proto.LivePK.NewPKInfo pkinfo;
        public long crash_id;

        public PKNoHeartBeatMsg(com.wali.live.proto.LivePK.PKNoHeartBeatMsg pKNoHeartBeatMsg) {
            pkinfo = pKNoHeartBeatMsg.getPkInfo();
            crash_id = pKNoHeartBeatMsg.getCrashUuid();
        }

        @Override
        public ByteString toByteString() {
            return null;
        }
    }

    /**
     * PK结束信息
     */
    public static class PkEndInfo extends PkScoreInfo {
        public long quitUuid;
        public String nickName1;
        public String nickName2;

        public PkEndInfo parseFromPB(com.wali.live.proto.LivePK.PKEndMessage msg) {
            if (msg == null || !msg.hasPkInfo()) {
                return null;
            }
            parseFromInfo(msg.getPkInfo());
            quitUuid = msg.getType() == 1 ? msg.getFromUuid() : 0; // type为1时表示PK提前结束
            nickName1 = String.valueOf(uuid1);
            nickName2 = String.valueOf(uuid2);
            return this;
        }

        public void setNickName(UserInfo user) {
            if (user == null || TextUtils.isEmpty(user.getUserNickname())) {
                return;
            }
            long uuid = user.getUserId();
            if (uuid1 == uuid) {
                nickName1 = user.getUserNickname();
            } else if (uuid2 == uuid) {
                nickName2 = user.getUserNickname();
            }
        }
    }

    public static class PKSysMsg implements BarrageMsg.MsgExt {
        public ByteString bytes;
        public int type;

        public PKSysMsg(com.wali.live.proto.LivePK.PKSysMsg pkSysMsg) {
            type = pkSysMsg.getType();
            bytes = ByteString.copyFrom(pkSysMsg.getExtMsg().toByteArray());
        }

        @Override
        public ByteString toByteString() {
            return null;
        }
    }

    public static class PKInviteMsg implements BarrageMsg.MsgExt {
        public long uuid;
        public String roomId;
        public long pkUid;
        public int type;
        public String time;
        public String nickName;
        public String pkType;
        public String punish;

        public com.wali.live.proto.LivePK.PKSetting pkSetting;
        public String anchorName;
        public long adminId;
        public String adminNickName;
        public String pkNickName;

        public PKInviteMsg(com.wali.live.proto.LivePK.PKInviteMsg pKInviteMsg) {
            uuid = pKInviteMsg.getUuid();
            roomId = pKInviteMsg.getLiveid();
            pkUid = pKInviteMsg.getPkUuid();
            type = pKInviteMsg.getType();
            nickName = pKInviteMsg.getNickname();
            pkSetting = pKInviteMsg.getSetting();
            if (pKInviteMsg.getSetting().hasContent()) {
                pkType = pKInviteMsg.getSetting().getContent().getName();
            }
            if (pKInviteMsg.getSetting().hasDuration()) {
                time = pKInviteMsg.getSetting().getDuration().getName();
            }

            if (pKInviteMsg.hasAdminUuid()) {
                adminId = pKInviteMsg.getAdminUuid();
            }
            if (pKInviteMsg.getSetting().hasPunish()) {
                if (pKInviteMsg.getSetting().getPunish().getType() == 2) {
                    punish = "";
                } else {
                    punish = pKInviteMsg.getSetting().getPunish().getName();
                }
            }
            adminNickName = pKInviteMsg.getAdminNickname();
            pkNickName = pKInviteMsg.getPkNickname();
        }

        @Override
        public ByteString toByteString() {
            return null;
        }
    }

    public static class PKAcceptMsg implements BarrageMsg.MsgExt {
        public long uuid;
        public long pkUid;
        public String pkRoomId;
        public int type;

        public long adminId;
        public String adminNickName;
        public String pkNickName;
        public String nickName;


        public PKAcceptMsg(com.wali.live.proto.LivePK.PKAcceptMsg pKAcceptMsg) {
            uuid = pKAcceptMsg.getUuid();
            pkRoomId = pKAcceptMsg.getPkLiveid();
            pkUid = pKAcceptMsg.getPkUuid();
            type = pKAcceptMsg.getType();


            if (pKAcceptMsg.hasAdminUuid()) {
                adminId = pKAcceptMsg.getAdminUuid();
            }
            adminNickName = pKAcceptMsg.getAdminNickname();
            pkNickName = pKAcceptMsg.getPkNickname();
            nickName = pKAcceptMsg.getNickname();
        }

        @Override
        public ByteString toByteString() {
            return null;
        }
    }

    public static class PKDeclineMsg implements BarrageMsg.MsgExt {
        public long uuid;
        public long pkUid;
        public String pkRoomId;
        public int type;

        public long adminId;
        public String adminNickName;
        public String pkNickName;
        public String nickName;

        public PKDeclineMsg(com.wali.live.proto.LivePK.PKDeclineMsg pKDeclineMsg) {
            uuid = pKDeclineMsg.getUuid();
            pkRoomId = pKDeclineMsg.getPkLiveid();
            pkUid = pKDeclineMsg.getPkUuid();
            type = pKDeclineMsg.getType();

            if (pKDeclineMsg.hasAdminUuid()) {
                adminId = pKDeclineMsg.getAdminUuid();
            }
            adminNickName = pKDeclineMsg.getAdminNickname();
            pkNickName = pKDeclineMsg.getPkNickname();
            nickName = pKDeclineMsg.getNickname();
        }

        @Override
        public ByteString toByteString() {
            return null;
        }
    }

    public static class PKCancelMsg implements BarrageMsg.MsgExt {
        public long uuid;
        public long pkUid;
        public String pkRoomId;
        public int type;

        public long adminId;
        public String adminNickName;
        public String pkNickName;
        public String nickName;

        public PKCancelMsg(com.wali.live.proto.LivePK.PKCancelInviteMsg pkCancelInviteMsg) {
            uuid = pkCancelInviteMsg.getUuid();
            pkRoomId = pkCancelInviteMsg.getLiveid();
            pkUid = pkCancelInviteMsg.getPkUuid();
            type = pkCancelInviteMsg.getType();

            if (pkCancelInviteMsg.hasAdminUuid()) {
                adminId = pkCancelInviteMsg.getAdminUuid();
            }
            adminNickName = pkCancelInviteMsg.getAdminNickname();
            pkNickName = pkCancelInviteMsg.getPkNickname();
            nickName = pkCancelInviteMsg.getNickname();
        }

        @Override
        public ByteString toByteString() {
            return null;
        }
    }

    public static class MedalConfigMessage {
        private List<InnerMedalConfig> beforeNickNameCofigList;
        private List<InnerMedalConfig> afterNickNameCofigList;
        private List<InnerMedalConfig> beforeContentCofigList;
        private List<InnerMedalConfig> AfterContentCofigList;

        public static MedalConfigMessage loadFromPB(com.wali.live.proto.LiveMessage.MedalConfigMessage message) {
            MedalConfigMessage medalConfigMessage = new MedalConfigMessage();
            List<com.wali.live.proto.LiveMessage.InnerMedalConfig> beforeContentConfigList = message.getBeforeContentConfigList();
            if (beforeContentConfigList != null && !beforeContentConfigList.isEmpty()) {
                medalConfigMessage.beforeContentCofigList = new ArrayList<>();
                for (com.wali.live.proto.LiveMessage.InnerMedalConfig config : beforeContentConfigList) {
                    InnerMedalConfig innerMedalConfig = InnerMedalConfig.loadFromPB(config);
                    medalConfigMessage.beforeContentCofigList.add(innerMedalConfig);
                }
            }

            List<com.wali.live.proto.LiveMessage.InnerMedalConfig> afterContentConfigList = message.getAfterContentConfigList();
            if (afterContentConfigList != null && !afterContentConfigList.isEmpty()) {
                medalConfigMessage.AfterContentCofigList = new ArrayList<>();
                for (com.wali.live.proto.LiveMessage.InnerMedalConfig config : afterContentConfigList) {
                    InnerMedalConfig innerMedalConfig = InnerMedalConfig.loadFromPB(config);
                    medalConfigMessage.AfterContentCofigList.add(innerMedalConfig);
                }
            }

            List<com.wali.live.proto.LiveMessage.InnerMedalConfig> beforeNicknameConfigList = message.getBeforeNicknameConfigList();
            if (beforeNicknameConfigList != null && !beforeNicknameConfigList.isEmpty()) {
                medalConfigMessage.beforeNickNameCofigList = new ArrayList<>();
                for (com.wali.live.proto.LiveMessage.InnerMedalConfig config : beforeNicknameConfigList) {
                    InnerMedalConfig innerMedalConfig = InnerMedalConfig.loadFromPB(config);
                    medalConfigMessage.beforeNickNameCofigList.add(innerMedalConfig);
                }
            }

            List<com.wali.live.proto.LiveMessage.InnerMedalConfig> afterNicknameConfigList = message.getAfterNicknameConfigList();
            if (afterNicknameConfigList != null && !afterNicknameConfigList.isEmpty()) {
                medalConfigMessage.afterNickNameCofigList = new ArrayList<>();
                for (com.wali.live.proto.LiveMessage.InnerMedalConfig config : afterNicknameConfigList) {
                    InnerMedalConfig innerMedalConfig = InnerMedalConfig.loadFromPB(config);
                    medalConfigMessage.afterNickNameCofigList.add(innerMedalConfig);
                }
            }

            return medalConfigMessage;
        }

        public List<InnerMedalConfig> getBeforeNickNameCofigList() {
            return beforeNickNameCofigList;
        }

        public void setBeforeNickNameCofigList(List<InnerMedalConfig> beforeNickNameCofigList) {
            this.beforeNickNameCofigList = beforeNickNameCofigList;
        }

        public List<InnerMedalConfig> getAfterNickNameCofigList() {
            return afterNickNameCofigList;
        }

        public void setAfterNickNameCofigList(List<InnerMedalConfig> afterNickNameCofigList) {
            this.afterNickNameCofigList = afterNickNameCofigList;
        }

        public List<InnerMedalConfig> getBeforeContentCofigList() {
            return beforeContentCofigList;
        }

        public List<InnerMedalConfig> getAfterContentCofigList() {
            return AfterContentCofigList;
        }
    }

    public static class InnerMedalConfig {
        private String picId;

        public static InnerMedalConfig loadFromPB(com.wali.live.proto.LiveMessage.InnerMedalConfig config) {
            InnerMedalConfig innerMedalConfig = new InnerMedalConfig();
            innerMedalConfig.picId = config.getPicId();
            return innerMedalConfig;
        }

        public String getPicId() {
            return picId;
        }

        public void setPicId(String picId) {
            this.picId = picId;
        }
    }

//    public static class TurnTableMessageExt implements BarrageMsg.MsgExt {
//
//        private TurnTableConfigModel turnTableConfigModel;
//
//        public TurnTableMessageExt(TurntablePush turntablePush) {
//            if (turntablePush == null) {
//                return;
//            }
//
//            TurntableConfig turntableConfig = turntablePush.getTurntableConfig();
//            turnTableConfigModel = new TurnTableConfigModel(turntableConfig);
//        }
//
//        public TurnTableConfigModel getTurnTableConfigModel() {
//            return turnTableConfigModel;
//        }
//
//        public void setTurnTableConfigModel(TurnTableConfigModel turnTableConfigModel) {
//            this.turnTableConfigModel = turnTableConfigModel;
//        }
//
//        @Override
//        public ByteString toByteString() {
//            return null;
//        }
//    }

}
