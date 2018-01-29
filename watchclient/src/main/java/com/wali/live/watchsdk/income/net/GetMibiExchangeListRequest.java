package com.wali.live.watchsdk.income.net;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.api.request.BaseRequest;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.wali.live.proto.MibiTicketProto;
import com.wali.live.proto.PayProto;

/**
 * Created by rongzhisheng on 16-12-15.
 */

public class GetMibiExchangeListRequest extends BaseRequest {
    public GetMibiExchangeListRequest() {
        super(MiLinkCommand.COMMAND_INCOME_GET_EXCHANGE_MIBI_LIST, "getExchangeMibiList");
        mRequest = MibiTicketProto.GetMibiExchangeListRequest.newBuilder()
                .setUuid(UserAccountManager.getInstance().getUuidAsLong())
                .setPlatform(PayProto.Platform.ANDROID)
                .build();
    }

    @Override
    protected MibiTicketProto.GetMibiExchangeListResponse parse(byte[] bytes) throws InvalidProtocolBufferException {
        return MibiTicketProto.GetMibiExchangeListResponse.parseFrom(bytes);
    }
}
