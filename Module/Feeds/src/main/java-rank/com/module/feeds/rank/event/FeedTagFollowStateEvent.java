package com.module.feeds.rank.event;

import com.module.feeds.watch.model.FeedRecommendTagModel;

public class FeedTagFollowStateEvent {
    FeedRecommendTagModel mFeedRecommendTagModel;

    public FeedTagFollowStateEvent(FeedRecommendTagModel feedRecommendTagModel) {
        mFeedRecommendTagModel = feedRecommendTagModel;
    }

    public FeedRecommendTagModel getFeedRecommendTagModel() {
        return mFeedRecommendTagModel;
    }
}
