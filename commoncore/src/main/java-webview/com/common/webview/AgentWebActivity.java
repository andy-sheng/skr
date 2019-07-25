package com.common.webview;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.KeyEvent;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.common.base.R;
import com.common.core.WebIpcCallback;
import com.common.core.WebIpcService;
import com.common.core.scheme.SchemeSdkActivity;
import com.common.log.MyLog;
import com.common.rxretrofit.cookie.persistence.SharedPrefsCookiePersistor;
import com.common.utils.U;
import com.common.view.titlebar.CommonTitleBar;
import com.common.webview.aidl.WebIpcClient;
import com.common.webview.aidl.WebIpcServer;
import com.jsbridge.BridgeWebView;
import com.jsbridge.BridgeWebViewClient;
import com.jsbridge.CallBackFunction;
import com.just.agentweb.AgentWebX5;
import com.just.agentweb.AgentWebX5Utils;
import com.just.agentweb.DefaultWebClient;
import com.just.agentweb.MiddleWareWebChromeBase;
import com.just.agentweb.MiddleWareWebClientBase;
import com.tencent.smtt.export.external.interfaces.ConsoleMessage;
import com.tencent.smtt.sdk.CookieManager;
import com.tencent.smtt.sdk.CookieSyncManager;
import com.tencent.smtt.sdk.ValueCallback;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.umeng.socialize.UMShareAPI;

import java.io.File;
import java.io.IOException;
import java.util.List;

import okhttp3.Cookie;

import static android.os.Build.VERSION_CODES.M;
import static com.common.core.scheme.SchemeConstants.SCHEME_INFRAMESKER;
import static com.common.webview.JsBridgeImpl.getJsonObj;

/**
 * 注意！！运行在 :tools 进程，要使用 aidl 进行通信
 */
class AgentWebActivity extends CameraAdapWebActivity {

    protected AgentWebX5 mAgentWeb;
    //    private AgentWebUIControllerImplBase mAgentWebUIController;
    private ErrorLayoutEntity mErrorLayoutEntity;
    private MiddleWareWebChromeBase mMiddleWareWebChrome;
    private MiddleWareWebClientBase mMiddleWareWebClient;
    AlertDialog mAlertDialog;
    private String[] mDialogSelectTexts = new String[]{"相册", "相机"};

    //    private SharedPrefsCookiePersistor mSharedPrefsCookiePersistor;
    CommonTitleBar mTitlebar;
    RelativeLayout mContentContainer;

    BridgeWebView mBridgeWebView;

    JsRegister mJsRegister;

