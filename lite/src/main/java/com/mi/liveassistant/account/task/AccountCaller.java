package com.mi.liveassistant.account.task;



import com.mi.liveassistant.account.UserAccountManager;
import com.mi.liveassistant.account.login.AccountLoginManager;
import com.mi.liveassistant.common.api.ErrorCode;
import com.mi.liveassistant.dao.UserAccount;
import com.mi.liveassistant.proto.AccountProto;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by lan on 16/11/22.
 *
 * @description 从app AccountTask中迁移出来
 */
public class AccountCaller {
    /**
     * 小米帐号sso登录
     */
    public static Observable<AccountProto.MiSsoLoginRsp> miSsoLogin(final long mid, final String miservicetoken, final int channelId) {
        return Observable.create(new Observable.OnSubscribe<AccountProto.MiSsoLoginRsp>() {
            @Override
            public void call(Subscriber<? super AccountProto.MiSsoLoginRsp> subscriber) {
                try {
                    AccountProto.MiSsoLoginRsp rsp = AccountLoginManager.miSsoLogin(mid, miservicetoken);

                    if (rsp != null && ErrorCode.CODE_SUCCESS == rsp.getRetCode()) {
//                        boolean isSameAccount = (UserAccountManager.getInstance().getUuidAsLong() == rsp.getUuid());
//                        if (!isSameAccount) {
//                            UserAccountManager.getInstance().clearDataFromDifAccount();
//                        }
                        UserAccount userAccount = new UserAccount();
                        userAccount.setUuid(String.valueOf(rsp.getUuid()));
                        userAccount.setPassToken(rsp.getPassToken());
                        userAccount.setServiceToken(rsp.getServiceToken());
                        userAccount.setSSecurity(rsp.getSecurityKey());
                        userAccount.setNeedEditUserInfo(rsp.getIsSetGuide());
                        UserAccountManager.getInstance().login(userAccount);
                    }

                    subscriber.onNext(rsp);
                    subscriber.onCompleted();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    /**
     * passtoken换servicetoken
     */
    public static Observable<Integer> getServiceToken(final String passToken, final String uuid) {
        return Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                AccountProto.GetServiceTokenRsp rsp = AccountLoginManager.getServiceTokenReq(passToken, uuid);
                if (rsp == null) {
                    subscriber.onError(new Exception("rsp is null"));
                    return;
                }
                int errCode;
                if ((errCode = rsp.getRetCode()) == ErrorCode.CODE_SUCCESS) {
                    UserAccountManager userAccountManager = UserAccountManager.getInstance();
                    userAccountManager.setPassToken(rsp.getPassToken());
                    userAccountManager.setServiceToken(rsp.getServiceToken());
                    userAccountManager.setSSecurity(rsp.getSecurityKey());
                    userAccountManager.completeToken();
                }
                subscriber.onNext(errCode);
                subscriber.onCompleted();
            }
        });
    }

    /**
     * 第三方授权登陆
     */
    public static Observable<AccountProto.LoginRsp> login(final int channelId, final int accountType, final String code, final String openId, final String accessToken, final String expires_in,
                                                final String refreshToken) {
        return Observable.create(new Observable.OnSubscribe<AccountProto.LoginRsp>() {
            @Override
            public void call(Subscriber<? super AccountProto.LoginRsp> subscriber) {
                AccountProto.LoginRsp rsp = AccountLoginManager.loginReq(accountType, code, openId, accessToken, expires_in, refreshToken);
                if (rsp == null) {
                    subscriber.onError(new Exception("rsp is null"));
                    return;
                }

                int errCode;
                if ((errCode = rsp.getRetCode()) == ErrorCode.CODE_SUCCESS) {

                    UserAccount userAccount = new UserAccount();
                    userAccount.setUuid(String.valueOf(rsp.getUuid()));
                    userAccount.setPassToken(rsp.getPassToken());
                    userAccount.setServiceToken(rsp.getServiceToken());
                    userAccount.setSSecurity(rsp.getSecurityKey());
                    userAccount.setNeedEditUserInfo(rsp.getIsSetGuide());

                    UserAccountManager.getInstance().login(userAccount);
                }
                subscriber.onNext(rsp);
                subscriber.onCompleted();
            }
        });
    }

    /**
     * 第三方登录
     *
     * @param channelId
     * @param xuid
     * @param sex
     * @param nickName
     * @param headUrl
     * @param sign
     * @return
     */
    public static Observable<AccountProto.ThirdPartSignLoginRsp> login(final int channelId, final String xuid, final int sex, final String nickName, final String headUrl, final String sign) {
        return Observable.create(new Observable.OnSubscribe<AccountProto.ThirdPartSignLoginRsp>() {
            @Override
            public void call(Subscriber<? super AccountProto.ThirdPartSignLoginRsp> subscriber) {
                AccountProto.ThirdPartSignLoginRsp rsp = AccountLoginManager.thridPartLogin(xuid,sex,nickName,headUrl,sign);
                if (rsp == null) {
                    subscriber.onError(new Exception("rsp is null"));
                    return;
                }
                if ((rsp.getRetCode()) == ErrorCode.CODE_SUCCESS) {

                    UserAccount userAccount = new UserAccount();
                    userAccount.setUuid(String.valueOf(rsp.getUuid()));
                    userAccount.setPassToken(rsp.getPassToken());
                    userAccount.setServiceToken(rsp.getServiceToken());
                    userAccount.setSSecurity(rsp.getSecurityKey());
                    userAccount.setNeedEditUserInfo(false);

                    UserAccountManager.getInstance().login(userAccount);
                }
                subscriber.onNext(rsp);
                subscriber.onCompleted();
            }
        });
    }
}
