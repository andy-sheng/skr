package com.wali.live.watchsdk.income;

import android.support.annotation.NonNull;

import com.wali.live.proto.MibiTicketProto;

/**
 * Created by rongzhisheng on 16-12-15.
 */

public class MibiExchange extends com.wali.live.income.Exchange {
    private String subtitle;//副标题
    private long beginTime;//限时商品，开始时间
    private long endTime;//限时商品，结束时间

    public MibiExchange(@NonNull MibiTicketProto.MibiExchange mibiExchange) {
        super(mibiExchange.getExchangeId(), mibiExchange.getMibiCnt(), mibiExchange.getTicketCnt(), mibiExchange.getGiveMibiCnt());
        subtitle = mibiExchange.getSubtitle();
        beginTime = mibiExchange.getBeginTime();
        endTime = mibiExchange.getEndTime();
    }

    public long getBeginTime() {
        return beginTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public String getSubtitle() {
        return subtitle;
    }
}
