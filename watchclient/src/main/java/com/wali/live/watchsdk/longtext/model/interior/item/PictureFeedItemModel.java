package com.wali.live.watchsdk.longtext.model.interior.item;

import com.wali.live.proto.Feeds;
import com.wali.live.watchsdk.longtext.adapter.FeedItemUiType;

/**
 * Created by lan on 2017/9/20.
 */
public class PictureFeedItemModel extends BaseFeedItemModel {
    private String mUrl;
    private int mWidth;
    private int mHeight;
    private String mDesc;

    public PictureFeedItemModel(Feeds.Picture protoPicture) {
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

    @Override
    public int getUiType() {
        return FeedItemUiType.UI_TYPE_PIC;
    }
}
