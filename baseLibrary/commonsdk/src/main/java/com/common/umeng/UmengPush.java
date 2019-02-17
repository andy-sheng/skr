package com.common.umeng;

import android.app.Notification;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RemoteViews;

import com.common.base.R;
import com.common.log.MyLog;
import com.common.utils.HandlerTaskTimer;
import com.common.utils.U;
import com.umeng.message.IUmengCallback;
import com.umeng.message.IUmengRegisterCallback;
import com.umeng.message.PushAgent;
import com.umeng.message.UTrack;
import com.umeng.message.UmengMessageHandler;
import com.umeng.message.UmengNotificationClickHandler;
import com.umeng.message.entity.UMessage;

import org.android.agoo.huawei.HuaWeiRegister;
import org.android.agoo.xiaomi.MiPushRegistar;
import org.greenrobot.eventbus.EventBus;

import java.util.Map;

public class UmengPush {
    public final static String TAG = "UmengPush";

    static String sDeviceToken = "";

    /**
     * 初始化友盟push
     * 以及集成各厂商的push
     */
    static void pushInit() {
        //获取消息推送代理示例
        PushAgent mPushAgent = PushAgent.getInstance(U.app());

        //自定义icon资源的包名，否则使用应用包名
//        mPushAgent.setResourcePackageName("com.common.base");

        //设置前台不显示通知
//        mPushAgent.setNotificaitonOnForeground(false);

        //注册推送服务，每次调用register方法都会回调该接口
        Log.d(TAG, "mPushAgent.register begin");
        registerUmengPush(0);

        // 小米push
        MiPushRegistar.register(U.app(), "2882303761517932750", "5701793259750");

        // 华为push
        HuaWeiRegister.register(U.app());

        UmengMessageHandler messageHandler = new UmengMessageHandler() {

            /**
             * 自定义通知栏样式的回调方法
             */
            @Override
            public Notification getNotification(Context context, UMessage msg) {
                MyLog.w(TAG, "getNotification " + getPrintPushMsg(msg));
                switch (msg.builder_id) {
                    case 1:
                        Notification.Builder builder = new Notification.Builder(context);
                        RemoteViews myNotificationView = new RemoteViews(context.getPackageName(),
                                R.layout.notification_view);
                        myNotificationView.setTextViewText(R.id.notification_title, msg.title);
                        myNotificationView.setTextViewText(R.id.notification_text, msg.text);
                        myNotificationView.setImageViewBitmap(R.id.notification_large_icon, getLargeIcon(context, msg));
                        myNotificationView.setImageViewResource(R.id.notification_small_icon,
                                getSmallIconId(context, msg));
                        builder.setContent(myNotificationView)
                                .setSmallIcon(getSmallIconId(context, msg))
                                .setTicker(msg.ticker)
                                .setAutoCancel(true);

                        return builder.getNotification();
                    default:
                        //默认为0，若填写的builder_id并不存在，也使用默认。
                        return super.getNotification(context, msg);
                }
            }
        };
        mPushAgent.setMessageHandler(messageHandler);

        UmengNotificationClickHandler notificationClickHandler = new UmengNotificationClickHandler() {
            @Override
            public void dealWithCustomAction(Context context, UMessage msg) {
                MyLog.w(TAG, "dealWithCustomAction " + getPrintPushMsg(msg));
                /**
                 * UmengNotificationClickHandler是在BroadcastReceiver中被调用，因此若需启动Activity，
                 * 需为Intent添加Flag：Intent.FLAG_ACTIVITY_NEW_TASK，否则无法启动Activity。
                 *
                 * 自定义消息不是通知，默认不会被SDK展示到通知栏上，【友盟+】推送仅负责将消息透传给SDK，
                 * 其内容和展示形式是则完全可以由开发者自己定义。
                 */
            }
        };

        mPushAgent.setNotificationClickHandler(notificationClickHandler);
    }

    static void registerUmengPush(int tryTime) {
        if (tryTime > 2) {
            return;
        }
        PushAgent mPushAgent = PushAgent.getInstance(U.app());
        mPushAgent.register(new IUmengRegisterCallback() {

            @Override
            public void onSuccess(String deviceToken) {
                //注册成功会返回deviceToken deviceToken是推送消息的唯一标志
                MyLog.w(TAG, "注册成功" + " deviceToken=" + deviceToken);
                sDeviceToken = deviceToken;
                EventBus.getDefault().post(new UmengPushRegisterSuccessEvent());
            }

            @Override
            public void onFailure(String s, String s1) {
                MyLog.w(TAG, "注册失败" + " s=" + s + " s1=" + s1);
                sDeviceToken = "";
                HandlerTaskTimer.newBuilder()
                        .delay(2000)
                        .start(new HandlerTaskTimer.ObserverW() {
                            @Override
                            public void onNext(Integer integer) {
                                MyLog.d(TAG, "重新尝试注册UmengPush");
                                registerUmengPush(tryTime + 1);
                            }
                        });

            }
        });
    }

    /**
     * 打印通知消息
     *
     * @param msg
     * @return
     */
    static String getPrintPushMsg(UMessage msg) {
        StringBuilder sb = new StringBuilder();
        sb.append(" title=").append(msg.title)
                .append(" message_id=").append(msg.message_id)
                .append(" url=").append(msg.url)
                .append(" activity=").append(msg.activity)
                .append(" after_open=").append(msg.after_open)
                .append(" alias=").append(msg.alias)
                .append(" bar_image=").append(msg.bar_image)
                .append(" custom=").append(msg.custom)
                .append(" ticker=").append(msg.ticker)
                .append(" icon=").append(msg.icon).append("\n");
        for (Map.Entry entry : msg.extra.entrySet()) {
            Object key = entry.getKey();
            Object value = entry.getValue();
            sb.append(key).append(":").append(value).append("\n");
        }
        return sb.toString();
    }

    /**
     * 获取注册push的id
     *
     * @return
     */
    public static String getDeviceToken() {
        return sDeviceToken;
    }

    /**
     * 设置别名
     *
     * @param uuidAsLong
     */
    public static void setAlias(String uuidAsLong) {
        if (TextUtils.isEmpty(sDeviceToken)) {
            PushAgent.getInstance(U.app()).setAlias(uuidAsLong, "skr_id", new UTrack.ICallBack() {
                @Override
                public void onMessage(boolean b, String s) {
                    MyLog.d(TAG, "setAlias onMessage" + " b=" + b + " s=" + s);
                }
            });
        }
    }

    /**
     * 清除别名
     *
     * @param uuidAsLong
     */
    public static void clearAlias(String uuidAsLong) {
        PushAgent.getInstance(U.app()).deleteAlias(uuidAsLong, "skr_id", new UTrack.ICallBack() {
            @Override
            public void onMessage(boolean b, String s) {
                MyLog.d(TAG, "clearAlias onMessage" + " b=" + b + " s=" + s);
            }
        });
    }

    /**
     * 关闭push
     */
    public static void disablePush() {
        MyLog.d(TAG, "disablePush");
        PushAgent.getInstance(U.app()).disable(new IUmengCallback() {
            @Override
            public void onSuccess() {
                MyLog.d(TAG, "disablePush onSuccess");
            }

            @Override
            public void onFailure(String s, String s1) {
                MyLog.d(TAG, "onFailure" + " s=" + s + " s1=" + s1);
            }
        });
    }
}
