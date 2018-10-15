package com.common.utils;

import android.app.Activity;
import android.graphics.Rect;
import android.os.Build;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.FrameLayout;


import com.common.base.BaseActivity;

import org.greenrobot.eventbus.EventBus;

import java.lang.ref.WeakReference;


/**
 * Created by chenyong1 on 20/7/15.
 */
public class AndroidBug5497WorkaroundSupportingTranslucentStatus {

    private static final String TAG = AndroidBug5497WorkaroundSupportingTranslucentStatus.class.getSimpleName();

    private static final int MIN_HEIGHT = U.getDisplayUtils().dip2px(0.3f);//最低不能小于1像素

    boolean hasDestroy = false;

    public static AndroidBug5497WorkaroundSupportingTranslucentStatus assistActivity(BaseActivity activity) {
        return new AndroidBug5497WorkaroundSupportingTranslucentStatus(activity);
    }

    private View mChildOfContent;
    private int usableHeightPrevious;
    private FrameLayout.LayoutParams frameLayoutParams;

    private boolean isTranslucentEnabled = false;
    private boolean isShowVirtualKeyBoard = false;

    private int virtualKeyBoardValue = 0;

    private boolean isFirstLayout = true;

    private int delta = 0;      //作为暂时解决沉浸式方案改变之后  状态栏高度的修正值

    private WeakReference<BaseActivity> baseActivityWeakReference;

    private ViewTreeObserver.OnGlobalLayoutListener mOnGlobalLayoutListener;

    private AndroidBug5497WorkaroundSupportingTranslucentStatus(final BaseActivity activity) {
        baseActivityWeakReference = new WeakReference<BaseActivity>(activity);
        FrameLayout content = (FrameLayout) activity.findViewById(android.R.id.content);
        final WindowManager.LayoutParams attrs = activity.getWindow()
                .getAttributes();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            isTranslucentEnabled = (attrs.flags & WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS) != 0;
        }
        mChildOfContent = content.getChildAt(0);

        mOnGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                if (baseActivityWeakReference != null && baseActivityWeakReference.get() != null && !hasDestroy) {
                    possiblyResizeChildOfContent(baseActivityWeakReference.get().isKeyboardResize(), baseActivityWeakReference.get());
                }
            }
        };

        mChildOfContent.getViewTreeObserver().addOnGlobalLayoutListener(mOnGlobalLayoutListener);
        frameLayoutParams = (FrameLayout.LayoutParams) mChildOfContent.getLayoutParams();
    }


    private void possiblyResizeChildOfContent(boolean isResize, BaseActivity activity) {
        int usableHeightNow = computeUsableHeight();
//        MyLog.d(TAG, "possiblyResizeChildOfContent isResize=" + isResize + " activity=" + activity);
        if (usableHeightNow != usableHeightPrevious) {
            int usableHeightSansKeyboard = mChildOfContent.getRootView().getHeight();
//            activity.setActivityHeight(usableHeightSansKeyboard);

//            MyLog.v(TAG, "keyBoardEvent usableHeightNow:" + usableHeightNow + " usableHeightPrevious:" + usableHeightPrevious);
//            MyLog.v(TAG, "keyBoardEvent usableHeightSansKeyboard:" + usableHeightSansKeyboard);
//            MyLog.v(TAG, "keyBoardEvent getActureHeight:" + getActureHeight(activity));
//            MyLog.v(TAG, "keyBoardEvent getScreenHeight:" + DisplayUtils.getScreenHeight());

            int actureHeight = getActureHeight(activity);

//            MyLog.v(TAG, "keyBoardEvent actureHeight:" + actureHeight);
            if (usableHeightSansKeyboard > U.getDisplayUtils().getScreenHeight()) {
                isShowVirtualKeyBoard = true;
                virtualKeyBoardValue = U.getDisplayUtils().getScreenHeight();
            } else if (usableHeightSansKeyboard > actureHeight) {
                isShowVirtualKeyBoard = true;
                virtualKeyBoardValue = actureHeight;
            }

            if (usableHeightNow >= usableHeightSansKeyboard) {
                isShowVirtualKeyBoard = false;
            }
            if (isShowVirtualKeyBoard) {
                usableHeightSansKeyboard = virtualKeyBoardValue;
            }

            int heightDifference = usableHeightSansKeyboard - usableHeightNow;
            if (heightDifference < 150) {               //状态栏高度一般不高  做模糊比较修正
                delta = heightDifference;
            }

//            MyLog.v(TAG, "keyboardEvent heightDifference " + heightDifference
//                    + " height " + usableHeightNow
//                    + " SansKeyboard " + usableHeightSansKeyboard);

            heightDifference = heightDifference - delta;
            if (heightDifference > MIN_HEIGHT) {
                // keyboard probably just became visible
                KeyboardEvent event = new KeyboardEvent(KeyboardEvent.EVENT_TYPE_KEYBOARD_VISIBLE_ALWAYS_SEND, heightDifference);
                event.from = baseActivityWeakReference.get().getClass().getName();
                EventBus.getDefault().post(event);
                if (!isResize) {
                    KeyboardEvent event2 = new KeyboardEvent(KeyboardEvent.EVENT_TYPE_KEYBOARD_VISIBLE, heightDifference);
                    event2.from = baseActivityWeakReference.get().getClass().getName();
                    EventBus.getDefault().post(event2);
                } else if (isTranslucentEnabled) {
                    frameLayoutParams.height = usableHeightSansKeyboard - heightDifference;
                }
            } else {
                // keyboard probably just became hidden
                if (!isResize) {
                    if (usableHeightPrevious > usableHeightNow && isShowVirtualKeyBoard && usableHeightNow == usableHeightSansKeyboard) {
                        //加该条件fixD5上虚拟按键bug LIVEAND-11510
                    } else {
                        if (!isFirstLayout) {
                            KeyboardEvent event = new KeyboardEvent(KeyboardEvent.EVENT_TYPE_KEYBOARD_HIDDEN);
                            event.from = baseActivityWeakReference.get().getClass().getName();
                            EventBus.getDefault().post(event);
                        } else {
                            isFirstLayout = false;
                        }
                    }
                } else if (isTranslucentEnabled) {
                    frameLayoutParams.height = usableHeightSansKeyboard - heightDifference;
                }
            }
            if (isResize && isTranslucentEnabled) {
                mChildOfContent.requestLayout();
            }
            usableHeightPrevious = usableHeightNow;
        }
    }

    private int computeUsableHeight() {
        Rect r = new Rect();
        mChildOfContent.getWindowVisibleDisplayFrame(r);
        if (isTranslucentEnabled) {
            r.top = 0;
        }
        return (r.bottom - r.top);
    }


    private int getActureHeight(Activity activity) {
        return activity.getWindowManager().getDefaultDisplay().getHeight();
    }


    public void destroy() {
        hasDestroy = true;
        if (baseActivityWeakReference != null && baseActivityWeakReference.get() != null && mOnGlobalLayoutListener != null) {
            FrameLayout content = (FrameLayout) baseActivityWeakReference.get().findViewById(android.R.id.content);
            mChildOfContent = content.getChildAt(0);
            mChildOfContent.getViewTreeObserver().removeOnGlobalLayoutListener(mOnGlobalLayoutListener);
        }
    }

}