package com.common.utils;

import android.app.Activity;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import com.common.base.BaseActivity;
import com.common.log.MyLog;

import org.greenrobot.eventbus.EventBus;

import java.lang.ref.WeakReference;


/**
 * 这个类我一定好好改改，flag
 */
public class AndroidBug5497WorkaroundSupportingTranslucentStatus {
    public final static String TAG = "AndroidBug5497WorkaroundSupportingTranslucentStatus";
    private WeakReference<BaseActivity> mBaseActivityRef;
    private String mFrom = "";
    // 我们页面的根布局以及layoutparams
    private View mChildOfContent;
    private FrameLayout.LayoutParams frameLayoutParams;

    // 我们视图view先前的高度
    private int usableHeightPrevious;


    ViewTreeObserver.OnGlobalLayoutListener mOnGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        public void onGlobalLayout() {
            /**
             * 这里要特别注意，如果又两个Activity A B，B在A之上，在B中弹出键盘，A中的  onGlobalLayout 也会发生回调
             */
            // 根布局需要重新计算layout时就会触发
            possiblyResizeChildOfContent();
        }
    };

    public static AndroidBug5497WorkaroundSupportingTranslucentStatus assistActivity(BaseActivity activity) {
        return new AndroidBug5497WorkaroundSupportingTranslucentStatus(activity);
    }

    private AndroidBug5497WorkaroundSupportingTranslucentStatus(BaseActivity activity) {
        mBaseActivityRef = new WeakReference<>(activity);
        mFrom = activity.getClass().getName();
        /**
         * 这里优化下
         * 1. 为了支持fragment，支持自己传入根view
         * 2. 希望只有view在前台可见时响应这个事件
         * 3. 兼容activity一开始没setContentView，后面又addFragment这种情况
         */
        // 拿到页面根布局
        FrameLayout content = (FrameLayout) activity.findViewById(android.R.id.content);
        // 拿到activity页面的 rootView
        mChildOfContent = content.getChildAt(0);
        /**
         * 所以这里就有一个bug
         */
        mChildOfContent.getViewTreeObserver().addOnGlobalLayoutListener(mOnGlobalLayoutListener);
        frameLayoutParams = (FrameLayout.LayoutParams) mChildOfContent.getLayoutParams();
    }

    private void possiblyResizeChildOfContent() {
        MyLog.d(TAG, "possiblyResizeChildOfContent mFrom:" + mFrom);
        Activity curActivity = U.getActivityUtils().getCurrentActivity();
        if (curActivity != mBaseActivityRef.get()) {
            MyLog.d(TAG, "not curActivity ,return");
            /**
             * 如果不是当前acitivity发出的就忽略吧。
             */
            return;
        }
        /**
         * 键盘弹出时
         * usableHeightNow = 1117-55 = 1062
         */
        int usableHeightNow = computeUsableHeight();
        MyLog.d(TAG, "possiblyResizeChildOfContent usableHeightNow:" + usableHeightNow);
        if (usableHeightNow != usableHeightPrevious) {
            /**
             * usableHeightSansKeyboard 这里为 1920 手机高度
             */
            int usableHeightSansKeyboard = mChildOfContent.getRootView().getHeight();
            /**
             * heightDifference = 1920-1062 = 858 可以认为是键盘高度
             */
            int heightDifference = usableHeightSansKeyboard - usableHeightNow;
            /**
             * 是否自己控制布局，当有键盘事件时
             */
            boolean resizeSelf = mBaseActivityRef.get().resizeLayoutSelfWhenKeybordShow();
            MyLog.d(TAG, "possiblyResizeChildOfContent usableHeightSansKeyboard=" + usableHeightSansKeyboard
                    + " usableHeightNow=" + usableHeightNow
                    + " heightDifference=" + heightDifference
                    + " resizeSelf=" + resizeSelf);

            if (heightDifference > (usableHeightSansKeyboard / 4)) {
                MyLog.d(TAG, "键盘变为可见");
                /**
                 * 键盘变为可见,这里有几种操作
                 * 1.改动根布局高度
                 * 2.不改变根布局高度，发event告知改 Activiy 键盘出现，让业务自己处理，
                 *  比如修改 PlaceHolderView 的高度
                 */
                if (resizeSelf) {
                    KeyboardEvent keyboardEvent = new KeyboardEvent(mFrom, KeyboardEvent.EVENT_TYPE_KEYBOARD_VISIBLE, heightDifference);
                    EventBus.getDefault().post(keyboardEvent);
                } else {
                    frameLayoutParams.height = usableHeightSansKeyboard - heightDifference;
                }
            } else {
                MyLog.d(TAG, "键盘变为不可见");
                // 键盘变为不可见
                if (resizeSelf) {
                    KeyboardEvent keyboardEvent = new KeyboardEvent(mFrom, KeyboardEvent.EVENT_TYPE_KEYBOARD_HIDDEN, 0);
                    EventBus.getDefault().post(keyboardEvent);
                }
                frameLayoutParams.height = usableHeightSansKeyboard;
            }
            mChildOfContent.requestLayout();
            usableHeightPrevious = usableHeightNow;
        }
    }

    /**
     * 返回我们视图view的高度
     *
     * @return
     */
    private int computeUsableHeight() {
        /**
         * 1080x1920手机，弹出键盘时
         * r.left=0
         * r.right=1080
         * r.top=55
         * r.bottom=1117
         *
         * 未弹出键盘时
         *  r.top=55
         *  r.bottom=1920
         */
        Rect r = new Rect();
        mChildOfContent.getWindowVisibleDisplayFrame(r);
        return (r.bottom - r.top);
    }

    public void destroy() {
        mChildOfContent.getViewTreeObserver().removeOnGlobalLayoutListener(mOnGlobalLayoutListener);
    }
}