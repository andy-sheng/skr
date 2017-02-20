package com.mi.live.data.api;

import com.base.log.MyLog;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.milink.MiLinkClientAdapter;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.mi.live.data.milink.constant.MiLinkConstant;
import com.mi.milink.sdk.aidl.PacketData;
import com.wali.live.proto.LiveProto;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yurui on 3/1/16.
 */
public class BanSpeakerUtils {

    private static final String TAG = BanSpeakerUtils.class.getSimpleName();
    public static final int RESULT_CODE_SUCCESS = 0; //表示成功的返回码

    /*
    *
    * 房间id     liveId
    * 主播id     zuid
    * 操作者id   uuid
    * 要禁言id   banUuid
    *
    */
    public static boolean banSpeaker(String liveId, long zuid, long uuid, long banUuid) {
        LiveProto.SetBanSpeakerReq request = LiveProto.SetBanSpeakerReq.newBuilder().setLiveId(liveId).setZuid(zuid).setUuid(uuid).addBanSpeaker(banUuid).build();
        PacketData packetData = new PacketData();
        packetData.setCommand(MiLinkCommand.COMMAND_BAN_SPEAKER);
        packetData.setData(request.toByteArray());
        PacketData responseData = MiLinkClientAdapter.getsInstance().sendSync(packetData, MiLinkConstant.TIME_OUT);
        MyLog.v(TAG + " banSpeaker:" + responseData);
        try {
            if (responseData != null) {
                LiveProto.SetBanSpeakerRsp response = LiveProto.SetBanSpeakerRsp.parseFrom(responseData.getData());
                MyLog.v(TAG + " banSpeaker result:" + response.getRetCode());//0:表示成功 5001:房间不存在 5004:参数错误​ 5005:操作人无权限 5006:观众不存在
                return response.getRetCode() == RESULT_CODE_SUCCESS;
            }
        } catch (InvalidProtocolBufferException e) {
            MyLog.e(e);
        }
        return false;
    }

    //产品定义只有主播能取消禁言
    public static boolean cancelBanSpeaker(String liveId, long zuid, long banUuid) {
        LiveProto.CancelBanSpeakerReq request = LiveProto.CancelBanSpeakerReq.newBuilder().setLiveId(liveId).setZuid(zuid).addBanSpeaker(banUuid).build();
        PacketData packetData = new PacketData();
        packetData.setCommand(MiLinkCommand.COMMAND_CANCEL_BAN_SPEAKER);
        packetData.setData(request.toByteArray());
        PacketData responseData = MiLinkClientAdapter.getsInstance().sendSync(packetData, MiLinkConstant.TIME_OUT);
        MyLog.v(TAG + " cancelBanSpeaker:" + responseData);
        try {
            if (responseData != null) {
                LiveProto.CancelBanSpeakerRsp response = LiveProto.CancelBanSpeakerRsp.parseFrom(responseData.getData());
                MyLog.v(TAG + " cancelBanSpeaker result:" + response.getRetCode());//0:表示成功 5001:房间不存在 5004:参数错误​ 5005:操作人无权限 5006:观众不存在
                return response.getRetCode() == RESULT_CODE_SUCCESS;
            }
        } catch (InvalidProtocolBufferException e) {
            MyLog.e(e);
        }
        return false;
    }

    //得到禁言列表
    public static List<Long> getBanSpeakerList(long uuid, long zuid, String liveId) {
        LiveProto.GetLiveKeyPersonInfoReq request = LiveProto.GetLiveKeyPersonInfoReq.newBuilder().setUuid(uuid).setZuid(zuid).setLiveId(liveId).build();
        PacketData packetData = new PacketData();
        packetData.setCommand(MiLinkCommand.COMMAND_GET_BANSPEAKER_LIST);
        packetData.setData(request.toByteArray());
        PacketData responseData = MiLinkClientAdapter.getsInstance().sendSync(packetData, MiLinkConstant.TIME_OUT);
        MyLog.v(TAG + " cancelBanSpeaker:" + responseData);
        try {
            if (responseData != null) {
                LiveProto.GetLiveKeyPersonInfoRsp response = LiveProto.GetLiveKeyPersonInfoRsp.parseFrom(responseData.getData());
                MyLog.v(TAG + " getBanSpeakerList result:" + response.getRetCode());
                if (response.getBanSpeakerList() != null) {
                    return response.getBanSpeakerList();
                }
            }
        } catch (InvalidProtocolBufferException e) {
            MyLog.e(e);
        }
        return new ArrayList<Long>();
    }
}
