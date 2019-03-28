package com.common.webview;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.common.base.BaseActivity;
import com.common.base.R;
import com.common.core.BuildConfig;
import com.common.rxretrofit.cookie.persistence.SharedPrefsCookiePersistor;
import com.common.utils.U;
import com.common.view.titlebar.CommonTitleBar;
import com.jsbridge.BridgeWebView;
import com.jsbridge.BridgeWebViewClient;
import com.just.agentweb.AgentWeb;
import com.just.agentweb.AgentWebUIControllerImplBase;
import com.just.agentweb.MiddlewareWebChromeBase;
import com.just.agentweb.MiddlewareWebClientBase;
import com.module.RouterConstants;
import com.umeng.socialize.UMShareAPI;

import java.io.File;
import java.io.IOException;
import java.util.List;

import okhttp3.Cookie;

import static android.os.Build.VERSION_CODES.M;
import static com.common.core.scheme.SchemeConstants.SCHEME_INFRAMESKER;
import static com.common.view.titlebar.CommonTitleBar.ACTION_LEFT_TEXT;


public class AgentWebActivity extends CameraAdapWebActivity {

    protected AgentWeb mAgentWeb;
    private AgentWebUIControllerImplBase mAgentWebUIController;
    private ErrorLayoutEntity mErrorLayoutEntity;
    private MiddlewareWebChromeBase mMiddleWareWebChrome;
    private MiddlewareWebClientBase mMiddleWareWebClient;

    private SharedPrefsCookiePersistor mSharedPrefsCookiePersistor;
    CommonTitleBar mTitlebar;
    RelativeLayout mContentContainer;

    BridgeWebView mBridgeWebView;

    JsRegister mJsRegister;

