package com.common.core.upgrade;


import android.text.TextUtils;

import java.io.Serializable;

/**
 * 几个典型的CASE
 * 1. 服务器配置的升级条目有
 * 1.7.0 不强升
 * 1.5.0 强升
 * 这时用户的版本为
 * a. 1.4.0 希望返回 1.7.0 强升
 * b. 1.6.0 希望返回 1.7.0 不强升
 */
public class UpgradeInfoModel implements Serializable {
    String downloadURL; // 下载url
    long updateTimeMs; // 更新时间
    int latestVersionCode;// url的包对应的版本号
    boolean forceUpdate;// 是否强制升级
    long packageSize;// 包大小
    String updateTitle;// 更新文本标题
    String updateContent;// 更新文本内容
    String mVersionName;// 版本name 客户端自用
    String mPackageSizeStr;// 包大小文本描述自用

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
