package com.component.lyrics.utils;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.common.log.MyLog;
import com.common.utils.U;
import com.common.videocache.MediaCacheManager;

import java.io.File;

public class SongResUtils {
    //后缀。后缀可以有很多
    public static final String SUFF_ZRCE = "zrce";
    public static final String SUFF_ACC = "mp3";
    public static final String SUFF_ORI = "mp3";
    public static final String SUFF_STAND = "mp3";
    public static final String SUFF_MIDI = "mid";
    public static final String SUFF_JSON = "json";
    public static final String SUFF_TXT = "txt";

    private static String rootFilePath;

    //存储目录
    static {
        getRootFile();
    }

    public static final String getRootFile() {
        if (TextUtils.isEmpty(rootFilePath)) {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                boolean hasPermission = U.getPermissionUtils().checkExternalStorage(U.getActivityUtils().getTopActivity());
                if (hasPermission) {
                    rootFilePath = U.getAppInfoUtils().getMainDir().getPath();
                }
            } else {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        if (TextUtils.isEmpty(rootFilePath)) {
                            boolean hasPermission = U.getPermissionUtils().checkExternalStorage(U.getActivityUtils().getTopActivity());
                            if (hasPermission) {
                                rootFilePath = U.getAppInfoUtils().getMainDir().getPath();
                            }
                        }
                    }
                });
            }
        }

        if (TextUtils.isEmpty(rootFilePath)) {
            return U.app().getFilesDir().getPath();
        } else {
            return rootFilePath;
        }
    }


    public static final String getLyricDir() {
        return getRootFile() + File.separator + "lyrics";
    }

    public static final String getACCDir() {
        return getRootFile() + File.separator + "media_cache";
    }

    public static final String getORIDir() {
        return getRootFile() + File.separator + "ori";
    }

    public static final String getMIDIDir() {
        return getRootFile() + File.separator + "midi";
    }

    public static final String getScoreDir() {
        return getRootFile() + File.separator + "score";
    }

    public static final String getStandDir() {
        return getRootFile() + File.separator + "stand";
    }

    public static final String getGrabLyricDir() {
        return getRootFile() + File.separator + "grabLyric";
    }

    public static final File getLyricFileByUrl(String resUrl) {

        return getFile(getLyricDir(), resUrl, U.getFileUtils().getSuffixFromUrl(resUrl, SUFF_ZRCE));
    }

    public static final File getAccFileByUrl(String resUrl) {

        return getFile(getACCDir(), resUrl, U.getFileUtils().getSuffixFromUrl(resUrl, SUFF_ACC));
    }

    public static final File getORIFileByUrl(String resUrl) {

        return getFile(getORIDir(), resUrl, U.getFileUtils().getSuffixFromUrl(resUrl, SUFF_ORI));
    }

    public static final File getMIDIFileByUrl(String resUrl) {

        return getFile(getMIDIDir(), resUrl, U.getFileUtils().getSuffixFromUrl(resUrl, SUFF_MIDI));
    }

    public static final File getScoreFileByUrl(String resUrl) {

        return getFile(getScoreDir(), resUrl, U.getFileUtils().getSuffixFromUrl(resUrl, SUFF_JSON));
    }

    public static final File getStandFileByUrl(String resUrl) {

        return getFile(getStandDir(), resUrl, U.getFileUtils().getSuffixFromUrl(resUrl, SUFF_STAND));
    }

    public static final File getGrabLyricFileByUrl(String resUrl) {

        return getFile(getGrabLyricDir(), resUrl, U.getFileUtils().getSuffixFromUrl(resUrl, SUFF_TXT));
    }

    private static File getFile(String dir, String url, String suff) {
        String url2 = MediaCacheManager.INSTANCE.getOriginCdnUrl(url);
//        MyLog.i("SongResUtils", "dir=" + dir);
        File file = new File(dir + File.separator + getFileNameWithMD5(url2) + "." + suff);
        if (file.exists()) {
            return file;
        }
        return file;
    }

    /**
     * 跟去url生成文件名字，不带后缀
     *
     * @param origen
     * @return
     */
    public static String getFileNameWithMD5(String origen) {
        if (TextUtils.isEmpty(origen)) {
            return "";
        }

        return U.getMD5Utils().MD5_16(origen);
    }

    public static String createLyricFileName(String url) {
        return SongResUtils.getLyricDir() + File.separator + SongResUtils.getFileNameWithMD5(url) + "." + U.getFileUtils().getSuffixFromUrl(url, SUFF_ZRCE);
    }

//    public static String createTempLyricFileName(String url) {
//        return SongResUtils.getLyricDir() + File.separator + SongResUtils.getFileNameWithMD5(url) + "temp" + "." + U.getFileUtils().getSuffixFromUrl(url, SUFF_ZRCE);
//    }

//    public static String createStandLyricTempFileName(String url) {
//        return SongResUtils.getGrabLyricDir() + File.separator + SongResUtils.getFileNameWithMD5(url) + "temp" + "." + SUFF_TXT;
//    }

    public static String createStandLyricFileName(String url) {
        return SongResUtils.getGrabLyricDir() + File.separator + SongResUtils.getFileNameWithMD5(url) + "." + U.getFileUtils().getSuffixFromUrl(url, SUFF_TXT);
    }
}
