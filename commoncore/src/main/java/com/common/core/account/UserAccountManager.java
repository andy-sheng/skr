package com.common.core.account;


import com.alibaba.fastjson.JSON;
import com.common.core.account.event.AccountEvent;
import com.common.core.channel.HostChannelManager;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.log.MyLog;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.U;

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
        long channelId = HostChannelManager.getInstance().getChannelId();
        UserAccount userAccount = UserAccountLocalApi.getUserAccount(channelId);
        setAccount(userAccount);
    }

    public void onLoginResult(UserAccount account) {
        MyLog.w(TAG, "login" + " account=" + account);
        if (account != null) {
            // 登出所有其他账号
            UserAccountLocalApi.loginAccount(account);
            // 用户登录成功，这里应该是要发出通知的
            setAccount(account);
            U.getActivityUtils().showSnackbar("登录成功", false);
        }
    }

    private void setAccount(UserAccount account) {
        mAccount = account;
        if (account != null) {
            // 取消匿名模式
//            MiLinkClientAdapter.getInstance().setIsTouristMode(false);
            // MilinkChannelClientAdapter.getInstance().destroy();
            // 进入实名模式
//            MiLinkClientAdapter.getInstance().initCallBackFirst();
            // 同步昵称等详细信息
            MyUserInfoManager.getInstance().init();

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
            return mAccount.getSSecurity();
        }
        return "";
    }

    public void logoff(final boolean deleteAccount) {
        if (mAccount != null) {
            Observable.create(new ObservableOnSubscribe<Object>() {
                @Override
                public void subscribe(ObservableEmitter<Object> emitter) throws Exception {
                    if (deleteAccount) {
                        UserAccountLocalApi.delete(mAccount);
                        ApiManager.getInstance().clearCookies();
                    } else {
                        mAccount.setIsLogOff(true);
                        UserAccountLocalApi.insertOrReplace(mAccount);
                        ApiManager.getInstance().clearCookies();
                    }
                    mAccount = null;
                    emitter.onComplete();
                }
            })
                    .subscribeOn(Schedulers.io())
                    .subscribe();
        }
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

    public void loginByPhoneNum(String phoneNum, String verifyCode) {
        UserAccountServerApi userAccountServerApi = ApiManager.getInstance().createService(UserAccountServerApi.class);
        // 1 为手机登录
        userAccountServerApi.login(1, phoneNum, verifyCode)
                .subscribeOn(Schedulers.io())
                .subscribe(new ApiObserver<ApiResult>() {
                    @Override
                    public void process(ApiResult obj) {
                        if (obj.getErrno() == 0) {
                            String passToken = obj.getData().getJSONObject("token").getString("T");
                            String serviceToken = obj.getData().getJSONObject("token").getString("S");

                            com.alibaba.fastjson.JSONObject profileJO = obj.getData().getJSONObject("profile");
                            long userID = profileJO.getLong("userID");
                            String nickName = profileJO.getString("nickname");
                            int sex = profileJO.getInteger("sex");
                            String birthday = profileJO.getString("birthday");

                            boolean isFirstLogin = obj.getData().getBoolean("isFirstLogin");

                            UserAccount userAccount = new UserAccount();
                            userAccount.setPassToken(passToken);
                            userAccount.setServiceToken(serviceToken);
                            userAccount.setUid(String.valueOf(userID));
                            userAccount.setNickName(nickName);
                            setAccount(userAccount);
                        }
                    }
                });

    }

    /**
     * 更新用户信息
     * @param nickName
     * @param sex
     * @param birthday
     */
    public void updateInfo(String nickName, int sex, String birthday) {
        HashMap<String,Object> map = new HashMap<>();
        map.put("nickname", nickName);
        map.put("sex", sex);
        map.put("birthday", birthday);

        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),  JSON.toJSONString(map));
        UserAccountServerApi userAccountServerApi = ApiManager.getInstance().createService(UserAccountServerApi.class);
        Observable<ApiResult> apiResultObservable = userAccountServerApi.updateInfo(body);
        ApiMethods.subscribe(apiResultObservable, null);
    }
}
