package com.common.core.pay.wx;

import com.common.core.pay.PayBaseReq;
import com.common.core.pay.IPayApi;
import com.common.core.share.ShareManager;
import com.common.log.MyLog;
import com.common.utils.U;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

public class WxPayApi implements IPayApi {
    IWXAPI api;

    public WxPayApi() {
        api = WXAPIFactory.createWXAPI(U.app(), ShareManager.WX_APP_ID);
    }

    @Override
    public void pay(PayBaseReq resp) {
        WxPayReq wxPayResp = (WxPayReq) resp;
        api.registerApp(ShareManager.WX_APP_ID);
        api.sendReq(wxPayResp.getReq());
    }

    @Override
    public void release(){

    }
}
