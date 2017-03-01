package com.wali.live.init;

import com.base.log.MyLog;
import com.mi.live.data.milink.event.MiLinkEvent;
import com.wali.live.feedback.FeedBackController;
import com.wali.live.task.ITaskCallBack;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Date;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * 帮助application处理一下一些全局EventBus事件,它的生命周期也是全局的
 * Created by chengsimin on 16/7/1.
 */
public class EventBusDelegate {
    public final static String TAG = EventBusDelegate.class.getSimpleName();
    private static EventBusDelegate sInstance = new EventBusDelegate();

    private EventBusDelegate() {
    }

    public static EventBusDelegate getInstance() {
        return sInstance;
    }

    Subscription uploadLogSubscription;

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEvent(MiLinkEvent.RequestUploadLog event) {
        if (uploadLogSubscription != null && !uploadLogSubscription.isUnsubscribed()) {
            MyLog.w("RequestUploadLog already uploading");
            return;
        }
        uploadLogSubscription = Observable.just(0)
                .map(new Func1<Integer, Object>() {
                    @Override
                    public Object call(Integer integer) {
                        uploadLog();
                        return null;
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {

                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(throwable);
                    }
                });

    }

    private void uploadLog() {
        MyLog.w("RequestUploadLog begin zip");
        FeedBackController.zipUploadFile(new ArrayList<String>(), 0);
        MyLog.w("RequestUploadLog begin upload");
        FeedBackController.uploadLogFile("", "来自milink.push.log;时间:" + new Date().toString(), new ITaskCallBack() {
            @Override
            public void processWithMore(Object... params) {

            }

            @Override
            public void processWithFailure(int errCode) {

            }

            @Override
            public void startProcess() {

            }

            @Override
            public void process(Object object) {

            }
        });
        MyLog.w("RequestUpload Over");
    }
}
