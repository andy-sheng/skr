package com.wali.live.sdk.manager;

import android.app.Activity;
import android.app.Application;
import android.support.annotation.IntRange;
import android.support.annotation.Keep;

import com.mi.live.data.location.Location;
import com.wali.live.watchsdk.ipc.service.BarrageInfo;
import com.wali.live.watchsdk.ipc.service.LiveInfo;
import com.wali.live.watchsdk.ipc.service.ShareInfo;
import com.wali.live.watchsdk.ipc.service.UserInfo;
import com.wali.live.watchsdk.watch.model.RoomInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lan on 17/2/20.
 *
 * @description 提供给第三方，只需要看这个文件
 */
@Keep
public interface IMiLiveSdk {

    /**
     * 初始化操作，建议在Application.onCreate()初始化
     */
    void init(Application application, int channelId, String channelSecret, boolean isCta, ICallback callback);

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
     * 上层应用设置支持的分享类型
     */
    void enableShare(boolean enable);

    /**
     * 上层应用设置是否支持关注
     */
    boolean enableRelationChain(boolean enable, IAssistantCallback callback);

    /**
     * OAuth登录
     *
     * @version 204000
     */
    void loginByMiAccountOAuth(String authCode, IAssistantCallback callback);

    /**
     * sso登录
     *
     * @version 204000
     */
    void loginByMiAccountSso(long miid, String serviceToken, IAssistantCallback callback);

    /**
     * 退出账号
     *
     * @version 204000
     */
    void clearAccount(IAssistantCallback callback);

    /**
     * 尝试schema跳转
     *
     * @param activity
     * @param schema
     * @param callback
     * @return
     */
    boolean tryJumpBySchema(Activity activity, String schema, IAssistantCallback callback);

    /**
     * 尝试schema跳转
     *
     * @param activity
     * @param schema
     * @param callback
     * @param pageChannelId
     * @return
     */
    boolean tryJumpBySchema(Activity activity, String schema, long pageChannelId, IAssistantCallback callback);

    /**
     * 打开直播观看页面
     *
     * @notice 不带gameId
     * @version 204000
     */
    void openWatch(Activity activity, long playerId, String liveId, String videoUrl, int liveType, IAssistantCallback callback);

    /**
     * 打开直播回放页面
     *
     * @notice 不带gameId
     * @version 204000
     */
    void openReplay(Activity activity, long playerId, String liveId, String videoUrl, int liveType, IAssistantCallback callback);


    /**
     * 打开直播观看页面
     *
     * @notice 带gameId
     * @version 204000
     */
    void openWatch(Activity activity, long playerId, String liveId, String videoUrl, int liveType, String gameId, IAssistantCallback callback);

    /**
     * 打开直播观看页面
     *
     * @notice 带频道id 不带gameId
     */
     void openWatch(Activity activity, long playerId, String liveId, String videoUrl, int liveType, long pageChannelId, IAssistantCallback callback);

    /**
     * 打开直播观看页面
     *
     * @version 205037
     */
    void openWatchRoom(Activity activity, RoomInfo roomInfo, IAssistantCallback callback);


    /**
     * 打开直播观看页面，同时能够滑动
     *
     * @version 205037
     */
    void openWatchRoomList(Activity activity, ArrayList<RoomInfo> list, int position, IAssistantCallback callback);

    /**
     * 打开直播回放页面
     *
     * @notice 带gameId
     * @version 204000
     */
    void openReplay(Activity activity, long playerId, String liveId, String videoUrl, int liveType, String gameId, IAssistantCallback callback);

    /**
     * 打开普通直播页面
     *
     * @version 205001
     */
    void openNormalLive(Activity activity, Location location, IAssistantCallback callback);

    /**
     * 打开游戏直播页面
     *
     * @version 205001
     */
    void openGameLive(Activity activity, Location location, IAssistantCallback callback);

    /**
     * 打开冲顶大会准备页
     *
     * @version 205055
     */
    void openContestPrepare(Activity activity, IAssistantCallback callback);

    /**
     * 打开冲顶大会直播间
     *
     * @version 205055
     */
    void openContestWatch(Activity activity, long playerId, String liveId, String videoUrl, IAssistantCallback callback);

    /**
     * 打开提现页
     *
     * @version 205056
     */
    void openContestWithdrawals(Activity activity, IAssistantCallback callback);

    void updateGameDownloadstatus(IAssistantCallback callback, long gameId, int type, int progress, String gamePageName, boolean isByQuery);

    /**
     * 第三方登录
     *
     * @param channelId 渠道ID
     * @param xuid      第三方Uid
     * @param sex       第三方用户性别,1:男 2:女 0:未知
     * @param nickname  第三方用户昵称
     * @param headUrl   第三方用户头像
     * @param sign      签名,请参考接入文档，签名算法一栏
     * @param callback
     * @version 205005
     */
    void thirdPartLogin(int channelId, String xuid, int sex, String nickname, String headUrl, String sign, IAssistantCallback callback);

