package com.wali.live.modulewatch.utils;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.common.log.MyLog;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {
    private static final Pattern sAtPattern = Pattern.compile("@<(\\d+)>");
    public static long getAtTargetUserId(@Nullable String body) {
        if (TextUtils.isEmpty(body)) {
            return 0;
        }
        Matcher matcher = sAtPattern.matcher(body);
        if (matcher.find()) {
            String atTargetUserId = matcher.group(1);
            try {
                return Long.parseLong(atTargetUserId);
            } catch (Exception e) {
                MyLog.e("StringUtils", "parse Long fail, str:" + matcher.group(1));
                return 0;
            }
        }
        return 0;
    }
}
