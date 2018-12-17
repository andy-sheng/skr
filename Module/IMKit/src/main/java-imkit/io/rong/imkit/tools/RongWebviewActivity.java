//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit.tools;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.webkit.DownloadListener;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import io.rong.common.RLog;
import io.rong.common.RongWebView;
import io.rong.imkit.R;
import io.rong.imkit.RongBaseActivity;

public class RongWebviewActivity extends RongBaseActivity {
    private static final String TAG = "RongWebviewActivity";
    private String mPrevUrl;
    private RongWebView mWebView;
    private ProgressBar mProgressBar;
    protected TextView mWebViewTitle;

    public RongWebviewActivity() {
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.rc_ac_webview);
        Intent intent = this.getIntent();
        this.mWebView = (RongWebView) this.findViewById(R.id.rc_webview);
        this.mProgressBar = (ProgressBar) this.findViewById(R.id.rc_web_progressbar);
        this.mWebViewTitle = (TextView) this.findViewById(R.id.rc_action_bar_title);
        this.mWebView.setVerticalScrollbarOverlay(true);
        this.mWebView.getSettings().setLoadWithOverviewMode(true);
        this.mWebView.getSettings().setJavaScriptEnabled(true);
        this.mWebView.getSettings().setUseWideViewPort(true);
        this.mWebView.getSettings().setBuiltInZoomControls(true);
        if (VERSION.SDK_INT > 11) {
            this.mWebView.getSettings().setDisplayZoomControls(false);
        }

        this.mWebView.getSettings().setSupportZoom(true);
        this.mWebView.setWebViewClient(new io.rong.imkit.tools.RongWebviewActivity.RongWebviewClient());
        this.mWebView.setWebChromeClient(new io.rong.imkit.tools.RongWebviewActivity.RongWebChromeClient());
        this.mWebView.setDownloadListener(new io.rong.imkit.tools.RongWebviewActivity.RongWebViewDownLoadListener());
        this.mWebView.getSettings().setDomStorageEnabled(true);
        this.mWebView.getSettings().setDefaultTextEncodingName("utf-8");
        String url = intent.getStringExtra("url");
        Uri data = intent.getData();
        if (url != null) {
            this.mPrevUrl = url;
            this.mWebView.loadUrl(url);
            String title = intent.getStringExtra("title");
            if (this.mWebViewTitle != null && !TextUtils.isEmpty(title)) {
                this.mWebViewTitle.setText(title);
            }
        } else if (data != null) {
            this.mPrevUrl = data.toString();
            this.mWebView.loadUrl(data.toString());
        }

    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == 4 && this.mWebView.canGoBack()) {
            this.mWebView.goBack();
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    public boolean checkIntent(Context context, Intent intent) {
        PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> apps = packageManager.queryIntentActivities(intent, 0);
        return apps.size() > 0;
    }

    private class RongWebViewDownLoadListener implements DownloadListener {
        private RongWebViewDownLoadListener() {
        }

        public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
            Uri uri = Uri.parse(url);
            Intent intent = new Intent("android.intent.action.VIEW", uri);
            if (io.rong.imkit.tools.RongWebviewActivity.this.checkIntent(io.rong.imkit.tools.RongWebviewActivity.this, intent)) {
                io.rong.imkit.tools.RongWebviewActivity.this.startActivity(intent);
                if (uri.getScheme().equals("file") && uri.toString().endsWith(".txt")) {
                    io.rong.imkit.tools.RongWebviewActivity.this.finish();
                }
            }

        }
    }

    private class RongWebChromeClient extends WebChromeClient {
        private RongWebChromeClient() {
        }

        public void onProgressChanged(WebView view, int newProgress) {
            if (newProgress == 100) {
                io.rong.imkit.tools.RongWebviewActivity.this.mProgressBar.setVisibility(8);
            } else {
                if (io.rong.imkit.tools.RongWebviewActivity.this.mProgressBar.getVisibility() == 8) {
                    io.rong.imkit.tools.RongWebviewActivity.this.mProgressBar.setVisibility(0);
                }

                io.rong.imkit.tools.RongWebviewActivity.this.mProgressBar.setProgress(newProgress);
            }

            super.onProgressChanged(view, newProgress);
        }

        public void onReceivedTitle(WebView view, String title) {
            if (io.rong.imkit.tools.RongWebviewActivity.this.mWebViewTitle != null && TextUtils.isEmpty(io.rong.imkit.tools.RongWebviewActivity.this.mWebViewTitle.getText())) {
                io.rong.imkit.tools.RongWebviewActivity.this.mWebViewTitle.setText(title);
            }

        }
    }

    private class RongWebviewClient extends WebViewClient {
        private RongWebviewClient() {
        }

        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (io.rong.imkit.tools.RongWebviewActivity.this.mPrevUrl != null) {
                if (!io.rong.imkit.tools.RongWebviewActivity.this.mPrevUrl.equals(url)) {
                    if (!url.toLowerCase().startsWith("http://") && !url.toLowerCase().startsWith("https://")) {
                        Intent intent = new Intent("android.intent.action.VIEW");
                        Uri content_url = Uri.parse(url);
                        intent.setData(content_url);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

                        try {
                            io.rong.imkit.tools.RongWebviewActivity.this.startActivity(intent);
                        } catch (Exception var6) {
                            RLog.e("RongWebviewActivity", "not apps install for this intent =" + var6.toString());
                            var6.printStackTrace();
                        }

                        return true;
                    } else {
                        io.rong.imkit.tools.RongWebviewActivity.this.mPrevUrl = url;
                        io.rong.imkit.tools.RongWebviewActivity.this.mWebView.loadUrl(url);
                        return true;
                    }
                } else {
                    return false;
                }
            } else {
                io.rong.imkit.tools.RongWebviewActivity.this.mPrevUrl = url;
                io.rong.imkit.tools.RongWebviewActivity.this.mWebView.loadUrl(url);
                return true;
            }
        }

        public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {
            Builder builder = new Builder(io.rong.imkit.tools.RongWebviewActivity.this);
            builder.setMessage(R.string.rc_notification_error_ssl_cert_invalid);
            builder.setPositiveButton(R.string.rc_dialog_ok, new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    handler.proceed();
                }
            });
            builder.setNegativeButton(R.string.rc_dialog_cancel, new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    handler.cancel();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }
}
