package com.wali.live.common.statistics;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.preference.PreferenceUtils;
import com.base.utils.Base64Coder;
import com.base.utils.Constants;
import com.base.utils.DeviceUtils;
import com.base.utils.MD5;
import com.base.utils.SHA;
import com.base.utils.channel.ReleaseChannelUtils;
import com.base.utils.language.LocaleUtil;
import com.base.utils.network.Network;
import com.base.utils.version.VersionManager;
import com.mi.live.data.account.UserAccountManager;
import com.wali.live.common.statistics.pojo.BaseStatisticsItem;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import rx.Subscription;

/**
 * Created by queda on 16-7-14.
 */
public abstract class BaseStatisticsWorker {

    public static final String UPLOAD_URL = "https://data.game.xiaomi.com/p.do";
    public static final String AC_APP = "ml_app";

    //内存中存储的打点日志记录
    protected List<BaseStatisticsItem> mItems = new ArrayList<>();

    //用于定时查询是否到了打点日志上传的事件
    protected Handler mHandler;
    protected HandlerThread mHandlerThread;

    private String TAG;
    private static final String KEY_TS = "base_statistics_worker_key_ts";

    //用来记录上次上传的时间，与当前系统时间对比，检查是否需要上传打点日志
    private long mLastUploadTimeStamp;
    //同时也作为打点的时间戳 每次上传作为id 用途给服务端去重用
    protected long mTS;

    //默认上传打点日志时间间隔
    private final static long DEFAULT_UPLOAD_INTERVAL = 30 * 60 * 1000;
    //默认检查时间
    private final static long DEFAULT_CHECK_INTERVAL = 15 * 60 * 1000;

