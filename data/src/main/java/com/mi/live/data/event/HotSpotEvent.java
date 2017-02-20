package com.mi.live.data.event;

/**
 * Created by zjn on 16-12-1.
 */
public class HotSpotEvent {

    public static final int SPOT_TYPE_LINK = 1;
    public static final int SPOT_TYPE_MUSIC = 2;
    public static final int SPOT_TYPE_SHARE_VIDEO = 3;
    public static final int SPOT_TYPE_SHARE_PIC = 4;
    public static final int SPOT_TYPE_LOTTERY = 5;
    public static final int SPOT_TYPE_LINK_DEVICE = 6;
    public static final int SPOT_TYPE_GIFT = 7;
    public static final int SPOT_TYPE_LIVE_RECORD = 8;
    public static final int SPOT_TYPE_REPLAY_RECORD = 9;

    private int hotType;

    private String song;

    private int giftId;

    public int getHotType() {
        return hotType;
    }

    public String getSong() {
        return song;
    }

    public int getGiftId() {
        return giftId;
    }

    public HotSpotEvent(int hotType, String song, int giftId) {
        this.hotType = hotType;
        this.song = song;
        this.giftId = giftId;
    }
}
