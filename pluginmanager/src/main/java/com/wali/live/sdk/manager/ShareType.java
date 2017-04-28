package com.wali.live.sdk.manager;

import android.support.annotation.Keep;

/**
 * Created by lan on 17/4/27.
 */
@Keep
public class ShareType {
    public static final int TYPE_WEIXIN = 0x1;          // 微信好友
    public static final int TYPE_MOMENT = 0x1 << 1;     // 微信朋友圈
    public static final int TYPE_QQ = 0x1 << 2;         // QQ好友
    public static final int TYPE_QZONE = 0x1 << 3;      // QQ空间
    public static final int TYPE_BLOG = 0x1 << 4;       // 微博
    public static final int TYPE_FACEBOOK = 0x1 << 5;   // Facebook
    public static final int TYPE_TWITTER = 0x1 << 6;    // Twitter
    public static final int TYPE_INSTAGRAM = 0x1 << 7;  // Instagram
    public static final int TYPE_WHATSAPP = 0x1 << 8;   // Whatsapp
    public static final int TYPE_MILIAO = 0x1 << 9;     // 米聊
    public static final int TYPE_FEEDS = 0x1 << 10;     // 米聊广播

    public static final int TYPE_MASK = 0x7ff;

    protected static boolean check() {
        return TYPE_MASK == (TYPE_WEIXIN | TYPE_MOMENT | TYPE_QQ | TYPE_QZONE | TYPE_BLOG |
                TYPE_FACEBOOK | TYPE_TWITTER | TYPE_INSTAGRAM | TYPE_WHATSAPP |
                TYPE_MILIAO | TYPE_FEEDS);
    }
}