    public BaseStatisticsWorker() {
        TAG = getTAG();
        mHandlerThread = new HandlerThread(TAG);
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());
        //用handler保证时序 处理必要耗时初始化工作
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                long last = now - DEFAULT_UPLOAD_INTERVAL;
                mLastUploadTimeStamp = PreferenceUtils.getSettingLong(GlobalData.app().getSharedPreferences(TAG, Context.MODE_PRIVATE), TAG, last);
                if (mLastUploadTimeStamp == last) {
                    PreferenceUtils.setSettingLong(GlobalData.app().getSharedPreferences(TAG, Context.MODE_PRIVATE), TAG, mLastUploadTimeStamp);
                }
                MyLog.v(TAG + " init mLastUploadTimeStamp=" + mLastUploadTimeStamp);
                mTS = PreferenceUtils.getSettingLong(GlobalData.app().getSharedPreferences(KEY_TS, Context.MODE_PRIVATE), KEY_TS, now);
                if (mTS == now) {
                    PreferenceUtils.setSettingLong(GlobalData.app().getSharedPreferences(KEY_TS, Context.MODE_PRIVATE), KEY_TS, mTS);
                }
                MyLog.v(TAG + " init mTS=" + mTS);
                //读取数据内存
                loadFromFileOnHandlerThread();
                MyLog.v(TAG + " mItems=" + mItems.toString());
                if (isNeedTimedUpload()) {
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            checkNeedUploadToServerOnHandlerThread();
                        }
                    }, DEFAULT_CHECK_INTERVAL);
                }
            }
        });
    }

    /**
     * 隔一段时间间隔来检查是否应该上传日志
     */
    private void checkNeedUploadToServerOnHandlerThread() {
        long now = System.currentTimeMillis();
        MyLog.v(TAG + " timer check " + (now - mLastUploadTimeStamp) + "/" + DEFAULT_UPLOAD_INTERVAL + " size=" + mItems.size());
        if (now - mLastUploadTimeStamp >= DEFAULT_UPLOAD_INTERVAL) {
            boolean success = pushDatasOnHandlerThread(mItems);
            if (success) {
                //清空所有数据
                mItems.clear();
                clearFileOnHandlerThread();
                mLastUploadTimeStamp = now;
                PreferenceUtils.setSettingLong(GlobalData.app().getSharedPreferences(TAG, Context.MODE_PRIVATE), TAG, mLastUploadTimeStamp);
            } else {
                MyLog.e("上传用户召回打点日志失败");
            }
        } else {
            if (now - mLastUploadTimeStamp < 0) {
                mLastUploadTimeStamp = now;
                PreferenceUtils.setSettingLong(GlobalData.app().getSharedPreferences(TAG, Context.MODE_PRIVATE), TAG, now);
            }
        }
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                checkNeedUploadToServerOnHandlerThread();
            }
        }, DEFAULT_CHECK_INTERVAL);
    }

    /**
     * 网络上报
     */
    public boolean pushDatasOnHandlerThread(List<BaseStatisticsItem> datas) {
        boolean can = (UserAccountManager.getInstance().hasAccount() || !ReleaseChannelUtils.isMIUICTAPkg());
        if (!can) {
            return false;
        }
        //无论成功失败修改上报id
        mTS = System.currentTimeMillis();
        PreferenceUtils.setSettingLong(GlobalData.app().getSharedPreferences(KEY_TS, Context.MODE_PRIVATE), KEY_TS, mTS);
        if (datas == null || datas.isEmpty()) {
            return true;
        }
        String data = getDataJSONString(datas);
        if (data == null || !Network.hasNetwork(GlobalData.app().getApplicationContext())) {
            return false;
        }
        MyLog.v(TAG + " data = " + data);
        List<NameValuePair> pairs = new ArrayList<>();
        try {
            String base64Data = new String(Base64Coder.encode(data.getBytes()));
            MyLog.v(TAG + " base64Data = " + base64Data);
            pairs.add(new BasicNameValuePair("data", base64Data));
            boolean ret = Network.doHttpPostReqOnly(GlobalData.app(), UPLOAD_URL, pairs);
            MyLog.v(TAG + " pushDataPost ret=" + ret);
            return ret;
        } catch (IOException e) {
            MyLog.e(e);
        }
        return false;
    }

    Subscription mPermissionSubsription;
    /**
     * 为网络上报进行上传数据段的构造
     *
     * @param datas 需要上报的打点数据集合
     * @return 返回构造好的json字符串
     */
    private String getDataJSONString(List<BaseStatisticsItem> datas) {
        if (null == datas || datas.isEmpty()) {
            return null;
        }

        JSONObject obj = new JSONObject();
        try {
            obj.put("mid", UserAccountManager.getInstance().getUuid());
            obj.put("appid", Constants.MILINK_APP_ID);
            obj.put("os", "android_" + Build.VERSION.RELEASE);
            obj.put("app_version", VersionManager.getCurrentVersionCode(GlobalData.app()));
            obj.put("chan_id", VersionManager.getReleaseChannel(GlobalData.app()));
            obj.put("model", Build.MODEL);
            String lang = "";
            Locale local = LocaleUtil.getLocale();
            if(local != null){
                lang = local.toString();
            }
            obj.put("lang", lang);

            String imei = DeviceUtils.getDeviceId();


            if (!TextUtils.isEmpty(imei)) {
                obj.put("device", SHA.miuiSHA1(imei));
                obj.put("device_md5", MD5.MD5_32(imei));
            }

            JSONArray contentArray = new JSONArray();
            for (
                    BaseStatisticsItem item
                    : datas)

            {
                JSONObject o = item.toJSONObject();
                if (null != o) {
                    contentArray.put(o);
                }
            }

            obj.put("content", contentArray);
            return obj.toString();
        } catch (
                JSONException e
                )

        {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 每次上传成功之后需要需要清除本地文件的日志内容
     */

    private void clearFileOnHandlerThread() {
        try {
            File file = getDiaryFile();
            if (file.exists()) {
                file.delete();
            }
        } catch (Exception e) {
            MyLog.e(TAG + " " + e.getMessage());
        }
    }

    /**
     * 提交打点需求之后，写入文件，等待一个上传周期一起上传
     * 持久化到本地
     *
     * @param item 需要写入文件的打点数据
     * @throws IOException
     */
    protected void writeToFile(BaseStatisticsItem item) throws IOException {
        MyLog.v("writeToFile start");
        FileWriter fw = null;
        BufferedWriter bw = null;
        try {
            fw = new FileWriter(getDiaryFile(), true);
            bw = new BufferedWriter(fw);
            JSONObject jsonObject = item.toJSONObject();
            String lineStr = jsonObject.toString() + "\n";
            bw.write(lineStr);
            bw.flush();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if (bw != null) {
                bw.close();
            }
            if (fw != null) {
                fw.close();
            }
        }
        MyLog.v("writeToFile stop");
    }

    /**
     * 提交打点数据到内存以及文件的外部接口
     *
     * @param item
     */
    public void recordOnMainThreadDelayUpload(final BaseStatisticsItem item) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                MyLog.w(TAG, " recordOnMainThreadDelayUpload actionKay=" + item.getLogKey());
                if (item != null) {
                    if (!needUpdateInMemory(item)) {
                        mItems.add(item);
                    }
                    try {
                        writeToFile(item);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public void recordOnMainThreadImmediatelyUpload(final BaseStatisticsItem item) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                MyLog.w(TAG, " recordOnMainThreadImmediatelyUpload actionKay=" + item.getLogKey());
                if (item != null) {
                    if (!needUpdateInMemory(item)) {
                        mItems.add(item);
                    }
                    //插入之后一起上传
                    boolean success = pushDatasOnHandlerThread(mItems);
                    if (success) {
                        //清空所有数据
                        mItems.clear();
                        clearFileOnHandlerThread();
                        mLastUploadTimeStamp = System.currentTimeMillis();
                        PreferenceUtils.setSettingLong(GlobalData.app().getSharedPreferences(TAG, Context.MODE_PRIVATE), TAG, mLastUploadTimeStamp);
                    } else {
                        try {
                            writeToFile(item);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        MyLog.e("上传打点失败 写入文件");
                    }
                }
            }
        });
    }


    public static String getDateYYYYMMDD() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String date = sdf.format(new java.util.Date());
        return date;
    }

    public void close() {
        if (null != mHandler) {
            mHandler.removeCallbacksAndMessages(null);
            if (Build.VERSION.SDK_INT >= 18) {
                mHandler.getLooper().quitSafely();
            } else {
                mHandler.getLooper().quit();
            }
            mHandler = null;
        }
    }

    protected abstract String getTAG();

    protected abstract File getDiaryFile() throws IOException;

    //第一次启动从本地持久化文件中读取到内存
    protected abstract void loadFromFileOnHandlerThread();

    //更新内存返回true  添加返回false
    protected abstract boolean needUpdateInMemory(BaseStatisticsItem item);

    //用来标识是否需要定时上传日志，是否启用handler一套
    protected abstract boolean isNeedTimedUpload();
}
