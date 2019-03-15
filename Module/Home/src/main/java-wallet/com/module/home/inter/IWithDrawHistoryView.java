package com.module.home.inter;

import com.module.home.model.WithDrawHistoryModel;

import java.util.List;

public interface IWithDrawHistoryView {
    void update(List<WithDrawHistoryModel> withDrawHistoryModelList);

    void hasMore(boolean hasMore);
}
