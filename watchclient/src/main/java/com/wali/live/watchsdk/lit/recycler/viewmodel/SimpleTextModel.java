package com.wali.live.watchsdk.lit.recycler.viewmodel;

/**
 * Created by lan on 16/6/28.
 */
public class SimpleTextModel extends BaseViewModel {
    protected String mText;

    public SimpleTextModel(String text) {
        mText = text;
    }

    public String getText() {
        return mText;
    }

    public void setText(String text) {
        mText = text;
    }
}
