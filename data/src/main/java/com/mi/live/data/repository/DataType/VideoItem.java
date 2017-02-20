package com.mi.live.data.repository.DataType;

import java.io.Serializable;

/**
 * Created by lan on 15-12-10.
 */
public class VideoItem implements Serializable {
    private static final String TAG = VideoItem.class.getSimpleName();

    private String mLocalPath;
    private long mDuration;

    // 与ui相关
    private boolean mIsSelected;
    public boolean mIsFirstItem = false;
    public VideoItem() {}

    public VideoItem(String localPath) throws Exception {
        setLocalPath(localPath);
    }

    public void setLocalPath(String localPath) {
        mLocalPath = localPath;
    }

    public String getLocalPath() {
        return mLocalPath;
    }

    public long getDuration() {
        return mDuration;
    }

    public void setDuration(long duration) {
        this.mDuration = duration;
    }

    public void setSelected(boolean isSelected) {
        mIsSelected = isSelected;
    }

    public boolean isSelected() {
        return mIsSelected;
    }
}
