package com.common.webview;

import android.util.Log;

import com.common.base.BaseActivity;
import com.common.log.MyLog;
import com.common.utils.U;
import com.github.lzyzsd.jsbridge.BridgeHandler;
import com.github.lzyzsd.jsbridge.BridgeWebView;
import com.github.lzyzsd.jsbridge.CallBackFunction;

public class JsRegister {
    public final static String TAG = "JsRegister";
    BridgeWebView mBridgeWebView;

    JsBridgeImpl mJsBridgeImpl;

    BaseActivity mBaseActivity;

    public JsRegister(BridgeWebView bridgeWebView, BaseActivity baseActivity) {
        mBridgeWebView = bridgeWebView;
        mBaseActivity = baseActivity;
        mJsBridgeImpl = new JsBridgeImpl(baseActivity);
    }

    public void register(){
        //所有H5来的需要跳转的都从这里
        mBridgeWebView.registerHandler("openSchema", new BridgeHandler() {
            @Override
            public void handler(String schema, CallBackFunction function) {
                MyLog.w(TAG, "handler" + " schema=" + schema);
                mJsBridgeImpl.openSchema(schema);
            }
        });

        mBridgeWebView.registerHandler("share", new BridgeHandler() {
            @Override
            public void handler(String schema, CallBackFunction function) {

            }
        });

//        mBridgeWebView.callHandler("callJs", "你好，js", new CallBackFunction() {
//            @Override
//            public void onCallBack(String data) {
//                Log.i(TAG,"data:"+data);
//            }
//        });
    }
}
