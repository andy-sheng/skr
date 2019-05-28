package com.module.home.inter;

import com.common.core.pay.PayBaseReq;
import com.module.home.model.RechargeItemModel;
import com.module.home.model.WalletRecordModel;
import com.module.home.model.WithDrawInfoModel;

import java.util.List;

public interface IBallanceView {
    void rechargeFailed(String errorMsg);

    void rechargeSuccess();

    void showRechargeList(List<RechargeItemModel> list);

    void sendOrder(PayBaseReq payBaseResp);

    void showBalance(String diamond);
}
