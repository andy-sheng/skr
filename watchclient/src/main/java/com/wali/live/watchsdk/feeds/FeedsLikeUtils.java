package com.wali.live.watchsdk.feeds;

import android.text.TextUtils;

import com.base.log.MyLog;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.account.MyUserInfoManager;
import com.mi.live.data.api.ErrorCode;
import com.mi.live.data.milink.MiLinkClientAdapter;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.mi.live.data.milink.constant.MiLinkConstant;
import com.mi.milink.sdk.aidl.PacketData;
import com.wali.live.proto.Feeds;

/**
 * Created by yangli on 2017/6/1.
 *
 * @module Feeds点赞
 */
public class FeedsLikeUtils {
    private final static String TAG = "FeedsLikeUtils";

    /**
     * 点赞
     *
     * @param feedId   feed ID
     * @param ownerId  feed作者ID
     * @param feedType feed类型，0-默认、1-小视频
     */
    public static boolean likeFeeds(
            String feedId,
            long ownerId,
            int feedType) {
        if (TextUtils.isEmpty(feedId)) {
            return false;
        }

        Feeds.FeedLikeReq.Builder builder = Feeds.FeedLikeReq.newBuilder()
                .setZuid(MyUserInfoManager.getInstance().getUuid())
                .setFeedId(feedId)
                .setFeedUserId(ownerId)
                .setFeedType(feedType);
        String nickname = MyUserInfoManager.getInstance().getNickname();
        if (!TextUtils.isEmpty(nickname)) {
            builder.setUserName(nickname);
        }
        Feeds.FeedLikeReq req = builder.build();
        PacketData data = new PacketData();
        data.setCommand(MiLinkCommand.COMMAND_FEEDS_LIKE);
        data.setData(req.toByteArray());

        MyLog.d(TAG, "likeFeeds request : " + req.toString());
        PacketData rspData = MiLinkClientAdapter.getsInstance().sendSync(data, MiLinkConstant.TIME_OUT);
        if (rspData == null) {
            MyLog.w(TAG, "likeFeeds failed, rspData is null");
            return false;
        }
        try {
            Feeds.FeedLikeRsp rsp = Feeds.FeedLikeRsp.parseFrom(rspData.getData());
            MyLog.d(TAG, "likeFeeds rsp : " + rsp.toString());
            return rsp != null && rsp.getRet() == ErrorCode.CODE_SUCCESS;
        } catch (InvalidProtocolBufferException e) {
            MyLog.e(TAG, "likeFeeds failed, exception=" + e);
        }
        return false;
    }

    /**
     * 取消点赞
     *
     * @param feedId   feed ID
     * @param ownerId  feed作者ID
     * @param feedType feed类型，0-默认、1-小视频
     */
    public static boolean cancelLikeFeeds(
            String feedId,
            long ownerId,
            int feedType) {
        if (TextUtils.isEmpty(feedId)) {
            return false;
        }

        Feeds.FeedLikeDeleteReq.Builder builder = Feeds.FeedLikeDeleteReq.newBuilder()
                .setZuid(MyUserInfoManager.getInstance().getUuid())
                .setFeedId(feedId)
                .setFeedUserId(ownerId)
                .setFeedType(feedType);
        Feeds.FeedLikeDeleteReq req = builder.build();
        PacketData data = new PacketData();
        data.setCommand(MiLinkCommand.COMMAND_FEEDS_CANCEL_LIKE);
        data.setData(req.toByteArray());

        MyLog.d(TAG, "cancelLikeFeeds request : " + req.toString());
        PacketData rspData = MiLinkClientAdapter.getsInstance().sendSync(data, MiLinkConstant.TIME_OUT);
        if (rspData == null) {
            MyLog.w(TAG, "cancelLikeFeeds failed, rspData is null");
            return false;
        }
        try {
            Feeds.FeedLikeDeleteRsp rsp = Feeds.FeedLikeDeleteRsp.parseFrom(rspData.getData());
            MyLog.d(TAG, "cancelLikeFeeds rsp : " + rsp.toString());
            return rsp != null && rsp.getRet() == ErrorCode.CODE_SUCCESS;
        } catch (InvalidProtocolBufferException e) {
            MyLog.e(TAG, "cancelLikeFeeds failed, exception=" + e);
        }
        return false;
    }
}
