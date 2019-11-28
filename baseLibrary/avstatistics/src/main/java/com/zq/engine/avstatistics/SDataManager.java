package com.zq.engine.avstatistics;


import android.content.Context;

import com.common.log.MyLog;
import com.zq.engine.avstatistics.datastruct.ILogItem;
import com.zq.engine.avstatistics.logservice.SLogServiceAgent;
import com.zq.engine.avstatistics.logservice.SLogServiceBase;
import com.zq.engine.avstatistics.sts.SSTSCredentialHolder;

import java.util.List;


public class SDataManager {
    public static boolean dbgMode = false;


    private String TAG = "[SLS]SDATA_MANAGER";

    private String LOG_PREFIX = "[SDM]"; //"[SDATA_MANAGER FLUSHED]"

    private SDataMgrBasicInfo mBasicInfo = null;

    private SDataHolderEx mADHolder = null; //Agora Data Holder
    private SLogServiceBase mLS = null;

    private SDataManager() {
        mBasicInfo = new SDataMgrBasicInfo();

        mADHolder = new SDataHolderEx();
        mADHolder.setLinePrefix(LOG_PREFIX);

        mLS = SLogServiceAgent.getService(SLogServiceAgent.LS_PROVIDER_ALIYUN);
    }

    private final static SDataManager INSTANCE = new SDataManager();

    public static final SDataManager getInstance() {
        return INSTANCE;
    }


    public void setDebugMode(boolean isDbg){
        dbgMode = isDbg;
    }

    public SDataManager setUserID(long userID) {
        try {
            mLS.setProp(SLogServiceBase.PROP_USER_ID, Long.valueOf(userID));
        } catch (Exception e) {
            MyLog.e(TAG, e);
        }
        mBasicInfo.userID = userID;
        return this;
    }

    public SDataManager setAppContext(Context ctx) {
        Boolean hasInit = (Boolean)mLS.getProp(SLogServiceBase.PROP_IS_INITIALIZED);
        if (!hasInit) {
            try {
                mLS.init(ctx);
            } catch (Exception e) {
                MyLog.e(TAG, e);
            }
        }

        return this;
    }

    public SDataManager setSTSCredentialHolder(SSTSCredentialHolder scHolder) {
        try {
            mLS.setProp(SLogServiceBase.PROP_STS_CREDENTIAL_HOLDER, scHolder);
        } catch (Exception e) {
            MyLog.e(TAG, e);
        }
        return this;
    }

    /**
     * @param enable true or false
     */
    public SDataManager enableLogService(boolean enable) {
        try {
            mLS.setProp(SLogServiceBase.PROP_ENABLE_SERVICES, new Boolean(enable));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }

    /**
     *  main log-project of log-service  is for release app version, otherwise, for test/dev/sandbox....use test log-store
     */
    public SDataManager useMainLogProject(boolean useMain) {
        try {
            mLS.setProp(SLogServiceBase.PROP_USE_MAIN_LOG_PROJECT, new Boolean(useMain));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }


//    public SDataManager setBasicInfo(SDataMgrBasicInfo info) {
//        mBasicInfo.userID = info.userID;
////        mBasicInfo.userName = info.userName;
//
//        return this;
//    }

    public SDataManager setChannelID(String channelID) {
        mBasicInfo.channelID = channelID;
        return this;
    }


    public SDataManager setChannelJoinElipse(int elapsed) {
        mBasicInfo.channelJoinElapsed = elapsed;
        return this;
    }

    public SDataHolderEx getDataHolder() {
        return mADHolder;
    }


    public final static int FLUSH_MODE_FILE = 0x00000001;
    public final static int FLUSH_MODE_UPLOAD = 0x00000002;
    public final static int FLUSH_MODE_ALL = (FLUSH_MODE_FILE | FLUSH_MODE_UPLOAD);

    public SDataManager flush(int flushMode) {//flush mode暂时留了个口子，目前是文件log和上传都处理

        StringBuilder logStr = new StringBuilder();

        logStr.append(LOG_PREFIX).append(" userID=").append(mBasicInfo.userID).append(", channelID=").append(mBasicInfo.channelID)
                .append(", channelJoinElapsed=").append(mBasicInfo.channelJoinElapsed).append("\n");
        logStr.append(mADHolder.toString());
        MyLog.w(logStr.toString());


        logStr.delete(LOG_PREFIX.length()+1, logStr.length()); //+1是给空格留的

        List<ILogItem> itemList = mADHolder.getItemList();
        int listSize = 0;

        long logLen = 0;

        if (null != itemList && (listSize = itemList.size()) > 0) {
            for (int i=0; i<listSize; i++) {
                ILogItem e = itemList.get(i);

                logStr.append(e.toString());
                MyLog.w(logStr.toString());//MyLog的flush行为由其自己控制
                logStr.delete(LOG_PREFIX.length()+1, logStr.length()); //+1是给空格留的

                mLS.appendLog(e);

                if (dbgMode) {
                    logLen += (e.toJSONObject().toString().length());
                }

            }

            if (dbgMode) {
                MyLog.e(TAG, "SDataManager.flush() once flush string length="+logLen);
            }

            mLS.flushLog(true);
        }



        mADHolder.reset();

//        MyLog.flushLog();

        return this;
    }


    public boolean need2Flush() {
        return mADHolder.need2Flush();
    }

    //用户基本信息
    public static class SDataMgrBasicInfo {
        public long userID = -1;  //Skr的用户ID//退出的时候要复位
        public String channelID = "no-channel";//退出的时候要复位
        public int channelJoinElapsed = -1; //退出的时候要复位
    }

}