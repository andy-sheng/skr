package com.wali.live.watchsdk.task;

/**
 * Created by lan on 15-11-14.
 */
public interface IActionCallBack {
    /*处理action*/
    void processAction(String action, int errCode, Object... objects);
}