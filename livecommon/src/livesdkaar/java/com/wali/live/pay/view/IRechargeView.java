package com.wali.live.pay.view;

import android.app.Activity;
import android.support.annotation.MainThread;
import android.support.annotation.StringRes;

import com.base.view.LoadDataView;
import com.wali.live.pay.model.Diamond;

import java.util.List;

/**
 * Created by rongzhisheng on 16-7-1.
 */
public interface IRechargeView extends LoadDataView {
    void showProcessDialog(long most, @StringRes int strId);

    void hideProcessDialog(long least);

    void setBalanceText(int balance, int vBalance);

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

    void updateBalanceAreaData();

}
