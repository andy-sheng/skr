package com.zq.mediaengine.kit;


import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.common.log.MyLog;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiResult;
import com.zq.engine.avstatistics.SDataManager;
import com.zq.engine.avstatistics.sts.SLogServiceSTSApi;
import com.zq.engine.avstatistics.sts.SSTSCredentialHolder;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Response;


/**
 * @author gongjun@skrer.net
 * @date 2019.11.12
 **/
public class ZqLSCredentialHolder implements SSTSCredentialHolder
{
    private final static String TAG = "[SLS]ZqLSCredHolder";

    private String mAK = null;
    private String mSK = null;
    private String mToken = null;

    private Date mDate = null;
    private ServiceStatus mSS = null;

    private final static int ERR_NONE = 0;
    private final static int ERR_1 = ERR_NONE + 1;
    private final static int ERR_2 = ERR_NONE + 2;
    private final static int ERR_3 = ERR_NONE + 3;
    private final static int ERR_4 = ERR_NONE + 4;
    private final static int ERR_5 = ERR_NONE + 5;
    private final static int ERR_6 = ERR_NONE + 6;
    private final static int ERR_7 = ERR_NONE + 7;
    private final static int ERR_8 = ERR_NONE + 8;
    private final static int ERR_9 = ERR_NONE + 9;
    private final static int ERR_10 = ERR_NONE + 10;
//    private final static int ERR_11 = ERR_NONE + 11;
//    private final static int xxx = -1;
    private final static int ERR_CUT_OFF = ERR_NONE + 106; //106 是以个与服务器约定的错误代码，表示熔断

    ZqLSCredentialHolder() {
        reset();
        mSS = new ServiceStatus();
    }


    private final static String DEFAULT_STRING_FOR_CREDENTIAL = "STRING_TO_AVOID_ALI_CRASH"; //绕开阿里云的bug: 如果key是零长度的 "" 会crash，且无法catch........
    private void reset() {
        mAK = DEFAULT_STRING_FOR_CREDENTIAL;
        mSK = DEFAULT_STRING_FOR_CREDENTIAL;
        mToken = DEFAULT_STRING_FOR_CREDENTIAL;
        mDate = null;
    }



    @Override
    public ServiceStatus getStatus() {
        boolean isExpired = false;
        boolean toCutOff = false;

        if (null == mDate) { //for first call to this api
            isExpired = true;
        }


        if (null != mDate && System.currentTimeMillis() >= mDate.getTime()) {
            isExpired = true;
            if (SDataManager.dbgMode) {
                Log.d(TAG, "isExpired(): Credential is expired!");
            }
        }

        if (isExpired) {
            int res = performAuthentication_byString();
            if (ERR_NONE != res) {
                MyLog.e(TAG, "in isExpired() performAuthentication return err="+res);
                reset();
            }
            if (ERR_CUT_OFF == res) {
                toCutOff = true;
            }
        }

        mSS.isExpired = isExpired;
        mSS.toCutOff = toCutOff;
        return mSS;
    }

    private final static String KEY_STATUS_CODE = "statusCode";
    private final static String KEY_ERRNO = "errno";
    private final static int STATUS_OK_FROM_ALIYUN = 200;

    public int performAuthentication_byString() {
        int res = 0;
        SLogServiceSTSApi stsAPI = ApiManager.getInstance().createService(SLogServiceSTSApi.class);
        if (null == stsAPI) return ERR_1;


        Call<String> call = stsAPI.getSTSTokenByString();
        if (null == call) return ERR_2;

        Response<String> response = null;
        try {
            response = call.execute();
        } catch (IOException e) {
            MyLog.e(TAG, e);
            return ERR_3;
        }
        if (null == response) return ERR_4;

        String apiResult = response.body();
        if (null == apiResult) return ERR_5;
        JSONObject jsObj = JSONObject.parseObject(apiResult);
        if (null == jsObj) return ERR_6;

        String expirationStr = null;
        if (jsObj.containsKey(KEY_STATUS_CODE) && STATUS_OK_FROM_ALIYUN == jsObj.getIntValue(KEY_STATUS_CODE)) {
            mAK = jsObj.getString("accessKeyId");
            mSK = jsObj.getString("accessKeySecret");
            mToken = jsObj.getString("securityToken");
            expirationStr = jsObj.getString("expiration");
            if (null == mAK || null == mSK || null == mToken || null == expirationStr) {
                MyLog.e(TAG, "performAuthentication_byString() something wrong with the ak/sk/token:");
                MyLog.e(TAG, "        mAK = "+mAK);
                MyLog.e(TAG, "        mSK = "+mSK);
                MyLog.e(TAG, "        mToken = "+mToken);
                MyLog.e(TAG, "        expirationStr(from STS server. GMT00:00 Time): "+expirationStr);
                return ERR_8;
            }
        }
        else if (jsObj.containsKey(KEY_ERRNO) && ERR_CUT_OFF == jsObj.getIntValue(KEY_ERRNO)){
            MyLog.e(TAG, "performAuthentication_byString() server cut off the services!");
            return ERR_CUT_OFF;
        }
        else {
            MyLog.e(TAG, "Exception case, return json string is: "+jsObj.toString());
            return ERR_10;
        }


//        if (SDataManager.dbgMode) {
            MyLog.w(TAG, "performAuthentication_byString() after get credential info:");
            MyLog.w(TAG, "        mAK = "+mAK);
            MyLog.w(TAG, "        mSK = "+mSK);
            MyLog.w(TAG, "        mToken = "+mToken);
            MyLog.w(TAG, "        expirationStr(from STS server. GMT00:00 Time): "+expirationStr);
//        }

        mDate = trans2ClientExpiredDate(expirationStr);
        if (null == mDate) return ERR_9;

        return ERR_NONE;
    }


