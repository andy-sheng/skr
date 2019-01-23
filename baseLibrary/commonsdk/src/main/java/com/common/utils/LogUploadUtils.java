package com.common.utils;

import com.common.log.MyLog;
import com.common.upload.UploadCallback;
import com.common.upload.UploadParams;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class LogUploadUtils {
    public final static String TAG = "LogUploadUtils";
    Disposable mUploadLogTask;

    public void upload(long uid) {

        if (mUploadLogTask != null && !mUploadLogTask.isDisposed()) {
            U.getToastUtil().showShort("正在上传日志");
            return;
        }

        mUploadLogTask = Observable.create(new ObservableOnSubscribe<File>() {
            @Override
            public void subscribe(ObservableEmitter<File> emitter) {
                File logDir = new File(U.getAppInfoUtils().getMainDir() + File.separator + "logs/");
                if (logDir == null) {
                    emitter.onError(new Throwable("没有log文件夹"));
                }

                String zipFile = U.getAppInfoUtils().getMainDir() + File.separator + "logs/" + System.currentTimeMillis() + "log.zip";
                File filez = new File(zipFile);
                if (filez.exists()) {
                    filez.delete();
                }

                try {
                    filez.createNewFile();
                } catch (Exception e) {
                    emitter.onError(new Throwable("文件创建失败" + e.getMessage()));
                }

                boolean success = false;
                try {
                    success = U.getZipUtils().zip(getLastThreeFile(), filez.getAbsolutePath());
                } catch (IOException e) {
                    emitter.onError(new Throwable("文件压缩失败" + e.getMessage()));
                }

                if (!success) {
                    emitter.onError(new Throwable("文件压缩没成功"));
                }

                emitter.onNext(filez);
                emitter.onComplete();
            }
        }).subscribeOn(Schedulers.io()).subscribe(new Consumer<File>() {
            @Override
            public void accept(File file) throws Exception {
                UploadParams.newBuilder(file.getAbsolutePath())
                        .setFileType(UploadParams.FileType.log)
                        .setFileName(uid + "_" + U.getDateTimeUtils().formatTimeStringForDate(System.currentTimeMillis()) + ".zip")
                        .startUploadAsync(new UploadCallback() {
                            @Override
                            public void onProgress(long currentSize, long totalSize) {

                            }

                            @Override
                            public void onSuccess(String url) {
                                MyLog.w(TAG, "日志上传成功");
                                U.getToastUtil().showShort("反馈成功");
                                file.delete();
                            }

                            @Override
                            public void onFailure(String msg) {
                                MyLog.e(TAG, msg);
                                file.delete();
                            }
                        });
            }
        }, throwable -> {
            MyLog.e(TAG, throwable);
        });
    }

    public List<String> getLastThreeFile() {
        File logDir = new File(U.getAppInfoUtils().getMainDir() + File.separator + "logs/");
        ArrayList<String> lastThreeFiles = new ArrayList<>();
        if (logDir.exists() && logDir.isDirectory()) {
            File[] fileList = logDir.listFiles();
            // 文件修改时间排序
            Arrays.sort(fileList, new Comparator<File>() {
                public int compare(File f1, File f2) {
                    long diff = f1.lastModified() - f2.lastModified();
                    if (diff > 0)
                        return 1;
                    else if (diff == 0)
                        return 0;
                    else
                        return -1;
                }
            });
            for (int i = fileList.length - 1; i >= 0; i--) {
                if (fileList[i].isFile() && fileList[i].getName().endsWith(".log")) {
                    lastThreeFiles.add(fileList[i].getAbsolutePath());
                    if (lastThreeFiles.size() == 4) {
                        break;
                    }
                }
            }
        }
        return lastThreeFiles;
    }
}
