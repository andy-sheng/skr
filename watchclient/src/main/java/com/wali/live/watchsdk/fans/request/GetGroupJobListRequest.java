package com.wali.live.watchsdk.fans.request;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.api.request.BaseRequest;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.wali.live.proto.VFansProto;

/**
 * Created by zyh on 2017/11/13.
 *
 * @module 获取粉丝群任务列表
 */
public class GetGroupJobListRequest extends BaseRequest {

    public GetGroupJobListRequest(long zuid) {
        super(MiLinkCommand.COMMAND_VFANS_JOB_LIST, "GetGroupJobListRequest");
        build(zuid);
    }

    private void build(long zuid) {
        VFansProto.GroupJobListReq.Builder builder = VFansProto.GroupJobListReq.newBuilder()
                .setZuid(zuid)
                .setUuid(UserAccountManager.getInstance().getUuidAsLong());
        mRequest = builder.build();
    }

    @Override
    protected VFansProto.GroupJobListRsp parse(byte[] bytes) throws InvalidProtocolBufferException {
        return VFansProto.GroupJobListRsp.parseFrom(bytes);
    }
}
