package com.wali.live.watchsdk.feeds;

import com.base.log.MyLog;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.milink.MiLinkClientAdapter;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.mi.live.data.milink.constant.MiLinkConstant;
import com.mi.milink.sdk.aidl.PacketData;
import com.wali.live.proto.Feeds;
import com.wali.live.watchsdk.feeds.model.FeedsCommentModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yangli on 2017/6/2.
 *
 * @author YangLi
 * @mail yanglijd@gmail.com
 */

public class FeedsCommentUtils {
    private final static String TAG = "FeedsCommentUtils";

    /**
     * 拉取一条feeds的评论
     *
     * @param feedId
     * @param ts
     * @param limit
     * @param onlyFocus
     * @param isAsc
     * @param type      //评论类型 1：热门评论  2：全部评论(type为0除去1)    0：代表老客户端拉取模式：热门和非热门评论混在一起，热门排在非热门的前面
     * @return
     */
    public static FeedsCommentList fetchFeedsCommentFromServer(
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

        MyLog.d(TAG + " fetchFeedsCommentFromServer request : \n" + request.toString());
        PacketData data = new PacketData();
        data.setCommand(MiLinkCommand.COMMAND_FEEDS_COMMENT_QUERY);
        data.setData(request.toByteArray());

        PacketData rspData = MiLinkClientAdapter.getsInstance().sendSync(data, MiLinkConstant.TIME_OUT);
        if (rspData == null) {
            MyLog.w(TAG, "fetchFeedsCommentFromServer failed, packet data is null");
            return null;
        }

        MyLog.v(TAG + " fetchFeedsCommentFromServer rsp : " + rspData.toString());
        try {
            Feeds.QueryFeedCommentsResponse rsp = Feeds.QueryFeedCommentsResponse.parseFrom(rspData.getData());
            MyLog.v(TAG + " fetchFeedsCommentFromServer rsp : " + rsp.toString());
            if (rsp != null && rsp.getErrCode() == 0) {
                long lastTs = rsp.getLastTs();
                Feeds.FeedComment feedComment = rsp.getFeedComment();
                if (feedComment == null) {
                    return null;
                } else {
                    List<Feeds.CommentInfo> commentInfoList = feedComment.getCommentInfosList();
                    if (commentInfoList == null) {
                        return null;
                    }
                    List<FeedsCommentModel.CommentInfo> comments = new ArrayList<FeedsCommentModel.CommentInfo>();
                    for (int i = 0; i < commentInfoList.size(); ++i) {
                        FeedsCommentModel.CommentInfo oneTmp = FeedsCommentModel.CommentInfo.obtain();
                        oneTmp.serialFromCommentInfoPb(commentInfoList.get(i));
                        comments.add(oneTmp);
                    }

                    return new FeedsCommentList(comments, rsp.getHasMore(), lastTs, feedComment.getTotal());
                }
            }
        } catch (InvalidProtocolBufferException e) {
            MyLog.e(TAG, e);
        }
        return null;
    }

    /**
     * 获取某个feedsInfo的评论的返回
     */
    public final static class FeedsCommentList {
        public List<FeedsCommentModel.CommentInfo> feedComments;
        public boolean hasMore;
        public long timestamp;
        public int commentsCount;

        public FeedsCommentList(
                List<FeedsCommentModel.CommentInfo> feedComments,
                boolean hasMore,
                long timestamp,
                int commentsCount) {
            this.feedComments = feedComments;
            this.hasMore = hasMore;
            this.timestamp = timestamp;
            this.commentsCount = commentsCount;
        }
    }
}
