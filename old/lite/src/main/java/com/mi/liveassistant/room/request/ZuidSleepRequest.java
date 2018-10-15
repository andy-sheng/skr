package com.mi.liveassistant.room.request;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.liveassistant.common.api.BaseRequest;
import com.mi.liveassistant.milink.command.MiLinkCommand;
import com.mi.liveassistant.proto.LiveProto.ZuidSleepReq;
import com.mi.liveassistant.proto.LiveProto.ZuidSleepRsp;

/**
 * Created by lan on 16-3-18.
 * 注意修改命令字和Action
 */
public class ZuidSleepRequest extends BaseRequest {
    public ZuidSleepRequest() {
        super(MiLinkCommand.COMMAND_LIVE_ZUID_SLEEP, "ZuidSleep");
    }

    public ZuidSleepRequest(String liveId) {
        this();
        mRequest = ZuidSleepReq.newBuilder()
                .setLiveId(liveId)
                .build();
    }

    protected ZuidSleepRsp parse(byte[] bytes) throws InvalidProtocolBufferException {
        return ZuidSleepRsp.parseFrom(bytes);
    }
}
