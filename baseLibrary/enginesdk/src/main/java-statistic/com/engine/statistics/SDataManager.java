package com.engine.statistics;


import com.common.log.MyLog;


public class SDataManager {

    private String TAG = "SDATA_MANAGER";

    private String PREFIX_4_AGORA_RTC = "[" + TAG + "_FLUSHED]"; //"[SDATA_MANAGER FLUSHED]"

    private SDataMgrBasicInfo mBasicInfo = null;

    private SAgoraDataHolder mADHolder = null; //Agora Data Holder

    private SDataManager() {
        mBasicInfo = new SDataMgrBasicInfo();

        mADHolder = new SAgoraDataHolder();
        mADHolder.setLinePrefix(PREFIX_4_AGORA_RTC);
    }

    private static class SDataManagerHolder {
        private static final SDataManager INSTANCE = new SDataManager();
    }


    public static final SDataManager getInstance() {
        return SDataManagerHolder.INSTANCE;
    }


    public SDataManager setBasicInfo(SDataMgrBasicInfo info) {
        mBasicInfo.userID = info.userID;
//        mBasicInfo.userName = info.userName;
        return this;
    }

    public SDataManager setChannelID(String channelID) {
        mBasicInfo.channelID = channelID;
        return this;
    }

    public SDataManager setUserID(int userID) {
        mBasicInfo.userID = userID;
        return this;
    }

    public SDataManager setChannelJoinElipse(int elapsed) {
        mBasicInfo.channelJoinElapsed = elapsed;
        return this;
    }

    public SAgoraDataHolder getAgoraDataHolder() {
        return mADHolder;
    }


    public final static int FLUSH_MODE_FILE = 0x00000001;
    public final static int FLUSH_MODE_UPLOAD = 0x00000002; //not support now!

    public SDataManager flush(int flushMode) {

        StringBuilder logStr = new StringBuilder();

        logStr.append(PREFIX_4_AGORA_RTC).append("userID=").append(mBasicInfo.userID).append(", channelID=").append(mBasicInfo.channelID)
                .append(", channelJoinElapsed=").append(mBasicInfo.channelJoinElapsed).append("\n");
        logStr.append(mADHolder.toString());

        reset();

        MyLog.w(TAG, logStr.toString());
//        MyLog.flushLog();

        return this;
    }

    public SDataManager reset() {
        mADHolder.reset();

        return this;
    }

    public boolean need2Flush() {
        return (mADHolder.need2Flush());
    }

    //用户基本信息
    public static class SDataMgrBasicInfo {
        public long userID = -1;  //Skr的用户ID//退出的时候要复位
        public String channelID = "no-channel";//退出的时候要复位
        public int channelJoinElapsed = -1; //退出的时候要复位
    }

}