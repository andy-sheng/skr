package com.module.home.inter;

import com.module.home.model.WithDrawInfoModel;

public interface IWithDrawView {
    void showWithDrawInfo(WithDrawInfoModel withDrawInfoModel);

    void bindWxResult(boolean success);

    void withDraw(boolean success);
}
