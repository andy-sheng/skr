package com.mi.liveassistant.common.api;

/**
 * Created by lan on 15-12-1.<br/>
 * For latest, visit <a>http://wiki.n.miui.com/pages/viewpage.action?pageId=18995829</a>
 */
public class ErrorCode {
    // 0：任何业务返回0，都代表成功
    public static final int CODE_SUCCESS = 0;
    public static final int CODE_ERROR_NORMAL = -1;
    public static final int CODE_ERROR_MISSING_PROJECTION = -2;
    public static final int CODE_ERROR_TOO_LOW_SDK = -3;

    public static final int CODE_ACCOUT_FORBIDDEN = 6021;
}