package com.module.home.inter;

import com.module.home.model.ExChangeInfoModel;

public interface IExchangeDiamomdView {
    void exChangeSuccess();

    void exChangeFailed(String errorMsg);

    void showDQ(ExChangeInfoModel exChangeInfoModel);
}
