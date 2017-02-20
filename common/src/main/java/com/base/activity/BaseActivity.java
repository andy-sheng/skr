package com.base.activity;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.os.SystemClock;
import android.view.ActionMode;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.base.activity.assist.IBindActivityLIfeCycle;
import com.base.common.BuildConfig;
import com.base.dialog.MyProgressDialog;
import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.presenter.Presenter;
import com.base.utils.AndroidBug5497WorkaroundSupportingTranslucentStatus;
import com.base.utils.Constants;
import com.base.utils.display.DisplayUtils;
import com.base.utils.language.LocaleUtil;
import com.base.view.IStatusBarOperator;

import org.greenrobot.eventbus.EventBus;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Locale;


/**
 * Created by chengsimin on 16/9/6.
 */
public abstract class BaseActivity extends RxActivity implements IStatusBarOperator {
    protected final String TAG = getTAG();

    protected static int actCnt = 0;

    public static long mLastResumeTime = SystemClock.elapsedRealtime();

    /*用来控制是否采用沉浸式*/
    private static final boolean IS_PROFILE_MODE = true;

    /*记录是不是MIUI*/
    protected static boolean sIsMIUIV6 = true;
    /*记录系统状态栏的高度*/
    private static int sStatusBarHeight;

    protected MyProgressDialog mDialog;

    protected boolean mIsForeground;

    protected static int sActivityHeight = DisplayUtils.getScreenHeight();

    public static boolean isMIUIV6() {
        return sIsMIUIV6;
    }

    /**
     * 获取对应的类名
     */
    protected String getTAG() {
        return getClass().getSimpleName();
    }

    // 想加入activity生命周期管理的presenter放在这里
    private HashSet<Presenter> mPresenterSet = new HashSet<>();

    private HashSet<IBindActivityLIfeCycle> mBindActivityLifeCycleSet = new HashSet<>();

    protected void addPresent(Presenter presenter) {
        if (presenter != null) {
            mPresenterSet.add(presenter);
        }
    }

