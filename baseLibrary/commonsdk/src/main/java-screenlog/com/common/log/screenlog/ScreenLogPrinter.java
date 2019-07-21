package com.common.log.screenlog;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.util.Log;

import com.common.log.MyLog;
import com.common.utils.ActivityUtils;
import com.common.utils.U;
import com.elvishew.xlog.LogLevel;
import com.elvishew.xlog.printer.Printer;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.HashSet;

public class ScreenLogPrinter implements Printer {
    public final String TAG = "ScreenLogPrinter";

    SensorManager mSensorManager;

    LogSensorEventListener mLogSensorEventListener;

    LogListContainer mLogListContainer = new LogListContainer();

    public void setListener(LogListContainer.Listener object) {
        mLogListContainer.setListener(object);
    }

    public String getLogByTag(HashSet<String> set) {
        return mLogListContainer.getLogByTag(set);
    }

    public HashMap<String, Integer> getAllLogTags() {
        return mLogListContainer.getTagMap();
    }

    private static class ScreenLogPrinterHolder {
        private static final ScreenLogPrinter INSTANCE = new ScreenLogPrinter();
    }

    private ScreenLogPrinter() {
        registerSensor();
        EventBus.getDefault().register(this);
    }

    public static final ScreenLogPrinter getInstance() {
        return ScreenLogPrinterHolder.INSTANCE;
    }

    public void onDebugOpenFlagChange(boolean openSensor) {
        if (openSensor) {
            registerSensor();
        } else {
            unRegisterSensor();
        }
    }

    /**
     * 注册传感器，注意，这玩意非常耗电的，可得悠着点用
     */
    void registerSensor() {
        boolean debugOpen = MyLog.isDebugLogOpen();
        Log.e(TAG, "registerSensor debugOpen:" + debugOpen);
        if (debugOpen) {
            if (mSensorManager == null) {
                mSensorManager = (SensorManager) U.app().getSystemService(Activity.SENSOR_SERVICE);
                if (mSensorManager != null) {
                    //获取加速度传感器
                    Sensor mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                    if (mAccelerometerSensor != null) {
                        mLogSensorEventListener = new LogSensorEventListener();
                        mSensorManager.registerListener(mLogSensorEventListener, mAccelerometerSensor, SensorManager.SENSOR_DELAY_UI);
                    }
                }
            }
        }
    }

    /**
     * 反注册传感器
     */
    void unRegisterSensor() {
        Log.e(TAG, "unRegisterSensor");
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(mLogSensorEventListener);
            mSensorManager = null;
        }
    }

    @Override
    public void println(int logLevel, String tag, String msg) {
        // v级别的log就不打印到屏幕了
        if (logLevel > LogLevel.VERBOSE && MyLog.isDebugLogOpen()) {
            //传感器是开着的，接受日志
            LogModel logModel = new LogModel();
            logModel.ts = System.currentTimeMillis();
            logModel.level = logLevel;
            logModel.tag = tag;
            logModel.msg = msg;
            mLogListContainer.addLog(logModel);
        }
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEvent(ActivityUtils.ForeOrBackgroundChange e) {
        if (e.foreground) {
            // 如果app在前台
            registerSensor();
        } else {
            unRegisterSensor();
        }
    }
}
