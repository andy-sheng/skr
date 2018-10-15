package com.wali.live.watchsdk.longtext.model.interior;

import com.wali.live.proto.Feeds;
import com.wali.live.watchsdk.lit.recycler.viewmodel.BaseViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lan on 2017/9/21.
 */
public class FeedLikeModel extends BaseViewModel {
    private List<FeedLikeItem> mItemList;

    private int mLikeCount = 0;
    private boolean mMyselfLike = false;

    public FeedLikeModel(Feeds.FeedLikeContent protoLike) {
        parse(protoLike);
    }

    public void parse(Feeds.FeedLikeContent protoLike) {
        if (mItemList == null) {
            mItemList = new ArrayList();
        }
        for (Feeds.FeedLike feedLike : protoLike.getFeedLikeListList()) {
            if (feedLike != null) {
                mItemList.add(new FeedLikeItem(feedLike));
            }
        }

        mLikeCount = protoLike.getLikeCount();
        mMyselfLike = protoLike.getMyselfLike();
    }

    public int getLikeCount() {
        return mLikeCount;
    }

    public static class FeedLikeItem {
        private long mUid;
        private long mTs;
        private String mNickname;

        private FeedLikeItem(Feeds.FeedLike protoLikeItem) {
            parse(protoLikeItem);
        }

        public void parse(Feeds.FeedLike protoLikeItem) {
            mUid = protoLikeItem.getZuid();
            mTs = protoLikeItem.getTs();
            mNickname = protoLikeItem.getUserName();
        }
    }
}
