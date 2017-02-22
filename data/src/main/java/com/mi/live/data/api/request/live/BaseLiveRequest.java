package com.mi.live.data.api.request.live;

import com.mi.live.data.api.request.BaseRequest;

/**
 * Created by lan on 16-3-18.
 * LiveRequest的基类
 * </p>
 * 目前主要功能是提供一致的TAG,方便调试
 */
public abstract class BaseLiveRequest extends BaseRequest {
    private static final String TAG = "LiveApi";

    protected String getTag() {
        return TAG;
    }

    public BaseLiveRequest(String command,String action,String channelId){
        super(command,action,channelId);
    }
}
