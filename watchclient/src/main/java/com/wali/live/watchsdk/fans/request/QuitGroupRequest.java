package com.wali.live.watchsdk.fans.request;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.api.request.BaseRequest;
import com.wali.live.proto.VFansProto;

import static com.mi.live.data.milink.command.MiLinkCommand.COMMAND_VFANS_QUIT_GROUP;

/**
 * Created by lan on 2017/11/17.
 */
public class QuitGroupRequest extends BaseRequest {
    public QuitGroupRequest(long zuid) {
        super(COMMAND_VFANS_QUIT_GROUP, "quitGroup");
        build(zuid);
    }

    private void build(long zuid) {
        VFansProto.QuitGroupReq.Builder builder = VFansProto.QuitGroupReq.newBuilder()
                .setUuid(UserAccountManager.getInstance().getUuidAsLong())
                .setZuid(zuid);
        mRequest = builder.build();
    }

    @Override
    protected VFansProto.QuitGroupRsp parse(byte[] bytes) throws InvalidProtocolBufferException {
        return VFansProto.QuitGroupRsp.parseFrom(bytes);
    }
}
