package com.wali.live.watchsdk.channel.list.request;

import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.account.channel.HostChannelManager;
import com.mi.live.data.api.request.BaseRequest;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.wali.live.proto.LiveShowProto;

/**
 * Created by vera on 2018/5/22.
 */

public class ChannelListRequest extends BaseRequest {
    private static final int CHANNEL_VERSION = 0;
    private static final int APP_TYPE = 4;

    public ChannelListRequest(long fcId) {
        super(MiLinkCommand.COMMAND_LIST_CHANNEL, "ChannelListRequest");
        generateRequest(fcId);
    }

    private void generateRequest(long fcId) {
        mRequest = LiveShowProto.GetChannelsReq.newBuilder()
                .setFcId(fcId)
                .setAppType(getAppTypeByChannelId())
                .setChannelVersion(0)
                .build();
    }

    private static int getAppTypeByChannelId() {
        int channelId = HostChannelManager.getInstance().getChannelId();
        switch (channelId) {
            case 50010: {
                // 游戏中心
                return 4;
            }
            default:
                return 0;
        }
    }


    @Override
    protected GeneratedMessage parse(byte[] bytes) throws InvalidProtocolBufferException {
        return LiveShowProto.GetChannelsRsp.parseFrom(bytes);
    }
}
