package com.wali.live.recharge.net;

import android.text.TextUtils;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.api.request.BaseRequest;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.wali.live.proto.PayProto;

/**
 * Created by rongzhisheng on 17-2-8.
 */

public class CheckOrderRequest extends BaseRequest {
    public CheckOrderRequest(String orderId, String payId, String receipt, String transactionId) {
        super(MiLinkCommand.COMMAND_PAY_CHECK_ORDER, "checkOrder");
        PayProto.CheckOrderRequest.Builder reqBuilder = PayProto.CheckOrderRequest.newBuilder()
                .setUuid(UserAccountManager.getInstance().getUuidAsLong())
                .setPlatform(PayProto.Platform.ANDROID)
                .setOrderId(orderId);
        if (!TextUtils.isEmpty(payId)) {
            reqBuilder.setPuid(payId);
        }
        if (!TextUtils.isEmpty(receipt)) {
            reqBuilder.setReceipt(receipt);
        }
        if (!TextUtils.isEmpty(transactionId)) {
            reqBuilder.setTransactionId(transactionId);
        }
        mRequest = reqBuilder.build();
    }

    @Override
    protected PayProto.CheckOrderResponse parse(byte[] bytes) throws InvalidProtocolBufferException {
        return PayProto.CheckOrderResponse.parseFrom(bytes);
    }
}
