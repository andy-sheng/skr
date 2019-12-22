package com.zq.engine.avstatistics.logservice;


import android.content.Context;

import com.aliyun.sls.android.sdk.ClientConfiguration;
import com.aliyun.sls.android.sdk.LOGClient;
import com.aliyun.sls.android.sdk.LogException;
import com.aliyun.sls.android.sdk.SLSLog;
import com.aliyun.sls.android.sdk.core.auth.StsTokenCredentialProvider;
import com.aliyun.sls.android.sdk.core.callback.CompletedCallback;
import com.aliyun.sls.android.sdk.model.Log;
import com.aliyun.sls.android.sdk.model.LogGroup;
import com.aliyun.sls.android.sdk.request.PostLogRequest;
import com.aliyun.sls.android.sdk.result.PostLogResult;
import com.common.log.MyLog;
import com.zq.engine.avstatistics.SDataManager;
import com.zq.engine.avstatistics.datastruct.ILogItem;
import com.zq.engine.avstatistics.sts.SSTSCredentialHolder;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author gongjun@skr.net 2019.10.18
 * @brief SLogServiceAliyun is the adapter to Aliyun-log-service
 *
 */

class SLogServiceAliyun extends SLogServiceBase{

    public final static String TAG = "[SLS]SLogServiceAliyun";

    private final static String LOG_TOPIC = "AVS";
    private final static String LOG_SOURSE= "Skr-app";

    /**
     * 填入必要的参数
     */
    /**
     * 内网域名: cn-beijing-intranet.log.aliyuncs.com
     * 外网域名: cn-beijing.log.aliyuncs.com
     */
    private final static String mEndPoint = "https://cn-beijing.log.aliyuncs.com";//"http://cn-hangzhou.sls.aliyuncs.com";
    private final static String MAIN_PROJECT = "skrapp-log";
    private final static String MAIN_LOGSTORE= "avs-ls-release-android";

    private final static String TEST_PROJECT = "avstatistics-test";
    private final static String TEST_LOGSTORE= "avs-ls-test-android";

    private final static String EXP_LOGSTORE =  "avs-ls-exp";

    private String mProject  = TEST_PROJECT;
    private String mLogStore = TEST_LOGSTORE;
    private String source_ip = "110.110.110.110";
    private boolean isAsyncGetIp = false;
    //client的生命周期和app保持一致
    private LOGClient mLogClient;
    private SLogServiceAgent.SAliYunSLParam mParam = null;

    private boolean mHasInitialized = false;
    private LogGroup mLogGroup = null;
    private SSTSCredentialHolder mSTSHolder = null;
    private boolean mUseMainPrj = false;

    private boolean mEnableLS = true; //false can cut the aliyun-log-service, if something un-predictable rush into our system

    public SLogServiceAliyun() {
        mParam = new SLogServiceAgent.SAliYunSLParam();

    }

    @Override
    public void init(Object param) throws Exception {
        if (!(param instanceof Context)) {
            throw new Exception("When init Aliyun LogSerivce, you should use: " + Context.class.getName());
        }

        mParam.appCtx = (Context)param;
        mLogGroup = new LogGroup(LOG_TOPIC, LOG_SOURSE);
        mHasInitialized = true;

        return;
    }

    @Override
    public void uninit() {

    }

    @Override
    public void appendLog(String key, String value) {
        if (!mEnableLS) return;

        //TODO: to implement if needed
        return;
    }

    @Override
    public void appendLog(String key, JSONObject jsonObject) {
        if (!mEnableLS) return;
        //TODO: to implement if needed
    }

    @Override
    public void appendLog(ILogItem itemOp) {

        if (!mEnableLS) return;

        JSONObject jsObj = itemOp.toJSONObject();
        try {
//            jsObj.put("comeFrom", "liveSDK");
            jsObj.put("SkrID", mParam.skrUid);
            Log log = new Log();
            log.PutContent(itemOp.getKey(), jsObj.toString());
            mLogGroup.PutLog(log);
        } catch (Exception e) {
            MyLog.e(e);
        }
    }

    @Override
    public void flushLog(boolean isSync) {

        if (!mEnableLS) return;

        if (!prepareSLSClientBySTS()) {
            return;
        }

        uploadLogGroupAsync(mLogGroup);
        mLogGroup = new LogGroup(LOG_TOPIC, LOG_SOURSE);
    }

    @Override
    public void setProp(int propID, Object prop) throws Exception {
        switch (propID) {
            case PROP_USER_ID:
                {
                    if (!(prop instanceof Long)) {
                        throw new Exception("when set propID("+propID+"), the prop object should be Long!");
                    }

                    mParam.skrUid = ((Long)prop).longValue();
                }
                break;
            case PROP_STS_CREDENTIAL_HOLDER:
                {
                    //本应允许随意替换mSTSHolder。 但为了尽可能做"非Synchronized"实现，又要保证另外一个线程正在使用mSTSHolder正确，只能这样处理
                    //即: 只能设置一次。（正常情况下，上层也不应该更换stsHolder)
                    if (null != mSTSHolder) return;

                    if (!(prop instanceof SSTSCredentialHolder)) {
                        throw new Exception("when set propID("+propID+"), the prop object should be SSTSCredentialHolder");
                    }
                    mSTSHolder = (SSTSCredentialHolder)prop;
                    mSTSHolder.setProp(SSTSCredentialHolder.PROP_RELEASE_VERSION, mUseMainPrj);
                }
                break;
            case PROP_ENABLE_SERVICES:
                {
                    if (!(prop instanceof Boolean)) {
                        throw new Exception("when set propID("+propID+"), the prop object should be "+Boolean.class.getSimpleName());
                    }
                    mEnableLS = ((Boolean)prop).booleanValue();
                    MyLog.w(TAG, "Enable log service="+mEnableLS);
                }
                break;
            case PROP_USE_MAIN_LOG_PROJECT:
                {
                    if (!(prop instanceof Boolean)) {
                        throw new Exception("when set propID("+propID+"), the prop object should be "+Boolean.class.getSimpleName());
                    }

                    mUseMainPrj = ((Boolean)prop).booleanValue();
                    if (mUseMainPrj) {
                        mProject = MAIN_PROJECT;
                        mLogStore= MAIN_LOGSTORE;
                    }
                    else {
                        mProject = TEST_PROJECT;
                        mLogStore= TEST_LOGSTORE;
                    }

                    if (null != mSTSHolder) { //main project 对应 release version
                        mSTSHolder.setProp(SSTSCredentialHolder.PROP_RELEASE_VERSION, mUseMainPrj);
                    }


                    MyLog.w(TAG, "useMain project="+mUseMainPrj);
                    MyLog.w(TAG, "    project = "+mProject);
                    MyLog.w(TAG, "    logStore= "+mLogStore);

                }
                break;
            default:
                {
                    throw new Exception("This ClassName(SLogServicesAliyun) doesn't support the propID("+propID+")");
                }

        }
    }

