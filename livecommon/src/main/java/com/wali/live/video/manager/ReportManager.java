package com.wali.live.video.manager;

import android.text.TextUtils;

import com.base.log.MyLog;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.api.ErrorCode;
import com.mi.live.data.milink.MiLinkClientAdapter;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.mi.live.data.milink.constant.MiLinkConstant;
import com.mi.milink.sdk.aidl.PacketData;
import com.wali.live.proto.ReportProto;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by liuyanyan on 16/3/26.
 */
public class ReportManager {

    private static final String TAG = "ReportManager";
    public static final int TYPE_PUSH_STREAM = 1;//推流
    public static final int TYPE_PULL_STREAM = 2;//拉流
    public static final int TYPE_FIRST_FRAME_DELAY = 3;//观看端首帧延时
    public static final int TYPE_PUSH_SUCCESS = 4;//推流启动成功
    public static final int TYPE_PUSH_FAIL = 5;//推流启动失败
    public static final int TYPE_PULL_SUCCESS = 6;//拉流启动成功
    public static final int TYPE_PUL_FAIL = 7;//拉流启动失败
    public static final int TYPE_NET = 1;//网宿
    public static final int TYPE_OTHER = 2;//金山云

    /*
    * zhibo.report.delay
    * */
    public static boolean reportStreamDelay(int type, String streamName, String streamIp, int delayTime) {
        if (TextUtils.isEmpty(streamName) || TextUtils.isEmpty(streamIp) || delayTime < 0) {
            MyLog.e(TAG, " getStreamRsp paras invalid streamName = " + streamName + " streamIp = " + streamIp);
            return false;
        }

        ReportProto.StreamDelayRequest req = ReportProto.StreamDelayRequest.newBuilder()
                .setZuid(UserAccountManager.getInstance().getUuidAsLong())
                .setType(type)
                .setStreamName(streamName)
                .setStreamIp(streamIp)
                .setDelayTime(delayTime)
                .build();
        PacketData data = new PacketData();
        data.setCommand(MiLinkCommand.COMMAND_DELAY_REPORT);
        data.setData(req.toByteArray());
        MyLog.d(TAG, " getSteamDelayReq = \n" + req.toString());

        PacketData rspData = MiLinkClientAdapter.getsInstance().sendSync(data, MiLinkConstant.TIME_OUT);
        if (null != rspData) {
            try {
                ReportProto.StreamDelayResponse rsp = ReportProto.StreamDelayResponse.parseFrom(rspData.getData());
                MyLog.d(TAG, " getSteamDelayRsp = \n" + rsp.toString());
                // TODO: 16/3/26 如有需要在此处理其他返回码
                return rsp.getRetCode() == ErrorCode.CODE_SUCCESS;
            } catch (InvalidProtocolBufferException e) {
                MyLog.e(TAG, e);
            }
        } else {
            MyLog.d(TAG, " getStreamDelayRsp rspData is null ");
        }
        return false;
    }

    /*
    * zhibo.ipselect.query
    * */
    public static List<String> getQueryIpRsp(String ip, String url, int type) {
        if (TextUtils.isEmpty(ip) || TextUtils.isEmpty(url)) {
            MyLog.e(TAG, " getQueryIpRsp paras invalid");
            return null;
        }
        ReportProto.QueryIpReq req = ReportProto.QueryIpReq.newBuilder()
                .setIp(ip)
                .setUrl(url)
                .setUuid(UserAccountManager.getInstance().getUuidAsLong())
                .setType(type)
                .build();
        PacketData data = new PacketData();
        data.setCommand(MiLinkCommand.COMMAND_IP_SELECT_QUERY);

        data.setData(req.toByteArray());
        MyLog.d(TAG, " getQueryIpReq = \n" + req.toString());

        PacketData rspData = MiLinkClientAdapter.getsInstance().sendSync(data, MiLinkConstant.TIME_OUT);
        if (null != rspData) {
            try {
                ReportProto.QueryIpRsp rsp = ReportProto.QueryIpRsp.parseFrom(rspData.getData());
                MyLog.d(TAG, " getQueryIpRsp = \n" + rsp.toString());

                if (rsp.getRet() == ErrorCode.CODE_SUCCESS) {
                    List<String> ipList = new ArrayList<>();
                    for (int i = 0; i < rsp.getIpListCount(); i++) {
                        if (!TextUtils.isEmpty(rsp.getIpList(i))) {
                            ipList.add(rsp.getIpList(i));
                        }
                    }
                    if (!ipList.isEmpty()) {
                        return ipList;
                    }
                }
            } catch (InvalidProtocolBufferException e) {
                MyLog.e(TAG, e);
            }
        } else {
            MyLog.d(TAG, " getQueryIpRsp is null");
        }
        return null;
    }

}
