package com.wali.live.component.view;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;

import com.base.log.MyLog;
import com.wali.live.component.view.panel.BaseBottomPanel;

/**
 * Created by yangli on 17-2-14.
 *
 * @module 底部面板
 */
public abstract class BasePanelContainer<PRESENTER, VIEW extends IViewProxy, VIEW_GROUP extends ViewGroup>
        implements IComponentView<PRESENTER, VIEW> {
    protected String TAG = getTAG();

    protected @Nullable PRESENTER mPresenter;

    protected @NonNull VIEW_GROUP mPanelContainer;
    protected BaseBottomPanel<? extends View, VIEW_GROUP> mCurrPanel;

    protected boolean mIsLandscape = false;

    protected String getTAG() {
        return BaseBottomPanel.class.getSimpleName();
    }

    @Override
    public final void setPresenter(@Nullable PRESENTER presenter) {
        mPresenter = presenter;
    }

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

    protected final boolean hidePanel(boolean useAnimation) {
        if (mCurrPanel != null) {
            mCurrPanel.hideSelf(useAnimation);
            mCurrPanel = null;
            return true;
        } else {
            return false;
        }
    }

    protected void showPanel(@Nullable BaseBottomPanel panel, boolean useAnimation) {
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
            if (mCurrPanel != null) {
                mCurrPanel.onOrientation(mIsLandscape);
            }
        }
    }
}
