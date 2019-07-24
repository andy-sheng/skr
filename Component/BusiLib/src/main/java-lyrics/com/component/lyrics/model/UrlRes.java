package com.component.lyrics.model;

import android.text.TextUtils;

import com.common.utils.U;

import java.io.File;

/**
 * 歌曲的资源，任何一种歌曲的文件，有可能是歌词，midi，mp3伴奏，mp3原唱
 */
public class UrlRes {
    //存储的名字
    public String outputFileName;
    //下载地址
    public String downloadUrl;
    //存放的目录
    public String fileDir;
    //后缀，可有可无
    public String suff;


    public long length;

    public UrlRes(String downloadUrl, String fileDir, String suff) {
        if(TextUtils.isEmpty(downloadUrl) || TextUtils.isEmpty(fileDir) || TextUtils.isEmpty(suff)){
            throw new IllegalStateException("songres param is error");
        }

        this.downloadUrl = downloadUrl;
        this.outputFileName = U.getMD5Utils().MD5_16(downloadUrl);
        this.fileDir = fileDir;
        this.suff = suff;
    }

    public void setSuff(String suff) {
        this.suff = suff;
    }

    public String getResUrl() {
        return downloadUrl;
    }

    public boolean isExist() {
        try {
            File file = new File(getAbsolutPath());
            if(file.exists()){
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public String getAbsolutPath(){
        return fileDir + File.separator + outputFileName + "." + suff;

    }
    public void flush(){

    }
}
