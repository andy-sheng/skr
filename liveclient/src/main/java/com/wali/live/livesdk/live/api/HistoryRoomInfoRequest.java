package com.wali.live.livesdk.live.api;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.api.request.live.BaseLiveRequest;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.wali.live.proto.Live2Proto.HistoryRoomInfoReq;
import com.wali.live.proto.Live2Proto.HistoryRoomInfoRsp;


public class HistoryRoomInfoRequest extends BaseLiveRequest {

    public HistoryRoomInfoRequest(long zuid) {
        super(MiLinkCommand.COMMAND_LIVE_HISTORY_ROOM_INFO, "HistoryRoomInfo", null);
        HistoryRoomInfoReq.Builder builder = HistoryRoomInfoReq.newBuilder();
        builder.setZuid(zuid);
        mRequest = builder.build();
    }

    @Override
    protected HistoryRoomInfoRsp parse(byte[] bytes) throws InvalidProtocolBufferException {
        return HistoryRoomInfoRsp.parseFrom(bytes);
    }
}