    protected void addBindActivityLifeCycle(IBindActivityLIfeCycle bindActivityLIfeCycle, boolean onActivityCreated) {
        if (bindActivityLIfeCycle != null) {
            if (onActivityCreated) {
                bindActivityLIfeCycle.onActivityCreate();
            }
            mBindActivityLifeCycleSet.add(bindActivityLIfeCycle);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Constants.isDebugBuild || BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectAll()
//                    .detectDiskWrites()
//                    .detectNetwork()   // or .detectAll() for all detectable problems
                    .penaltyLog()
                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build());
        }
        MyLog.w(TAG, "onCreate");
        EventBus.getDefault().register(this);
        if (LocaleUtil.isNeedSetLocale()) {
            Locale locale = LocaleUtil.getLocale();
            LocaleUtil.setLocale(locale);
        }
        super.onCreate(savedInstanceState);
        addSelfToStatusList();
    }

    @Override
    protected void onStart() {
        super.onStart();
        for (Presenter presenter : mPresenterSet) {
            presenter.start();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mIsForeground = true;
        for (Presenter presenter : mPresenterSet) {
            presenter.resume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mIsForeground = false;
        for (Presenter presenter : mPresenterSet) {
            presenter.pause();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        for (Presenter presenter : mPresenterSet) {
            presenter.stop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }

        removeSelfFromStatusList();
        for (Presenter presenter : mPresenterSet) {
            presenter.destroy();
        }
        mPresenterSet.clear();
        for (IBindActivityLIfeCycle bindActivityLIfeCycle : mBindActivityLifeCycleSet) {
            bindActivityLIfeCycle.onActivityDestroy();
        }
    }

    @Override
    public void onActionModeFinished(ActionMode mode) {
        super.onActionModeFinished(mode);
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        if (isProfileMode()) {
            setProfileMode();
        } else {
            AndroidBug5497WorkaroundSupportingTranslucentStatus.assistActivity(this);
        }
    }

    public boolean isKeyboardResize() {
        return true;
    }

    /**
     * 设置名片沉浸式样式
     */
    protected void setProfileMode() {
        setTranslucentStatus(this, true);
        AndroidBug5497WorkaroundSupportingTranslucentStatus.assistActivity(this);
        adjustStatusBar();
    }

    @Override
    public final void addSelfToStatusList() {
        if (isOverrideStatusBar()) {
            MyLog.d(TAG, "addSelfToStatusList");
            sStatusBarOperators.add(this);
        }
    }

    @Override
    public final void removeSelfFromStatusList() {
        if (isOverrideStatusBar()) {
            MyLog.d(TAG, "removeSelfFromStatusList");
            sStatusBarOperators.remove(this);
            if (!sStatusBarOperators.isEmpty()) {
                IStatusBarOperator statusBarOperator = sStatusBarOperators.getLast();
                if (statusBarOperator != null) {
                    statusBarOperator.restoreStatusBar(this.isStatusBarDark(), true);
                }
            }
        }
    }

    /**
     * 更新通知栏，实现沉浸式
     */
    @Override
    public final void adjustStatusBar() {
        if (isOverrideStatusBar()) {
            MyLog.d(TAG, "adjustStatusBar isDark=" + isStatusBarDark());
            setStatusColor(this, isStatusBarDark());
        }
    }

    @Override
    public final void restoreStatusBar(boolean isPrevDark, boolean isFromActivity) {
        if (isFromActivity || isStatusBarDark() != isPrevDark) {
            MyLog.d(TAG, "restoreStatusBar prev=" + isPrevDark + ", fromActivity=" + isFromActivity);
            adjustStatusBar();
        }
    }

    @Override
    public boolean isOverrideStatusBar() {
        return true;
    }

    @Override
    public boolean isStatusBarDark() {
        return true;
    }

    /**
     * 设置状态栏顶部字的颜色
     */
    public static boolean setStatusColor(final Activity act, boolean isDark) {
        MyLog.d("BaseActivity", "setStatusColor statusBar isDark=" + isDark);
        Class<? extends Window> clazz = act.getWindow().getClass();
        try {
            int darkModeFlag = 0;
            Class<?> layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
            Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
            darkModeFlag = field.getInt(layoutParams);
            Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);
            extraFlagField.invoke(act.getWindow(), isDark ? darkModeFlag : 0, darkModeFlag);
            sIsMIUIV6 = true;
            return true;
        } catch (Exception e) {
            // ignore
        }
        sIsMIUIV6 = false;
        return setStatusColorForMeizu(act, isDark);
    }

    private static boolean setStatusColorForMeizu(Activity act, boolean isDark) {
        MyLog.d("BaseActivity", "setStatusColorForMeizu statusBar isDark=" + isDark);
        try {
            WindowManager.LayoutParams lp = act.getWindow().getAttributes();
            Field darkFlag = WindowManager.LayoutParams.class
                    .getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON");
            Field meizuFlags = WindowManager.LayoutParams.class
                    .getDeclaredField("meizuFlags");
            darkFlag.setAccessible(true);
            meizuFlags.setAccessible(true);
            int bit = darkFlag.getInt(null);
            int value = meizuFlags.getInt(lp);
            if (isDark) {
                value |= bit;
            } else {
                value &= ~bit;
            }
            meizuFlags.setInt(lp, value);
            act.getWindow().setAttributes(lp);
            return true;
        } catch (Exception e) {
        }
        return false;
    }

    /**
     * 用于获取状态栏的高度。
     */
    public static int getStatusBarHeight() {
        if (sStatusBarHeight == 0) {
            try {
                Class<?> c = Class.forName("com.android.internal.R$dimen");
                Object o = c.newInstance();
                Field field = c.getField("status_bar_height");
                int x = (Integer) field.get(o);

                sStatusBarHeight = GlobalData.app().getResources().getDimensionPixelSize(x);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return sStatusBarHeight;
    }

    /**
     * 设置全屏
     */
    public static void setFullScreen(Activity activity, boolean on) {
        Window win = activity.getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        final int bits = WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }

    /**
     * 设置状态栏透明
     */
    public static void setTranslucentStatus(Activity activity, boolean on) {
        Window win = activity.getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }

    /**
     * 可以设置沉浸式的条件，手动配置，同时满足版本要求
     */
    public static boolean isProfileMode() {
        return IS_PROFILE_MODE && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }


    /**
     * 显示加载进度
     *
     * @param resId
     */
    public void showProgress(final int resId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!isFinishing()) {
                    mDialog = MyProgressDialog.show(BaseActivity.this, null, getResources().getString(resId), true, true);
                    MyLog.d(TAG, "" + mDialog.hashCode());
                }
            }
        });
    }

    public void hideProgress() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!isFinishing()) {
                    if (null != mDialog && mDialog.isShowing()) {
                        MyLog.d(TAG, "" + mDialog.hashCode());
                        mDialog.dismiss();
                    }
                }
            }
        });
    }

    // 不想使用ButterKnife，可以使用下面方法简化代码
    public <V extends View> V $(int id) {
        return (V) findViewById(id);
    }

    public static int getActCnt() {
        return actCnt;
    }

    /**
     * 获取真实的activity高度，因为在虚拟机键盘情况下通过DisplayUtil获取的不准
     */
    public int getActivityHeight() {
        return sActivityHeight;
    }

    public void setActivityHeight(int mActivityHeight) {
        sActivityHeight = mActivityHeight;
    }
}
