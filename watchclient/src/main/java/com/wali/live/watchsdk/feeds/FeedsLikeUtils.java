package com.wali.live.watchsdk.feeds;

import android.text.TextUtils;

import com.base.log.MyLog;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.milink.MiLinkClientAdapter;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.mi.live.data.milink.constant.MiLinkConstant;
import com.mi.live.data.user.User;
import com.mi.milink.sdk.aidl.PacketData;
import com.wali.live.proto.Feeds;
import com.wali.live.watchsdk.feeds.model.IFeedsInfoable;

/**
 * 这个类控制着点赞的接口
 * Created by yaojian on 16-5-3.
 *
 * @module feeds
 */
public class FeedsLikeUtils {
    private final static String TAG = "FeedsLikeUtils";

    public final static int UGC_TYPE_SMALLVIDEO_WORKS = 6;          //作品

    public final static int FEED_TYPE_SMALL_VIDEO = 1;     // 1 小视频 feedType

    /**
     * 点赞
     *
     * @param user      点赞的用户
     * @param feedsInfo feedsInfo
     * @return
     */
    public static boolean likeFeeds(User user, IFeedsInfoable feedsInfo) {
        if (user == null) {
            return false;
        }

        if (feedsInfo == null) {
            return false;
        }

        Feeds.FeedLikeReq.Builder builder = Feeds.FeedLikeReq.newBuilder();
        if (user.getUid() > 0) {
            builder.setZuid(user.getUid());
        }
        if (!TextUtils.isEmpty(feedsInfo.getFeedsInfoId())) {
            builder.setFeedId(feedsInfo.getFeedsInfoId());
        }
        if (!TextUtils.isEmpty(feedsInfo.getOwnerUserNickName())) {
            builder.setUserName(feedsInfo.getOwnerUserNickName());
        }
        if (feedsInfo.getOwnerUserId() > 0) {
            builder.setFeedUserId(feedsInfo.getOwnerUserId());
        } else {
            builder.setFeedUserId(user.getUid());
        }
        if (feedsInfo.getFeedsContentType() == UGC_TYPE_SMALLVIDEO_WORKS) {
            builder.setFeedType(FEED_TYPE_SMALL_VIDEO);
        }

        Feeds.FeedLikeReq req = builder.build();
        MyLog.d(TAG + " likeFeeds request : \n" + req.toString());
        PacketData data = new PacketData();
        data.setCommand(MiLinkCommand.COMMAND_FEEDS_LIKE);
        data.setData(req.toByteArray());

        PacketData rspData = MiLinkClientAdapter.getsInstance().sendSync(data, MiLinkConstant.TIME_OUT);
        if (null == rspData) {
            MyLog.d(TAG, "likeFeeds failed, packetdata is null");
            return false;
        }

        MyLog.v(TAG + " likeFeeds rsp : " + rspData.toString());
        try {
            Feeds.FeedLikeRsp rsp = Feeds.FeedLikeRsp.parseFrom(rspData.getData());
            MyLog.v(TAG + " likeFeeds rsp : " + rsp.toString());
            if (rsp == null) {
                return false;
            } else {
                //成功
                return rsp.getRet() == 0;
            }
        } catch (InvalidProtocolBufferException e) {
            MyLog.e(TAG, e);
        }

        return false;
    }


    /**
     * 取消点赞
     *
     * @param user
     * @param feedsInfo
     * @return
     */
    public static boolean cancelLikeFeeds(User user, IFeedsInfoable feedsInfo) {
        if (user == null) {
            return false;
        }

        if (feedsInfo == null) {
            return false;
        }

        Feeds.FeedLikeDeleteReq.Builder builder = Feeds.FeedLikeDeleteReq.newBuilder();
        builder.setZuid(user.getUid());
        if (!TextUtils.isEmpty(feedsInfo.getFeedsInfoId())) {
            builder.setFeedId(feedsInfo.getFeedsInfoId());
        }
        if (feedsInfo.getOwnerUserId() > 0) {
            builder.setFeedUserId(feedsInfo.getOwnerUserId());
        }
        if (feedsInfo.getFeedsContentType() == UGC_TYPE_SMALLVIDEO_WORKS) {
            builder.setFeedType(FEED_TYPE_SMALL_VIDEO);
        }
        Feeds.FeedLikeDeleteReq req = builder.build();
        MyLog.d(TAG + " cancelLikeFeeds request : \n" + req.toString());
        PacketData data = new PacketData();
        data.setCommand(MiLinkCommand.COMMAND_FEEDS_CANCEL_LIKE);
        data.setData(req.toByteArray());

        PacketData rspData = MiLinkClientAdapter.getsInstance().sendSync(data, MiLinkConstant.TIME_OUT);

        if (null == rspData) {
            MyLog.d(TAG, "cancelLikeFeeds failed,packetdata is null");
            return false;
        }

        MyLog.v(TAG + " cancelLikeFeeds rsp : " + rspData.toString());
        try {
            Feeds.FeedLikeDeleteRsp rsp = Feeds.FeedLikeDeleteRsp.parseFrom(rspData.getData());
            MyLog.v(TAG + " cancelLikeFeeds rsp : " + rsp.toString());
            if (rsp == null) {
                return false;
            } else {
                //成功
                return rsp.getRet() == 0;
            }
        } catch (InvalidProtocolBufferException e) {
            MyLog.e(TAG, e);
        }

        return false;
    }

}
