package com.zq.lyrics.model;

import com.common.utils.U;

import java.io.File;

/**
 * 歌曲的资源，任何一种歌曲的文件，有可能是歌词，midi，mp3伴奏，mp3原唱
 */
public class SongRes {
    //存储的名字
    public String outputFileName;
    //下载地址
    public String downloadUrl;
    //存放的目录
    public String fileDir;
    //后缀，可有可无
    public String suff;


    public long length;

    public SongRes(String downloadUrl, String fileDir) {
        this.downloadUrl = downloadUrl;
        this.outputFileName = U.getMD5Utils().MD5_16(downloadUrl);
        this.fileDir = fileDir;
    }

    public void setSuff(String suff) {
        this.suff = suff;
    }

    public String getResUrl() {
        return downloadUrl;
    }

    public boolean isExist() {
        try {
            File file = new File(fileDir + File.separator + outputFileName);
            if(file.exists()){
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public void flush(){

    }
}
