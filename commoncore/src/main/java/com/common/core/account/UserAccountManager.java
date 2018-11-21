package com.common.core.account;


import com.common.core.account.event.AccountEvent;
import com.common.core.channel.HostChannelManager;
import com.common.core.login.LoginType;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.log.MyLog;
import com.common.milink.MiLinkClientAdapter;
import com.common.rx.RxRetryAssist;
import com.common.utils.U;
import com.wali.live.proto.Account.LoginRsp;
import com.wali.live.proto.Account.MiSsoLoginRsp;

import org.greenrobot.eventbus.EventBus;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;


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
            MiLinkClientAdapter.getInstance().setIsTouristMode(false);
            // MilinkChannelClientAdapter.getInstance().destroy();
            // 进入实名模式
            MiLinkClientAdapter.getInstance().initCallBackFirst();
            // 同步昵称等详细信息
            MyUserInfoManager.getInstance().init();

            EventBus.getDefault().post(new AccountEvent.SetAccountEvent());
        } else {

        }
        // 只有非游客模式才发已有账号的事件
        if (!MiLinkClientAdapter.getInstance().isTouristMode() && account != null) {

        }
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

    public void logoff(final boolean deleteAccount){
        if(mAccount!=null){
            Observable.create(new ObservableOnSubscribe<Object>() {
                @Override
                public void subscribe(ObservableEmitter<Object> emitter) throws Exception {
                    if(deleteAccount){
                        UserAccountLocalApi.delete(mAccount);
                    }else {
                        mAccount.setIsLogOff(true);
                        UserAccountLocalApi.insertOrReplace(mAccount);
                    }
                    mAccount = null;
                    emitter.onComplete();
                }
            })
                    .subscribeOn(Schedulers.io())
                    .subscribe();
        }
    }

    /**
     * 具体发登录请求
     **/

    public void loginByMiOauth(final String code) {
        Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(ObservableEmitter<Object> emitter) throws Exception {
                int channelId = HostChannelManager.getInstance().getChannelId();
                LoginRsp rsp = UserAccountServerApi.loginByThirdPartyOauthloginReq(LoginType.LOGIN_XIAOMI, code, null,
                        null, null, null
                        , String.valueOf(channelId));
                if (rsp == null) {
                    emitter.onError(new Exception("loginRsp == null"));
                    return;
                }
                if (rsp.getRetCode() == 0) {
                    //登录成功
                    UserAccount userAccount = new UserAccount();
                    userAccount.setChannelId(channelId);
                    userAccount.setUid(String.valueOf(rsp.getUuid()));
                    userAccount.setNickName(rsp.getNickname());
                    userAccount.setImgUrl(rsp.getHeadimgurl());
                    userAccount.setPassToken(rsp.getPassToken());
                    userAccount.setServiceToken(rsp.getServiceToken());
                    userAccount.setSSecurity(rsp.getSecurityKey());
                    userAccount.setNeedEditUserInfo(rsp.getIsSetGuide());
                    userAccount.setIsLogOff(false);

                    onLoginResult(userAccount);
                    emitter.onComplete();
                } else {
                    emitter.onError(new Exception("retcode = " + rsp.getRetCode()));
                }

            }
        }).subscribeOn(Schedulers.io())
                .retryWhen(new RxRetryAssist())
                .subscribe(new Observer<Object>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Object o) {

                    }

                    @Override
                    public void onError(Throwable e) {
                        U.getActivityUtils().showSnackbar(e.getMessage(), false);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void loginByMiSso(final long mid, final String token) {
        Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(ObservableEmitter<Object> emitter) throws Exception {
                MiSsoLoginRsp rsp = UserAccountServerApi.loginByMiSso(mid, token, HostChannelManager.getInstance().getChannelId());

                UserAccount userAccount = new UserAccount();
                userAccount.setChannelId(HostChannelManager.getInstance().getChannelId());
                userAccount.setUid(String.valueOf(rsp.getUuid()));
                userAccount.setPassToken(rsp.getPassToken());
                userAccount.setServiceToken(rsp.getServiceToken());
                userAccount.setSSecurity(rsp.getSecurityKey());
                userAccount.setNeedEditUserInfo(rsp.getIsSetGuide());
                userAccount.setIsLogOff(false);
//                userAccount.setMiid(miid);
                onLoginResult(userAccount);
                emitter.onComplete();
            }
        }).subscribeOn(Schedulers.io())
                .subscribe();
    }
}
