package com.common.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.*
import com.common.guard.IpcCallback
import com.common.log.MyLog
import com.common.playcontrol.RemoteControlEvent
import com.common.utils.U
import org.greenrobot.eventbus.EventBus
import java.lang.Exception

class SensorManagerHelper : SensorEventListener {

    val TAG = "SensorManagerHelper"

    // 传感器管理器
    private var sensorManager: SensorManager? = null

    // 手机震动
    private var vibrator: Vibrator? = null
    private var isShake = false
    private val mUiHandler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message?) {
            super.handleMessage(msg)
        }
    }

    private val FORCE_THRESHOLD = 1200
    private val TIME_THRESHOLD = 100
    private val SHAKE_TIMEOUT = 400
    private val SHAKE_DURATION = 2500
    private val SHAKE_COUNT = 4

    private var mLastX = -1.0f
    private var mLastY = -1.0f
    private var mLastZ = -1.0f
    private var mLastTime: Long = 0
    private var mShakeCount = 0
    private var mLastShake: Long = 0
    private var mLastForce: Long = 0

    init {
        vibrator = U.app().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?
    }

    internal fun startSensor() {
        MyLog.d(TAG, "startSensor")
        sensorManager = U.app().getSystemService(Context.SENSOR_SERVICE) as SensorManager?
        /**
        SensorManager.SENSOR_DELAY_FASTEST： 以最快的速度获得传感器数据
        SENSOR_DELAY_GAME： 适合与在游戏中获得传感器数据
        SENSOR_DELAY_NORMAL： 以一般的速度获得传感器数据
        SENSOR_DELAY_UI ：适合于在ui空间中获得数据
        这四种更新速率依次递减
         */
        sensorManager?.registerListener(this, sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI)
        //sensorManager?.registerListener(this, sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER), SensorManager.SENSOR_DELAY_UI)
        //sensorManager?.registerListener(this, sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR), SensorManager.SENSOR_DELAY_UI)


        //val pm = U.app().getSystemService(Context.POWER_SERVICE) as PowerManager?
        //val wakeLock = pm?.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"com.zq.live:sensorlock")
        //wakeLock?.acquire()
    }

    internal fun stopSensor() {
        MyLog.d(TAG, "stopSensor")
        sensorManager?.unregisterListener(this)
    }

    fun register() {
        bindSensorService {
            try {
                it?.call(1, null, object : IpcCallback.Stub() {
                    override fun callback(type: Int, json: String?) {
                        if (type == 2) {
                            if (EventBus.getDefault().hasSubscriberForEvent(RemoteControlEvent::class.java)) {
                                // 有监听者才发这个
                                vibrator?.vibrate(500)
                                EventBus.getDefault().post(RemoteControlEvent(RemoteControlEvent.FROM_SHAKE))
                            }
                        }
                    }
                })
            }catch (e:Exception){
                MyLog.e(e)
            }

        }
    }

    fun unregister() {
        stopSensorService()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        MyLog.d(TAG, "onAccuracyChanged sensor = $sensor, accuracy = $accuracy")
    }

    override fun onSensorChanged(event1: SensorEvent?) {
        event1?.let { event ->
            val type = event.sensor.getType()
            if (type == Sensor.TYPE_ACCELEROMETER) {
                //MyLog.d(TAG, "onSensorChanged TYPE_STEP_DETECTOR values = ${printV(event.values)} accuracy = ${event.accuracy} timestamp = ${event.timestamp}")
                val now = System.currentTimeMillis()
                if (now - mLastForce > SHAKE_TIMEOUT) {
                    mShakeCount = 0
                }
                val values = event.values
                if (now - mLastTime > TIME_THRESHOLD) {
                    val diff = now - mLastTime
                    val speed = Math.abs(values[SensorManager.DATA_X] + values[SensorManager.DATA_Y] + values[SensorManager.DATA_Z] - mLastX - mLastY - mLastZ) / diff * 10000
                    MyLog.d(TAG, "onSensorChanged TYPE_STEP_DETECTOR diff = $diff speed = $speed mShakeCount=$mShakeCount mLastShake=$mLastShake")
                    if (speed > FORCE_THRESHOLD) {
                        if (++mShakeCount >= SHAKE_COUNT && now - mLastShake > SHAKE_DURATION) {
                            mLastShake = now
                            mShakeCount = 0
                            EventBus.getDefault().post(RemoteControlEvent(RemoteControlEvent.FROM_SHAKE))
                        }
                        mLastForce = now
                    }
                    mLastTime = now
                    mLastX = values[SensorManager.DATA_X]
                    mLastY = values[SensorManager.DATA_Y]
                    mLastZ = values[SensorManager.DATA_Z]
                }
            } else if (type == Sensor.TYPE_STEP_DETECTOR) {
                MyLog.d(TAG, "onSensorChanged TYPE_STEP_DETECTOR values = ${printV(event.values)} accuracy = ${event.accuracy} timestamp = ${event.timestamp}")
            } else if (type == Sensor.TYPE_STEP_COUNTER) {
                MyLog.d(TAG, "onSensorChanged TYPE_STEP_COUNTER values = ${printV(event.values)} accuracy = ${event.accuracy} timestamp = ${event.timestamp}")
            }
        }
    }

    private fun printV(arr: FloatArray): String {
        var r = "["
        arr.forEach {
            r += "$it,"
        }
        return r + "]"
    }
}