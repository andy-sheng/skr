package com.wali.live.watchsdk.webview;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.text.TextUtils;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.mi.live.data.config.GetConfigManager;
import com.wali.live.watchsdk.scheme.SchemeConstants;
import com.wali.live.watchsdk.scheme.SchemeSdkActivity;
import com.wali.live.watchsdk.scheme.specific.SpecificConstants;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.URLEncoder;
import java.util.Set;

/**
 * Created by zhangzhiyuan on 2016/2/24.
 */
public class LiveWebViewClient extends WebViewClient {
    protected static final String TAG = "LiveWebViewClient";

    protected WebViewListener mWebViewListener;
    protected int mWebViewCount = -1;

    protected WeakReference<Activity> mBaseActivity;

//    private NewH5CachePackage mNewCachePkg;

    public int getWebViewCount() {
        return mWebViewCount;
    }

    public void setWebViewCount(int webViewCount) {
        this.mWebViewCount = webViewCount;
    }

//    public void setNewCachePackage(NewH5CachePackage cpkg) {
//        mNewCachePkg = cpkg;
//    }

    public LiveWebViewClient(WebViewListener mWebViewListener, Activity activity) {
        this.mWebViewListener = mWebViewListener;
        this.mBaseActivity = new WeakReference(activity);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        if (null != mWebViewListener) {
            mWebViewListener.onPageFinished(url);
            mWebViewListener.onReceivedTitle(view.getTitle());
        }
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
        if (null != mWebViewListener) {
            mWebViewListener.onPageStarted(url);
        }
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        super.onReceivedError(view, errorCode, description, failingUrl);
        if (null != mWebViewListener) {
            mWebViewListener.onReceivedError(description, failingUrl);
        }
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        MyLog.d(TAG, "shouldOverrideUrlLoading url:" + url);
        if (TextUtils.isEmpty(url)) {
            return false;
        }
        if (url.startsWith(SchemeConstants.SCHEME_LIVESDK) ||
                url.startsWith(SchemeConstants.SCHEME_WALILIVE) ||
                url.startsWith(SpecificConstants.SCHEME_GAMECENTER) ||
                GetConfigManager.getInstance().isValidHost(url)) {
            Uri uri = Uri.parse(url);
            SchemeSdkActivity.openActivity(mBaseActivity.get(), uri);
        } else {
            view.loadUrl(url);
            mWebViewCount++;
        }
        return true;
    }

    //webView默认是不处理https请求的，页面显示空白，需要进行如下设置：
    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        super.onReceivedSslError(view, handler, error);
        handler.proceed();
    }

    @SuppressLint("NewApi")
    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, final String url) {
        boolean urlValid = isValidUrl(url);
        MyLog.d(TAG, "url=" + url + ", urlValid=" + urlValid);
//        if (urlValid) {
//            WebResourceResponse response = null;
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
//                try {
//                    if (null == response && mNewCachePkg != null) {
//                        InputStream in = mNewCachePkg.loadCache(url, GlobalData.app().getApplicationContext());
//                        if (in != null) {
//                            NewH5CachePackage.H5CacheFileInfo fileInfo = mNewCachePkg.getFileInfo(url);
//
//                            if (fileInfo != null) {
//                                MyLog.w(TAG, "new intercept response=" + fileInfo.getContentType());
//                                response = new WebResourceResponse(
//                                        fileInfo.getContentType(),
//                                        fileInfo.getCharset(), in);
//                            }
//                        }
//                    }
//                } catch (Exception e) {
//                    MyLog.e(TAG, "", e);
//                }
//            }
//            if (response != null) {
//                return response;
//            }
//            return super.shouldInterceptRequest(view, url);
//        } else {
            return super.shouldInterceptRequest(view, url);
//        }
    }

    public static boolean isValidUrl(String url) {
        Uri uri = Uri.parse(url);
        if ("file".equals(uri.getScheme())) {
            // 本地文件放行
            return true;
        }
        String host = uri.getHost();
        boolean urlValid = false;
        Set<String> set = GetConfigManager.getInstance().getWhiteListUrlForWebView();
        if (set != null) {
            for (String keyHost : set) {
                if (null != host && host.endsWith(keyHost)) {
                    urlValid = true;
                    break;
                }
            }
        }
        return urlValid;
    }

    //非白名单的链接则直接通过服务的重定向跳转
    public static String getCheckedUrl(String url) {
        if (!TextUtils.isEmpty(url)) {
            StringBuilder newUrl = new StringBuilder();
            newUrl.append("http://uck.g.mi.com/?src=");
            try {
                newUrl.append(URLEncoder.encode(url, "UTF-8"));
                return newUrl.toString();
            } catch (UnsupportedEncodingException e) {
                MyLog.w("getCheckedUrl UnsupportedEncodingException");
            }
        }
        return url;
    }
}
