package com.wali.live.watchsdk.fans.request;

import android.text.TextUtils;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.api.request.BaseRequest;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.wali.live.proto.VFansCommonProto;
import com.wali.live.proto.VFansProto;

/**
 * Created by lan on 2017/11/17.
 */
public class FinishJobRequest extends BaseRequest {
    public FinishJobRequest(long zuid, VFansCommonProto.GroupJobType jobType, String roomId) {
        super(MiLinkCommand.COMMAND_VFANS_FINISH_GROUP_JOB, "finishJob");
        build(zuid, jobType, roomId);
    }

    private void build(long zuid, VFansCommonProto.GroupJobType jobType, String roomId) {
        VFansProto.FinishGroupJobReq.Builder builder = VFansProto.FinishGroupJobReq.newBuilder()
                .setUuid(UserAccountManager.getInstance().getUuidAsLong())
                .setZuid(zuid)
                .setJobType(jobType);
        if (!TextUtils.isEmpty(roomId)) {
            builder.setRoomId(roomId);
        }
        mRequest = builder.build();
    }

    @Override
    protected VFansProto.FinishGroupJobRsp parse(byte[] bytes) throws InvalidProtocolBufferException {
        return VFansProto.FinishGroupJobRsp.parseFrom(bytes);
    }
}
