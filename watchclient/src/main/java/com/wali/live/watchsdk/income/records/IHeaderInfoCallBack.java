package com.wali.live.watchsdk.income.records;


import com.wali.live.watchsdk.income.records.model.RecordsItem;

/**
 * Created by zhaomin on 17-6-27.
 */
public interface IHeaderInfoCallBack {

    RecordsItem getInfo(int position);

    /**
     * 如果是今天  那么要显示一个文案。。。
     * @param position
     * @return
     */
    boolean isTodayAndHasIncome(int position);
}
