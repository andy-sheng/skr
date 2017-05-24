package com.wali.live.pay.protocol;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.api.request.BaseRequest;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.wali.live.proto.PayProto;

/**
 * Created by rongzhisheng on 16-12-29.
 */
@Deprecated
public class GetGemPriceRequest extends BaseRequest {
    public GetGemPriceRequest() {
        super(MiLinkCommand.COMMAND_PAY_PRICE_LIST, "getGemPriceListV2", null);
        mRequest = PayProto.GetGemPriceRequest.newBuilder()
                .setPlatform(PayProto.Platform.ANDROID)
                .setUuid(UserAccountManager.getInstance().getUuidAsLong())
                .setChannel(null)//TODO
                .build();
    }

    @Override
    protected PayProto.GetGemPriceResponse parse(byte[] bytes) throws InvalidProtocolBufferException {
        return PayProto.GetGemPriceResponse.parseFrom(bytes);
    }
}
