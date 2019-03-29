package com.common.webview;

import com.alibaba.fastjson.JSONObject;
import com.common.base.BaseActivity;
import com.common.base.BuildConfig;
import com.common.log.MyLog;
import com.common.utils.U;
import com.jsbridge.BridgeHandler;
import com.jsbridge.BridgeWebView;
import com.jsbridge.CallBackFunction;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;

public class JsRegister {
    public final static String TAG = "JsRegister";
    public static final String OPT = "opt";
    public static final String DATA = "data";

    public static final String SHARE = "share";
    public static final String BIND_WE_CHAT = "bindWeChat";
    public static final String BIND_QQ = "bindQqChat";
    public static final String BIND_GET_VERSION = "getAppVersion";
    public static final String GET_CLIP_BOARD = "getClipboard";
    public static final String AUTH_SUCCESS = "authSuccess";
    public static final String FINISH = "finish";
    public static final String CHECK_CAMERA_PERM = "checkCameraPerm";

    BridgeWebView mBridgeWebView;

    JsBridgeImpl mJsBridgeImpl;

    BaseActivity mBaseActivity;

    public JsRegister(BridgeWebView bridgeWebView, BaseActivity baseActivity) {
        mBridgeWebView = bridgeWebView;
        mBaseActivity = baseActivity;
        mJsBridgeImpl = new JsBridgeImpl(baseActivity);
    }

    public void register() {
        //所有H5来的需要跳转的都从这里
        mBridgeWebView.registerHandler("callNative", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                processOpt(data, function);
            }
        });
    }

    private void processOpt(String data, CallBackFunction callBackFunction) {
        MyLog.w(TAG, "processOpt" + " data is =" + data);
        JSONObject jsonObject = JSONObject.parseObject(data);
        String opt = jsonObject.getString(OPT);
        String paramData = jsonObject.getString(DATA);

        if (SHARE.equals(opt)) {
            mJsBridgeImpl.share(paramData, callBackFunction);
        } else if (BIND_WE_CHAT.equals(opt)) {
            mJsBridgeImpl.bindWeChat(paramData, callBackFunction);
        } else if (BIND_QQ.equals(opt)) {
            mJsBridgeImpl.bindQqChat(paramData, callBackFunction);
        } else if (BIND_GET_VERSION.equals(opt)) {
            mJsBridgeImpl.getAppVersion(paramData, callBackFunction);
        } else if (GET_CLIP_BOARD.equals(opt)) {
            mJsBridgeImpl.getClipboard(callBackFunction);
        } else if (AUTH_SUCCESS.equals(opt)) {
            mJsBridgeImpl.authSuccess(callBackFunction);
        } else if (FINISH.equals(opt)) {
            mJsBridgeImpl.finish(callBackFunction);
        } else if (CHECK_CAMERA_PERM.equals(opt)) {
            mJsBridgeImpl.checkCameraPerm(callBackFunction);
        } else {
            mJsBridgeImpl.noMethed(callBackFunction);
        }
    }
}
