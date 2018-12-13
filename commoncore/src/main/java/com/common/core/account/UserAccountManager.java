package com.common.core.account;


import android.text.TextUtils;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.common.core.account.event.AccountEvent;
import com.common.core.account.event.VerifyCodeErrorEvent;
import com.common.core.channel.HostChannelManager;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.log.MyLog;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.statistics.UmengStatistics;
import com.common.utils.U;
import com.module.ModuleServiceManager;
import com.module.common.ICallback;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

import java.util.HashMap;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;


/**
 * 保存账号token等信息
 * 数据库支持多账号的
 * Created by chengsimin on 16/7/1.
 */
public class UserAccountManager {
    private static final String TAG = UserAccountManager.class.getSimpleName();

    private UserAccount mAccount;

    private static class UserAccountManagerHolder {
        private static final UserAccountManager INSTANCE = new UserAccountManager();
    }

    private UserAccountManager() {

    }

    public static final UserAccountManager getInstance() {
        return UserAccountManagerHolder.INSTANCE;
    }

    public void init() {
        MyLog.d(TAG, "init");
        long channelId = HostChannelManager.getInstance().getChannelId();
        UserAccount userAccount = UserAccountLocalApi.getUserAccount(channelId);
        setAccount(userAccount);
    }

    public void onLoginResult(UserAccount account) {
        MyLog.w(TAG, "login" + " account=" + account);
        if (account != null) {
            account.setIsLogOff(false);
            // 登出所有其他账号
            UserAccountLocalApi.loginAccount(account);
            // 用户登录成功，这里应该是要发出通知的
            setAccount(account);
            U.getActivityUtils().showSnackbar("登录成功", false);
        }
    }

    private void setAccount(UserAccount account) {
        MyLog.d(TAG, "setAccount" + " account=" + account);
        mAccount = account;
        if (account != null) {
            // 取消匿名模式
//            MiLinkClientAdapter.getInstance().setIsTouristMode(false);
            // MilinkChannelClientAdapter.getInstance().destroy();
            // 进入实名模式
//            MiLinkClientAdapter.getInstance().initCallBackFirst();
            // 同步昵称等详细信息
            MyUserInfoManager.getInstance().init();

            // 与融云服务器建立连接
            connectRongIM(account.getRongToken());

            EventBus.getDefault().post(new AccountEvent.SetAccountEvent());
        } else {

        }
        // 只有非游客模式才发已有账号的事件
    }

