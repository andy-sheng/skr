package com.zq.lyrics.utils;

import android.text.TextUtils;

import com.common.utils.U;

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

    //存储目录
    private static final String LYRIC_DIR = U.getAppInfoUtils().getMainDir() + File.separator + "lyrics";
    private static final String ACC_DIR = U.getAppInfoUtils().getMainDir() + File.separator + "acc";
    private static final String ORI_DIR = U.getAppInfoUtils().getMainDir() + File.separator + "ori";
    private static final String MIDI_DIR = U.getAppInfoUtils().getMainDir() + File.separator + "midi";
    private static final String SCORE_DIR = U.getAppInfoUtils().getMainDir() + File.separator + "score";
    private static final String STAND_DIR = U.getAppInfoUtils().getMainDir() + File.separator + "stand";
    private static final String GRAB_LYRIC_DIR = U.getAppInfoUtils().getMainDir() + File.separator + "grabLyric";

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

    public static final String getScoreDir(){
        return SCORE_DIR;
    }

    public static final String getStandDir(){
        return STAND_DIR;
    }

    public static final String getGrabLyricDir(){
        return GRAB_LYRIC_DIR;
    }

    public static final File getLyricFileByUrl(String resUrl) {

        return getFile(LYRIC_DIR, resUrl, U.getFileUtils().getSuffixFromUrl(resUrl, SUFF_ZRCE));
    }

    public static final File getAccFileByUrl(String resUrl) {

        return getFile(ACC_DIR, resUrl, U.getFileUtils().getSuffixFromUrl(resUrl, SUFF_ACC));
    }

    public static final File getORIFileByUrl(String resUrl) {

        return getFile(ORI_DIR, resUrl, U.getFileUtils().getSuffixFromUrl(resUrl, SUFF_ORI));
    }

    public static final File getMIDIFileByUrl(String resUrl) {

        return getFile(MIDI_DIR, resUrl, U.getFileUtils().getSuffixFromUrl(resUrl, SUFF_MIDI));
    }

    public static final File getScoreFileByUrl(String resUrl) {

        return getFile(SCORE_DIR, resUrl, U.getFileUtils().getSuffixFromUrl(resUrl, SUFF_JSON));
    }

    public static final File getStandFileByUrl(String resUrl) {

        return getFile(STAND_DIR, resUrl, U.getFileUtils().getSuffixFromUrl(resUrl, SUFF_STAND));
    }

    public static final File getGrabLyricFileByUrl(String resUrl) {

        return getFile(GRAB_LYRIC_DIR, resUrl, U.getFileUtils().getSuffixFromUrl(resUrl, SUFF_TXT));
    }

    private static File getFile(String dir, String url, String suff) {
        File file = new File(dir + File.separator + getFileNameWithMD5(url) + "." + suff);

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

    public static String createTempLyricFileName(String url) {
        return SongResUtils.getLyricDir() + File.separator + SongResUtils.getFileNameWithMD5(url) + "temp" + "." + U.getFileUtils().getSuffixFromUrl(url, SUFF_ZRCE);
    }

    public static String createStandLyricTempFileName(String url){
        return SongResUtils.getGrabLyricDir() + File.separator + SongResUtils.getFileNameWithMD5(url) + "temp" + "." + SUFF_TXT;
    }

    public static String createStandLyricFileName(String url){
        return SongResUtils.getGrabLyricDir() + File.separator + SongResUtils.getFileNameWithMD5(url) + "." + SUFF_TXT;
    }
}
