package com.wali.live.watchsdk.channel.viewmodel;

/**
 * Created by lan on 16/6/28.
 *
 * @module 频道
 * @description 频道简单文本的数据模型，提供非频道相关的占位信息
 */
public class SimpleTextViewModel extends BaseViewModel {
    protected String mText;

    public SimpleTextViewModel(String text) {
        mText = text;
    }

    public String getText() {
        return mText;
    }

    public void setText(String text) {
        mText = text;
    }
}