    /**
     * 编辑用户资料
     *
     * @param channelId
     * @param xuid
     * @param sex
     * @param nickname
     * @param headUrl
     * @param callback
     */
    public void editUserInfo(int channelId, String xuid, int sex, String nickname, String headUrl, IAssistantCallback callback);

    /**
     * 用户反馈
     */
    public void doFeedBack(IAssistantCallback callback);

    /**
     * 获取频道列表
     *
     * @version 205008
     */
    void getChannelLives(IChannelAssistantCallback callback);

    /**
     * 获取关注人信息列表
     *
     * @version 205011
     */
    void getFollowingUserList(boolean isBothWay, long timeStamp, IFollowingUsersCallback callback);

    /**
     * 宿主app通知sdk分享成功接口
     *
     * @version 205014
     */
    void notifyShare(boolean success, int type, IAssistantCallback callback);

    /**
     * 获取宿主关注人直播列表
     *
     * @version 205017
     */
    void getFollowingLiveList(IFollowingLivesCallback callback);

    /**
     * 统计打点
     *
     * @version 205025
     */
    void statistic(String key, long time, IAssistantCallback callback);

    /**
     * 判断该手机中是否安装的直播助手
     */
    boolean hasInstallLiveSdk(Application application);

    /**
     * 从上层重新传入channelId，用于测试，上线后删除
     */
    void setChannelId(int channelId);

    void startBarragePull(String roomId, int[] msgType, IGetBarrageCallback callback);

    void stopBarragePull(String roomId, IAssistantCallback assistantCallback);

    boolean openChannelList(Activity activity,IAssistantCallback assistantCallback);

    void getLiveUid(IAssistantCallback assistantCallback);

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

        int THIRD_PART_LOGIN = 1004;

        int LOGIN_OFF = 1005;
        /**
         * 列表相关的接口
         */
        int GET_CHANNEL_LIVES = 1100;

        int GET_FOLLOWING_USERS = 1101;

        int GET_FOLLOWING_LIVES = 1102;

        int GET_BARRAGE = 1103;

        int GET_LIVE_UID = 1104;
        /**
         * 分享相关接口标志
         */
        int NOTIFY_SHARE_AIDL = 1200;

        /**
         * 编辑资料相关接口
         */
        int EDIT_USER_INFO = 1300;

        /**
         * 用户反馈接口
         */
        int DO_FEED_BACK = 1301;

        int UPDATE_GAME_DOWNLOAD_STATUS = 1302;

        /**
         * 登录相关接口的返回码
         */
        int CODE_SUCCESS = 0;

        /**
         * 通知上层应用，aidl service为空
         */
        void notifyServiceNull(int errCode);

        /**
         * 通知上层应用aidl失败
         */
        void notifyAidlFailure(int errCode);

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

        /**
         * 通知上层分享
         */
        void notifyRecvInfo(int type,String json);

        boolean notifyGameInstallOpt(int type, long gameId, String packageName, String apkUrl);

        boolean notifyQueryGameDownloadStatus(long gameId, String packageName, String apkUrl);
    }

    /**
     * 直播助手回调
     */
    @Keep
    interface IAssistantCallback {
        /**
         * 通知上层版本过低
         */
        void notifyVersionLow();

        /**
         * 通知上层应用，直播助手未安装
         */
        void notifyNotInstall();
    }

    @Keep
    interface IChannelAssistantCallback extends IAssistantCallback {
        /**
         * 通知上层直播列表的方法回调
         */
        void notifyGetChannelLives(int errCode, List<LiveInfo> liveInfos);
    }

    @Keep
    interface IFollowingUsersCallback extends IAssistantCallback {
        void notifyGetFollowingUserList(int errCode, List<UserInfo> userInfos, int total, long timeStamp);
    }

    @Keep
    interface IFollowingLivesCallback extends IAssistantCallback {
        void notifyGetFollowingLiveList(int errCode, List<LiveInfo> liveInfos);
    }

    @Keep
    interface IGetBarrageCallback extends IAssistantCallback {
        void notifyGetBarrageList(List<BarrageInfo> barrageInfoList);
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
        public void notifyServiceNull(int errCode) {
        }

        @Override
        public void notifyAidlFailure(int errCode) {
        }

        @Override
        public void notifyLogin(int errCode) {
        }

        @Override
        public void notifyLogoff(int errCode) {
        }

        @Override
        public void notifyWantLogin() {
        }

        @Override
        public void notifyVerifyFailure(int errCode) {
        }

        @Override
        public void notifyOtherAppActive() {
        }

        @Override
        public void notifyWantShare(ShareInfo shareInfo) {

        }

        @Override
        public void notifyRecvInfo(int type, String json) {

        }

        @Override
        public boolean notifyGameInstallOpt(int type, long gameId, String packageName, String apkUrl) {
            return false;
        }

        @Override
        public boolean notifyQueryGameDownloadStatus(long gameId, String packageName, String apkUrl) {
            return false;
        }
    }
}
