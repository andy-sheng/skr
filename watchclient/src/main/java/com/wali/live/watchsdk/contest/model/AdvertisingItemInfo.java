package com.wali.live.watchsdk.contest.model;

import com.wali.live.proto.LiveSummitProto;

/**
 * Created by wanglinzhang on 2018/2/1.
 */
public class AdvertisingItemInfo {
    private String mIconUrl;
    private String mTitle;
    private String mSubTitle;
    private boolean mHasDownloadCard;
    private boolean mHasOpenCard;

    private String mDownloadUrl;
    private String mPackageName;
    private String mName;

    public AdvertisingItemInfo(LiveSummitProto.GameRevivalActInfo actInfo) {
        mIconUrl = actInfo.getIcon();
        mTitle = actInfo.getTitle();
        mSubTitle = actInfo.getSubTitle();
        mHasDownloadCard = actInfo.getCanDown();
        mHasOpenCard = actInfo.getCanOpen();
        mDownloadUrl = actInfo.getDownloadUrl();
        mPackageName = actInfo.getGamePkgName();
        mName = mTitle;
    }

    public void setIconUrl(String iconUrl) {
        mIconUrl = iconUrl;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public void setSubTitle(String subTitle) {
        mSubTitle = subTitle;
    }

    public void setDownloadCard(boolean has) {
        mHasDownloadCard = has;
    }

    public void setOpenCard(boolean has) {
        mHasOpenCard = has;
    }

    public String getIconUrl() {
        return mIconUrl;
    }

    public String getTitle() {
        return mTitle;
    }

    public boolean hasDownloadCard() {
        return mHasDownloadCard;
    }

    public boolean hasOpenCard() {
        return mHasOpenCard;
    }

    public String getDownloadUrl() {
        return mDownloadUrl;
    }

    public String getPackageName() {
        return mPackageName;
    }

    public String getName() {
        return mName;
    }

    @Override
    public String toString() {
        return "AdvertisingItemInfo{" +
                "iconUrl='" + mIconUrl + '\'' +
                ", title=" + mTitle +
                ", subTitle=" + mSubTitle +
                ", packageName=" + mPackageName +
                ", downloadUrl=" + mDownloadUrl +
                ", canDownload=" + mHasDownloadCard +
                ", canOpen=" + mHasOpenCard +
                '}';
    }
}
