package com.wali.live.watchsdk.login;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.utils.CommonUtils;
import com.mi.live.data.account.HostChannelManager;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.account.login.AccountLoginManager;
import com.mi.live.data.api.ErrorCode;
import com.wali.live.dao.UserAccount;
import com.wali.live.proto.AccountProto;
import com.xiaomi.accountsdk.account.data.ExtendedAuthToken;

/**
 * Created by lan on 2017/7/7.
 */
public class LoginInternalService extends IntentService {
    private static final String TAG = "a";

    public LoginInternalService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        MyLog.w(TAG, "handleIntent");
        if (CommonUtils.isMIUI()) {
            String miAccount = CommonUtils.getSysMiAccount();
            if (TextUtils.isDigitsOnly(miAccount)) {
                long miid = Long.parseLong(miAccount);
                String authToken = getAuthToken(miid);
                int channelId = HostChannelManager.getInstance().getChannelId();

                MyLog.w(TAG, "handleIntent channelId=" + channelId);
                ssoLoginByAuthToken(miid, authToken, channelId);
            }
        }
    }

    private String getAuthToken(long miid) {
        MyLog.w(TAG, "getAuthToken miid=" + miid);
        Account account = null;

        AccountManager am = AccountManager.get(GlobalData.app());
        Account[] accounts = am.getAccountsByType(LoginPresenter.ACCOUNT_TYPE);
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
            future = AccountManager.get(GlobalData.app()).getAuthToken(account, LoginPresenter.MI_LIVE_SID, null, true, null, null);
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
     * sso登录
     */
    private void ssoLoginByAuthToken(long miid, String authToken, int channelId) {
        MyLog.w(TAG, "ssoLoginByAuthToken miid=" + miid);
        String serviceToken = getServiceToken(authToken);

        if (TextUtils.isEmpty(serviceToken)) {
            MyLog.d(TAG, "ssoLoginByAuthToken serviceToken is empty");
            return;
        }

        AccountProto.MiSsoLoginRsp rsp = AccountLoginManager.miSsoLogin(miid, serviceToken, channelId);

        if (rsp == null) {
            MyLog.w(TAG, "miSsoLoginRsp is null");
            return;
        }

        int code = rsp.getRetCode();
        MyLog.w(TAG, "miSsoLogin retCode=" + code);
        if (code == ErrorCode.CODE_ACCOUT_FORBIDDEN) {
            return;
        } else if (code != ErrorCode.CODE_SUCCESS) {
            return;
        }

        UserAccount userAccount = new UserAccount();
        userAccount.setChannelid(channelId);
        userAccount.setUuid(String.valueOf(rsp.getUuid()));
        userAccount.setPassToken(rsp.getPassToken());
        userAccount.setServiceToken(rsp.getServiceToken());
        userAccount.setSSecurity(rsp.getSecurityKey());
        userAccount.setNeedEditUserInfo(rsp.getIsSetGuide());
        UserAccountManager.getInstance().login(userAccount);
    }

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
}
