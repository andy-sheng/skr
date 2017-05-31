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
 * Feeds点赞的 数据结构
 * Created by yaojian on 16-7-1.
 * @module feeds
 */
public class FeedsLikeModel implements JSONable {

    private final static String TAG = "FeedLikeModel";

    public List<FeedsLike> likelist = new ArrayList<FeedsLike>();      //点赞列表
    public int likeCount = 0;          //总的点赞人数
    public boolean myselfLike = false;          //自己是否点赞

    public FeedsLikeModel() {

    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("[");
        sb.append(" likeCount = " + likeCount);
        sb.append(" myselfLike = " + myselfLike);
        if (likelist == null || likelist.size() <= 0) {
            sb.append(" likelist = null");
        } else {
            for (int i = 0; i < likelist.size(); ++i) {
                FeedsLike tmp = likelist.get(i);
                sb.append(" The " + i + " feedLike : " + tmp.toString());
            }
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * 由feedInfo序列化FeedsLikeModel结构, FeedInfo由PB提供
     * @param feedInfo
     */
    public void serialFromFeedInfoPb(Feeds.FeedInfo feedInfo){
        if(feedInfo == null){
            return;
        }

        if(feedInfo.getFeedLikeContent() == null){
            return;
        }
        Feeds.FeedLikeContent feedLikeContent = feedInfo.getFeedLikeContent();
        this.likeCount = feedLikeContent.getLikeCount();
        this.myselfLike = feedLikeContent.getMyselfLike();
        List<Feeds.FeedLike> likeList = feedLikeContent.getFeedLikeListList();
        this.likelist = new ArrayList<FeedsLike>();
        if(likelist != null && likeList.size() > 0){
            for (Feeds.FeedLike like : likeList) {
                FeedsLike feedsLike = new FeedsLike();
                feedsLike.serialFromFeedLikePb(like);
                this.likelist.add(feedsLike);
            }
        }
    }

    public void clear() {
        if (likelist == null || likelist.size() <= 0) {
            likelist = new ArrayList<>();
        } else {
            for (FeedsLike like : likelist) {
                FeedsLike.bucket(like);
            }

            likelist = new ArrayList<>();
        }

        likeCount = -1;
        myselfLike = false;
    }

    //JSONable interface funcs begins *****************************

    @Override
    public JSONObject serialToJSON() {
        JSONObject result = new JSONObject();
        try {
            result.put("feedlikeinfo_likecount", this.likeCount);
            result.put("feedlikeinfo_myselfLike", this.myselfLike);
            if (likelist == null || likelist.size() <= 0) {
                JSONArray tmp = new JSONArray();
                result.put("feedlikeinfo_likelist", tmp);
            } else {
                JSONArray tmp = new JSONArray();
                for (int i = 0; i < likelist.size(); ++i) {
                    FeedsLike feedlike = likelist.get(i);
                    if (feedlike != null) {
                        JSONObject json = feedlike.serialToJSON();
                        if (json != null) {
                            tmp.put(json);
                        }
                    }
                }

                result.put("feedlikeinfo_likelist", tmp);
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
            this.likeCount = jsonObject.getInt("feedlikeinfo_likecount");
            this.myselfLike = jsonObject.getBoolean("feedlikeinfo_myselfLike");

            Object obj = jsonObject.get("feedlikeinfo_likelist");
            this.likelist = new ArrayList<FeedsLike>();
            if (obj != null && obj instanceof JSONArray) {
                JSONArray array = (JSONArray) obj;
                for (int i = 0; i < array.length(); ++i) {
                    Object item = array.get(i);
                    if (item != null && item instanceof JSONObject) {
                        FeedsLike like = new FeedsLike();
                        like.serialFromJSON((JSONObject) item);
                        this.likelist.add(like);
                    }
                }
            }

        } catch (JSONException e) {
            MyLog.e(TAG, e);
        }

    }


    /**
     * 一个赞信息
     */
    public static class FeedsLike implements JSONable {
        public long userId = 0;             //用户id
        public long timestamp = 0;          //赞的时间戳
        public String nickname = null;      //点赞用户的昵称
        public int certType = 0;            //认证类型

        private final static int MAX_CACHE_COUNT = 100;     //最大cache条数

        private static Vector<FeedsLike> sCache = new Vector<>(10);

        /**
         * 构造函数设置成私有, 是希望调用obtain函数得到一个FeedLike
         */
        private FeedsLike() {

        }

        /**
         * 由FeedLike序列化FeedsLike结构, FeedLike由protobuf提供
         * @param like
         */
        public void serialFromFeedLikePb(Feeds.FeedLike like){
            if(like == null){
                return;
            }

            userId = like.getZuid();
            timestamp = like.getTs();
            nickname = like.getUserName();
            //TODO 设置feedslike的认证 进入主页需要提前知道
            certType = 0;

        }

        /**
         * 　清除数据
         */
        public void clear() {
            userId = 0;
            timestamp = 0;
            certType = 0;
        }

        /**
         * 得到一个FeedLike对象
         *
         * @return
         */
        public static FeedsLike obtain() {
            if (sCache == null || sCache.size() <= 0) {
                return new FeedsLike();
            } else {
                FeedsLike item = sCache.remove(0);
                if (item == null) {
                    return new FeedsLike();
                } else {
                    item.clear();
                    return item;
                }
            }
        }

        /**
         * cache一个FeedLike对象
         */
        public static void bucket(FeedsLike feedLike) {
            if (feedLike == null) {
                return;
            }

            if (sCache == null || sCache.size() > MAX_CACHE_COUNT) {
                return;
            }

            feedLike.clear();
            sCache.add(feedLike);
            return;
        }

        //JSONable interface funcs begins *******************************

        @Override
        public void serialFromJSON(JSONObject jsonObject) {
            if (jsonObject == null) {
                return;
            }

            try {
                this.userId = jsonObject.getInt("feedlike_userid");
                this.timestamp = jsonObject.getLong("feedlike_timestamp");
                this.nickname = jsonObject.getString("feedlike_nickname");
                this.certType = jsonObject.getInt("feedlike_cert_type");
            } catch (JSONException e) {
                MyLog.e(TAG, e);
            }

        }

        @Override
        public JSONObject serialToJSON() {
            JSONObject result = new JSONObject();

            try {
                result.put("feedlike_userid", this.userId);
                result.put("feedlike_timestamp", this.timestamp);
                result.put("feedlike_nickname", this.nickname);
                result.put("feedlike_cert_type", this.certType);
            } catch (JSONException e) {
                MyLog.e(TAG, e);
            }

            return result;
        }

        //JSONable interface funcs ends **********************************
    }


}


