package com.wali.live.livesdk.live.api;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.api.request.BaseRequest;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.wali.live.proto.RankProto;

/**
 * Created by wuxiaoshan on 17-4-26.
 */
public class GetRankListRequest extends BaseRequest {

    public GetRankListRequest() {
        super(MiLinkCommand.COMMAND_GET_RANK_LIST, "zhibo.rank.list");
        mRequest = RankProto.GetRankListRequest.newBuilder().setZuid(UserAccountManager.getInstance().getUuidAsLong()).setOffset(0).setLimit(3).build();

    }

    @Override
    protected RankProto.GetRankListResponse parse(byte[] bytes) throws InvalidProtocolBufferException {
        RankProto.GetRankListResponse response = RankProto.GetRankListResponse.parseFrom(bytes);
        return response;
    }
}
