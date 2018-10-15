package com.wali.live.watchsdk.webview;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.base.activity.BaseSdkActivity;
import com.base.dialog.DialogUtils;
import com.base.global.GlobalData;
import com.base.keyboard.KeyboardUtils;
import com.base.log.MyLog;
import com.base.utils.CommonUtils;
import com.base.utils.Constants;
import com.base.utils.language.LocaleUtil;
import com.base.utils.toast.ToastUtils;
import com.base.utils.version.VersionManager;
import com.base.view.BackTitleBar;
import com.base.view.BottomButton;
import com.mi.live.data.account.UserAccountManager;
import com.wali.live.event.EventClass;
import com.wali.live.statistics.StatisticsKey;
import com.wali.live.statistics.StatisticsWorker;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.scheme.SchemeConstants;
import com.wali.live.watchsdk.scheme.SchemeSdkActivity;
import com.wali.live.watchsdk.scheme.specific.SpecificConstants;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by zhangzhiyuan on 2016/2/23.
 */
public class HalfWebViewActivity extends BaseSdkActivity implements View.OnClickListener {
    public static final String EXTRA_URL = "extra_url";
    public static final String EXTRA_UID = "extra_uid";
    public static final String EXTRA_ZUID = "extra_zuid";
    public static final String EXTRA_AVATAR = "extra_avatar";
    public static final String EXTRA_DISPLAY_MENU = "extra_display_menu";
    public static final String EXTRA_DISPLAY_TYPE = "extra_display_type";
    public static final String EXTRA_WIDGET_ID = "extra_widget_id";

    protected final float TITLE_BAR_RIGHT_BTN_MARGIN = 0.33f;

    private int mWidgetId;
    private long zuid;
    private String mUrl;
    private String mTitle = "";
    protected String mimeType;
    protected String encoding;
    protected String data;

    private WebViewListener mWebViewListener;
    private InjectedWebViewClient mWebViewClient;
    private BackTitleBar mTitleBar;
    private WebView mWebView;
    private View mErrorView;
    private TextView mErrorDesTv;
    private BottomButton mErrorOpenInSystemBtn;
    protected ImageView mMoreView;

    protected ViewGroup mWebViewContainer;
    private ViewGroup videoLayout;

    protected PopupWindow mPopupWindow;
    public long mOwnerId;
    public long mAvatarTs;
    private String mLauncherPicPath;
    private View mOutView;
    private ImageView mImgLoading;
    private Animation anim;
    private boolean isFromH5Share = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        handleIntent(getIntent());
        overridePendingTransition(R.anim.open_room, 0);
        if (TextUtils.isEmpty(mUrl)) {
            finish();
        }
        setContentView(R.layout.half_webview_activity);
        mOutView = findViewById(R.id.out_view);
        if (mOutView != null) {
            mOutView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }

        anim = AnimationUtils.loadAnimation(this, R.anim.ml_loading_animation);
        bindViews();
        mImgLoading.startAnimation(anim);
        mWebView.setVisibility(View.GONE);
        initializeViews();
        setCookies();

        boolean urlValid = LiveWebViewClient.isValidUrl(mUrl);
        if (!urlValid) {
            mUrl = LiveWebViewClient.getCheckedUrl(mUrl);
        }
        mWebView.loadUrl(mUrl);
        mWebView.setBackgroundColor(0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //解决了bug点击进入广告页分享至微信点击打开键盘返回上级页面发现键盘不消失
        KeyboardUtils.hideKeyboard(this);
        if (mWebViewClient != null) {
            mWebViewClient.refresh(mWebView);
        }
    }

    protected void bindViews() {
        mImgLoading = (ImageView) findViewById(R.id.imgLoading);
        mWebViewContainer = (ViewGroup) findViewById(R.id.web_view_container);
        mWebView = new WebView(GlobalData.app());
        mWebView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        mWebViewContainer.addView(mWebView);
        videoLayout = (ViewGroup) findViewById(R.id.videoLayout);
        mTitleBar = (BackTitleBar) findViewById(R.id.title_bar);
        mMoreView = mTitleBar.getRightImageBtn();
        mErrorView = findViewById(R.id.errorPage);
        mErrorDesTv = (TextView) findViewById(R.id.error_tip);
        mErrorOpenInSystemBtn = (BottomButton) findViewById(R.id.open_insystem);
        mTitleBar.getBackBtn().setOnClickListener(this);
        mErrorOpenInSystemBtn.setOnClickListener(this);
    }

