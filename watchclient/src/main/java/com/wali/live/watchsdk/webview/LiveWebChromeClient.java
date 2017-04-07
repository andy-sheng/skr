package com.wali.live.watchsdk.webview;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Build;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.FrameLayout;

import com.base.permission.PermissionUtils;
import com.base.utils.toast.ToastUtils;
import com.wali.live.watchsdk.R;

/**
 * Created by zhangzhiyuan on 2016/2/24.
 */
public class LiveWebChromeClient extends WebChromeClient implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {

    private View activityNonVideoView;
    private ViewGroup activityVideoView;
    private View loadingView;
    private WebView webView;
    private Window window;
    private Activity activity;
    private boolean isVideoFullscreen;
    private FrameLayout videoViewContainer;
    private CustomViewCallback videoViewCallback;

    private ToggledFullscreenCallback toggledFullscreenCallback = new ToggledFullscreenCallback() {
        @Override
        public void toggledFullscreen(boolean fullscreen) {
            if (fullscreen) {
                WindowManager.LayoutParams attrs = window.getAttributes();
                attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
                attrs.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
                window.setAttributes(attrs);
                if (Build.VERSION.SDK_INT >= 14) {
                    window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
                }
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            } else {

                WindowManager.LayoutParams attrs = window.getAttributes();
                attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
                attrs.flags &= ~WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
                window.setAttributes(attrs);
                if (Build.VERSION.SDK_INT >= 14) {
                    window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                }
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        }
    };

    private interface ToggledFullscreenCallback {
        void toggledFullscreen(boolean fullscreen);
    }

    WebViewListener mWebViewListener;

    public LiveWebChromeClient(final Activity activity, WebViewListener mWebViewListener, View activityNonVideoView, ViewGroup activityVideoView, View loadingView, WebView webView) {
        this.activity = activity;
        this.window = activity.getWindow();
        this.mWebViewListener = mWebViewListener;
        this.activityNonVideoView = activityNonVideoView;
        this.activityVideoView = activityVideoView;
        this.loadingView = loadingView;
        this.webView = webView;
        this.isVideoFullscreen = false;
    }

    @Override
    public void onReceivedTitle(WebView view, String title) {
        super.onReceivedTitle(view, title);
        mWebViewListener.onReceivedTitle(title);
    }

    @Override
    public void onProgressChanged(WebView view, int newProgress) {
        mWebViewListener.onProgressChanged(newProgress);
    }

    @Override
    public void onShowCustomView(View view, CustomViewCallback callback) {
        if (view instanceof FrameLayout) {

            FrameLayout frameLayout = (FrameLayout) view;
            frameLayout.setBackgroundColor(Color.BLACK);
            View focusedChild = frameLayout.getFocusedChild();

            this.isVideoFullscreen = true;
            this.videoViewContainer = frameLayout;
            this.videoViewCallback = callback;

            // Hide the non-video view, add the video view, and show it
            activityNonVideoView.setVisibility(View.INVISIBLE);
            activityVideoView.addView(videoViewContainer, new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            activityVideoView.setVisibility(View.VISIBLE);

            if (focusedChild instanceof android.widget.VideoView) {
                // android.widget.VideoView (typically API level <11)
                android.widget.VideoView videoView = (android.widget.VideoView) focusedChild;

                // Handle all the required events
                videoView.setOnPreparedListener(this);
                videoView.setOnCompletionListener(this);
                videoView.setOnErrorListener(this);
            } else {
                // Other classes, including:
                // - android.webkit.HTML5VideoFullScreen$VideoSurfaceView, which inherits from
                // android.view.SurfaceView (typically API level 11-18)
                // - android.webkit.HTML5VideoFullScreen$VideoTextureView, which inherits from
                // android.view.TextureView (typically API level 11-18)
                // - com.android.org.chromium.content.browser.ContentVideoView$VideoSurfaceView, which inherits from
                // android.view.SurfaceView (typically API level 19+)

                // Handle HTML5 video ended event only if the class is a SurfaceView
                // Test case: TextureView of Sony Xperia T API level 16 doesn't work fullscreen when loading the
                // javascript below
                if (webView != null && focusedChild instanceof SurfaceView) {
                    // Run javascript code that detects the video end and notifies the Javascript interface
                    String js = "javascript:";
                    js += "var _ytrp_html5_video_last;";
                    js += "var _ytrp_html5_video = document.getElementsByTagName('video')[0];";
                    js += "if (_ytrp_html5_video != undefined && _ytrp_html5_video != _ytrp_html5_video_last) {";
                    {
                        js += "_ytrp_html5_video_last = _ytrp_html5_video;";
                        js += "function _ytrp_html5_video_ended() {";
                        {
                            js += "window.imagelistner.notifyVideoEnd();"; // Must match Javascript interface name
                            // and method of VideoEnableWebView
                        }
                        js += "}";
                        js += "_ytrp_html5_video.addEventListener('ended', _ytrp_html5_video_ended);";
                    }
                    js += "}";
                    webView.loadUrl(js);
                }
            }

            // Notify full-screen change
            if (toggledFullscreenCallback != null) {
                toggledFullscreenCallback.toggledFullscreen(true);
            }
        }
    }

    // Available in　API level 14+,deprecated in　API level 18+
    @Override
    @SuppressWarnings("deprecation")
    public void onShowCustomView(View view, int requestedOrientation, CustomViewCallback callback) {
        onShowCustomView(view, callback);
    }

    @Override
    public void onHideCustomView() {
        // This method should be manually called on video end in all cases because it's not always called　automatically.
        // This method must be manually called on back key press (from this class' onBackPressed() method).

        if (isVideoFullscreen) {
            // Hide the video view, remove it, and show the non-video view
            activityVideoView.setVisibility(View.INVISIBLE);
            activityVideoView.removeView(videoViewContainer);
            activityNonVideoView.setVisibility(View.VISIBLE);

            // Call back (only in API level <19, because in API level 19+ with chromium webview it crashes)
            if (videoViewCallback != null && !videoViewCallback.getClass().getName().contains(".chromium.")) {
                videoViewCallback.onCustomViewHidden();
            }

            // Reset video related variables
            isVideoFullscreen = false;
            videoViewContainer = null;
            videoViewCallback = null;

            // Notify full-screen change
            if (toggledFullscreenCallback != null) {
                toggledFullscreenCallback.toggledFullscreen(false);
            }
            webView.clearFocus();
        }
    }

    @Override
    public View getVideoLoadingProgressView() // Video will start loading
    {
        if (loadingView != null) {
            loadingView.setVisibility(View.VISIBLE);
            return loadingView;
        }
        return loadingView;
    }

    // Video will start playing, only called in the case of android.widget.VideoView (typically API level <11)
    @Override
    public void onPrepared(MediaPlayer mp) {
        if (loadingView != null) {
            loadingView.setVisibility(View.GONE);
        }
    }

    // Video finished playing, only called in the case of android.widget.VideoView (typically API level <11)
    @Override
    public void onCompletion(MediaPlayer mp) {
        onHideCustomView();
    }

    // Error while playing video, only called in the　case of android.widget.VideoView (typically API level <11)
    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false; // By returning false, onCompletion() will be called
    }

    /**
     * Notifies the class that the back key has been pressed by the user.
     * This must be called from the Activity's onBackPressed(), and if it returns false, the activity itself should handle it. Otherwise don't do anything.
     *
     * @return Returns true if the event was handled, and false if was not (video view is not visible)
     */
    // 增加这个,好像能修某个bug
    public boolean onBackPressed() {
        if (isVideoFullscreen) {
            onHideCustomView();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onGeolocationPermissionsHidePrompt() {
        super.onGeolocationPermissionsHidePrompt();
    }

    @Override
    public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
        super.onGeolocationPermissionsShowPrompt(origin, callback);
        if (webView != null && !PermissionUtils.checkAccessLocation(webView.getContext())) {
            if (webView.getContext() instanceof Activity) {
                PermissionUtils.requestPermissionDialog((Activity) webView.getContext(), PermissionUtils.PermissionType.ACCESS_COARSE_LOCATION);
            } else {
                ToastUtils.showToast(R.string.message_location_permission);
            }
        }
        callback.invoke(origin, true, false);
    }
}
