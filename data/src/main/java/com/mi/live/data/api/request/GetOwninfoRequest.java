package com.mi.live.data.api.request;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.wali.live.proto.UserProto;

/**
 * Created by wuxiaoshan on 17-3-6.
 */
public class GetOwninfoRequest extends BaseRequest {
    public GetOwninfoRequest(long uuid) {
        super(MiLinkCommand.COMMAND_GET_OWN_INFO, "getowninfo", null);
        mRequest = UserProto.GetOwnInfoReq.newBuilder().setZuid(uuid).build();
    }

    @Override
    protected UserProto.GetOwnInfoRsp parse(byte[] bytes) throws InvalidProtocolBufferException {
        if (bytes != null) {
            return UserProto.GetOwnInfoRsp.parseFrom(bytes);
        }
        return null;
    }
}
