package com.zq.live.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.common.core.R;
import com.common.core.pay.EPayPlatform;
import com.common.core.pay.event.PayResultEvent;
import com.common.core.share.ShareManager;
import com.common.log.MyLog;
import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.greenrobot.eventbus.EventBus;

public class WXPayEntryActivity extends Activity implements IWXAPIEventHandler {
    private static final String TAG = "MicroMsg.SDKSample.WXPayEntryActivity";

    private IWXAPI api;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.empty_activity_layout);

        api = WXAPIFactory.createWXAPI(this, ShareManager.WX_APP_ID);
        boolean success = api.handleIntent(getIntent(), this);
        MyLog.w(TAG, "handleIntent " + success);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        api.handleIntent(intent, this);
    }

    @Override
    public void onReq(BaseReq req) {

    }

    @Override
    public void onResp(BaseResp resp) {
        MyLog.d(TAG, "onPayFinish, errCode = " + resp.errCode);
        finish();
        if (resp.getType() == ConstantsAPI.COMMAND_PAY_BY_WX) {
            EventBus.getDefault().post(new PayResultEvent(EPayPlatform.WX_PAY, resp.errStr, resp.errCode));
        }
    }
}
