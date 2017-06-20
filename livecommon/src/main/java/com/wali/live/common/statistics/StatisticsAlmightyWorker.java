package com.wali.live.common.statistics;

import android.os.Environment;

import com.base.log.MyLog;
import com.wali.live.common.statistics.pojo.BaseStatisticsItem;
import com.wali.live.common.statistics.pojo.StatisticsAlmightyItem;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by yurui on 8/4/16.
 */
public class StatisticsAlmightyWorker extends BaseStatisticsWorker {
    public static final String TAG = StatisticsAlmightyWorker.class.getSimpleName();
    public static final boolean ENABLE = true;

    private final static String DIARY_FILE_DIR = Environment.getExternalStorageDirectory() + "/Xiaomi/WALI_LIVE/statisticsWorker";
    private final static String DIARY_FILE_NAME = StatisticsAlmightyWorker.class.getSimpleName();
    private final static String DIARY_FILE_PATH = DIARY_FILE_DIR + File.separator + DIARY_FILE_NAME;

    public static final String KEY_KEY = "key";
    public static final String KEY_DATE = "date";
    public static final String KEY_TIMES = "times";

    private static StatisticsAlmightyWorker sInstance = null;

    private StatisticsAlmightyWorker() {
        super();
    }

    public synchronized static StatisticsAlmightyWorker getsInstance() {
        if (null == sInstance) {
            sInstance = new StatisticsAlmightyWorker();
        }
        return sInstance;
    }

    /**
     * @param acKey
     * @param extParams 打点规范，如需要打 观看视频的时间长度
     *                  key:"watch_times"
     *                  time:"20"
     *                  <p>
     *                  则需要调用 recordDelay("ml_app","key","watch_times","time","20");
     */
    //定时上报
    public void recordDelay(final String acKey, final String... extParams) {
        if (ENABLE) {
            //如果不post mTS可能为0
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    StatisticsAlmightyItem item = new StatisticsAlmightyItem(acKey, getDateYYYYMMDD(), System.currentTimeMillis(), extParams);
                    MyLog.v(TAG + " recordDelay: " + item.toString());
                    if (item.isLegal()) {
                        recordOnMainThreadDelayUpload(item);
                    }
                }
            });
        }
    }

    /**
     * 定时上报简化接口
     */
    public void recordDelayDefault(String key, long times) {
        recordDelay(StatisticsAlmightyWorker.AC_APP, KEY_KEY, key, KEY_TIMES, String.valueOf(times), KEY_DATE, getDateYYYYMMDD());
    }

    //实时上报
    public void recordImmediately(final String acKey, final String... extParams) {
        if (ENABLE) {
            //如果不post mTS可能为0
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    StatisticsAlmightyItem item = new StatisticsAlmightyItem(acKey, getDateYYYYMMDD(), System.currentTimeMillis(), extParams);
                    if (item.isLegal()) {
                        recordOnMainThreadImmediatelyUpload(item);
                    }
                }
            });
        }
    }

    /**
     * 实时上报简化接口
     */
    public void recordImmediatelyDefault(String key, long times) {
        recordImmediately(StatisticsAlmightyWorker.AC_APP, KEY_KEY, key, KEY_TIMES, String.valueOf(times), KEY_DATE, getDateYYYYMMDD());
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
                        StatisticsAlmightyItem item = new StatisticsAlmightyItem(line);
                        if (item.isLegal()) {
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
    protected void writeToFile(BaseStatisticsItem baseItem) throws IOException {
        if (baseItem instanceof StatisticsAlmightyItem) {
            StatisticsAlmightyItem item = (StatisticsAlmightyItem) baseItem;
            FileWriter fw = null;
            BufferedWriter bw = null;
            try {
                fw = new FileWriter(getDiaryFile(), true);
                bw = new BufferedWriter(fw);
                JSONObject jsonObject = item.toJSONObjectLocal();
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
        } else {
            super.writeToFile(baseItem);
        }
    }

    @Override
    protected boolean needUpdateInMemory(BaseStatisticsItem itm) {
        return false;
    }

    @Override
    protected boolean isNeedTimedUpload() {
        return true;
    }
}
