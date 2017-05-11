package com.mi.liveassistant.room.manager;

import android.support.annotation.Nullable;

import com.mi.liveassistant.room.RoomConstant;

import component.EventController;

/**
 * Created by yangli on 2017/5/9.
 */
public class LiveEventController extends EventController {
    private final static String TAG = "LiveEventController";

    // 推流相关
    private static final int MSG_PUSH_STREAM_FIRST = 10000;
    public static final int MSG_BEGIN_LIVE_SUCCESS = MSG_PUSH_STREAM_FIRST; // 开始直播 成功
    public static final int MSG_BEGIN_LIVE_FAILED = MSG_PUSH_STREAM_FIRST + 1; // 开始直播 失败
    public static final int MSG_END_LIVE_SUCCESS = MSG_PUSH_STREAM_FIRST + 2; // 结束直播 成功
    public static final int MSG_END_LIVE_FAILED = MSG_PUSH_STREAM_FIRST + 3; // 结束直播 失败
    // 拉流相关
    private static final int MSG_PULL_STREAM_FIRST = 20000;
    public static final int MSG_ENTER_LIVE_SUCCESS = MSG_PULL_STREAM_FIRST; // 开始观看 成功
    public static final int MSG_ENTER_LIVE_FAILED = MSG_PULL_STREAM_FIRST + 1; // 开始观看 失败

    @Nullable
    @Override
    protected String getTAG() {
        return RoomConstant.LOG_PREFIX + TAG;
    }
}
