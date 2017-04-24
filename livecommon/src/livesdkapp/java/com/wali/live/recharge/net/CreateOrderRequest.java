package com.wali.live.recharge.net;

import android.support.annotation.NonNull;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.account.HostChannelManager;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.api.request.live.BaseLiveRequest;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.wali.live.pay.model.Diamond;
import com.wali.live.proto.PayProto;

/**
 * Created by rongzhisheng on 16-12-31.
 */

public class CreateOrderRequest extends BaseLiveRequest {

    public CreateOrderRequest(@NonNull Diamond goods, PayProto.PayType payType, PayProto.RChannel channel) {
        super(MiLinkCommand.COMMAND_PAY_CREATE_ORDER,"createOrder",null);
        mRequest = PayProto.CreateOrderRequest.newBuilder()
                .setUuid(UserAccountManager.getInstance().getUuidAsLong())
                .setPlatform(PayProto.Platform.ANDROID)
                .setGoodsId(goods.getId())
                .setGemCnt(goods.getCount())
                .setPrice(goods.getPrice())
                .setPayType(payType)
                .setChannel(channel)
                .setGiveGemCnt(goods.getExtraGive())
                .setAppChannel(String.valueOf(HostChannelManager.getInstance().getChannelId()))
                .setAppType(PayProto.AppType.ZHIBO_ZHUSHOU)
                .build();
    }

    @Override
    protected PayProto.CreateOrderResponse parse(byte[] bytes) throws InvalidProtocolBufferException {
        return PayProto.CreateOrderResponse.parseFrom(bytes);
    }
}
