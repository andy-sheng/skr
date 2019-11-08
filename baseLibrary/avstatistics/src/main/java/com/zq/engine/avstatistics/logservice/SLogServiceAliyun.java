package com.zq.engine.avstatistics.logservice;


import android.content.Context;

import com.aliyun.sls.android.sdk.ClientConfiguration;
import com.aliyun.sls.android.sdk.LOGClient;
import com.aliyun.sls.android.sdk.LogException;
import com.aliyun.sls.android.sdk.SLSLog;
import com.aliyun.sls.android.sdk.core.auth.PlainTextAKSKCredentialProvider;
import com.aliyun.sls.android.sdk.core.callback.CompletedCallback;
import com.aliyun.sls.android.sdk.model.Log;
import com.aliyun.sls.android.sdk.model.LogGroup;
import com.aliyun.sls.android.sdk.request.PostLogRequest;
import com.aliyun.sls.android.sdk.result.PostLogResult;
import com.common.log.MyLog;
import com.zq.engine.avstatistics.datastruct.ILogItem;

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
//        mParam.skrUid = initParam.skrUid;
//        mParam.appCtx = initParam.appCtx;
        mParam.appCtx = (Context)param;
        setupSLSClient(mParam.appCtx);

        mLogGroup = new LogGroup(LOG_TOPIC, LOG_SOURSE);
        mHasInitialized = true;
        return;
    }

    @Override
    public void uninit() {

    }

    @Override
    public void appendLog(String key, String value) {
        //TODO: to implement
        return;
    }

    @Override
    public void appendLog(String key, JSONObject jsonObject) {
        //TODO: to implement
    }

    @Override
    public void appendLog(ILogItem itemOp) {
        JSONObject jsObj = itemOp.toJSONObject();
        try {
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


    private void setupSLSClient(Context ctx) {
        //        移动端是不安全环境，不建议直接使用阿里云主账号ak，sk的方式。建议使用STS方式。具体参见
//        https://help.aliyun.com/document_detail/62681.html
//        注意：SDK 提供的 PlainTextAKSKCredentialProvider 只建议在测试环境或者用户可以保证阿里云主账号AK，SK安全的前提下使用。
//		  具体使用如下

//        //主账户使用方式
        String AK = "LTAI4FjvHH8Bp5rpXjnEKmqs";//"********";
        String SK = "7LuZLkpfvYc0fKp6BCHWNibRQNY0L7";//"********";
        PlainTextAKSKCredentialProvider credentialProvider =
                new PlainTextAKSKCredentialProvider(AK, SK);

//        //STS使用方式
//        String STS_AK = "haha_AK";//"******";
//        String STS_SK = "haha_SK";//"******";
//        String STS_TOKEN = "haha_TOKEN";//"******";
//        StsTokenCredentialProvider credentialProvider =
//                new StsTokenCredentialProvider(STS_AK, STS_SK, STS_TOKEN);




        ClientConfiguration conf = new ClientConfiguration();
        conf.setConnectionTimeout(15 * 1000); // 连接超时，默认15秒
        conf.setSocketTimeout(15 * 1000); // socket超时，默认15秒
        conf.setMaxConcurrentRequest(5); // 最大并发请求书，默认5个
        conf.setMaxErrorRetry(2); // 失败后最大重试次数，默认2次
        conf.setCachable(false);
        conf.setConnectType(ClientConfiguration.NetworkPolicy.WWAN_OR_WIFI);
        SLSLog.enableLog(); // log打印在控制台

        mLogClient = new LOGClient(ctx, mEndPoint, credentialProvider, conf);
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
