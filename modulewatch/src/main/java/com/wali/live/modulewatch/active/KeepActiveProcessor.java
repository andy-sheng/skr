package com.wali.live.modulewatch.active;

import android.os.Process;

import com.common.log.MyLog;

import java.lang.reflect.Method;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by lan on 17/3/30.
 */
public class KeepActiveProcessor {
    private static final String TAG = KeepActiveProcessor.class.getSimpleName();

    private static final int ACTIVE_TYPE = 0x800;

    private static final int STATUS_RUNNING = 1;
    private static final int STATUS_STOP = 2;

    public static void keepActive() {
        Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(ObservableEmitter<Object> emitter) throws Exception {
                try {
                    Class clazz = Class.forName("com.miui.whetstone.WhetstoneManager");
                    Method method = clazz.getDeclaredMethod("updateActiveProcessStatus", int.class, int.class, int.class, int.class);
                    method.invoke(clazz, Process.myUid(), Process.myPid(), ACTIVE_TYPE, STATUS_RUNNING);
                    MyLog.d(TAG, "keepActive success, type=" + Integer.toHexString(ACTIVE_TYPE));
                } catch (Exception e) {
                    MyLog.e(e);
                }
            }
        }).subscribeOn(Schedulers.io())
                .subscribe();
    }

    public static void stopActive() {
        Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(ObservableEmitter<Object> emitter) throws Exception {
                try {
                    Class clazz = Class.forName("com.miui.whetstone.WhetstoneManager");
                    Method method = clazz.getDeclaredMethod("updateActiveProcessStatus", int.class, int.class, int.class, int.class);
                    method.invoke(clazz, Process.myUid(), Process.myPid(), ACTIVE_TYPE, STATUS_STOP);
                    MyLog.d(TAG, "stopActive success, type=" + Integer.toHexString(ACTIVE_TYPE));
                } catch (Exception e) {
                    MyLog.e(e);
                }
            }
        }).subscribeOn(Schedulers.io())
                .subscribe();
    }
}
