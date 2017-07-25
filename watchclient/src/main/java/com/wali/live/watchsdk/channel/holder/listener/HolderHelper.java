package com.wali.live.watchsdk.channel.holder.listener;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import com.base.activity.BaseSdkActivity;
import com.wali.live.watchsdk.scheme.SchemeSdkActivity;

/**
 * Created by lan on 16/7/15.
 *
 * @module 频道
 * @description ViewHolder辅助类：提供scheme跳转
 */
public class HolderHelper {
    private final static String TAG = HolderHelper.class.getSimpleName();

    /**
     * 点击入口
     */
    public static void jumpScheme(Context context, String uri) {
        if (!TextUtils.isEmpty(uri)) {
            SchemeSdkActivity.openActivity((BaseSdkActivity) context, Uri.parse(uri));
        }
    }
}
