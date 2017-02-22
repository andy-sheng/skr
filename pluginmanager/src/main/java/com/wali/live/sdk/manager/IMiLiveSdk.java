package com.wali.live.sdk.manager;

import android.app.Activity;
import android.app.Application;
import android.support.annotation.IntRange;
import android.support.annotation.Keep;

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
     * 设置plugin日志是否开启，默认false
     */
    void setLogEnabled(boolean isEnabled);

    /**
     * 获取上层传入的channelId
     */
    int getChannelId();

    /**
     * 获取上层传入的channelSecret
     */
    String getChannelSecret();

    /**
     * 打开直播观看页面
     */
    void openWatch(Activity activity, long playerId, String liveId, String videoUrl);

    /**
     * 打开直播回放页面
     */
    void openReplay(Activity activity, long playerId, String liveId, String videoUrl);

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
     * 从上层重新传入channelId，用于测试，上线后删除
     */
    void setChannelId(int channelId);

    /**
     * 随便打开一个直播，用于测试，上线后删除
     */
    void openRandomLive(Activity activity);

    /**
     * sdk 上层应用回调
     */
    @Keep
    interface ICallback {
        int LOGIN_SSO_AIDL = 1001;

        int LOGIN_OAUTH_AIDL = 1002;

        int CLEAR_ACCOUNT_AIDL = 1003;

        /**
         * 通知上层应用，直播助手未安装
         */
        void notifyNotInstall();

        /**
         * 通知上层应用，aidl service为空
         */
        void notifyServiceNull(int aidlFlag);

        /**
         * 通知上层应用aidl失败
         */
        void notifyAidlFailure(int aidlFlag);
    }

    @Keep
    interface IUpdateListener {
        /**
         * 通知上层应用可更新直播助手
         */
        void onNewVersionAvail(boolean isForce);

        /**
         * 通知上层应用当前无需更新直播助手
         */
        void onNoNewerVersion();

        /**
         * 通知上层应用更新检查失败
         */
        void onCheckVersionFailed();

        /**
         * 通知上层应用重复的更新检查请求
         */
        void onDuplicatedRequest();

        /**
         * 通知上层应用更新下载开始
         */
        void onDownloadStart();

        /**
         * 通知上层应用更新下载进度更新
         */
        void onDownloadProgress(@IntRange(from = 0, to = 100) int progress);

        /**
         * 通知上层应用更新下载成功
         */
        void onDownloadSuccess();

        /**
         * 通知上层应用更新下载失败
         */
        void onDownloadFailed(int errCode);
    }

    /**
     * 回调的包装器
     */
    @Keep
    class CallbackWrapper implements ICallback {
        @Override
        public void notifyNotInstall() {
        }

        @Override
        public void notifyServiceNull(int aidlFlag) {
        }

        @Override
        public void notifyAidlFailure(int aidlFlag) {
        }
    }
}
