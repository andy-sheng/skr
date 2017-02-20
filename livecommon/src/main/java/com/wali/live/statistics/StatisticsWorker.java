package com.wali.live.statistics;

import android.os.Environment;
import android.text.TextUtils;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.preference.PreferenceUtils;
import com.wali.live.common.statistics.BaseStatisticsWorker;
import com.wali.live.common.statistics.pojo.BaseStatisticsItem;
import com.wali.live.statistics.pojo.StatisticsItem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by yurui on 3/8/16.
 */
public class StatisticsWorker extends BaseStatisticsWorker {
    public static final String TAG = StatisticsWorker.class.getSimpleName();
    public static final String DAILY_REPORT = "statis_daily_report";//偏好key 每天只执行一次 为了保证日活打点不会丢第一次启动app立即上传
    public static final boolean ENABLE = true;

    private final static String DIARY_FILE_DIR = Environment.getExternalStorageDirectory() + "/Xiaomi/WALI_LIVE/statisticsWorker";
    private final static String DIARY_FILE_NAME = "statisticsWorkerDiary3";
    private final static String DIARY_FILE_PATH = DIARY_FILE_DIR + File.separator + DIARY_FILE_NAME;

    private static StatisticsWorker sInstance = null;

    private StatisticsWorker() {
        super();
        boolean isDaily = false;
        String date = getDateYYYYMMDD();
        if (PreferenceUtils.hasKey(GlobalData.app(), DAILY_REPORT)) {
            String recordDate = PreferenceUtils.getSettingString(GlobalData.app(), DAILY_REPORT, "");
            if (!recordDate.equals(date)) {
                isDaily = true;
            }
            MyLog.v(TAG + " DAILY_REPORT last=" + recordDate + " now=" + date);
        } else {
            isDaily = true;
        }
        if (isDaily) {
            //防止没有数据 所以增加一条活跃
            sendCommandRealTime(StatisticsWorker.AC_APP, StatisticsKey.TYPE_APP_OPEN_COUNT, 1);
        }
    }

    public synchronized static StatisticsWorker getsInstance() {
        if (null == sInstance) {
            sInstance = new StatisticsWorker();
        }
        return sInstance;
    }

    //定时上报
    public void sendCommand(final String ac, final String actionKey, final long value) {
        if (ENABLE) {
            //如果不post mTS可能为0
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    StatisticsItem item = new StatisticsItem(ac, actionKey, getDateYYYYMMDD(), value, mTS);
                    if (item.isLegal()) {
                        recordOnMainThreadDelayUpload(item);
                    }
                }
            });
        }
    }

    //实时上报
    public void sendCommandRealTime(final String ac, final String actionKey, final long value) {
        if (ENABLE) {
            //如果不post mTS可能为0
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    StatisticsItem item = new StatisticsItem(ac, actionKey, getDateYYYYMMDD(), value, mTS);
                    if (item.isLegal()) {
                        recordOnMainThreadImmediatelyUpload(item);
                    }
                }
            });
        }
    }

    private StatisticsItem findStatisticsItemInMemory(final String acIn, final String actionKeyIn, final String date, final long ts) {
        if (TextUtils.isEmpty(acIn) || TextUtils.isEmpty(actionKeyIn) || TextUtils.isEmpty(date) || ts == 0) {
            return null;
        }

        for (int i = 0; i < mItems.size(); ++i) {
            StatisticsItem tmp = ((StatisticsItem) mItems.get(i));
            if (TextUtils.isEmpty(tmp.getAc()) || TextUtils.isEmpty(tmp.getActionKey())) {
                continue;
            }
            if (tmp.getAc().equals(acIn) && tmp.getActionKey().equals(actionKeyIn) && tmp.getDate().equals(date) && tmp.getTimeStamp() == ts) {
                return tmp;
            }
        }
        return null;
    }

    @Override
    protected File getDiaryFile() throws IOException {
        File tempPath = new File(DIARY_FILE_DIR);
        if (!tempPath.exists()) {
            tempPath.mkdirs();
        }
        File file = new File(DIARY_FILE_PATH);
        if (!file.exists()) {
            file.createNewFile();
        }
        return file;
    }

    @Override
    public void close() {
        super.close();
        sInstance = null;
    }

    @Override
    protected String getTAG() {
        return TAG;
    }

    @Override
    protected void loadFromFileOnHandlerThread() {
        BufferedReader fileReader = null;
        try {
            if (getDiaryFile() != null) {
                FileReader fr = new FileReader(DIARY_FILE_PATH);
                fileReader = new BufferedReader(fr);
                String line = null;
                while ((line = fileReader.readLine()) != null) {
                    try {
                        StatisticsItem item = new StatisticsItem(line);
                        if (item.isLegal()) {
                            StatisticsItem itemInMemory = findStatisticsItemInMemory(item.getAc(), item.getActionKey(), item.getDate(), item.getTimeStamp());
                            if (itemInMemory != null) {
                                itemInMemory.setValue(itemInMemory.getValue() + item.getValue());
                                continue;
                            }
                            mItems.add(item);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                fileReader.close();
                fr.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != fileReader) {
                try {
                    fileReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected boolean needUpdateInMemory(BaseStatisticsItem itm) {
        if (itm instanceof StatisticsItem) {
            StatisticsItem item = (StatisticsItem) itm;
            if (item.isLegal()) {
                StatisticsItem itemInMemory = findStatisticsItemInMemory(item.getAc(), item.getActionKey(), item.getDate(), item.getTimeStamp());
                if (itemInMemory != null) {
                    itemInMemory.setValue(itemInMemory.getValue() + item.getValue());
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected boolean isNeedTimedUpload() {
        return true;
    }
}
