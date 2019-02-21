package com.common.webview;

import android.text.TextUtils;

import com.alibaba.fastjson.JSONObject;
import com.common.base.BaseActivity;

import com.common.core.share.SharePanel;
import com.common.core.share.ShareType;
import com.common.log.MyLog;
import com.jsbridge.CallBackFunction;


public class JsBridgeImpl {
    public final static String TAG = "JsBridgeImpl";
    BaseActivity mBaseActivity;

    public JsBridgeImpl(BaseActivity baseActivity) {
        mBaseActivity = baseActivity;
    }

    public void share(String data, CallBackFunction function) {
        MyLog.w(TAG, "share" + " data=" + data + " function=" + function);
        JSONObject jsonObject = JSONObject.parseObject(data);

        if (jsonObject == null) {
            MyLog.w(TAG, "share" + " jsonObject=null");
            return;
        }

        String type = jsonObject.getString("type");
        if (TextUtils.isEmpty(type)) {
            MyLog.w(TAG, "share" + " type=empty");
            return;
        }

        String url = jsonObject.getString("url");

        if ("image".equals(type)) {
            String icon = jsonObject.getString("icon");
            String des = jsonObject.getString("des");
            String title = jsonObject.getString("title");

            SharePanel sharePanel = new SharePanel(mBaseActivity);
            sharePanel.setShareContent(icon, title, des, url);
            sharePanel.show(ShareType.URL);
        } else if ("url".equals(type)) {
            SharePanel sharePanel = new SharePanel(mBaseActivity);
            sharePanel.setShareContent(url);
            sharePanel.show(ShareType.IMAGE_RUL);
        }
    }
}
