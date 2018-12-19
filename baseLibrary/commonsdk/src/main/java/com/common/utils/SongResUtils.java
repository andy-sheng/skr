package com.common.utils;

import android.text.TextUtils;

import java.io.File;

public class SongResUtils {
    //后缀。后缀可以有很多
    public static String SUFF_ZRCE = "zrce";
    public static String SUFF_ACC = "mp3";
    public static String SUFF_ORI = "mp3";
    public static String SUFF_MIDI = "mid";

    //存储目录
    private static final String LYRIC_DIR = U.getAppInfoUtils().getMainDir() + File.separator + "lyrics";
    private static final String ACC_DIR = U.getAppInfoUtils().getMainDir() + File.separator + "acc";
    private static final String ORI_DIR = U.getAppInfoUtils().getMainDir() + File.separator + "ori";
    private static final String MIDI_DIR = U.getAppInfoUtils().getMainDir() + File.separator + "midi";

    public static final String getLyricDir() {
        return LYRIC_DIR;
    }

    public static final String getACCDir() {
        return ACC_DIR;
    }

    public static final String getORIDir() {
        return ORI_DIR;
    }

    public static final String getMIDIDir() {
        return MIDI_DIR;
    }

    public static final File getZRCELyricFileByUrl(String resUrl) {

        return getFile(LYRIC_DIR, resUrl, SUFF_ZRCE);
    }

    public static final File getAccFileByUrl(String resUrl) {

        return getFile(ACC_DIR, resUrl, SUFF_ACC);
    }

    public static final File getORIFileByUrl(String resUrl) {

        return getFile(ORI_DIR, resUrl, SUFF_ORI);
    }

    public static final File getMIDIFileByUrl(String resUrl) {

        return getFile(MIDI_DIR, resUrl, SUFF_MIDI);
    }

    private static File getFile(String dir, String url, String suff){
        File file = new File(dir + File.separator + getFileNameWithMD5(url) + "." + suff);

        if(file.exists()){
            return file;
        }

        return null;
    }

    /**
     * 跟去url生成文件名字，不带后缀
     * @param origen
     * @return
     */
    public static String getFileNameWithMD5(String origen){
        if(TextUtils.isEmpty(origen)){
            return "";
        }

        return U.getMD5Utils().MD5_16(origen);
    }

    public static String createLyricFileName(String url){
        return SongResUtils.getLyricDir() + File.separator + SongResUtils.getFileNameWithMD5(url) + "." + SUFF_ZRCE;
    }

    public static String createTempLyricFileName(String url){
        return SongResUtils.getLyricDir() + File.separator + SongResUtils.getFileNameWithMD5(url) + "temp" + "." + SUFF_ZRCE;
    }
}
