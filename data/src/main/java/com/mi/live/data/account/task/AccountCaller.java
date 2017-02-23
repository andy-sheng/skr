package com.mi.live.data.account.task;

import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.account.login.AccountLoginManager;
import com.mi.live.data.api.ErrorCode;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.wali.live.dao.UserAccount;
import com.wali.live.proto.AccountProto;

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
                    AccountProto.MiSsoLoginRsp rsp = AccountLoginManager.miSsoLogin(mid, miservicetoken, channelId);

                    if (rsp != null && ErrorCode.CODE_SUCCESS == rsp.getRetCode()) {
//                        boolean isSameAccount = (UserAccountManager.getInstance().getUuidAsLong() == rsp.getUuid());
//                        if (!isSameAccount) {
//                            UserAccountManager.getInstance().clearDataFromDifAccount();
//                        }
                        UserAccount userAccount = new UserAccount();
                        userAccount.setChannelid(channelId);
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
    public static Observable<ActionParam> login(final int channelId, final int accountType, final String code, final String openId, final String accessToken, final String expires_in,
                                                final String refreshToken) {
        return Observable.create(new Observable.OnSubscribe<ActionParam>() {
            @Override
            public void call(Subscriber<? super ActionParam> subscriber) {
                AccountProto.LoginRsp rsp = AccountLoginManager.loginReq(accountType, code, openId, accessToken, expires_in, refreshToken, String.valueOf(channelId));
                if (rsp == null) {
                    subscriber.onError(new Exception("rsp is null"));
                    return;
                }

                ActionParam actionParam = new ActionParam();
                int errCode;
                if ((errCode = rsp.getRetCode()) == ErrorCode.CODE_SUCCESS) {

                    UserAccount userAccount = new UserAccount();
                    userAccount.setChannelid(channelId);
                    userAccount.setUuid(String.valueOf(rsp.getUuid()));
                    userAccount.setPassToken(rsp.getPassToken());
                    userAccount.setServiceToken(rsp.getServiceToken());
                    userAccount.setSSecurity(rsp.getSecurityKey());
                    userAccount.setNeedEditUserInfo(rsp.getIsSetGuide());

                    UserAccountManager.getInstance().login(userAccount);

                    Object[] params = new Object[8];
                    params[0] = rsp.getLoginStatus();
                    params[1] = rsp.getHasInnerAvatar();
                    params[2] = rsp.getHasInnerNickname();
                    params[3] = rsp.hasHasInnerSex();
                    params[4] = rsp.getHeadimgurl();
                    params[5] = rsp.getNickname();
                    params[6] = rsp.getSex();
                    params[7] = rsp.getIsSetGuide();
                    actionParam.setParams(params);
                }
                actionParam.setAction(MiLinkCommand.COMMAND_LOGIN);
                actionParam.setErrCode(errCode);

                subscriber.onNext(actionParam);
                subscriber.onCompleted();
            }
        });
    }
}
