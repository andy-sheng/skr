package com.wali.live.sdk.manager.aardemo;

import android.accounts.OperationCanceledException;
import android.app.Activity;

import com.xiaomi.account.openauth.XMAuthericationException;
import com.xiaomi.account.openauth.XiaomiOAuthConstants;
import com.xiaomi.account.openauth.XiaomiOAuthFuture;
import com.xiaomi.account.openauth.XiaomiOAuthResults;
import com.xiaomi.account.openauth.XiaomiOAuthorize;

import java.io.IOException;

/**
 * Created by linjinbin on 16/2/17.
 *
 * @module 小米账号登录
 */
public class XiaoMiOAuth {
    static final String TAG = XiaoMiOAuth.class.getSimpleName();

    static final String REDIRECT_URL = "https://login.game.xiaomi.com/zhibo/login";
    public static final long APP_ID_LOGIN = 2882303761517438806L;
    public static final String APP_KEY_LOGIN = "5431743870806";

    public static final long APP_ID_PAY;
    public static final String APP_KEY_PAY;

    static {
            APP_ID_PAY = 2882303761517438806L;
            APP_KEY_PAY = "5431743870806";
    }

    static final boolean sKeepCookies = true;

    /**
     * 获取小米OAuth登录的code
     * 这个是一个耗时的操作，不能在UI线程调用
     */
    public static final String getOAuthCode(final Activity activity) {
        XiaomiOAuthFuture<XiaomiOAuthResults> future = new XiaomiOAuthorize()
                .setAppId(APP_ID_LOGIN)
                .setRedirectUrl(REDIRECT_URL)
//                .setScope(getScopeFromUi()) //TODO 拿掉
                .setKeepCookies(sKeepCookies) // 不调的话默认是false
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

    private static int[] getScopeFromUi() {
        int[] scopes = new int[2];
        scopes[0] = XiaomiOAuthConstants.SCOPE_PROFILE;
        scopes[1] = XiaomiOAuthConstants.SCOPE_OPEN_ID;

        return scopes;
    }

}
