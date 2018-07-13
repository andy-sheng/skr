package com.wali.live.component.view;

import android.content.Context;
import android.graphics.Color;
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
import com.thornbirds.component.view.IComponentView;
import com.thornbirds.component.view.IViewProxy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by yangli on 16-8-29.
 *
 * @module 底部按钮视图
 */
public abstract class BaseBottomButton<PRESENTER, VIEW extends IViewProxy> implements
        View.OnClickListener, IComponentView<PRESENTER, VIEW> {
    protected final String TAG = getTAG();

    protected static final int BTN_MARGIN = DisplayUtils.dip2px(3.33f);
    protected static final int BTN_MARGIN_LEFT = DisplayUtils.dip2px(6.67f);

    protected RelativeLayout mContentContainer;
    protected final List<View> mLeftBtnSetPort = new ArrayList<>();
    protected final List<View> mRightBtnSetPort = new ArrayList<>();
    protected final List<View> mAboveTheRightBtnSetPort = new ArrayList<>();

    protected final List<View> mBottomBtnSetLand = new ArrayList<>();

    protected boolean mIsLandscape = false;
    @Nullable
    protected PRESENTER mPresenter;

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

    protected final void addCreatedView(@NonNull View view, int width, int height, @IdRes int id) {
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(width, height);
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

    protected void resetChildLayout(RelativeLayout.LayoutParams lp) {
        lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
        lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
        lp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
        lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0);
        lp.addRule(RelativeLayout.LEFT_OF, 0);
        lp.addRule(RelativeLayout.RIGHT_OF, 0);
        lp.addRule(RelativeLayout.ABOVE, 0);
        lp.addRule(RelativeLayout.BELOW, 0);
    }

    static class AlginParams {
        public int guardId;
        public int verb;
        public int verbDefault;

        public AlginParams(int guardId, int verb, int verbDefault) {
            this.guardId = guardId;
            this.verb = verb;
            this.verbDefault = verbDefault;
        }
    }

    protected void alignViewToGuard(View view, List<AlginParams> list) {
        if (view == null) {
            MyLog.e(TAG, "alignViewToGuard, but view is null");
            return;
        }
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
        resetChildLayout(layoutParams);
        for (AlginParams alginParams : list) {
            if (alginParams.guardId != 0) {
                layoutParams.addRule(alginParams.verb, alginParams.guardId);
            } else {
                layoutParams.addRule(alginParams.verbDefault, RelativeLayout.TRUE);
            }
        }
        view.setLayoutParams(layoutParams);
    }

    protected void alignViewToGuard(View view, int guardId, int verb, int verbDefault) {
        alignViewToGuard(view, Arrays.asList(new AlginParams(guardId, verb, verbDefault)));
    }

    protected void orientChild() {
        if (mIsLandscape) {
            int guardId = 0;
            for (View view : mBottomBtnSetLand) {
                if (view.getVisibility() == View.VISIBLE) {
                    List<AlginParams> list = new ArrayList<>();
                    list.add(new AlginParams(guardId, RelativeLayout.ABOVE, RelativeLayout.ALIGN_PARENT_BOTTOM));
                    list.add(new AlginParams(0, 0, RelativeLayout.ALIGN_PARENT_RIGHT));
                    alignViewToGuard(view, list);
                    guardId = view.getId();
                }
            }
        } else {
            int guardId = 0;
            for (View view : mLeftBtnSetPort) {
                if (view.getVisibility() == View.VISIBLE) {
                    alignViewToGuard(view, guardId, RelativeLayout.RIGHT_OF, RelativeLayout.ALIGN_PARENT_LEFT);
                    guardId = view.getId();
                }
            }


            guardId = 0;
            for (int j = 0; j < mAboveTheRightBtnSetPort.size(); j++) {
                View view2 = mAboveTheRightBtnSetPort.get(j);
                if (view2.getVisibility() == View.VISIBLE) {
                    List<AlginParams> list = new ArrayList<>();
                    if (j != 0) {
                        list.add(new AlginParams(guardId, RelativeLayout.ABOVE, 0));
                    }else{
                    }
                    list.add(new AlginParams(0, 0, RelativeLayout.ALIGN_PARENT_RIGHT));
                    alignViewToGuard(view2, list);
                    guardId = view2.getId();
                }
            }


            guardId = 0;
            for (int i = 0; i < mRightBtnSetPort.size(); i++) {
                View view = mRightBtnSetPort.get(i);
                if (view.getVisibility() == View.VISIBLE) {

                    List<AlginParams> list = new ArrayList<>();
                    if (i != 0) {
                        list.add(new AlginParams(guardId, RelativeLayout.LEFT_OF, RelativeLayout.ALIGN_PARENT_RIGHT));
                    } else {
                        list.add(new AlginParams(0, 0, RelativeLayout.ALIGN_PARENT_RIGHT));
                    }
                    if (!mAboveTheRightBtnSetPort.isEmpty()) {
                        int belowId = mAboveTheRightBtnSetPort.get(0).getId();
                        list.add(new AlginParams(belowId, RelativeLayout.BELOW, 0));
                    }

                    alignViewToGuard(view, list);

                    guardId = view.getId();
                }
            }
        }
    }

    protected void orientSelf() {
        RelativeLayout.LayoutParams lp =
                (RelativeLayout.LayoutParams) mContentContainer.getLayoutParams();
        if (mIsLandscape) {
            lp.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
        } else {
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
            lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        }
    }

    public final void onOrientation(boolean isLandscape) {
        if (mIsLandscape != isLandscape) {
            mIsLandscape = isLandscape;
            orientSelf();
            orientChild();
        }
    }

}
