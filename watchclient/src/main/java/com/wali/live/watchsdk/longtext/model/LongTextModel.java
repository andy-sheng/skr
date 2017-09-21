package com.wali.live.watchsdk.longtext.model;

import com.base.log.MyLog;
import com.wali.live.proto.Feeds.FeedInfo;
import com.wali.live.proto.Feeds.UGCFeed;
import com.wali.live.watchsdk.longtext.model.interior.BlogFeedModel;
import com.wali.live.watchsdk.longtext.model.interior.item.BaseFeedItemModel;
import com.wali.live.watchsdk.longtext.model.interior.item.OwnerFeedItemModel;
import com.wali.live.watchsdk.longtext.model.interior.item.ViewerFeedItemModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lan on 2017/9/20.
 */
public class LongTextModel extends BaseFeedInfoModel<BlogFeedModel> {
    private List<BaseFeedItemModel> mDataList;

    private OwnerFeedItemModel mOwnerItem;
    private ViewerFeedItemModel mViewerItem;

    public LongTextModel(FeedInfo protoInfo) {
        super(protoInfo);
    }

    @Override
    protected void parseUgcFeed(UGCFeed protoUgcFeed) {
        try {
            mUgcFeedModel = new BlogFeedModel(protoUgcFeed);
        } catch (Exception e) {
            MyLog.e(TAG, e);
        }
    }

    public List<BaseFeedItemModel> getDataList() {
        if (mUgcFeedModel == null) {
            MyLog.e(TAG, "getDataList ugcFeedModel is null");
            return null;
        }

        if (mDataList == null) {
            mDataList = new ArrayList();
            mDataList.add(mUgcFeedModel.getCoverItem());

            OwnerFeedItemModel ownerItem = getOwnerItem();
            if (ownerItem != null) {
                mDataList.add(ownerItem);
            }

            mDataList.add(mUgcFeedModel.getTitleItem());

            ViewerFeedItemModel viewerItem = getViewerItem();
            if (viewerItem != null) {
                mDataList.add(viewerItem);
            }

            mDataList.addAll(mUgcFeedModel.getItemList());
        }
        return mDataList;
    }

    private OwnerFeedItemModel getOwnerItem() {
        if (mOwner == null) {
            return null;
        }
        if (mOwnerItem == null) {
            mOwnerItem = new OwnerFeedItemModel(mOwner);
        }
        return mOwnerItem;
    }

    public ViewerFeedItemModel getViewerItem() {
        if (mUgcFeedModel == null) {
            return null;
        }
        if (mViewerItem == null) {
            mViewerItem = new ViewerFeedItemModel(mUgcFeedModel.getViewerCount(), mLikeModel, mCreateTime);
        }
        return mViewerItem;
    }

    public String toPrint() {
        return mFeedId + ":" + mOwner.getUid() + ":" + mUgcFeedModel.toPrint();
    }
}
