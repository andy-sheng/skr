package com.mi.live.data.push.model;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.protobuf.ByteString;
import com.mi.live.data.user.User;
import com.wali.live.proto.LiveMicProto;
import com.wali.live.proto.LivePKProto;

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
            LiveMicProto.MicBeginMessage> {
        public float scaleX; // 子视图左上角X在实际推流视频中的比例
        public float scaleY; // 子视图左上角Y在实际推流视频中的比例
        public float scaleW; // 子视图宽度在实际推流视频中的比例
        public float scaleH; // 子视图高度左上角在推流视频中的比例

        @Override
        public MicBeginInfo parseFromPB(LiveMicProto.MicBeginMessage msg) {
            if (msg == null) {
                return null;
            }
            roomId = msg.getLiveId();
            zuid = msg.getZuid();
            lineType = msg.getType();
            final LiveMicProto.MicInfo micInfo = msg.getMicInfo();
            micuid = micInfo.getMicuid();
            micLiveId = micInfo.getMicLiveid();
            if (micInfo.hasSubViewPos()) {
                final LiveMicProto.MicSubViewPos viewPos = micInfo.getSubViewPos();
                scaleX = viewPos.getTopXScale();
                scaleY = viewPos.getTopYScale();
                scaleW = viewPos.getWidthScale();
                scaleH = viewPos.getHeightScale();
            }
            return this;
        }

        public MicBeginInfo parseFromPB(LiveMicProto.MicInfo micInfo) {
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
            LiveMicProto.MicEndMessage> {

        @Override
        public MicEndInfo parseFromPB(LiveMicProto.MicEndMessage msg) {
            if (msg == null) {
                return null;
            }
            roomId = msg.getLiveId();
            zuid = msg.getZuid();
            lineType = msg.getType();
            final LiveMicProto.MicInfo micInfo = msg.getMicInfo();
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
            LivePKProto.PKScoreChangeMsg> {
        public long uuid1;
        public long uuid2;
        public long score1;
        public long score2;
        public String pkType;
        protected String timeName;
        protected long beginTs;

        protected final PkScoreInfo parseFromInfo(@NonNull LivePKProto.NewPKInfo msg) {
            final LivePKProto.PKInfoItem item1 = msg.getFirst(), item2 = msg.getSecond();
            uuid1 = item1.getUuid();
            score1 = item1.getScore();
            uuid2 = item2.getUuid();
            score2 = item2.getScore();
            final LivePKProto.PKSetting setting = msg.getSetting();
            pkType = setting.getContent().getName();
            timeName = setting.getDuration().getName();
            beginTs = msg.getBeginTs();
            return this;
        }

        @Override
        public PkScoreInfo parseFromPB(LivePKProto.PKScoreChangeMsg msg) {
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

        public PkStartInfo parseFromInfo(LivePKProto.NewPKInfo msg, long currServerTs) {
            if (msg == null) {
                return null;
            }
            parseFromInfo(msg);
            calcRemainTime(currServerTs);
            return this;
        }

        public PkStartInfo parseFromPB(LivePKProto.PKBeginMessage msg, long currServerTs) {
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

    /**
     * PK结束信息
     */
    public static class PkEndInfo extends PkScoreInfo {
        public long quitUuid;
        public String nickName1;
        public String nickName2;

        public PkEndInfo parseFromPB(LivePKProto.PKEndMessage msg) {
            if (msg == null || !msg.hasPkInfo()) {
                return null;
            }
            parseFromInfo(msg.getPkInfo());
            quitUuid = msg.getType() == 1 ? msg.getFromUuid() : 0; // type为1时表示PK提前结束
            nickName1 = String.valueOf(uuid1);
            nickName2 = String.valueOf(uuid2);
            return this;
        }

        public void setNickName(User user) {
            if (user == null || TextUtils.isEmpty(user.getNickname())) {
                return;
            }
            long uuid = user.getUid();
            if (uuid1 == uuid) {
                nickName1 = user.getNickname();
            } else if (uuid2 == uuid) {
                nickName2 = user.getNickname();
            }
        }
    }
}
