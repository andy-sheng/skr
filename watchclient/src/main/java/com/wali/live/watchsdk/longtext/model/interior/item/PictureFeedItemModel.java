package com.wali.live.watchsdk.longtext.model.interior.item;

import com.wali.live.proto.Feeds;

/**
 * Created by lan on 2017/9/20.
 */
public class PictureFeedItemModel extends BaseFeedItemModel {
    private String mUrl;
    private int mWidth;
    private int mHeight;
    private String mDesc;

    public PictureFeedItemModel(int mediaType, Feeds.Picture protoPicture) {
        super(mediaType);
        parse(protoPicture);
    }

    public void parse(Feeds.Picture protoPicture) {
        mUrl = protoPicture.getUrl();
        mWidth = protoPicture.getWidth();
        mHeight = protoPicture.getHeight();
        mDesc = protoPicture.getDesc();
    }

    public String getUrl() {
        return mUrl;
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    public String getDesc() {
        return mDesc;
    }
}
