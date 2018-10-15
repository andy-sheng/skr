package media;

/**
 * Created by linjinbin on 15/2/10.
 */

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Vibrator;
import android.text.TextUtils;

import com.base.log.MyLog;

/**
 * voip媒体播放相关工具类
 *
 * @author 陈雍 E-mail:chenyong1@xiaomi.com
 * @version 创建时间：2014-10-21 下午4:57:08
 */
public class ContestMediaUtils {

    private static final String TAG = ContestMediaUtils.class.getSimpleName() + "-cauhn";

    private MediaPlayer mMediaPlayer;
    private Vibrator mVibrator; // 震动控制
    private int mPlayTimes = 0; //播放次数
    private boolean hasInit = false;

    /**
     * 初始化媒体工具类  在app的onCreate中执行
     */
    public void init(final Context context) {
        if (!hasInit) {
            mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            mMediaPlayer = new MediaPlayer();
            hasInit = true;
        }
    }

    /**
     * 播放指定音频文件
     *
     * @param sourceUrl
     * @param times
     * @param listener
     */
    public void playAudioMedia(String sourceUrl, int times, final OnCompletionListener listener) {
        if (TextUtils.isEmpty(sourceUrl)) {
            return;
        }
        synchronized (this) {
            MyLog.i(TAG, "playTone: delayTime=" + times + ", sourceUrl=" + sourceUrl + ", listener=" + listener);

            if (checkMediaPlayerIsEmpty()) {
                MyLog.w(TAG, "playTone times, but mMediaPlayer is empty!");
                if (listener != null) {
                    listener.onCompletion();
                }
                return;
            }

            try {
                mMediaPlayer.reset();
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
                mPlayTimes = times;
                mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        mPlayTimes--;
                        if (mPlayTimes < 1) {
                            MyLog.i(TAG, "playTone times, mMediaPlayer end onCompletion");
                            if (listener != null) {
                                listener.onCompletion();
                            }
                        } else {
                            if (mMediaPlayer != null) {
                                mMediaPlayer.start();
                            } else {
                                stopToneAndVibrate();
                            }
                        }
                    }
                });
                mMediaPlayer.setDataSource(sourceUrl);
                mMediaPlayer.prepare();
                mMediaPlayer.start();
            } catch (Exception e) {
                MyLog.e(TAG, "playTone times, Exception: " + e);
                MyLog.i(TAG, "playTone times, mMediaPlayer failed onCompletion");
                if (listener != null) {
                    listener.onCompletion();
                }
            }
        }
    }

    private boolean checkMediaPlayerIsEmpty() {
        return mMediaPlayer == null;
    }

    /**
     * 停止声音与振动
     */
    public synchronized void stopToneAndVibrate() {
        MyLog.i(TAG, "stopToneAndVibrate");
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            try {
                MyLog.i(TAG, "stopToneAndVibrate mMediaPlayer is playing, try to stop it");
                mMediaPlayer.pause();
                mMediaPlayer.stop();
            } catch (Exception e) {
                MyLog.e(TAG, "stopToneAndVibrate Exception: " + e);
                e.printStackTrace();
            }
        }

        if (mVibrator != null) {
            mVibrator.cancel();
        }
    }

    public void closeMedia() {
        if (mMediaPlayer != null) {
            stopToneAndVibrate();
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    /**
     * 释放媒体工具类  在app的onTeminate中执行
     */
    public void release() {
        closeMedia();
        hasInit = false;
    }

    public interface OnCompletionListener {
        public void onCompletion();
    }
}
