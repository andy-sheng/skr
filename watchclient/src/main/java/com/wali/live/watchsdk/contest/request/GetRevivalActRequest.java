package com.wali.live.watchsdk.contest.request;

import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.api.request.BaseRequest;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.wali.live.proto.LiveSummitProto;

/**
 * Created by wanglinzhang on 2018/1/31.
 */

public class GetRevivalActRequest extends BaseRequest {
    public GetRevivalActRequest(String contestId) {
        super(MiLinkCommand.COMMAND_GET_REVIVAL_ACT, "GetRevivalAct");
        build(contestId);
    }

    private void build(String contestId) {
        mRequest = LiveSummitProto.GetRevivalActivityReq.newBuilder()
                .setUuid(UserAccountManager.getInstance().getUuidAsLong())
                .setContestId(contestId)
                .build();
    }

    @Override
    protected LiveSummitProto.GetRevivalActivityRsp parse(byte[] bytes) throws InvalidProtocolBufferException {
        return LiveSummitProto.GetRevivalActivityRsp.parseFrom(bytes);
    }
}
