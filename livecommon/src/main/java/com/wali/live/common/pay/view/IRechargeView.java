package com.wali.live.common.pay.view;

import android.app.Activity;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;

import com.base.view.LoadDataView;
import com.wali.live.common.pay.manager.PayManager;
import com.wali.live.common.pay.model.Diamond;

import java.util.List;

/**
 * Created by rongzhisheng on 16-7-1.
 */
public interface IRechargeView extends LoadDataView, PayManager.PullRechargeListIface {
    void showProcessDialog(long most);

    void hideProcessDialog(long least);

    void showToast(@NonNull String msg);

    String getPackageName0();

    void setBalanceText(int balance,int vBalance);

    @MainThread
    void setRecyclerViewAdapterDataSourceAndNotify(List<Diamond> diamonds);

    void setRecyclerViewLoadingStatusAndNotify();

    void updateExchangeableAndWillExpireDiamond(int exchangeableDiamondCnt, int willExpireDiamondCnt
            , int willExpireGiftCardCnt);

    Activity getActivity();

    boolean isFirstRecharge();

    //boolean existExpandedPayWay();
    //
    //void setExpandableListAdapterDataSourceAndNotify(List<Diamond> diamonds);
    //
    //void setExpandableListLoadingStatusAndNotify();

    void showPopupWindow();

}
