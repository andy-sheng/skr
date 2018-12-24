package com.common.rxretrofit;

import android.util.Log;

import com.alibaba.android.arouter.launcher.ARouter;
import com.common.core.account.UserAccountManager;
import com.common.log.MyLog;
import com.common.utils.U;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * 自定义的订阅者，加一层适配来处理各种常见问题
 * 比如 弹出错误提示
 *
 * @param <T>
 */
public abstract class ApiObserver<T> implements Observer<T> {
    public final static String API_TAG = "ApiObserver";

    @Override
    public void onSubscribe(Disposable d) {

    }

    public void onNext(T obj) {
        if (obj instanceof ApiResult) {
            ApiResult result = (ApiResult) obj;
            if (result.errno != 0) {
                if (MyLog.isDebugLogOpen()) {
                    U.getToastUtil().showShort("errno:" + result.errno + " errmsg:" + result.errmsg);
                }
            }
            // TODO: 2018/12/24 身份解析失败，需要调到登陆页面
            if (result.errno == 107) {
                UserAccountManager.getInstance().notifyAccountExpired();
            }
        }
        process(obj);
    }

    public abstract void process(T obj);

    @Override
    public void onError(Throwable e) {
        if (MyLog.isDebugLogOpen()) {
            String log = Log.getStackTraceString(e);
            U.getToastUtil().showShort(log);
        }
        MyLog.e(API_TAG, e);
    }

    @Override
    public void onComplete() {

    }
}
