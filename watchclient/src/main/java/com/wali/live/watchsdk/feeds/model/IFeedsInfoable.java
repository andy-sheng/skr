package com.wali.live.watchsdk.feeds.model;
import com.wali.live.watchsdk.feeds.model.FeedsCommentModel;
import com.wali.live.watchsdk.feeds.model.FeedsLikeModel;

import java.util.List;

/**
 * 一个接口, 代表对FeedsInfo的操作
 * Created by yaojian on 16-7-1.
 * @module feeds
 */
public interface IFeedsInfoable {

    /**
     * 获取feeds　info type
     */
    long getFeedsInfoType();

    /**
     * 获取feeds content type, 比如直播, 小视频
     */
    int getFeedsContentType();

    /**
     * 获取直播类型, 公开:0 私密:1 回放:2 pk:3 口令:4
     */
    int getLiveShowType();

    /**
     * 获取回放类型，公开:0 私密:1 口令:2 门票:3
     */
    int getReplayType();

    /**
     * 获取feedsInfo id
     */
    String getFeedsInfoId();

    /**
     * 自己是否点赞
     * @return 自己点赞返回true, 否则返回false
     */
    boolean isSelfLike();

    public void setSelfLike(boolean isLike);
    /**
     * 返回点赞个数
     */
    int getLikeCount();

    /**
     * 得到评论总数
     */
    int getCommentCount();

    /**
     * 返回feedsinfo创建时间
     */
    long getCreateTimestamp();

    /**
     * 得到 feeds 所有人的userid
     */
    long getOwnerUserId();

    /**
     * 得到feeds owner昵称
     */
    String getOwnerUserNickName();

    /**
     * 得到地理位置
     */
    String getLocation();

    /**
     * 返回点赞列表
     */
    List<FeedsLikeModel.FeedsLike> getAllLike();

    /**
     * 返回评论的列表
     */
    List<FeedsCommentModel.CommentInfo> getAllComments();

    /**
     * 得到用户的认证类型
     */
    int getOwnerCertType();

    /**
     * 得到用户的等级
     */
    int getOwnerUserLevel();

    /**
     * 得到用户的性别
     */
    int getOwnerUserGender();

    /**
     * 得到用户的性别
     */
    String getOwnerUserSign();

    /**
     * 得到 封面的url
     */
    String getCoverUrl();

    /**
     * 返回gif真实地址
     */
    String getUrl();

    /**
     * 得到回放合集包含的回放数
     */
    int getBackShowCount();

    /**
     * 得到feeds标题
     */
    String getFeedsTitle();

    /**
     * 得到观看人数
     */
    int getFeedsViewerCount();

    /**
     * 得到clientId
     */
    String getFeedsClientId();

    /**
     * 得到视频url
     */
    String getVideoUrl();

    /**
     * 得到直播房间号id
     */
    String getLiveShowId();

    /**
     * 得到回放的房间号id
     */
    String getBackShowId();

    /**
     * 得到分享地址
     */
    String getShareUrl();

    /**
     * 得到图片或视频的高度
     */
    int getHeight();

    /**
     * 得到图片或视频的宽度
     */
    int getWidth();

    /**
     * 得到小视频时长 单位ms
     */
    long getDuration();

    /**
     * 得到文件大小 单位bytes
     */
    long getVideoFileSize();

    /**
     * 得到 视频开始时间
     */
    long getVideoStartTimestamp();

    /**
     * 得到 视频结束时间
     */
    long getVideoEndTimestamp();

    /**
     * 得到 头像的水位
     */
    long getAvatarWater();

    void setViewHeight(float f);

    float getViewHeight();

    /**
     * ugcfeeds的观看人数
     */
    int getUGCFeedViwerCount();

    /**
     * 返回ugc 扩展类型, 值为1 == 日志类型(BlogFeed)    值为2 == 直播录屏(RoomRecord)
     */
    int getUGCExtraType();

    /**
     * 返回ugc直播录屏类型 主播id
     */
    long getUGCExtra_RecordHostId();

    /**
     * 返回ugc直播录屏类型 主播房间id
     */
    String getUGCExtra_RecordRoomId();

    /**
     * 返回ugc直播录屏类型 直播类型 ,公开:0 私密:1 密码 2 门票 3
     */
    int getUGCExtra_RecordLiveType();

    /**
     * 返回ugc直播录屏类型 0=小米直播app, 1=无人机, 2=导播台, 3=游戏, 4=一直播
     */
    int getUGCExtra_RecordAppType();

    /**
     * 返回ugc直播录屏类型 直播标题
     */
    String getUGCExtra_RecordRoomTitle();

    /**
     * 返回ugc直播录屏类型 直播封面
     */
    String getUGCExtra_RecordLiveCover();

    /**
     * 得到描述
     */
    String getFeedsDesc();


    /**
     * 用来回写当前全文或者收起的状态
     */
    boolean getState();

    void setState(boolean state);

    int getAppType();

    int getRet();

    double getDistance();

}
