package com.common.core.upgrade;


import android.text.TextUtils;

import java.io.Serializable;

public class UpgradeInfoModel implements Serializable {
    String downloadURL;
    long updateTimeMs;
    int latestVersionCode;
    boolean forceUpdate;
    long packageSize;
    String updateTitle;
    String updateContent;
    String mVersionName;
    String mPackageSizeStr;

    public String getDownloadURL() {
        return downloadURL;
    }

    public void setDownloadURL(String downloadURL) {
        this.downloadURL = downloadURL;
    }

    public long getUpdateTimeMs() {
        return updateTimeMs;
    }

    public void setUpdateTimeMs(long updateTimeMs) {
        this.updateTimeMs = updateTimeMs;
    }

    public int getLatestVersionCode() {
        return latestVersionCode;
    }

    public void setLatestVersionCode(int latestVersionCode) {
        this.latestVersionCode = latestVersionCode;
    }

    public boolean isForceUpdate() {
        return forceUpdate;
    }

    public void setForceUpdate(boolean forceUpdate) {
        this.forceUpdate = forceUpdate;
    }

    public long getPackageSize() {
        return packageSize;
    }

    public void setPackageSize(long packageSize) {
        this.packageSize = packageSize;
    }

    public String getUpdateTitle() {
        return updateTitle;
    }

    public void setUpdateTitle(String updateTitle) {
        this.updateTitle = updateTitle;
    }

    public String getUpdateContent() {
        return updateContent;
    }

    public void setUpdateContent(String updateContent) {
        this.updateContent = updateContent;
    }

    public String getVersionName() {
        if (TextUtils.isEmpty(mVersionName)) {
            int a = latestVersionCode / (1000 * 1000);
            int b = (latestVersionCode - a * (1000 * 1000)) / 1000;
            int c = (latestVersionCode - a * (1000 * 1000) - b * 1000);
            mVersionName = a + "." + b + "." + c;
        }
        return mVersionName;
    }

    public String getPackageSizeStr() {
        if (TextUtils.isEmpty(mPackageSizeStr)) {
//            int a  = (int) (packageSize/(1024*1024));
            mPackageSizeStr = String.format("%.1fM", packageSize / (1024 * 1024.0));
        }
        return mPackageSizeStr;
    }
}
