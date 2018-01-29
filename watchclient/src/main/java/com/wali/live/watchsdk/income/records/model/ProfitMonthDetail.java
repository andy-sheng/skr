package com.wali.live.watchsdk.income.records.model;

import com.base.utils.CommonUtils;
import com.wali.live.proto.PayProto;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhaomin on 17-6-27.
 */
public class ProfitMonthDetail {

    private long incomeProfit;

    private long costProfit;

    private List<PayProto.ProfitDayDetail> dayDetails;

    public ProfitMonthDetail(PayProto.ProfitRecordResponse rsp) {
        if (CommonUtils.isChinese()) {
            incomeProfit = rsp.getIncomeProfitRMB();
            costProfit = rsp.getCostProfitRMB();
        } else {
            incomeProfit = rsp.getIncomeProfitDollar();
            costProfit = rsp.getCostProfitDollar();
        }
        dayDetails = rsp.getProfitDayDetailsList();
    }

    public List<RecordsItem> parseToRecordsList() {
        if (dayDetails == null || dayDetails.isEmpty()) {
            return null;
        }
        List<RecordsItem> recordsItems = new ArrayList<>();
        for (PayProto.ProfitDayDetail detail : dayDetails) {
            recordsItems.add(new RecordsItem(true, detail.getDay()));
            for (PayProto.ProfitDayDetailInfo info : detail.getProfitDayDetailInfoList()) {
                recordsItems.add(new RecordsItem(info, detail.getDay()));
            }
        }
        return recordsItems;
    }

    public long getIncomeProfit() {
        return incomeProfit;
    }

    public long getCostProfit() {
        return costProfit;
    }
}
