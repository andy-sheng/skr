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
        mBridgeWebView.registerHandler("callNative", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                if(BuildConfig.DEBUG){
                    U.getToastUtil().showShort("callNative data is:" + data);
                }
                processOpt(data, function);
            }
        });

//        Observable.timer(5000, TimeUnit.MILLISECONDS).subscribe(new Consumer<Long>() {
//            @Override
//            public void accept(Long aLong) throws Exception {
//                mBridgeWebView.callHandler("callJs", "你好，js", new CallBackFunction() {
//                    @Override
//                    public void onCallBack(String data) {
////                Log.i(TAG,"data:"+data);
//                        U.getToastUtil().showShort("callJs onCallBack data is:" + data);
//                    }
//                });
//            }
//        });
    }

    private void processOpt(String data, CallBackFunction callBackFunction){
        MyLog.w(TAG, "processOpt" + " data is =" + data);
        JSONObject jsonObject = JSONObject.parseObject(data);
        String opt = jsonObject.getString(OPT);
        String paramData = jsonObject.getString(DATA);

        if(SHARE.equals(opt)){
            mJsBridgeImpl.share(paramData, callBackFunction);
        }
    }
}
