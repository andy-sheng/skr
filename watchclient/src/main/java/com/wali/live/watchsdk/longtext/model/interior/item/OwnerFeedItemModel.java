package com.wali.live.watchsdk.longtext.model.interior.item;

import com.mi.live.data.user.User;
import com.wali.live.watchsdk.longtext.adapter.FeedItemUiType;

/**
 * Created by lan on 2017/9/20.
 */
public class OwnerFeedItemModel extends BaseFeedItemModel {
    private User mOwner;

    public OwnerFeedItemModel(User owner) {
        mOwner = owner;
    }

    public User getOwner() {
        return mOwner;
    }

    @Override
    public int getUiType() {
        return FeedItemUiType.UI_TYPE_OWNER;
    }
}
