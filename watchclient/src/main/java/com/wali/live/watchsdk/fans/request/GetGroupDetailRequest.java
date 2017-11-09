package com.wali.live.watchsdk.fans.request;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.api.request.BaseRequest;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.wali.live.proto.VFansProto;

/**
 * Created by zyh on 2017/11/8.
 */

public class GetGroupDetailRequest extends BaseRequest {

    private GetGroupDetailRequest() {
        super(MiLinkCommand.COMMAND_VFANS_GROUP_DETAIL, "GetGroupDetailRequest");
        build(-1);
    }

    public GetGroupDetailRequest(long anchorId) {
        super(MiLinkCommand.COMMAND_VFANS_GROUP_DETAIL, "GetGroupDetailRequest");
        build(anchorId);
    }

    private void build(long anchorId) {
        VFansProto.GroupDetailReq.Builder builder = VFansProto.GroupDetailReq.newBuilder()
                .setZuid(anchorId)
                .setUuid(UserAccountManager.getInstance().getUuidAsLong());
        mRequest = builder.build();
    }


    @Override
    protected VFansProto.GroupDetailRsp parse(byte[] bytes) throws InvalidProtocolBufferException {
        return VFansProto.GroupDetailRsp.parseFrom(bytes);
    }
}
