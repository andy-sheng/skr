package com.wali.live.watchsdk.component.view;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.base.activity.RxActivity;
import com.base.log.MyLog;
import com.mi.live.data.push.model.BarrageMsg;
import com.thornbirds.component.view.IComponentView;
import com.thornbirds.component.view.IViewProxy;
import com.wali.live.common.gift.utils.AnimationPlayControlTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zyh on 2018/01/02.
 *
 * @module 弹幕消息vip入场动画view
 */
public class BarrageControlAnimView extends RelativeLayout implements IComponentView<BarrageControlAnimView.IPresenter,
        BarrageControlAnimView.IView> {
    private static final String TAG = "BarrageControlAnimView" + "_BarrageAnimView";
    private AnimationPlayControlTemplate<BarrageMsg> mFlyBarrageControl; //播放队列控制器
    private List<IAnimView> mAnimViews = new ArrayList<>();

    private BarrageAnimView mBarrageAnimView;

    @Nullable
    protected IPresenter mPresenter;

    @Override
    public void setPresenter(@Nullable IPresenter iPresenter) {
        mPresenter = iPresenter;
    }

    public void setJoinAnimEnable(boolean enable) {
        if (mBarrageAnimView != null) {
            mBarrageAnimView.setJoinAnimEnable(enable);
        }
    }

    protected final <T extends View> T $(@IdRes int resId) {
        return (T) findViewById(resId);
    }

    protected final void $click(View view, View.OnClickListener listener) {
        if (view != null) {
            view.setOnClickListener(listener);
        }
    }

    public BarrageControlAnimView(Context context) {
        this(context, null);
    }

    public BarrageControlAnimView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BarrageControlAnimView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        initView();
        initData();
    }

    private void initData() {
        mFlyBarrageControl = new AnimationPlayControlTemplate<BarrageMsg>((RxActivity) getContext(), false, 4) {
            @Override
            public void onStart(BarrageMsg model) {
                for (IAnimView animView : mAnimViews) {
                    if (animView.isAccepted(model)) {
                        animView.onStart();
                    }
                }
            }

            @Override
            protected void onEnd(BarrageMsg model) {
                for (IAnimView animView : mAnimViews) {
                    animView.onEnd();
                }
            }
        };
    }

    private void setData(BarrageMsg model) {
        MyLog.d(TAG, "setData model=" + model);
        for (IAnimView animView : mAnimViews) {
            if (animView.isAccepted(model)) {
                mFlyBarrageControl.add(model, true);
            }
        }
    }

    private void initView() {
        mAnimViews.add(new BarrageAnimView(getContext()));
        for (IAnimView view : mAnimViews) {
            addView((View) view);
        }
    }

    private void destroy() {
        MyLog.w(TAG, "destroy");
        mFlyBarrageControl.destroy();
        if (mAnimViews != null) {
            for (IAnimView item : mAnimViews) {
                item.onDestroy();
            }
        }
    }

    @Override
    public IView getViewProxy() {
        class ComponentView implements IView {
            @Override
            public <T extends View> T getRealView() {
                return (T) BarrageControlAnimView.this;
            }

            @Override
            public void setData(BarrageMsg model) {
                BarrageControlAnimView.this.setData(model);
            }

            @Override
            public void setJoinEnable(boolean enable) {
                BarrageControlAnimView.this.setJoinAnimEnable(enable);
            }

            @Override
            public void destroy() {
                BarrageControlAnimView.this.destroy();
            }
        }
        return new ComponentView();
    }

    public interface IPresenter {
    }

    public interface IView extends IViewProxy {
        void setData(BarrageMsg model);

        void setJoinEnable(boolean enable);

        void destroy();
    }
}
