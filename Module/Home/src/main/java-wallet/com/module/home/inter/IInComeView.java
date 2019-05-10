package com.module.home.inter;

import com.module.home.model.ExChangeInfoModel;

public interface IInComeView {
    void showCash(String availableBalance);

    void showDq(String dq);

    void showRule(ExChangeInfoModel exChangeInfoModel);
}
