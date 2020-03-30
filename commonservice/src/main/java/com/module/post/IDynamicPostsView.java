package com.module.post;

import com.module.common.IBooleanCallback;

public interface IDynamicPostsView {

    void loadData(int clubId, IBooleanCallback callback);

    void loadMoreData(IBooleanCallback callback);

    void cancel();
    boolean isHasMore();

}
