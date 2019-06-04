package com.common.core.account;


import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.callback.Callback;
import com.common.core.account.event.AccountEvent;
import com.common.core.account.event.VerifyCodeErrorEvent;
import com.common.core.channel.HostChannelManager;
import com.common.core.myinfo.Location;
import com.common.core.myinfo.MyUserInfo;
import com.common.core.myinfo.MyUserInfoLocalApi;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.myinfo.MyUserInfoServerApi;
import com.common.core.userinfo.UserInfoLocalApi;
import com.common.core.userinfo.UserInfoManager;
import com.common.core.userinfo.remark.RemarkLocalApi;
import com.common.jiguang.JiGuangPush;
import com.common.log.MyLog;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.statistics.StatisticsAdapter;
import com.common.statistics.UmengStatistics;
import com.common.utils.HandlerTaskTimer;
import com.common.utils.U;
import com.module.ModuleServiceManager;
import com.module.common.ICallback;
import com.tencent.bugly.crashreport.CrashReport;


import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Response;


/**
 * 保存账号token等信息
 * 数据库支持多账号的
 * Created by chengsimin on 16/7/1.
 */
public class UserAccountManager {
    private static final String TAG = UserAccountManager.class.getSimpleName();

    public static final int SYSTEM_ID = 1;       //系统id
    public static final int SYSTEM_GRAB_ID = 2;  //一唱到底多音
    public static final int SYSTEM_RANK_AI = 3;  //AI裁判
    public static final String SYSTEM_AVATAR = "http://res-static.inframe.mobi/common/system2.png"; //系统头像

    private UserAccount mAccount;

    private boolean mHasTryConnentRM = false;//有没有尝试过登录过融云

    private boolean mHasloadAccountFromDB = false;//有没有尝试load过账号

    private int mOldOrNewAccount = 0;

    public static final int MSG_DELAY_GET_RC_TOKEN = 10 * 1000;

