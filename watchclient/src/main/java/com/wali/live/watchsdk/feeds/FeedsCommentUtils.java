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
import com.wali.live.proto.Live2Proto;
import com.wali.live.watchsdk.videodetail.adapter.DetailCommentAdapter;

import static com.wali.live.watchsdk.feeds.FeedsInfoUtils.FEED_TYPE_DEFAULT;

/**
 * Created by yangli on 2017/6/2.
 *
 * @module Feeds评论
 */
public class FeedsCommentUtils {
    private final static String TAG = "FeedsCommentUtils";

    public final static int PULL_TYPE_ALL_HYBRID = 0; // 代表老客户端拉取模式：热门和非热门评论混在一起，热门排在非热门的前面
    public final static int PULL_TYPE_HOT = 1;        // 热门评论
    public final static int PULL_TYPE_ALL_EXCLUSIVE_HOT = 2; // 全部评论, type0除去type1

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
     * 拉取feeds的评论
     *
     * @param feedId       feed ID
     * @param ts           时间戳
     * @param limit        拉取数量上限，最大100
     * @param onlyFocus    true-只拉取关注人的评论；false-拉取所有人的评论(默认)
     * @param isAsc        true-拉取ts之后的评论，按时间增序；false-拉取ts之前的评论，按时间逆序
     * @param type         拉取的评论类型，{@link #PULL_TYPE_ALL_HYBRID}、{@link #PULL_TYPE_HOT}、{@link #PULL_TYPE_ALL_EXCLUSIVE_HOT}
     * @param includeShare 是否加上分享产生的评论
     */
    public static Feeds.QueryFeedCommentsResponse fetchFeedsComment(
            String feedId,
            long ts,
            int limit,
            boolean onlyFocus,
            boolean isAsc,
            int type,
            boolean includeShare) {
        if (TextUtils.isEmpty(feedId)) {
            return null;
        }
        Feeds.QueryFeedCommentsRequest request = Feeds.QueryFeedCommentsRequest.newBuilder()
                .setFeedId(feedId)
                .setTs(ts)
                .setLimit(limit)
                .setIsOnlyFocus(onlyFocus)
                .setIsAsc(isAsc)
                .setType(type)
                .setIsAddSgc(includeShare)
                .build();
        PacketData data = new PacketData();
        data.setCommand(MiLinkCommand.COMMAND_FEEDS_COMMENT_QUERY);
        data.setData(request.toByteArray());

        MyLog.d(TAG, "fetchFeedsComment request : " + request.toString());
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
     * 创建评论
     *
     * @param commentItem 评论信息
     * @param feedId      feed ID
     * @param ownerId     feed作者ID
     * @param feedType    feed类型，0-默认、1-小视频
     * @param commentType 评论类型，0-默认、1-分享被动评论
     */
    public static DetailCommentAdapter.CommentItem sendComment(
            DetailCommentAdapter.CommentItem commentItem,
            String feedId,
            long ownerId,
            int feedType,
            int commentType) {
        if (commentItem == null || TextUtils.isEmpty(commentItem.content)
                || TextUtils.isEmpty(feedId)) {
            return null;
        }

        Feeds.CreateFeedCommnetRequest.Builder builder = Feeds.CreateFeedCommnetRequest.newBuilder()
                .setFromUid(commentItem.fromUid)
                .setFromNickname(commentItem.fromNickName)
                .setContent(SmileyParser.getInstance().convertString(
                        commentItem.content, SmileyParser.TYPE_LOCAL_TO_GLOBAL).toString())
                .setFeedId(feedId)
                .setFeedOwnerId(ownerId);
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
        PacketData data = new PacketData();
        data.setCommand(MiLinkCommand.COMMAND_FEEDS_COMMENT_CREATE);
        data.setData(req.toByteArray());

        MyLog.d(TAG, "sendComment request : " + req.toString());
        PacketData rspData = MiLinkClientAdapter.getsInstance().sendSync(data, MiLinkConstant.TIME_OUT);
        if (rspData == null) {
            MyLog.e(TAG, "sendComment failed, packetData is null");
            return null;
        }
        try {
            Feeds.CreateFeedCommnetResponse rsp = Feeds.CreateFeedCommnetResponse.parseFrom(rspData.getData());
            MyLog.d(TAG, "sendComment rsp : " + rsp.toString());
            if (rsp != null && rsp.getErrCode() == ErrorCode.CODE_SUCCESS) {
                commentItem.commentId = rsp.getCommnetId();
                commentItem.createTime = rsp.getCreateTime();
                return commentItem;
            }
        } catch (InvalidProtocolBufferException e) {
            MyLog.e(TAG, "sendComment failed, exception=" + e);
        }
        return null;
    }

    /**
     * 删除评论
     *
     * @param commentItem 评论信息
     * @param feedId      feed ID
     * @param ownerId     feed作者ID
     * @param feedType    feed类型，0-默认、1-小视频
     */
    public static boolean deleteComment(
            DetailCommentAdapter.CommentItem commentItem,
            String feedId,
            long ownerId,
            int feedType) {
        if (commentItem == null || TextUtils.isEmpty(feedId)) {
            MyLog.w(TAG, "deleteComment commentItem or feedId is null");
            return false;
        }

        Feeds.DeleteFeedCommnetRequest.Builder builder = Feeds.DeleteFeedCommnetRequest.newBuilder()
                .setFromUid((int) commentItem.fromUid)
                .setCommnetId((int) commentItem.commentId)
                .setFeedId(feedId)
                .setOwnerId(ownerId);
        if (feedType != FEED_TYPE_DEFAULT) {
            builder.setFeedType(feedType);
        }
        Feeds.DeleteFeedCommnetRequest request = builder.build();
        PacketData data = new PacketData();
        data.setCommand(MiLinkCommand.COMMAND_FEEDS_COMMENT_DELETE);
        data.setData(request.toByteArray());

        MyLog.d(TAG, "deleteComment request : " + request.toString());
        PacketData rspData = MiLinkClientAdapter.getsInstance().sendSync(data, MiLinkConstant.TIME_OUT);
        if (null == rspData) {
            MyLog.e(TAG, "deleteComment failed, packetData is null");
            return false;
        }
        try {
            Feeds.DeleteFeedCommnetResponse rsp = Feeds.DeleteFeedCommnetResponse.parseFrom(rspData.getData());
            MyLog.d(TAG, "deleteComment rsp : " + rsp);
            return rsp != null && rsp.getErrCode() == ErrorCode.CODE_SUCCESS;
        } catch (InvalidProtocolBufferException e) {
            MyLog.e(TAG, "deleteComment failed, exception=" + e);
        }
        return false;
    }

    /**
     * 查询回放列表
     */
    public static Live2Proto.HistoryLiveRsp getHistoryShowList(long uuid, long zuid) {
        Live2Proto.HistoryLiveReq request = Live2Proto.HistoryLiveReq.newBuilder().setUuid(uuid).setZuid(zuid).build();
        PacketData packetData = new PacketData();
        packetData.setCommand(MiLinkCommand.COMMAND_LIST_HISTORY);
        packetData.setData(request.toByteArray());
        MyLog.w(TAG, "getHistoryShowList request : \n" + request.toString());
        PacketData responseData = MiLinkClientAdapter.getsInstance().sendSync(packetData, MiLinkConstant.TIME_OUT);
        if (responseData == null) {
            MyLog.w(TAG, "getHistoryShowList failed, packet data is null");
            return null;
        }
        try {
            Live2Proto.HistoryLiveRsp response = Live2Proto.HistoryLiveRsp.parseFrom(responseData.getData());
            MyLog.v(TAG, "getHistoryShowList responseCode: \n" + response.getRetCode());
            if (response != null && response.getRetCode() == 0) {
                return response;
            }
        } catch (InvalidProtocolBufferException e) {
            MyLog.e(e);
        }
        return null;
    }
}
