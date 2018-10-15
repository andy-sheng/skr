package com.wali.live.watchsdk;

import android.app.Activity;
import android.app.Application;
import android.support.annotation.Keep;

import com.mi.live.data.location.Location;
import com.wali.live.watchsdk.ipc.service.LiveInfo;
import com.wali.live.watchsdk.ipc.service.ShareInfo;
import com.wali.live.watchsdk.ipc.service.UserInfo;

import java.util.List;

/**
 * Created by lan on 17/2/20.
 *
 * @description 提供给第三方，只需要看这个文件
 */
public interface IMiLiveSdk {

    /**
     * 初始化操作，要求在Application.onCreate()初始化
     */
    void init(Application application, int channelId, String channelSecret);

    /**
     * 设置操作的统一回调，需要设置回调功能才可正常使用
     */
    void setCallback(ICallback callback);

    /**
     * 上层应用设置支持的分享类型
     */
    void enableShare(boolean enable);

    /**
     * OAuth登录
     */
    void loginByMiAccountOAuth(String authCode);

    /**
     * sso登录
     */
    void loginByMiAccountSso(long miid, String serviceToken);

    /**
     * 退出账号
     */
    void clearAccount();

    /**
     * 打开直播观看页面
     */
    void openWatch(Activity activity, long playerId, String liveId, String videoUrl, int liveType);

    /**
     * 打开直播回放页面
     */
    void openReplay(Activity activity, long playerId, String liveId, String videoUrl, int liveType);

    /**
     * 打开直播观看页面
     */
    void openWatch(Activity activity, long playerId, String liveId, String videoUrl, int liveType, String gameId);

    /**
     * 打开直播回放页面
     */
    void openReplay(Activity activity, long playerId, String liveId, String videoUrl, int liveType, String gameId);

    /**
     * 打开普通直播页面
     */
    void openNormalLive(Activity activity, Location location);

    /**
     * 打开游戏直播页面
     */
    void openGameLive(Activity activity, Location location);

    /**
     * 测试接口，上线后删除
     */
    void enterWatch(Activity activity, String playerId);

    /**
     * 测试接口，上线后删除
     */
    void enterReplay(Activity activity, String playerId);

    /**
     * 第三方登录
     *
     * @param channelId 渠道ID
     * @param xuid      第三方Uid
     * @param sex       第三方用户性别,1:男 2:女 0:未知
     * @param nickname  第三方用户昵称
     * @param headUrl   第三方用户头像
     * @param sign      签名,请参考接入文档，签名算法一栏
     */
    void thirdPartLogin(int channelId, String xuid, int sex, String nickname, String headUrl, String sign);


    /**
     * 获取频道列表
     */
    void getChannelLives(IChannelCallback callback);

    /**
     * 获取关注人信息列表
     */
    void getFollowingUserList(boolean isBothWay, long timeStamp, IFollowingUsersCallback callback);

    /**
     * 宿主app通知sdk分享成功接口
     */
    void notifyShare(boolean success, int type);

    /**
     * 获取关注人直播列表信息
     */
    void getFollowingLiveList(IFollowingLivesCallback callback);

    /**
     * 下载游戏的回调
     * @param gameId
     * @param type
     * @param progress
     */
    void updateGameDownloadstatus(long gameId, int type, int progress, String packageName, boolean isByQuery);

    /**
     * sdk 上层应用回调
     */
    @Keep
    interface ICallback {

        /**
         * 登录相关接口的返回码
         */
        int CODE_SUCCESS = 0;

        /**
         * 通知登录
         */
        void notifyLogin(int errCode);

        /**
         * 通知注销
         */
        void notifyLogoff(int errCode);

        /**
         * 通知上层需要登录
         */
        void notifyWantLogin();

        /**
         * 通知上层权限验证失败
         */
        void notifyVerifyFailure(int errCode);

        /**
         * 通知上层有其它的app在活跃状态
         */
        void notifyOtherAppActive();

        /**
         * 通知上层分享
         */
        void notifyWantShare(ShareInfo shareInfo);

    }

    @Keep
    interface IChannelCallback {
        /**
         * 通知上层直播列表的方法回调
         */
        void notifyGetChannelLives(int errCode, List<LiveInfo> liveInfos);
    }

    @Keep
    interface IFollowingUsersCallback {
        void notifyGetFollowingUserList(int errCode, List<UserInfo> userInfos, int total, long timeStamp);
    }

    @Keep
    interface IFollowingLivesCallback {
        void notifyGetFollowingLiveList(int errCode, List<LiveInfo> liveInfos);
    }
}
