package com.mi.liveassistant.room.request;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.mi.liveassistant.common.api.BaseRequest;
import com.mi.liveassistant.proto.LiveProto.EndLiveReq;
import com.mi.liveassistant.proto.LiveProto.EndLiveRsp;

/**
 * Created by lan on 16-3-18.
 *
 * @version lit structure
 * @notice 注意修改命令字和Action
 */
public class EndLiveRequest extends BaseRequest {
    public EndLiveRequest() {
        super(MiLinkCommand.COMMAND_LIVE_END, "EndLive", null);
    }

    public EndLiveRequest(String liveId) {
        this();
        if (TextUtils.isEmpty(liveId)) {
            return;
        }
        build(liveId);
    }

    private void build(@NonNull String liveId) {
        mRequest = EndLiveReq.newBuilder()
                .setUuid(UserAccountManager.getInstance().getUuidAsLong())
                .setLiveId(liveId)
                .build();
    }

    protected EndLiveRsp parse(byte[] bytes) throws InvalidProtocolBufferException {
        return EndLiveRsp.parseFrom(bytes);
    }
}
