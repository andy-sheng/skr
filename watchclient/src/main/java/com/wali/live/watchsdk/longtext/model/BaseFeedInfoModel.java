package com.wali.live.watchsdk.longtext.model;

import com.base.log.MyLog;
import com.mi.live.data.user.User;
import com.wali.live.proto.Feeds;
import com.wali.live.proto.Feeds.FeedContent;
import com.wali.live.proto.Feeds.FeedInfo;
import com.wali.live.proto.Feeds.UGCFeed;
import com.wali.live.proto.LiveShowProto;
import com.wali.live.watchsdk.lit.recycler.viewmodel.BaseViewModel;
import com.wali.live.watchsdk.longtext.model.interior.FeedLikeModel;
import com.wali.live.watchsdk.longtext.model.interior.UgcFeedModel;

/**
 * Created by lan on 2017/9/20.
 *
 * @notice 此model有很多类型，注意类型的归属
 */
public abstract class BaseFeedInfoModel<UFM extends UgcFeedModel> extends BaseViewModel {
    protected String mFeedId;
    protected long mCreateTime;
    protected long mOwnerId;

    protected int mFeedType;
    protected UFM mUgcFeedModel;

    protected FeedLikeModel mLikeModel;

    protected User mOwner;

    public BaseFeedInfoModel(FeedInfo protoInfo) {
        parse(protoInfo);
    }

    private void parse(FeedInfo protoInfo) {
        mFeedId = protoInfo.getFeedId();
        mCreateTime = protoInfo.getFeedCteateTime();
        mOwnerId = protoInfo.getUserId();

        parseFeedContent(protoInfo.getFeedContent());
        parseFeedLike(protoInfo.getFeedLikeContent());
        parseUser(protoInfo.getUserShow());
    }

    private void parseFeedContent(FeedContent protoContent) {
        if (protoContent == null) {
            MyLog.e(TAG, "parseFeedContent protoContent is null");
            return;
        }
        mFeedType = protoContent.getFeedType();
        parseUgcFeed(protoContent.getUgcFeed());
    }

    protected abstract void parseUgcFeed(UGCFeed protoUgcFeed);

    private void parseFeedLike(Feeds.FeedLikeContent protoLike) {
        if (protoLike == null) {
            MyLog.e(TAG, "parseFeedContent protoLike is null");
            return;
        }
        mLikeModel = new FeedLikeModel(protoLike);
    }

    private void parseUser(LiveShowProto.UserShow userShow) {
        if (userShow == null) {
            MyLog.e(TAG, "parseUser userShow is null");
            return;
        }
        mOwner = new User(userShow.getUId(), userShow.getAvatar(), userShow.getNickname());
        mOwner.setGender(userShow.getGender());
        mOwner.setLevel(userShow.getLevel());
        mOwner.setCertificationType(userShow.getCertType());
    }
}