    protected WebChromeClient mWebChromeClient = new WebChromeClient() {
        // Work on Android 4.4.2 Zenfone 5
        public void showFileChooser(ValueCallback<String[]> filePathCallback,
                                    String acceptType, boolean paramBoolean) {

        }

        //for  Android 4.0+
        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
            MyLog.d(getTAG(), "openFileChooser 1");
            if (nFilePathCallback != null) {
                nFilePathCallback.onReceiveValue(null);
            }
            nFilePathCallback = uploadMsg;
            if ("image/*".equals(acceptType)) {
                mAlertDialog = new AlertDialog.Builder(AgentWebActivity.this)//
                        .setSingleChoiceItems(mDialogSelectTexts, -1, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (mAlertDialog != null) {
                                    mAlertDialog.dismiss();
                                }

                                if (which == 1) {
                                    //相机
                                    File photoFile = null;
                                    try {
                                        photoFile = createImageFile();
                                    } catch (IOException ex) {
                                        Log.e("TAG", "Unable to create Image File", ex);
                                    }

                                    Intent takePictureIntent = AgentWebX5Utils.getIntentCaptureCompat(U.app(), photoFile);
                                    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                                        if (photoFile != null) {
                                            mCameraPhotoPath = "file:" + photoFile.getAbsolutePath();
                                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                                                    Uri.fromFile(photoFile));
                                        } else {
                                            takePictureIntent = null;
                                        }
                                    }

                                    startActivityForResult(takePictureIntent, INPUT_FILE_REQUEST_CODE);
                                } else {
                                    //相册
                                    Intent takePictureIntent = AgentWebX5Utils.getIntentGetContentCompat();

                                    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                                        startActivityForResult(takePictureIntent, INPUT_IMAGE_CODE);
                                    }
                                }
                            }
                        }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                                    if (nFilePathCallback != null) {
                                        nFilePathCallback.onReceiveValue(null);
                                        nFilePathCallback = null;
                                    }
                                    return;
                                } else {
                                    if (mFilePathCallback != null) {
                                        mFilePathCallback.onReceiveValue(null);
                                        mFilePathCallback = null;
                                    }
                                    return;
                                }
                            }
                        }).create();

                mAlertDialog.show();
            } else if ("video/*".equals(acceptType)) {
                Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takeVideoIntent, INPUT_VIDEO_CODE);
                }
            }
        }

        @SuppressLint("NewApi")
        @Override
        public boolean onShowFileChooser(final WebView webView, final ValueCallback<Uri[]> filePathCallback,
                                         final WebChromeClient.FileChooserParams fileChooserParams) {
            MyLog.d(getTAG(), "onShowFileChooser 2 fileChooserParams mode is " + fileChooserParams.getMode() + " getAcceptTypes is " + fileChooserParams.getAcceptTypes() + " isCaptureEnabled " + fileChooserParams.isCaptureEnabled() + " getTitle is " + fileChooserParams.getTitle() + " getFilenameHint is " + fileChooserParams.getFilenameHint());
            if (mFilePathCallback != null) {
                mFilePathCallback.onReceiveValue(null);
            }
            mFilePathCallback = filePathCallback;
            String[] acceptTypes = fileChooserParams.getAcceptTypes();
            if (acceptTypes[0].equals("image/*")) {
                if (fileChooserParams.getMode() == FileChooserParams.MODE_OPEN) {
                    oponCameraNewApi();
                } else if (fileChooserParams.getMode() == FileChooserParams.MODE_OPEN_MULTIPLE) {
                    mAlertDialog = new AlertDialog.Builder(AgentWebActivity.this)//
                            .setSingleChoiceItems(mDialogSelectTexts, -1, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (mAlertDialog != null) {
                                        mAlertDialog.dismiss();
                                    }

                                    if (which == 1) {
                                        //相机
                                        oponCameraNewApi();
                                    } else {
                                        //相册
                                        Intent takePictureIntent = AgentWebX5Utils.getIntentGetContentCompat();

                                        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                                            startActivityForResult(takePictureIntent, INPUT_IMAGE_CODE);
                                        }
                                    }
                                }
                            }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                                        if (nFilePathCallback != null) {
                                            nFilePathCallback.onReceiveValue(null);
                                            nFilePathCallback = null;
                                        }
                                        return;
                                    } else {
                                        if (mFilePathCallback != null) {
                                            mFilePathCallback.onReceiveValue(null);
                                            mFilePathCallback = null;
                                        }
                                        return;
                                    }
                                }
                            }).create();

                    mAlertDialog.show();
                }
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

    public void oponCameraNewApi() {
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            Log.e("TAG", "Unable to create Image File", ex);
        }

        Intent takePictureIntent = AgentWebX5Utils.getIntentCaptureCompat(U.app(), photoFile);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            if (Build.VERSION.SDK_INT > M) {
                if (photoFile != null) {
                    photoURI = takePictureIntent.getParcelableExtra(MediaStore.EXTRA_OUTPUT);
                }
            } else {
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
    }

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
        mBridgeWebView.setBackgroundColor(Color.parseColor("#FF7088FF"));
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
//        String url = "http://test.app.inframe.mobi/test/bridge";

        BridgeWebViewClient mBridgeWebViewClient = new BridgeWebViewClient(mBridgeWebView) {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                pageFinished(view, url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (!TextUtils.isEmpty(url) && url.startsWith(SCHEME_INFRAMESKER)) {
                    Intent intent = new Intent(AgentWebActivity.this, SchemeSdkActivity.class);
                    intent.putExtra("uri", url);
                    AgentWebActivity.this.startActivity(intent);
                    Uri uri = Uri.parse(url);
                    if (uri != null) {
                        /**
                         * 改成双进程后，WebViewActivity 不会加到主进程 的Activity 列表中
                         * 所以 如果跳到首页的话，这里要主动 finish自己 。
                         */
                        String au = uri.getAuthority();
                        String pa = uri.getPath();
                        if (("home".equals(au) && "/jump".equals(pa))
                                || ("person".equals(au) && "/jump_person_center".equals(pa)
                                || ("room".equals(au) && "/chat_page".equals(pa)))
                        ) {
                            finish();
                        }
                    }
                    return true;
                }
                return super.shouldOverrideUrlLoading(view, url);
            }
        };


