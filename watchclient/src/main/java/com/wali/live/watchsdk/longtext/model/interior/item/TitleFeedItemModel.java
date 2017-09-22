package com.wali.live.watchsdk.longtext.model.interior.item;

import com.wali.live.watchsdk.longtext.adapter.FeedItemUiType;

/**
 * Created by lan on 2017/9/20.
 */
public class TitleFeedItemModel extends BaseFeedItemModel {
    private String mTitle;

    public TitleFeedItemModel(String title) {
        mTitle = title;
    }

    public String getTitle() {
        return mTitle;
    }

    @Override
    public int getUiType() {
        return FeedItemUiType.UI_TYPE_TITLE;
    }
}
