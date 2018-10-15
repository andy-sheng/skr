package com.wali.live.livesdk.live.task;

import com.wali.live.task.ITaskCallBack;

/**
 * Created by lan on 15-12-7.
 * ITaskCallBack的包装器
 */
public class TaskCallBackWrapper implements ITaskCallBack {
    // 正常处理返回值
    public void process(Object object) {
    }

    //  多个参数的返回值处理
    public void processWithMore(Object... objects) {
    }

    //  特殊错误码处理
    public void processWithFailure(int errCode) {
    }

    @Override
    public void startProcess() {
    }

    public void process(Object object, boolean isClear) {
    }
}
