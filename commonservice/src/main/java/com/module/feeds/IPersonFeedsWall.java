package com.module.feeds;

public interface IPersonFeedsWall {

    void getFeeds(boolean flag);

    void getMoreFeeds();

    void setUserInfoModel(Object userInfoModel);

    void selected();

    void unselected();

    boolean isHasMore();

    void destroy();
}
