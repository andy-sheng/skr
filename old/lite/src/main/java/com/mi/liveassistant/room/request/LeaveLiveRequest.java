package com.mi.liveassistant.room.request;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.liveassistant.account.UserAccountManager;
import com.mi.liveassistant.common.api.BaseRequest;
import com.mi.liveassistant.milink.command.MiLinkCommand;
import com.mi.liveassistant.proto.LiveProto.LeaveLiveReq;
import com.mi.liveassistant.proto.LiveProto.LeaveLiveRsp;

/**
 * Created by lan on 16-3-18.
 *
 * @version lit structure
 * @notice 注意修改命令字和Action
 */
public class LeaveLiveRequest extends BaseRequest {
    public LeaveLiveRequest(long playerId, String liveId) {
        super(MiLinkCommand.COMMAND_LIVE_ENTER, "EnterLive");
        build(playerId, liveId);
    }

    private void build(long playerId, String liveId) {
        mRequest = LeaveLiveReq.newBuilder()
                .setUuid(UserAccountManager.getInstance().getUuidAsLong())
                .setZuid(playerId)
                .setLiveId(liveId)
                .build();
    }

    protected LeaveLiveRsp parse(byte[] bytes) throws InvalidProtocolBufferException {
        return LeaveLiveRsp.parseFrom(bytes);
    }
}
