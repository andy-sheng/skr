package com.wali.live.watchsdk.webview;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.base.log.MyLog;
import com.base.utils.CommonUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class NewH5CachePackage {
    private String mPackageName;    // 本包的本地包名
    private String mUrlRootPath;    // 本包所对应的URL目录
    private int mVersion;

    private Map<String, H5CacheFileInfo> mFileMap = new HashMap();

    private boolean mFromAsset = true;
    private String mMD5;
    private String mFileName;       // 不含路径

    public static final String KCachePackageFileDir = "NewH5Cache";
    private static final String KCacheMapFile = "mapping.json";

    private NewH5CachePackage(String mapJson, boolean fromAsset, String fileName) {
        parseMapJson(mapJson);
        mFromAsset = fromAsset;
        mFileName = fileName;
    }

    public boolean fromAsset() {
        return mFromAsset;
    }

    public String getPkgName() {
        return mPackageName;
    }

    public void setMD5(String md5) {
        MyLog.w(NewH5CacheManager.TAG, mFileName + ";md5=" + md5);
        mMD5 = md5;
    }

    public String getMD5() {
        return mMD5;
    }

    public int getVersion() {
        return mVersion;
    }

    public String fileName() {
        return mFileName;
    }

    /**
     * @param context
     * @param cacheFiles
     * @param asset
     * @return
     */
    public static ArrayList<NewH5CachePackage> loadCachePackage(Context context,
                                                                String[] cacheFiles, boolean asset) {
        ArrayList<NewH5CachePackage> pkgList = new ArrayList<NewH5CachePackage>();
        ArrayList<String> fileNameList = NewH5CacheManager.readValidCacheFiles(context);
        String localCacheFilePath = NewH5CachePackage.getLocalCachePath(context);
        String localZipFullFileName = null;
        InputStream packFileStream = null;
        for (String localZipName : cacheFiles) {
            if (!asset) {//asset目录下否是有效的 保险 XXX
                localZipFullFileName = localCacheFilePath + localZipName;//因为文件里面记得是全路径名
                if (!fileNameList.contains(localZipFullFileName)) {
                    continue;
                }
            }

            ZipInputStream inZip = null;
            ZipEntry entry = null;
            String mapJson = "";
            try {
                packFileStream = NewH5CachePackage.findPackage(localZipName, context, asset);
                if (packFileStream == null) {
                    continue;
                }

                inZip = new ZipInputStream(packFileStream);
                while ((entry = inZip.getNextEntry()) != null) {
                    if (!entry.isDirectory() && entry.getName().equalsIgnoreCase(KCacheMapFile)) {
                        mapJson = new String(readStream(inZip));
                        break;
                    }
                }
            } catch (Exception e) {
                Log.w(NewH5CacheManager.TAG, e);
            } finally {
                if (packFileStream != null) {
                    try {
                        packFileStream.close();
                    } catch (IOException e) {
                        MyLog.w(NewH5CacheManager.TAG, "", e);
                    }
                    packFileStream = null;
                }
                if (inZip != null) {
                    try {
                        inZip.close();
                        inZip = null;
                    } catch (IOException e) {
                        Log.w(NewH5CacheManager.TAG, e);
                    }
                }
            }

            if (!TextUtils.isEmpty(mapJson)) {
                try {
                    packFileStream = NewH5CachePackage.findPackage(localZipName, context, asset);
                    NewH5CachePackage pkg = new NewH5CachePackage(mapJson, asset, localZipName);
                    pkg.setMD5(CommonUtils.getFileStreamMd5(packFileStream));
                    pkgList.add(pkg);
                } catch (Exception e) {
                    Log.w(NewH5CacheManager.TAG, e);
                }
            }
        }
        return pkgList;
    }

    private static byte[] readStream(InputStream inputStream) {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        try {
            while ((len = inputStream.read(buffer)) != -1) {
                bout.write(buffer, 0, len);
            }
            bout.close();
        } catch (IOException e) {
            Log.w(NewH5CacheManager.TAG, e);
        }
        return bout.toByteArray();
    }

    //从assets文件夹或File目录下，获取Cache压缩包
    private static InputStream findPackage(String localName, Context context, boolean asset) {
        if (asset) {
            //从Asset中读取
            try {
                InputStream assetFileStream = NewH5CachePackage.getLocalAssetPath(context, localName);
                if (assetFileStream != null) {
                    return assetFileStream;
                }
            } catch (Exception e) {
                Log.w(NewH5CacheManager.TAG, e);
            }
        } else {
            //从本地数据中读取
            String localPath = getLocalCachePath(context) + localName;
            try {
                FileInputStream fileStream = new FileInputStream(localPath);
                if (fileStream != null) {
                    return fileStream;
                }
            } catch (Exception e) {
                Log.w(NewH5CacheManager.TAG, e);
            }
        }
        return null;
    }

    //是否是某个url的包
    public boolean isCachedPackage(String url) {
        String rootPath = getRootPathForCache(url);
        if (TextUtils.isEmpty(rootPath)) {
            return false;
        }
        return rootPath.startsWith(mUrlRootPath);
    }

    /**
     * @param url
     * @return
     */
    public static String getRootPathForCache(String url) {
        int pos = url.lastIndexOf("/");
        if (pos < 0) {
            return null;
        }
        return url.substring(0, pos);
    }

    public static String removeParamenters(String filePath) {
        int pos = filePath.indexOf("?");
        if (pos >= 0)
            filePath = filePath.substring(0, pos);
        if (filePath.indexOf("/") == 0)
            filePath = filePath.substring(1);
        return filePath;
    }

    //读取cache中的文件
    public InputStream loadCache(String url, Context context) {
        InputStream in = loadCacheByUrl(url, context);
        return in;
    }

    private InputStream loadCacheByUrl(String url, Context context) {
        url = url.toLowerCase(Locale.US);
        if (url.startsWith(mUrlRootPath)) {
            String filePath = url.substring(mUrlRootPath.length());

            filePath = removeParamenters(filePath);

            // in cache map.json?
            if (!mFileMap.containsKey(filePath)) {
                return null;
            }

            InputStream packFileStream = NewH5CachePackage.findPackage(mFileName,
                    context, mFromAsset);
            if (null == packFileStream) {
                return null;
            }

            ZipInputStream inZip = new ZipInputStream(packFileStream);
            ZipEntry entry = null;

            try {
                while ((entry = inZip.getNextEntry()) != null) {
                    if (!entry.isDirectory() && entry.getName().equalsIgnoreCase(filePath)) {
                        return inZip;
                    }
                }
                inZip.close();
            } catch (IOException e) {
                Log.w(NewH5CacheManager.TAG, e);
            }
        }
        return null;
    }

    /**
     * 用于放升级的包
     *
     * @param context
     * @return
     */
    public static String getLocalCachePath(Context context) {
        StringBuilder sb = new StringBuilder();
        sb.append(context.getFilesDir()).append('/');
        sb.append(NewH5CachePackage.KCachePackageFileDir);
        sb.append('/');
        return sb.toString();
    }

    public static InputStream getLocalAssetPath(Context ctx, String localFileName) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(KCachePackageFileDir);
        sb.append('/').append(localFileName);
        return ctx.getAssets().open(sb.toString());
    }

    public H5CacheFileInfo getFileInfo(String url) {
        url = url.toLowerCase(Locale.US);
        if (url.startsWith(mUrlRootPath)) {
            String filePath = url.substring(mUrlRootPath.length());

            filePath = removeParamenters(filePath);

            // in cache map.json?
            if (!mFileMap.containsKey(filePath)) {
                return null;
            }

            return mFileMap.get(filePath);
        }
        return null;
    }

    static public class H5CacheFileInfo {
        private String mMd5;
        private String mContentType;
        private String charset;

        public H5CacheFileInfo(JSONObject obj) throws JSONException {
            mMd5 = obj.optString("md5", "");
            mContentType = obj.optString("Content-Type", "");
            charset = obj.optString("charset", "");
        }

        public String getMd5() {
            return mMd5;
        }

        public String getContentType() {
            return mContentType;
        }

        public String getCharset() {
            return charset;
        }
    }

    private void parseMapJson(String mapJson) {
        mFileMap.clear();
        try {
            JSONObject obj = new JSONObject(mapJson);
            Iterator<String> iter = obj.keys();
            while (iter.hasNext()) {
                String key = iter.next();
                if ("onlinePath".equalsIgnoreCase(key)) {
                    mUrlRootPath = obj.getString(key);
                } else if ("pkgName".equalsIgnoreCase(key)) {
                    mPackageName = obj.getString(key);
                } else if ("version".equalsIgnoreCase(key)) {
                    mVersion = obj.getInt(key);
                } else if (!TextUtils.isEmpty(key)) {
                    JSONObject info = obj.optJSONObject(key);
                    if (info != null) {
                        H5CacheFileInfo fileInfo = new H5CacheFileInfo(info);
                        mFileMap.put(key.toLowerCase(Locale.US), fileInfo);
                    }
                }
            }
        } catch (JSONException e) {
            Log.w(NewH5CacheManager.TAG, e);
        }
    }
}
