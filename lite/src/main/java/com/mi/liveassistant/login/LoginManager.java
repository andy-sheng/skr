package com.mi.liveassistant.login;

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
}
