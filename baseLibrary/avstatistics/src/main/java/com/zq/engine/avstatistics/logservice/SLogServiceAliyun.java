package com.zq.engine.avstatistics.logservice;


import android.content.Context;

import com.aliyun.sls.android.sdk.ClientConfiguration;
import com.aliyun.sls.android.sdk.LOGClient;
import com.aliyun.sls.android.sdk.LogException;
import com.aliyun.sls.android.sdk.SLSLog;
import com.aliyun.sls.android.sdk.core.AsyncTask;
import com.aliyun.sls.android.sdk.core.auth.PlainTextAKSKCredentialProvider;
import com.aliyun.sls.android.sdk.core.auth.StsTokenCredentialProvider;
import com.aliyun.sls.android.sdk.core.callback.CompletedCallback;
import com.aliyun.sls.android.sdk.model.Log;
import com.aliyun.sls.android.sdk.model.LogGroup;
import com.aliyun.sls.android.sdk.request.PostLogRequest;
import com.aliyun.sls.android.sdk.result.PostLogResult;
import com.common.log.MyLog;
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

    public final static String TAG = "SLogServiceAliyun";

    private final static String LOG_TOPIC = "sls-test";
    private final static String LOG_SOURSE= "Skr-app";

    /**
     * 填入必要的参数
     */
    /**
     * 内网域名: cn-beijing-intranet.log.aliyuncs.com
     * 外网域名: cn-beijing.log.aliyuncs.com
     */
    private String mEndPoint = "https://cn-beijing.log.aliyuncs.com";//"http://cn-hangzhou.sls.aliyuncs.com";
    private String mProject = "test-by-gongjun";
    private String mLogStore = "test-logstore";
    private String source_ip = "110.110.110.110";
    private boolean isAsyncGetIp = false;
    //client的生命周期和app保持一致
    private LOGClient mLogClient;
    private SLogServiceAgent.SAliYunSLParam mParam = null;

    private boolean mHasInitialized = false;
    private LogGroup mLogGroup = null;
    private SSTSCredentialHolder mSTSHolder = null;


    public SLogServiceAliyun() {
        mParam = new SLogServiceAgent.SAliYunSLParam();

    }

    @Override
    public void init(Object param) throws Exception {
        if (!(param instanceof Context)) {
            throw new Exception("When init Aliyun LogSerivce, you should use: " + Context.class.getName());
        }
//
//        SLogServiceAgent.SAliYunSLParam initParam = (SLogServiceAgent.SAliYunSLParam)param;
//
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
        //TODO: to implement if needed
        return;
    }

    @Override
    public void appendLog(String key, JSONObject jsonObject) {
        //TODO: to implement if needed
    }

    @Override
    public void appendLog(ILogItem itemOp) {
        JSONObject jsObj = itemOp.toJSONObject();
        try {
//            jsObj.put("comeFrom", "liveSDK");
            jsObj.put("SkrID", mParam.skrUid);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log log = new Log();
        log.PutContent(itemOp.getKey(), jsObj.toString());

//        android.util.Log.d(TAG, "Key is: "+itemOp.getKey());

        mLogGroup.PutLog(log);
    }

    @Override
    public void flushLog(boolean isSync) {

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
                    //本不应有这句代码，可以允许修改mSTSHolder。 但因上层要求做"非Synchronized"函数实现，又要保证另外一个线程正在使用mSTSHolder正确，只能这样处理
                    //即: 只能设置一次，正常情况下，app也不应该更换stsHolder
                    if (null != mSTSHolder) return;

                    if (!(prop instanceof SSTSCredentialHolder)) {
                        throw new Exception("when set propID("+propID+"), the prop object should be "+SSTSCredentialHolder.class.getSimpleName());
                    }
                    mSTSHolder = (SSTSCredentialHolder)prop;
                }
                break;
            default:
                {
                    throw new Exception("This ClassName("+
                            this.getClass().getSimpleName()+") doesn't support the propID("+propID+")");
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

        if (mSTSHolder.isExpired()) { //for the first time
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

            boolean dbgMode = false;
            if (dbgMode) {
                android.util.Log.d(TAG, "new sts credentials: ");
                android.util.Log.d(TAG, "        AK = "+AK);
                android.util.Log.d(TAG, "        SK = "+SK);
                android.util.Log.d(TAG, "        token = "+token);
            }


            //STS使用方式
            StsTokenCredentialProvider credentialProvider =
                    new StsTokenCredentialProvider(AK, SK, token);

            ClientConfiguration conf = new ClientConfiguration();
            conf.setConnectionTimeout(15 * 1000); // 连接超时，默认15秒
            conf.setSocketTimeout(15 * 1000); // socket超时，默认15秒
            conf.setMaxConcurrentRequest(5); // 最大并发请求书，默认5个
            conf.setMaxErrorRetry(2); // 失败后最大重试次数，默认2次
            conf.setCachable(false);
            conf.setConnectType(ClientConfiguration.NetworkPolicy.WWAN_OR_WIFI);
            SLSLog.enableLog(); // log打印在控制台

            mLogClient = new LOGClient(mParam.appCtx, mEndPoint, credentialProvider, conf);
        }


        return true;
    }

    private boolean prepareSLSCredential_byMainKey(Context ctx) {
        if (null != mLogClient)
            return true;


        //        移动端是不安全环境，不建议直接使用阿里云主账号ak，sk的方式。建议使用STS方式。具体参见
//        https://help.aliyun.com/document_detail/62681.html
//        注意：SDK 提供的 PlainTextAKSKCredentialProvider 只建议在测试环境或者用户可以保证阿里云主账号AK，SK安全的前提下使用。
//		  具体使用如下

        //主账户使用方式
        String AK = "LTAI4FjvHH8Bp5rpXjnEKmqs";//"********";
        String SK = "7LuZLkpfvYc0fKp6BCHWNibRQNY0L7";//"********";
        PlainTextAKSKCredentialProvider credentialProvider =
                new PlainTextAKSKCredentialProvider(AK, SK);


        ClientConfiguration conf = new ClientConfiguration();
        conf.setConnectionTimeout(15 * 1000); // 连接超时，默认15秒
        conf.setSocketTimeout(15 * 1000); // socket超时，默认15秒
        conf.setMaxConcurrentRequest(5); // 最大并发请求书，默认5个
        conf.setMaxErrorRetry(2); // 失败后最大重试次数，默认2次
        conf.setCachable(false);
        conf.setConnectType(ClientConfiguration.NetworkPolicy.WWAN_OR_WIFI);
        SLSLog.enableLog(); // log打印在控制台

        mLogClient = new LOGClient(ctx, mEndPoint, credentialProvider, conf);
        return true; //表示成功
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
