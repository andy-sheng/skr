package com.wali.live.sdk.litedemo.account;

import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.os.RemoteException;

import com.mi.liveassistant.account.login.LoginType;
import com.mi.liveassistant.account.task.AccountCaller;
import com.mi.liveassistant.common.api.ErrorCode;
import com.mi.liveassistant.common.log.MyLog;
import com.mi.liveassistant.proto.AccountProto;
import com.wali.live.sdk.litedemo.utils.ToastUtils;
import com.xiaomi.account.openauth.XMAuthericationException;
import com.xiaomi.account.openauth.XiaomiOAuthFuture;
import com.xiaomi.account.openauth.XiaomiOAuthResults;
import com.xiaomi.account.openauth.XiaomiOAuthorize;

import java.io.IOException;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by lan on 17/4/25.
 */
public class LoginManager {
    private static final String TAG = LoginManager.class.getSimpleName();

    public static final String REDIRECT_URL = "https://login.game.xiaomi.com/zhibo/login";
    public static final long APP_ID_LOGIN = 2882303761517438806L;

    /**
     * 获取小米OAuth登录的code
     * 这个是一个耗时的操作，不能在UI线程调用
     */
    public static final String getOAuthCode(final Activity activity) {
        XiaomiOAuthFuture<XiaomiOAuthResults> future = new XiaomiOAuthorize()
                .setAppId(APP_ID_LOGIN)
                .setRedirectUrl(REDIRECT_URL)
                .startGetOAuthCode(activity);
        try {
            XiaomiOAuthResults result = future.getResult();
            if (null != result) {
                return result.getCode();
            }
        } catch (OperationCanceledException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XMAuthericationException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void loginByMiAccountOAuth(final int channelId, final String code) throws RemoteException {
        AccountCaller.login(channelId, LoginType.LOGIN_XIAOMI, code, null, null, null, null)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<AccountProto.LoginRsp>() {
                    @Override
                    public void onCompleted() {
                        MyLog.w(TAG, "miLoginByCode login onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MyLog.w(TAG, "miLoginByCode login onError=" + e.getMessage());
                        ToastUtils.showToast("login failure");
                    }

                    @Override
                    public void onNext(AccountProto.LoginRsp rsp) {
                        MyLog.w(TAG, "miLoginByCode login onNext");
                        if (rsp.getRetCode() == ErrorCode.CODE_SUCCESS) {
                            ToastUtils.showToast("login success");
                        } else {
                            ToastUtils.showToast("login failure");
                        }
                    }
                });
    }
}
