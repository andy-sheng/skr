package com.common.floatwindow;

import android.animation.TimeInterpolator;
import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.common.log.MyLog;
import com.common.utils.U;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by yhao on 2017/12/22.
 * <p>
 * 问题可查看
 * https://github.com/yhaolpz/FloatWindow
 */

public class FloatWindow {

    public final static String TAG = "FloatWindow";

    private FloatWindow() {

    }

    private static final String mDefaultTag = "default_float_window_tag";
    private static Map<String, IFloatWindow> mFloatWindowMap;

    public static IFloatWindow get() {
        return get(mDefaultTag);
    }

    public static IFloatWindow get(@NonNull String tag) {
        return mFloatWindowMap == null ? null : mFloatWindowMap.get(tag);
    }

    private static B mBuilder = null;

    @MainThread
    public static B with(@NonNull Context applicationContext) {
        return mBuilder = new B(applicationContext);
    }

    public static boolean hasFollowWindowShow() {
        if(mFloatWindowMap!=null){
            return mFloatWindowMap.size()>0;
        }
        return false;
    }

    public static boolean destroy() {
        return  destroy(mDefaultTag);
    }

    public static boolean destroy(String tag) {
        if (mFloatWindowMap == null || !mFloatWindowMap.containsKey(tag)) {
            return false;
        }
        mFloatWindowMap.get(tag).dismiss();
        mFloatWindowMap.remove(tag);
        return true;
    }

    public static class B {
        Context mApplicationContext;
        View mView;
        private int mLayoutId;
        int mWidth = ViewGroup.LayoutParams.WRAP_CONTENT;
        int mHeight = ViewGroup.LayoutParams.WRAP_CONTENT;
        int gravity = Gravity.TOP | Gravity.START;
        int xOffset;
        int yOffset;
        boolean mShow = true;
        Class[] mActivities;
        int mMoveType = MoveType.slide;
        int mSlideLeftMargin;
        int mSlideRightMargin;
        long mDuration = 300;
        TimeInterpolator mInterpolator;
        String mTag = mDefaultTag;
        boolean mDesktopShow;
        ViewStateListener mViewStateListener;
        boolean cancelIfExist = false;// true 取消，如果该tag的window以及存在了。false会销毁之前的再新建
        boolean reqPermissionIfNeed = true;// true 如果没有权限则会申请权限，false 没有权限不会申请就用别的方式兼容

        private B() {

        }

        B(Context applicationContext) {
            mApplicationContext = applicationContext;
        }

        public B setView(@NonNull View view) {
            mView = view;
            return this;
        }

        public B setView(@LayoutRes int layoutId) {
            mLayoutId = layoutId;
            return this;
        }

        public B setWidth(int width) {
            mWidth = width;
            return this;
        }

        public B setHeight(int height) {
            mHeight = height;
            return this;
        }

        public B setWidth(@Screen.screenType int screenType, float ratio) {
            mWidth = (int) ((screenType == Screen.width ?
                    U.getDisplayUtils().getScreenWidth() :
                    U.getDisplayUtils().getScreenHeight()) * ratio);
            return this;
        }


        public B setHeight(@Screen.screenType int screenType, float ratio) {
            mHeight = (int) ((screenType == Screen.width ?
                    U.getDisplayUtils().getScreenWidth() :
                    U.getDisplayUtils().getScreenHeight()) * ratio);
            return this;
        }


        public B setX(int x) {
            xOffset = x;
            return this;
        }

        public B setY(int y) {
            yOffset = y;
            return this;
        }

        public B setX(@Screen.screenType int screenType, float ratio) {
            xOffset = (int) ((screenType == Screen.width ?
                    U.getDisplayUtils().getScreenWidth() :
                    U.getDisplayUtils().getScreenHeight()) * ratio);
            return this;
        }

        public B setY(@Screen.screenType int screenType, float ratio) {
            yOffset = (int) ((screenType == Screen.width ?
                    U.getDisplayUtils().getScreenWidth() :
                    U.getDisplayUtils().getScreenHeight()) * ratio);
            return this;
        }


        /**
         * 设置 Activity 过滤器，用于指定在哪些界面显示悬浮窗，默认全部界面都显示
         *
         * @param show       　过滤类型,子类类型也会生效
         * @param activities 　过滤界面
         */
        public B setFilter(boolean show, @NonNull Class... activities) {
            mShow = show;
            mActivities = activities;
            return this;
        }

        public B setMoveType(@MoveType.MOVE_TYPE int moveType) {
            return setMoveType(moveType, 0, 0);
        }


        /**
         * 设置带边距的贴边动画，只有 moveType 为 MoveType.slide，设置边距才有意义，这个方法不标准，后面调整
         *
         * @param moveType         贴边动画 MoveType.slide
         * @param slideLeftMargin  贴边动画左边距，默认为 0
         * @param slideRightMargin 贴边动画右边距，默认为 0
         */
        public B setMoveType(@MoveType.MOVE_TYPE int moveType, int slideLeftMargin, int slideRightMargin) {
            mMoveType = moveType;
            mSlideLeftMargin = slideLeftMargin;
            mSlideRightMargin = slideRightMargin;
            return this;
        }

        public B setMoveStyle(long duration, @Nullable TimeInterpolator interpolator) {
            mDuration = duration;
            mInterpolator = interpolator;
            return this;
        }

        public B setTag(@NonNull String tag) {
            mTag = tag;
            return this;
        }

        public B setDesktopShow(boolean show) {
            mDesktopShow = show;
            return this;
        }

        public B setViewStateListener(ViewStateListener listener) {
            mViewStateListener = listener;
            return this;
        }

        public B setCancelIfExist(boolean cancelIfExist) {
            this.cancelIfExist = cancelIfExist;
            return this;
        }

        public B setReqPermissionIfNeed(boolean reqPermissionIfNeed) {
            this.reqPermissionIfNeed = reqPermissionIfNeed;
            return this;
        }

        public void build() {
            if (mFloatWindowMap == null) {
                mFloatWindowMap = new HashMap<>();
            }
            if (mFloatWindowMap.containsKey(mTag)) {
                if (cancelIfExist) {
                    MyLog.w(TAG, "FloatWindow of this tag has been added,cancel");
                    return;
                } else {
                    MyLog.w(TAG, "FloatWindow of this tag has been added,remove first");
                    destroy(mTag);
                }
            }
            if (mView == null && mLayoutId == 0) {
                MyLog.w(TAG, "View has not been set!");
                return;
            }
            if (mView == null) {
                LayoutInflater inflate = (LayoutInflater) mApplicationContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                mView = inflate.inflate(mLayoutId, null);
            }
            IFloatWindow floatWindowImpl = new IFloatWindowImpl(this);
            floatWindowImpl.show("build");
            mFloatWindowMap.put(mTag, floatWindowImpl);
        }

    }
}
