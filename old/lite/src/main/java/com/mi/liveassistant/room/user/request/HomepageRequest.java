package com.mi.liveassistant.room.user.request;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.liveassistant.common.api.BaseRequest;
import com.mi.liveassistant.milink.command.MiLinkCommand;
import com.mi.liveassistant.proto.UserProto.GetHomepageReq;
import com.mi.liveassistant.proto.UserProto.GetHomepageRsp;

/**
 * Created by lan on 17/4/24.
 */
public class HomepageRequest extends BaseRequest {
    public HomepageRequest(long uuid) {
        super(MiLinkCommand.COMMAND_GET_HOMEPAGE, "Homepage");
        build(uuid);
    }

    private void build(long uuid) {
        mRequest = GetHomepageReq.newBuilder()
                .setZuid(uuid)
                .build();
    }

    @Override
    protected GetHomepageRsp parse(byte[] bytes) throws InvalidProtocolBufferException {
        return GetHomepageRsp.parseFrom(bytes);
    }
}
