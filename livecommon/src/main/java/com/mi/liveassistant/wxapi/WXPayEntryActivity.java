package com.mi.liveassistant.wxapi;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.base.utils.Constants;
import com.tencent.mm.sdk.constants.ConstantsAPI;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

public class WXPayEntryActivity extends Activity implements IWXAPIEventHandler {

    private static final String TAG = "WXPayEntryActivity";

    public static final String APP_ID;

    private IWXAPI api;

    static {
        if (Constants.isTestBuild) {
            APP_ID = "wxf703eba7b0387387";
        } else {
            APP_ID = "wx0b1f5dd377f1cc6c";
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        api = WXAPIFactory.createWXAPI(this, APP_ID);
        api.registerApp(APP_ID);
        api.handleIntent(getIntent(), this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        api.handleIntent(intent, this);
    }


    @Override
    public void onReq(BaseReq baseReq) {

    }

    @Override
    public void onResp(BaseResp resp) {
        /*
         因为微信支付和微信登录用的两个 libammsdk.jar 冲突，这里直接将常量的值拿来使用
		 */
        if (resp.getType() == ConstantsAPI.COMMAND_PAY_BY_WX) {
            Intent intent = new Intent("com.xiaomi.gamecenter.wxpay");
            intent.putExtra("errCode", resp.errCode + "");
            intent.putExtra("errStr", resp.errStr);
            sendBroadcast(intent);
        }
        finish();
    }
}