package com.module.playways.grab.room.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.common.base.BaseActivity;
import com.common.log.MyLog;
import com.common.utils.U;
import com.common.view.titlebar.CommonTitleBar;
import com.just.agentweb.AgentWeb;
import com.just.agentweb.AgentWebSettingsImpl;
import com.just.agentweb.AgentWebUIControllerImplBase;
import com.just.agentweb.MiddlewareWebChromeBase;
import com.just.agentweb.MiddlewareWebClientBase;
import com.module.RouterConstants;
import com.module.rank.R;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.media.UMWeb;
import com.umeng.socialize.shareboard.SnsPlatform;
import com.umeng.socialize.utils.ShareBoardlistener;

import java.lang.ref.WeakReference;

import static com.common.view.titlebar.CommonTitleBar.ACTION_LEFT_TEXT;
import static com.common.view.titlebar.CommonTitleBar.ACTION_RIGHT_BUTTON;

@Route(path = RouterConstants.ACTIVITY_SHARE_WEB)
public class ShareWebActivity extends BaseActivity {

        protected AgentWeb mAgentWeb;
        private AgentWebUIControllerImplBase mAgentWebUIController;
        private ErrorLayoutEntity mErrorLayoutEntity;
        private MiddlewareWebChromeBase mMiddleWareWebChrome;
        private MiddlewareWebClientBase mMiddleWareWebClient;


        CommonTitleBar mTitlebar;
        RelativeLayout mContentContainer;

        String mTitle;
        String mDes;
        String mIcon;

