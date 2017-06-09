package com.wali.live.watchsdk.feeds;

import android.text.TextUtils;

import com.base.log.MyLog;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.milink.MiLinkClientAdapter;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.mi.live.data.milink.constant.MiLinkConstant;
import com.mi.milink.sdk.aidl.PacketData;
import com.wali.live.proto.Feeds;

/**
 * Created by yangli on 2017/6/1.
 *
 * @module feeds
 */
public class FeedsInfoUtils {
    private final static String TAG = "FeedsInfoUtils";

    /**
     * 从服务端获取一个FeedsInfo
     *
     * @param userId
     * @param feedId
     * @param isOnlyFocus 是否只拉关注的人
     */
    public static Feeds.GetFeedInfoResponse fetchFeedsInfo(
            long userId, String feedId, boolean isOnlyFocus, long ownerId) {
        MyLog.d(TAG, " fetchFeedsInfo userId=" + userId + " feedsId=" + feedId +
                " isOnlyFocus=" + isOnlyFocus + " ownerId=" + ownerId);
        if (userId <= 0 || TextUtils.isEmpty(feedId)) {
            return null;
        }

        Feeds.GetFeedInfoRequest req = Feeds.GetFeedInfoRequest.newBuilder()
                .setUserId(userId)
                .setFeedId(feedId)
                .setIsOnlyFocus(isOnlyFocus)
                .setFeedOwnerId(ownerId)
                .build();
        MyLog.d(TAG, "fetchFeedsInfo request : " + req.toString());
        PacketData data = new PacketData();
        data.setCommand(MiLinkCommand.COMMAND_FEEDS_GET_FEED_INFO);
        data.setData(req.toByteArray());

        PacketData rspData = MiLinkClientAdapter.getsInstance().sendSync(data, MiLinkConstant.TIME_OUT);
        if (rspData == null) {
            MyLog.w(TAG, "fetchFeedsInfo failed, rspData is null");
            return null;
        }

        try {
            Feeds.GetFeedInfoResponse rsp = Feeds.GetFeedInfoResponse.parseFrom(rspData.getData());
            MyLog.d(TAG, "fetchFeedsInfo rsp : " + rsp);
            return rsp;
        } catch (InvalidProtocolBufferException e) {
            MyLog.e(TAG, "fetchFeedsInfo failed, exception=" + e);
        }
        return null;
    }
}
