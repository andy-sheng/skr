package com.zq.mediaengine.effect;

import com.common.log.MyLog;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiResult;
import com.common.utils.HttpUtils;
import com.common.utils.U;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.zip.ZipInputStream;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.functions.Consumer;

/**
 * 管理抖音特效资源
 */
public class DyEffectResManager {
    public final static String TAG = "DyEffectResManager";

    /**
     * 资源下载地址
     */
    static final String RES_DOWNLOAD_URL = "http://res-static.inframe.mobi/pkgs/android/dy_effect_resource.zip";
    private File sRootDir = U.app().getExternalFilesDir("dy_effects");// 抖音资源文件根目录
    private File mLicenseFile = new File(sRootDir, "license/dy_android_license.licbag");// 抖音资源文件根目录
    private File mModelDir = new File(sRootDir, "model/");

    public DyEffectResManager() {

    }

    Callback mCallback;

    public void tryLoadRes(Callback callback) {
        mCallback = callback;
        Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(ObservableEmitter<Object> emitter) throws Exception {
                step1();
            }
        }).subscribeOn(U.getThreadUtils().urgentIO())
                .subscribe();
    }

    private void step1() {
        // 判断证书文件是否快过期,留一天buffer
        long expireTs = U.getPreferenceUtils().getSettingLong(U.getPreferenceUtils().longlySp(), "license_expire_ts", 0);
        if (expireTs - 24 * 60 * 60 * 1000 < System.currentTimeMillis()) {
            MyLog.e(TAG, "证书快过期了，请求服务器最新证书地址，下载");
            downloadLicense();
        } else {
            // 判断证书文件是否存在
            if (mLicenseFile.exists()) {
                MyLog.e(TAG, "证书存在，继续");
                // 可继续
                step2();
            } else {
                downloadLicense();
            }
        }
    }

    private void step2() {
        if (mModelDir.exists() && mModelDir.isDirectory()) {
            MyLog.e(TAG, "model目录存在,继续");
            if (mCallback != null) {
                mCallback.onResReady(mModelDir.getPath(), mLicenseFile.getPath());
            }
        } else {
            downloadRes();
        }
    }

    private void downloadLicense() {
        MyLog.d(TAG, "downloadLicense");
        // 目前服务器没接口下载 走解压逻辑
        EffectServerApi effectServerApi = ApiManager.getInstance().createService(EffectServerApi.class);
        effectServerApi.getDyLicenseUrl(20)
                .subscribe(new Consumer<ApiResult>() {
                    @Override
                    public void accept(ApiResult apiResult) throws Exception {
                        if (apiResult.getErrno() == 0) {
                            String url = apiResult.getData().getString("caURL");
                            String expireMs = apiResult.getData().getString("expireMs");
                            if (U.getHttpUtils().downloadFileSync(url, mLicenseFile, true, null)) {
                                MyLog.e(TAG, "证书下载成功");
                                U.getPreferenceUtils().setSettingLong(U.getPreferenceUtils().longlySp(), "license_expire_ts", Long.parseLong(expireMs));
                                step2();
                            } else {
                                MyLog.e(TAG, "证书下载失败");
                            }

                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable e) throws Exception {
                        MyLog.e(TAG, e);
                    }
                });
    }

    private void downloadRes() {
        U.getFileUtils().deleteAllFiles(U.getAppInfoUtils().getSubDirPath("dy_effects"));
        File resZipFile = new File(U.getAppInfoUtils().getMainDir(), "dy_effect_resource.zip");
        if (U.getHttpUtils().downloadFileSync(RES_DOWNLOAD_URL, resZipFile, true, null)) {
            MyLog.d(TAG, "资源下载成功");
            try {
                String tempFile = U.getAppInfoUtils().getSubDirPath("effect_temp");
                U.getZipUtils().unzipFile(resZipFile.getPath(), tempFile);
                MyLog.d(TAG, "资源解压成功");
                File modelFile = U.getFileUtils().findSubDirByName(new File(tempFile), "model");
                MyLog.d(TAG, "modelFile="+modelFile.getPath());
                if (modelFile != null) {
                    U.getFileUtils().moveFile(modelFile.getPath(), mModelDir.getPath());
                    MyLog.e(TAG, "model remove 成功");
                }
                File resFile = U.getFileUtils().findSubDirByName(new File(tempFile), "res");
                MyLog.d(TAG, "resFile="+resFile.getPath());
                if (resFile != null) {
                    U.getFileUtils().moveFile(resFile.getPath(), new File(sRootDir, "res/").getPath());
                    MyLog.d(TAG, "resFile remove 成功");
                }
                U.getFileUtils().deleteAllFiles(resZipFile);
                U.getFileUtils().deleteAllFiles(tempFile);
                if (mCallback != null) {
                    mCallback.onResReady(mModelDir.getPath(), mLicenseFile.getPath());
                }
            } catch (IOException e) {
                MyLog.e(TAG, e);
            }
        } else {
            MyLog.e(TAG, "资源下载失败");
        }
    }

    private void unzipDyRes() {
        ZipInputStream zipInputStream = null;
        try {
            zipInputStream = new ZipInputStream(U.app().getAssets().open("dy_effects.zip"));
            U.getZipUtils().unzipFile(zipInputStream, U.getAppInfoUtils().getMainDir());
            MyLog.e(TAG, "抖音资源解压成功");
            //2019/6/30 20:27:29
            //1561897649000
            U.getPreferenceUtils().setSettingLong(U.getPreferenceUtils().longlySp(), "license_expire_ts", 1561897649000L);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public File[] getResources(String type) {
        File file = new File(new File(sRootDir, type), "");
        if (file.exists() && file.isDirectory())
            return file.listFiles();
        return new File[0];
    }

    public String getBeautyResPath() {
        File[] file = getResources("res/BeautyResource.bundle");
        if (file.length > 0) {
            return file[0].getPath();
        }
        return null;
    }

    public String getReshapeResPath() {
        File files[] = getResources("res/ReshapeResource.bundle");
        if (files.length > 0) {
            return files[0].getPath();
        }
        return null;
    }

    public String getFilterResources(int no) {
        File[] files = getResources("res/FilterResource.bundle/1");
        List<File> fileList = filterFiles(files);

        if (no >= fileList.size() || no < 0) {
            return null;
        }
        return fileList.get(no).getPath();
    }

    public String getStickersPath(int no) {
        File file = new File(new File(sRootDir, "res"), "stickers");
        List<File> fileList = filterFiles(file.listFiles());

        if (no >= fileList.size() || no < 0) {
            return null;
        }
        return fileList.get(no).getPath();
    }

    /**
     * 过滤调 .DS_Store 等文件
     *
     * @param files
     * @return
     */
    List<File> filterFiles(File files[]) {
        List<File> list = new ArrayList<>();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    list.add(f);
                }
            }
        }
        Collections.sort(list, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        return list;
    }
//    public String getReshapeResPath() {
//        File file =  new File(sRootDir,"res/ReshapeResource.bundle");
//        if(file.exists()){
//            return file.getPath();
//        }
//        MyLog.e(TAG,"模型文件不存在2");
//        return null;
//    }

    public interface Callback {
        void onResReady(String modelDir, String licensePath);
    }
}