    protected WebChromeClient mWebChromeClient = mWebChromeClient = new WebChromeClient() {
        // Work on Android 4.4.2 Zenfone 5
        public void showFileChooser(ValueCallback<String[]> filePathCallback,
                                    String acceptType, boolean paramBoolean){




            // TODO Auto-generated method stub
        }
        //for  Android 4.0+
        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {

            if (nFilePathCallback != null) {
                nFilePathCallback.onReceiveValue(null);
            }
            nFilePathCallback = uploadMsg;
            if ("image/*".equals(acceptType)) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                        takePictureIntent.putExtra("PhotoPath", mCameraPhotoPath);
                    } catch (IOException ex) {
                        Log.e("TAG", "Unable to create Image File", ex);
                    }
                    if (photoFile != null) {
                        mCameraPhotoPath = "file:" + photoFile.getAbsolutePath();
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                                Uri.fromFile(photoFile));
                    } else {
                        takePictureIntent = null;
                    }
                }
                startActivityForResult(takePictureIntent, INPUT_FILE_REQUEST_CODE);
            } else if ("video/*".equals(acceptType)) {
                Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takeVideoIntent, INPUT_VIDEO_CODE);
                }
            }
        }
        @SuppressLint("NewApi")
        @Override
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback,
                                         WebChromeClient.FileChooserParams fileChooserParams) {
            if (mFilePathCallback != null) {
                mFilePathCallback.onReceiveValue(null);
            }
            mFilePathCallback = filePathCallback;
            String[] acceptTypes = fileChooserParams.getAcceptTypes();
            if (acceptTypes[0].equals("image/*")) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                        takePictureIntent.putExtra("PhotoPath", mCameraPhotoPath);
                    } catch (IOException ex) {
                        Log.e("TAG", "Unable to create Image File", ex);
                    }
                    //适配7.0
                    if(Build.VERSION.SDK_INT > M) {
                        if (photoFile != null) {
                            photoURI = FileProvider.getUriForFile(AgentWebActivity.this,
                                    BuildConfig.APPLICATION_ID+".fileprovider", photoFile);
                            takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                        }
                    }else{
                        if (photoFile != null) {
                            mCameraPhotoPath = "file:" + photoFile.getAbsolutePath();
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                                    Uri.fromFile(photoFile));
                        } else {
                            takePictureIntent = null;
                        }
                    }
                }
                startActivityForResult(takePictureIntent, INPUT_FILE_REQUEST_CODE);
            } else if (acceptTypes[0].equals("video/*")) {
                Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takeVideoIntent, INPUT_VIDEO_CODE);
                }
            }
            return true;
        }

        @Override
        public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
            return true;
        }

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            //do you work
//            Log.i("Info","onProgress:"+newProgress);
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
            if (mTitlebar != null) {
                mTitlebar.getCenterTextView().setText(title);
            }
            receivedTitle(view, title);
        }
    };

    @Override
    public int initView(@Nullable Bundle savedInstanceState) {
        return R.layout.web_activity_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mTitlebar = (CommonTitleBar) this.findViewById(R.id.titlebar);
        mContentContainer = (RelativeLayout) this.findViewById(R.id.content_container);
        mBridgeWebView = new BridgeWebView(this);
        buildAgentWeb();
    }

    protected void pageFinished(WebView view, String url) {

    }

    protected void receivedTitle(WebView view, String title) {

    }

    /**
     * 更多使用实例，参考 AgentWeb 官网上的 samples
     */
    protected void buildAgentWeb() {
        ErrorLayoutEntity mErrorLayoutEntity = getErrorLayoutEntity();
        String url = getIntent().getStringExtra("url");

        BridgeWebViewClient mBridgeWebViewClient = new BridgeWebViewClient(mBridgeWebView) {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                pageFinished(view, url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (!TextUtils.isEmpty(url) && url.startsWith(SCHEME_INFRAMESKER)) { //
                    ARouter.getInstance().build(RouterConstants.ACTIVITY_SCHEME)
                            .withString("uri", url)
                            .navigation();

                    return true;
                }

                return super.shouldOverrideUrlLoading(view, url);
            }
        };

        setCookie(url);

        mAgentWeb = AgentWeb.with(this)//
                .setAgentWebParent((ViewGroup) mContentContainer, new RelativeLayout.LayoutParams(-1, -1))//
                .useDefaultIndicator(-1, 2)//
//                .setAgentWebWebSettings(getSettings())//
                .setWebViewClient(mBridgeWebViewClient)
                .setWebChromeClient(mWebChromeClient)
                .setWebView(mBridgeWebView)
                .setSecurityType(AgentWeb.SecurityType.STRICT_CHECK)
//                .setDownloadListener(mDownloadListener) 4.0.0 删除该API
                .createAgentWeb()//
                .ready()//
                .go(url);

        WebSettings settings = mBridgeWebView.getSettings();
        String userAgentString = settings.getUserAgentString();
        mBridgeWebView.getSettings().setUserAgentString(userAgentString + " pid/" + U.getDeviceUtils().getDeviceID());

        mJsRegister = new JsRegister(mBridgeWebView, this);
        mJsRegister.register();
    }

    private void setCookie(String url) {
        //种cookie，先注释掉
        if (!TextUtils.isEmpty(url) && url.contains("inframe.mobi")) {
            mSharedPrefsCookiePersistor = new SharedPrefsCookiePersistor(this);
            List<Cookie> cookies = mSharedPrefsCookiePersistor.loadAll();
            if (cookies != null && cookies.size() > 0) {
                CookieSyncManager.createInstance(this);
                CookieManager cookieManager = CookieManager.getInstance();
                cookieManager.setAcceptCookie(true);
                for (Cookie cookie : cookies) {
                    cookieManager.setCookie("inframe.mobi", cookie.toString());//cookies是在HttpClient中获得的cookie
                }
                CookieSyncManager.getInstance().sync();
            }
        }
    }


    protected @NonNull
    ErrorLayoutEntity getErrorLayoutEntity() {
        if (this.mErrorLayoutEntity == null) {
            this.mErrorLayoutEntity = new ErrorLayoutEntity();
        }
        return mErrorLayoutEntity;
    }


    protected static class ErrorLayoutEntity {
        private int layoutRes = R.layout.agentweb_error_page;
        private int reloadId;

        public void setLayoutRes(int layoutRes) {
            this.layoutRes = layoutRes;
            if (layoutRes <= 0) {
                layoutRes = -1;
            }
        }

        public void setReloadId(int reloadId) {
            this.reloadId = reloadId;
            if (reloadId <= 0) {
                reloadId = -1;
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mAgentWeb != null && mAgentWeb.handleKeyEvent(keyCode, event)) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onPause() {
        if (mAgentWeb != null) {
            mAgentWeb.getWebLifeCycle().onPause();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        if (mAgentWeb != null) {
            mAgentWeb.getWebLifeCycle().onResume();
        }
        super.onResume();
    }

    @Override
    protected void destroy() {
        if (mAgentWeb != null) {
            mAgentWeb.getWebLifeCycle().onDestroy();
        }
        super.destroy();
    }

    @Override
    public boolean canSlide() {
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data);
    }

    protected @NonNull
    MiddlewareWebChromeBase getMiddleWareWebChrome() {
        return this.mMiddleWareWebChrome = new MiddlewareWebChromeBase() {
        };
    }

    protected @NonNull
    MiddlewareWebClientBase getMiddleWareWebClient() {
        return this.mMiddleWareWebClient = new MiddlewareWebClientBase() {
        };
    }

    @Override
    public boolean useEventBus() {
        return false;
    }
}
