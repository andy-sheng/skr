package com.base.utils;

import android.app.Activity;
import android.graphics.Rect;
import android.os.Build;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.base.activity.BaseActivity;
import com.base.event.KeyboardEvent;
import com.base.utils.display.DisplayUtils;
import com.base.log.MyLog;

import org.greenrobot.eventbus.EventBus;


/**
 * Created by chenyong1 on 20/7/15.
 */
public class AndroidBug5497WorkaroundSupportingTranslucentStatus {

    private static final String TAG = AndroidBug5497WorkaroundSupportingTranslucentStatus.class.getSimpleName();

    // For more information, see https://code.google.com/p/android/issues/detail?id=5497
    // To use this class, simply invoke assistActivity() on an Activity that already has its content view set.

    public static void assistActivity(BaseActivity activity) {
        new AndroidBug5497WorkaroundSupportingTranslucentStatus(activity);
    }

    private View mChildOfContent;
    private int usableHeightPrevious;
    private FrameLayout.LayoutParams frameLayoutParams;

    private boolean isTranslucentEnabled = false;
    private boolean isShowVirtureKeyBorad = false;

    private int virtureKeyBoradVaule = 0;

    private AndroidBug5497WorkaroundSupportingTranslucentStatus(final BaseActivity activity) {
        FrameLayout content = (FrameLayout) activity.findViewById(android.R.id.content);
        final WindowManager.LayoutParams attrs = activity.getWindow()
                .getAttributes();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            isTranslucentEnabled = (attrs.flags & WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS) != 0;
        }
        mChildOfContent = content.getChildAt(0);
        mChildOfContent.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                possiblyResizeChildOfContent(activity.isKeyboardResize(),activity);
            }
        });
        frameLayoutParams = (FrameLayout.LayoutParams) mChildOfContent.getLayoutParams();
    }


    private void possiblyResizeChildOfContent(boolean isResize, BaseActivity activity) {
        int usableHeightNow = computeUsableHeight();
        if (usableHeightNow != usableHeightPrevious) {

            MyLog.v(TAG, "KeyBoardEvent usableHeightNow:"+usableHeightNow);

            int usableHeightSansKeyboard = mChildOfContent.getRootView().getHeight();

            activity.setActivityHeight(usableHeightSansKeyboard);

            MyLog.v(TAG, "KeyBoardEvent usableHeightSansKeyboard:"+usableHeightSansKeyboard);

            MyLog.v(TAG, "KeyBoardEvent getActureHeight:"+ getActureHeight(activity));

            MyLog.v(TAG, "KeyBoardEvent GlobalData.screenHeight :"+ DisplayUtils.getScreenHeight());

            MyLog.v(TAG, "KeyBoardEvent getScreenHeight:"+ DisplayUtils.getScreenHeight());

            if (usableHeightNow == DisplayUtils.getScreenHeight() && usableHeightSansKeyboard >  DisplayUtils.getScreenHeight()) {
                isShowVirtureKeyBorad = true;
                virtureKeyBoradVaule = DisplayUtils.getScreenHeight();
            }else if (usableHeightNow == DisplayUtils.getScreenHeight() && usableHeightSansKeyboard >  DisplayUtils.getScreenHeight()) {
                isShowVirtureKeyBorad = true;
                virtureKeyBoradVaule = DisplayUtils.getScreenHeight();
            }else if (usableHeightNow == getActureHeight(activity) && usableHeightSansKeyboard >  getActureHeight(activity)) {
                isShowVirtureKeyBorad = true;
                virtureKeyBoradVaule =  getActureHeight(activity);
            }

            if (usableHeightNow == usableHeightSansKeyboard) {
                isShowVirtureKeyBorad = false;
            }

            if(isShowVirtureKeyBorad){
                usableHeightSansKeyboard = virtureKeyBoradVaule;
            }

            MyLog.v(TAG, "KeyBoardEvent usableHeightSansKeyboard:"+usableHeightSansKeyboard);

            int heightDifference = usableHeightSansKeyboard - usableHeightNow;

            MyLog.v(TAG, "keyboardEvent usableHeightNow"+ heightDifference + " height "+usableHeightNow +" SansKeyboard "+usableHeightSansKeyboard );

            if (heightDifference > (usableHeightSansKeyboard / 5)) {
                // keyboard probably just became visible
                EventBus.getDefault().post(new KeyboardEvent(KeyboardEvent.EVENT_TYPE_KEYBOARD_VISIBLE_ALWAYS_SEND, heightDifference));
                if (!isResize) {
                    EventBus.getDefault().post(new KeyboardEvent(KeyboardEvent.EVENT_TYPE_KEYBOARD_VISIBLE, heightDifference));
                } else if (isTranslucentEnabled) {
                    frameLayoutParams.height = usableHeightSansKeyboard - heightDifference;
                }
            } else {
                // keyboard probably just became hidden
                if (!isResize) {
                    EventBus.getDefault().post(new KeyboardEvent(KeyboardEvent.EVENT_TYPE_KEYBOARD_HIDDEN));
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


    private int getActureHeight(Activity activity){
        return activity.getWindowManager().getDefaultDisplay().getHeight();
    }

}