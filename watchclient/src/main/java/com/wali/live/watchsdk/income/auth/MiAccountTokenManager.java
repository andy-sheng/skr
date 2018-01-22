package com.wali.live.watchsdk.income.auth;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.base.log.MyLog;
import com.mi.live.data.account.XiaoMiOAuth;
import com.mi.live.data.api.ErrorCode;
import com.wali.live.income.model.ExceptionWithCode;
import com.wali.live.proto.MibiTicketProto;
import com.wali.live.watchsdk.income.net.GetMiAccessTokenByCodeRequest;
import com.wali.live.watchsdk.income.net.RefreshMiAccessTokenByCodeRequest;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

/**
 * Created by rongzhisheng on 16-12-16.
 */

public class MiAccountTokenManager {
    private static final String TAG = MiAccountTokenManager.class.getSimpleName();

    /**
     * 返回的accessToken和refreshToken信息需要{@link MiAccountToken#setTokens(String, String)}
     *
     * @param activity
     * @return
     */
    public static Observable<MibiTicketProto.OpenAccountInfo> jumpToAuthorizedPage(@NonNull final Activity activity) {
        return Observable.create(new Observable.OnSubscribe<MibiTicketProto.OpenAccountInfo>() {
            @Override
            public void call(Subscriber<? super MibiTicketProto.OpenAccountInfo> subscriber) {
                try {
                    String code = XiaoMiOAuth.getOAuthCode(activity);
                    MyLog.d(TAG, "code:" + code);
                    if (!TextUtils.isEmpty(code)) {
                        GetMiAccessTokenByCodeRequest getMiAccessTokenByCodeRequest = new GetMiAccessTokenByCodeRequest(code);
                        MibiTicketProto.GetMiAccessTokenByCodeRsp accessRsp = getMiAccessTokenByCodeRequest.syncRsp();
                        if (accessRsp != null) {
                            if (accessRsp.getRetCode() == ErrorCode.CODE_SUCCESS) {
                                MibiTicketProto.OpenAccountInfo openAccountInfo = accessRsp.getOpenAccountInfo();
                                subscriber.onNext(openAccountInfo);
                                subscriber.onCompleted();
                            } else {
                                subscriber.onError(new ExceptionWithCode(accessRsp.getRetCode()));
                            }
                        } else {
                            subscriber.onError(new Exception("GetMiAccessTokenByCodeRsp is null"));
                        }
                    } else {
                        subscriber.onError(new Exception("code is empty"));
                    }
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        })
                .subscribeOn(Schedulers.io());
    }

    /**
     * 调用成功后需要{@link MiAccountToken#setTokens(String, String)}
     *
     * @return
     */
    public static Observable<MibiTicketProto.OpenAccountInfo> refreshAccessToken() {
        return Observable.create(new Observable.OnSubscribe<MibiTicketProto.OpenAccountInfo>() {
            @Override
            public void call(Subscriber<? super MibiTicketProto.OpenAccountInfo> subscriber) {
                RefreshMiAccessTokenByCodeRequest refreshMiAccessTokenByCodeRequest = new RefreshMiAccessTokenByCodeRequest(MiAccountToken.getTokens().second);
                MibiTicketProto.RefreshMiAccessTokenByCodeRsp refreshRsp = refreshMiAccessTokenByCodeRequest.syncRsp();
                if (refreshRsp != null) {
                    if (refreshRsp.getRetCode() == ErrorCode.CODE_SUCCESS) {
                        MibiTicketProto.OpenAccountInfo openAccountInfo = refreshRsp.getOpenAccountInfo();
                        subscriber.onNext(openAccountInfo);
                        subscriber.onCompleted();
                    } else {
                        subscriber.onError(new ExceptionWithCode(refreshRsp.getRetCode()));
                    }
                } else {
                    subscriber.onError(new Exception("OpenAccountInfo is null"));
                }
            }
        })
                .subscribeOn(Schedulers.io());
    }


}
