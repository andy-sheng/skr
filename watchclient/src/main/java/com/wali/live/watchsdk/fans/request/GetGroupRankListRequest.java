package com.wali.live.watchsdk.fans.request;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.api.request.BaseRequest;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.wali.live.proto.VFansCommonProto;
import com.wali.live.proto.VFansProto;

/**
 * Created by lan on 2017/11/29.
 */
public class GetGroupRankListRequest extends BaseRequest {
    public GetGroupRankListRequest(long zuid, int start, int limit, VFansCommonProto.RankDateType dateType) {
        super(MiLinkCommand.COMMAND_VFANS_GROUP_RANK_LIST, "getGroupRankList");
        build(zuid, start, limit, dateType);
    }

    private void build(long zuid, int start, int limit, VFansCommonProto.RankDateType dateType) {
        mRequest = VFansProto.GroupRankListReq.newBuilder()
                .setUuid(zuid)
                .setDateType(dateType)
                .setStart(start)
                .setLimit(limit)
                .build();
    }

    @Override
    protected VFansProto.GroupRankListRsp parse(byte[] bytes) throws InvalidProtocolBufferException {
        return VFansProto.GroupRankListRsp.parseFrom(bytes);
    }
}
