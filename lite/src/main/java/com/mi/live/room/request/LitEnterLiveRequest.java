package com.mi.live.room.request;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.api.request.BaseRequest;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.wali.live.proto.LiveProto.EnterLiveReq;
import com.wali.live.proto.LiveProto.EnterLiveRsp;

/**
 * Created by lan on 16-3-18.
 *
 * @version lit structure
 * @notice 注意修改命令字和Action
 */
public class LitEnterLiveRequest extends BaseRequest {
    public LitEnterLiveRequest(long playerId, String liveId) {
        super(MiLinkCommand.COMMAND_LIVE_ENTER, "EnterLive", null);
        build(playerId, liveId);
    }

    private void build(long playerId, String liveId) {
        mRequest = EnterLiveReq.newBuilder()
                .setUuid(UserAccountManager.getInstance().getUuidAsLong())
                .setZuid(playerId)
                .setLiveId(liveId)
                .build();
    }

    protected EnterLiveRsp parse(byte[] bytes) throws InvalidProtocolBufferException {
        return EnterLiveRsp.parseFrom(bytes);
    }
}
