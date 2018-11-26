//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit.utils;

import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Build.VERSION;

import java.lang.reflect.Method;

public class NotificationUtil {
    public NotificationUtil() {
    }

    public static void showNotification(Context context, String title, String content, PendingIntent intent, int notificationId, int defaults) {
        Notification notification = createNotification(context, title, content, intent, defaults);
        NotificationManager nm = (NotificationManager) context.getSystemService("notification");
        if (notification != null) {
            nm.notify(notificationId, notification);
        }

    }

    public static void showNotification(Context context, String title, String content, PendingIntent intent, int notificationId) {
        showNotification(context, title, content, intent, notificationId, -1);
    }

    public static void clearNotification(Context context, int notificationId) {
        NotificationManager nm = (NotificationManager) context.getSystemService("notification");
        nm.cancel(notificationId);
    }

    private static Notification createNotification(Context context, String title, String content, PendingIntent pendingIntent, int defaults) {
        String tickerText = context.getResources().getString(context.getResources().getIdentifier("rc_notification_ticker_text", "string", context.getPackageName()));
        Notification notification;
        if (VERSION.SDK_INT < 11) {
            try {
                notification = new Notification(context.getApplicationInfo().icon, tickerText, System.currentTimeMillis());
                Class<?> classType = Notification.class;
                Method method = classType.getMethod("setLatestEventInfo", Context.class, CharSequence.class, CharSequence.class, PendingIntent.class);
                method.invoke(notification, context, title, content, pendingIntent);
                notification.flags = 48;
                notification.defaults = -1;
            } catch (Exception var12) {
                var12.printStackTrace();
                return null;
            }
        } else {
            boolean isLollipop = VERSION.SDK_INT >= 21;
            int smallIcon = context.getResources().getIdentifier("notification_small_icon", "drawable", context.getPackageName());
            if (smallIcon <= 0 || !isLollipop) {
                smallIcon = context.getApplicationInfo().icon;
            }

            Drawable loadIcon = context.getApplicationInfo().loadIcon(context.getPackageManager());
            Bitmap appIcon = null;

            try {
                if (VERSION.SDK_INT >= 26 && loadIcon instanceof AdaptiveIconDrawable) {
                    appIcon = Bitmap.createBitmap(loadIcon.getIntrinsicWidth(), loadIcon.getIntrinsicHeight(), Config.ARGB_8888);
                    Canvas canvas = new Canvas(appIcon);
                    loadIcon.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                    loadIcon.draw(canvas);
                } else {
                    appIcon = ((BitmapDrawable) loadIcon).getBitmap();
                }
            } catch (Exception var13) {
                var13.printStackTrace();
            }

            Builder builder = new Builder(context);
            builder.setLargeIcon(appIcon);
            builder.setSmallIcon(smallIcon);
            builder.setTicker(tickerText);
            builder.setContentTitle(title);
            builder.setContentText(content);
            builder.setContentIntent(pendingIntent);
            builder.setAutoCancel(true);
            builder.setOngoing(true);
            builder.setDefaults(defaults);
            notification = builder.getNotification();
        }

        return notification;
    }

    public static int getRingerMode(Context context) {
        AudioManager audio = (AudioManager) context.getSystemService("audio");
        return audio.getRingerMode();
    }
}
