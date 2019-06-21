package com.zq.mediaengine.effect;

import com.common.log.MyLog;
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

/**
 * 管理抖音特效资源
 */
public class DyEffectResManager {
    public final static String TAG = "DyEffectResManager";
    private String LICENSE_NAME = "sige_20190620_20190630_com.zq.live_sige_v2.4.0.licbag";
    //    private File sRootDir = U.app().getExternalFilesDir("effects");
    private File sRootDir = U.getAppInfoUtils().getSubDirFile("dy_effects");// 抖音资源文件根目录

    public DyEffectResManager() {

    }

    Callback mCallback;

    public void tryLoadRes(Callback callback) {
        mCallback = callback;
        step1();
    }

    private void step1() {
        // 判断证书文件是否快过期,留一天buffer
        long expireTs = U.getPreferenceUtils().getSettingLong(U.getPreferenceUtils().longlySp(), "license_expire_ts", 0);
        if (expireTs - 24 * 60 * 60 * 1000 < System.currentTimeMillis()) {
            MyLog.e(TAG, "证书快过期了，请求服务器最新证书地址，下载");
            downloadLicense();
        } else {
            // 判断证书文件是否存在
            File licenseFile = new File(sRootDir, "license/dy_android_license.licbag");
            if (licenseFile.exists()) {
                MyLog.e(TAG, "证书存在，继续");
                // 可继续
                step2();
            } else {
                downloadLicense();
            }
        }
    }

    private void step2() {
        File file = new File(sRootDir, "model/");
        if (file.exists() && file.isDirectory()) {
            MyLog.e(TAG, "model目录存在,继续");
            if (mCallback != null) {
                mCallback.onResReady(file.getPath(), new File(sRootDir, "license/dy_android_license.licbag").getPath());
            }
        } else {
            MyLog.d(TAG, "step2 model 目录不存在");
        }
    }

    private void downloadLicense() {
        MyLog.d(TAG, "downloadLicense");
        // 目前服务器没接口下载 走解压逻辑
        unzipDyRes();
        step2();
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
