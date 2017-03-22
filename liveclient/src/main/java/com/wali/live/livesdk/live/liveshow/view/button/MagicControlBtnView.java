package com.wali.live.livesdk.live.liveshow.view.button;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.wali.live.component.view.IComponentView;
import com.wali.live.component.view.IViewProxy;
import com.wali.live.livesdk.R;

/**
 * Created by yangli on 2017/03/09.
 * <p>
 * Generated using create_component_view.py
 *
 * @module 美妆按钮视图
 */
public class MagicControlBtnView extends FrameLayout implements IComponentView<MagicControlBtnView.IPresenter, MagicControlBtnView.IView> {
    private static final String TAG = "MagicControlBtnView";

    @Nullable
    protected IPresenter mPresenter;

    ImageView mMagicControlBtn;

    ImageView mAlertRedIcon;

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

    public MagicControlBtnView(Context context) {
        this(context, null);
    }

    public MagicControlBtnView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MagicControlBtnView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        inflate(context, R.layout.magic_control_btn_view, this);

        mMagicControlBtn = $(R.id.magic_control_btn);
        mAlertRedIcon = $(R.id.alert_red_icon);
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
                return (T) MagicControlBtnView.this;
            }

            @Override
            public void showRedDot(boolean isShow) {
                mAlertRedIcon.setVisibility(isShow ? View.VISIBLE : View.GONE);
            }
        }
        return new ComponentView();
    }

    public interface IPresenter {
    }

    public interface IView extends IViewProxy {
        /**
         * 是否显示红点
         */
        void showRedDot(boolean isShow);
    }
}
