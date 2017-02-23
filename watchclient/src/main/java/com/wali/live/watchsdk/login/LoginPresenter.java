package com.wali.live.watchsdk.login;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.os.Bundle;
import android.text.TextUtils;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.presenter.RxLifeCyclePresenter;
import com.base.utils.CommonUtils;
import com.base.utils.network.Network;
import com.base.utils.toast.ToastUtils;
import com.mi.live.data.account.XiaoMiOAuth;
import com.mi.live.data.account.event.AccountEventController;
import com.mi.live.data.account.login.LoginType;
import com.mi.live.data.account.task.AccountCaller;
import com.mi.live.data.account.task.ActionParam;
import com.mi.live.data.api.ErrorCode;
import com.mi.live.data.base.BaseSdkActivity;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.wali.live.proto.AccountProto;
import com.xiaomi.accountsdk.account.data.ExtendedAuthToken;

import java.io.IOException;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by lan on 16/11/21.
 */
public class LoginPresenter extends RxLifeCyclePresenter {
    private static final String TAG = LoginPresenter.class.getSimpleName();

    public static final String ACCOUNT_TYPE = "com.xiaomi";
    public static final String MI_LIVE_SID = "xmzhibo";

    private BaseSdkActivity mActivity;

    public LoginPresenter(BaseSdkActivity activity) {
        mActivity = activity;
    }

    /**
     * 系统小米账号登录
     */
    public void systemLogin(final int channelId) {
        MyLog.w(TAG, "systemLogin start");
        Observable
                .create(new Observable.OnSubscribe<Object>() {
                    @Override
                    public void call(Subscriber<? super Object> subscriber) {
                        systemLoginInner(channelId);
                        subscriber.onCompleted();
                    }
                })
                .subscribeOn(Schedulers.io())
                .subscribe();
    }

    // 再io线程执行
    public void systemLoginInner(final int channelId) {
        if (!Network.hasNetwork(GlobalData.app())) {
            return;
        }
        if (CommonUtils.isMIUI()) {
            String miAccount = CommonUtils.getSysMiAccount();
            if (!TextUtils.isEmpty(miAccount)) {
                if (!TextUtils.isDigitsOnly(miAccount) || !CommonUtils.isMIUI8()) {
                    // auth登录
                    miLogin();
                } else {
                    // miui8 自动登录
                    ssoLogin(Long.parseLong(miAccount), channelId);
                }
            } else {
                miLogin();
            }
        } else {
            miLogin();
        }
    }

