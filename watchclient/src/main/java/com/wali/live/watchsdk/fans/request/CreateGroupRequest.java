package com.wali.live.watchsdk.fans.request;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.api.request.BaseRequest;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.wali.live.proto.VFansProto;

/**
 * Created by lan on 2017/11/15.
 */
public class CreateGroupRequest extends BaseRequest {
    public CreateGroupRequest(String name) {
        super(MiLinkCommand.COMMAND_VFANS_CREATE_GROUP, "createGroup");
        build(name);
    }

    private void build(String name) {
        mRequest = VFansProto.CreateGroupReq.newBuilder()
                .setZuid(UserAccountManager.getInstance().getUuidAsLong())
                .setGroupName(name)
                .build();
    }

    @Override
    protected VFansProto.CreateGroupRsp parse(byte[] bytes) throws InvalidProtocolBufferException {
        return VFansProto.CreateGroupRsp.parseFrom(bytes);
    }
}
