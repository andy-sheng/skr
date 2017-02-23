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
    void openWatch(Activity activity, long playerId, String liveId, String videoUrl, int liveType, IOpenCallback callback);

    /**
     * 打开直播回放页面
     */
    void openReplay(Activity activity, long playerId, String liveId, String videoUrl, int liveType, IOpenCallback callback);

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
     * 判断该手机中是否安装的直播助手
     */
    boolean hasInstallLiveSdk();

    /**
     * 从上层重新传入channelId，用于测试，上线后删除
     */
    void setChannelId(int channelId);

    /**
     * sdk 上层应用回调
     */
    @Keep
    interface ICallback {
        /**
         * 登录相关接口的标志
         */
        int LOGIN_SSO_AIDL = 1001;

        int LOGIN_OAUTH_AIDL = 1002;

        int CLEAR_ACCOUNT_AIDL = 1003;

        /**
         * 登录相关接口的返回码
         */
        int CODE_SUCCESS = 0;

        /**
         * 通知上层应用，aidl service为空
         */
        void notifyServiceNull(int aidlFlag);

        /**
         * 通知上层应用aidl失败
         */
        void notifyAidlFailure(int aidlFlag);

        /**
         * 通知登录成功
         */
        void notifyLogin(int code);

        /**
         * 通知登录失败
         */
        void notifyLogoff(int code);

        /**
         * 通知上层需要登录
         */
        void notifyWantLogin();

        /**
         * 通知上层权限验证失败
         */
        void notifyVerifyFailure(int code);
    }

    @Keep
    interface IOpenCallback {
        /**
         * 打开直播，回放时，通知上层应用，直播助手未安装
         */
        void notifyNotInstall();
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
        void onDownloadSuccess(String path);

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
        public void notifyServiceNull(int aidlFlag) {
        }

        @Override
        public void notifyAidlFailure(int aidlFlag) {
        }

        @Override
        public void notifyLogin(int code) {
        }

        @Override
        public void notifyLogoff(int code) {
        }

        @Override
        public void notifyWantLogin() {
        }

        @Override
        public void notifyVerifyFailure(int code) {
        }
    }
}
