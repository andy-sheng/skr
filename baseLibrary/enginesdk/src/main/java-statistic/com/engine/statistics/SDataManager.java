package com.engine.statistics;


import com.common.log.MyLog;
import com.engine.statistics.logservice.SLogServiceBase;


public class SDataManager
{
    /**
     * singleton of SDataManager
     */
    private static SDataManager gDMInstance = null;

    private SDataMgrBasicInfo mBasicInfo = null;

    private SAgoraDataHolder mADHolder = null; //Agora Data Holder


    private String TAG = "SDATA_MANAGER";
    private String LOG_PREFIX = "["+TAG+"_FLUSHED]"; //"[SDATA_MANAGER FLUSHED]"

    private SDataManager(){
        mBasicInfo = new SDataMgrBasicInfo();

        mADHolder = new SAgoraDataHolder();
        mADHolder.setLinePrefix(LOG_PREFIX);
    }

    public static synchronized SDataManager instance() {
        if (null == gDMInstance) {
            gDMInstance = new SDataManager();
        }
        return gDMInstance;
    }


    //用户基本信息
    public static class SDataMgrBasicInfo {
        public long userID  = -1;  //Skr的用户ID//退出的时候要复位
        public String channelID ="no-channel";//退出的时候要复位
        public int channelJoinElapsed= -1; //退出的时候要复位
    }

    public void setLogServices(SLogServiceBase ls) {
        mADHolder.setLogServices(ls);
    }

    public SDataManager setBasicInfo(SDataMgrBasicInfo info){
        mBasicInfo.userID   = info.userID;
//        mBasicInfo.userName = info.userName;
        return this;
    }

    public synchronized SDataManager setChannelID(String channelID) {
        mBasicInfo.channelID = channelID;
        return this;
    }
    public synchronized SDataManager setUserID(int userID) {
        mBasicInfo.userID = userID;
        return this;
    }
    public synchronized SDataManager setChannelJoinElipse(int elapsed) {
        mBasicInfo.channelJoinElapsed = elapsed;
        return this;
    }
    public synchronized SAgoraDataHolder getAgoraDataHolder() {
        return mADHolder;
    }


    public final static int FLUSH_MODE_FILE = 0x00000001;
    public final static int FLUSH_MODE_UPLOAD = 0x00000002; //not support now!
    public synchronized SDataManager flush(int flushMode) {

        String logStr = "";

        logStr += (LOG_PREFIX+ "userID="+mBasicInfo.userID + ", channelID=" + mBasicInfo.channelID +
                    ", channelJoinElapsed="+mBasicInfo.channelJoinElapsed+"\n");
        logStr += mADHolder.toString();


        mADHolder.flushLS();
        mADHolder.reset();

        MyLog.w(TAG, logStr);
        MyLog.flushLog();

        return this;
    }

    public SDataManager reset() {
        mADHolder.reset();

        return this;
    }

    public boolean need2Flush(){
        return (mADHolder.need2Flush());
    }


}