    public void setAnonymousId(long anonymousId) {

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

//    /**
//     * 具体发登录请求
//     **/
//    public void loginByMiOauth(final String code) {
//        Observable.create(new ObservableOnSubscribe<Object>() {
//            @Override
//            public void subscribe(ObservableEmitter<Object> emitter) throws Exception {
//                int channelId = HostChannelManager.getInstance().getChannelId();
//                LoginRsp rsp = UserAccountServerApi.loginByThirdPartyOauthloginReq(LoginType.LOGIN_XIAOMI, code, null,
//                        null, null, null
//                        , String.valueOf(channelId));
//                if (rsp == null) {
//                    emitter.onError(new Exception("loginRsp == null"));
//                    return;
//                }
//                if (rsp.getRetCode() == 0) {
//                    //登录成功
//                    UserAccount userAccount = new UserAccount();
//                    userAccount.setChannelId(channelId);
//                    userAccount.setUid(String.valueOf(rsp.getUuid()));
//                    userAccount.setNickName(rsp.getNickname());
//                    userAccount.setImgUrl(rsp.getHeadimgurl());
//                    userAccount.setPassToken(rsp.getPassToken());
//                    userAccount.setServiceToken(rsp.getServiceToken());
//                    userAccount.setSSecurity(rsp.getSecurityKey());
//                    userAccount.setNeedEditUserInfo(rsp.getIsSetGuide());
//                    userAccount.setIsLogOff(false);
//
//                    onLoginResult(userAccount);
//                    emitter.onComplete();
//                } else {
//                    emitter.onError(new Exception("retcode = " + rsp.getRetCode()));
//                }
//
//            }
//        }).subscribeOn(Schedulers.io())
//                .retryWhen(new RxRetryAssist())
//                .subscribe(new Observer<Object>() {
//                    @Override
//                    public void onSubscribe(Disposable d) {
//
//                    }
//
//                    @Override
//                    public void onNext(Object o) {
//
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        U.getActivityUtils().showSnackbar(e.getMessage(), false);
//                    }
//
//                    @Override
//                    public void onComplete() {
//
//                    }
//                });
//    }
//
//    public void loginByMiSso(final long mid, final String token) {
//        Observable.create(new ObservableOnSubscribe<Object>() {
//            @Override
//            public void subscribe(ObservableEmitter<Object> emitter) throws Exception {
//                MiSsoLoginRsp rsp = UserAccountServerApi.loginByMiSso(mid, token, HostChannelManager.getInstance().getChannelId());
//
//                UserAccount userAccount = new UserAccount();
//                userAccount.setChannelId(HostChannelManager.getInstance().getChannelId());
//                userAccount.setUid(String.valueOf(rsp.getUuid()));
//                userAccount.setPassToken(rsp.getPassToken());
//                userAccount.setServiceToken(rsp.getServiceToken());
//                userAccount.setSSecurity(rsp.getSecurityKey());
//                userAccount.setNeedEditUserInfo(rsp.getIsSetGuide());
//                userAccount.setIsLogOff(false);
////                userAccount.setMiid(miid);
//                onLoginResult(userAccount);
//                emitter.onComplete();
//            }
//        }).subscribeOn(Schedulers.io())
//                .subscribe();
//    }

    // 手机登录
    public void loginByPhoneNum(String phoneNum, String verifyCode) {
        UserAccountServerApi userAccountServerApi = ApiManager.getInstance().createService(UserAccountServerApi.class);
        // 1 为手机登录
        userAccountServerApi.login(1, phoneNum, verifyCode)
                .subscribeOn(Schedulers.io())
                .subscribe(new ApiObserver<ApiResult>() {
                    @Override
                    public void process(ApiResult obj) {
                        if (obj.getErrno() == 0) {
                            String secretToken = obj.getData().getJSONObject("token").getString("T");
                            String serviceToken = obj.getData().getJSONObject("token").getString("S");
                            String rongToken = obj.getData().getJSONObject("token").getString("RC");
                            com.alibaba.fastjson.JSONObject profileJO = obj.getData().getJSONObject("profile");
                            long userID = profileJO.getLong("userID");
                            String nickName = profileJO.getString("nickname");
                            int sex = profileJO.getInteger("sex");
                            String birthday = profileJO.getString("birthday");
                            boolean isFirstLogin = obj.getData().getBoolean("isFirstLogin");

                            UserAccount userAccount = new UserAccount();
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
                            UmengStatistics.onProfileSignIn("phone", userAccount.getUid());
                        } else {
                            EventBus.getDefault().post(new VerifyCodeErrorEvent(obj.getErrno(), obj.getErrmsg()));
                        }
                    }
                });

    }

    // todo 登出
    public void logOut(final boolean deleteAccount) {
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

    // todo 检查昵称是否可用
    public void checkNickName(String nickname) {
        UserAccountServerApi userAccountServerApi = ApiManager.getInstance().createService(UserAccountServerApi.class);
        ApiMethods.subscribe(userAccountServerApi.checkNickName(nickname), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    boolean isValid = result.getData().getBoolean("isValid");
                    if (isValid){
                        // 昵称可用
                    }else {
                        // 昵称不可用和理由
                        String unValidReason = result.getData().getString("unValidReason");
                    }
                }
            }
        });
    }

    // 获取IM的token
    public void getIMToken() {
        UserAccountServerApi userAccountServerApi = ApiManager.getInstance().createService(UserAccountServerApi.class);
        ApiMethods.subscribe(userAccountServerApi.getIMToken(), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    String token = result.getData().getString("RC");
                    if (!TextUtils.isEmpty(token)) {
                        connectRongIM(token);
                    } else {
                        MyLog.e(TAG, "getIMToken from Server is null");
                    }

                }
            }
        });
    }

    public void connectRongIM(String rongToken) {
        if (TextUtils.isEmpty(rongToken)) {
            getIMToken();
        } else {
            ModuleServiceManager.getInstance().getMsgService().connectRongIM(rongToken, new ICallback() {
                @Override
                public void onSucess(Object obj) {
                    U.getToastUtil().showShort("与融云服务器连接成功");
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

}
