package com.wali.live.statistics;

import android.text.TextUtils;

/**
 * this class用于频道上某一位置的统计打点
 * Created by yaojian on 16-4-7.
 *
 * @module 频道打点
 */
public class ChannelStatisticsHelper {
    //打点的key, 格式为source-from-cid(channelID)-roomid-position-type
    private final static String ACTION_KEY_FORMAT = "source-%d-%s-%s-%d-%d";

    //以下为from的可选值
    public final static int FROM_DEFAULT = 0;           //from的默认值
    public final static int FROM_HOMEPAGR = 1;         //首页
    public final static int FROM_DYNAMIC = 2;          //动态
    public final static int FROM_NAMECARD = 3;         //名片
    public final static int FROM_PUSH = 4;             //push
    public final static int FROM_BANNER = 5;           //banner
    public final static int FROM_GLOBAL_MSG = 6;           //神龙

    //以下为cid的可选值
    public final static long CID_DEFAULT = 0;            //默认的cid值

    //以下为roomid的可选值
    public final static String ROOMID_DEFAULT = "0";     //roomid的默认值

    //以下为position的可选值
    public final static int POSITION_DEFAULT = 0;       //默认的position值

    //以下为type的可选值
    public final static int TYPE_DEFAULT = 0;           //默认的type值
    public final static int TYPE_LIVE = 1;              //直播
    public final static int TYPE_REPLAY = 2;            //回放


    /**
     * 打点数据的抽象
     */
    public static class ChannelStatisticsEntry {

        private int mFromField = FROM_DEFAULT;
        private long mCIDField = CID_DEFAULT;
        private String mRoomIdField = ROOMID_DEFAULT;
        private int mPositionField = POSITION_DEFAULT;
        private int mTypeField = TYPE_DEFAULT;

        private ChannelStatisticsEntry() {

        }

        public static class Builder {
            private ChannelStatisticsEntry item = new ChannelStatisticsEntry();

            public Builder() {
            }

            public Builder setFrom(int from) {
                if (item == null) {
                    item = new ChannelStatisticsEntry();
                }
                item.mFromField = from;
                return this;
            }

            public Builder setCid(long cid) {
                if (item == null) {
                    item = new ChannelStatisticsEntry();
                }
                item.mCIDField = cid;
                return this;
            }

            public Builder setRoomId(String roomId) {
                if (item == null) {
                    item = new ChannelStatisticsEntry();
                }
                if (!TextUtils.isEmpty(roomId)) {
                    item.mRoomIdField = roomId;
                } else {
                    item.mRoomIdField = ROOMID_DEFAULT;
                }
                return this;
            }

            public Builder setPosition(int position) {
                if (item == null) {
                    item = new ChannelStatisticsEntry();
                }
                item.mPositionField = position;
                return this;
            }

            public Builder setType(int type) {
                if (item == null) {
                    item = new ChannelStatisticsEntry();
                }
                item.mTypeField = type;
                return this;
            }

            public ChannelStatisticsEntry build() {
                if (item == null) {
                    item = new ChannelStatisticsEntry();
                }
                return item;
            }
        }
    }

    /**
     * 进行一次打点
     */
    public static void pushOneItem(ChannelStatisticsEntry entry) {
        if (entry == null) {
            return;
        }
        String key = formatString(entry);
        if (TextUtils.isEmpty(key)) {
            return;
        }
        StatisticsWorker.getsInstance().sendCommandRealTime(StatisticsWorker.AC_APP, key, 1);
    }

    /**
     * 打点拼字符串
     */
    public static String formatString(ChannelStatisticsEntry entry) {
        if (entry == null) {
            return null;
        }
        return String.format(ACTION_KEY_FORMAT, entry.mFromField, entry.mCIDField, entry.mRoomIdField, entry.mPositionField, entry.mTypeField);
    }
}
