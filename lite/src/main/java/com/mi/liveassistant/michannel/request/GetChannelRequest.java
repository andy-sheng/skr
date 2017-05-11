package com.mi.liveassistant.michannel.request;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.liveassistant.account.UserAccountManager;
import com.mi.liveassistant.common.api.BaseRequest;
import com.mi.liveassistant.milink.command.MiLinkCommand;
import com.mi.liveassistant.proto.HotChannelProto.GetRecommendListReq;
import com.mi.liveassistant.proto.HotChannelProto.GetRecommendListRsp;

/**
 * Created by lan on 16-3-18.
 *
 * @module 频道
 * @description 请求推荐频道的数据
 */
public class GetChannelRequest extends BaseRequest {
    public GetChannelRequest(long channelId) {
        super(MiLinkCommand.COMMAND_HOT_CHANNEL_LIST, "GetRecommendChannel");
        generateRequest(channelId);
    }

    private GetRecommendListReq.Builder generateBuilder() {
        return GetRecommendListReq.newBuilder()
                .setUid(UserAccountManager.getInstance().getUuidAsLong());
    }

    private void generateRequest(long channelId) {
        mRequest = generateBuilder()
                .setChannelId((int) channelId)
                .build();
    }

    protected GetRecommendListRsp parse(byte[] bytes) throws InvalidProtocolBufferException {
        return GetRecommendListRsp.parseFrom(bytes);
    }
}
