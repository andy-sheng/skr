package com.wali.live.watchsdk.webview;

import android.content.Context;
import android.text.TextUtils;

import com.base.log.MyLog;
import com.base.utils.Constants;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 目前只有资讯页面才使用新的缓存包
 *
 * @author liubiqiang
 */
public class NewH5CacheManager {
    public static final String TAG = "H5Cache";

    private static final String H5_VALID_FILE = "h5cache_invalid";
    private static final String ASSET_ZIP_FILE_PREFIX = "asset_";

    private static NewH5CacheManager sInstance;

    private List<NewH5CachePackage> mCachedPackage;
    private boolean mDirty = false;

    /***
     * @param ctx
     * @param webkitProcess : 用于跨进程访问时用
     * @return
     */
    public static NewH5CacheManager getInstance(Context ctx, boolean webkitProcess) {
        synchronized (NewH5CacheManager.class) {
            if (sInstance == null) {
                sInstance = new NewH5CacheManager(ctx, webkitProcess);
            }
        }
        if (sInstance.isDirty()) {
            sInstance.loadAllPackage(ctx);
            sInstance.setDirty(false);
        } else if (webkitProcess) {//只有webkit页面才有实际访问 有可能是主线程的先执行
            List<NewH5CachePackage> cachePkgList = sInstance.cachePackageList();
            if (!cachePkgList.isEmpty()) {
                ArrayList<String> listFiles = readValidCacheFiles(ctx);
                String localCachePath = NewH5CachePackage.getLocalCachePath(ctx);
                boolean isDirty = false;
                for (NewH5CachePackage pkg : cachePkgList) {
                    if (!pkg.fromAsset()) {
                        if (!listFiles.contains(localCachePath + pkg.fileName())) {
                            isDirty = true;
                            break;
                        }
                    } else {
                        if (!listFiles.contains(ASSET_ZIP_FILE_PREFIX + pkg.fileName())) {
                            isDirty = true;
                            break;
                        }
                    }
                }

                if (isDirty) {
                    sInstance.loadAllPackage(ctx);
                }
            }
        }
        return sInstance;
    }

    public boolean isDirty() {
        return mDirty;
    }

    public void setDirty(boolean dirty) {
        mDirty = dirty;
    }

    public List<NewH5CachePackage> cachePackageList() {
        return mCachedPackage;
    }

