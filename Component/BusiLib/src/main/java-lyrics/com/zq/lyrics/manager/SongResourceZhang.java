package com.zq.lyrics.manager;

import com.common.log.MyLog;
import com.common.utils.HttpUtils;
import com.common.utils.U;
import com.zq.lyrics.model.SongRes;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;

/**
 * 由于多个文件在一个地方被用到，但是又有可能分开用，所以无法使用一个zip包解决
 */
public class SongResourceZhang {
    public static final String TAG = "SongResourceZhang";

    List<SongRes> taskQueue;

    volatile SongRes currentTask;

    HttpUtils.OnDownloadProgress onDownloadProgress;

    //所有url对应的资源文件的大小
    volatile long totalLength = 0;
    volatile long downloadLength = 0;

    public SongResourceZhang(List<SongRes> songResArrayList, HttpUtils.OnDownloadProgress onDownloadProgress){
        this.taskQueue = new LinkedList<>(songResArrayList);
        this.onDownloadProgress = onDownloadProgress;
    }

    /**
     * 开始下载
     */
    public void go(){
        if(taskQueue == null){
            if(onDownloadProgress != null){
                onDownloadProgress.onFailed();
            }
            MyLog.e(TAG, "queue is null");
            return;
        }

        Observable.fromIterable(taskQueue)
                .subscribeOn(Schedulers.io())
                .filter(new Predicate<SongRes>() {
                    @Override
                    public boolean test(SongRes songRes) throws Exception {
                        if(songRes.isExist()){
                            taskQueue.remove(songRes);
                            return false;
                        }

                        return true;
                    }
                })
                .retry(3)
                .subscribe(new Observer<SongRes>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(SongRes songRes) {
                long length = U.getHttpUtils().getFileLength(songRes.downloadUrl);
                if(length > 0){
                    totalLength =+ length;
                    songRes.length = length;
                }
            }

            @Override
            public void onError(Throwable e) {
                MyLog.e(TAG, "what the fuck");
                if(onDownloadProgress != null){
                    onDownloadProgress.onFailed();
                }
            }

            @Override
            public void onComplete() {
                startQueue();
            }
        });
    }

    public void startQueue(){
        SongRes res = null;
        if(taskQueue.size() > 0){
            res = taskQueue.remove(0);
        }

        final SongRes songRes = res;

        //下载完了
        if(songRes == null){
            if(onDownloadProgress != null){
                onDownloadProgress.onCompleted("");
            }

            return;
        }

        if(songRes.length > 0){
            currentTask = songRes;

            //先用临时文件路径下载
            File file = new File(songRes.fileDir + File.separator + "temp" + songRes.outputFileName);
            if (!file.exists()) {
                File parentFile = file.getParentFile();
                if (parentFile != null && !parentFile.exists()) {
                    parentFile.mkdirs();
                }
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            U.getHttpUtils().downloadFileSync(songRes.getResUrl(), file, new HttpUtils.OnDownloadProgress() {
                @Override
                public void onDownloaded(long downloaded, long length) {
                    if(onDownloadProgress != null){
                        //这个totalLength所有文件的大小
                        onDownloadProgress.onDownloaded(downloadLength + downloaded, totalLength);
                    }
                }

                @Override
                public void onCompleted(String localPath) {
                    downloadLength = songRes.length;
                    File oldName = new File(localPath);
                    File newName = new File(songRes.fileDir + File.separator + songRes.outputFileName);

                    if(oldName.renameTo(newName)) {
                        System.out.println("已重命名");
                    } else {
                        System.out.println("Error");
                    }

                    if(taskQueue.size() == 0){
                        if(onDownloadProgress != null){
                            onDownloadProgress.onCompleted(localPath);
                        }
                    }
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
                 }
            });
        } else {
           startQueue();
        }
    }

    /**
     * 取消当前以及之后的所有task
     */
    public void cancelAllTask(){
        if(currentTask != null){
            U.getHttpUtils().cancelDownload(currentTask.downloadUrl);
            taskQueue.clear();
            onDownloadProgress.onCanceled();
        }
    }

    //下载歌曲资源，包括歌词，mid,mp3文件
//    private void downloadRes(final SongRes songRes, final HttpUtils.OnDownloadProgress onDownloadProgress){
//        if(songRes == null || TextUtils.isEmpty(songRes.getResUrl())){
//            MyLog.w(TAG, "downloadRes songres is error");
//            return;
//        }
//
//        String outputFilePath = U.getMD5Utils().MD5_16(songRes.getResUrl());
//        outputFilePath = U.getAppInfoUtils().getMainDir() + File.separator + outputFilePath;
//        final File outputFile = new File(outputFilePath);
//        if(outputFile.exists()){
//            outputFile.delete();
//        }
//        outputFile.mkdirs();
//
//
//    }
}
