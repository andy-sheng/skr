package com.common.webview;

import android.text.TextUtils;
import android.util.Pair;

import com.alibaba.fastjson.JSONObject;
import com.common.base.BaseActivity;

import com.common.core.share.SharePanel;
import com.common.core.share.ShareType;
import com.common.log.MyLog;
import com.jsbridge.CallBackFunction;
import com.umeng.socialize.UMAuthListener;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.bean.SHARE_MEDIA;

import java.util.Map;


public class JsBridgeImpl {
    public final static String TAG = "JsBridgeImpl";
    BaseActivity mBaseActivity;

    public JsBridgeImpl(BaseActivity baseActivity) {
        mBaseActivity = baseActivity;
    }

    public void share(String data, CallBackFunction function) {
        MyLog.w(TAG, "share" + " data=" + data);
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

        if ("url".equals(type)) {
            String icon = jsonObject.getString("icon");
            String des = jsonObject.getString("des");
            String title = jsonObject.getString("title");

            SharePanel sharePanel = new SharePanel(mBaseActivity);
            sharePanel.setShareContent(icon, title, des, url);
            sharePanel.show(ShareType.URL);
        } else if ("image".equals(type)) {
            SharePanel sharePanel = new SharePanel(mBaseActivity);
            sharePanel.setShareContent(url);
            sharePanel.show(ShareType.IMAGE_RUL);
        }

        function.onCallBack(getJsonObj(new Pair("errcode", "0"), new Pair("errmsg", ""), new Pair("data", "{}")));
    }

    public void bindWeChat(String data, final CallBackFunction function) {
        MyLog.w(TAG, "bindWeChat" + " data=" + data);
        UMShareAPI.get(mBaseActivity).doOauthVerify(mBaseActivity, SHARE_MEDIA.QQ, new UMAuthListener() {
            @Override
            public void onStart(SHARE_MEDIA share_media) {

            }

            @Override
            public void onComplete(SHARE_MEDIA share_media, int i, Map<String, String> data) {
                String accessToken = data.get("access_token");
                String openid = data.get("openid");
                function.onCallBack(getJsonObj(new Pair("errcode", "0"), new Pair("errmsg", ""),
                        new Pair("data", getJsonObj(new Pair("access_token", accessToken), new Pair("open_id", openid)))));
            }

            @Override
            public void onCancel(SHARE_MEDIA share_media, int i) {
                function.onCallBack(getJsonObj(new Pair("errcode", "1"), new Pair("errmsg", "取消授权"),
                        new Pair("data", getJsonObj(new Pair("access_token", ""), new Pair("open_id", "")))));
            }

            @Override
            public void onError(SHARE_MEDIA share_media, int i, Throwable throwable) {
                function.onCallBack(getJsonObj(new Pair("errcode", "2"), new Pair("errmsg", throwable.getMessage()),
                        new Pair("data", getJsonObj(new Pair("access_token", ""), new Pair("open_id", "")))));
            }
        });
    }

    public static String getJsonObj(Pair<String, String>... pairArray){
        JSONObject jsonObject = new JSONObject();
        for (Pair<String, String> pair : pairArray){
            jsonObject.put(pair.first, pair.second);
        }

        return jsonObject.toJSONString();
    }
}
