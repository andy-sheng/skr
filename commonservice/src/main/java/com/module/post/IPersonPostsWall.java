package com.module.post;

public interface IPersonPostsWall {

    void getPosts(boolean flag);

    void getMorePosts();

    void setUserInfoModel(Object userInfoModel);

    void selected();

    void unselected(int reason);

    void stopPlay();

    boolean isHasMore();

    void destroy();
}
