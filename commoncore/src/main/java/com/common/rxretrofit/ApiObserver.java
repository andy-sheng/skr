package com.common.rxretrofit;

import android.text.TextUtils;
import android.util.Log;

import com.alibaba.android.arouter.launcher.ARouter;
import com.common.core.account.UserAccountManager;
import com.common.log.MyLog;
import com.common.utils.U;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import retrofit2.adapter.rxjava2.HttpException;

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
            } else if (result.errno == 0) {
                UserAccountManager.getInstance().accountValidFromServer();
            }
        }
        process(obj);
    }

    public abstract void process(T obj);

    @Override
    public void onError(Throwable e) {
        String log = Log.getStackTraceString(e);
        if (TextUtils.isEmpty(log)) {
            log = e.getMessage();
        }
        if (MyLog.isDebugLogOpen()) {
            if (!TextUtils.isEmpty(log)) {
                U.getToastUtil().showShort(log);
            }
        }
        MyLog.e(API_TAG, log);
        if (e instanceof UnknownHostException) {
            onNetworkError(ErrorType.unknownHost);
        } else if (e instanceof SocketTimeoutException) {
            onNetworkError(ErrorType.socketTimeout);
        }else if(e instanceof HttpException){

        }
    }

    @Override
    public void onComplete() {

    }


    /**
     * 业务方想处理超时逻辑，请覆盖这个方法
     *
     * @param errorType
     */
    public void onNetworkError(ErrorType errorType) {

    }

    public enum ErrorType {
        unknownHost,// 解析域名失败，一般无网络情况会有这个
        socketTimeout,// 超时，弱网络情况下容易触发这个
        http404,// 接口不存在
    }
}
