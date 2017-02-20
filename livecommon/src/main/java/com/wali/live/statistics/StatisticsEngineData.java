package com.wali.live.statistics;

/**
 * Created by qianyuan on 16/3/22.
 */
public class StatisticsEngineData {

    private final String TAG = StatisticsEngineData.class.getSimpleName();

    public static int FIRST_VIDEO_PACKET_DURATION = 100;
    public static int FIRST_VIDEO_DECODED_DURATION = 101;
    public static int FIRST_VIDEO_RENDER_DURATION = 102;

    public long mFirstVideoPacketDuration;
    public long mFirstVideoDecodeDuration;
    public long mFirstVideoRenderDuration;

    public StatisticsEngineData(long firstVideoPacketDuration, long firstVideoDecodedDuration, long firstVideoRenderDuration) {
        mFirstVideoPacketDuration = firstVideoPacketDuration;
        mFirstVideoDecodeDuration = firstVideoDecodedDuration;
        mFirstVideoRenderDuration = firstVideoRenderDuration;
    }

    public void setFirstVideoPacketDuration(long firstVideoPacketDuration) {
        mFirstVideoPacketDuration = firstVideoPacketDuration;
    }

    public long getmFirstVideoPacketDuration() {
        return mFirstVideoPacketDuration;
    }

    public void setFirstVideoDecodedDuration(long firstVideoDecodedDuration) {
        mFirstVideoDecodeDuration = firstVideoDecodedDuration;
    }

    public long getmFirstVideoDecodeDuration() {
        return mFirstVideoDecodeDuration;
    }

    public void setFirstVideoRenderDuration(long firstVideoRenderDuration) {
        mFirstVideoRenderDuration = firstVideoRenderDuration;
    }

    public long getmFirstVideoRenderDuration() {
        return mFirstVideoRenderDuration;
    }

}
