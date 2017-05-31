package com.wali.live.watchsdk.feeds.model;

import com.base.log.MyLog;
import com.mi.live.data.api.JSONable;
import com.wali.live.proto.Feeds;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * feeds　评论
 * Created by yaojian on 16-7-1.
 *
 * @module feeds
 */
public class FeedsCommentModel implements JSONable {

    private final static String TAG = "FeedsCommentModel";

    public List<CommentInfo> comments = new ArrayList<>();
    public int commentCount = 0;            //评论总数

    /**
     * 由FeedInfo序列化FeedsCommentModel结构, FeedInfo由protobuf提供
     *
     * @param feedInfo
     */
    public void serialFromFeedInfoPb(Feeds.FeedInfo feedInfo) {
        if (feedInfo == null) {
            return;
        }

        Feeds.FeedAbstractComment comment = feedInfo.getFeedAbstractComment();
        if (comment == null) {
            return;
        }

        commentCount = comment.getCommentNumbers();
        comments = new ArrayList<CommentInfo>();

        if (comment.getCommentList() != null && comment.getCommentList().size() > 0) {
            List<Feeds.CommentInfo> commentList = comment.getCommentList();
            for (Feeds.CommentInfo commentInfo : commentList) {
                CommentInfo feedsCommentInfo = new CommentInfo();
                feedsCommentInfo.serialFromCommentInfoPb(commentInfo);
                comments.add(feedsCommentInfo);
            }
        }


    }


    @Override
    public JSONObject serialToJSON() {
        JSONObject result = new JSONObject();
        try {
            result.put("feedcommentinfo_commentcount", this.commentCount);
            if (comments != null && comments.size() > 0) {
                JSONArray jsonArray = new JSONArray();
                for (int i = 0; i < comments.size(); ++i) {
                    CommentInfo tmp = comments.get(i);
                    if (tmp != null) {
                        JSONObject jo = tmp.serialToJSON();
                        if (jo != null) {
                            jsonArray.put(jo);
                        }
                    }
                }
                result.put("feedcommentinfo_commentslist", jsonArray);
            } else {
                JSONArray jsonArray = new JSONArray();
                result.put("feedcommentinfo_commentslist", jsonArray);
            }

            return result;

        } catch (JSONException e) {
            MyLog.e(TAG, e);
        }

        return result;
    }

    @Override
    public void serialFromJSON(JSONObject jsonObject) {
        if (jsonObject == null) {
            return;
        }

        try {
            this.commentCount = jsonObject.getInt("feedcommentinfo_commentcount");
            Object obj = jsonObject.get("feedcommentinfo_commentslist");
            if (obj != null && obj instanceof JSONArray) {
                JSONArray jsonArray = (JSONArray) obj;
                if (jsonArray == null || jsonArray.length() <= 0) {
                    this.comments = new ArrayList<CommentInfo>();
                } else {
                    this.comments = new ArrayList<CommentInfo>();
                    for (int i = 0; i < jsonArray.length(); ++i) {
                        Object tmp = jsonArray.get(i);
                        if (tmp != null && tmp instanceof JSONObject) {
                            CommentInfo comment = new CommentInfo();
                            comment.serialFromJSON((JSONObject) tmp);
                            this.comments.add(comment);
                        }
                    }
                }
            } else {
                this.comments = new ArrayList<CommentInfo>();
            }
        } catch (JSONException e) {
            MyLog.e(TAG, e);
        }
    }

    /**
     * 一条feed 评论, 对应着PB里CommentInfo数据结构
     */
    public static class CommentInfo implements JSONable {
        public static final int MSG_SHARE = 1;
        public static final int MSG_NORMAL = 0;
        public long commentId = 0;          //评论id
        public long fromUid = 0;            //
        public String content = null;              //评论内容
        public long createTimestamp = 0;            //创建时间戳
        public long toUid = 0;              //
        public String fromNickName = null;
        public String toNickName = null;
        public long indexTs;       //排序时间搓
        public boolean isGoodType; //是否是热门评论
        public int fromUserLevel; //评论作者等级
        public long fromAvatar; // 用户头像
        public int commentType; // 0 默认 1 分享被动评论

        private final static int MAX_CACHE_COUNT = 50;

