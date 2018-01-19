package com.wali.live.watchsdk.contest.request;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.api.request.BaseRequest;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.wali.live.proto.LiveSummitProto;

/**
 * Created by lan on 2018/1/12.
 */
public class SetContestInviteCodeRequest extends BaseRequest {
    public SetContestInviteCodeRequest(String inviteCode) {
        super(MiLinkCommand.COMMAND_SET_CONTEST_INVITE_CODE, "SetContestInviteCode");
        build(inviteCode);
    }

    private void build(String inviteCode) {
        mRequest = LiveSummitProto.SetContestInviteCodeReq.newBuilder()
                .setUuid(UserAccountManager.getInstance().getUuidAsLong())
                .setInviteCode(inviteCode)
                .build();
    }

    @Override
    protected LiveSummitProto.SetContestInviteCodeRsp parse(byte[] bytes) throws InvalidProtocolBufferException {
        return LiveSummitProto.SetContestInviteCodeRsp.parseFrom(bytes);
    }
}
