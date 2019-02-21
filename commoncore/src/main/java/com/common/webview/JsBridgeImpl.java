package com.common.webview;

import android.text.TextUtils;

import com.alibaba.fastjson.JSONObject;
import com.common.base.BaseActivity;

import com.common.log.MyLog;
import com.jsbridge.CallBackFunction;


public class JsBridgeImpl {
    public final static String TAG = "JsBridgeImpl";
    BaseActivity mBaseActivity;

    public JsBridgeImpl(BaseActivity baseActivity) {
        mBaseActivity = baseActivity;
    }

    public void share(String data, CallBackFunction function){
        MyLog.w(TAG, "share" + " data=" + data + " function=" + function);
        JSONObject jsonObject = JSONObject.parseObject(data);

        if(jsonObject == null){
            MyLog.w(TAG, "share" + " jsonObject=null");
            return;
        }

        String url = jsonObject.getString("url");
        if(TextUtils.isEmpty(url)){
            MyLog.w(TAG, "share" + " url=empty");
            return;
        }

//        SharePanel sharePanel = new SharePanel(mBaseActivity);
//        sharePanel.setShareContent(url);
//        sharePanel.show(ShareType.URL);
    }
}
