package com.mi.live.data.report.keyflow;

import android.os.Environment;
import android.text.TextUtils;

import com.base.log.MyLog;
import com.base.thread.ThreadPool;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @module 打点
 * <p>
 * 推拉流关键流程打点上报
 * http://wiki.n.miui.com/pages/viewpage.action?pageId=25039885
 * http://wiki.n.miui.com/pages/viewpage.action?pageId=25037480
 * <p>
 * Created by yangli on 16-7-19.
 */
public enum KeyFlowReportManager {

    INSTANCE;

    private static final String TAG = "KeyFlowReportManager";

    public static final String TYPE_LIVE = "live";
    public static final String TYPE_WATCH = "watch";
    public static final String TYPE_REPLAY = "replay";

    private static final String REPORT_DIR_ROOT = "/Xiaomi/WALI_LIVE/Report";
    private static final String LOG_FILE_DIR = "KeyFlowReport";
    private static final String SPLITTER = " ";

    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor(
            new ThreadPool.NamedThreadFactory("KeyFlowReportManager"));

    private String mLogFilePath;
    private String mStorageKey = "";
    private File mStorageFile;
    private FileWriter mFileWriter;
    private StringBuilder mInfoCache = new StringBuilder("");

    KeyFlowReportManager() {
        mLogFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() +
                REPORT_DIR_ROOT + "/" + LOG_FILE_DIR;
        File file = new File(mLogFilePath);
        if (!file.exists()) {
            file.mkdirs();
        }
        MyLog.w(TAG, "KeyFlowReportManager, mLogFilePath=" + mLogFilePath);
    }

