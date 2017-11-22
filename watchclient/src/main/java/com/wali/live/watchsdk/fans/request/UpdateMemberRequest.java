package com.wali.live.watchsdk.fans.request;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.api.request.BaseRequest;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.wali.live.proto.VFansCommonProto;
import com.wali.live.proto.VFansProto;

/**
 * Created by yangli on 2017/11/21.
 */
public class UpdateMemberRequest extends BaseRequest {

    protected UpdateMemberRequest() {
        super(MiLinkCommand.COMMAND_VFANS_UPDATE_GROUP_MEM, "update_groupmem");
    }

    public UpdateMemberRequest(long zuid, long targetId, int updateType, int memType) {
        this();
        build(zuid, targetId, updateType, memType);
    }

    private void build(long zuid, long targetId, int updateType, int memType) {
        VFansProto.UpdateGroupMemInfo info = VFansProto.UpdateGroupMemInfo.newBuilder()
                .setUuid(targetId)
                .setUpdateType(VFansProto.UpdateGroupMemType.valueOf(updateType))
                .setMemType(VFansCommonProto.GroupMemType.valueOf(memType))
                .build();
        VFansProto.UpdateGroupMemReq.Builder builder = VFansProto.UpdateGroupMemReq.newBuilder()
                .setZuid(zuid)
                .setAdminId(UserAccountManager.getInstance().getUuidAsLong())
                .setUpdateMemInfo(info);
        mRequest = builder.build();
    }

    @Override
    protected VFansProto.UpdateGroupMemRsp parse(byte[] bytes) throws InvalidProtocolBufferException {
        return VFansProto.UpdateGroupMemRsp.parseFrom(bytes);
    }
}
