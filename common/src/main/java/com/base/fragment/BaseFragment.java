package com.base.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.base.activity.BaseActivity;
import com.base.fragment.utils.FragmentNaviUtils;
import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.utils.Constants;
import com.base.view.IStatusBarOperator;
import com.squareup.leakcanary.RefWatcher;

import java.lang.reflect.Field;

import butterknife.ButterKnife;

/**
 * Created by lan on 15-11-4.
 *
 * @module 基础模块
 */
public abstract class BaseFragment extends Fragment implements FragmentListener, FragmentPageListener, IStatusBarOperator {
    protected final String TAG = getTAG();

    protected static final String PARAM_FROM_TYPE = "type";
    public static final String PARAM_FORCE_PORTRAIT = "forcePortrait";
    public static final String PAPAM_FORCE_LANDSCAPE = "forceLandscape";
    public static final String PARAM_FOLLOW_SYS = "follow_sys";

    /**
     * 总榜点击进入
     */
    public static final String PARAM_FROM_TOTAL = "total";
    /**
     * 本场榜点击进入
     */
    public static final String PARAM_FROM_CURRENT = "current";

    public static final String IS_SHOW_CURRENT = "isShowCurrent";

    public static final String EXTRA_SCREEN_ORIENTATION = "extra_screen_orientation";

    protected int mCurrentScrrenRotateIsLandScape = 0; //当前屏幕的方向,在onDesoty的时候需要恢复s

    protected boolean mIsFollowSysRotateForViewPagerFragment = false;

    boolean mForceOrient = false;
    /*fragment根布局*/
    protected View mRootView;

    public abstract int getRequestCode();

    protected int mRequestCode;
    protected FragmentDataListener mDataListener;

    public boolean isScollToTop = false;
    public boolean isSelected = false;
    public boolean isResume = false;
    public static final int FAST_DOUBLE_CLICK_INTERVAL = 500;
    private long sLastClickTime = 0;

    protected boolean mNeedTransmissionTouchEvent = true;

