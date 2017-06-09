package com.wali.live.watchsdk.feeds;

import android.content.Context;
import android.text.ClipboardManager;
import android.text.TextUtils;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.api.ErrorCode;
import com.mi.live.data.milink.MiLinkClientAdapter;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.mi.live.data.milink.constant.MiLinkConstant;
import com.mi.milink.sdk.aidl.PacketData;
import com.wali.live.common.smiley.SmileyParser;
import com.wali.live.proto.Feeds;
import com.wali.live.watchsdk.videodetail.adapter.DetailCommentAdapter;

import static com.wali.live.watchsdk.feeds.FeedsLikeUtils.FEED_TYPE_SMALL_VIDEO;
import static com.wali.live.watchsdk.feeds.FeedsLikeUtils.UGC_TYPE_SMALLVIDEO_WORKS;

/**
 * Created by yangli on 2017/6/2.
 *
 * @module feeds
 */
public class FeedsCommentUtils {
    private final static String TAG = "FeedsCommentUtils";

    public final static int FEEDS_COMMENT_PULL_TYPE_ALL_HYBIRD = 0;     //代表老客户端拉取模式：热门和非热门评论混在一起，热门排在非热门的前面
    public final static int FEEDS_COMMENT_PULL_TYPE_HOT = 1;        //热门评论
    public final static int FEEDS_COMMENT_PULL_TYPE_ALL_EXCLUSIVE_HOT = 2;    //全部评论, type0除去type1

    public static void copyToClipboard(CharSequence str, boolean addSpan) {
        if (!TextUtils.isEmpty(str)) {
            final ClipboardManager clip = (ClipboardManager) GlobalData.app()
                    .getSystemService(Context.CLIPBOARD_SERVICE);
            if (addSpan) {
                str = SmileyParser.getInstance().addSmileySpans(
                        GlobalData.app(), str, 0);
            }
            clip.setText(str);
        }
    }

    /**
     * 拉取一条feeds的评论
     *
     * @param feedId
     * @param ts
     * @param limit
     * @param onlyFocus
     * @param isAsc
     * @param type      //评论类型 1：热门评论  2：全部评论(type为0除去1)    0：代表老客户端拉取模式：热门和非热门评论混在一起，热门排在非热门的前面
     */
    public static Feeds.QueryFeedCommentsResponse fetchFeedsComment(
            String feedId, long ts, int limit, boolean onlyFocus, boolean isAsc, int type, boolean includeShare) {
        Feeds.QueryFeedCommentsRequest request = Feeds.QueryFeedCommentsRequest.newBuilder()
                .setFeedId(feedId)
                .setTs(ts)
                .setLimit(limit)
                .setIsOnlyFocus(onlyFocus)
                .setIsAsc(isAsc)
                .setType(type)
                .setIsAddSgc(includeShare)
                .build();

        MyLog.d(TAG, "fetchFeedsComment request : " + request.toString());
        PacketData data = new PacketData();
        data.setCommand(MiLinkCommand.COMMAND_FEEDS_COMMENT_QUERY);
        data.setData(request.toByteArray());

        PacketData rspData = MiLinkClientAdapter.getsInstance().sendSync(data, MiLinkConstant.TIME_OUT);
        if (rspData == null) {
            MyLog.w(TAG, "fetchFeedsComment failed, rspData is null");
            return null;
        }

        try {
            Feeds.QueryFeedCommentsResponse rsp = Feeds.QueryFeedCommentsResponse.parseFrom(rspData.getData());
            MyLog.d(TAG, "fetchFeedsComment rsp : " + rsp);
            return rsp;
        } catch (InvalidProtocolBufferException e) {
            MyLog.e(TAG, "fetchFeedsComment failed, exception=" + e);
        }
        return null;
    }

