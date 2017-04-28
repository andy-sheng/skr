package com.wali.live.sdk.litedemo.account;

import android.accounts.OperationCanceledException;
import android.app.Activity;

import com.xiaomi.account.openauth.XMAuthericationException;
import com.xiaomi.account.openauth.XiaomiOAuthFuture;
import com.xiaomi.account.openauth.XiaomiOAuthResults;
import com.xiaomi.account.openauth.XiaomiOAuthorize;

import java.io.IOException;

/**
 * Created by lan on 17/4/25.
 */
public class AccountManager {
    private static final String TAG = AccountManager.class.getSimpleName();

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
}