    //后期再切换到这个实现, 看一下ApiResult与retrofit的对接
//    public int performAuthentication_ByApiResult() {
//        int res = 0;
//        SLogServiceSTSApi stsAPI = ApiManager.getInstance().createService(SLogServiceSTSApi.class);
//        if (null == stsAPI) return ERR_1;
//
//        //以ApiResult为例
//        Call<ApiResult> call = stsAPI.getSTSTokenByApiResult();
//        if (null == call) return ERR_2;
//
//        Response<ApiResult> response = null;
//        try {
//            response = call.execute();
//        } catch (IOException e) {
//            MyLog.e(TAG, e);
//            return ERR_3;
//        }
//        if (null == response) return ERR_4;
//
//        ApiResult apiResult = response.body();
//        if (null == apiResult) return ERR_5;
//        if (0 != apiResult.getErrno()) {
//            MyLog.e(TAG, "performAuthentication_ByApiResult() apiResult err="+apiResult.getErrno());
//            return ERR_6;
//        }
//
//        JSONObject jsObj = apiResult.getData();
//        if (null == jsObj) return ERR_6;
//
//        int  statusCode = jsObj.getIntValue(KEY_STATUS_CODE);
//        if (ERR_CUT_OFF == statusCode) {
//            MyLog.e(TAG, "performAuthentication_ByApiResult() statusCode="+statusCode+", log services will be cut off!");
//            return statusCode;
//        }
//
//        if (200 != statusCode) {
//            MyLog.e(TAG, "performAuthentication_ByApiResult() statusCode="+statusCode);
//            return ERR_7;
//        }
//
//        mAK = jsObj.getString("accessKeyId");
//        mSK = jsObj.getString("accessKeySecret");
//        mToken = jsObj.getString("securityToken");
//        String expirationStr = jsObj.getString("expiration");
//        if (null == mAK || null == mSK || null == mToken || null == expirationStr) {
//            MyLog.e(TAG, "performAuthentication_ByApiResult() something wrong with the ak/sk/token:");
//            MyLog.e(TAG, "        mAK = "+mAK);
//            MyLog.e(TAG, "        mSK = "+mSK);
//            MyLog.e(TAG, "        mToken = "+mToken);
//            MyLog.e(TAG, "        expirationStr(from STS server. GMT00:00 Time): "+expirationStr);
//            return ERR_8;
//        }
//
//        if (SDataManager.dbgMode) {
//            Log.d(TAG, "performAuthentication_ByApiResult() after get credential info:");
//            Log.d(TAG, "        mAK = "+mAK);
//            Log.d(TAG, "        mSK = "+mSK);
//            Log.d(TAG, "        mToken = "+mToken);
//            Log.d(TAG, "        expirationStr(from STS server. GMT00:00 Time): "+expirationStr);
//        }
//
//        mDate = trans2ClientExpiredDate(expirationStr);
//        if (null == mDate) return ERR_9;
//
//        return ERR_NONE;
//    }



    @Override
    public String getAK() {
        return mAK;//DEFAULT_STRING_FOR_CREDENTIAL;//
    }

    @Override
    public String getSK() {
        return mSK;//DEFAULT_STRING_FOR_CREDENTIAL;//
    }

    @Override
    public String getToken() {
        return mToken;//DEFAULT_STRING_FOR_CREDENTIAL;//
    }




    /**
     *  处理必须考虑到：用户本地时间不准的问题
     *  近似处理: 服务器返回的expiration 减去1小时，就是服务器当前的时间
     *  这里与服务器有一个重要约定: 服务器的credential有效期必须保持1个小时
     *  服务器返回的字段示例(TimeZone=GMT00:00)：
     *      "expiration": "2019-11-11T12:01:09Z"
     */
    private static long CREDENTIAL_PERSISTENT_MS = 60 * 60 * 1000;//1小时
    private static long REDUNTANT_MS= 5 *60 * 1000; //留5分钟的冗余，提前5分钟认为过期


    private final static String EXIRATION_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private Date trans2ClientExpiredDate(String serverExpiration) {
        if (SDataManager.dbgMode) {//debug mode缩短有效期(需服务器配合)，阿里STS最短900秒; 测试用的log-store
            CREDENTIAL_PERSISTENT_MS = 900 * 1000;
            REDUNTANT_MS = 60 * 1000;
        }


        SimpleDateFormat sdf = new SimpleDateFormat(EXIRATION_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT00:00"));
        Date serverExpiredDate = null;
        Date clientExpiredDate = null;


        long serverCurMS = 0;
        long clientCurMS = System.currentTimeMillis();
        long offset = 0;

        try {
            serverExpiredDate = sdf.parse(serverExpiration);
//            if (SDataManager.dbgMode) {
                MyLog.w(TAG, "trans2ClientExpiredDate(): serverExpiredDate is:"+serverExpiredDate.toString());
//            }
        } catch (ParseException e) {
            e.printStackTrace();
//            return ERR_9;
        }

        serverCurMS = serverExpiredDate.getTime() - CREDENTIAL_PERSISTENT_MS;
        offset = clientCurMS - serverCurMS;

        clientExpiredDate = new Date(serverExpiredDate.getTime() + offset - REDUNTANT_MS);
//        if (SDataManager.dbgMode) {
            MyLog.w(TAG, "clientCurMS - serverCurMS = "+offset);
            MyLog.w(TAG, "trans2ClientExpiredDate(): new client expiration is:"+clientExpiredDate.toString());
//        }
        return clientExpiredDate;
    }



}