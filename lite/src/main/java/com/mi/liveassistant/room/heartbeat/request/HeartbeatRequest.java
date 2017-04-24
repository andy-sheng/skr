package com.mi.liveassistant.room.heartbeat.request;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.liveassistant.common.api.BaseRequest;
import com.mi.liveassistant.milink.command.MiLinkCommand;
import com.mi.liveassistant.proto.LiveProto.HeartBeatReq;
import com.mi.liveassistant.proto.LiveProto.HeartBeatRsp;

/**
 * Created by lan on 17/4/24.
 */
public class HeartbeatRequest extends BaseRequest {
    public HeartbeatRequest(String liveId, int status) {
        super(MiLinkCommand.COMMAND_LIVE_HB, "Heartbeat", null);
        build(liveId, status);
    }

    private void build(String liveId, int status) {
        mRequest = HeartBeatReq.newBuilder()
                .setLiveId(liveId)
                .setStatus(status)
                .build();
    }

    @Override
    protected HeartBeatRsp parse(byte[] bytes) throws InvalidProtocolBufferException {
        return HeartBeatRsp.parseFrom(bytes);
    }
}
