package com.wali.live.watchsdk.fans.request;

import android.text.TextUtils;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.api.request.BaseRequest;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.wali.live.proto.VFansProto;

/**
 * Created by lan on 2017/11/16.
 */
public class ApplyJoinGroupRequest extends BaseRequest {
    public ApplyJoinGroupRequest(long zuid, String applyMsg, String roomId) {
        super(MiLinkCommand.COMMAND_VFANS_APPLY_JOIN_GROUP, "applyJoinGroup");
        build(zuid, applyMsg, roomId);
    }

    private void build(long zuid, String applyMsg, String roomId) {
        VFansProto.ApplyJoinGroupReq.Builder builder = VFansProto.ApplyJoinGroupReq.newBuilder()
                .setUuid(UserAccountManager.getInstance().getUuidAsLong())
                .setZuid(zuid)
                .setApplyMsg(applyMsg);
        if (!TextUtils.isEmpty(roomId)) {
            builder.setRoomId(roomId);
        }
        mRequest = builder.build();
    }

    @Override
    protected VFansProto.ApplyJoinGroupRsp parse(byte[] bytes) throws InvalidProtocolBufferException {
        return VFansProto.ApplyJoinGroupRsp.parseFrom(bytes);
    }
}
