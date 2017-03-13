package com.wali.live.livesdk.live.component.presenter;


import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;

import com.base.log.MyLog;
import com.wali.live.component.presenter.ComponentPresenter;
import com.wali.live.component.view.panel.BaseBottomPanel;

/**
 * Created by yangli on 2017/03/13.
 *
 * @module 面板表现基类
 */
public abstract class BaseContainerPresenter<VIEW_GROUP extends ViewGroup>
        extends ComponentPresenter<VIEW_GROUP> {

    protected String TAG = getTAG();

    protected boolean mIsLandscape = false;

    protected BaseBottomPanel<? extends View, VIEW_GROUP> mCurrPanel;

    protected abstract String getTAG();

    public BaseContainerPresenter(@NonNull IComponentController componentController) {
        super(componentController);
    }

    protected final boolean hidePanel(boolean useAnimation) {
        if (mCurrPanel != null) {
            mCurrPanel.hideSelf(useAnimation);
            mCurrPanel = null;
            return true;
        } else {
            return false;
        }
    }

    protected final void showPanel(@Nullable BaseBottomPanel panel, boolean useAnimation) {
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
        if (mIsLandscape == isLandscape) {
            return;
        }
        mIsLandscape = isLandscape;
        if (mCurrPanel != null) {
            mCurrPanel.onOrientation(mIsLandscape);
        }
    }

}
