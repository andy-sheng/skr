package com.wali.live.watchsdk.personalcenter.level.net;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.api.request.BaseRequest;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.wali.live.proto.Vip.VipProto;

/**
 * 获取用户VIP等级信息
 * Created by rongzhisheng on 17-4-25.
 */

public class VipHomePageRequest extends BaseRequest {

    public VipHomePageRequest() {
        super(MiLinkCommand.COMMAND_GET_VIP_INFO, "getVipInfo");
        mRequest = VipProto.VipHomePageReq.newBuilder()
                .setUuid(UserAccountManager.getInstance().getUuidAsLong())
                .build();
    }

    @Override
    protected VipProto.VipHomePageRsp parse(byte[] bytes) throws InvalidProtocolBufferException {
        return VipProto.VipHomePageRsp.parseFrom(bytes);
    }
}
