package com.module.playways.grab.room.view;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;

/**
 * 包裹懒加载view的一些常用方法
 */
public abstract class ExViewStub {
    protected ViewStub mViewStub;
    protected View mParentView;

    public ExViewStub(ViewStub viewStub) {
        mViewStub = viewStub;
    }

    public void tryInflate() {
        if (mParentView == null) {
            mParentView = mViewStub.inflate();
            init(mParentView);
            mViewStub = null;
        }
    }

    protected abstract void init(View parentView);

    public void setVisibility(int visibility) {
        if (visibility == View.GONE) {
            if (mParentView != null) {
                mParentView.setVisibility(View.GONE);
                mParentView.clearAnimation();
            }
        } else {
            tryInflate();
            mParentView.setVisibility(visibility);
        }
    }
}
