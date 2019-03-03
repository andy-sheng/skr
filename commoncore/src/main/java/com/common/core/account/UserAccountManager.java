package com.common.core.account;


import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.core.account.event.AccountEvent;
import com.common.core.account.event.VerifyCodeErrorEvent;
import com.common.core.channel.HostChannelManager;
import com.common.core.myinfo.Location;
import com.common.core.myinfo.MyUserInfo;
import com.common.core.myinfo.MyUserInfoLocalApi;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.myinfo.MyUserInfoServerApi;
import com.common.log.MyLog;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.statistics.UmengStatistics;
import com.common.umeng.UmengPush;
import com.common.umeng.UmengPushRegisterSuccessEvent;
import com.common.utils.HandlerTaskTimer;
import com.common.utils.U;
import com.module.ModuleServiceManager;
import com.module.common.ICallback;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

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

    private UserAccount mAccount;

    private boolean mHasTryConnentRM = false;//有没有尝试过登录过融云

    private boolean mHasloadAccountFromDB = false;//有没有尝试load过账号

    private int mOldOrNewAccount = 0;

    private static class UserAccountManagerHolder {
        private static final UserAccountManager INSTANCE = new UserAccountManager();
    }

    private UserAccountManager() {
        EventBus.getDefault().register(this);
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

            }

            trySetUmengPushAlias();
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
    public void loginByPhoneNum(final String phoneNum, String verifyCode) {
        UserAccountServerApi userAccountServerApi = ApiManager.getInstance().createService(UserAccountServerApi.class);
        // 1 为手机登录
        String imei = U.getDeviceUtils().getImei();
        userAccountServerApi.login(1, phoneNum, verifyCode, U.getChannelUtils().getChannel(), U.getMD5Utils().MD5_32(imei))
                .subscribeOn(Schedulers.io())
                .subscribe(new ApiObserver<ApiResult>() {
                    @Override
                    public void process(ApiResult obj) {
                        if (obj.getErrno() == 0) {
                            UserAccount userAccount = parseRsp(obj.getData(), phoneNum);
                            UmengStatistics.onProfileSignIn("phone", userAccount.getUid());
                        } else {
                            EventBus.getDefault().post(new VerifyCodeErrorEvent(obj.getErrno(), obj.getErrmsg()));
                        }
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
        UserAccountServerApi userAccountServerApi = ApiManager.getInstance().createService(UserAccountServerApi.class);
        String imei = U.getDeviceUtils().getImei();
        userAccountServerApi.loginWX(mode, accessToken, openId, U.getChannelUtils().getChannel(), U.getMD5Utils().MD5_32(imei))
                .subscribeOn(Schedulers.io())
                .subscribe(new ApiObserver<ApiResult>() {
                    @Override
                    public void process(ApiResult obj) {
                        if (obj.getErrno() == 0) {
                            UserAccount userAccount = parseRsp(obj.getData(), "");
                            if (mode == 3) {
                                UmengStatistics.onProfileSignIn("wx", userAccount.getUid());
                            } else if (mode == 2) {
                                UmengStatistics.onProfileSignIn("qq", userAccount.getUid());
                            }
                        } else {
                            EventBus.getDefault().post(new VerifyCodeErrorEvent(obj.getErrno(), obj.getErrmsg()));
                        }
                    }
                });
    }

    UserAccount parseRsp(JSONObject jsonObject, String phoneNum) {
        String secretToken = jsonObject.getJSONObject("token").getString("T");
        String serviceToken = jsonObject.getJSONObject("token").getString("S");
        String rongToken = jsonObject.getJSONObject("token").getString("RC");
        com.alibaba.fastjson.JSONObject profileJO = jsonObject.getJSONObject("profile");
        long userID = profileJO.getLong("userID");
        String nickName = profileJO.getString("nickname");
        int sex = profileJO.getInteger("sex");
        String birthday = profileJO.getString("birthday");
        String avatar = profileJO.getString("avatar");
        String sign = profileJO.getString("signature");
        Location location = JSON.parseObject(profileJO.getString("location"), Location.class);

        boolean isFirstLogin = jsonObject.getBoolean("isFirstLogin");
        if (isFirstLogin) {
            U.getPreferenceUtils().setSettingLong("first_login_time", System.currentTimeMillis());
        }

        // 设置个人信息
        MyUserInfo myUserInfo = new MyUserInfo();
        myUserInfo.setUserId(userID);
        myUserInfo.setUserNickname(nickName);
        myUserInfo.setSex(sex);
        myUserInfo.setBirthday(birthday);
        myUserInfo.setAvatar(avatar);
        myUserInfo.setSignature(sign);
        myUserInfo.setLocation(location);
        MyUserInfoLocalApi.insertOrUpdate(myUserInfo);

        UserAccount userAccount = new UserAccount();
        userAccount.setPhoneNum(phoneNum);
        userAccount.setServiceToken(serviceToken);
        userAccount.setSecretToken(secretToken);
        userAccount.setRongToken(rongToken);
        userAccount.setUid(String.valueOf(userID));
        userAccount.setNickName(nickName);
        userAccount.setSex(sex);
        userAccount.setBirthday(birthday);
        userAccount.setNeedEditUserInfo(isFirstLogin);
        userAccount.setChannelId(HostChannelManager.getInstance().getChannelId());
        onLoginResult(userAccount);
        return userAccount;
    }

    /**
     * 用户主动退出登录
     */
    public void logoff() {
        logoff(false, AccountEvent.LogoffAccountEvent.REASON_SELF_QUIT, true);
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
        tryConnectRongIM();
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
                    UmengStatistics.onProfileSignOff();
                    UmengPush.clearAlias(userId);
                    MyUserInfoManager.getInstance().logoff();
                    EventBus.getDefault().post(new AccountEvent.LogoffAccountEvent(reason));
                    emitter.onComplete();
                }
            })
                    .subscribeOn(Schedulers.io())
                    .subscribe();
        }
    }

    // todo 检查昵称是否可用
    public void checkNickName(String nickname) {
        UserAccountServerApi userAccountServerApi = ApiManager.getInstance().createService(UserAccountServerApi.class);
        ApiMethods.subscribe(userAccountServerApi.checkNickName(nickname), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    boolean isValid = result.getData().getBoolean("isValid");
                    if (isValid) {
                        // 昵称可用
                    } else {
                        // 昵称不可用和理由
                        String unValidReason = result.getData().getString("unValidReason");
                    }
                }
            }
        });
    }

    public void tryConnectRongIM() {
        if (mHasTryConnentRM) {
            return;
        }
        if (mAccount != null) {
            String token = mAccount.getRongToken();
            if (!TextUtils.isEmpty(token)) {
                mHasTryConnentRM = true;
                connectRongIM(token);
            }
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
                if (result.getErrno() == 0) {
                    String token = result.getData().getString("RC");
                    if (!TextUtils.isEmpty(token)) {
                        // 更新数据库中融云token
                        mAccount.setRongToken(token);
                        UserAccountLocalApi.loginAccount(mAccount);
                        // 连接融云
                        connectRongIM(token);
                    } else {
                        MyLog.e(TAG, "getIMToken from Server is null");
                    }

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
                }

                @Override
                public void onFailed(Object obj, int errcode, String message) {
                    boolean result = (boolean) obj;
                    if (result) {
                        // todo 连接融云失败
                        U.getToastUtil().showShort("服务连接不可用");
                    } else {
                        // todo token有问题, 重试一次(可能过期或者appkey不一致等)
                        getIMToken();
                    }
                }
            });
        }
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEvent(UmengPushRegisterSuccessEvent event) {
        trySetUmengPushAlias();
    }

    /**
     * 给Umeng的push通道设置 Alias
     */
    void trySetUmengPushAlias() {
        if (UserAccountManager.getInstance().hasAccount()) {
            UmengPush.setAlias(UserAccountManager.getInstance().getUuid());
        }
    }
}