    public void miLogin() {
        MyLog.w(TAG, "miLogin");
        Observable
                .create(new Observable.OnSubscribe<String>() {
                    @Override
                    public void call(Subscriber<? super String> subscriber) {
                        try {
                            String code = XiaoMiOAuth.getOAuthCode(mActivity);
                            if (!TextUtils.isEmpty(code)) {
                                MyLog.d(TAG, "miLogin code :" + code);
                                subscriber.onNext(code);
                                subscriber.onCompleted();
                            } else {
                                subscriber.onError(new Exception("code is empty"));
                            }
                        } catch (Exception e) {
                            subscriber.onError(e);
                        }

                        subscriber.onCompleted();
                    }
                })
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {
                        MyLog.d(TAG, "miLogin getOAuthCode onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MyLog.d(TAG, "miLogin getOAuthCode onError=" + e.getMessage());
                    }

                    @Override
                    public void onNext(String code) {
                        miLoginByCode(code);
                    }
                });
    }

    public void miLoginByCode(String code) {
        AccountCaller.login(-1, LoginType.LOGIN_XIAOMI, code, null, null, null, null)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ActionParam>() {
                    @Override
                    public void onCompleted() {
                        MyLog.w(TAG, "miLoginByCode login onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MyLog.w(TAG, "miLoginByCode login onError=" + e.getMessage());
                    }

                    @Override
                    public void onNext(ActionParam actionParam) {
                        MyLog.w(TAG, "miLoginByCode login onNext");
                        if (actionParam != null) {
                            int errCode = actionParam.getErrCode();
                            if (errCode == ErrorCode.CODE_SUCCESS) {
                                MyLog.w(TAG, "miLogin login success");
                                ToastUtils.showToast("milogin 登录成功");
                                AccountEventController.onActionLogin(AccountEventController.LoginEvent.EVENT_TYPE_LOGIN_SUCCESS);
                            } else if (errCode == ErrorCode.CODE_ACCOUT_FORBIDDEN) {
                                //todo
                                ToastUtils.showToast("milogin 账号禁用");
                            } else {
                                MyLog.d(TAG, "miLogin processAction failed");
                            }
                        }
                    }
                });
    }

    public void processAction(String action, int errCode, Object... objects) {
        MyLog.w(TAG, "processAction : " + action + " , errCode : " + errCode);
        switch (action) {
            case MiLinkCommand.COMMAND_LOGIN:
                if (errCode == ErrorCode.CODE_SUCCESS) {
                    MyLog.w(TAG, "miLogin login success");
                    ToastUtils.showToast("milogin 登录成功");
                    AccountEventController.onActionLogin(AccountEventController.LoginEvent.EVENT_TYPE_LOGIN_SUCCESS);
                } else if (errCode == ErrorCode.CODE_ACCOUT_FORBIDDEN) {
                    //todo
                    ToastUtils.showToast("milogin 账号禁用");
                } else {
                    MyLog.d(TAG, "miLogin processAction failed");
                }
                break;
        }
    }

    /**
     * sso登录
     */
    public void ssoLogin(final long miid, final int channelId) {
        MyLog.w(TAG, "ssoLogin start, miid=" + miid);
        try {
            Observable.just(0)
                    .compose(bindUntilEvent(PresenterEvent.DESTROY))
                    .map(new Func1<Object, String>() {
                        @Override
                        public String call(Object o) {
                            return getAuthToken(miid);
                        }
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<String>() {
                        @Override
                        public void call(String var) {
                            ssoLoginByAuthToken(miid, var, channelId);
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            MyLog.e(TAG, "ssoLogin error", throwable);
                        }
                    });
        } catch (Exception e) {
            MyLog.e(TAG, "ssoLogin error", e);
        }
    }

    /**
     * 获取直播miui系统帐号的authtoken
     */
    private String getAuthToken(long miid) {
        MyLog.w(TAG, "getAuthToken miid=" + miid);
        Account account = null;

        AccountManager am = AccountManager.get(GlobalData.app());
        Account[] accounts = am.getAccountsByType(ACCOUNT_TYPE);
        if (accounts != null && accounts.length > 0) {
            for (Account acct : accounts) {
                if (Long.parseLong(acct.name) == miid) {
                    account = acct;
                    break;
                }
            }
        }

        if (account == null) {
            MyLog.e(TAG, "account is null");
            return null;
        }

        try {
            AccountManagerFuture<Bundle> future = null;
            if (GlobalData.app() == null) {
                future = AccountManager.get(GlobalData.app()).getAuthToken(account, MI_LIVE_SID, null, true, null, null);
            } else {
                future = AccountManager.get(GlobalData.app()).getAuthToken(account, MI_LIVE_SID, null, mActivity, null, null);
            }
            if (future != null) {
                String authToken = future.getResult().getString(AccountManager.KEY_AUTHTOKEN);
                return authToken;
            }
        } catch (OperationCanceledException e) {
            MyLog.e(TAG, "get auth token error", e);
        } catch (AuthenticatorException e) {
            MyLog.e(TAG, "get auth token error", e);
        } catch (IOException e) {
            MyLog.e(TAG, "get auth token error", e);
        } catch (Exception e) {
            MyLog.e(TAG, "get auth token error", e);
        }
        return null;
    }

    /**
     * sso登录
     */
    private void ssoLoginByAuthToken(long miid, final String authToken, final int channelId) {
        MyLog.w(TAG, "ssoLoginByAuthToken miid=" + miid + " authToken=" + authToken);
        String serviceToken = getServiceToken(authToken);

        if (TextUtils.isEmpty(serviceToken)) {
            MyLog.d(TAG, "ssoLoginByAuthToken serviceToken is empty");
            return;
        }

        Observable<AccountProto.MiSsoLoginRsp> observable = AccountCaller.miSsoLogin(miid, serviceToken, channelId);
        observable.subscribeOn(Schedulers.io())
                .compose(this.<AccountProto.MiSsoLoginRsp>bindUntilEvent(PresenterEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<AccountProto.MiSsoLoginRsp>() {
                    @Override
                    public void onCompleted() {
                        MyLog.w(TAG, "miSsoLogin on completed");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MyLog.e(TAG, "miSsoLogin error", e);
                    }

                    @Override
                    public void onNext(AccountProto.MiSsoLoginRsp miSsoLoginRsp) {
                        try {
                            if (miSsoLoginRsp == null) {
                                MyLog.w(TAG, "miSsoLoginRsp is null");
                                return;
                            }
                            MyLog.w(TAG, "miSsoLogin retCode=" + miSsoLoginRsp.getRetCode());
                            if (miSsoLoginRsp.getRetCode() == ErrorCode.CODE_ACCOUT_FORBIDDEN) {
                                ToastUtils.showToast("账号被禁用");
                                // todo
                                return;
                            } else if (miSsoLoginRsp.getRetCode() != ErrorCode.CODE_SUCCESS) {
                                clearServiceToken(authToken);
                                return;
                            }

                            if (miSsoLoginRsp.getIsSetGuide()) {
                                MyLog.w(TAG, "miSsoLogin set userinfo");
                                ToastUtils.showToast("账号缺少头像昵称等基本信息");
                                //todo
                            } else {
                                MyLog.w(TAG, "miSsoLogin login success");
                                ToastUtils.showToast("登录成功");
                                AccountEventController.onActionLogin(AccountEventController.LoginEvent.EVENT_TYPE_LOGIN_SUCCESS);
                            }
                        } catch (Exception e) {
                            MyLog.w(TAG, "miSsoLogin error", e);
                            return;
                        }
                    }
                });
    }

    /**
     * 由authToken获取serviceToken
     *
     * @param authToken 直播miui系统帐号的authToken
     */
    private String getServiceToken(String authToken) {
        try {
            if (TextUtils.isEmpty(authToken)) {
                return null;
            }
            MyLog.w(TAG, "get service token start");
            ExtendedAuthToken token = ExtendedAuthToken.parse(authToken);
            if (token != null && !TextUtils.isEmpty(token.authToken)) {
                MyLog.w(TAG, "get service token success");
                return token.authToken;
            }
        } catch (Exception e) {
            MyLog.e(TAG, "get service token error", e);
        }
        MyLog.w(TAG, "get service token fail");
        return null;
    }

    /**
     * 清除直播的miui系统帐号token
     */
    private void clearServiceToken(String authToken) {
        MyLog.w(TAG, "clearServiceToken authToken=" + authToken);
        try {
            AccountManager am = AccountManager.get(mActivity);
            am.invalidateAuthToken(MI_LIVE_SID, authToken);
        } catch (Exception e) {
            MyLog.e(TAG, e);
        }
    }
}
