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

public class ExchangeMibiRequest extends BaseRequest {
    public ExchangeMibiRequest(int exchangeId, int mibiCount, int mibiTicketCount, int giveMibiCount, String accessToken) {
        super(MiLinkCommand.COMMAND_INCOME_EXCHANGE_MIBI, "exchangeMibi");
        mRequest = MibiTicketProto.ExchangeMibiRequest.newBuilder()
                .setUuid(UserAccountManager.getInstance().getUuidAsLong())
                .setClientId(System.currentTimeMillis())
                .setExchangeId(exchangeId)
                .setMibiCnt(mibiCount)
                .setMibiTicketCnt(mibiTicketCount)
                .setGiveMibiCnt(giveMibiCount)
                .setAccessToken(accessToken)
                .setPlatform(PayProto.Platform.ANDROID)
                .build();
    }

    @Override
    protected MibiTicketProto.ExchangeMibiResponse parse(byte[] bytes) throws InvalidProtocolBufferException {
        return MibiTicketProto.ExchangeMibiResponse.parseFrom(bytes);
    }
}
