package com.wali.live.watchsdk.income.net;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.api.request.BaseRequest;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.wali.live.proto.MibiTicketProto;

/**
 * Created by rongzhisheng on 16-12-15.
 */

public class GetMiAccessTokenByCodeRequest extends BaseRequest {
    public GetMiAccessTokenByCodeRequest(String code) {
        super(MiLinkCommand.COMMAND_OAUTH_GET_MI_ACCESS_TOKEN, "getMiAccessToken");
        mRequest = MibiTicketProto.GetMiAccessTokenByCodeReq.newBuilder()
                .setUuid(UserAccountManager.getInstance().getUuidAsLong())
                .setCode(code)
                .build();
    }

    @Override
    protected MibiTicketProto.GetMiAccessTokenByCodeRsp parse(byte[] bytes) throws InvalidProtocolBufferException {
        return MibiTicketProto.GetMiAccessTokenByCodeRsp.parseFrom(bytes);
    }
}
