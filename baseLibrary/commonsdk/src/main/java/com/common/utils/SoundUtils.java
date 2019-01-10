package com.common.utils;

import android.media.AudioManager;
import android.media.SoundPool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 播放音效用的
 * 可以同时播放多个又短又频繁的音效
 */
public class SoundUtils {

    HashMap<String, Holder> mSoundPoolMap = new HashMap<>();

    SoundUtils() {

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
    }

    /**
     * 不再使用这些音效是调用
     * 一般为退出某个界面时调用
     *
     * @param key
     */
    public void release(String key) {
        Holder holder = mSoundPoolMap.get(key);
        if (holder != null) {
            holder.soundPool.release();
            mSoundPoolMap.remove(key);
        }
    }

    public void play(String key, int rawId) {
        Holder holder = mSoundPoolMap.get(key);
        if (holder != null) {
            for (Item item : holder.mItemList) {
                if (item.rawId == rawId) {
                    holder.soundPool.play(item.seq, 1, 1, 0, 0, 1);
                    return;
                }
            }
            // 这里不能自动加载，会覆盖已加载的音效
            U.getToastUtil().showShort("没找到相关音效，请确认是否加载");
        } else {
            // 做个容错，忘记加载了，就自动加载下
            preLoad(key, rawId);
            play(key, rawId);
        }
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
