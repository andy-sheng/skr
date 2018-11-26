//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit.notification;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;
import android.text.TextUtils;

import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import io.rong.common.RLog;
import io.rong.common.SystemUtils;
import io.rong.imkit.RongContext;
import io.rong.imkit.RongIM;
import io.rong.imkit.RongNotificationManager;
import io.rong.imkit.model.ConversationInfo;
import io.rong.imkit.utils.NotificationUtil;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.RongIMClient.ErrorCode;
import io.rong.imlib.RongIMClient.ResultCallback;
import io.rong.imlib.model.Conversation.ConversationNotificationStatus;
import io.rong.imlib.model.Conversation.ConversationType;
import io.rong.imlib.model.MentionedInfo;
import io.rong.imlib.model.MentionedInfo.MentionedType;
import io.rong.imlib.model.Message;

public class MessageNotificationManager {
    private static final String TAG = "MessageNotificationManager";
    private static final int SOUND_INTERVAL = 3000;
    private long lastSoundTime = 0L;
    private String startTime;
    private int spanTime;
    MediaPlayer mediaPlayer;

    public MessageNotificationManager() {
    }

    public String getNotificationQuietHoursStartTime() {
        return this.startTime;
    }

    public int getNotificationQuietHoursSpanTime() {
        return this.spanTime;
    }

    public static io.rong.imkit.notification.MessageNotificationManager getInstance() {
        return io.rong.imkit.notification.MessageNotificationManager.SingletonHolder.instance;
    }

    public void setNotificationQuietHours(String startTime, int spanTime) {
        this.spanTime = spanTime;
        this.startTime = startTime;
    }

    public void clearNotificationQuietHours() {
        this.startTime = null;
        this.spanTime = 0;
    }

    public void notifyIfNeed(final Context context, final Message message, final int left) {
        if (message.getContent().getMentionedInfo() != null) {
            MentionedInfo mentionedInfo = message.getContent().getMentionedInfo();
            if (mentionedInfo.getType().equals(MentionedType.ALL) || mentionedInfo.getType().equals(MentionedType.PART) && mentionedInfo.getMentionedUserIdList() != null && mentionedInfo.getMentionedUserIdList().contains(RongIMClient.getInstance().getCurrentUserId())) {
                this.notify(context, message, left);
                return;
            }
        }

        boolean quiet = this.isInQuietTime();
        if (quiet) {
            RLog.d("MessageNotificationManager", "in quiet time, don't notify.");
        } else {
            if (left == 0) {
                RongIM.getInstance().getConversationNotificationStatus(message.getConversationType(), message.getTargetId(), new ResultCallback<ConversationNotificationStatus>() {
                    public void onSuccess(ConversationNotificationStatus conversationNotificationStatus) {
                        if (conversationNotificationStatus.equals(ConversationNotificationStatus.NOTIFY)) {
                            io.rong.imkit.notification.MessageNotificationManager.getInstance().notify(context, message, left);
                        }

                    }

                    public void onError(ErrorCode e) {
                    }
                });
            }

        }
    }

    private void notify(Context context, Message message, int left) {
        boolean isInBackground = SystemUtils.isInBackground(context);
        RLog.d("MessageNotificationManager", "isInBackground:" + isInBackground);
        if (message.getConversationType() != ConversationType.CHATROOM) {
            if (isInBackground) {
                RongNotificationManager.getInstance().onReceiveMessageFromApp(message);
            } else if (!this.isInConversationPager(message.getTargetId(), message.getConversationType()) && left == 0 && System.currentTimeMillis() - this.lastSoundTime > 3000L) {
                this.lastSoundTime = System.currentTimeMillis();
                int ringerMode = NotificationUtil.getRingerMode(context);
                if (ringerMode != 0) {
                    if (ringerMode != 1) {
                        this.sound();
                    }

                    this.vibrate();
                }
            }

        }
    }

    public boolean isInQuietTime() {
        int hour = -1;
        int minute = -1;
        int second = -1;
        if (!TextUtils.isEmpty(this.startTime) && this.startTime.contains(":")) {
            String[] time = this.startTime.split(":");

            try {
                if (time.length >= 3) {
                    hour = Integer.parseInt(time[0]);
                    minute = Integer.parseInt(time[1]);
                    second = Integer.parseInt(time[2]);
                }
            } catch (NumberFormatException var9) {
                RLog.e("MessageNotificationManager", "getConversationNotificationStatus NumberFormatException");
            }
        }

        if (hour != -1 && minute != -1 && second != -1) {
            Calendar startCalendar = Calendar.getInstance();
            startCalendar.set(Calendar.HOUR_OF_DAY, hour);
            startCalendar.set(Calendar.MINUTE, minute);
            startCalendar.set(Calendar.SECOND, second);
            long startTime = startCalendar.getTimeInMillis();
            Calendar endCalendar = Calendar.getInstance();
            endCalendar.setTimeInMillis(startTime + (long) (this.spanTime * 60 * 1000));
            Calendar currentCalendar = Calendar.getInstance();
            if (currentCalendar.get(5) != endCalendar.get(5)) {
                if (currentCalendar.before(startCalendar)) {
                    endCalendar.add(5, -1);
                    return currentCalendar.before(endCalendar);
                } else {
                    return true;
                }
            } else {
                return currentCalendar.after(startCalendar) && currentCalendar.before(endCalendar);
            }
        } else {
            return false;
        }
    }

    private boolean isInConversationPager(String id, ConversationType type) {
        List<ConversationInfo> list = RongContext.getInstance().getCurrentConversationList();
        Iterator var4 = list.iterator();
        if (!var4.hasNext()) {
            return false;
        } else {
            ConversationInfo conversationInfo = (ConversationInfo) var4.next();
            return id.equals(conversationInfo.getTargetId()) && type == conversationInfo.getConversationType();
        }
    }

    private void sound() {
        Uri res = RingtoneManager.getDefaultUri(2);
        if (RongContext.getInstance().getNotificationSound() != null && !TextUtils.isEmpty(RongContext.getInstance().getNotificationSound().toString())) {
            res = RongContext.getInstance().getNotificationSound();
        }

        try {
            if (this.mediaPlayer != null) {
                this.mediaPlayer.stop();
                this.mediaPlayer.reset();
                this.mediaPlayer.release();
                this.mediaPlayer = null;
            }

            this.mediaPlayer = new MediaPlayer();
            this.mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
                public void onCompletion(MediaPlayer mp) {
                    if (mp != null) {
                        try {
                            mp.stop();
                            mp.reset();
                            mp.release();
                        } catch (Exception var3) {
                            var3.printStackTrace();
                        }
                    }

                    if (io.rong.imkit.notification.MessageNotificationManager.this.mediaPlayer != null) {
                        io.rong.imkit.notification.MessageNotificationManager.this.mediaPlayer = null;
                    }

                }
            });
            this.mediaPlayer.setAudioStreamType(2);
            this.mediaPlayer.setDataSource(RongContext.getInstance(), res);
            this.mediaPlayer.prepare();
            this.mediaPlayer.start();
        } catch (Exception var3) {
            RLog.e("MessageNotificationManager", "sound", var3);
            if (this.mediaPlayer != null) {
                this.mediaPlayer = null;
            }
        }

    }

    private void vibrate() {
        Vibrator vibrator = (Vibrator) RongContext.getInstance().getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(new long[]{0L, 200L, 250L, 200L}, -1);
    }

    private static class SingletonHolder {
        static final io.rong.imkit.notification.MessageNotificationManager instance = new io.rong.imkit.notification.MessageNotificationManager();

        private SingletonHolder() {
        }
    }
}