    /**
     * 创建一条评论
     */
    public static DetailCommentAdapter.CommentItem sendComment(
            DetailCommentAdapter.CommentItem commentItem,
            long feedOwnerId,
            String feedId,
            int feedType,
            int commentType) {
        if (commentItem == null || TextUtils.isEmpty(commentItem.content)) {
            return null;
        }

        Feeds.CreateFeedCommnetRequest.Builder builder = Feeds.CreateFeedCommnetRequest.newBuilder();
        builder.setFromUid(commentItem.fromUid);
        builder.setFromNickname(commentItem.fromNickName);
        builder.setContent(SmileyParser.getInstance()
                .convertString(commentItem.content, SmileyParser.TYPE_LOCAL_TO_GLOBAL).toString());
        builder.setFeedId(feedId);
        builder.setFeedOwnerId(feedOwnerId);
        if (commentItem.toUid > 0) {
            builder.setToUid(commentItem.toUid);
        }
        if (!TextUtils.isEmpty(commentItem.toNickName)) {
            builder.setToNickname(commentItem.toNickName);
        }
        if (feedType != 0) {
            builder.setFeedType(feedType);
        }
        if (commentType != 0) {
            builder.setCommentType(commentType);
        }

        Feeds.CreateFeedCommnetRequest req = builder.build();
        MyLog.d(TAG, "sendComment request : \n" + req.toString());
        PacketData data = new PacketData();
        data.setCommand(MiLinkCommand.COMMAND_FEEDS_COMMENT_CREATE);
        data.setData(req.toByteArray());

        PacketData rspData = MiLinkClientAdapter.getsInstance().sendSync(data, MiLinkConstant.TIME_OUT);
        if (null == rspData) {
            MyLog.e(TAG, "sendComment failed, packetData is null");
            return null;
        }

        MyLog.v(TAG, "sendComment rsp : " + rspData.toString());
        try {
            Feeds.CreateFeedCommnetResponse rsp = Feeds.CreateFeedCommnetResponse.parseFrom(rspData.getData());
            MyLog.v(TAG, "sendComment rsp : " + rsp.toString());
            if (rsp != null && rsp.getErrCode() == ErrorCode.CODE_SUCCESS) {
                commentItem.commentId = rsp.getCommnetId();
                commentItem.createTime = rsp.getCreateTime();
                return commentItem;
            }
        } catch (InvalidProtocolBufferException e) {
            MyLog.e(TAG, e);
        }
        return null;
    }

    /**
     * 删除服务端　的一条　评论
     */
    public static boolean deleteComment(
            DetailCommentAdapter.CommentItem commentItem,
            long feedOwnerId,
            String feedId,
            int feedType) {
        if (commentItem == null || TextUtils.isEmpty(feedId)) {
            MyLog.w(TAG, "deleteComment commentItem or feedId is null");
            return false;
        }

        Feeds.DeleteFeedCommnetRequest.Builder builder = Feeds.DeleteFeedCommnetRequest.newBuilder()
                .setFromUid((int) commentItem.fromUid)
                .setCommnetId((int) commentItem.commentId)
                .setFeedId(feedId)
                .setOwnerId(feedOwnerId);
        if (feedType == UGC_TYPE_SMALLVIDEO_WORKS) {
            builder.setFeedType(FEED_TYPE_SMALL_VIDEO);
        }
        Feeds.DeleteFeedCommnetRequest request = builder.build();
        MyLog.d(TAG, "deleteComment request : " + request.toString());
        PacketData data = new PacketData();
        data.setCommand(MiLinkCommand.COMMAND_FEEDS_COMMENT_DELETE);
        data.setData(request.toByteArray());

        PacketData rspData = MiLinkClientAdapter.getsInstance().sendSync(data, MiLinkConstant.TIME_OUT);
        if (null == rspData) {
            MyLog.e(TAG, "deleteComment failed, packetData is null");
            return false;
        }

        try {
            Feeds.DeleteFeedCommnetResponse rsp = Feeds.DeleteFeedCommnetResponse.parseFrom(rspData.getData());
            if (rsp == null) {
                MyLog.v(TAG, "deleteComment rsp == null");
                return false;
            } else {
                MyLog.v(TAG, "deleteComment rsp : " + rsp.toString());
                return rsp.getErrCode() == 0;
            }
        } catch (InvalidProtocolBufferException e) {
            MyLog.e(TAG, e);
        }

        return false;
    }
}