    // 需要类似activity onActivityResult，请务必初始化
    public void initDataResult(int requestCode, @Nullable FragmentDataListener dataListener) {
        if (dataListener == null) {
            MyLog.d(TAG, "initDataResult : FragmentDataListener is null");
            return;
        }
        mRequestCode = requestCode;
        mDataListener = dataListener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (container == null) {
            return null;
        }
        //BaseRotateActivity
        if (getActivity() != null && getActivity() instanceof IRotateActivity && needForceActivityOrientation()) {
            IRotateActivity rotateActivity = (IRotateActivity) getActivity();
            mCurrentScrrenRotateIsLandScape = rotateActivity.getScreenOrientation();

            Bundle b = getArguments();
            if (!mIsFollowSysRotateForViewPagerFragment) {
                String screenOrientation = PARAM_FORCE_PORTRAIT;
                if (b != null) {
                    if (b.containsKey(EXTRA_SCREEN_ORIENTATION)) {
                        screenOrientation = b.getString(EXTRA_SCREEN_ORIENTATION);
                    }
                }
                switch (screenOrientation) {
                    case PARAM_FOLLOW_SYS:
                        break;
                    case PAPAM_FORCE_LANDSCAPE:
                        rotateActivity.forceLandscape();
                        rotateActivity.increaseLockScreenRefCount();
                        mForceOrient = true;
                        break;
                    case PARAM_FORCE_PORTRAIT:
                        rotateActivity.forcePortrait();
                        rotateActivity.increaseLockScreenRefCount();
                        mForceOrient = true;
                        break;
                }
            }
        }
        mRootView = createView(inflater, container);
        ButterKnife.bind(this, mRootView);
        // 阻碍点击事件穿透
//        mRootView.setClickable(true);
        if (mNeedTransmissionTouchEvent) {
            mRootView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return true;
                }
            });
        }
        bindView();
        return mRootView;
    }

    /**
     * 是否需要　强制设置attach的activity的方向, 子类需要override该方法, 默认返回true
     *
     * @return
     * @author yaojian 2016-10-26
     */
    protected boolean needForceActivityOrientation() {
        return true;
    }


    protected abstract View createView(LayoutInflater inflater, ViewGroup container);

    protected abstract void bindView();

    @Override
    public boolean onBackPressed() {
        return false;
    }

    @Override
    public boolean onHomePressed() {
        return false;
    }

    @Override
    public void onDeselect() {
        isSelected = false;
    }

    @Override
    public void onSelect() {
        isSelected = true;
    }

    @Override
    public void onAttach(Activity activity) {
        if (TAG != null) {
            MyLog.d(TAG, "onAttach");
        }
        super.onAttach(activity);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        if (TAG != null) {
//            MyLog.d(TAG, "onCreate");
        }
        super.onCreate(savedInstanceState);
        addSelfToStatusList();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        if (TAG != null) {
//            MyLog.d(TAG, "onActivityCreated");
        }
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        if (TAG != null) {
//            MyLog.d(TAG, "onStart");
        }
        super.onStart();
    }

    @Override
    public void onResume() {
        if (TAG != null) {
//            MyLog.d(TAG, "onResume");
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        if (TAG != null) {
//            MyLog.d(TAG, "onPause");
        }
        super.onPause();
    }

    @Override
    public void onStop() {
        if (TAG != null) {
//            MyLog.d(TAG, "onStop");
        }
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        if (TAG != null) {
//            MyLog.d(TAG, "onDestroyView");
        }
        super.onDestroyView();
        if (mForceOrient) {
            if (getActivity() instanceof IRotateActivity) {
                IRotateActivity rotateActivity = (IRotateActivity) getActivity();
                rotateActivity.decreaseLockScreenRefCount();
                rotateActivity.forceRotate(mCurrentScrrenRotateIsLandScape);
                if (rotateActivity.checkLockScreenRefCount()) {
                    if (rotateActivity.isRotateOn()) {
                        rotateActivity.openOrientation();
                    } else {
                        rotateActivity.openOrientationButNotRotate();
                    }
                }
                mForceOrient = false;
            }
        }
    }

    @Override
    public void onDestroy() {
        if (TAG != null) {
//            MyLog.d(TAG, "onDestroy");
        }
        super.onDestroy();
        if (!Constants.isDebugMiChanel) { //耗时包不监控
            RefWatcher refWatcher = GlobalData.getRefWatcher();
            if (refWatcher != null) {
                refWatcher.watch(this);
            }
        }
        removeSelfFromStatusList();
    }

    protected String getTAG() {
        return getClass().getSimpleName();
    }

    //默认500毫秒
    public boolean isFastDoubleClick() {
        long now = System.currentTimeMillis();
        if (now - sLastClickTime > 0 && now - sLastClickTime < FAST_DOUBLE_CLICK_INTERVAL) {
            //加个大于0的判断是为了防止用户手动把系统时间调小,就会导致这里永远返回true
            return true;
        }
        sLastClickTime = now;
        return false;
    }

    @Override
    public void onDetach() {
        MyLog.v(BaseFragment.class.getName() + " finish");

        if (TAG != null) {
//            MyLog.d(TAG, "onDetach");
        }
        super.onDetach();
        try {
            Field childFragmentManager = Fragment.class.getDeclaredField("mChildFragmentManager");
            childFragmentManager.setAccessible(true);
            childFragmentManager.set(this, null);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Shows a {@link android.widget.Toast} message.
     *
     * @param message An string representing a message to be shown.
     */
    protected void showToastMessage(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public final void addSelfToStatusList() {
        if (isOverrideStatusBar()) {
//            MyLog.d(TAG, "addSelfToStatusList");
            sStatusBarOperators.add(this);
            adjustStatusBar();
        }
    }

    @Override
    public final void removeSelfFromStatusList() {
        if (isOverrideStatusBar()) {
            MyLog.d(TAG, "removeSelfFromStatusList");
            BaseActivity.sStatusBarOperators.remove(this);
            if (!sStatusBarOperators.isEmpty()) {
                IStatusBarOperator statusBarOperator = sStatusBarOperators.getLast();
                if (statusBarOperator != null) {
                    statusBarOperator.restoreStatusBar(this.isStatusBarDark(), false);
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
            BaseActivity.setStatusColor(getActivity(), isStatusBarDark());
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
    public boolean isStatusBarDark() {
        return true;
    }

    @Override
    public boolean isOverrideStatusBar() {
        return false;
    }

    /**
     * 不想使用ButterKnife，可以使用下面方法简化代码，JQuery选择器风格
     */
    @Nullable
    public <V extends View> V $(@IdRes int resId) {
        if (mRootView == null) {
            return null;
        }
        return (V) mRootView.findViewById(resId);
    }

    @Override
    public void onSelfPopped() {
        FragmentNaviUtils.popFragment(getActivity());
    }
}
