package com.mi.live.data.event;

/**
 * 关注和取消关注的event
 */
public class FollowOrUnfollowEvent {
    public static final String FOLLOW_FROM_RANK_PAGE = "rank_page"; //从排行榜点的关注

    public static final int EVENT_TYPE_FOLLOW = 1;
    public static final int EVENT_TYPE_UNFOLLOW = 2;

    public int eventType;
    public long uuid;
    public boolean isBothFollow;
    public String source; // 从那里点击的关注, 打点需要

    public FollowOrUnfollowEvent(int type, long uid) {
        this.eventType = type;
        this.uuid = uid;
    }

    public FollowOrUnfollowEvent(int eventType, long uuid, String source) {
        this.eventType = eventType;
        this.uuid = uuid;
        this.source = source;
    }
}