    Handler mUiHanlder = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == MSG_DELAY_GET_RC_TOKEN) {
                getIMToken();
            }
        }
    };

    private static class UserAccountManagerHolder {
        private static final UserAccountManager INSTANCE = new UserAccountManager();
    }

    private UserAccountManager() {
        //EventBus.getDefault().register(this);
    }

    public static final UserAccountManager getInstance() {
        return UserAccountManagerHolder.INSTANCE;
    }

    public void init() {
        MyLog.d(TAG, "init");
        long channelId = HostChannelManager.getInstance().getChannelId();
        UserAccount userAccount = UserAccountLocalApi.getUserAccount(channelId);
        setAccount(userAccount, false);
    }

    public void onLoginResult(UserAccount account) {
        if (account != null) {
            MyLog.w(TAG, "login" + " accountId=" + account.getUid());
            account.setIsLogOff(false);
            // 登出所有其他账号
            UserAccountLocalApi.loginAccount(account);
            // 用户登录成功，这里应该是要发出通知的
            setAccount(account, true);
            U.getActivityUtils().showSnackbar("登录成功", false);

            if (!MyUserInfoManager.getInstance().isFirstLogin()) {
                // 是个老用户，打个点
                StatisticsAdapter.recordCountEvent("signup", "oldid", null, true);
            }
        }
    }

    private void setAccount(UserAccount account, boolean fromServer) {
        mAccount = account;
        mHasloadAccountFromDB = true;
        if (account != null) {
            MyLog.d(TAG, "setAccount" + " accountId=" + account.getUid());
            // 取消匿名模式
//            MiLinkClientAdapter.getInstance().setIsTouristMode(false);
            // MilinkChannelClientAdapter.getInstance().destroy();
            // 进入实名模式
//            MiLinkClientAdapter.getInstance().initCallBackFirst();
            // 同步昵称等详细信息
            MyUserInfoManager.getInstance().init();

            if (fromServer) {
                // 与融云服务器建立连接
                MyLog.d(TAG, "从服务器取的账号，可以立马登录融云");
                mHasTryConnentRM = true;
                connectRongIM(account.getRongToken());
            } else {
                // 账号不是从服务器取的，至少一个服务器请求成功后，才登录融云
                mHasTryConnentRM = false;
            }

            trySetAlias();
//            ScreenLogView.addInfo("用户id", account.getUid());
        } else {

        }
        EventBus.getDefault().post(new AccountEvent.SetAccountEvent());
        // 只有非游客模式才发已有账号的事件
    }

    public boolean hasLoadAccountFromDB() {
        return mHasloadAccountFromDB;
    }


    public boolean hasAccount() {
        if (mAccount == null) {
            return false;
        }
        if (mAccount.getIsLogOff() == null) {
            return false;
        }
        if (mAccount.getIsLogOff().booleanValue()) {
            return false;
        }
        return true;
    }

    public String getUuid() {
        if (mAccount != null) {
            return mAccount.getUid();
        }
        return "";
    }

    public long getUuidAsLong() {
        if (mAccount != null) {
            return Long.parseLong(mAccount.getUid());
        }
        return 0L;
    }

    public String getPhoneNum() {
        if (mAccount != null) {
            return mAccount.getPhoneNum();
        }
        return "";
    }

    public String getServiceToken() {
        if (mAccount != null) {
            return mAccount.getServiceToken();
        }
        return "";
    }

    public String getSSecurity() {
        if (mAccount != null) {
            return mAccount.getSecretToken();
        }
        return "";
    }

    public String getRongToken() {
        if (mAccount != null) {
            return mAccount.getRongToken();
        }
        return "";
    }

    // 是否是老账号
    private boolean isNewAccount() {
        if (mOldOrNewAccount == 0) {
            long firstLoginTime = U.getPreferenceUtils().getSettingLong("first_login_time", 0);
            if (System.currentTimeMillis() - firstLoginTime > 24 * 3600 * 1000) {
                mOldOrNewAccount = 2;
            } else {
                // 一天内的算新用户
                mOldOrNewAccount = 1;
            }
        }
        if (mOldOrNewAccount == 1) {
            return true;
        }
        return false;


    }

    public String getGategory(String category) {
        return isNewAccount() ? ("new_" + category) : ("old_" + category);
    }

    // 手机登录
    public void loginByPhoneNum(final String phoneNum, String verifyCode, final Callback callback) {
        StatisticsAdapter.recordCountEvent("signup", "api_begin", null);
        UserAccountServerApi userAccountServerApi = ApiManager.getInstance().createService(UserAccountServerApi.class);
        // 1 为手机登录
        String deviceId = U.getDeviceUtils().getImei();
        if (TextUtils.isEmpty(deviceId)) {
            deviceId = "";
        } else {
            deviceId = U.getMD5Utils().MD5_32(deviceId);
        }
        userAccountServerApi.login(1, phoneNum, verifyCode, 20, U.getChannelUtils().getChannel(), deviceId)
                .subscribeOn(Schedulers.io())
                .subscribe(new ApiObserver<ApiResult>() {
                    @Override
                    public void process(ApiResult obj) {
                        if (obj.getErrno() == 0) {
                            UserAccount userAccount = parseRsp(obj.getData(), phoneNum, callback);
                            UmengStatistics.onProfileSignIn("phone", userAccount.getUid());
                        } else {
                            U.getToastUtil().showShort(obj.getErrmsg());
                            HashMap map = new HashMap();
                            map.put("error", obj.getErrno() + "");
                            StatisticsAdapter.recordCountEvent("signup", "api_failed", map);
                            EventBus.getDefault().post(new VerifyCodeErrorEvent(obj.getErrno(), obj.getErrmsg()));
                        }
                    }

                    @Override
                    public void onNetworkError(ErrorType errorType) {
                        super.onNetworkError(errorType);
                        HashMap map = new HashMap();
                        map.put("error", "network_error");
                        StatisticsAdapter.recordCountEvent("signup", "api_failed", map);
                    }
                });

    }

    /**
     * 第三方登录
     *
     * @param mode        3 为微信登录, 2 为qq
     * @param accessToken
     * @param openId
     */
    public void loginByThirdPart(final int mode, String accessToken, String openId) {
        StatisticsAdapter.recordCountEvent("signup", "api_begin", null);
        UserAccountServerApi userAccountServerApi = ApiManager.getInstance().createService(UserAccountServerApi.class);
        String deviceId = U.getDeviceUtils().getImei();
//        MyLog.d(TAG, "mimusic md5 = " + getMd5Digest(deviceId.getBytes()));
//        MyLog.d(TAG, "skr md5 = " + U.getMD5Utils().MD5_32(deviceId));
        if (TextUtils.isEmpty(deviceId)) {
            deviceId = "";
        } else {
            deviceId = U.getMD5Utils().MD5_32(deviceId);
        }
        userAccountServerApi.loginWX(mode, accessToken, openId, 20, U.getChannelUtils().getChannel(), deviceId)
                .subscribeOn(Schedulers.io())
                .subscribe(new ApiObserver<ApiResult>() {
                    @Override
                    public void process(ApiResult obj) {
                        if (obj.getErrno() == 0) {
                            UserAccount userAccount = parseRsp(obj.getData(), "", null);
                            if (mode == 3) {
                                UmengStatistics.onProfileSignIn("wx", userAccount.getUid());
                            } else if (mode == 2) {
                                UmengStatistics.onProfileSignIn("icon_qq", userAccount.getUid());
                            }
                        } else {
                            U.getToastUtil().showShort(obj.getErrmsg());
                            HashMap map = new HashMap();
                            map.put("error", obj.getErrno() + "");
                            StatisticsAdapter.recordCountEvent("signup", "api_failed", map);
                        }
                    }

                    @Override
                    public void onNetworkError(ErrorType errorType) {
                        super.onNetworkError(errorType);
                        HashMap map = new HashMap();
                        map.put("error", "network_error");
                        StatisticsAdapter.recordCountEvent("signup", "api_failed", map);
                    }
                });
    }

    UserAccount parseRsp(JSONObject jsonObject, String phoneNum, Callback callback) {
        String secretToken = jsonObject.getJSONObject("token").getString("T");
        String serviceToken = jsonObject.getJSONObject("token").getString("S");
        String rongToken = jsonObject.getJSONObject("token").getString("RC");
        com.alibaba.fastjson.JSONObject profileJO = jsonObject.getJSONObject("profile");
        long userID = profileJO.getLongValue("userID");
        String nickName = profileJO.getString("nickname");
        int sex = profileJO.getIntValue("sex");
        String birthday = profileJO.getString("birthday");
        String avatar = profileJO.getString("avatar");
        String sign = profileJO.getString("signature");
        Location location = JSON.parseObject(profileJO.getString("location"), Location.class);

        boolean isFirstLogin = jsonObject.getBooleanValue("isFirstLogin");
        if (isFirstLogin) {
            U.getPreferenceUtils().setSettingLong("first_login_time", System.currentTimeMillis());
        }
        HashMap map = new HashMap();
        map.put("isFirstLogin", "" + isFirstLogin);
        StatisticsAdapter.recordCountEvent("signup", "api_success", map);
        boolean needBeginnerGuide = jsonObject.getBooleanValue("needBeginnerGuide");

        // 设置个人信息
        MyUserInfo myUserInfo = new MyUserInfo();
        myUserInfo.setUserId(userID);
        myUserInfo.setUserNickname(nickName);
        myUserInfo.setSex(sex);
        myUserInfo.setBirthday(birthday);
        myUserInfo.setAvatar(avatar);
        myUserInfo.setSignature(sign);
        myUserInfo.setLocation(location);
        MyUserInfoManager.getInstance().setFirstLogin(isFirstLogin);
        MyUserInfoManager.getInstance().setNeedBeginnerGuide(needBeginnerGuide);
        MyUserInfoLocalApi.insertOrUpdate(myUserInfo);
        MyUserInfoManager.getInstance().setMyUserInfo(myUserInfo, true, "parseRsp");

        UserAccount userAccount = new UserAccount();
        userAccount.setPhoneNum(phoneNum);
        userAccount.setServiceToken(serviceToken);
        userAccount.setSecretToken(secretToken);
        userAccount.setRongToken(rongToken);
        userAccount.setUid(String.valueOf(userID));
        userAccount.setNeedEditUserInfo(isFirstLogin);
        userAccount.setChannelId(HostChannelManager.getInstance().getChannelId());
        onLoginResult(userAccount);
        if (callback != null) {
            callback.onCallback(1, null);
        }
        return userAccount;
    }

    /**
     * 用户主动退出登录
     */
    public void logoff() {
        logoff(false, AccountEvent.LogoffAccountEvent.REASON_SELF_QUIT, true);
        mUiHanlder.removeCallbacksAndMessages(null);
    }

    /**
     * 收到账号过期的通知，被踢下线等等
     */
    public void notifyAccountExpired() {
        logoff(false, AccountEvent.LogoffAccountEvent.REASON_ACCOUNT_EXPIRED, false);
    }

    /**
     * 经过服务器的api认证，账号有效
     */
    public void accountValidFromServer() {
        // 认证有效时，再连融云，防止无效的账号将有效账号的融云踢下线
        tryConnectRongIM(false);
    }

    /**
     * 退出登录
     *
     * @param deleteAccount
     */
    public void logoff(final boolean deleteAccount, final int reason, boolean notifyServer) {
        MyLog.w(TAG, "logoff" + " deleteAccount=" + deleteAccount + " reason=" + reason + " notifyServer=" + notifyServer);
        if (!UserAccountManager.getInstance().hasAccount()) {
            MyLog.w(TAG, "logoff but hasAccount = false");
            return;
        }
        if (notifyServer) {
            UserAccountServerApi userAccountServerApi = ApiManager.getInstance().createService(UserAccountServerApi.class);
            ApiMethods.subscribe(userAccountServerApi.loginOut(), new ApiObserver<ApiResult>() {
                @Override
                public void process(ApiResult result) {
                    if (result.getErrno() == 0) {
                        U.getToastUtil().showShort("登出成功了");
                    }
                }
            });
        }
        ModuleServiceManager.getInstance().getMsgService().logout();
        if (mAccount != null) {
            final String userId = mAccount.getUid();
            Observable.create(new ObservableOnSubscribe<Object>() {
                @Override
                public void subscribe(ObservableEmitter<Object> emitter) throws Exception {
                    if (deleteAccount) {
                        UserAccountLocalApi.delete(mAccount);
                    } else {
                        mAccount.setIsLogOff(true);
                        UserAccountLocalApi.insertOrReplace(mAccount);
                    }
                    mAccount = null;
                    ApiManager.getInstance().clearCookies();
                    // 清除pref 清除数据库
                    U.getPreferenceUtils().clearPreference();
                    UserInfoLocalApi.deleteAll();
                    RemarkLocalApi.deleteAll();
                    // 清除备注的缓存
                    UserInfoManager.getInstance().clearRemark();
                    UmengStatistics.onProfileSignOff();
                    JiGuangPush.clearAlias(userId);
                    MyUserInfoManager.getInstance().logoff();
                    EventBus.getDefault().post(new AccountEvent.LogoffAccountEvent(reason));
                    emitter.onComplete();
                }
            })
                    .subscribeOn(Schedulers.io())
                    .subscribe();
        }
    }

    public void tryConnectRongIM(boolean force) {
        if (mHasTryConnentRM && !force) {
            return;
        }
        if (force) {
            MyLog.d(TAG, "强制重连融云");
        }
        MyLog.d(TAG, "tryConnectRongIM" + " force=" + force);
        if (mAccount != null) {
            String token = mAccount.getRongToken();
            MyLog.d(TAG, "tryConnectRongIM token=" + token);
            mHasTryConnentRM = true;
            connectRongIM(token);
        } else {
            MyLog.d(TAG, "tryConnectRongIM" + " mAccount==null");
        }
    }

    /**
     * 融云账号被人提了
     * 融云账号被人提了，不一定代表这个账号无效了
     * 这时候访问一下服务器，看看这个账号是不是真的无效
     */
    public void rcKickedByOthers(final int times) {
        MyLog.d(TAG, "融云被KICK了  rcKickedByOthers" + " times=" + times);
        if (times > 5) {
            MyLog.d(TAG, "rcKickedByOthers" + " times=" + times + ",超过重试次数了");
            return;
        }
        if (hasAccount()) {
            //还有账号
            MyUserInfoServerApi api = ApiManager.getInstance().createService(MyUserInfoServerApi.class);
            Call<ApiResult> apiResultCall = api.getUserInfo((int) getUuidAsLong());
            if (apiResultCall != null) {
                try {
                    Response<ApiResult> resultResponse = apiResultCall.execute();
                    if (resultResponse != null) {
                        ApiResult obj = resultResponse.body();
                        if (obj != null) {
                            if (obj.getErrno() == 0) {
                                connectRongIM(mAccount.getRongToken());
                            } else if (obj.getErrno() == 107) {
                                UserAccountManager.getInstance().notifyAccountExpired();
                            }
                        } else {
                            MyLog.w(TAG, "syncMyInfoFromServer obj==null");
                        }
                    }
                } catch (Exception e) {
                    MyLog.d(e);
                }
            } else {
                HandlerTaskTimer.newBuilder().delay(2000)
                        .start(new HandlerTaskTimer.ObserverW() {
                            @Override
                            public void onNext(Integer integer) {
                                rcKickedByOthers(times + 1);
                            }
                        });
            }
        } else {
            MyLog.d(TAG, "rcKickedByOthers no account,do nothing");
        }
    }

    // 获取IM的token
    private void getIMToken() {
        MyLog.d(TAG, "getIMToken");
        UserAccountServerApi userAccountServerApi = ApiManager.getInstance().createService(UserAccountServerApi.class);
        ApiMethods.subscribe(userAccountServerApi.getIMToken(), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                String token = null;
                if (result.getErrno() == 0) {
                    token = result.getData().getString("RC");
                    if (!TextUtils.isEmpty(token)) {
                        // 更新数据库中融云token
                        mAccount.setRongToken(token);
                        UserAccountLocalApi.loginAccount(mAccount);
                        // 连接融云
                        connectRongIM(token);
                    } else {
                        if (MyLog.isDebugLogOpen()) {
                            U.getToastUtil().showShort("服务器返回的融云token为空，检查是否触发了测试环境100用户上线");
                        }
                        MyLog.e(TAG, "getIMToken from Server is null");
                    }
                } else {
                    if (result.getErrno() == 8302102) {
                        U.getToastUtil().showShort("GET融云token失败，测试用户账号超过100限度");
                    } else {
                        U.getToastUtil().showShort("GET融云token error=" + result.getErrno());
                    }

                }
                if (TextUtils.isEmpty(token)) {
                    mUiHanlder.removeMessages(MSG_DELAY_GET_RC_TOKEN);
                    mUiHanlder.sendEmptyMessageDelayed(MSG_DELAY_GET_RC_TOKEN, 10 * 1000);
                }
            }
        });
    }

    private void connectRongIM(String rongToken) {
        MyLog.d(TAG, "connectRongIM" + " rongToken=" + rongToken);
        if (TextUtils.isEmpty(rongToken)) {
            getIMToken();
        } else {
            ModuleServiceManager.getInstance().getMsgService().connectRongIM(rongToken, new ICallback() {
                @Override
                public void onSucess(Object obj) {
//                    U.getToastUtil().showShort("与服务器连接成功");
                    MyLog.e(TAG, "与融云服务器连接成功");
                    mUiHanlder.removeMessages(MSG_DELAY_GET_RC_TOKEN);
                }

                @Override
                public void onFailed(Object obj, int errcode, String message) {
                    boolean result = (boolean) obj;
                    if (result) {
                        // todo 连接融云失败
                        if (U.getActivityUtils().isAppForeground()) {
                            U.getToastUtil().showShort("融云连接不可用");
                        }
                    } else {
                        // todo token有问题, 重试一次(可能过期或者appkey不一致等)
                        getIMToken();
                    }
                }
            });
        }
    }

//    @Subscribe(threadMode = ThreadMode.POSTING)
//    public void onEvent(UmengPushRegisterSuccessEvent event) {
//        trySetAlias();
//    }

    /**
     * 给Umeng的push通道设置 Alias
     */
    void trySetAlias() {
        if (UserAccountManager.getInstance().hasAccount()) {
            //com.common.umeng.UmengPush.UmengPush.setAlias(UserAccountManager.getInstance().getUuid());
            com.common.jiguang.JiGuangPush.setAlias(UserAccountManager.getInstance().getUuid());
            if (U.getChannelUtils().isStaging()) {
                CrashReport.setUserId("dev_" + UserAccountManager.getInstance().getUuid());
            } else {
                CrashReport.setUserId(UserAccountManager.getInstance().getUuid());
            }

        }
    }
}
