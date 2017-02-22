package com.mi.live.data.api.request.live;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.wali.live.proto.LiveProto.LeaveLiveReq;
import com.wali.live.proto.LiveProto.LeaveLiveRsp;

/**
 * Created by lan on 16-3-18.
 * 注意修改命令字和Action
 */
public class LeaveLiveRequest extends BaseLiveRequest {
    {
        mCommand = MiLinkCommand.COMMAND_LIVE_LEAVE;
        mAction = "LeaveLive";
    }

    public LeaveLiveRequest(long ownerId, String liveId) {
        super(MiLinkCommand.COMMAND_LIVE_LEAVE, "LeaveLive", null);
        mRequest = generateBuilder()
                .setZuid(ownerId)
                .setLiveId(liveId)
                .build();
    }

    public LeaveLiveRequest(long ownerId, String liveId, int messageMode) {
        super(MiLinkCommand.COMMAND_LIVE_LEAVE, "LeaveLive", null);
        mRequest = generateBuilder()
                .setZuid(ownerId)
                .setLiveId(liveId)
                .setMessageMode(messageMode)
                .build();
    }

    private LeaveLiveReq.Builder generateBuilder() {
        return LeaveLiveReq.newBuilder()
                .setUuid(UserAccountManager.getInstance().getUuidAsLong());
    }

    protected LeaveLiveRsp parse(byte[] bytes) throws InvalidProtocolBufferException {
        return LeaveLiveRsp.parseFrom(bytes);
    }
}
