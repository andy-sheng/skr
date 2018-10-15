package com.wali.live.watchsdk.fans.request;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.api.request.BaseRequest;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.wali.live.proto.VFansProto;

/**
 * Created by lan on 2017/11/7.
 */
public class GetGroupListRequest extends BaseRequest {
    private static final int LIMIT_COUNT = 100;

    public GetGroupListRequest() {
        super(MiLinkCommand.COMMAND_VFANS_GROUP_LIST, "getGroupList");
        build(0, LIMIT_COUNT);
    }

    public GetGroupListRequest(int start) {
        super(MiLinkCommand.COMMAND_VFANS_GROUP_LIST, "getGroupList");
        build(start, LIMIT_COUNT);
    }

    public GetGroupListRequest(int start, int limit) {
        super(MiLinkCommand.COMMAND_VFANS_GROUP_LIST, "getGroupList");
        build(start, limit);
    }

    private void build(int start, int limit) {
        VFansProto.GetGroupListReq.Builder builder = VFansProto.GetGroupListReq.newBuilder()
                .setUuid(UserAccountManager.getInstance().getUuidAsLong())
                .setStart(start)
                .setLimit(limit);
        mRequest = builder.build();
    }

    @Override
    protected VFansProto.GetGroupListRsp parse(byte[] bytes) throws InvalidProtocolBufferException {
        return VFansProto.GetGroupListRsp.parseFrom(bytes);
    }
}
