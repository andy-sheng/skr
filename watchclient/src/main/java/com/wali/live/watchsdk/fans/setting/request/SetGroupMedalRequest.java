package com.wali.live.watchsdk.fans.setting.request;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.api.request.BaseRequest;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.wali.live.proto.VFansCommonProto;
import com.wali.live.proto.VFansProto;

/**
 * Created by lan on 2017/11/24.
 */
public class SetGroupMedalRequest extends BaseRequest {
    public SetGroupMedalRequest(long zuid, int level, String medal) {
        super(MiLinkCommand.COMMAND_VFAN_SET_GROUP_MEDAL, "setGroupMedal");
        build(zuid, level, medal);
    }

    private void build(long zuid, int level, String medal) {
        VFansCommonProto.GroupMedalInfo groupMedalInfo = VFansCommonProto.GroupMedalInfo.newBuilder()
                .setLevel(level)
                .setMedalValue(medal)
                .build();
        mRequest = VFansProto.SetGroupMedalReq.newBuilder()
                .setZuid(zuid)
                .setAdminId(UserAccountManager.getInstance().getUuidAsLong())
                .addMedalList(groupMedalInfo)
                .build();
    }

    @Override
    protected VFansProto.SetGroupMedalRsp parse(byte[] bytes) throws InvalidProtocolBufferException {
        return VFansProto.SetGroupMedalRsp.parseFrom(bytes);
    }
}
