package com.wali.live.watchsdk.fans.request;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.api.request.BaseRequest;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.wali.live.proto.VFansProto;

/**
 * Created by lan on 2017/11/9.
 */
public class GetGroupDetailRequest extends BaseRequest {
    public GetGroupDetailRequest(long zuid) {
        super(MiLinkCommand.COMMAND_VFANS_GROUP_DETAIL, "getGroupDetail");
        build(zuid);
    }

    private void build(long zuid) {
        mRequest = VFansProto.GroupDetailReq.newBuilder()
                .setUuid(UserAccountManager.getInstance().getUuidAsLong())
                .setZuid(zuid)
                .build();
    }
    
    @Override
    protected VFansProto.GroupDetailRsp parse(byte[] bytes) throws InvalidProtocolBufferException {
        return VFansProto.GroupDetailRsp.parseFrom(bytes);
    }
}
