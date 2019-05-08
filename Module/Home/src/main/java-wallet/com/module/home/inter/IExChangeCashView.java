package com.module.home.inter;

import com.module.home.model.ExChangeInfoModel;

public interface IExChangeCashView {
    void showExChangeInfo(ExChangeInfoModel exChangeInfoModel);

    void exChangeSuccess();

    void exChangeFailed(String errorMsg);
}
