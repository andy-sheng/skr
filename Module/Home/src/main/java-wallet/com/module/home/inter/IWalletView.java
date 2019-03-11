package com.module.home.inter;

import com.module.home.model.WalletRecordModel;

import java.util.List;

public interface IWalletView {

    void onGetBalanceSucess(String availableBalance, String lockedBalance);

    void onGetIncrRecords(int offset, List<WalletRecordModel> list);
}
