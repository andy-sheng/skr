package com.wali.live.watchsdk.longtext.model.interior.item;

import com.wali.live.watchsdk.longtext.adapter.FeedItemUiType;

/**
 * Created by lan on 2017/9/20.
 */
public class CoverFeedItemModel extends BaseFeedItemModel {
    private String mCoverUrl;

    public CoverFeedItemModel(String coverUrl) {
        mCoverUrl = coverUrl;
    }

    public String getCoverUrl() {
        return mCoverUrl;
    }

    @Override
    public int getUiType() {
        return FeedItemUiType.UI_TYPE_COVER;
    }
}
