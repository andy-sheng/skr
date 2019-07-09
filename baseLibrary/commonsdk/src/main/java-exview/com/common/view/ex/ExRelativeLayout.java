package com.common.view.ex;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

import com.common.base.R;
import com.common.lifecycle.ActivityLifecycleForRxLifecycle;
import com.common.rx.ViewEvent;
import com.common.view.ex.shadow.ShadowConfig;
import com.common.view.ex.shadow.ShadowHelper;
import com.trello.rxlifecycle2.LifecycleTransformer;
import com.trello.rxlifecycle2.RxLifecycle;

import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;

public class ExRelativeLayout extends RelativeLayout implements RxLifecycleView{

    ShadowConfig mShadowConfig;

    public ExRelativeLayout(Context context) {
        super(context);
    }

    public ExRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        loadAttributes(context, attrs);
    }

    public ExRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        loadAttributes(context, attrs);
    }

    private void loadAttributes(Context context, AttributeSet attrs) {
        AttributeInject.injectBackground(this, context, attrs);
//        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.background);
//        int shadowColor = typedArray.getInt(R.styleable.background_bl_shadow_Color, Color.TRANSPARENT);
//        typedArray.recycle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mShadowConfig != null) {
            //实现阴影
            ShadowHelper.draw(canvas, this, mShadowConfig);
        }
    }

    public void setShadowConfig(ShadowConfig shadowConfig) {
        mShadowConfig = shadowConfig;
        //invalidate();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        /**
         * 是否需要在顶部把这个事件给拦截住，目前用于点击空白区域 ，美颜面板的消失
         */
        if (mListener != null && mListener.onInterceptTouchEvent(ev)) {
            return true;
        }
        return super.onInterceptTouchEvent(ev);
    }

    Listener mListener;

    public void setListener(Listener listener) {
        mListener = listener;
    }

    private BehaviorSubject<ViewEvent> mLifecycleSubject =  null;

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mLifecycleSubject != null) {
            mLifecycleSubject.onNext(ViewEvent.DETACH);
        }
    }

    /**
     * 事件在 {@link ActivityLifecycleForRxLifecycle}发出
     * 绑定 Activity 的指定生命周期
     *
     * @param <T>
     * @return
     */
    @Override
    public <T> LifecycleTransformer<T> bindDetachEvent() {
        return RxLifecycle.bindUntilEvent(provideLifecycleSubject(), ViewEvent.DETACH);
    }

    @NonNull
    public final Subject<ViewEvent> provideLifecycleSubject() {
        if(mLifecycleSubject==null){
            mLifecycleSubject = BehaviorSubject.create();
        }
        return mLifecycleSubject;
    }

    public interface Listener {
        boolean onInterceptTouchEvent(MotionEvent ev);
    }
}
