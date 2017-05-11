package com.mi.liveassistant.room.viewer.request;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.liveassistant.account.UserAccountManager;
import com.mi.liveassistant.common.api.BaseRequest;
import com.mi.liveassistant.milink.command.MiLinkCommand;
import com.mi.liveassistant.proto.LiveProto.ViewerTopReq;
import com.mi.liveassistant.proto.LiveProto.ViewerTopRsp;

/**
 * Created by lan on 16-3-18.
 * 注意修改命令字和Action
 */
public class ViewerTopRequest extends BaseRequest {
    public ViewerTopRequest(long playerId, String liveId) {
        super(MiLinkCommand.COMMAND_LIVE_VIEWER_TOP, "ViewerTop");
        mRequest = ViewerTopReq.newBuilder()
                .setUuid(UserAccountManager.getInstance().getUuidAsLong())
                .setZuid(playerId)
                .setLiveId(liveId)
                .build();
    }

    protected ViewerTopRsp parse(byte[] bytes) throws InvalidProtocolBufferException {
        return ViewerTopRsp.parseFrom(bytes);
    }
}
