package com.imagebrowse;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

// todo 需要补一个图片在放大状态的时候，不要拦截上下滑动处理
public class SlideColseLayout extends RelativeLayout {

    public final static String TAG = "SlideColseLayout";

    int previousX;
    int previousY;

    LayoutScrollListener mScrollListener;
    Drawable mBackground;
    boolean isScrollingUp;

    public SlideColseLayout(Context context) {
        super(context);
    }

    public SlideColseLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SlideColseLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setGradualBackground(Drawable background) {
        this.mBackground = background;
    }

    public void setLayoutScrollListener(LayoutScrollListener listener) {
        this.mScrollListener = listener;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        //判断几指操作，大于1时认为在对图片进行放大缩小操作，不拦截事件
        //交由下面控件处理
        if (ev.getPointerCount() > 1) {
            return false;
        } else {
            final int y = (int) ev.getRawY();
            final int x = (int) ev.getRawX();
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    previousX = x;
                    previousY = y;
                    break;
                case MotionEvent.ACTION_MOVE:
                    int diffY = y - previousY;
                    int diffX = x - previousX;
                    //当Y轴移动距离大于X轴50个单位时拦截事件
                    //进入onTouchEvent开始处理上下滑动退出效果
                    if (Math.abs(diffX) + 50 < Math.abs(diffY)) {
                        return true;
                    }
                    break;
            }
            return false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int y = (int) event.getRawY();
        final int x = (int) event.getRawX();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                previousX = x;
                previousY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                int diffY = y - previousY;
                //判断手指向上还是向下移动，关联手指抬起后的动画位移方向
                isScrollingUp = diffY <= 0;
                this.setTranslationY(diffY);
                if (mBackground != null) {
                    //透明度跟随手指的移动距离发生变化
                    int alpha = (int) (255 * Math.abs(diffY * 1f)) / getHeight();
                    mBackground.setAlpha(255 - alpha);
                    //回调给外面做更多操作
                    mScrollListener.onLayoutScrolling(alpha / 255f);
                }
                break;
            case MotionEvent.ACTION_UP:
                int height = this.getHeight();
                //滑动距离超过临界值才执行退出动画，临界值为控件高度1/4
                if (Math.abs(getTranslationY()) > (height / 4)) {
                    //执行退出动画
                    layoutExitAnim();
                } else {
                    //执行恢复动画
                    layoutRecoverAnim();
                }
        }
        return true;
    }

    public void layoutExitAnim() {
        ObjectAnimator exitAnim;
        //从手指抬起的位置继续向上或向下的位移动画
        exitAnim = ObjectAnimator.ofFloat(this, "translationY", getTranslationY(), isScrollingUp ? -getHeight() : getHeight());
        exitAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                //动画结束时将背景置为完全透明
                if (mBackground != null) {
                    mBackground.setAlpha(0);
                }
                //执行回调，退出页面
                if (mScrollListener != null) {
                    mScrollListener.onLayoutClosed();
                }

            }
        });
        exitAnim.addUpdateListener(animation -> {
            if (mBackground != null) {
                //根据位移计算设置背景透明度
                int alpha = (int) (255 * Math.abs(getTranslationY() * 1f)) / getHeight();
                mBackground.setAlpha(255 - alpha);
            }
        });
        exitAnim.setDuration(200);
        exitAnim.start();
    }


    private void layoutRecoverAnim() {
        //从手指抬起的地方恢复到原点
        ObjectAnimator recoverAnim = ObjectAnimator.ofFloat(this, "translationY", this.getTranslationY(), 0);
        recoverAnim.setDuration(100);
        recoverAnim.start();
        if (mBackground != null) {
            //将背景置为完全不透明
            mBackground.setAlpha(255);
            mScrollListener.onLayoutScrollRevocer();
        }
    }


    public interface LayoutScrollListener {
        //关闭布局
        void onLayoutClosed();

        //正在滑动
        void onLayoutScrolling(float alpha);

        //滑动结束并且没有触发关闭
        void onLayoutScrollRevocer();
    }
}
