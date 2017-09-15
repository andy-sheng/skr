package com.mi.live.data.push.model;

import com.google.protobuf.ByteString;
import com.wali.live.proto.LiveCommonProto;
import com.wali.live.proto.LiveMessageProto;

/**
 * Created by yangli on 2017/9/15.
 */
public class BarrageMsgExt {

    interface IProtoBarrage<EXT extends BarrageMsg.MsgExt, PROTO> extends BarrageMsg.MsgExt {
        EXT parseFromPB(PROTO msg);
    }

    /**
     * 连麦开始信息
     */
    public static class MicBeginInfo implements IProtoBarrage<MicBeginInfo,
            LiveMessageProto.MicBeginMessage> {
        public String roomId; // 房间号
        public long zuid;     // 主播用户id
        public long micuid;   // 对应的用户id
        public int lineType;  // 0:主播与观众连麦 1：主播与主播连麦

        public float scaleX; // 子视图左上角X在实际推流视频中的比例
        public float scaleY; // 子视图左上角Y在实际推流视频中的比例
        public float scaleW; // 子视图宽度在实际推流视频中的比例
        public float scaleH; // 子视图高度左上角在推流视频中的比例

        public MicBeginInfo() {
        }

        @Override
        public MicBeginInfo parseFromPB(LiveMessageProto.MicBeginMessage msg) {
            if (msg == null) {
                return null;
            }
            roomId = msg.getLiveId();
            zuid = msg.getZuid();
            micuid = msg.getMicInfo().getMicuid();
            lineType = msg.getType();
            if (msg.getMicInfo().hasSubViewPos()) {
                scaleX = msg.getMicInfo().getSubViewPos().getTopXScale();
                scaleY = msg.getMicInfo().getSubViewPos().getTopYScale();
                scaleW = msg.getMicInfo().getSubViewPos().getWidthScale();
                scaleH = msg.getMicInfo().getSubViewPos().getHeightScale();
            }
            return this;
        }

        public MicBeginInfo parseFromPB(LiveCommonProto.MicInfo micInfo) {
            if (micInfo == null) {
                return null;
            }
            micuid = micInfo.getMicuid();
            lineType = micInfo.getType();
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
    public static class MicEndInfo implements IProtoBarrage<MicEndInfo,
            LiveMessageProto.MicEndMessage> {
        public String roomId; // 房间号
        public long zuid;     // 主播用户id
        public long micuid;   // 对应的用户id
        public int lineType;  // 0:主播与观众连麦 1：主播与主播连麦

        public MicEndInfo() {
        }

        @Override
        public MicEndInfo parseFromPB(LiveMessageProto.MicEndMessage msg) {
            if (msg == null) {
                return null;
            }
            roomId = msg.getLiveId();
            zuid = msg.getZuid();
            micuid = msg.getMicInfo().getMicuid();
            lineType = msg.getType();
            return this;
        }

        @Override
        public ByteString toByteString() {
            return null;
        }
    }
}
