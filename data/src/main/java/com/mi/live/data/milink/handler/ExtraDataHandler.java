package com.mi.live.data.milink.handler;

import com.base.log.MyLog;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.account.event.TouristLoginEvent;
import com.mi.live.data.milink.MiLinkClientAdapter;
import com.mi.live.data.milink.callback.MiLinkPacketDispatcher;
import com.mi.live.data.milink.event.MiLinkEvent;
import com.mi.milink.sdk.aidl.PacketData;
import com.mi.milink.sdk.data.Const;
import com.mi.milink.sdk.proto.DataExtraProto;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by chengsimin on 16/7/1.
 */
public class ExtraDataHandler implements MiLinkPacketDispatcher.PacketDataHandler {

    private static final java.lang.String TAG = "ExtraDataHandler'";

    @Override
    public boolean processPacketData(PacketData data) {
        switch (data.getCommand()) {
            case Const.DATA_CLIENTIP_EXTRA_CMD: {
                try {
                    DataExtraProto.DataClientIp dataExtra = DataExtraProto.DataClientIp.parseFrom(data.getData());
                    String clientIp = dataExtra.getClientIp();
                    MyLog.w(TAG, "client ip is " + clientIp);
                    MiLinkClientAdapter.getsInstance().setClientIp(clientIp);
                } catch (InvalidProtocolBufferException e) {
                    MyLog.e(TAG, e);
                }
            }
            break;
            case Const.MnsCmd.MNS_MILINK_PUSH_LOG: {
                // 请求上传日志
                MyLog.w(TAG, "recv milink.push.log,post upload eventbus");
                EventBus.getDefault().post(new MiLinkEvent.RequestUploadLog());
            }
            break;
            case Const.DATA_ANONYMOUSWID_EXTRA_CMD: {
                // 匿名通道的用户id
                MyLog.w(TAG, "DATA_ANONYMOUSWID_EXTRA_CMD");
                try {
                    DataExtraProto.DataAnonymousWid dataExtra = DataExtraProto.DataAnonymousWid.parseFrom(data.getData());
                    long anonymousId = dataExtra.getWid();
                    UserAccountManager.getInstance().setAnonymousId(anonymousId);
                    MyLog.w(TAG, "anonymousId:" + anonymousId);
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }
            }
            break;
            case Const.DATA_CHANNEL_ANONYMOUSWID_EXTRA_CMD: {
                // 通道模式的用户id
                // TODO 但要注册 PacketDispatch 才有可能被回调
                MyLog.w(TAG, "DATA_CHANNEL_ANONYMOUSWID_EXTRA_CMD");
                try {
                    DataExtraProto.DataAnonymousWid dataExtra = DataExtraProto.DataAnonymousWid.parseFrom(data.getData());
                    long anonymousId = dataExtra.getWid();
                    UserAccountManager.getInstance().setAnonymousId(anonymousId);
                    EventBus.getDefault().post(new TouristLoginEvent());
                    MyLog.w(TAG, "channel anonymousId:" + anonymousId);
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }
            }
            break;
        }

        return true;
    }

    @Override
    public String[] getAcceptCommand() {
        return new String[]{
                Const.DATA_CLIENTIP_EXTRA_CMD,
                Const.MnsCmd.MNS_MILINK_PUSH_LOG,
                Const.DATA_ANONYMOUSWID_EXTRA_CMD,
                Const.DATA_CHANNEL_ANONYMOUSWID_EXTRA_CMD
        };
    }
}
