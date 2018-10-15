package com.wali.live.watchsdk.channel.request;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.wali.live.proto.HotChannelProto;
import com.wali.live.proto.HotChannelProto.GetRecListReq.Builder;
import com.mi.live.data.api.request.BaseRequest;

/**
 * Created by liuting on 18-9-11.
 */

public class GetRecListRequest extends BaseRequest {
    public GetRecListRequest(long viewerId, long anchorId, String packageName, long gameId, int recType, int reqFrom) {
        super(MiLinkCommand.COMMAND_HOT_CHANNEL_REC_LIST, "GetRecListRequest");
        generateRequest(viewerId, anchorId, packageName, gameId, recType, reqFrom);
    }

    private Builder generateBuilder() {
        return HotChannelProto.GetRecListReq.newBuilder();
    }

    private void generateRequest(long viewerId, long anchorId, String packageName, long gameId, int recType, int reqFrom) {
        mRequest = generateBuilder()
                .setViewerId(viewerId)
                .setAnchorId(anchorId)
                .setPackageName(packageName)
                .setGameId(gameId)
                .setRecType(recType)
                .setReqFrom(reqFrom)
                .build();
    }

    protected HotChannelProto.GetRecListRsp parse(byte[] bytes) throws InvalidProtocolBufferException {
        return HotChannelProto.GetRecListRsp.parseFrom(bytes);
    }
}
