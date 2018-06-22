package com.wali.live.watchsdk.personalcenter.relation.contact;

/**
 * Created by zhujianning on 18-6-21.
 */

public class FollowOptContact {

    public interface Iview {
        void followSuccess(long targetUid);

        void unFollowSuccess();
    }

    public interface Ipresenter {
        void follow(long targetUid);

        void unFollow(long targetUid);
    }
}
