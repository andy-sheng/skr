package com.common.floatwindow;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;

import com.common.log.MyLog;
import com.common.utils.U;

/**
 * Created by yhao on 17-11-14.
 * https://github.com/yhaolpz
 */

class FloatPhone extends FloatView {
    public final static String TAG = "FloatPhone";
    private FloatWindow.B mB;
    private WindowManager mWindowManager;
    private final WindowManager.LayoutParams mLayoutParams;
    private boolean isRemove = true;

    private int MSG_CHECK = 1;
    Handler mUiHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what == MSG_CHECK){
                if(isShow()){
                    MyLog.d(TAG,"handleMessage 浮窗可见");
                }else{
                    MyLog.d(TAG,"handleMessage 浮窗不可见");
                    attachView(true);
                }
            }
        }
    };

    FloatPhone(FloatWindow.B b) {
        mB = b;

        mWindowManager = (WindowManager) mB.mApplicationContext.getSystemService(Context.WINDOW_SERVICE);
        mLayoutParams = new WindowManager.LayoutParams();
        mLayoutParams.format = PixelFormat.RGBA_8888;
        mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
        mLayoutParams.windowAnimations = 0;

        mLayoutParams.gravity = mB.gravity;
        mLayoutParams.x = mB.xOffset;
        mLayoutParams.y = mB.yOffset;
        mLayoutParams.width = mB.mWidth;
        mLayoutParams.height = mB.mHeight;
    }

    @Override
    public void init() {
        initInner(mB.reqPermissionIfNeed);
    }

    void initInner(boolean reqPermissionIfNeed) {
        boolean hasFloatWindowPermission = U.getPermissionUtils().checkFloatWindow(mB.mApplicationContext);
        MyLog.d(TAG, "init hasFloatWindowPermission=" + hasFloatWindowPermission + " reqPermissionIfNeed=" + reqPermissionIfNeed);
        if (hasFloatWindowPermission) {
            /**
             * 4.4以前不用判断权限可以直接使用 4.4 以前 hasFloatWindowPermission为true
             * 在4.4到6.0之前，google没有提供方法让我们用于判断悬浮窗权限，同时也没有跳转到设置界面进行开启的方法，
             * 因为此权限是默认开启的，但是有一些产商会修改它，所以在使用之前最好进行判断，以免使用时出现崩溃，
             * 判断方法是用反射的方式获取出是否开启了悬浮窗权限。
             *
             * 在6.0以及以后的版本中，google为我们提供了判断方法和跳转界面的方法，
             * 直接使用Settings.canDrawOverlays(context) 就可以判断是否开启了悬浮窗权限，
             * 没有开启可以跳转到设置界面让用户开启
             */
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else {
                mLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
            }
            addView();
        } else {
            if (reqPermissionIfNeed) {
                //TODO 这里不好加悬浮窗权限是否获得成功的回调
                U.getPermissionUtils().requestFloatWindow(U.getActivityUtils().getTopActivity());
            } else {

                /**
                 * mLayoutParams.type android 窗口类型
                 *
                 *         有3种主要类型：
                 *
                 *         Applicationwindows：
                 *         取值在 FIRST_APPLICATION_WINDOW 和 LAST_APPLICATION_WINDOW 之间。  1-99
                 *         是通常的、顶层的应用程序窗口。必须将 token 设置成 activity 的 token 。
                 *
                 *        Sub_windows：
                 *         取值在 FIRST_SUB_WINDOW 和 LAST_SUB_WINDOW 之间。  1000-1999
                 *         与顶层窗口相关联，token 必须设置为它所附着的宿主窗口的 token。
                 *
                 *         Systemwindows：
                 *         取值在 FIRST_SYSTEM_WINDOW 和 LAST_SYSTEM_WINDOW 之间。 2000 - 2999
                 *         用于特定的系统功能。它不能用于应用程序，使用时需要特殊权限。
                 *
                 * TYPE_STATUS_BAR=2000 状态栏
                 * TYPE_PHONE=2002 它用来提供与用户交互的界面(特别是接电话的界面)
                 * TYPE_SYSTEM_ALERT = 2003 低电量警告
                 * TYPE_TOAST = 2005 短暂的通知
                 * TYPE_SYSTEM_OVERLAY = 2006 系统用来覆盖屏幕用的
                 *
                 * 总得来说有个原则,type值越大则显示的越靠上层,
                 * 上面的这些type常量都是系统中各种UI默认的使用的值
                 *
                 * 如果要达到你想要达到的效果甚至可以自己设置想要的int值
                 *
                 * 比如想要覆盖在状态栏之上,就设置个大于2001且小于2999的值就行
                 *
                 * 有一点要注意,api>=23之后type要是>=2000则需要一些权限才能使用
                 *
                 * 而且api>=23之后,要正确设置token值才能使用,要注意哦
                 */

                attachView(false);
                mUiHandler.sendEmptyMessageDelayed(MSG_CHECK,200);
            }
        }
    }

    void attachView(boolean attachActivity){
        MyLog.d(TAG,"attachView" + " attachActivity=" + attachActivity);
        if(attachActivity) {
            Activity topActivityOrApp = U.getActivityUtils().getTopActivity();
            if (topActivityOrApp != null) {
                if (!topActivityOrApp.isFinishing() && !topActivityOrApp.isDestroyed()) {
                    mWindowManager = topActivityOrApp.getWindowManager();
                }
            }
            mLayoutParams.type = WindowManager.LayoutParams.LAST_APPLICATION_WINDOW;
        }else{
            mLayoutParams.type = WindowManager.LayoutParams.FIRST_SYSTEM_WINDOW + 37;
        }
//        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N_MR1) {
//            mLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
//        } else {
//            //mWM = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
//
//        }
        final Configuration config = U.app().getResources().getConfiguration();
        final int gravity = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1
                ? Gravity.getAbsoluteGravity(mB.gravity, config.getLayoutDirection())
                : mB.gravity;

        mLayoutParams.y = mB.yOffset;
        mLayoutParams.height = mB.mHeight;
        mLayoutParams.width = mB.mWidth;
        mLayoutParams.format = PixelFormat.TRANSLUCENT;
        mLayoutParams.windowAnimations = android.R.style.Animation_Toast;

        mLayoutParams.setTitle("ToastWithoutNotification");
//                mLayoutParams.flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
//                        | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
//                        | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        mLayoutParams.gravity = gravity;
        if ((gravity & Gravity.HORIZONTAL_GRAVITY_MASK) == Gravity.FILL_HORIZONTAL) {
            mLayoutParams.horizontalWeight = 1.0f;
        }
        if ((gravity & Gravity.VERTICAL_GRAVITY_MASK) == Gravity.FILL_VERTICAL) {
            mLayoutParams.verticalWeight = 1.0f;
        }
        mLayoutParams.x = mB.xOffset;
        mLayoutParams.packageName = U.app().getPackageName();

        addView();
    }

    public void addView() {
        try {
            if (isRemove) {
                MyLog.d(TAG, "addView type=" + mLayoutParams.type);
                mWindowManager.addView(mB.mView, mLayoutParams);
                isRemove = false;
            }
        } catch (Exception e) {
            MyLog.e(TAG, e);
        }
    }

    public boolean isShow(){
        if(mB.mView!=null && mB.mView.isShown()){
            return true;
        }
        return false;
    }

    @Override
    public void dismiss() {
        MyLog.d(TAG, "dismiss isRemove=" + isRemove);
        mUiHandler.removeCallbacksAndMessages(null);
        try {
            if (!isRemove) {
                isRemove = true;
                mWindowManager.removeView(mB.mView);
            }
        } catch (Exception e) {
            MyLog.e(TAG, e);
        }

    }

    @Override
    public void updateXY(int x, int y) {
        MyLog.d(TAG, "updateXY" + " x=" + x + " y=" + y);
        if (isRemove) return;
        mB.xOffset = x;
        mB.yOffset = y;
        mLayoutParams.x = x;
        mLayoutParams.y = y;
        mWindowManager.updateViewLayout(mB.mView, mLayoutParams);
    }

    @Override
    void updateX(int x) {
        if (isRemove) return;
        mB.xOffset = x;
        mLayoutParams.x = x;
        mWindowManager.updateViewLayout(mB.mView, mLayoutParams);
    }

    @Override
    void updateY(int y) {
        if (isRemove) return;
        mB.yOffset = y;
        mLayoutParams.y = y;
        mWindowManager.updateViewLayout(mB.mView, mLayoutParams);
    }

    @Override
    int getX() {
        return mB.xOffset;
    }

    @Override
    int getY() {
        return mB.yOffset;
    }


}