    @Override
    public Object getProp(int propID) {
        Object obj = null;
        switch (propID) {
            case PROP_IS_INITIALIZED:
                {
                    obj = new Boolean(mHasInitialized);
                }
                break;
            default:
                break;
        }
        return obj;
    }


    private boolean prepareSLSClientBySTS() {

        if (null == mSTSHolder || null == mParam.appCtx) {
            MyLog.e(TAG, "in prepareSLSClientBySTSHolder() err!!!check null: mSTSHolder="+mSTSHolder+", mParam.appCtx="+mParam.appCtx);
            return false;
        }

        SSTSCredentialHolder.ServiceStatus ss = mSTSHolder.getStatus();
        if (ss.toCutOff) {
            mEnableLS = false;
            return false;
        }

        if (ss.isExpired) { //for the first time
            String AK = mSTSHolder.getAK();
            String SK = mSTSHolder.getSK();
            String token = mSTSHolder.getToken();

            if (null == AK || null == SK || null == token){
                MyLog.e(TAG, "there is null return, when call mSTSHolder's API. Check below: ");
                MyLog.e(TAG, "    mSTSHolder.getAK() = "+AK);
                MyLog.e(TAG, "    mSTSHolder.getSK() = "+SK);
                MyLog.e(TAG, "    mSTSHolder.getToken() = "+token);
                return false;
            }



            //STS使用方式
            StsTokenCredentialProvider credentialProvider =
                    new StsTokenCredentialProvider(AK, SK, token);

            ClientConfiguration conf = new ClientConfiguration();
            conf.setConnectionTimeout(15 * 1000); // 连接超时，默认15秒
            conf.setSocketTimeout(15 * 1000); // socket超时，默认15秒
            conf.setMaxConcurrentRequest(5); // 最大并发请求书，默认5个
            conf.setMaxErrorRetry(2); // 失败后最大重试次数，默认2次
            conf.setCachable(false); //先关闭cache，绕开aliyun SLS的问题
            conf.setConnectType(ClientConfiguration.NetworkPolicy.WWAN_OR_WIFI);

            if (!mUseMainPrj) { //说明是测试版
                SLSLog.enableLog(); // log打印在控制台
            }

            mLogClient = new LOGClient(mParam.appCtx, mEndPoint, credentialProvider, conf);
        }


        return true;
    }



    /*
     *  推荐使用的方式，直接调用异步接口，通过callback 获取回调信息
     */
    private void uploadLogGroupAsync(LogGroup group) {
        try {
            PostLogRequest request = new PostLogRequest(mProject, mLogStore, group);

            mLogClient.asyncPostLog(request, new CompletedCallback<PostLogRequest, PostLogResult>() {
                @Override
                public void onSuccess(PostLogRequest request, PostLogResult result) {
//                    android.util.Log.d(TAG, "asyncPostLog onSuccess");
//                    MyLog.d(TAG, "asyncPostLog onSuccess");
                }

                @Override
                public void onFailure(PostLogRequest request, LogException exception) {
//                    android.util.Log.e(TAG, "asyncPostLog onFailure:"+exception.toString());
                    MyLog.w(TAG, "asyncPostLog onFailure:"+exception.toString());
                }
            });
        } catch (LogException e) {

            MyLog.w(TAG, "uploadLogGroupAsync() exception = "+ e.toString());

            //e.printStackTrace();
        }
        return;
    }


    /*
     *  推荐使用的方式，直接调用异步接口，通过callback 获取回调信息
     */
    private void asyncUploadLog(String key, String jsonStr) {
        /* 创建logGroup */
        LogGroup group = new LogGroup("sls test", "110.110.110.110");


        /* 存入一条log */
        Log log = new Log();
        log.PutContent(key, jsonStr);
        group.PutLog(log);


        try {
            PostLogRequest request = new PostLogRequest(mProject, mLogStore, group);
            mLogClient.asyncPostLog(request, new CompletedCallback<PostLogRequest, PostLogResult>() {
                @Override
                public void onSuccess(PostLogRequest request, PostLogResult result) {
//                    android.util.Log.d(TAG, "asyncPostLog onSuccess");
                    MyLog.d(TAG, "asyncPostLog onSuccess");
                }

                @Override
                public void onFailure(PostLogRequest request, LogException exception) {
//                    android.util.Log.e(TAG, "asyncPostLog onFailure:"+exception.toString());
                    MyLog.w(TAG, "asyncPostLog onFailure:"+exception.toString());
                }
            });
        } catch (LogException e) {
            e.printStackTrace();
        }
    }

}
