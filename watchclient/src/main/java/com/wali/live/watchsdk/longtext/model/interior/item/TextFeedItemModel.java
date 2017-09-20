package com.wali.live.watchsdk.longtext.model.interior.item;

import com.wali.live.proto.Feeds;

/**
 * Created by lan on 2017/9/20.
 */
public class TextFeedItemModel extends BaseFeedItemModel {
    public static final int TYPE_TEXT_TITLE = 1;
    public static final int TYPE_TEXT_CONTENT = 2;

    private int mTextType;
    private String mContent;
    private boolean mIsInner;

    public TextFeedItemModel(int mediaType, Feeds.Text protoText) {
        super(mediaType);
        parse(protoText);
    }

    public void parse(Feeds.Text protoText) {
        mTextType = protoText.getType();
        mContent = protoText.getContent();
        mIsInner = protoText.getIsInner();
    }

    public int getTextType() {
        return mTextType;
    }

    public String getContent() {
        return mContent;
    }

    public boolean isInner() {
        return mIsInner;
    }
}
