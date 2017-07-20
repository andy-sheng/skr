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

    /**
     * 获取抢红包结果详细列表
     */
    public static RedEnvelProto.GetEnvelopRsp getRedEnvelope(String redEnvelopeId, String roomId, long timeStamp) {
        RedEnvelProto.GetEnvelopReq req = RedEnvelProto.GetEnvelopReq.newBuilder()
                .setUserId(UserAccountManager.getInstance().getUuidAsLong())
                .setRedEnvelopId(redEnvelopeId)
                .build();
        PacketData data = new PacketData();
        data.setCommand(MiLinkCommand.COMMAND_GET_RED_ENVELOP);
        data.setData(req.toByteArray());
        MyLog.d(TAG, "getRedEnvelope request:" + req.toString());
        PacketData response = MiLinkClientAdapter.getsInstance().sendSync(data, 10 * 1000);
        RedEnvelProto.GetEnvelopRsp getEnvelopRsp = null;
        try {
            getEnvelopRsp = RedEnvelProto.GetEnvelopRsp.parseFrom(response.getData());
            MyLog.d(TAG, "getRedEnvelope response:" + getEnvelopRsp);
        } catch (Exception e) {
        }
        return getEnvelopRsp;
    }

    /**
     * 发红包
     */
    public static RedEnvelProto.CreateRedEnvelopRsp createRedEnvelope(
            long anchorId, String roomId, int viewerCnt, int gemCnt, String msg) {
        RedEnvelProto.CreateRedEnvelopReq req = RedEnvelProto.CreateRedEnvelopReq.newBuilder()
                .setUserId(UserAccountManager.getInstance().getUuidAsLong())
                .setZuid(anchorId)
                .setRoomId(roomId)
                .setViewerCnt(viewerCnt)
                .setGemCnt(gemCnt)
                .setMsg(msg)
                .setClientId(String.valueOf(System.currentTimeMillis()))
                .setPlatform(RedEnvelProto.Platform.ANDROID)
                .build();
        PacketData data = new PacketData();
        data.setCommand(MiLinkCommand.COMMAND_CREATE_REDENVELOP);
        data.setData(req.toByteArray());
        MyLog.v(TAG, "grabRedEnvelope request:" + req.toString());
        PacketData response = MiLinkClientAdapter.getsInstance().sendSync(data, 10 * 1000);
        RedEnvelProto.CreateRedEnvelopRsp createRedEnvelopRsp = null;
        try {
            createRedEnvelopRsp = RedEnvelProto.CreateRedEnvelopRsp.parseFrom(response.getData());
            MyLog.v(TAG, "grabRedEnvelope response:" + createRedEnvelopRsp);
        } catch (Exception e) {
        }
        return createRedEnvelopRsp;
    }
}
