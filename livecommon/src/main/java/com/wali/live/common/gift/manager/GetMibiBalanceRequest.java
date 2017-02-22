package com.wali.live.common.gift.manager;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.mi.live.data.api.request.live.BaseLiveRequest;
import com.wali.live.proto.GiftProto;

/**
 * Created by zjn on 16-10-17.
 */
public class GetMibiBalanceRequest extends BaseLiveRequest{

    public GetMibiBalanceRequest() {
        super(MiLinkCommand.COMMAND_MIBI_BALANCE, "GetMiBiBalance", null);
        mRequest = GiftProto.GetMibiBalanceRequest.newBuilder().setUuid(UserAccountManager.getInstance().getUuidAsLong()).build();
    }

    @Override
    protected GiftProto.GetMibiBalanceResponse parse(byte[] bytes) throws InvalidProtocolBufferException {
        return GiftProto.GetMibiBalanceResponse.parseFrom(bytes);
    }
}
