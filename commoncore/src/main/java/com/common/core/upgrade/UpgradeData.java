package com.common.core.upgrade;

import org.greenrobot.eventbus.EventBus;

public class UpgradeData {
    public static final int STATUS_INIT = 1;
    public static final int STATUS_LOAD_DATA_FROM_SERVER = 2;
    public static final int STATUS_DOWNLOWNING = 3;
    public static final int STATUS_DOWNLOWNED = 4;
    public static final int STATUS_INSTALLING = 5;
    public static final int STATUS_FINISH = 6;

    int mStatus = STATUS_INIT;
    UpgradeInfoModel mUpgradeInfoModel;
    boolean mute = false;
    long mDownloadId;
    boolean needShowDialog = false;
    private boolean mNeedUpdate;
    boolean needShowRedDot = false; // 是否需要显示升级红点

    public boolean isNeedShowDialog() {
        return needShowDialog;
    }

    public void setNeedShowDialog(boolean needShowDialog) {
        this.needShowDialog = needShowDialog;
    }

    public boolean isMute() {
        return mute;
    }

    public void setMute(boolean mute) {
        this.mute = mute;
    }

    public int getStatus() {
        return mStatus;
    }

    public void setStatus(int status) {
        mStatus = status;
    }

    public long getDownloadId() {
        return mDownloadId;
    }

    public void setDownloadId(long downloadId) {
        mDownloadId = downloadId;
    }

    public UpgradeInfoModel getUpgradeInfoModel() {
        return mUpgradeInfoModel;
    }

    public void setUpgradeInfoModel(UpgradeInfoModel upgradeInfoModel) {
        mUpgradeInfoModel = upgradeInfoModel;
    }

    public void setNeedUpdate(boolean needUpdate) {
        mNeedUpdate = needUpdate;
    }

    public boolean getNeedUpdate() {
        return mNeedUpdate;
    }

    public boolean isNeedShowRedDot() {
        return needShowRedDot;
    }

    public void setNeedShowRedDot(boolean needShowRedDot) {
        if(this.needShowRedDot != needShowRedDot){
            this.needShowRedDot = needShowRedDot;
            EventBus.getDefault().post(new RedDotStatusEvent(needShowRedDot));
        }
    }

    public static class RedDotStatusEvent{
        boolean show = false;

        public RedDotStatusEvent(boolean show) {
            this.show = show;
        }
    }
}
