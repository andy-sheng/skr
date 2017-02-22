package com.wali.live.livesdk.live.api;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.api.request.live.BaseLiveRequest;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.wali.live.proto.LiveProto.ZuidActiveReq;
import com.wali.live.proto.LiveProto.ZuidActiveRsp;

/**
 * Created by lan on 16-3-18.
 * 注意修改命令字和Action
 */
public class ZuidActiveRequest extends BaseLiveRequest {
    public ZuidActiveRequest() {
        super(MiLinkCommand.COMMAND_LIVE_ZUID_ACTIVE, "ZuidActive", null);
    }

    public ZuidActiveRequest(String liveId) {
        this();
        mRequest = ZuidActiveReq.newBuilder()
                .setLiveId(liveId)
                .build();
    }

    protected ZuidActiveRsp parse(byte[] bytes) throws InvalidProtocolBufferException {
        return ZuidActiveRsp.parseFrom(bytes);
    }
}
