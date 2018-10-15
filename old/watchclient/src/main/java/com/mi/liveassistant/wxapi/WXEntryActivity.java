package com.mi.liveassistant.wxapi;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.base.activity.BaseSdkActivity;
import com.base.log.MyLog;
import com.base.utils.toast.ToastUtils;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.account.event.AccountEventController;
import com.mi.live.data.api.ErrorCode;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.tencent.mm.sdk.constants.ConstantsAPI;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.thornbirds.component.EventController;
import com.wali.live.task.IActionCallBack;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.eventbus.EventClass;
import com.wali.live.watchsdk.income.auth.WXOAuth;
import com.wali.live.watchsdk.login.LoginPresenter;

import org.greenrobot.eventbus.EventBus;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 此Activity通过约定名字和包名与微信SDK实现了关联，一定不能随意重命名或挪动此类<br>
 *
 * Created by zhangyuheuan on 3/22/16.
 * @module 微信分享和登录的回调类
 */

public class WXEntryActivity extends BaseSdkActivity implements IWXAPIEventHandler, IActionCallBack {

    private static final String TAG = WXEntryActivity.class.getSimpleName();
    private String mCode;
    private WXOAuth mWXOAuth;
    private ExecutorService mExecutorService = Executors.newSingleThreadExecutor();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        MyLog.w(TAG, "wwe onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_wx);
        mWXOAuth = new WXOAuth();
        mWXOAuth.handleIntent(getIntent(), this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        MyLog.w(TAG, "onNewIntent");
        super.onNewIntent(intent);
        setIntent(intent);
        mWXOAuth.handleIntent(getIntent(), this);
    }

    @Override
    public void onReq(BaseReq req) {
        MyLog.w(TAG, "onReq req.getType = " + req.getType() + "scope =" + ((SendAuth.Req) req).scope);
    }

    @Override
    public void onResp(BaseResp resp) {
        MyLog.w(TAG, "wwe onResp resprrCode = " + resp.errCode);
        int result = 0;
        Bundle bundle = new Bundle();
        switch (resp.errCode) {
            case BaseResp.ErrCode.ERR_OK:
                if (resp.getType() == ConstantsAPI.COMMAND_SENDAUTH) {
                    resp.toBundle(bundle);
                    SendAuth.Resp sp = new SendAuth.Resp(bundle);
                    mCode = sp.code; //code
                    String state = sp.state;
                    MyLog.w(TAG, "mCode=" + mCode + " state:" + state);
                    /*
                    int from = LoginPresenter.REQUEST_MAIN_LOGIN;
                    if (WXOAuth.WEIXIN_REQ_FINDPWD_STATE.equals(state)) {
                        from = FindAccountPwdActivity.REQUEST_FIND_PWD; //200
                    } else if (WXOAuth.WEIXIN_REQ_LOGIN_STATE.equals(state)) {
                        from = LoginPresenter.REQUEST_LOGIN; //1000
                    } else if (WXOAuth.WEIXIN_REQ_MAIN_LOGIN_STATE.equals(state)) {
                        from = LoginPresenter.REQUEST_MAIN_LOGIN; //1001
                    } else if (WXOAuth.WEIXIN_REQ_GUIDE_STATE.equals(state)) {
                        from = GuidePresenter.REQUEST_WX_GUIDE; //1002
                    }*/
                    EventClass.OauthResultEvent event = new EventClass.OauthResultEvent(123, EventClass.OauthResultEvent.EVENT_TYPE_CODE, null, null, null, mCode, null);
                    EventBus.getDefault().post(event);
                }
        }
        finish();
    }

    @Override
    public void onDestroy() {
        MyLog.w(TAG, "onDestroy");
        super.onDestroy();
        mExecutorService.shutdown();
        mWXOAuth = null;
    }


    @Override
    public void processAction(String action, int errCode, Object... objects) {
        MyLog.w(TAG, "processAction : " + action + " , errCode : " + errCode);
        switch (action) {
            case MiLinkCommand.COMMAND_EXPLEVEL_UPDATE:
                if (errCode == ErrorCode.CODE_SUCCESS) {
                    MyLog.e(TAG, "COMMAND_EXPLEVEL_UPDATE explevel update success");
                } else {
                    MyLog.e(TAG, "COMMAND_EXPLEVEL_UPDATE explevel update failure");
                }
                break;
        }
    }

}