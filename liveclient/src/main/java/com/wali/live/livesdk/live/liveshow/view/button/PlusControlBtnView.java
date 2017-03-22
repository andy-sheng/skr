package com.wali.live.livesdk.live.liveshow.view.button;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.log.MyLog;
import com.base.utils.display.DisplayUtils;
import com.wali.live.component.view.IComponentView;
import com.wali.live.component.view.IOrientationListener;
import com.wali.live.component.view.IViewProxy;
import com.wali.live.livesdk.R;

/**
 * Created by yangli on 2017/03/08.
 * <p>
 * Generated using create_component_view.py
 *
 * @module 直播加按钮视图
 */
public class PlusControlBtnView extends RelativeLayout implements View.OnClickListener,
        IComponentView<PlusControlBtnView.IPresenter, PlusControlBtnView.IView> {
    private static final String TAG = "PlusControlBtnView";

    @Nullable
    protected IPresenter mPresenter;

    private final static int PADDING = DisplayUtils.dip2px(10f);

    private boolean mIsLandscape = false;

    private ImageView mIconView;
    private TextView mInfoView;

    protected final <T extends View> T $(@IdRes int resId) {
        return (T) findViewById(resId);
    }

    protected final void $click(View view, View.OnClickListener listener) {
        if (view != null) {
            view.setOnClickListener(listener);
        }
    }

    @Override
    public void setPresenter(@Nullable IPresenter iPresenter) {
        mPresenter = iPresenter;
    }

    @Override
    public void onClick(View view) {
        if (mPresenter != null && view.getId() == R.id.info_view) {
            mPresenter.notifyInfoViewClick();
        }
    }

    public PlusControlBtnView(Context context) {
        this(context, null);
    }

    public PlusControlBtnView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PlusControlBtnView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        inflate(context, R.layout.plus_control_btn_view, this);

        mIconView = $(R.id.icon_view);
        mInfoView = $(R.id.info_view);

        $click(mInfoView, this);
    }

    public void onOrientation(boolean isLandscape) {
        if (mIsLandscape == isLandscape) {
            return;
        }
        mIsLandscape = isLandscape;
        MyLog.d(TAG, "onOrientation isLandscape=" + isLandscape);
        if (mIsLandscape) {
            mInfoView.setMaxWidth(DisplayUtils.dip2px(30f));
            mInfoView.setPadding(DisplayUtils.dip2px(5f), PADDING, DisplayUtils.dip2px(5f), PADDING);
        } else {
            mInfoView.setPadding(PADDING, 0, PADDING, 0);
            mInfoView.setMaxWidth(Integer.MAX_VALUE);
        }
    }

    @Override
    public IView getViewProxy() {
        /**
         * 局部内部类，用于Presenter回调通知该View改变状态
         */
        class ComponentView implements IView {
            @Nullable
            @Override
            public <T extends View> T getRealView() {
                return (T) PlusControlBtnView.this;
            }

            @Override
            public void enterInfoMode() {
                mIconView.setVisibility(View.GONE);
                mInfoView.setVisibility(View.VISIBLE);
            }

            @Override
            public void exitInfoMode() {
                mIconView.setVisibility(View.VISIBLE);
                mInfoView.setVisibility(View.GONE);
            }

            @Override
            public void setInfoText(String infoText) {
                mInfoView.setText(infoText);
            }

            @Override
            public void onOrientation(boolean isLandscape) {
                PlusControlBtnView.this.onOrientation(isLandscape);
            }
        }
        return new ComponentView();
    }

    public interface IPresenter {
        /**
         * 显示文本
         */
        void notifyInfoViewClick();
    }

    public interface IView extends IViewProxy, IOrientationListener {
        /**
         * 显示文本
         */
        void enterInfoMode();

        /**
         * 显示图标
         */
        void exitInfoMode();

        /**
         * 设置按钮文本
         */
        void setInfoText(String infoText);
    }
}
