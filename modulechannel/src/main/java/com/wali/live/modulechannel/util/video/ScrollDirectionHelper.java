package com.wali.live.modulechannel.util.video;


public class ScrollDirectionHelper {

    private static final String TAG = ScrollDirectionHelper.class.getSimpleName();

    private final OnDetectScrollListener mOnDetectScrollListener;

    private int mOldTop;
    private int mOldFirstVisibleItem;

    private ScrollDirection mOldScrollDirection = null;

    public ScrollDirectionHelper(OnDetectScrollListener onDetectScrollListener) {
        mOnDetectScrollListener = onDetectScrollListener;
    }

    public interface OnDetectScrollListener {
        void onScrollDirectionChanged(ScrollDirection scrollDirection);
    }

    public enum ScrollDirection {
        UP, DOWN
    }

    public void onDetectedListScroll(int top, int firstVisibleItem) {
       com.common.log.MyLog.d(TAG, ">> onDetectedListScroll, firstVisibleItem " + firstVisibleItem + ", mOldFirstVisibleItem " + mOldFirstVisibleItem + " top: "+ top +  ", mOldTop " + mOldTop);

        if (firstVisibleItem == mOldFirstVisibleItem) {
            if (top > mOldTop) {
                onScrollUp();
            } else if (top < mOldTop) {
                onScrollDown();
            }
        } else {
            if (firstVisibleItem < mOldFirstVisibleItem) {
                onScrollUp();
            } else {
                onScrollDown();
            }
        }

        mOldTop = top;
        mOldFirstVisibleItem = firstVisibleItem;
    }

    private void onScrollDown() {
       com.common.log.MyLog.d(TAG, "onScroll Down");

        if (mOldScrollDirection != ScrollDirection.DOWN) {
            mOldScrollDirection = ScrollDirection.DOWN;
            mOnDetectScrollListener.onScrollDirectionChanged(ScrollDirection.DOWN);
        } else {
           com.common.log.MyLog.d(TAG, "onDetectedListScroll, scroll state not changed " + mOldScrollDirection);
        }
    }

    private void onScrollUp() {
       com.common.log.MyLog.d(TAG, "onScroll Up");

        if (mOldScrollDirection != ScrollDirection.UP) {
            mOldScrollDirection = ScrollDirection.UP;
            mOnDetectScrollListener.onScrollDirectionChanged(ScrollDirection.UP);
        } else {
           com.common.log.MyLog.d(TAG, "onDetectedListScroll, scroll state not changed " + mOldScrollDirection);
        }
    }
}