//        mAgentWeb = AgentWebX5.with(this)//
//                .setAgentWebParent((ViewGroup) mContentContainer, new RelativeLayout.LayoutParams(-1, -1))//
//                .useDefaultIndicator()//
////                .setAgentWebWebSettings(getSettings())//
//                .setWebViewClient(mBridgeWebViewClient)
//                .setWebChromeClient(mWebChromeClient)
//                .setWebView(mBridgeWebView)
//                .setSecurityType(AgentWebX5.SecurityType.strict)
////                .setDownloadListener(mDownloadListener) 4.0.0 删除该API
//                .createAgentWeb()//
//                .ready()//
//                .go(url);

        mAgentWeb = AgentWebX5.with(this)//
                .setAgentWebParent(mContentContainer, new LinearLayout.LayoutParams(-1, -1))//
                .useDefaultIndicator()//
                .defaultProgressBarColor()
                .setWebChromeClient(mWebChromeClient)
                .setWebViewClient(mBridgeWebViewClient)
                .setWebView(mBridgeWebView)
                .useMiddleWareWebChrome(getMiddleWareWebChrome())
                .useMiddleWareWebClient(getMiddleWareWebClient())
                .setOpenOtherPageWays(DefaultWebClient.OpenOtherPageWays.ASK)
                .interceptUnkownScheme()
                .setSecutityType(AgentWebX5.SecurityType.strict)
//                .setWebLayout(new WebLayout(this))
                .createAgentWeb()//
                .ready()
                .go(url);

        WebSettings settings = mBridgeWebView.getSettings();
        String userAgentString = settings.getUserAgentString();
        mBridgeWebView.getSettings().setUserAgentString(userAgentString
                + " pid/" + U.getDeviceUtils().getDeviceID()
                + " NetType/" + getNet()
                + " Pixel/" + U.getDisplayUtils().getPhoneWidth());

        mJsRegister = new JsRegister(mBridgeWebView, this);
        mJsRegister.register();

        setCookie(url);
    }

    private String getNet() {
        if (U.getNetworkUtils().isWifi()) {
            return "wifi";
        } else {
            return "WWAN";
        }
    }

    private void setCookie(String url) {
        //种cookie，先注释掉
        if (!TextUtils.isEmpty(url) && url.contains("inframe.mobi")) {
            WebIpcClient.getServer(new WebIpcClient.GetCallback() {
                @Override
                public void get(WebIpcService webIpcService) {
                    try {
                        webIpcService.call(WebIpcServer.TYPE_GET_COOKIES, null, new WebIpcCallback.Stub() {
                            @Override
                            public void callback(String json) throws RemoteException {
                                JSONArray jsonArray = JSON.parseArray(json);
                                if (jsonArray != null && jsonArray.size() > 0) {
                                    CookieSyncManager.createInstance(AgentWebActivity.this);
                                    CookieManager cookieManager = CookieManager.getInstance();
                                    cookieManager.setAcceptCookie(true);
                                    for (int i = 0; i < jsonArray.size(); i++) {
                                        cookieManager.setCookie("inframe.mobi", jsonArray.getString(i));//cookies是在HttpClient中获得的cookie
                                    }
                                    CookieSyncManager.getInstance().sync();
                                }
                            }
                        });
                    } catch (RemoteException e) {
                        MyLog.e(e);
                        SharedPrefsCookiePersistor mSharedPrefsCookiePersistor = new SharedPrefsCookiePersistor(AgentWebActivity.this);
                        List<Cookie> cookies = mSharedPrefsCookiePersistor.loadAll();
                        if (cookies != null && cookies.size() > 0) {
                            CookieSyncManager.createInstance(AgentWebActivity.this);
                            CookieManager cookieManager = CookieManager.getInstance();
                            cookieManager.setAcceptCookie(true);
                            for (Cookie cookie : cookies) {
                                cookieManager.setCookie("inframe.mobi", cookie.toString());//cookies是在HttpClient中获得的cookie
                            }
                            CookieSyncManager.getInstance().sync();
                        }
                    }
                }
            });
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
        try {
            if (mAgentWeb != null && mAgentWeb.handleKeyEvent(keyCode, event)) {
                return true;
            }
            return super.onKeyDown(keyCode, event);
        } catch (Exception e) {
            MyLog.e(getTAG(), e);
            return true;
        }
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
        mBridgeWebView.callHandler("callJs", getJsonObj(new Pair("opt", "pageActive")).toJSONString(), new CallBackFunction() {
            @Override
            public void onCallBack(String data) {

            }
        });
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
        UMShareAPI.get(U.app()).onActivityResult(requestCode, resultCode, data);
    }

    protected @NonNull
    MiddleWareWebChromeBase getMiddleWareWebChrome() {
        return this.mMiddleWareWebChrome = new MiddleWareWebChromeBase() {
        };
    }

    protected @NonNull
    MiddleWareWebClientBase getMiddleWareWebClient() {
        return this.mMiddleWareWebClient = new MiddleWareWebClientBase() {
        };
    }

    @Override
    public boolean useEventBus() {
        return false;
    }

}
