package com.wali.live.watchsdk.income.net;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.api.request.BaseRequest;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.wali.live.proto.MibiTicketProto;

/**
 * Created by rongzhisheng on 16-12-15.
 */

public class RefreshMiAccessTokenByCodeRequest extends BaseRequest {
    public RefreshMiAccessTokenByCodeRequest(String refreshToken) {
        super(MiLinkCommand.COMMAND_OAUTH_REFRESH_MI_ACCESS_TOKEN, "refreshMiAccessToken");
        mRequest = MibiTicketProto.RefreshMiAccessTokenByCodeReq.newBuilder()
                .setUuid(UserAccountManager.getInstance().getUuidAsLong())
                .setRefreshToken(refreshToken)
                .build();
    }

    @Override
    protected MibiTicketProto.RefreshMiAccessTokenByCodeRsp parse(byte[] bytes) throws InvalidProtocolBufferException {
        return MibiTicketProto.RefreshMiAccessTokenByCodeRsp.parseFrom(bytes);
    }
}
