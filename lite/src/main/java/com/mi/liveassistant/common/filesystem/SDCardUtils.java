package com.mi.liveassistant.common.filesystem;

import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;

import com.mi.liveassistant.config.Constants;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

public class SDCardUtils {
    public static final String MAIN_DIR_PREFIX = "Xiaomi";
    public static final String MAIN_DIR_NAME = MAIN_DIR_PREFIX + "/" + Constants.APP_NAME;
    public static final String MAIN_DIR_PATH = "/" + MAIN_DIR_NAME;

    public static final String IMAGE_DIR_NAME = "image";
    public static final String IMAGE_DIR_PATH = MAIN_DIR_PATH + "/" + IMAGE_DIR_NAME;

    public static final String CACHE_DIR_NAME = "cache";
    public static final String CACHE_DIR_PATH = MAIN_DIR_PATH + "/" + CACHE_DIR_NAME;

    public static final String FRESCO_DIR_NAME = "fresco";
    public static final String FRESCO_DIR_PATH = MAIN_DIR_PATH + "/" + FRESCO_DIR_NAME;

    public static final String KSY_DIR_NAME = "ksy";
    public static final String KSY_DIR_PATH = MAIN_DIR_PATH + "/" + KSY_DIR_NAME;

    public static final String KSY_LOG_DIR_NAME = "ksyLog";
    public static final String KSY_LOG_DIR_PATH = MAIN_DIR_PATH + "/" + KSY_LOG_DIR_NAME;

    public static final String ADVERTISE_DIR_NAME = "advertiseimages";
    public static final String ADVERTISE_IMAGES_DIR = MAIN_DIR_PATH + "/" + ADVERTISE_DIR_NAME;

    public static final String VIDEO_DIR_NAME = "live/video";

    // 最大保留10个文件,从20开始清理
    public static final int MAX_REMAIN = 40;
    public static final int MAX_THRESHOLD = 50;

    public static boolean generateDirectory() {
        if (SDCardUtils.isSDCardBusy()) {
            return false;
        }

        // 创建对应的文件夹
        File imageDir = new File(Environment.getExternalStorageDirectory(), SDCardUtils.IMAGE_DIR_PATH);
        if (!imageDir.exists()) {
            imageDir.mkdirs();
        }

        File cacheDir = new File(Environment.getExternalStorageDirectory(), SDCardUtils.CACHE_DIR_PATH);
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }

        File frescoDir = new File(Environment.getExternalStorageDirectory(), SDCardUtils.FRESCO_DIR_PATH);
        if (!frescoDir.exists()) {
            frescoDir.mkdirs();
        }

        File ksyDir = new File(Environment.getExternalStorageDirectory(), SDCardUtils.KSY_DIR_PATH);
        if (!ksyDir.exists()) {
            ksyDir.mkdirs();
        }

        File ksyLogDir = new File(Environment.getExternalStorageDirectory(), SDCardUtils.KSY_LOG_DIR_PATH);
        if (!ksyLogDir.exists()) {
            ksyLogDir.mkdirs();
        }

        File advertiseDir = new File(Environment.getExternalStorageDirectory(), SDCardUtils.ADVERTISE_IMAGES_DIR);
        if (!advertiseDir.exists()) {
            advertiseDir.mkdirs();
        }

        //防止媒体搜索
        File mainDir = new File(Environment.getExternalStorageDirectory(), SDCardUtils.MAIN_DIR_NAME);
        IOUtils.hideFromMediaScanner(mainDir);
        IOUtils.hideFromMediaScanner(imageDir);

        return true;
    }

    /**
     * 返回金山保存的路径
     */
    public static String getKsyPath() {
        return Environment.getExternalStorageDirectory().getPath() + "/" + KSY_DIR_PATH;
    }

    /**
     * 清理下ksy录制视频的文件夹
     */
    public static void clearKsyDumpFile() {
        clearFile(getKsyPath(), MAX_THRESHOLD, MAX_REMAIN);
    }

    /**
     * 返回金山保存的路径
     */
    public static String getKsyLogPath() {
        return Environment.getExternalStorageDirectory().getPath() + "/" + KSY_LOG_DIR_PATH;
    }

    /**
     * 清理下ksy日志的文件夹
     */
    public static void clearKsyLogFile() {
        clearFile(getKsyLogPath(), MAX_THRESHOLD, MAX_REMAIN);
    }

    private static void clearFile(final String dirPath, final int threshold, final int remain) {
        File dir = new File(dirPath);
        if (!dir.isDirectory()) {
            return;
        }
        File[] files = dir.listFiles();
        if (files == null || files.length <= threshold) {
            return;
        }
        getSortFiles(files);
        if (files != null && files.length > threshold) {
            for (int i = 0; i < files.length - remain; i++) {
                files[i].delete();
            }
        }
    }

    /**
     * 按时间排序文件
     */
    public static void getSortFiles(File[] files) {
        if (files != null && files.length > 0) {
            Arrays.sort(files, new Comparator<File>() {
                @Override
                public int compare(File file, File newFile) {
                    if (file.lastModified() < newFile.lastModified()) {
                        return -1;
                    } else if (file.lastModified() == newFile.lastModified()) {
                        return 0;
                    } else {
                        return 1;
                    }
                }
            });
        }
    }

    /**
     * 没有检测到SD卡
     */
    public static boolean isSDCardUnavailable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_REMOVED);
    }

    /**
     * @return true 如果SD卡处于不可读写的状态
     */
    public static boolean isSDCardBusy() {
        return !Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * 检查SD卡是否已满。如果SD卡的剩余空间小于１００ｋ，则认为SD卡已满。
     */
    public static boolean isSDCardFull() {
        return getSDCardAvailableBytes() <= (100 * 1024);
    }

    public static boolean isSDCardUseful() {
        return (!isSDCardBusy()) && (!isSDCardFull()) && (!isSDCardUnavailable());
    }

    /**
     * 获取SD卡的剩余字节数。
     */
    public static long getSDCardAvailableBytes() {
        if (isSDCardBusy()) {
            return 0;
        }

        final File path = Environment.getExternalStorageDirectory();
        if (path == null || TextUtils.isEmpty(path.getPath())) {
            return 0;
        }

        final StatFs stat = new StatFs(path.getPath());
        final long blockSize = stat.getBlockSize();
        final long availableBlocks = stat.getAvailableBlocks();
        return blockSize * (availableBlocks - 4);
    }
}

