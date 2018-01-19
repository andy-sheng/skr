package com.wali.live.watchsdk.contest.rank.request;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.api.request.BaseRequest;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.wali.live.proto.LiveSummitProto;

/**
 * Created by lan on 2018/1/11.
 */
public class ContestAllRankRequest extends BaseRequest {
    public ContestAllRankRequest() {
        super(MiLinkCommand.COMMAND_CONTEST_ALL_RANK, "contestAllRank");
        build();
    }

    private void build() {
        mRequest = LiveSummitProto.GetContestAllRankReq.newBuilder()
                .setUuid(UserAccountManager.getInstance().getUuidAsLong())
                .setLimit(100)
                .build();
    }

    @Override
    protected LiveSummitProto.GetContestAllRankRsp parse(byte[] bytes) throws InvalidProtocolBufferException {
        return LiveSummitProto.GetContestAllRankRsp.parseFrom(bytes);
    }
}
