package com.wali.live.watchsdk.longtext.model;

import com.base.log.MyLog;
import com.wali.live.proto.Feeds.FeedInfo;
import com.wali.live.proto.Feeds.UGCFeed;
import com.wali.live.watchsdk.longtext.model.interior.BlogFeedModel;
import com.wali.live.watchsdk.longtext.model.interior.item.BaseFeedItemModel;

import java.util.List;

/**
 * Created by lan on 2017/9/20.
 */
public class LongTextModel extends BaseFeedInfoModel<BlogFeedModel> {
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
        return mUgcFeedModel.getItemList();
    }

    public String toPrint() {
        return mFeedId + ":" + mOwner.getUid() + ":" + mUgcFeedModel.toPrint();
    }
}
