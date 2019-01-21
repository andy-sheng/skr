package com.common.utils;

import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.common.log.MyLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 播放音效用的
 * 可以同时播放多个又短又频繁的音效
 */
public class SoundUtils {

    public static final String PREF_KEY_GAME_VOLUME_SWITCH = "pref_game_volume_switch";

    static final int MSG_RELEASE_TAG = 1;
    HashMap<String, Holder> mSoundPoolMap = new HashMap<>();

    private String mPreventReleaseTag;

    private long mPreventReleaseTime;

    private String mPendingReleaseTag;

    private boolean isPlay;

    Handler mUiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == MSG_RELEASE_TAG) {
                release((String) msg.obj);
                mPendingReleaseTag = null;
            }
        }
    };

    SoundUtils() {
        isPlay = U.getPreferenceUtils().getSettingBoolean(PREF_KEY_GAME_VOLUME_SWITCH, true);
    }

    public boolean isPlay() {
        return isPlay;
    }

    public void setPlay(boolean play) {
        isPlay = play;
    }

    /**
     * 预加载某个页面所有的音效资源
     *
     * @param key    如 "HomeActivity"
     * @param rawIds 对应 res/raw/下的音效资源
     */
    public void preLoad(String key, int... rawIds) {
        release(key);
        SoundPool soundPool = new SoundPool(10, AudioManager.STREAM_SYSTEM, 0);
        List<Item> itemList = new ArrayList<>();
        for (int i = 0; i < rawIds.length; i++) {
            soundPool.load(U.app(), rawIds[i], 1);
            itemList.add(new Item(rawIds[i], i + 1));
        }
        Holder h = new Holder(soundPool, itemList);
        mSoundPoolMap.put(key, h);
        if (key.equals(mPreventReleaseTag)) {
            mUiHandler.removeMessages(MSG_RELEASE_TAG);
        }
    }

    /**
     * 不再使用这些音效是调用
     * 一般为退出某个界面时调用
     *
     * @param key
     */
    public void release(String key) {
        if (key.equals(mPreventReleaseTag) && System.currentTimeMillis() < mPreventReleaseTime) {
            // 有音效阻止release，所以延迟release
            long delay = mPreventReleaseTime - System.currentTimeMillis();
            mPendingReleaseTag = key;
            Message msg = mUiHandler.obtainMessage(MSG_RELEASE_TAG);
            msg.obj = mPendingReleaseTag;
            mUiHandler.sendMessageDelayed(msg, delay);
        } else {
            Holder holder = mSoundPoolMap.get(key);
            if (holder != null) {
                holder.soundPool.release();
                mSoundPoolMap.remove(key);
            }
        }
    }

    /**
     * @param key
     * @param rawId
     * @param inteceptorReleaseTs 是否要挂起release，比如 inteceptorReleaseTs = 500，在500ms内，这个key下资源不会被释放
     *                            一般用于返回键的音效播放，因为返回键一般伴随着destroy方法
     */
    public void play(String key, int rawId, int inteceptorReleaseTs) {
        if (!isPlay) {
            MyLog.d("SoundUtils", "starPlay" + " isPlay = false ");
            return;
        }
        Holder holder = mSoundPoolMap.get(key);
        if (holder != null) {
            for (Item item : holder.mItemList) {
                if (item.rawId == rawId) {
                    holder.soundPool.play(item.seq, 1, 1, 0, 0, 1);
                    if (inteceptorReleaseTs > 0) {
                        mPreventReleaseTime = System.currentTimeMillis() + inteceptorReleaseTs;
                        mPreventReleaseTag = key;
                    }
                    return;
                }
            }
            // 这里不能自动加载，会覆盖已加载的音效
            U.getToastUtil().showShort("没找到相关音效，请确认是否加载");
        } else {
            // 做个容错，忘记加载了，就自动加载下
            preLoad(key, rawId);
            play(key, rawId, -1);
        }
    }

    public void play(String key, int rawId) {
        play(key, rawId, -1);
    }

    static class Holder {
        SoundPool soundPool;
        List<Item> mItemList;

        public Holder(SoundPool soundPool, List<Item> itemList) {
            this.soundPool = soundPool;
            mItemList = itemList;
        }
    }

    static class Item {
        int rawId;
        int seq;

        public Item(int rawId, int seq) {
            this.rawId = rawId;
            this.seq = seq;
        }
    }
}
