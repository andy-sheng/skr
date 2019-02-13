package com.common.webview;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.common.core.share.SharePanel;
import com.common.core.share.ShareType;
import com.common.log.MyLog;
import com.common.view.titlebar.CommonTitleBar;

import static com.common.view.titlebar.CommonTitleBar.ACTION_LEFT_TEXT;
import static com.common.view.titlebar.CommonTitleBar.ACTION_RIGHT_BUTTON;

@Route(path = "/common/ExpendWebActivity")
public class ExpendWebActivity extends AgentWebActivity {

    boolean mShowShareBtn = false;

    String mTitle;
    String mDes;
    String mIcon;
    String mUrl;

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        super.initData(savedInstanceState);

        mUrl = getIntent().getStringExtra("url");
        mShowShareBtn = getIntent().getBooleanExtra("showShare", false);

        try {
            injectObj();
        } catch (Exception e) {
            MyLog.e(e);
        }

        if(!mShowShareBtn){
            mTitlebar.getRightImageButton().setVisibility(View.GONE);
        }

        mTitlebar.setListener(new CommonTitleBar.OnTitleBarListener() {
            @Override
            public void onClicked(View v, int action, String extra) {
                if (action == ACTION_LEFT_TEXT) {
                    finish();
                } else if (action == ACTION_RIGHT_BUTTON) {
                    SharePanel sharePanel = new SharePanel(ExpendWebActivity.this);
                    sharePanel.setShareContent(mTitle, mDes, mUrl);
                    sharePanel.show(ShareType.URL);
                }
            }
        });
    }

    protected void pageFinished(WebView view, String url){
        view.loadUrl("javascript:window.local_obj.showSource("
                + "document.querySelector('meta[name=\"shareIcon\"]').getAttribute('content'),"
                + "document.querySelector('meta[name=\"description\"]').getAttribute('content')"
                + ");");
    }

    protected void receivedTitle(WebView view, String title){
        mTitle = title;
    }

    public void injectObj() throws Exception {
        mAgentWeb.getJsInterfaceHolder().addJavaObject("local_obj", new InJavaScriptLocalObj());
    }

    final class InJavaScriptLocalObj {
        @JavascriptInterface
        public void showSource(String shareIcon, String des) {
            MyLog.d(TAG, "showSource" + " shareIcon=" + shareIcon + " des=" + des);
            mDes = des;
            mIcon = shareIcon;
        }
    }
}
