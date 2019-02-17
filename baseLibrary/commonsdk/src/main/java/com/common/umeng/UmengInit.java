package com.common.umeng;

import android.app.Notification;
import android.content.Context;
import android.util.Log;
import android.widget.RemoteViews;

import com.common.base.R;
import com.common.log.MyLog;
import com.common.statistics.UmengStatistics;
import com.common.utils.U;
import com.umeng.analytics.MobclickAgent;
import com.umeng.commonsdk.UMConfigure;
import com.umeng.message.IUmengRegisterCallback;
import com.umeng.message.PushAgent;
import com.umeng.message.UmengMessageHandler;
import com.umeng.message.UmengNotificationClickHandler;
import com.umeng.message.entity.UMessage;

import org.android.agoo.xiaomi.MiPushRegistar;

public class UmengInit {
    public final static String TAG = "UmengInit";
    private static boolean hasInited = false;

    public static void init() {
        if (hasInited) {
            return;
        }
        synchronized (UmengStatistics.class) {
            if (hasInited) {
                return;
            }
            Log.d(TAG, "UmengInit init 友盟初始化开始 "+U.getProcessName());
            UMConfigure.init(U.app(), "5bf40cc8f1f556f36200032b"
                    , U.getChannelUtils().getChannel(), UMConfigure.DEVICE_TYPE_PHONE, "34d3e8844e007050b8a968d974f1adee");
//            UMConfigure.setLogEnabled(true);
            MobclickAgent.setScenarioType(U.app(), MobclickAgent.EScenarioType.E_UM_NORMAL);
            pushInit();
            hasInited = true;
        }
    }

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
        mPushAgent.register(new IUmengRegisterCallback() {

            @Override
            public void onSuccess(String deviceToken) {
                //注册成功会返回deviceToken deviceToken是推送消息的唯一标志
                Log.w(TAG, "注册成功" + " deviceToken=" + deviceToken);
                MyLog.w(TAG, "注册成功" + " deviceToken=" + deviceToken);
            }

            @Override
            public void onFailure(String s, String s1) {
                MyLog.w(TAG, "注册失败" + " s=" + s + " s1=" + s1);
            }
        });

        // 小米push
//        MiPushRegistar.register(U.app(), "2882303761517932750", "5701793259750");

        UmengMessageHandler messageHandler = new UmengMessageHandler() {

            /**
             * 自定义通知栏样式的回调方法
             */
            @Override
            public Notification getNotification(Context context, UMessage msg) {
                MyLog.w(TAG,"getNotification" + " context=" + context + " msg=" + msg);
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
                MyLog.w(TAG, "dealWithCustomAction" + " context=" + context + " msg=" + msg);
            }

        };

        mPushAgent.setNotificationClickHandler(notificationClickHandler);
    }
}
