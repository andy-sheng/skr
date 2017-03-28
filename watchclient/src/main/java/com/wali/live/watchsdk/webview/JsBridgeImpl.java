package com.wali.live.watchsdk.webview;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.webkit.WebBackForwardList;
import android.webkit.WebView;

import com.base.activity.BaseSdkActivity;
import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.utils.display.DisplayUtils;
import com.base.utils.version.VersionManager;
import com.mi.live.data.account.MyUserInfoManager;
import com.mi.live.data.user.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by yangli on 16-6-21.
 */
public class JsBridgeImpl {
    private static final String TAG = "JsBridgeImpl";

    private String mCurrUrl = null;
    private boolean mIsDebug = false;
    private List<String> noHistory = new ArrayList();

    private InjectedWebViewClient.BridgeHandler mBridgeHandler;

    private BaseSdkActivity mActivity;

    private String callBackId = "";
    private WebView webView;
    private boolean immediateExit = false;

    public JsBridgeImpl(InjectedWebViewClient.BridgeHandler bridgeHandler, BaseSdkActivity activity) {
        mBridgeHandler = bridgeHandler;
        this.mActivity = activity;
    }

    public void updateCurrUrl(String url) {
        mCurrUrl = url != null ? url : "";
        mIsDebug = mCurrUrl.contains("debug=1");
    }

    private void setResultToJs(final WebView webView, JSONObject result) {
        if (mBridgeHandler != null) {
            Message msg = mBridgeHandler.obtainMessage(
                    InjectedWebViewClient.BridgeHandler.MSG_SET_RESULT_TO_JS,
                    webView);
            Bundle data = msg.getData();
            data.putString(JsBridgeProtocol.EXTRA_KEY_RESULT, result.toString());
            mBridgeHandler.sendMessage(msg);
        }
    }

    public void show_log(final WebView webView, String msgType, String callBackId, JSONObject params) {
        int level = params.optInt("level", 1);
        String logText = params.optString("text", "");
        if (!TextUtils.isEmpty(logText)) {
            switch (level) {
                case 0:
                    MyLog.e(TAG, "show_log: " + logText);
                    break;
                default:
                    MyLog.w(TAG, "show_log: " + logText);
                    break;
            }
        } else {
            MyLog.w(TAG, "show_log but logText is empty");
        }
    }

