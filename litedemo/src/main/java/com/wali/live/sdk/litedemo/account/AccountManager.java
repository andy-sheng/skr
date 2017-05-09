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

    public static final String RSA_PRIVATE_KEY = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAMC8ISWECSak6Z1X" +
            "tgTy9jrq85dZ7Z95CndJ6Sz0ty5fiVqiJ4WrRf7d+78hlEOvlE0fwLQraHZ28gkD" +
            "kdNX1ycFDV+SBDTn+rFnRJQZjA8t3cQGiJmpyFIpaSzpz9PMTScxDmmxygUzsTXe" +
            "sCcFV8p9thCyJj5kGsUFxzkfwR7dAgMBAAECgYAqUCMmzVoE9eej94GqjHyqarKX" +
            "49JbVIOLtNpQWFlvAOJy12691eBEGBAQ4hpe0clJNVNlOrJwb6SrffEh6QL+2Aht" +
            "oocO7ST4kGpYTk53ofkK9AOwdZkhhzn226qRlDFN+OyAedLsv5sZ3166KTfxaCkO" +
            "5/KeXuD9BucT4eHTMQJBAPUGugXFJBVUZimsqwi5PKBGtmqQEJAi5M0vZvGz4vtq" +
            "H8pXQVYHOwAQA2Kmx7LSWqUa5EZCfKQIHE88dhmcru8CQQDJXd825pM0FW6ENr/9" +
            "IZLZBMgOlFG06WkVa442trbViGP0TPJMeEzBHoCDtlxDUxKcbFworXvVk+f8SYUo" +
            "6g7zAkAJSIb1vwFd+YOhYpRcUUBVxjgVE349J8VJbNlWoP0hj2TC8slb7Aw1NWYb" +
            "b7wzLzsV9E3fx5cXU+NWsTC8Sa5rAkEAw8DL4/UWmQVUoJcQ4KUoumwZh4LMQ1C8" +
            "5SPf5nSNHNwwPygmTAyOoRZj3KcE3jX9267DkI/F2ISmeu2F05Zl3QJAX8qggola" +
            "wpkdbvZn81X80lFuye6b0KjSWqlrrQLtjSR9/ov/avbuEDI+Ni4rDZn5a0rkGuaN" +
            "DzBZBemtWvPkjg==";

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
