package com.wali.live.watchsdk.fans.request;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.api.request.BaseRequest;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.wali.live.proto.VFansProto;

/**
 * Created by lan on 2017/11/13.
 */
public class GetGroupJobRequest extends BaseRequest {
    public GetGroupJobRequest(long zuid) {
        super(MiLinkCommand.COMMAND_VFANS_JOB_LIST, "getGroupJob");
        build(zuid);
    }

    private void build(long zuid) {
        mRequest = VFansProto.GroupJobListReq.newBuilder()
                .setUuid(UserAccountManager.getInstance().getUuidAsLong())
                .setZuid(zuid)
                .build();
    }

    @Override
    protected VFansProto.GroupJobListRsp parse(byte[] bytes) throws InvalidProtocolBufferException {
        return VFansProto.GroupJobListRsp.parseFrom(bytes);
    }
}
