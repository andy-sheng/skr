package com.wali.live.livesdk.live.api;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.api.request.live.BaseLiveRequest;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.wali.live.proto.LiveProto.ZuidSleepReq;
import com.wali.live.proto.LiveProto.ZuidSleepRsp;

/**
 * Created by lan on 16-3-18.
 * 注意修改命令字和Action
 */
public class ZuidSleepRequest extends BaseLiveRequest {
    public ZuidSleepRequest() {
        super(MiLinkCommand.COMMAND_LIVE_ZUID_SLEEP, "ZuidSleep", null);
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
