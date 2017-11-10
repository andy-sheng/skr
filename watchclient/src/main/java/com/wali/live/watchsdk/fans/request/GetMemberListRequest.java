package com.wali.live.watchsdk.fans.request;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.api.request.BaseRequest;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.wali.live.proto.VFansCommonProto;
import com.wali.live.proto.VFansProto;

/**
 * Created by lan on 2017/11/7.
 */
public class GetMemberListRequest extends BaseRequest {
    public GetMemberListRequest() {
        super(MiLinkCommand.COMMAND_VFANS_MEM_LIST, "getMemberList");
    }

    public GetMemberListRequest(long zuid, int start, int limit) {
        this(zuid, start, limit, null, null);
    }

    public GetMemberListRequest(long zuid, int start, int limit, VFansCommonProto.MemRankType rankType) {
        this(zuid, start, limit, rankType, null);
    }

    public GetMemberListRequest(long zuid, int start, int limit, VFansCommonProto.MemRankType rankType, VFansCommonProto.RankDateType dateType) {
        this();
        build(zuid, start, limit, rankType, dateType);
    }

    private void build(long zuid, int start, int limit, VFansCommonProto.MemRankType rankType, VFansCommonProto.RankDateType dateType) {
        VFansProto.MemberListReq.Builder builder = VFansProto.MemberListReq.newBuilder()
                .setUuid(UserAccountManager.getInstance().getUuidAsLong())
                .setZuid(zuid)
                .setStart(start)
                .setLimit(limit);
        if (rankType != null) {
            builder.setRankType(rankType);
        }
        if (dateType != null) {
            builder.setDateType(dateType);
        }
        mRequest = builder.build();
    }

    @Override
    protected VFansProto.MemberListRsp parse(byte[] bytes) throws InvalidProtocolBufferException {
        return VFansProto.MemberListRsp.parseFrom(bytes);
    }
}
