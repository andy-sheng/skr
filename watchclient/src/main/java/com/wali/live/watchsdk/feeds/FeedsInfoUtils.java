package com.wali.live.watchsdk.feeds;

import android.text.TextUtils;

import com.base.log.MyLog;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.account.MyUserInfoManager;
import com.mi.live.data.milink.MiLinkClientAdapter;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.mi.live.data.milink.constant.MiLinkConstant;
import com.mi.milink.sdk.aidl.PacketData;
import com.wali.live.proto.Feeds;

/**
 * Created by yangli on 2017/6/1.
 *
 * @module Feeds信息
 */
public class FeedsInfoUtils {
    private final static String TAG = "FeedsInfoUtils";

    public final static int UGC_TYPE_SMALL_VIDEO_WORKS = 6; // 作品

    public final static int FEED_TYPE_DEFAULT = 0; // 默认
    public final static int FEED_TYPE_SMALL_VIDEO = 1; // 小视频

    /**
     * 获取一个FeedsInfo
     *
     * @param feedId      feed ID
     * @param ownerId     feed作者ID
     * @param isOnlyFocus 是否只拉取关注人的赞
     */
    public static Feeds.GetFeedInfoResponse fetchFeedsInfo(
            String feedId,
            long ownerId,
            boolean isOnlyFocus) {
        if (TextUtils.isEmpty(feedId)) {
            return null;
        }

        Feeds.GetFeedInfoRequest req = Feeds.GetFeedInfoRequest.newBuilder()
                .setUserId(MyUserInfoManager.getInstance().getUuid())
                .setFeedId(feedId)
                .setFeedOwnerId(ownerId)
                .setIsOnlyFocus(isOnlyFocus)
                .build();
        PacketData data = new PacketData();
        data.setCommand(MiLinkCommand.COMMAND_FEEDS_GET_FEED_INFO);
        data.setData(req.toByteArray());

        MyLog.d(TAG, "fetchFeedsInfo request : " + req.toString());
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
