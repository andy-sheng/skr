package com.module.home.inter;

import com.module.home.model.RechargeItemModel;
import com.module.home.model.WalletRecordModel;
import com.module.home.model.WithDrawInfoModel;

import java.util.List;

public interface IBallanceView {
    void rechargeFailed();

    void rechargeSuccess();

    void showRechargeList(List<RechargeItemModel> list);

    void sendOrder();

    void showBalance(long diamond);
}