    private void createFlowStorage(String storageKey) {
        if (mStorageKey.equals(storageKey)) {
            MyLog.e(TAG, "createFlowStorage, but storageKey is already in use");
            return;
        }
        MyLog.w(TAG, "createFlowStorage, storageKey=" + storageKey);
        mStorageKey = storageKey;
        mStorageFile = new File(mLogFilePath + "/" + mStorageKey);
        closeFileWriter();
        try {
            mFileWriter = new FileWriter(mStorageFile, true);
            mFileWriter.write(mInfoCache.toString());
            mFileWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mInfoCache.delete(0, mInfoCache.length());
    }

    /**
     * 直播页需要考虑重入，所以使用房间号创建日志文件，并加入.temp后缀标记该日志文件是否需要上传
     * 当直播结束(或用户取消继续上次异常结束的直播)时，将.temp后缀删除，表示日志写入过程结束
     */
    public void createFlowStorageWithRoomId(final String roomId, final String type) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (!TextUtils.isEmpty(roomId)) {
                    createFlowStorage("room_" + roomId + "." + type + ".temp");
                } else {
                    MyLog.e(TAG, "createFlowStorageWithRoomId, but input is illegal");
                    return;
                }
            }
        });
    }

    /**
     * 观看页和回放页不需要考虑重入，所以使用传入的storageKey创建日志文件
     * 由观看页和回放页保证storageKey的唯一性
     */
    public void createFlowStorageWithKey(final String storageKey, final String type) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                createFlowStorage("key_" + storageKey + "." + type);
            }
        });
    }

    public void flushFlowStorage(final String type) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                MyLog.w(TAG, "flushFlowStorage");
                if (mFileWriter == null) {
                    createFlowStorage("time_" + System.currentTimeMillis() + "." + type);
                }
            }
        });
    }

    private void renameFlowStorage(String storageKey) {
        if (!TextUtils.isEmpty(storageKey) && storageKey.endsWith(".temp")) {
            File storageFile = new File(mLogFilePath + "/" + storageKey);
            File targetFile = new File(mLogFilePath + "/" + storageKey.substring(0, storageKey.length() - 5));
            boolean ret = storageFile.renameTo(targetFile);
            MyLog.w(TAG, "renameFlowStorage rename " + storageFile.getName() + " to " + targetFile.getName() + " " + ret);
        }
    }

    private void closeFileWriter() {
        if (mFileWriter != null) {
            try {
                mFileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                mFileWriter = null;
            }
        }
    }

    public void closeCurrFlowStorage() {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                MyLog.w(TAG, "closeCurrFlowStorage");
                closeFileWriter();
                renameFlowStorage(mStorageKey);
                mStorageKey = "";
                mStorageFile = null;
                mInfoCache.delete(0, mInfoCache.length());
            }
        });
    }

    /**
     * 直播异常结束，用户选择不继续直播时，使用该接口重命名上次直播的日志文件
     */
    public void closeFlowStorage(final String roomId, final String type) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                renameFlowStorage("room_" + roomId + "." + type + "temp");
            }
        });
    }

    private void reportAllToServer(boolean ignoreTemp) {
        MyLog.w(TAG, "reportAllToServer ignoreTemp=" + ignoreTemp);
        File localPath = new File(mLogFilePath);
        File[] logFiles = localPath.listFiles();
        if (logFiles != null) {
            for (File logFile : logFiles) {
                if (ignoreTemp && logFile.getName().endsWith("temp")) {
                    continue;
                }
                if (new KeyFlowReporter().reportFile(logFile)) {
                    boolean ret = logFile.delete(); // delete file if upload done success
                    MyLog.w(TAG, "reportAllToServer delete " + logFile.getName() + " " + ret);
                } else {
                    MyLog.e(TAG, "reportAllToServer failed, file=" + logFile.getName());
                }
            }
        }
    }

    public void reportCurrToServer() {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                reportAllToServer(false);
            }
        });
    }

    public void reportAllToServer() {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                reportAllToServer(true);
            }
        });
    }

    private void writeContent(String content) {
        if (mFileWriter != null) {
            try {
                mFileWriter.write(content + "\n");
                mFileWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            mInfoCache.append(content + "\n");
        }
    }

    public void insertId(final long userId) {
        if (userId <= 0) {
            MyLog.e(TAG, "insertId, but input is illegal");
            return;
        }
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                writeContent(KeyFlowProtocol.KEY_ID + SPLITTER + userId);
            }
        });
    }

    public void insertAnchorId(final long userId) {
        if (userId <= 0) {
            MyLog.e(TAG, "insertAnchorId, but input is illegal");
            return;
        }
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                writeContent(KeyFlowProtocol.KEY_ANCHOR_ID + SPLITTER + userId);
            }
        });
    }

    public void insertUrl(final String url) {
        if (TextUtils.isEmpty(url)) {
            MyLog.e(TAG, "insertUrl, but input is illegal");
            return;
        }
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                writeContent(KeyFlowProtocol.KEY_URL + SPLITTER + url);
            }
        });
    }

    public void insertBegin(final long timeStamp) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                MyLog.w(TAG, KeyFlowProtocol.KEY_BEGIN_TIME + SPLITTER + timeStamp);

                writeContent(KeyFlowProtocol.KEY_BEGIN_TIME + SPLITTER + timeStamp);
            }
        });
    }

    public void insertEnd(final long timeStamp) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                MyLog.w(TAG, KeyFlowProtocol.KEY_END_TIME + SPLITTER + timeStamp);
                writeContent(KeyFlowProtocol.KEY_END_TIME + SPLITTER + timeStamp);
            }
        });
    }

    public void insertCreateRoom(final String roomId, final long timeStamp) {
        if (TextUtils.isEmpty(roomId)) {
            MyLog.e(TAG, "insertCreateRoom, but input is illegal");
            return;
        }
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                MyLog.w(TAG, KeyFlowProtocol.KEY_CREATE_ROOM + SPLITTER + timeStamp + SPLITTER + roomId);
                writeContent(KeyFlowProtocol.KEY_CREATE_ROOM + SPLITTER + timeStamp + SPLITTER + roomId);
            }
        });
    }

    public void insertEngineInit(final long timeStamp) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                MyLog.w(TAG, KeyFlowProtocol.KEY_ENGINE_INIT + SPLITTER + timeStamp);
                writeContent(KeyFlowProtocol.KEY_ENGINE_INIT + SPLITTER + timeStamp);
            }
        });
    }

    public void insertDnsParse(final long timeStamp) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                MyLog.w(TAG, KeyFlowProtocol.KEY_DNS_PARSE + SPLITTER + timeStamp);
                writeContent(KeyFlowProtocol.KEY_DNS_PARSE + SPLITTER + timeStamp);
            }
        });
    }

    public void insertErrorInfo(final int errno, final String errMsg) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                writeContent(KeyFlowProtocol.KEY_ERRNO + SPLITTER + errno + SPLITTER + errMsg);
            }
        });
    }

    public void insertIp(final String ip, final int flag, final long timeStamp) {
        if (TextUtils.isEmpty(ip)) {
            MyLog.e(TAG, "insertIp, but input is illegal");
            return;
        }
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                writeContent(KeyFlowProtocol.KEY_IP + SPLITTER + ip + SPLITTER + flag + SPLITTER + timeStamp);
            }
        });
    }

    public void insertStutterBegin(final long timeStamp) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                writeContent(KeyFlowProtocol.KEY_STUTTER_BEGIN + SPLITTER + timeStamp);
            }
        });
    }

    public void insertStutterEnd(final long timeStamp) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                writeContent(KeyFlowProtocol.KEY_STUTTER_END + SPLITTER + timeStamp);
            }
        });
    }

    public void insertStatus(final int status) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                writeContent(KeyFlowProtocol.KEY_STATUS + SPLITTER + status);
            }
        });
    }

    // public void insertIpBegin(long timeStamp) {
    //     mExecutor.execute(() -> writeContent(KeyFlowProtocol.KEY_IP_BEGIN + SPLITTER + timeStamp));
    // }

    // public void insertIpEnd(long timeStamp) {
    //     mExecutor.execute(() -> writeContent(KeyFlowProtocol.KEY_IP_END + SPLITTER + timeStamp));
    // }

}
