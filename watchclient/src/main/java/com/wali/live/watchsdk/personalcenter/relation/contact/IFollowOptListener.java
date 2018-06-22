package com.wali.live.watchsdk.personalcenter.relation.contact;

/**
 * Created by zhujianning on 18-6-21.
 */

public interface IFollowOptListener {
    void follow(long targetId);

    void unFollow(long targetId);
}
