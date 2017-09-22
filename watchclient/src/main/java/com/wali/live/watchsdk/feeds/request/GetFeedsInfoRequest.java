package com.wali.live.watchsdk.feeds.request;

import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.api.request.BaseRequest;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.wali.live.proto.Feeds.GetFeedInfoRequest;
import com.wali.live.proto.Feeds.GetFeedInfoResponse;

/**
 * Created by lan on 2017/9/19.
 */
public class GetFeedsInfoRequest extends BaseRequest {
    public GetFeedsInfoRequest(String feedId, boolean isOnlyFocus, long ownerId) {
        super(MiLinkCommand.COMMAND_FEEDS_GET_FEED_INFO, "GetFeedsInfo");
        generateRequest(feedId, isOnlyFocus, ownerId);
    }

    private GetFeedInfoRequest.Builder generateBuilder() {
        return GetFeedInfoRequest.newBuilder()
                .setUserId(UserAccountManager.getInstance().getUuidAsLong());
    }

    private void generateRequest(String feedId, boolean isOnlyFocus, long ownerId) {
        mRequest = generateBuilder()
                .setFeedId(feedId)
                .setIsOnlyFocus(isOnlyFocus)
                .setFeedOwnerId(ownerId)
                .build();
    }

    @Override
    protected GeneratedMessage parse(byte[] bytes) throws InvalidProtocolBufferException {
        return GetFeedInfoResponse.parseFrom(bytes);
    }
}
