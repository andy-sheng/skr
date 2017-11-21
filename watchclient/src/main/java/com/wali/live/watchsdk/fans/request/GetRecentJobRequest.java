package com.wali.live.watchsdk.fans.request;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.api.request.BaseRequest;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.wali.live.proto.VFansProto;

/**
 * Created by lan on 2017/11/21.
 */
public class GetRecentJobRequest extends BaseRequest {
    public GetRecentJobRequest(long zuid) {
        super(MiLinkCommand.COMMAND_VFAN_GET_RECENT_JOB, "getRecentJob");
        build(zuid);
    }

    private void build(long zuid) {
        VFansProto.GetRecentJobReq.Builder builder = VFansProto.GetRecentJobReq.newBuilder()
                .setUuid(UserAccountManager.getInstance().getUuidAsLong())
                .setZuid(zuid);
        mRequest = builder.build();
    }

    @Override
    protected VFansProto.GetRecentJobRsp parse(byte[] bytes) throws InvalidProtocolBufferException {
        return VFansProto.GetRecentJobRsp.parseFrom(bytes);
    }
}