        private WebChromeClient mWebChromeClient = new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                //do you work
//            Log.i("Info","onProgress:"+newProgress);
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                mTitle = title;
                if (mTitlebar != null) {
                    mTitlebar.getCenterTextView().setText(title);
                }
            }
        };

        private WebViewClient mWebViewClient = new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return super.shouldOverrideUrlLoading(view, request);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                //do you  work
                Log.i("Info", "BaseWebActivity onPageStarted");
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                view.loadUrl("javascript:window.local_obj.showSource("
                        + "document.querySelector('meta[name=\"shareIcon\"]').getAttribute('content'),"
                        + "document.querySelector('meta[name=\"description\"]').getAttribute('content')"
                        + ");");
            }
        };

        @Override
        public int initView(@Nullable Bundle savedInstanceState) {
            return R.layout.share_web_activity_layout;
        }

        CustomShareListener mShareListener;
        private ShareAction mShareAction;
        @Override
        public void initData(@Nullable Bundle savedInstanceState) {
            mTitlebar = (CommonTitleBar) this.findViewById(R.id.titlebar);
            mContentContainer = (RelativeLayout) this.findViewById(R.id.content_container);
            buildAgentWeb();

            mTitlebar.setListener(new CommonTitleBar.OnTitleBarListener() {
                @Override
                public void onClicked(View v, int action, String extra) {
                    if(action == ACTION_LEFT_TEXT){
                        finish();
                    }else if(action == ACTION_RIGHT_BUTTON){
                        mShareListener = new CustomShareListener(ShareWebActivity.this);
                        /*增加自定义按钮的分享面板*/
                    mShareAction = new ShareAction(ShareWebActivity.this).setDisplayList(
                            SHARE_MEDIA.WEIXIN, SHARE_MEDIA.WEIXIN_CIRCLE, SHARE_MEDIA.QQ)
                            .setShareboardclickCallback(new ShareBoardlistener() {
                                @Override
                                public void onclick(SnsPlatform snsPlatform, SHARE_MEDIA share_media) {

                                        UMWeb web = new UMWeb("http://test.static.inframe.mobi/app/");
                                        web.setTitle(mTitle);
                                        web.setDescription(mDes);
                                        web.setThumb(new UMImage(ShareWebActivity.this, R.drawable.share_app_icon));
                                        new ShareAction(ShareWebActivity.this).withMedia(web)
                                                .setPlatform(share_media)
                                                .setCallback(mShareListener)
                                                .share();
                                }
                            });
                    mShareAction.open();
                    }
                }
            });

            try {
                injectObj();
            }catch (Exception e){
                MyLog.e(e);
            }
        }

        private static class CustomShareListener implements UMShareListener {

            private WeakReference<ShareWebActivity> mActivity;

            private CustomShareListener(ShareWebActivity activity) {
                mActivity = new WeakReference(activity);
            }

            @Override
            public void onStart(SHARE_MEDIA platform) {

            }

            @Override
            public void onResult(SHARE_MEDIA platform) {

                if (platform.name().equals("WEIXIN_FAVORITE")) {
                    Toast.makeText(mActivity.get(), platform + " 收藏成功啦", Toast.LENGTH_SHORT).show();
                } else {
                    if (platform != SHARE_MEDIA.MORE && platform != SHARE_MEDIA.SMS
                            && platform != SHARE_MEDIA.EMAIL
                            && platform != SHARE_MEDIA.FLICKR
                            && platform != SHARE_MEDIA.FOURSQUARE
                            && platform != SHARE_MEDIA.TUMBLR
                            && platform != SHARE_MEDIA.POCKET
                            && platform != SHARE_MEDIA.PINTEREST
                            && platform != SHARE_MEDIA.INSTAGRAM
                            && platform != SHARE_MEDIA.GOOGLEPLUS
                            && platform != SHARE_MEDIA.YNOTE
                            && platform != SHARE_MEDIA.EVERNOTE) {
                        Toast.makeText(mActivity.get(), "分享成功啦", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onError(SHARE_MEDIA platform, Throwable t) {
                if (platform != SHARE_MEDIA.MORE && platform != SHARE_MEDIA.SMS
                        && platform != SHARE_MEDIA.EMAIL
                        && platform != SHARE_MEDIA.FLICKR
                        && platform != SHARE_MEDIA.FOURSQUARE
                        && platform != SHARE_MEDIA.TUMBLR
                        && platform != SHARE_MEDIA.POCKET
                        && platform != SHARE_MEDIA.PINTEREST
                        && platform != SHARE_MEDIA.INSTAGRAM
                        && platform != SHARE_MEDIA.GOOGLEPLUS
                        && platform != SHARE_MEDIA.YNOTE
                        && platform != SHARE_MEDIA.EVERNOTE) {
                    Toast.makeText(mActivity.get(), "分享失败啦", Toast.LENGTH_SHORT).show();

                }

            }

            @Override
            public void onCancel(SHARE_MEDIA platform) {

                Toast.makeText(mActivity.get(), "分享取消了", Toast.LENGTH_SHORT).show();
            }
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

        /**
         * 更多使用实例，参考 AgentWeb 官网上的 samples
         */
        protected void buildAgentWeb() {
            ErrorLayoutEntity mErrorLayoutEntity = getErrorLayoutEntity();
            String url = getIntent().getStringExtra("url");
            mAgentWeb = AgentWeb.with(this)
                    .setAgentWebParent(mContentContainer, new RelativeLayout.LayoutParams(-1, -1))
                    .useDefaultIndicator(Color.parseColor("#ff0000"), 3)
                    .setWebChromeClient(mWebChromeClient)
                    .setWebViewClient(mWebViewClient)
                    .setMainFrameErrorView(mErrorLayoutEntity.layoutRes, mErrorLayoutEntity.reloadId)
//                .setWebView(getWebView())
//                .setPermissionInterceptor(getPermissionInterceptor())
//                .setWebLayout(getWebLayout())
//                .setAgentWebUIController(getAgentWebUIController())
//                .interceptUnkownUrl()
//                .setOpenOtherPageWays(getOpenOtherAppWay())
                    .useMiddlewareWebChrome(getMiddleWareWebChrome())
                    .useMiddlewareWebClient(getMiddleWareWebClient())
                    .setAgentWebWebSettings(AgentWebSettingsImpl.getInstance())
//                .setSecurityType(AgentWeb.SecurityType.STRICT_CHECK)
                    .createAgentWeb()
                    .ready()
                    .go(url);
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
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
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



