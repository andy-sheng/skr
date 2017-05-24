package com.wali.live.recharge.payway;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.wali.live.pay.model.Diamond;
import com.wali.live.proto.PayProto;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rongzhisheng on 16-12-31.
 */

public abstract class ChinaPay implements IPayWay {

    @Override
    public List<Diamond> parseGemPriceResponse(@NonNull PayProto.GetGemPriceResponse rsp) {
        List<Diamond> priceList = new ArrayList<>();
        for (PayProto.GemGoods goods : rsp.getGemGoodsListList()) {
            priceList.add(Diamond.parse(goods));
        }
        return priceList;
    }

    @Override
    public void consumeHoldProduct() {

    }

    @Override
    public boolean postHandleAfterCheckOrder(String receipt) {
        return false;
    }

    @Override
    public void handlePayResult(int resultCode, Intent intent) {

    }

    @Override
    public void onExitRecharge(@NonNull Activity activity) {

    }

}
