package com.wali.live.watchsdk.login;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.os.Bundle;
import android.text.TextUtils;

import com.base.activity.BaseSdkActivity;
import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.presenter.RxLifeCyclePresenter;
import com.base.utils.network.Network;
import com.base.utils.toast.ToastUtils;
import com.mi.live.data.account.XiaoMiOAuth;
import com.mi.live.data.account.event.AccountEventController;
import com.mi.live.data.account.login.LoginType;
import com.mi.live.data.account.task.AccountCaller;
import com.mi.live.data.api.ErrorCode;
import com.wali.live.proto.AccountProto;
import com.xiaomi.accountsdk.account.data.ExtendedAuthToken;

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

    private static final int LOGIN_SSO = 1;
    private static final int LOGIN_OAUTH = 2;

    private BaseSdkActivity mActivity;
    private int mChannelId;
    private long mMiid;

    public LoginPresenter(BaseSdkActivity activity) {
        mActivity = activity;
    }

    /**
     * 小米账号登录，支持oauth和sso两种方式
     */
    public void miLogin(final int channelId) {
        mChannelId = channelId;

        MyLog.w(TAG, "miLogin enter channelId=" + mChannelId);
        if (!Network.hasNetwork(GlobalData.app())) {
            return;
        }
        MyLog.d(TAG, "miLogin start");
        Observable
                .create(new Observable.OnSubscribe<Integer>() {
                    @Override
                    public void call(Subscriber<? super Integer> subscriber) {
                        boolean isSupportSso = isSupportSso();
                        subscriber.onNext(isSupportSso ? LOGIN_SSO : LOGIN_OAUTH);
                        subscriber.onCompleted();
                    }
                })
                .subscribeOn(Schedulers.io())
                .compose(mActivity.<Integer>bindUntilEvent())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer loginType) {
                        MyLog.w(TAG, "miLogin call loginType=" + loginType);
                        if (loginType != null) {
                            miLoginInternal(loginType);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, "miLogin error", throwable);
                    }
                });
    }

    /**
     * 判断是否支持sso
     */
    private boolean isSupportSso() {
//        if (CommonUtils.isMIUI()) {
//            String miAccount = CommonUtils.getSysMiAccount();
//            if (!TextUtils.isEmpty(miAccount)) {
//                if (TextUtils.isDigitsOnly(miAccount) && CommonUtils.isMIUI8()) {
//                    mMiid = Long.parseLong(miAccount);
//                    MyLog.w(TAG, "isSupportSso miid=" + mMiid);
//                    return true;
//                }
//            }
//        }
        return false;
    }

    /**
     * 小米账号内部处理
     */
    private void miLoginInternal(int loginType) {
        switch (loginType) {
            case LOGIN_OAUTH:
                oauthLogin(mChannelId);
                break;
            case LOGIN_SSO:
                ssoLogin(mMiid, mChannelId);
                break;
        }
    }

    public void oauthLogin(final int channelId) {
        MyLog.w(TAG, "oauthLogin channelId=" + channelId);
        Observable
                .create(new Observable.OnSubscribe<String>() {
                    @Override
                    public void call(Subscriber<? super String> subscriber) {
                        String code = XiaoMiOAuth.getOAuthCode(mActivity);
                        MyLog.d(TAG, "presenter oauthcode=" + code);
                        subscriber.onNext(code);
                        subscriber.onCompleted();
                    }
                })
                .subscribeOn(Schedulers.io())
                .compose(mActivity.<String>bindUntilEvent())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String code) {
                        MyLog.w(TAG, "oauthLogin call");
                        oauthLoginByCode(channelId, code);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, "oauthLogin error", throwable);
                    }
                });
    }

    private void oauthLoginByCode(int channelId, String code) {
        AccountCaller.login(channelId, LoginType.LOGIN_XIAOMI, code, null, null, null, null)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<AccountProto.LoginRsp>() {
                    @Override
                    public void onCompleted() {
                        MyLog.w(TAG, "oauthLoginByCode onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MyLog.w(TAG, "oauthLoginByCode onError=" + e.getMessage());
                    }

                    @Override
                    public void onNext(AccountProto.LoginRsp rsp) {
                        MyLog.w(TAG, "oauthLoginByCode onNext");
                        if (rsp != null) {
                            int errCode = rsp.getRetCode();
                            MyLog.w(TAG, "oauthLoginByCode retCode=" + errCode);
                            if (errCode == ErrorCode.CODE_SUCCESS) {
                                ToastUtils.showToast("小米账号登录成功");
                                AccountEventController.onActionLogin(AccountEventController.LoginEvent.EVENT_TYPE_LOGIN_SUCCESS);
                            } else if (errCode == ErrorCode.CODE_ACCOUT_FORBIDDEN) {
                                ToastUtils.showToast("小米账号被禁用");
                            }
                        }
                    }
                });
    }

    public void ssoLogin(final long miid, final int channelId) {
        MyLog.w(TAG, "ssoLogin channelId=" + channelId);
        Observable.just(0)
                .map(new Func1<Object, String>() {
                    @Override
                    public String call(Object o) {
                        String autoToken = getAuthToken(miid);
                        return autoToken;
                    }
                })
                .subscribeOn(Schedulers.io())
                .compose(mActivity.<String>bindUntilEvent())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String autoToken) {
                        MyLog.w(TAG, "ssoLogin error authToken(null)=" + (autoToken == null));
                        // 如果没有获取到autoToken说明还是没有权限，所以换成oauth登录
                        if (!TextUtils.isEmpty(autoToken)) {
                            ssoLoginByAuthToken(miid, autoToken, channelId);
                        } else {
                            oauthLogin(channelId);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, "ssoLogin error", throwable);
                    }
                });
    }

    /**
     * 根据小米id获取authToken
     */
    public String getAuthToken(long miid) {
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
            if (mActivity == null) {
                future = AccountManager.get(GlobalData.app()).getAuthToken(account, MI_LIVE_SID, null, true, null, null);
            } else {
                future = AccountManager.get(GlobalData.app()).getAuthToken(account, MI_LIVE_SID, null, mActivity, null, null);
            }
            if (future != null) {
                String authToken = future.getResult().getString(AccountManager.KEY_AUTHTOKEN);
                return authToken;
            }
        } catch (Exception e) {
            MyLog.e(TAG, "get auth token error", e);
        }
        return null;
    }

    /**
     * 使用authToken进行sso登录
     */
    private void ssoLoginByAuthToken(long miid, final String authToken, final int channelId) {
        MyLog.w(TAG, "ssoLoginByAuthToken miid=" + miid);
        String serviceToken = getServiceToken(authToken);

        if (TextUtils.isEmpty(serviceToken)) {
            MyLog.d(TAG, "ssoLoginByAuthToken serviceToken is empty");
            return;
        }

        AccountCaller.miSsoLogin(miid, serviceToken, channelId)
                .subscribeOn(Schedulers.io())
                .compose(mActivity.<AccountProto.MiSsoLoginRsp>bindUntilEvent())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<AccountProto.MiSsoLoginRsp>() {
                    @Override
                    public void onCompleted() {
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
                                ToastUtils.showToast("小米账号被禁用");
                                return;
                            } else if (miSsoLoginRsp.getRetCode() != ErrorCode.CODE_SUCCESS) {
                                clearServiceToken(authToken);
                                return;
                            }

                            if (miSsoLoginRsp.getIsSetGuide()) {
                                MyLog.w(TAG, "miSsoLogin set userinfo");
                            } else {
                                MyLog.w(TAG, "miSsoLogin login success");
                                ToastUtils.showToast("小米账号登录成功");
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
     */
    private String getServiceToken(String authToken) {
        try {
            if (TextUtils.isEmpty(authToken)) {
                return null;
            }
            MyLog.w(TAG, "getServiceToken start");
            ExtendedAuthToken token = ExtendedAuthToken.parse(authToken);
            if (token != null && !TextUtils.isEmpty(token.authToken)) {
                MyLog.w(TAG, "getServiceToken success");
                return token.authToken;
            }
        } catch (Exception e) {
            MyLog.e(TAG, "getServiceToken error", e);
        }
        return null;
    }

    private void clearServiceToken(String authToken) {
        MyLog.w(TAG, "clearServiceToken");
        try {
            AccountManager am = AccountManager.get(mActivity);
            am.invalidateAuthToken(MI_LIVE_SID, authToken);
        } catch (Exception e) {
            MyLog.e(TAG, e);
        }
    }
}
