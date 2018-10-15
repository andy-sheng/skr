package com.wali.live.sdk.litedemo.base.activity;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;

import com.trello.rxlifecycle.ActivityEvent;
import com.trello.rxlifecycle.RxLifecycle;

import rx.Observable;
import rx.subjects.BehaviorSubject;

/**
 * Created by lan on 2017/4/10.
 */
public class RxActivity extends BaseActivity {
    private final BehaviorSubject<ActivityEvent> mLifecycleSubject = BehaviorSubject.create();

    @CheckResult
    public final <T> Observable.Transformer<T, T> bindUntilEvent(@NonNull ActivityEvent event) {
        return RxLifecycle.bindUntilActivityEvent(mLifecycleSubject, event);
    }

    @Override
    protected void onDestroy() {
        mLifecycleSubject.onNext(ActivityEvent.DESTROY);
        super.onDestroy();
    }

    public <T> Observable.Transformer<T, T> bindUntilEvent() {
        return bindUntilEvent(ActivityEvent.DESTROY);
    }
}
