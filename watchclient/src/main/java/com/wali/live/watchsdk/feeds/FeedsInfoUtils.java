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
    public static Feeds.GetFeedInfoResponse fetchFetchInfoFromServer(long userId, String feedId, boolean isOnlyFocus, long ownerId) {
        MyLog.v(TAG + " fetchOneFetchInfoFromServer userId == " + userId + " feedsId == " + feedId + " isOnlyFocus == " + isOnlyFocus + " ownerId == " + ownerId);

        if (userId <= 0) {
            return null;
        }

        if (TextUtils.isEmpty(feedId)) {
            return null;
        }

        Feeds.GetFeedInfoRequest.Builder builder = Feeds.GetFeedInfoRequest.newBuilder();
        builder.setUserId(userId).setFeedId(feedId).setIsOnlyFocus(isOnlyFocus).setFeedOwnerId(ownerId);

        Feeds.GetFeedInfoRequest req = builder.build();
        MyLog.d(TAG + " fetchOneFetchInfoFromServer request : \n" + req.toString());
        PacketData data = new PacketData();
        data.setCommand(MiLinkCommand.COMMAND_FEEDS_GET_FEED_INFO);
        data.setData(req.toByteArray());

        PacketData rspData = MiLinkClientAdapter.getsInstance().sendSync(data, MiLinkConstant.TIME_OUT);
        if (null == rspData) {
            MyLog.d(TAG, "fetchOneFetchInfoFromServer failed,packetdata is null");
            return null;
        }
        MyLog.v(TAG + " fetchOneFetchInfoFromServer rsp : " + rspData.toString());

        try {
            Feeds.GetFeedInfoResponse rsp = Feeds.GetFeedInfoResponse.parseFrom(rspData.getData());
            if (rsp != null) {
                MyLog.v(TAG + " fetchOneFetchInfoFromServer rsp : " + rsp.toString());
            } else {
                MyLog.v(TAG + " fetchOneFetchInfoFromServer rsp == null");
            }
            return rsp;
        } catch (InvalidProtocolBufferException e) {
            MyLog.e(TAG, e);
        }
        return null;
    }
}
