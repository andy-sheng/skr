package com.wali.live.livesdk.live.view.container;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

import com.base.log.MyLog;
import com.wali.live.livesdk.live.view.panel.BaseSettingPanel;

/**
 * Created by yangli on 17-2-14.
 *
 * @module 底部面板
 */
public class BasePanelContainer<VIEW_GROUP extends ViewGroup> {
    protected String TAG = getTAG();

    protected @NonNull VIEW_GROUP mPanelContainer;
    protected BaseSettingPanel mCurrPanel;

    protected boolean mIsLandscape = false;

    public BasePanelContainer(@NonNull VIEW_GROUP panelContainer) {
        mPanelContainer = panelContainer;
        mPanelContainer.setSoundEffectsEnabled(false);
        mPanelContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hidePanel(true);
            }
        });
    }

    protected String getTAG() {
        return BaseSettingPanel.class.getSimpleName();
    }

    protected boolean hidePanel(boolean useAnimation) {
        if (mCurrPanel != null) {
            mCurrPanel.hideSelf(useAnimation);
            mCurrPanel = null;
            return true;
        } else {
            return false;
        }
    }

    protected void showPanel(BaseSettingPanel panel, boolean useAnimation) {
        if (mCurrPanel != null && mCurrPanel == panel) {
            return;
        }
        if (mCurrPanel != null && mCurrPanel != panel) {
            mCurrPanel.hideSelf(useAnimation);
        }
        mCurrPanel = panel;
        if (mCurrPanel != null) {
            mCurrPanel.showSelf(useAnimation, mIsLandscape);
        }
    }

    @CallSuper
    public void onOrientation(boolean isLandscape) {
        MyLog.w(TAG, "onOrientation isLandscape=" + isLandscape);
        if (mIsLandscape != isLandscape) {
            mIsLandscape = isLandscape;
            mCurrPanel.onOrientation(mIsLandscape);
        }
    }

    public boolean processBackPress() {
        return hidePanel(true);
    }
}
