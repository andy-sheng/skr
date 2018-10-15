package com.wali.live.watchsdk.longtext.model.interior.item;

import com.wali.live.watchsdk.longtext.adapter.FeedItemUiType;
import com.wali.live.watchsdk.longtext.model.interior.FeedLikeModel;

/**
 * Created by lan on 2017/9/20.
 */
public class ViewerFeedItemModel extends BaseFeedItemModel {
    private int mViewerCount;
    private FeedLikeModel mLikeModel;
    private long mCreateTime;

    public ViewerFeedItemModel(int viewerCount, FeedLikeModel likeModel, long createTime) {
        mViewerCount = viewerCount;
        mLikeModel = likeModel;
        mCreateTime = createTime;
    }

    public int getViewerCount() {
        return mViewerCount;
    }

    public int getLikeCount() {
        return mLikeModel == null ? 0 : mLikeModel.getLikeCount();
    }

    public long getCreateTime() {
        return mCreateTime;
    }

    @Override
    public int getUiType() {
        return FeedItemUiType.UI_TYPE_VIEWER;
    }
}
