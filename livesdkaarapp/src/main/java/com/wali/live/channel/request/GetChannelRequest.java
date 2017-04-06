package com.wali.live.channel.request;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.api.request.BaseRequest;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.wali.live.proto.HotChannelProto.GetRecommendListReq;
import com.wali.live.proto.HotChannelProto.GetRecommendListReq.Builder;
import com.wali.live.proto.HotChannelProto.GetRecommendListRsp;

/**
 * Created by lan on 16-3-18.
 *
 * @module 频道
 * @description 请求推荐频道的数据
 */
public class GetChannelRequest extends BaseRequest {
    public GetChannelRequest(long channelId) {
        super(MiLinkCommand.COMMAND_HOT_CHANNEL_LIST, "GetRecommendChannel", String.valueOf(channelId));
        generateRequest(channelId);
    }

    private Builder generateBuilder() {
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
