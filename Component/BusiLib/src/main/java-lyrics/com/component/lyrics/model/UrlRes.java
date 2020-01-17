package com.component.lyrics.model;

import android.text.TextUtils;

import com.common.utils.U;

import java.io.File;

/**
 * 歌曲的资源，任何一种歌曲的文件，有可能是歌词，midi，mp3伴奏，mp3原唱
 */
public class UrlRes {
    //下载地址
    public String downloadUrl;
    //存放的目录
    public File outFile;

    public long length;

    public UrlRes(String downloadUrl, File outFile) {
        this.downloadUrl = downloadUrl;
        this.outFile = outFile;
    }


    public String getResUrl() {
        return downloadUrl;
    }

    public boolean isExist() {
        if (outFile!=null && outFile.exists()) {
            return true;
        }
        return false;
    }

}
