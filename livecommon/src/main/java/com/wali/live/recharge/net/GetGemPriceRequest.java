package com.wali.live.recharge.net;

import android.support.annotation.NonNull;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.api.request.BaseRequest;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.wali.live.proto.PayProto;

/**
 * Created by rongzhisheng on 17-1-1.
 */

public class GetGemPriceRequest extends BaseRequest {
    {
        mCommand = MiLinkCommand.COMMAND_PAY_PRICE_LIST;
        mAction = "getGemPriceListV2";
    }

    public GetGemPriceRequest(@NonNull PayProto.RChannel channel) {
        super(MiLinkCommand.COMMAND_PAY_PRICE_LIST, "getGemPriceListV2", null);
        mRequest = PayProto.GetGemPriceRequest.newBuilder()
                .setPlatform(PayProto.Platform.ANDROID)
                .setUuid(UserAccountManager.getInstance().getUuidAsLong())
                .setChannel(channel)
                .build();
    }

    @Override
    protected PayProto.GetGemPriceResponse parse(byte[] bytes) throws InvalidProtocolBufferException {
        return PayProto.GetGemPriceResponse.parseFrom(bytes);
    }
}
