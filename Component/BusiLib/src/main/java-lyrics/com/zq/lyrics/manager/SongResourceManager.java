package com.zq.lyrics.manager;

import android.text.TextUtils;

import com.common.log.MyLog;
import com.common.utils.HttpUtils;
import com.common.utils.U;
import com.zq.lyrics.model.SongRes;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;

public class SongResourceManager {
    public static final String TAG = "SongResourceManager";
    private ConcurrentHashMap<Long, SongRes> songReslist = new ConcurrentHashMap<>();

    public static class SongResourceManagerHolder{
        public static SongResourceManager INSTANCE = new SongResourceManager();
    }

    private SongResourceManager(){

    }

    public static SongResourceManager getInstance(){
        return SongResourceManagerHolder.INSTANCE;
    }

    public boolean checkSongResPrepare(long songResId){
        SongRes songRes = songReslist.get(songResId);
        if(songRes != null){
            return songRes.isPrepared();
        }
        return false;
    }

    //下载歌曲资源，包括歌词，mid,mp3文件
    private void downloadRes(final SongRes songRes, final HttpUtils.OnDownloadProgress onDownloadProgress){
        if(songRes == null || TextUtils.isEmpty(songRes.getResUrl())){
            MyLog.w(TAG, "downloadRes songres is error");
            return;
        }

        String outputFilePath = U.getMD5Utils().MD5_16(songRes.getResUrl() + songRes.getSongResId());
        outputFilePath = U.getAppInfoUtils().getMainDir() + File.separator + outputFilePath;
        final File outputFile = new File(outputFilePath);
        if(outputFile.exists()){
            outputFile.delete();
        }
        outputFile.mkdirs();

        U.getHttpUtils().downloadFile(songRes.getResUrl(), outputFile, new HttpUtils.OnDownloadProgress() {
            @Override
            public void onDownloaded(long downloaded, long totalLength) {
                if(onDownloadProgress != null){
                    onDownloadProgress.onDownloaded(downloaded, totalLength);
                }
            }

            @Override
            public void onCompleted(String localPath) {
                if(onDownloadProgress != null){
                    onDownloadProgress.onCompleted(localPath);
                }

                songRes.parseResFile(outputFile);
            }

            @Override
            public void onCanceled() {
                if(onDownloadProgress != null){
                    onDownloadProgress.onCanceled();
                }
            }

            @Override
            public void onFailed() {
                if(onDownloadProgress != null){
                    onDownloadProgress.onFailed();
                }

                Observable.timer(3, TimeUnit.SECONDS).subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        downloadRes(songRes, null);
                    }
                });
            }
        });
    }

    public SongRes getSongResById(long songResId){
        return songReslist.get(songResId);
    }
}
