package com.wali.live.moduletest.replugin;

import com.common.utils.U;

public class PackageData {
    public static final int STATUS_UNINSTALL = 1;
    public static final int STATUS_INSTALLING = 2;
    public static final int STATUS_INSTALLED = 3;

    private String sdcardPath;
    private String appName;
    private String packageName;
    private String versionName;
    private int status;
    private boolean mFromInstallView = false;
    private int versionCode = 0;
    private int oldVersionCode = 0;

    @Override
    public String toString() {
        return U.getFileUtils().getFileNameFromFilePath(sdcardPath) + "\r\n" +
                appName + "\r\n" +
                packageName + "\r\n" +
                oldVersionCode + "=>" + versionCode;
    }

//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//        PackageData that = (PackageData) o;
//
//        return sdcardPath.equals(that.sdcardPath);
//    }
//
//    @Override
//    public int hashCode() {
//        return sdcardPath.hashCode();
//    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getSdcardPath() {
        return sdcardPath;
    }

    public void setSdcardPath(String sdcardPath) {
        this.sdcardPath = sdcardPath;
    }

    public String getAppName() {
        return appName;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setFromInstallView(boolean fromInstallView) {
        mFromInstallView = fromInstallView;
    }

    public boolean isFromInstallView() {
        return mFromInstallView;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public int getOldVersionCode() {
        return oldVersionCode;
    }

    public void setOldVersionCode(int oldVersionCode) {
        this.oldVersionCode = oldVersionCode;
    }
}
