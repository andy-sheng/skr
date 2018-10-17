package com.wali.live.modulechannel.api;

import com.common.core.account.UserAccountManager;
import com.common.core.channel.HostChannelManager;
import com.common.log.MyLog;
import com.common.milink.MiLinkClientAdapter;
import com.common.milink.command.MiLinkCommand;
import com.common.milink.constant.MiLinkConstant;
import com.mi.milink.sdk.aidl.PacketData;
import com.wali.live.proto.HotChannel.GetRecommendListReq;
import com.wali.live.proto.HotChannel.GetRecommendListRsp;
import com.wali.live.proto.LiveShow.GetChannelsReq;
import com.wali.live.proto.LiveShow.GetChannelsRsp;

import io.reactivex.Observable;

/**
 * Created by zhujianning on 18-10-17.
 */

public class ChannelDataStore {

    public static final String TAG = ChannelDataStore.class.getSimpleName();

    private static PacketData sendToMiLinkClient(byte[] data, String command) {
        PacketData packetData = new PacketData();
        packetData.setCommand(command);
        packetData.setData(data);
        return MiLinkClientAdapter.getInstance().sendSync(packetData, MiLinkConstant.TIME_OUT);
    }

    public Observable<GetRecommendListRsp> getHotChannelObservable(final long channelId) {
        return Observable.create(emitter -> {
            GetRecommendListReq req = new GetRecommendListReq.Builder().setUid(UserAccountManager.getInstance().getUuidAsLong()).setChannelId((int) channelId).build();
            PacketData rspData = sendToMiLinkClient(req.toByteArray(), MiLinkCommand.COMMAND_HOT_CHANNEL_LIST);
            if(rspData != null) {
                GetRecommendListRsp rsp = GetRecommendListRsp.parseFrom(rspData.getData());
                if(rsp != null) {
                    if(rsp.getRetCode() != 0) {
                        emitter.onError(new Exception(String.format("GetRecommendListRsp retCode = %d", rsp.getRetCode())));
                    } else {
                        MyLog.d(TAG, "getChannelListObservable rsp= " + rsp.toString());
                        emitter.onNext(rsp);
                        emitter.onComplete();
                    }
                } else {
                    emitter.onError(new Exception("getChannelListObservable is null"));
                }

            } else {
                emitter.onError(new Exception("GetRecommendListRsp rspData is null"));
            }
        });
    }

    public Observable<GetChannelsRsp> getChannelListObservable(final long fcId) {

        return Observable.create(emitter -> {
            GetChannelsReq.Builder builder = new GetChannelsReq(fcId, getAppTypeByChannelId(), 0).newBuilder();
            GetChannelsReq req = builder.build();
            PacketData rspData = sendToMiLinkClient(req.toByteArray(), MiLinkCommand.COMMAND_LIST_CHANNEL);
            if(rspData != null) {
                GetChannelsRsp rsp = GetChannelsRsp.parseFrom(rspData.getData());
                if(rsp != null) {
                    if(rsp.getRet() != 0) {
                        emitter.onError(new Exception(String.format("GetChannelsRsp retCode = %d", rsp.getRet())));
                    } else {
                        MyLog.d(TAG, "getChannelListObservable rsp= " + rsp.toString());
                        emitter.onNext(rsp);
                        emitter.onComplete();
                    }
                } else {
                    emitter.onError(new Exception("getChannelListObservable rsp is null"));
                }
            } else {
                emitter.onError(new Exception("getChannelListObservable rspData is null"));
            }
        });
    }

    private static int getAppTypeByChannelId() {
        int channelId = HostChannelManager.getInstance().getChannelId();
        switch (channelId) {
            case 50001: {
                // 测试demo，测试音乐的
                return 5;
            }
            case 50019: {
                // 小米音乐
                return 5;
            }
            case 50010: {
                // 小米音乐 测试
                return 4;
            }
            default:
                // 默认小米直播
                return 0;
        }
    }
}
