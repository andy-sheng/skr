package com.wali.live.watchsdk.callback;

import com.base.log.MyLog;
import com.mi.live.data.account.HostChannelManager;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * 权限检查回调，用于除登录接口的其它接口
 *
 * Created by wuxiaoshan on 17-2-23.
 */
public abstract class SecureCommonCallBack implements ISecureCallBack {
    private String TAG = getClass().getSimpleName();
    @Override
    public void process(Object... objects) {
        int channelId = (int)objects[0];
        String packageName = (String)objects[1];
        HostChannelManager.getInstance().checkChannel(channelId,packageName)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Boolean>() {
                    @Override
                    public void onCompleted() {
                        MyLog.v(TAG,"onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MyLog.e(TAG,e);
                        postError();
                    }

                    @Override
                    public void onNext(Boolean aBoolean) {
                        MyLog.v(TAG, "onNext");
                        postProcess();
                    }
                });
    }

    public abstract void postProcess();

    public abstract void postError();
}
