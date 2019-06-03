package com.common.webview;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Pair;
import android.view.View;
import android.webkit.WebView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.common.core.share.SharePanel;
import com.common.core.share.ShareType;
import com.common.log.MyLog;
import com.common.view.titlebar.CommonTitleBar;
import com.jsbridge.CallBackFunction;

import org.json.JSONException;
import org.json.JSONObject;

import static com.common.view.titlebar.CommonTitleBar.ACTION_LEFT_TEXT;
import static com.common.view.titlebar.CommonTitleBar.ACTION_RIGHT_BUTTON;
import static com.common.webview.JsBridgeImpl.getJsonObj;

/**
 * 注意！！运行在 :tools 进程，要使用 aidl 进行通信
 */
@Route(path = "/common/ExpendWebActivity")
public class ExpendWebActivity extends AgentWebActivity {
    public final static String TAG = "ExpendWebActivity";

    boolean mShowShareBtn = false;

    String mTitle;
    String mDes;
    String mIcon;
    String mUrl;

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        super.initData(savedInstanceState);

        mUrl = getIntent().getStringExtra("url");
        mShowShareBtn = getIntent().getBooleanExtra("showShare", true);

        if (!mShowShareBtn) {
            mTitlebar.getRightImageButton().setVisibility(View.GONE);
        }

        mTitlebar.setListener(new CommonTitleBar.OnTitleBarListener() {
            @Override
            public void onClicked(View v, int action, String extra) {

                if (action == ACTION_LEFT_TEXT) {
                    if (mBridgeWebView != null && mBridgeWebView.canGoBack()) {
                        mBridgeWebView.goBack();
                    } else {
                        finish();
                    }
                } else if (action == ACTION_RIGHT_BUTTON) {
                    SharePanel sharePanel = new SharePanel(ExpendWebActivity.this);
                    sharePanel.setShareContent(mIcon, mTitle, mDes, mUrl);
                    sharePanel.show(ShareType.URL);
                }
            }
        });
    }

    protected void pageFinished(WebView view, String url) {
        mBridgeWebView.callHandler("callJs", getJsonObj(new Pair("opt", "fetchPageShareInfo")).toJSONString(), new CallBackFunction() {
            @Override
            public void onCallBack(String data) {
                MyLog.d(TAG, "onCallBack" + " data=" + data);
                try {
                    JSONObject jsonObject = new JSONObject(data);
                    mTitle = jsonObject.getString("title");
                    mDes = jsonObject.getString("description");
                    mIcon = jsonObject.getString("icon");
                } catch (JSONException e) {
                    MyLog.d(TAG, e);
                }
            }
        });
    }

}
