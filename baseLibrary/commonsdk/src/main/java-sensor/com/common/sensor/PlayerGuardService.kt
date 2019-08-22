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
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject

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
class PlayerGuardService : Service() {
    val TAG = "SensorGuardService"
    val NOTIFICATION_ID = 110

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        MyLog.d(TAG, "onStartCommand intent=$intent flags=$flags startId=$startId")
        showPlayerNotification(null)
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        return Service.START_STICKY
    }

    private fun showPlayerNotification(jo: JSONObject?) {
//        val notification = createNotification1(createPlayerView(jo))
        val notification = createNotification2()
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun createPlayerView(jo: JSONObject?): RemoteViews {
        val songName = jo?.getString("songName")
        val playing = jo?.getBooleanValue("playing")

        val intent1 = Intent(this, NotifitionPlayerActionReceiver::class.java)
        intent1.action = PRE_SONG_ACTION
        val button1PI = PendingIntent.getBroadcast(this, 0, intent1, 0)
        val intent2 = Intent(this, NotifitionPlayerActionReceiver::class.java)
        intent2.action = PRE_SONG_ACTION
        val button2PI = PendingIntent.getBroadcast(this, 0, intent2, 0)

        val intent3 = Intent(this, NotifitionPlayerActionReceiver::class.java)
        if (playing == true) {
            intent3.action = STOP_PLAY_SONG_ACTION
        } else {
            intent3.action = START_PLAY_SONG_ACTION
        }
        val button3PI = PendingIntent.getBroadcast(this, 0, intent3, 0)

        /**
         * 注意 布局不支持 ConstraintLayout
         */
        val remoteViews = RemoteViews(packageName, R.layout.player_notification_view)
        /*
         * 对于自定义布局文件中的控件通过RemoteViews类的对象进行事件处理
         */
        remoteViews.setOnClickPendingIntent(R.id.last_view, button1PI)
        remoteViews.setOnClickPendingIntent(R.id.next_view, button2PI)
        remoteViews.setOnClickPendingIntent(R.id.play_view, button3PI)
        if (playing == true) {
            remoteViews.setTextViewText(R.id.play_view, "$songName 正在播放")
        } else {
            remoteViews.setTextViewText(R.id.play_view, "$songName 已经暂停")
        }

        return remoteViews
    }

    private fun createNotification1(remoteView: RemoteViews): Notification {
        val id = "com.zq.live"
        val name = "player"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(id, name, NotificationManager.IMPORTANCE_LOW)
            val manager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
        val contentIntent = Intent()
        contentIntent.data = Uri.parse("inframeskr://home/trywakeup")
        val pendingContentIntent = PendingIntent.getActivity(this, 0,
                contentIntent, 0)

        val notification = NotificationCompat.Builder(this, id)
                .setContentTitle("撕歌") // 创建通知的标题
                .setContentText("撕歌正在播放音乐，摇一摇切歌") // 创建通知的内容
                .setSmallIcon(R.drawable.app_icon) // 创建通知的小图标
                .setLargeIcon(BitmapFactory.decodeResource(resources,
                        R.drawable.app_icon)) // 创建通知的大图标
                .setDefaults(Notification.DEFAULT_ALL)  // 设置通知提醒方式为系统默认的提醒方式
                .setContentIntent(pendingContentIntent)
                .setContent(remoteView)
                .build() // 创建通知（每个通知必须要调用这个方法来创建）
        return notification
    }

    private fun createNotification2(): Notification {
        val id = "com.zq.live"
        val name = "player"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(id, name, NotificationManager.IMPORTANCE_LOW)
            val manager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        val contentIntent = Intent(this, NotifitionPlayerActionReceiver::class.java)
        contentIntent.action = TRY_WAKEUP_HOME_ACTION
//        val contentIntent = Intent()
//        contentIntent.data = Uri.parse("inframeskr://home/trywakeup")
        val pendingContentIntent = PendingIntent.getBroadcast(this, 0,
                contentIntent, 0)

        val notification = NotificationCompat.Builder(this, id)
                .setContentTitle("撕歌") // 创建通知的标题
                .setContentText("撕歌正在播放音乐,摇一摇切歌") // 创建通知的内容
                .setSmallIcon(R.drawable.app_icon) // 创建通知的小图标
                .setLargeIcon(BitmapFactory.decodeResource(resources,
                        R.drawable.app_icon)) // 创建通知的大图标
                .setDefaults(Notification.DEFAULT_ALL)  // 设置通知提醒方式为系统默认的提醒方式
                .setContentIntent(pendingContentIntent)
                .build() // 创建通知（每个通知必须要调用这个方法来创建）

        return notification
    }

    var callback: IpcCallback? = null

    override fun onBind(intent: Intent): IBinder? {
        MyLog.d(TAG, "onBind intent=$intent")
        return object : IpcService.Stub() {
            @Throws(RemoteException::class)
            override fun call(type: Int, json: String?, callback: IpcCallback?) {
                MyLog.d(TAG, "call type=$type json=$json callback=$callback")
                if (type == 1) {
                    //启动传感器
                    SensorManagerHelper.startSensor()
                    showPlayerNotification(null)
                    this@PlayerGuardService.callback = callback
                } else if (type == 2) {
                    SensorManagerHelper.stopSensor()
                    this@PlayerGuardService.stopForeground(true)
                } else if (type == 3) {
                    val jo = JSON.parseObject(json)
                    showPlayerNotification(jo)
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
        callback?.callback(2, null)
    }
}

var sIpcService: IpcService? = null

fun bindSensorService(callback: ((IpcService?) -> Unit)?) {
    MyLog.d("SensorGuardService", "bindService")
    if (sIpcService != null) {
        var alive = true
        try {
            sIpcService?.call(-1, null, null)
        } catch (e: RemoteException) {
            MyLog.w("SensorGuardService", "sIpcService 已经死了")
            alive = false
        }
        if (alive) {
            callback?.invoke(sIpcService)
            return
        }
    }
    val intent = Intent(U.app(), PlayerGuardService::class.java)
    U.app().startService(intent)
    U.app().bindService(intent, object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            sIpcService = IpcService.Stub.asInterface(service)
            callback?.invoke(sIpcService)
        }

        override fun onServiceDisconnected(name: ComponentName) {
            // stopService 也会回调这个方法
            if (!SensorManagerHelper.userSet.isEmpty()) {
                bindSensorService(callback)
            } else {
                sIpcService = null
            }
        }
    }, Context.BIND_IMPORTANT)
}

fun stopSensorService() {
    MyLog.d("SensorGuardService", "stopSensorService")
    if (sIpcService !== null) {
        sIpcService?.call(2, null, null)
    } else {
        val intent = Intent(U.app(), PlayerGuardService::class.java)
        U.app().stopService(intent)
        sIpcService = null
    }
}

fun tryUpdatePlayerNofication(songName: String?, playing: Boolean) {
    val js = JSONObject()
    js["songName"] = songName
    js["playing"] = playing
    sIpcService?.call(3, js.toString(), null)
}