    public void get_session_data(final WebView webView, String msgType, String callBackId, JSONObject params) {
        MyLog.w(TAG, "get_session_data msgType=" + msgType + ", callBackId=" + callBackId + ", params=" + params);
        if (webView == null || params == null) {
            return;
        }
        try {
            JSONObject result = new JSONObject();
            result.put(JsBridgeProtocol.MSG_TYPE, JsBridgeProtocol.MSG_TYPE_CALLBACK);
            result.put(JsBridgeProtocol.CALLBACK_ID, callBackId);
            JSONObject sessionInfo = new JSONObject();
            result.put(JsBridgeProtocol.CALLBACK_PARAMS, sessionInfo);

            // 上传环境变量
            JSONObject envInfo = new JSONObject();
            sessionInfo.put("env", envInfo);
            envInfo.put("bid", "##");
            envInfo.put("carrier", "##");
            envInfo.put("cid", VersionManager.getReleaseChannel(GlobalData.app().getApplicationContext()));
            envInfo.put("co", Locale.getDefault().getCountry());
            envInfo.put("density", DisplayUtils.getDensity());
            envInfo.put("imei", "##");
            envInfo.put("imei_md5", "##");
            envInfo.put("isDebug", mIsDebug);
            envInfo.put("la", Locale.getDefault().getLanguage());
            envInfo.put("mnc", "##");
            // TODO
//            envInfo.put("os", StreamerUtils.getOsInfo());
            envInfo.put("platform", "android");
            envInfo.put("sdk", Build.VERSION.SDK_INT);
            envInfo.put("stampTime", System.currentTimeMillis());
            // TODO
//            envInfo.put("ua", StreamerUtils.getModelInfo());
            envInfo.put("versionCode", VersionManager.getCurrentVersionCode(GlobalData.app().getApplicationContext()));
            envInfo.put("vn", "##");

            // 上传用户信息
            JSONObject userInfo = new JSONObject();
            sessionInfo.put("userInfo", userInfo);
            User user = MyUserInfoManager.getInstance().getUser();
            if (user != null) {
                userInfo.put("avatar", user.getAvatar());
                userInfo.put("fuid", user.getUid());
                userInfo.put("nickName", user.getNickname());
                userInfo.put("token", "##");
                userInfo.put("uid", user.getUid());
            }

            // 上传统计数据
            JSONObject reportInfo = new JSONObject();
            sessionInfo.put("report", reportInfo);
            reportInfo.put("channel", "##");
            reportInfo.put("curPage", mCurrUrl);
            reportInfo.put("from", "##");
            reportInfo.put("fromId", "##");
            reportInfo.put("fromLabel", "##");
            reportInfo.put("moduleId", "##");
            reportInfo.put("position", "##");

            setResultToJs(webView, result);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void get_geolocation(final WebView webView, String msgType, String callBackId, JSONObject params) {
        MyLog.w(TAG, "get_geolocation msgType=" + msgType + ", callBackId=" + callBackId + ", params=" + params);
        if (webView == null) {
            return;
        }
        webView.getSettings().setGeolocationEnabled(true);
    }

    /**
     * 半屏webview 隐藏loading图
     */
    public void hide_loading(final WebView webView, String msgType, String callBackId, JSONObject params) {
        MyLog.w(TAG, "hide_loading msgType=" + msgType + ", callBackId=" + callBackId + ", params=" + params);
        if (webView == null) {
            return;
        }
        setSuccessCallback();
        // TODO
//        EventBus.getDefault().post(new EventClass.LoadingEndEvent());
    }

    /**
     * 半屏webview 关闭
     */
    public void close_webview(final WebView webView, String msgType, String callBackId, JSONObject params) {
        MyLog.w(TAG, "close_webview msgType=" + msgType + ", callBackId=" + callBackId + ", params=" + params);
        if (webView == null) {
            return;
        }
        // TODO
//        EventBus.getDefault().post(new EventClass.CloseWebEvent());
    }

    /**
     * 本地浏览器打开
     *
     * @param webView
     * @param msgType
     * @param callBackId
     * @param params
     */
    public void openInBrowser(final WebView webView, String msgType, String callBackId, JSONObject params) {
        this.callBackId = callBackId;
        this.webView = webView;
        try {
            String mUrl = (String) params.get("url");
            if (!TextUtils.isEmpty(mUrl)) {
                Intent intent = new Intent();
                if (!mUrl.startsWith("http://")) {
                    mUrl = "http://".concat(mUrl);
                }
                intent.setAction("android.intent.action.VIEW");
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                Uri content_url = Uri.parse(mUrl);
                intent.setData(content_url);
                setSuccessCallback();
                mActivity.startActivity(intent);
            } else {
                setFailCallback();
                MyLog.e(TAG, "openInBrowser url is null");
            }
        } catch (JSONException e) {
            setFailCallback();
            e.printStackTrace();
        }

    }


    /**
     * 使用本地播放器播放视频
     *
     * @param webView
     * @param msgType
     * @param callBackId
     * @param params
     */
    public void video_play(final WebView webView, String msgType, String callBackId, JSONObject params) {
        this.callBackId = callBackId;
        this.webView = webView;
        try {
            String mUrl = (String) params.get("url");
            if (!TextUtils.isEmpty(mUrl)) {
                Uri uri = Uri.parse(mUrl);
                //调用系统自带的播放器
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(uri, "video/mp4");
                setSuccessCallback();
                mActivity.startActivity(intent);
            } else {
                setFailCallback();
                MyLog.e(TAG, "video_play url is null");
            }
        } catch (JSONException e) {
            setFailCallback();
            e.printStackTrace();
        }
    }

    /**
     * 复制文本
     *
     * @param webView
     * @param msgType
     * @param callBackId
     * @param params
     */
    public void copy(final WebView webView, String msgType, String callBackId, JSONObject params) {
        this.callBackId = callBackId;
        this.webView = webView;

        try {
            String copy = (String) params.get("text");
            if (!TextUtils.isEmpty(copy)) {
                ClipboardManager cm = (ClipboardManager) mActivity.getSystemService(Context.CLIPBOARD_SERVICE);
                cm.setPrimaryClip(ClipData.newPlainText(null, copy));
                setSuccessCallback();
            } else {
                setFailCallback();
            }
        } catch (JSONException e) {
            setFailCallback();
            e.printStackTrace();
        }
    }

    /**
     * 图片预览功能
     *
     * @param webView
     * @param msgType
     * @param callBackId
     * @param params
     */
    public void view_img_list(final WebView webView, String msgType, String callBackId, JSONObject params) {
        this.callBackId = callBackId;
        this.webView = webView;

        try {

            JSONArray jsonArray = params.getJSONArray("images");
            int curIndex = (Integer) params.get("curIndex");
            List<String> list = new ArrayList();
            for (int i = 0; i < jsonArray.length(); i++) {
                list.add(jsonArray.get(i).toString());
            }
            setSuccessCallback();
        } catch (JSONException e) {
            setFailCallback();
            e.printStackTrace();
        }
    }

    /**
     * 关闭浏览器
     *
     * @param webView
     * @param msgType
     * @param callBackId
     * @param params
     */
    public void history_jumpout_webview(final WebView webView, String msgType, String callBackId, JSONObject params) {
        this.callBackId = callBackId;
        this.webView = webView;
        immediateExit = true;
        setSuccessCallback();
    }

    public void history_not_records(final WebView webView, String msgType, String callBackId, JSONObject params) {
        this.callBackId = callBackId;
        this.webView = webView;
        WebBackForwardList list = webView.copyBackForwardList();
        noHistory.add(list.getCurrentItem().getUrl());
        setSuccessCallback();
    }

    public void goBackIndex(WebView webView) {

        if (immediateExit) {
            mActivity.finish();
            return;
        }

        if (noHistory.isEmpty()) {
            if (webView.canGoBack()) {
                webView.goBack();
            } else {
                mActivity.finish();
            }
        } else {
            WebBackForwardList list = webView.copyBackForwardList();
            if (list.getSize() > 1) {
                int i = list.getCurrentIndex() - 1;

                if (i >= 0 && !noHistory.contains(list.getItemAtIndex(i).getUrl())) {
                    if (webView.canGoBack()) {
                        webView.goBack();
                    } else {
                        mActivity.finish();
                    }
                } else {

                    noHistory.remove(list.getItemAtIndex(i).getUrl());
                    int newi = searchIndex(i - 1, list);
                    webView.goBackOrForward(newi - i - 1);

                }
            } else {
                mActivity.finish();
            }
        }

    }

    private int searchIndex(int i, WebBackForwardList list) {
        if (noHistory.isEmpty()) {
            return i;
        }

        for (; i > 0; i--) {
            if (!noHistory.isEmpty() && noHistory.contains(list.getItemAtIndex(i).getUrl())) {
                noHistory.remove(list.getItemAtIndex(i).getUrl());
            } else {
                return i;
            }
        }
        return i;
    }

    /**
     * 浏览器回退
     *
     * @param webView
     * @param msgType
     * @param callBackId
     * @param params
     */
    public void web_go_back(final WebView webView, String msgType, String callBackId, JSONObject params) {
        this.callBackId = callBackId;
        this.webView = webView;
        boolean goBack = webView.canGoBack();
        if (goBack) {
            goBackIndex(webView);
            setSuccessCallback();
        } else {
            setFailCallback();
        }
    }

    /**
     * H5回调：成功
     */
    private void setSuccessCallback() {
        JSONObject result = new JSONObject();
        try {
            result.put(JsBridgeProtocol.MSG_TYPE, JsBridgeProtocol.MSG_TYPE_CALLBACK);
            result.put(JsBridgeProtocol.CALLBACK_ID, callBackId);
            JSONObject sessionInfo = new JSONObject();
            result.put(JsBridgeProtocol.CALLBACK_PARAMS, sessionInfo);
            sessionInfo.put("status", "0");
            if (webView != null) {
                setResultToJs(webView, result);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * H5回调：失败
     */
    private void setFailCallback() {
        JSONObject result = new JSONObject();
        try {
            result.put(JsBridgeProtocol.MSG_TYPE, JsBridgeProtocol.MSG_TYPE_CALLBACK);
            result.put(JsBridgeProtocol.CALLBACK_ID, callBackId);
            JSONObject sessionInfo = new JSONObject();
            result.put(JsBridgeProtocol.CALLBACK_PARAMS, sessionInfo);
            sessionInfo.put("status", "-1");
            if (webView != null) {
                setResultToJs(webView, result);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void shareCallBack(int status, String type) {
        JSONObject result = new JSONObject();
        try {
            result.put(JsBridgeProtocol.MSG_TYPE, JsBridgeProtocol.MSG_TYPE_CALLBACK);
            result.put(JsBridgeProtocol.CALLBACK_ID, callBackId);
            JSONObject sessionInfo = new JSONObject();
            result.put(JsBridgeProtocol.CALLBACK_PARAMS, sessionInfo);
            sessionInfo.put("status", status);
            sessionInfo.put("type", type);
            if (webView != null) {
                setResultToJs(webView, result);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void onDestroy() {
        mActivity = null;
    }
}