        public static Vector<CommentInfo> sCache = new Vector<>(10);

        /**
         * 构造函数设置成私有, 是希望调用obtain函数得到一个FeedComment
         */
        public CommentInfo() {

        }

        /**
         * 由CommentInfo序列化, 传进来的参数CommentInfo由Pb提供
         *
         * @param commentInfo
         */
        public void serialFromCommentInfoPb(Feeds.CommentInfo commentInfo) {
            if (commentInfo == null) {
                return;
            }
            commentId = commentInfo.getCommentId();
            fromUid = commentInfo.getFromUid();
            content = commentInfo.getContent();
            createTimestamp = commentInfo.getCreateTime();
            toUid = commentInfo.getToUid();
            fromNickName = commentInfo.getFromNickname();
            toNickName = commentInfo.getToNickname();
            indexTs = commentInfo.getIndexTs();
            isGoodType = commentInfo.getIsGood();
            fromUserLevel = commentInfo.getFromUserLevel();
            fromAvatar = commentInfo.getFromAvatar();
            commentType = commentInfo.getCommentType();
        }


        /**
         * 　清除数据
         */
        public void clear() {
            commentId = 0;
            fromUid = 0;
            toUid = 0;
            content = null;
            createTimestamp = 0;
            indexTs = 0;
            isGoodType = false;
            fromUserLevel = 0;
            fromAvatar = 0;
            commentType = 0;
        }

        /**
         * 得到一个FeedLike对象
         *
         * @return
         */
        public static CommentInfo obtain() {
            if (sCache == null || sCache.size() <= 0) {
                return new CommentInfo();
            } else {
                CommentInfo item = sCache.remove(0);
                if (item == null) {
                    return new CommentInfo();
                } else {
                    item.clear();
                    return item;
                }
            }
        }

        /**
         * cache一个FeedLike对象
         */
        public static void bucket(CommentInfo feedComment) {
            if (feedComment == null) {
                return;
            }

            if (sCache == null || sCache.size() > MAX_CACHE_COUNT) {
                return;
            }

            feedComment.clear();
            sCache.add(feedComment);
            return;
        }


        //JSONable interface funcs begins *******************************

        @Override
        public JSONObject serialToJSON() {
            JSONObject result = new JSONObject();

            try {
                result.put("feedcomment_commentid", this.commentId);
                result.put("feedcomment_fromuid", this.fromUid);
                result.put("feedcomment_content", this.content);
                result.put("feedcomment_createtime", this.createTimestamp);
                result.put("feedcomment_touid", this.toUid);
                result.put("feedcomment_fromNickName", this.fromNickName);
                result.put("feedcomment_toNickName", this.toNickName);
                result.put("feedcomment_indexTs", this.indexTs);
                result.put("feedcomment_isGood", this.isGoodType);
                result.put("feedcomment_fromUserLevel", this.fromUserLevel);
                result.put("feedcomment_fromAvatar",this.fromAvatar);
                result.put("feedcomment_commentType",this.commentType);
            } catch (JSONException e) {
                MyLog.e(TAG, e);
            }

            return result;
        }

        @Override
        public void serialFromJSON(JSONObject jsonObject) {
            if (jsonObject == null) {
                return;
            }

            try {
                this.commentId = jsonObject.getLong("feedcomment_commentid");
                this.fromUid = jsonObject.getLong("feedcomment_fromuid");
                this.content = jsonObject.getString("feedcomment_content");
                this.createTimestamp = jsonObject.getLong("feedcomment_createtime");
                this.toUid = jsonObject.getLong("feedcomment_touid");
                this.fromNickName = jsonObject.getString("feedcomment_fromNickName");
                this.toNickName = jsonObject.getString("feedcomment_toNickName");
                this.indexTs = jsonObject.optInt("feedcomment_indexTs", 0);
                this.isGoodType = jsonObject.optBoolean("feedcomment_isGood");
                this.fromUserLevel = jsonObject.optInt("feedcomment_fromUserLevel");
                this.fromAvatar = jsonObject.getLong("feedcomment_fromAvatar");
                this.commentType = jsonObject.getInt("feedcomment_commentType");
            } catch (JSONException e) {
                MyLog.e(TAG, e);
            }

        }

        //JSONable interface funcs ends *********************************

    }
}
