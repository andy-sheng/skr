package com.module.home.inter;

import com.module.home.model.ExChangeInfoModel;
import com.module.home.model.WithDrawInfoModel;

public interface IInComeView {
    void showCash(String availableBalance);

    void showDq(String dq);

    void showRule(ExChangeInfoModel exChangeInfoModel);

    void showWithDrawInfo(WithDrawInfoModel withDrawInfoModel);
}
