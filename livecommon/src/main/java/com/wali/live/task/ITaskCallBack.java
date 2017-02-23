package com.wali.live.task;

import com.base.utils.callback.ICommonCallBack;

/**
 * Created by lan on 15-11-14.
 */
public interface ITaskCallBack extends ICommonCallBack {
    //  多个参数的返回值处理
    void processWithMore(Object... params);

    //  特殊错误码处理
    void processWithFailure(int errCode);

    //开始处理的回调
    void startProcess();
}