    private NewH5CacheManager(Context ctx, boolean webkitProcess) {
        MyLog.w(TAG, "current process is webkit ? " + webkitProcess);
        mCachedPackage = new CopyOnWriteArrayList<NewH5CachePackage>();// XXX so it can not sort
        if (webkitProcess) {
            //删除无用的cache文件
            ArrayList<String> listFiles = readValidCacheFiles(ctx);
            if (listFiles != null && !listFiles.isEmpty()) {
                try {
                    String localCachePath = NewH5CachePackage.getLocalCachePath(ctx);
                    File file = new File(localCachePath);
                    String[] h5CacheFiles = file.list();
                    String fullPath = null;
                    if (h5CacheFiles != null && h5CacheFiles.length > 0) {
                        for (String h5CacheFile : h5CacheFiles) {
                            fullPath = localCachePath + h5CacheFile;//全路径判断哦
                            if (!listFiles.contains(fullPath)) {
                                File cacheFile = new File(fullPath);
                                if (cacheFile.exists()) {
                                    cacheFile.delete();
                                    MyLog.w(TAG, "delete unused h5 cache file=" + fullPath);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    MyLog.w(TAG, e);
                }
            }
        }
        loadAllPackage(ctx);
    }

    private void loadAllPackage(Context ctx) {
        mCachedPackage.clear();

        //load all cache Package
        String[] h5CacheFiles = null;
        boolean asset = false;
        String localCachePath = null;
        try {
            localCachePath = NewH5CachePackage.getLocalCachePath(ctx);
            File file = new File(localCachePath);
            h5CacheFiles = file.list();
        } catch (Exception e) {
            MyLog.w(TAG, e);
        }

        if (h5CacheFiles != null && h5CacheFiles.length > 0) {
            ArrayList<NewH5CachePackage> pkgList = NewH5CachePackage.loadCachePackage(ctx, h5CacheFiles, asset);
            if (pkgList != null && !pkgList.isEmpty()) {
                if (Constants.isDebugBuild) {
                    for (NewH5CachePackage pkg : pkgList) {
                        MyLog.e(TAG, "add one local cache pkg;version=" + pkg.getVersion());
                    }
                }
                mCachedPackage.addAll(pkgList);
            }
        }

        if (1 == mCachedPackage.size()) {//已经读到最大的缓存包个数了，就没有必要读其他的了
            return;
        }

        //有可能有两个包，只更新其中的一个
        try {
            h5CacheFiles = ctx.getAssets().list(NewH5CachePackage.KCachePackageFileDir);
            asset = true;
        } catch (Exception e) {
            MyLog.w(TAG, e);
        }

        if (h5CacheFiles != null && h5CacheFiles.length > 0) {
            ArrayList<NewH5CachePackage> pkgList = NewH5CachePackage.loadCachePackage(ctx, h5CacheFiles, asset);
            ArrayList<String> fileList = readValidCacheFiles(ctx);
            boolean changed = false;
            if (pkgList != null && !pkgList.isEmpty()) {
                if (mCachedPackage.isEmpty()) {//没有缓存数据可以直接添加
                    if (Constants.isDebugBuild) {
                        for (NewH5CachePackage pkg : pkgList) {
                            MyLog.e(TAG, "add one asset pkg;version=" + pkg.getVersion());
                        }
                    }
                    mCachedPackage.addAll(pkgList);

                    String fileName;
                    for (NewH5CachePackage pkg : pkgList) {
                        fileName = ASSET_ZIP_FILE_PREFIX + pkg.fileName();
                        if (!fileList.contains(fileName)) {
                            fileList.add(fileName);
                            changed = true;
                        }
                    }
                } else {
                    for (NewH5CachePackage pkg : pkgList) {
                        for (NewH5CachePackage localPkg : mCachedPackage) {
                            if (!TextUtils.equals(localPkg.getPkgName(),
                                    pkg.getPkgName())) {
                                MyLog.e(TAG, "add one asset pkg;version=" + pkg.getVersion());
                                mCachedPackage.add(pkg);
                                if (!fileList.contains(ASSET_ZIP_FILE_PREFIX + pkg.fileName())) {
                                    fileList.add(ASSET_ZIP_FILE_PREFIX + pkg.fileName());
                                    changed = true;
                                }
                            }
                        }
                    }
                }
            }
            if (changed) {
                writeValidCacheFiles(ctx, fileList);
            }
        }
    }

    //载入缓存数据
    public NewH5CachePackage loadPackage(String path, Context context) {
        //已经加载过的数据
        for (NewH5CachePackage cp : mCachedPackage) {
            if (cp.isCachedPackage(path)) {
                MyLog.e(TAG, "find one pkg;version=" + cp.getVersion());
                return cp;
            }
        }
        return null;
    }

    /**
     * 将asset中的文件拷贝出来以便进行diff path
     *
     * @param context
     * @param localZipName
     * @return 拷贝成功的文件
     */
    public static String copyAssets(Context context, String localZipName) {
        MyLog.w(TAG, "copy asset files");
        InputStream assetFileStream = null;
        OutputStream destination = null;
        byte[] buffer = new byte[1024];
        int nread;

        File dstPath = new File(NewH5CachePackage.getLocalCachePath(context));
        if (!dstPath.exists()) {
            dstPath.mkdirs();
        }
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(dstPath.getAbsolutePath()).append('/');
            sb.append("asset_").append(localZipName);
            String fileName = sb.toString();
            File file = new File(fileName);
            if (file.exists()) {// 如果存在就不拷贝
                return null;
            }

            destination = new FileOutputStream(fileName);

            assetFileStream = context.getAssets().open(NewH5CachePackage.KCachePackageFileDir + '/' + localZipName);
            while ((nread = assetFileStream.read(buffer)) != -1) {
                if (nread == 0) {
                    nread = assetFileStream.read();
                    if (nread < 0) {
                        break;
                    }
                    destination.write(nread);
                    continue;
                }
                destination.write(buffer, 0, nread);
            }
            return fileName;
        } catch (Exception e) {
            if (Constants.isDebugBuild)
                MyLog.w(TAG, e);
        } finally {
            if (assetFileStream != null) {
                try {
                    assetFileStream.close();
                    assetFileStream = null;
                } catch (IOException e) {
                    if (Constants.isDebugBuild)
                        MyLog.w(TAG, e);
                }
            }
            if (destination != null) {
                try {
                    destination.close();
                    destination = null;
                } catch (IOException e) {
                    if (Constants.isDebugBuild)
                        MyLog.w(TAG, e);
                }
            }
        }
        return null;
    }

    public static ArrayList<String> readValidCacheFiles(Context ctx) {
        ArrayList<String> fileNameList = new ArrayList<String>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(ctx.getFilesDir()
                    .getAbsolutePath() + '/' + H5_VALID_FILE));
            String fileName = null;
            while ((fileName = br.readLine()) != null) {
                if (Constants.isDebugBuild) {
                    MyLog.w(TAG, "valid File:read file name=" + fileName);
                }
                if (!fileNameList.contains(fileName)) {
                    fileNameList.add(fileName);
                    if (Constants.isDebugBuild)
                        MyLog.w(TAG, "valid File:add file name=" + fileName);
                }
            }
        } catch (Exception e) {
            if (Constants.isDebugBuild) {
                MyLog.w(TAG, e);
            }
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    if (Constants.isDebugBuild) {
                        MyLog.w(TAG, e);
                    }
                }
            }
        }
        return fileNameList;
    }

    public static void writeValidCacheFiles(Context ctx, ArrayList<String> fileNames) {
        if (null == fileNames || fileNames.isEmpty()) {
            return;
        }

        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(ctx.getFilesDir()
                    .getAbsolutePath() + '/' + H5_VALID_FILE, false));
            for (String fileName : fileNames) {
                if (Constants.isDebugBuild) MyLog.w(TAG, "valid File:write file name=" + fileName);
                bw.write(fileName + '\n');//需要自己手动添加换行符
            }
            bw.flush();
        } catch (Exception e) {
            if (Constants.isDebugBuild) {
                MyLog.w(TAG, e);
            }
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                    if (Constants.isDebugBuild) {
                        MyLog.w(TAG, e);
                    }
                }
            }
        }
    }
}
