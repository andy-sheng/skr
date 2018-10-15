package com.mi.live.data.api.request.live;

import android.text.TextUtils;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.api.request.BaseRequest;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.wali.live.proto.LiveProto.EnterLiveReq;
import com.wali.live.proto.LiveProto.EnterLiveRsp;

/**
 * Created by lan on 16-3-18.
 * 注意修改命令字和Action
 */
public class EnterLiveRequest extends BaseRequest {
    private EnterLiveReq.Builder builder = EnterLiveReq.newBuilder();

    public EnterLiveRequest(long ownerId, String liveId) {
        super(MiLinkCommand.COMMAND_LIVE_ENTER, "EnterLive");
        build(ownerId, liveId);
    }

    public EnterLiveRequest(long ownerId, String liveId, String password) {
        super(MiLinkCommand.COMMAND_LIVE_ENTER, "EnterLive");
        if (!TextUtils.isEmpty(password)) {
            builder.setPassword(password.trim());
        }
        build(ownerId, liveId);
    }

    private void build(long ownerId, String liveId) {
        builder.setUuid(UserAccountManager.getInstance().getUuidAsLong())
                .setZuid(ownerId)
                .setLiveId(liveId);
        mRequest = builder.build();
    }

    protected EnterLiveRsp parse(byte[] bytes) throws InvalidProtocolBufferException {
        return EnterLiveRsp.parseFrom(bytes);
    }
}
