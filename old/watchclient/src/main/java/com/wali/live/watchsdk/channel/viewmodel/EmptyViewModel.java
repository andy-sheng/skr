package com.wali.live.watchsdk.channel.viewmodel;

/**
 * Created by lan on 16/6/28.
 *
 * @module 频道
 */
public class EmptyViewModel extends SimpleTextViewModel {
    protected int mIconId;

    public EmptyViewModel(String text) {
        super(text);
    }

    public EmptyViewModel(String text, int iconId) {
        super(text);
        mIconId = iconId;
    }

    public int getIconId() {
        return mIconId;
    }

    public void setIconId(int iconId) {
        mIconId = iconId;
    }
}
