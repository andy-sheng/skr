package com.mi.liveassistant.login;

import android.support.annotation.NonNull;

import com.mi.liveassistant.account.login.LoginType;
import com.mi.liveassistant.account.task.AccountCaller;
import com.mi.liveassistant.common.log.MyLog;
import com.mi.liveassistant.proto.AccountProto;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static android.content.ContentValues.TAG;

/**
 * Created by chenyong on 2017/4/28.
 */

public class LoginManager {

    public static void loginByMiAccountOAuth(final int channelId, final String code) {
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
                    }

                    @Override
                    public void onNext(AccountProto.LoginRsp rsp) {
                        MyLog.w(TAG, "miLoginByCode login onNext");
                    }
                });
    }

    public static void thirdPartLogin(final int channelId, final String xuid, final String name,
                                      @NonNull final String headUrl, final int sex, final String sign) {
        AccountCaller.login(channelId, xuid, sex, name, headUrl, sign)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<AccountProto.ThirdPartSignLoginRsp>() {
                    @Override
                    public void onCompleted() {
                        MyLog.w(TAG, "thirdPartLogin login onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MyLog.w(TAG, "thirdPartLogin login onError=" + e);
                    }

                    @Override
                    public void onNext(AccountProto.ThirdPartSignLoginRsp rsp) {
                        MyLog.w(TAG, "thirdPartLogin login onNext, rsp=" + rsp);
                    }
                });
    }
}
