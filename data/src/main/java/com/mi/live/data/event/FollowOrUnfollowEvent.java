package com.mi.live.data.event;

/**
 * 关注和取消关注的event
 */
public class FollowOrUnfollowEvent {
    public int eventType;
    public long uuid;
    public boolean isBothFollow;

    public static final int EVENT_TYPE_FOLLOW = 1;
    public static final int EVENT_TYPE_UNFOLLOW = 2;

    public FollowOrUnfollowEvent(int type, long uid) {
        this.eventType = type;
        this.uuid = uid;
    }
}