package com.wali.live.pay.protocol;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.api.request.live.BaseLiveRequest;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.wali.live.proto.PayProto;

/**
 * @module 充值
 * Created by rongzhisheng on 16-8-31.
 */
public class QueryBalanceDetailRequest extends BaseLiveRequest {
    private static final String TAG = QueryBalanceDetailRequest.class.getSimpleName();

    @Override
    protected String getTag() {
        return TAG;
    }

    public QueryBalanceDetailRequest() {
        super(MiLinkCommand.COMMAND_PAY_GET_BALANCE_DETAIL,"GetBalanceDetail",null);
        mRequest = PayProto.QueryBalanceDetailRequest.newBuilder()
                .setUuid(UserAccountManager.getInstance().getUuidAsLong())
                .setPlatform(PayProto.Platform.ANDROID)
                .build();
    }

    @Override
    protected PayProto.QueryBalanceDetailResponse parse(byte[] bytes) throws InvalidProtocolBufferException {
        return PayProto.QueryBalanceDetailResponse.parseFrom(bytes);
    }
}
