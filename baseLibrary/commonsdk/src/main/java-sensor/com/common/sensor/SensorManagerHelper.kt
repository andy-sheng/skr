package com.common.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.*
import com.common.guard.IpcCallback
import com.common.log.MyLog
import com.common.sensor.event.ShakeEvent
import com.common.utils.U
import org.greenrobot.eventbus.EventBus

object SensorManagerHelper : SensorEventListener {

    val TAG = "SensorManagerHelper"

    internal val userSet = HashSet<String>()
    // 传感器管理器
    private var sensorManager: SensorManager? = null

    // 手机震动
    private var vibrator: Vibrator? = null
    private var isShake = false
    private val mUiHandler = object :Handler(Looper.getMainLooper()){
        override fun handleMessage(msg: Message?) {
            super.handleMessage(msg)
        }
    }

    init {

    }

    internal fun startSensor(){
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
        vibrator = U.app().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?

        //val pm = U.app().getSystemService(Context.POWER_SERVICE) as PowerManager?
        //val wakeLock = pm?.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"com.zq.live:sensorlock")
        //wakeLock?.acquire()
    }

    internal fun stopSensor(){
        MyLog.d(TAG, "stopSensor")
        sensorManager?.unregisterListener(this)
    }

    fun register(tag: String) {
        MyLog.d(TAG, "register tag = $tag")
        if (userSet.isEmpty()) {
            bindSensorService {
                it?.call(1, null, object : IpcCallback.Stub() {
                    override fun callback(type: Int, json: String?) {
                        if (type == 2) {
                            EventBus.getDefault().post(ShakeEvent())
                        }
                    }
                })
            }
        }
        userSet.add(tag)
    }

    fun unregister(tag: String) {
        MyLog.d(TAG, "unregister tag = $tag")
        userSet.remove(tag)
        if (userSet.isEmpty()) {
            stopSensorService()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        MyLog.d(TAG, "onAccuracyChanged sensor = $sensor, accuracy = $accuracy")
    }

    override fun onSensorChanged(event1: SensorEvent?) {
        event1?.let { event ->
            val type = event.sensor.getType()
            if (type == Sensor.TYPE_ACCELEROMETER) {
                //MyLog.d(TAG, "onSensorChanged TYPE_ACCELEROMETER values = ${printV(event.values)} accuracy = ${event.accuracy} timestamp = ${event.timestamp}")
                //获取三个方向值
                val values = event.values
                val x = values[0]
                val y = values[1]
                val z = values[2]
                if ((Math.abs(x) > 17 || Math.abs(y) > 17 || Math.abs(z) > 17) && !isShake) {
                    isShake = true
                    vibrator?.vibrate(500)
                    EventBus.getDefault().post(ShakeEvent())
                    mUiHandler.postDelayed({
                        isShake = false
                    }, 500)
                }
            } else if (type == Sensor.TYPE_STEP_DETECTOR) {
                MyLog.d(TAG, "onSensorChanged TYPE_STEP_DETECTOR values = ${printV(event.values)} accuracy = ${event.accuracy} timestamp = ${event.timestamp}")
            } else if (type == Sensor.TYPE_STEP_COUNTER) {
                MyLog.d(TAG, "onSensorChanged TYPE_STEP_COUNTER values = ${printV(event.values)} accuracy = ${event.accuracy} timestamp = ${event.timestamp}")
            }
        }
    }

    private fun printV(arr:FloatArray):String{
        var r = "["
        arr.forEach {
            r+="$it,"
        }
        return r+"]"
    }
}