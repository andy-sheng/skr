package com.wali.live.watchsdk.lit.recycler.viewmodel;

/**
 * Created by lan on 16/6/28.
 */
public class EmptyModel extends SimpleTextModel {
    protected int mIconId;

    public EmptyModel(String text) {
        super(text);
    }

    public EmptyModel(String text, int iconId) {
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
