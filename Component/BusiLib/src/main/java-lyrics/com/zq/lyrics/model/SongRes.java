package com.zq.lyrics.model;

import java.io.File;

import io.reactivex.Observable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Predicate;

public class SongRes {
    //包含所有文件的文件夹的绝对路径
    private String parentFileAbselutePath;

    private long songResId;

    private String songName;

    private String songDuratuin;

    private String resUrl;

    //伴奏
    private String mp3Path;

    private String lyricPath;

    //原唱
    private String singingPath;

    private String midPath;

    private String hashcode;

    private boolean isPrepared = false;

    public SongRes(long songResId, String songName, String resUrl){
        this.songResId = songResId;
        this.songName = songName;
        this.resUrl = resUrl;
    }

    public long getSongResId() {
        return songResId;
    }

    public String getResUrl() {
        return resUrl;
    }

    public boolean isPrepared() {
        return isPrepared;
    }

    public void parseResFile(final File resFile){
        //解析所有的的资源
        if(resFile != null && resFile.exists() && resFile.isDirectory()){
            Observable.fromArray(resFile.list())
                    .filter(new Predicate<String>() {
                @Override
                public boolean test(String fileName) throws Exception {
                    return fileName.endsWith("txt") || fileName.endsWith("mp3") || fileName.endsWith("mid");
                }
            })
                    .subscribe(new Consumer<String>() {
                        @Override
                        public void accept(String s) throws Exception {

                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {

                        }
                    }, new Action() {
                        @Override
                        public void run() throws Exception {
                            parentFileAbselutePath = resFile.getAbsolutePath();
                            isPrepared = true;
                        }
                    });
        }
    }

    public String getMp3Path() {
        if(isPrepared){
            return parentFileAbselutePath + File.separator + mp3Path;
        }
        return "";
    }

    public String getLyricPath() {
        if(isPrepared){
            return parentFileAbselutePath + File.separator + lyricPath;
        }
        return "";
    }

    public String getSingingPath() {
        if(isPrepared){
            return parentFileAbselutePath + File.separator + singingPath;
        }
        return "";
    }

    public String getMidPath() {
        if(isPrepared){
            return parentFileAbselutePath + File.separator + midPath;
        }
        return "";
    }
}
