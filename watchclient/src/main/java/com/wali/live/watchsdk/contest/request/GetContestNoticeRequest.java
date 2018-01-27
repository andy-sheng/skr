package com.wali.live.watchsdk.contest.request;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.api.request.BaseRequest;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.wali.live.proto.LiveSummitProto;

/**
 * Created by lan on 2018/1/11.
 */
public class GetContestNoticeRequest extends BaseRequest {
    public GetContestNoticeRequest(long zuid) {
        super(MiLinkCommand.COMMAND_GET_CONTEST_NOTICE, "getContestNotice");
        build(zuid);
    }

    private void build(long zuid) {
        mRequest = LiveSummitProto.GetContestNoticeReq.newBuilder()
                .setUuid(UserAccountManager.getInstance().getUuidAsLong())
                .setZuid(zuid)
                .build();
    }

    @Override
    protected LiveSummitProto.GetContestNoticeRsp parse(byte[] bytes) throws InvalidProtocolBufferException {
        return LiveSummitProto.GetContestNoticeRsp.parseFrom(bytes);
    }
}
