package com.common.sensor

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.RemoteException
import android.support.v4.app.NotificationCompat
import android.widget.RemoteViews

import com.common.base.R
import com.common.guard.IpcCallback
import com.common.guard.IpcService
import com.common.log.MyLog
import com.common.sensor.event.ShakeEvent
import com.common.utils.U
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * 守护传感器的service 进程，以前台通知的权限运行
 */
class SensorGuardService : Service() {
    val TAG = "SensorGuardService"

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        MyLog.d(TAG, "onStartCommand intent=$intent flags=$flags startId=$startId")
        val id = "com.zq.live"
        val name = "player"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(id, name, NotificationManager.IMPORTANCE_LOW)
            val manager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        val button1I = Intent("PressPauseOrPlayButton")
        val button1PI = PendingIntent.getBroadcast(this, 0, button1I, 0)
        val button2I = Intent("PressNextButton")
        val button2PI = PendingIntent.getBroadcast(this, 0, button2I, 0)
        /*
         * 通知布局如果使用自定义布局文件中的话要通过RemoteViews类来实现，
         * 其实无论是使用系统提供的布局还是自定义布局，都是通过RemoteViews类实现，如果使用系统提供的布局，
         * 系统会默认提供一个RemoteViews对象。如果使用自定义布局的话这个RemoteViews对象需要我们自己创建，
         * 并且加入我们需要的对应的控件事件处理，然后通过setContent(RemoteViews remoteViews)方法传参实现
         */
        val remoteViews = RemoteViews(packageName, R.layout.player_notification_view)
        /*
         * 对于自定义布局文件中的控件通过RemoteViews类的对象进行事件处理
         */
        remoteViews.setOnClickPendingIntent(R.id.last_view, button1PI)
        remoteViews.setOnClickPendingIntent(R.id.next_view, button2PI)

        val contentIntent = Intent()
        contentIntent.data = Uri.parse("inframeskr://home/trywakeup")
        val pendingContentIntent = PendingIntent.getActivity(this, 0,
                intent, 0)

        val notification = NotificationCompat.Builder(this, id)
                .setContentTitle("通知2") // 创建通知的标题
                .setContentText("这是第二个通知") // 创建通知的内容
                .setSmallIcon(R.drawable.app_icon) // 创建通知的小图标
                .setLargeIcon(BitmapFactory.decodeResource(resources,
                        R.drawable.app_icon)) // 创建通知的大图标
                /*
                 * 是使用自定义视图还是系统提供的视图，上面4的属性一定要设置，不然这个通知显示不出来
                 */
                .setDefaults(Notification.DEFAULT_ALL)  // 设置通知提醒方式为系统默认的提醒方式
                .setContent(remoteViews) // 通过设置RemoteViews对象来设置通知的布局，这里我们设置为自定义布局
                .setContentIntent(pendingContentIntent)
                .build() // 创建通知（每个通知必须要调用这个方法来创建）

        startForeground(110, notification)
        if(!EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().register(this)
        }
        return Service.START_STICKY
    }

    var callback:IpcCallback? = null

    override fun onBind(intent: Intent): IBinder? {
        MyLog.d(TAG, "onBind intent=$intent")
        return object : IpcService.Stub() {
            @Throws(RemoteException::class)
            override fun call(type: Int, json: String?, callback: IpcCallback?) {
                MyLog.d(TAG, "call type=$type json=$json callback=$callback")
                if (type == 1) {
                    SensorManagerHelper.startSensor()
                    this@SensorGuardService.callback = callback
                }
            }
        }
    }

    override fun onDestroy() {
        MyLog.d(TAG, "onDestroy")
        super.onDestroy()
        callback = null
        SensorManagerHelper.stopSensor()
        EventBus.getDefault().unregister(this)
    }

    override fun onUnbind(intent: Intent): Boolean {
        return super.onUnbind(intent)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(e: ShakeEvent) {
        callback?.callback(2,null)
    }
}

var sIpcService: IpcService? = null

fun bindSensorService(callback: ((IpcService?)->Unit)?) {
    MyLog.d("SensorGuardService", "bindService")
    val intent = Intent(U.app(), SensorGuardService::class.java)
    U.app().startService(intent)
    U.app().bindService(intent, object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            sIpcService = IpcService.Stub.asInterface(service)
            callback?.invoke(sIpcService)
        }

        override fun onServiceDisconnected(name: ComponentName) {
            // stopService 也会回调这个方法
            if(!SensorManagerHelper.userSet.isEmpty()){
                bindSensorService(callback)
            }
        }
    }, Context.BIND_IMPORTANT)
}

fun stopSensorService() {
    MyLog.d("SensorGuardService", "stopSensorService")
    val intent = Intent(U.app(), SensorGuardService::class.java)
    U.app().stopService(intent)
}