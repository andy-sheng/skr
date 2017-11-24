package com.wali.live.watchsdk.fans.setting.request;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.api.request.BaseRequest;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.wali.live.proto.VFansProto;

/**
 * Created by lan on 2017/11/24.
 */
public class GetGroupMedalRequest extends BaseRequest {
    public GetGroupMedalRequest(long zuid) {
        super(MiLinkCommand.COMMAND_VFAN_GET_GROUP_MEDAL, "getGroupMedal");
        build(zuid);
    }

    private void build(long zuid) {
        mRequest = VFansProto.GetGroupMedalReq.newBuilder()
                .setUuid(UserAccountManager.getInstance().getUuidAsLong())
                .setZuid(zuid)
                .build();
    }

    @Override
    protected VFansProto.GetGroupMedalRsp parse(byte[] bytes) throws InvalidProtocolBufferException {
        return VFansProto.GetGroupMedalRsp.parseFrom(bytes);
    }
}
