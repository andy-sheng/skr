package com.wali.live.watchsdk.component.utils;

import com.base.log.MyLog;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.milink.MiLinkClientAdapter;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.mi.milink.sdk.aidl.PacketData;
import com.wali.live.proto.RedEnvelProto;

/**
 * Created by yangli on 2017/7/14.
 *
 * @module 红包
 */
public class EnvelopeUtils {
    private static final String TAG = "EnvelopeUtils";

    /**
     * 同步抢红包
     */
    public static RedEnvelProto.GrabEnvelopRsp grabRedEnvelope(String redEnvelopeId) {
        RedEnvelProto.GrabEnvelopReq req = RedEnvelProto.GrabEnvelopReq.newBuilder()
                .setUserId(UserAccountManager.getInstance().getUuidAsLong())
                .setRedEnvelopId(redEnvelopeId)
                .build();
        PacketData data = new PacketData();
        data.setCommand(MiLinkCommand.COMMAND_GRAP_RED_ENVELOP);
        data.setData(req.toByteArray());
        MyLog.d(TAG, "grabRedEnvelope request:" + req.toString());
        PacketData response = MiLinkClientAdapter.getsInstance().sendSync(data, 10 * 1000);
        RedEnvelProto.GrabEnvelopRsp grabEnvelopRsp = null;
        try {
            grabEnvelopRsp = RedEnvelProto.GrabEnvelopRsp.parseFrom(response.getData());
            MyLog.d(TAG, "grabRedEnvelope response:" + grabEnvelopRsp);
        } catch (Exception e) {
        }
        return grabEnvelopRsp;
    }
}
