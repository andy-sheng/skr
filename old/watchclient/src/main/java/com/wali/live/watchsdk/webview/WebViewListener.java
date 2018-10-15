package com.wali.live.watchsdk.webview;

/**
 * Created by Lan on 1/26/16.
 */
public abstract class WebViewListener {
    public void onProgressChanged(int progress) {
    }

    public void onReceivedTitle(String title) {
    }

    public void onReceivedError(String description, String failingUrl) {
    }

    public void onPageStarted(String url) {
    }

    public void onPageFinished(String url) {
    }

    public void onLoadResource(String url) {
    }

    public void onPageCommitVisible(String url) {
    }
}
