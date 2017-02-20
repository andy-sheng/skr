package com.wali.live.channel.request;

import com.mi.live.data.api.request.BaseRequest;

/**
 * Created by lan on 16-3-18.
 *
 * @module 频道
 * @description ChannelRequest的基类
 * 目前主要功能是提供一致的TAG,方便调试
 */
public abstract class BaseChannelRequest extends BaseRequest {
    private static final String TAG = "ChannelApi";

    protected String getTag() {
        return TAG;
    }
}
