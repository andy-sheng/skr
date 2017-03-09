package com.wali.live.component.view;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.base.log.MyLog;
import com.base.utils.display.DisplayUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yangli on 16-8-29.
 *
 * @module 底部按钮视图
 */
public abstract class BaseBottomButton<PRESENTER, VIEW extends IViewProxy> implements
        View.OnClickListener, IComponentView<PRESENTER, VIEW> {
    protected final String TAG = getTAG();

    private static final int BTN_MARGIN = DisplayUtils.dip2px(3.33f);
    private static final int BTN_MARGIN_LEFT = DisplayUtils.dip2px(6.67f);

    protected RelativeLayout mContentContainer;
    protected final List<View> mLeftBtnSetPort = new ArrayList<>();
    protected final List<View> mRightBtnSetPort = new ArrayList<>();
    protected final List<View> mBottomBtnSetLand = new ArrayList<>();

    protected boolean mIsLandscape = false;
    protected @Nullable PRESENTER mPresenter;

    protected abstract String getTAG();

    @Override
    public final void setPresenter(@Nullable PRESENTER presenter) {
        mPresenter = presenter;
    }

    protected final Context getContext() {
        return mContentContainer.getContext();
    }

    protected final ImageView createImageView(@DrawableRes int resId) {
        ImageView imageView = new ImageView(getContext());
        imageView.setImageResource(resId);
        return imageView;
    }

    protected final void addCreatedView(@NonNull View view, @IdRes int id) {
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.alignWithParent = true;
        layoutParams.setMargins(BTN_MARGIN, BTN_MARGIN, BTN_MARGIN, BTN_MARGIN);
        mContentContainer.addView(view, layoutParams);
        view.setId(id);
        view.setOnClickListener(this);
    }

    public BaseBottomButton(@NonNull RelativeLayout contentContainer) {
        mContentContainer = contentContainer;
        mContentContainer.removeAllViews();
        mContentContainer.setPadding(BTN_MARGIN, BTN_MARGIN, BTN_MARGIN, BTN_MARGIN);
    }

    protected void orientSelf(boolean isLandscape) {
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mContentContainer.getLayoutParams();
        if (isLandscape) {
            lp.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
            lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0);
            lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
        } else {
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
            lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
            lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        }
    }

    private void resetChildLayout(RelativeLayout.LayoutParams lp) {
        lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
        lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
        lp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
        lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0);
        lp.addRule(RelativeLayout.LEFT_OF, 0);
        lp.addRule(RelativeLayout.RIGHT_OF, 0);
        lp.addRule(RelativeLayout.ABOVE, 0);
        lp.addRule(RelativeLayout.BELOW, 0);
    }

    protected void alignViewToGuard(View view, int guardId, int verb, int verbDefault) {
        if (view == null) {
            MyLog.e(TAG, "alignViewToGuard, but view is null");
            return;
        }
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
        resetChildLayout(layoutParams);
        if (guardId != 0) {
            layoutParams.addRule(verb, guardId);
        } else {
            layoutParams.addRule(verbDefault, RelativeLayout.TRUE);
        }
        view.setLayoutParams(layoutParams);
    }

    protected void orientChild(boolean isLandscape) {
        if (isLandscape) {
            int guardId = 0;
            for (View view : mBottomBtnSetLand) {
                alignViewToGuard(view, guardId, RelativeLayout.ABOVE, RelativeLayout.ALIGN_PARENT_BOTTOM);
                guardId = view.getId();
            }
        } else {
            int guardId = 0;
            for (View view : mLeftBtnSetPort) {
                alignViewToGuard(view, guardId, RelativeLayout.RIGHT_OF, RelativeLayout.ALIGN_PARENT_LEFT);
                guardId = view.getId();
            }
            guardId = 0;
            for (View view : mRightBtnSetPort) {
                alignViewToGuard(view, guardId, RelativeLayout.LEFT_OF, RelativeLayout.ALIGN_PARENT_RIGHT);
                guardId = view.getId();
            }
        }
    }

    public void onOrientation(boolean isLandscape) {
        mIsLandscape = isLandscape;
        orientSelf(mIsLandscape);
        orientChild(mIsLandscape);
    }

}