    protected void initializeViews() {
        View loadingView = LayoutInflater.from(this).inflate(R.layout.view_loading_video, null);
        mWebViewListener = new LiveWebViewListener();
        mWebView.setWebChromeClient(new LiveWebChromeClient(this, mWebViewListener, mWebViewContainer, videoLayout, loadingView, mWebView));
        mWebViewClient = new InjectedWebViewClient(mWebViewListener, this);
        mWebView.setWebViewClient(mWebViewClient);
        if (CommonUtils.isChineseLocale()) {
            mWebViewClient.setWebViewCount(0);
        }

        fixWebViewSecurity();

        WebSettings settings = mWebView.getSettings();
        String userAgent = settings.getUserAgentString();
        userAgent += "mizhiBo-a-" + VersionManager.getVersionName(this) + "-" + LocaleUtil.getLanguageCode().toLowerCase();
        settings.setUserAgentString(userAgent);
        settings.setJavaScriptEnabled(true);
        settings.setPluginState(WebSettings.PluginState.ON);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(false);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setDomStorageEnabled(true);
        settings.setGeolocationEnabled(true);
        settings.setGeolocationDatabasePath(getFilesDir().getPath());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            settings.setAllowUniversalAccessFromFileURLs(true);
        }
        if (Constants.isDebugBuild && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        mMoreView.setVisibility(View.VISIBLE);
        mMoreView.setImageResource(R.drawable.image_close);
        mMoreView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        mWebView.clearCache(false);
        mWebView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(final String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                DialogUtils.showNormalDialog(HalfWebViewActivity.this, "", getString(R.string.message_open_system_browser), R.string.ok, R.string.cancel,
                        new DialogUtils.IDialogCallback() {
                            @Override
                            public void process(DialogInterface dialogInterface, int i) {
                                openSystemBrowser(url);
                            }
                        }, null);
            }
        });

        mWebView.addJavascriptInterface(new JavaScriptInterface(this), "MiLive");
    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }

    private void openSystemBrowser(String url) {
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(@NonNull Intent intent) {
        mUrl = intent.getStringExtra(EXTRA_URL);
        mOwnerId = intent.getLongExtra(EXTRA_UID, 0);
        mAvatarTs = intent.getLongExtra(EXTRA_AVATAR, 0);
        mWidgetId = intent.getIntExtra(EXTRA_WIDGET_ID, 0);
        zuid = intent.getLongExtra(EXTRA_ZUID, 0);
        if (!TextUtils.isEmpty(mUrl) && mWebView != null) {
            mWebView.loadUrl(mUrl);
            mWebView.setBackgroundColor(0);
        }
    }

    private void fixWebViewSecurity() {
        if (Build.VERSION.SDK_INT > 10 && Build.VERSION.SDK_INT < 17) {
            mWebView.removeJavascriptInterface("searchBoxJavaBridge_");
        }
    }

    public class LiveWebViewListener extends WebViewListener {
        public LiveWebViewListener() {
        }

        @Override
        public void onProgressChanged(int newProgress) {
            if (newProgress >= 100) {
                mImgLoading.clearAnimation();
                mImgLoading.setVisibility(View.GONE);
            }

        }

        @Override
        public void onReceivedTitle(String title) {
            mTitle = title;
            mTitleBar.setTitle(mTitle);
        }

        @Override
        public void onReceivedError(String description, String failingUrl) {
            mImgLoading.clearAnimation();
            mImgLoading.setVisibility(View.GONE);
            showErrorView(description, failingUrl);
        }

        @Override
        public void onPageStarted(String url) {
            mUrl = url;
            hideErrorView();
        }

        @Override
        public void onPageFinished(String url) {
            mPopupWindow = null;
        }

        @Override
        public void onLoadResource(String url) {
        }

        @Override
        public void onPageCommitVisible(String url) {
        }
    }

    private void showErrorView(String description, final String url) {
        if (!TextUtils.isEmpty(url)) {
            if (url.startsWith(SchemeConstants.SCHEME_LIVESDK)) {
                openUrlWithBrowserIntent(url, this);
                return;
            } else if (url.startsWith(SchemeConstants.SCHEME_WALILIVE) ||
                    url.startsWith(SpecificConstants.SCHEME_GAMECENTER)) {
                SchemeSdkActivity.openActivity(this, Uri.parse(url));
                return;
            } else {
                int httpIndex = url.indexOf("http");
                if (httpIndex > 0 && httpIndex < url.length()) {
                    mUrl = url.substring(url.indexOf("http"));
                }
            }
        }
        mWebView.setVisibility(View.GONE);
        mErrorView.setVisibility(View.VISIBLE);
        mErrorDesTv.setText(getString(R.string.open_link_forbidden_tips, description));
    }

    private void hideErrorView() {
        if (null != mWebView) {
            mWebView.setVisibility(View.VISIBLE);
        }
        if (null != mErrorView) {
            mErrorView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.open_insystem) {
            hideErrorView();
            openUrlInSystemBrowser(mUrl, this);
        } else if (id == R.id.back_iv) {
            onBackPressed();
        }
    }

    public void openUrlWithBrowserIntent(final String url, final Activity ctx) {
        if (!TextUtils.isEmpty(url)) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            if (CommonUtils.isIntentAvailable(ctx, intent)) {
                ctx.startActivity(intent);
            } else {
                ToastUtils.showToast(ctx, R.string.invalid_url);
            }
        }
    }

    public void openUrlInSystemBrowser(final String url, final Activity ctx) {
        if (!TextUtils.isEmpty(url)) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            if (CommonUtils.isIntentAvailable(ctx, intent)) {
                ctx.startActivity(intent);
            } else {
                ToastUtils.showToast(ctx, R.string.invalid_url);
            }
        }
    }

    @Override
    public void onBackPressed() {
        boolean goBack = mWebView.canGoBack();
        int webViewCount = mWebViewClient.getWebViewCount();
        if (!goBack || webViewCount == 0) {
            super.onBackPressed();
        } else {
            mWebView.goBack();
            mWebViewClient.setWebViewCount(--webViewCount);
        }
    }

    @Override
    protected void onDestroy() {
        destroyWebView();
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        if (null != mWebViewClient) {
            mWebViewClient.onDestroy();
        }
        overridePendingTransition(0, R.anim.zoom_out);
    }

    @Override
    protected void onPause() {
        super.onPause();
        KeyboardUtils.hideKeyboardImmediately(this);
    }

    public void destroyWebView() {
        try {
            Class.forName("android.webkit.WebView")
                    .getMethod("onPause", (Class[]) null)
                    .invoke(mWebView, (Object[]) null);

        } catch (Exception e) {
            MyLog.e(TAG, e);
        }
        mWebViewContainer.removeAllViews();
        mWebView.removeAllViews();
        mWebView.destroy();
        mWebView = null;
    }

    /**
     * 设置cookies
     */
    public static void setCookies() {
        CookieSyncManager.createInstance(GlobalData.app());

        UserAccountManager userAccountManager = UserAccountManager.getInstance();
        final String userId = userAccountManager.getUuid();
        final String passToken = userAccountManager.getPassToken();
        final String serviceToken = userAccountManager.getServiceToken();
        MyLog.v(" setCookies zhiboUuid == " + userId);
        MyLog.v(" setCookies passToken == " + passToken);
        MyLog.v(" setCookies zhiboServiceToken == " + serviceToken);

        final CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        if (!TextUtils.isEmpty(userId)) {
            cookieManager.setCookie(".xiaomi.com", "zhiboUuid =" + userId);
            cookieManager.setCookie(".xiaomi.cn", "zhiboUuid =" + userId);
            cookieManager.setCookie(".xiaomi.net", "zhiboUuid =" + userId);
            cookieManager.setCookie(".mi.com", "zhiboUuid =" + userId);
        }

        if (!TextUtils.isEmpty(passToken)) {
            String zhiboPassToken = "zhiboPassToken =";// 用passToken会导致小米钱包每次都要输入密码
            cookieManager.setCookie(".xiaomi.com", zhiboPassToken + passToken);
            cookieManager.setCookie(".xiaomi.cn", zhiboPassToken + passToken);
            cookieManager.setCookie(".xiaomi.net", zhiboPassToken + passToken);
            cookieManager.setCookie(".mi.com", zhiboPassToken + passToken);
        }

        if (!TextUtils.isEmpty(serviceToken)) {
            cookieManager.setCookie(".xiaomi.com", "zhiboServiceToken =" + serviceToken);
            cookieManager.setCookie(".xiaomi.cn", "zhiboServiceToken =" + serviceToken);
            cookieManager.setCookie(".xiaomi.net", "zhiboServiceToken =" + serviceToken);
            cookieManager.setCookie(".mi.com", "zhiboServiceToken =" + serviceToken);
        }
        CookieSyncManager.getInstance().sync();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EventClass.CloseWebEvent event) {
        finish();
        String key = String.format(StatisticsKey.KEY_WIDGET_CLICK, String.valueOf(mWidgetId), "webViewClick", String.valueOf(zuid));
        if (TextUtils.isEmpty(key)) {
            return;
        }
        StatisticsWorker.getsInstance().sendCommandRealTime(StatisticsWorker.AC_APP, key, 1);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EventClass.LoadingEndEvent event) {
        mImgLoading.clearAnimation();
        mImgLoading.setVisibility(View.GONE);
        mWebView.setVisibility(View.VISIBLE);
    }

    /**
     * JSBridge
     */
    private class JavaScriptInterface {
        Context mContext;

        /**
         * Instantiate the interface and set the context
         */
        JavaScriptInterface(Context c) {
            mContext = c;
        }

        /**
         * 打开全屏详情页
         * 由Js调用执行Native本地Java方法
         */
        @JavascriptInterface
        public void close() {
            finish();
            String key = String.format(StatisticsKey.KEY_WIDGET_CLICK, String.valueOf(mWidgetId), "webViewClick", String.valueOf(zuid));
            if (TextUtils.isEmpty(key)) {
                return;
            }
            StatisticsWorker.getsInstance().sendCommandRealTime(StatisticsWorker.AC_APP, key, 1);
        }
    }

    public static void open(@NonNull Activity activity, @NonNull String url) {
        Intent intent = new Intent(activity, HalfWebViewActivity.class);
        intent.putExtra(EXTRA_URL, url);
        activity.startActivity(intent);
    }
}
