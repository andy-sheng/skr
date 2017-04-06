package com.wali.live.livesdk.live;

import android.app.Activity;
import android.app.Application;
import android.support.annotation.Keep;

import com.mi.live.data.location.Location;

/**
 * Created by lan on 17/2/20.
 *
 * @description 提供给第三方，只需要看这个文件
 */
public interface IMiLiveSdk {

    /**
     * 初始化操作，建议在Application.onCreate()初始化
     */
    void init(Application application, int channelId, String channelSecret, ICallback callback);

    /**
     * OAuth登录
     *
     * @version 204000
     */
    void loginByMiAccountOAuth(String authCode);

    /**
     * sso登录
     *
     * @version 204000
     */
    void loginByMiAccountSso(long miid, String serviceToken);

    /**
     * 退出账号
     *
     * @version 204000
     */
    void clearAccount();

    /**
     * 打开直播观看页面
     *
     * @version 204000
     */
    void openWatch(Activity activity, long playerId, String liveId, String videoUrl, int liveType);

    /**
     * 打开直播回放页面
     *
     * @version 204000
     */
    void openReplay(Activity activity, long playerId, String liveId, String videoUrl, int liveType);

    /**
     * 打开普通直播页面
     *
     * @version 205001
     */
    void openNormalLive(Activity activity, Location location);

    /**
     * 打开游戏直播页面
     *
     * @version 205001
     */
    void openGameLive(Activity activity, Location location);

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

    }
}
