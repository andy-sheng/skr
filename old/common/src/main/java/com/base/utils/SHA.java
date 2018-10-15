/**
 * Copyright (C) 2013, Xiaomi Inc. All rights reserved.
 */

package com.base.utils;

import android.util.Base64;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class SHA {

    /**
     * copy com.xiaomi.accountsdk.utils.CloudCoder 的hashDeviceInfo函数
     * 与miui.cloud.CloudManager.getHashedDeviceId使用相同的SHA1算法，确保与方流统计组使用同样算法。
     * 增强了异常情况的处理。输入null时，返回""。 保证不会出现闪退。
     * *
     */
    public static String miuiSHA1(String plain) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA1");
            return Base64.encodeToString(md.digest(plain.getBytes()),
                    Base64.URL_SAFE).substring(0, 16);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

}

