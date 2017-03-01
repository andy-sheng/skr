package com.wali.live.fragment.utils;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.CheckResult;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;

import com.base.activity.BaseActivity;
import com.base.common.R;
import com.base.log.MyLog;
import com.wali.live.fragment.BaseFragment;

import java.lang.reflect.Constructor;
import java.util.ArrayList;

/**
 * @module base
 */
public class FragmentNaviUtils {
    private final static String TAG = FragmentNaviUtils.class.getSimpleName();

    public static class FragmentArguments {

        public BaseFragment sourceFragment;
        public Class<?> targetFragment;
        public int containerViewId = android.R.id.content;
        public Bundle bundle = null;
        public boolean addToBackStack = true;
        public boolean hasAnimation = false;
        public boolean allowStateLoss = true;
        public int enterAnim;
        public int exitAnim;
        public BaseActivity fragmentActivity;

        public FragmentArguments(BaseFragment sourceFragment, Class<?> targetFragment) {
            this.sourceFragment = sourceFragment;
            this.targetFragment = targetFragment;
        }

        public FragmentArguments(BaseFragment sourceFragment, Class<BaseFragment> targetFragment, int containerViewId) {
            this.sourceFragment = sourceFragment;
            this.targetFragment = targetFragment;
            this.containerViewId = containerViewId;
        }

        public FragmentArguments(BaseFragment sourceFragment, Class<?> targetFragment, Bundle bundle) {
            this.sourceFragment = sourceFragment;
            this.targetFragment = targetFragment;
            this.bundle = bundle;
        }

        public FragmentArguments(BaseFragment sourceFragment, Class<?> targetFragment, Bundle bundle,
                                 boolean hasAnimation, int enterAnim, int exitAnim) {
            this.sourceFragment = sourceFragment;
            this.targetFragment = targetFragment;
            this.bundle = bundle;
            this.hasAnimation = hasAnimation;
            this.enterAnim = enterAnim;
            this.exitAnim = exitAnim;

        }

        public FragmentArguments(BaseActivity fragmentActivity, Class<?> targetFragment, Bundle bundle) {
            this.fragmentActivity = fragmentActivity;
            this.targetFragment = targetFragment;
            this.bundle = bundle;
        }

        public FragmentArguments(BaseActivity fragmentActivity, Class<?> targetFragment, Bundle bundle,
                                 boolean hasAnimation, int enterAnim, int exitAnim) {
            this.fragmentActivity = fragmentActivity;
            this.targetFragment = targetFragment;
            this.bundle = bundle;
            this.hasAnimation = hasAnimation;
            this.enterAnim = enterAnim;
            this.exitAnim = exitAnim;
        }

        public FragmentArguments(BaseFragment sourceFragment, Class<BaseFragment> targetFragment, int containerViewId, Bundle bundle) {
            this.sourceFragment = sourceFragment;
            this.targetFragment = targetFragment;
            this.containerViewId = containerViewId;
            this.bundle = bundle;
        }

        public FragmentArguments(BaseFragment sourceFragment, Class<BaseFragment> targetFragment, int containerViewId,
                                 boolean addToBackStack, Bundle bundle) {
            this.sourceFragment = sourceFragment;
            this.targetFragment = targetFragment;
            this.containerViewId = containerViewId;
            this.addToBackStack = addToBackStack;
            this.bundle = bundle;
        }

        public FragmentArguments(BaseFragment sourceFragment, Class<BaseFragment> targetFragment, int containerViewId,
                                 boolean addToBackStack, Bundle bundle, boolean allowStateLoss) {
            this.sourceFragment = sourceFragment;
            this.targetFragment = targetFragment;
            this.containerViewId = containerViewId;
            this.addToBackStack = addToBackStack;
            this.bundle = bundle;
            this.allowStateLoss = allowStateLoss;
        }

        public FragmentArguments(Builder builder) {
            this.sourceFragment = builder.sourceFragment;
            this.targetFragment = builder.targetFragment;
            this.containerViewId = builder.containerViewId;
            this.bundle = builder.bundle;
            this.addToBackStack = builder.addToBackStack;
            this.hasAnimation = builder.hasAnimation;
            this.allowStateLoss = builder.allowStateLoss;
            this.enterAnim = builder.enterAnim;
            this.exitAnim = builder.exitAnim;
            this.fragmentActivity = builder.fragmentActivity;
        }


        public static class Builder {
            public BaseFragment sourceFragment;
            public Class<?> targetFragment;
            public int containerViewId = android.R.id.content;
            public Bundle bundle = null;
            public boolean addToBackStack = true;
            public boolean hasAnimation = false;
            public boolean allowStateLoss = true;
            public int enterAnim;
            public int exitAnim;
            public BaseActivity fragmentActivity;

            public Builder() {

            }

            public Builder setSourceFragment(BaseFragment sourceFragment) {
                this.sourceFragment = sourceFragment;
                return this;
            }

            public Builder setTargetFragment(Class<?> targetFragment) {
                this.targetFragment = targetFragment;
                return this;
            }

            public Builder setContainerViewId(int containerViewId) {
                this.containerViewId = containerViewId;
                return this;
            }

            public Builder setBundle(Bundle bundle) {
                this.bundle = bundle;
                return this;
            }

            public Builder setAddToBackStack(boolean addToBackStack) {
                this.addToBackStack = addToBackStack;
                return this;
            }

            public Builder setHasAnimation(boolean hasAnimation) {
                this.hasAnimation = hasAnimation;
                return this;
            }

            public Builder setAllowStateLoss(boolean allowStateLoss) {
                this.allowStateLoss = allowStateLoss;
                return this;
            }

            public Builder setEnterAnim(int enterAnim) {
                this.enterAnim = enterAnim;
                return this;
            }

            public Builder setExitAnim(int exitAnim) {
                this.exitAnim = exitAnim;
                return this;
            }

            public Builder setFragmentActivity(BaseActivity fragmentActivity) {
                this.fragmentActivity = fragmentActivity;
                return this;
            }

            public FragmentArguments build() {
                return new FragmentArguments(this);
            }
        }

    }

    public static BaseFragment addFragment(FragmentArguments fragmentArguments) {
        if (fragmentArguments == null) {
            return null;
        }
        if (fragmentArguments.sourceFragment != null) {
            return addFragment(fragmentArguments.sourceFragment, fragmentArguments.containerViewId,
                    fragmentArguments.targetFragment, fragmentArguments.bundle, fragmentArguments.addToBackStack,
                    fragmentArguments.hasAnimation, fragmentArguments.allowStateLoss);
        }
        if (fragmentArguments.fragmentActivity != null) {
            return addFragment(fragmentArguments.fragmentActivity, fragmentArguments.containerViewId,
                    fragmentArguments.targetFragment, fragmentArguments.bundle, fragmentArguments.addToBackStack,
                    fragmentArguments.hasAnimation, fragmentArguments.allowStateLoss);
        }
        return null;
    }

    public static BaseFragment addFragment(BaseFragment sourceFragment, Class<?> FragmentCls) {
        return addFragment(sourceFragment, FragmentCls, null);
    }

    public static BaseFragment addFragment(BaseFragment sourceFragment, Class<?> FragmentCls, int containerViewId) {
        return addFragment(sourceFragment, FragmentCls, null, containerViewId);
    }

    public static BaseFragment addFragment(BaseFragment sourceFragment, Class<?> FragmentCls, Bundle bundle) {
        return addFragment(sourceFragment, android.R.id.content, FragmentCls, bundle, true, true, true);
    }

    public static BaseFragment addFragment(BaseFragment sourceFragment, Class<?> FragmentCls, Bundle bundle, int containerViewId) {
        return addFragment(sourceFragment, containerViewId, FragmentCls, bundle, true, true, true);
    }

    public static BaseFragment addFragment(BaseFragment sourceFragment, int containerViewId, Class<?> cls, Bundle bundle,
                                           boolean addToBackStack, boolean hasAnimation, boolean allowStateLoss) {
        if (sourceFragment == null) {
            return null;
        }
        FragmentActivity fragmentActivity = sourceFragment.getActivity();
        String tag = cls.getName();
        FragmentManager fragmentManager = fragmentActivity.getSupportFragmentManager();
        BaseFragment fragment = null;
        BaseFragment oldFragment = null;
        FragmentTransaction ft = fragmentManager.beginTransaction();
        if (fragmentManager.findFragmentByTag(tag) != null && (fragmentManager.findFragmentByTag(tag)) instanceof BaseFragment) {
            oldFragment = (BaseFragment) fragmentManager.findFragmentByTag(tag);
            if (oldFragment != null) {
                if (bundle == null) {
                    if (!oldFragment.isAdded()) {
                        ft.add(containerViewId, oldFragment, tag);
                    }
                    if (oldFragment.isHidden()) {
                        ft.show(oldFragment);
                    }
                    ft.commitAllowingStateLoss();
                    return oldFragment;
                } else {
                    ft.remove(oldFragment);
                    ft.commitAllowingStateLoss();

                    ft = fragmentManager.beginTransaction();
                }
            }
        }
        fragment = createFragment(cls);
        if (fragment == null) {
            return null;
        }
        if (bundle != null) {
            fragment.setArguments(bundle);
        }
        fragment.setTargetFragment(sourceFragment, fragment.getRequestCode());

        if (hasAnimation) {
            //ft.setCustomAnimations(R.anim.slide_right_in, R.anim.slide_right_out, R.anim.slide_right_in, R.anim.slide_right_out);
        }
        if (addToBackStack) {
            ft.addToBackStack(tag);
        }
        ft.add(containerViewId, fragment, tag);
//        if (allowStateLoss) {
//            ft.commitAllowingStateLoss();
//        } else {
//            ft.commit();
//        }
        ft.commitAllowingStateLoss();

        return fragment;
    }

    public static BaseFragment addFragment(BaseActivity fragmentActivity, Class<?> FragmentCls) {

        return addFragment(fragmentActivity, FragmentCls, null);
    }

    public static void openFragment(BaseActivity fragmentActivity, Class<?> FragmentCls) {

        openFragment(fragmentActivity, FragmentCls, null, android.R.id.content, false, 0, 0);
    }

    public static BaseFragment addFragment(BaseActivity fragmentActivity, Class<?> FragmentCls, Bundle bundle) {
        return addFragment(fragmentActivity, android.R.id.content, FragmentCls, bundle, true, true, true);
    }

    public static BaseFragment addFragment(BaseActivity fragmentActivity, Class<?> FragmentCls, int containerViewId) {
        return addFragment(fragmentActivity, containerViewId, FragmentCls, null, true, true, true);
    }

    public static BaseFragment addFragment(BaseActivity fragmentActivity, Class<?> FragmentCls, Bundle bundle, int containerViewId) {
        return addFragment(fragmentActivity, containerViewId, FragmentCls, bundle, true, true, true);
    }

    public static BaseFragment addFragment(FragmentActivity fragmentActivity, int containerViewId, Class<?> cls, Bundle bundle,
                                           boolean addToBackStack, boolean hasAnimation, boolean allowStateLoss) {
        int[] anim = {R.anim.slide_right_in, R.anim.slide_right_out, R.anim.slide_right_in, R.anim.slide_right_out};
        return addFragment(fragmentActivity, containerViewId, cls, bundle, addToBackStack, hasAnimation, anim, allowStateLoss);
    }

    /**减少对返回值进行类型转换的操作*/
    @CheckResult
    public static <T extends BaseFragment> T addFragmentHandy(FragmentActivity fragmentActivity, int containerViewId, Class<T> cls, Bundle bundle,
                                                              boolean addToBackStack, boolean hasAnimation, boolean allowStateLoss) {
        return (T) addFragment(fragmentActivity, containerViewId, cls, bundle, addToBackStack, hasAnimation, allowStateLoss);
    }

    //从下向上滑入
    public static BaseFragment addFragment(FragmentActivity fragmentActivity, int containerViewId, Class<?> cls, Bundle bundle,
                                           boolean addToBackStack, boolean hasAnimation, int anim[], boolean allowStateLoss) {
        if (fragmentActivity == null) {
            return null;
        }
        String tag = cls.getName();
        FragmentManager fragmentManager = fragmentActivity.getSupportFragmentManager();
        BaseFragment fragment = null;
        BaseFragment oldFragment = null;
        FragmentTransaction ft = fragmentManager.beginTransaction();
        if (hasAnimation) {
            ft.setCustomAnimations(anim[0], anim[1], anim[2], anim[3]);
        }
        if (fragmentManager.findFragmentByTag(tag) != null && (fragmentManager.findFragmentByTag(tag)) instanceof BaseFragment) {
            oldFragment = (BaseFragment) fragmentManager.findFragmentByTag(tag);
            if (oldFragment != null) {
                if (bundle == null) {
                    if (!oldFragment.isAdded()) {
                        ft.add(containerViewId, oldFragment, tag);
                    }
                    if (oldFragment.isHidden()) {
                        ft.show(oldFragment);
                    }
                    ft.commitAllowingStateLoss();
                    return oldFragment;
                } else {
                    ft.remove(oldFragment);
                    ft.commitAllowingStateLoss();

                    ft = fragmentManager.beginTransaction();
                }
            }
        }
        fragment = createFragment(cls);
        if (fragment == null) {
            return null;
        }
        if (bundle != null) {
            fragment.setArguments(bundle);
        }

        if (addToBackStack) {
            ft.addToBackStack(tag);
        }
        ft.add(containerViewId, fragment, tag);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (!fragmentActivity.isFinishing() && !fragmentActivity.isDestroyed()) {
                ft.commitAllowingStateLoss();
            }
        } else {
            if (!fragmentActivity.isFinishing()) {
                ft.commitAllowingStateLoss();
            }
        }
        return fragment;
    }

    public static BaseFragment addFragmentWithZoomInOutAnimation(BaseActivity fragmentActivity, int containerViewId, Class<?> cls, Bundle bundle,
                                                                 boolean addToBackStack, boolean hasAnimation, boolean allowStateLoss) {
        if (fragmentActivity == null) {
            return null;
        }
        String tag = cls.getName();
        FragmentManager fragmentManager = fragmentActivity.getSupportFragmentManager();
        BaseFragment fragment = null;
        BaseFragment oldFragment = null;
        FragmentTransaction ft = fragmentManager.beginTransaction();
        if (hasAnimation) {
            ft.setCustomAnimations(R.anim.zoom_in, R.anim.zoom_out, R.anim.zoom_in, R.anim.zoom_out);
        }
        if (fragmentManager.findFragmentByTag(tag) != null && (fragmentManager.findFragmentByTag(tag)) instanceof BaseFragment) {
            oldFragment = (BaseFragment) fragmentManager.findFragmentByTag(tag);
            if (oldFragment != null) {
                popFragment(fragmentActivity);
                ft.remove(oldFragment);
//                if(bundle == null){
//                    if(!oldFragment.isAdded()){
//                        ft.add(containerViewId, oldFragment, tag);
//                    }
//                    if(oldFragment.isHidden()){
//                        ft.show(oldFragment);
//                    }
//                    ft.commitAllowingStateLoss();
//                    return oldFragment;
//                }else{
//                    ft.remove(oldFragment);
//                }
            }
        }
        fragment = createFragment(cls);
        if (fragment == null) {
            return null;
        }
        if (bundle != null) {
            fragment.setArguments(bundle);
        }

        if (addToBackStack) {
            ft.addToBackStack(tag);
        }
        ft.add(containerViewId, fragment, tag);
        if (!fragmentActivity.isFinishing()) {
//            if (allowStateLoss) {
//                ft.commitAllowingStateLoss();
//            } else {
//                ft.commit();
//            }
            ft.commitAllowingStateLoss();
        }
        return fragment;
    }

    /**
     * 加时间戳重名防止重名
     */
    public static BaseFragment addFragmentDuplicatly(BaseActivity fragmentActivity, int containerViewId, Class<?> cls, Bundle bundle,
                                                     boolean addToBackStack, boolean hasAnimation, boolean allowStateLoss) {
        if (fragmentActivity == null) {
            return null;
        }
        // 加时间戳重名
        String tag = cls.getName() + "#" + System.currentTimeMillis();
        FragmentManager fragmentManager = fragmentActivity.getSupportFragmentManager();
        BaseFragment fragment = null;
        if (fragmentManager.findFragmentByTag(tag) != null && (fragmentManager.findFragmentByTag(tag)) instanceof BaseFragment) {
            fragment = (BaseFragment) fragmentManager.findFragmentByTag(tag);
        }
        if (fragment == null) {
            fragment = createFragment(cls);
        }

        if (fragment == null) {
            return null;
        }
        if (bundle != null) {
            fragment.setArguments(bundle);
        }
        FragmentTransaction ft = fragmentManager.beginTransaction();
        if (hasAnimation) {
            ft.setCustomAnimations(R.anim.slide_right_in, R.anim.slide_right_out, R.anim.slide_right_in, R.anim.slide_right_out);
        }
        if (addToBackStack) {
            ft.addToBackStack(tag);
        }
        ft.add(containerViewId, fragment, tag);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (!fragmentActivity.isFinishing() && !fragmentActivity.isDestroyed()) {
                ft.commitAllowingStateLoss();
            }
        } else {
            if (!fragmentActivity.isFinishing()) {
                ft.commitAllowingStateLoss();
            }
        }
        return fragment;
    }

    public static void switchToFragment(FragmentArguments arguments) {
        if (arguments == null) {
            return;
        }
        if (arguments.sourceFragment != null) {
            switchToFragment(arguments.sourceFragment, arguments.targetFragment, arguments.bundle,
                    arguments.containerViewId, arguments.hasAnimation, arguments.enterAnim, arguments.exitAnim);
        }
        if (arguments.fragmentActivity != null) {
            openFragment(arguments.fragmentActivity, arguments.targetFragment, arguments.bundle,
                    arguments.containerViewId, arguments.hasAnimation, arguments.enterAnim, arguments.exitAnim);
        }

    }

    public static void switchToFragment(BaseFragment sourceFragment, Class<?> FragmentCls, Bundle bundle,
                                        int containerViewId, boolean hasAnimation, int enterAnim, int exitAnim) {
        if (sourceFragment == null) {
            return;
        }
        FragmentActivity fragmentActivity = sourceFragment.getActivity();
        //BaseFragment sourceFragment = fragmentActivity.getSupportFragmentManager().getFragments();
        String tag = FragmentCls.getName();
        FragmentManager fragmentManager = fragmentActivity.getSupportFragmentManager();
        BaseFragment fragment = null;
        if (fragmentManager.findFragmentByTag(tag) != null && (fragmentManager.findFragmentByTag(tag)) instanceof BaseFragment) {
            fragment = (BaseFragment) fragmentManager.findFragmentByTag(tag);
        }
        if (fragment == null) {
            fragment = createFragment(FragmentCls);
        }
        if (fragment == null) {
            return;
        }
        if (bundle != null) {
            fragment.setArguments(bundle);
        }
        fragment.setTargetFragment(sourceFragment, fragment.getRequestCode());
        FragmentTransaction ft = fragmentManager.beginTransaction();
        if (hasAnimation) {
            ft.setCustomAnimations(enterAnim, exitAnim);
        }
        ft.replace(containerViewId, fragment, tag);
        ft.addToBackStack(tag);
        ft.commitAllowingStateLoss();

    }

    public static void openFragment(FragmentActivity fragmentActivity, Class<?> FragmentCls, Bundle bundle,
                                    int containerViewId, boolean hasAnimation, int enterAnim, int exitAnim) {
        if (fragmentActivity == null) {
            return;
        }
        //BaseFragment sourceFragment = fragmentActivity.getSupportFragmentManager().getFragments();
        String tag = FragmentCls.getName();
        FragmentManager fragmentManager = fragmentActivity.getSupportFragmentManager();
        BaseFragment fragment = null;
        if (fragmentManager.findFragmentByTag(tag) != null && (fragmentManager.findFragmentByTag(tag)) instanceof BaseFragment) {
            fragment = (BaseFragment) fragmentManager.findFragmentByTag(tag);
        }
        if (fragment == null) {
            fragment = createFragment(FragmentCls);
        }
        if (fragment == null) {
            return;
        }
        if (bundle != null) {
            fragment.setArguments(bundle);
        }

        FragmentTransaction ft = fragmentManager.beginTransaction();
        if (hasAnimation) {
            ft.setCustomAnimations(enterAnim, exitAnim);
        }
        ft.replace(containerViewId, fragment, tag);
        ft.addToBackStack(tag);
        ft.commitAllowingStateLoss();
    }

    public static void switchToFragment(FragmentActivity fragmentActivity, Class<?> FragmentCls, Bundle bundle) {
        if (fragmentActivity == null) {
            return;
        }
        String tag = FragmentCls.getName();
        FragmentManager fragmentManager = fragmentActivity.getSupportFragmentManager();
        BaseFragment fragment = null;
        if (fragmentManager.findFragmentByTag(tag) != null && (fragmentManager.findFragmentByTag(tag)) instanceof BaseFragment) {
            fragment = (BaseFragment) fragmentManager.findFragmentByTag(tag);
        }
        if (fragment == null) {
            fragment = createFragment(FragmentCls);
        }
        if (fragment == null) {
            return;
        }
        if (bundle != null) {
            fragment.setArguments(bundle);
        }
        //fragment.setTargetFragment(sourceFragment,fragment.requestCode);
        FragmentTransaction t = fragmentManager.beginTransaction();
        t.replace(android.R.id.content, fragment, tag);
        t.addToBackStack(tag);
        t.commitAllowingStateLoss();
    }

    public static Fragment addFragment(BaseFragment fragment, int containerViewId, Class<BaseFragment> cls, Bundle bundle,
                                       boolean hasAnimation, boolean allowStateLoss) {
        return addFragment(fragment, containerViewId, cls, bundle, true, hasAnimation, allowStateLoss);
    }

    // added by mk
    public static BaseFragment addFragmentJustLikeSingleTask(FragmentActivity fragmentActivity, int containerViewId, Class<?> cls, Bundle bundle,
                                                             boolean addToBackStack, boolean hasAnimation, boolean allowStateLoss) {
        if (fragmentActivity == null) {
            return null;
        }
        MyLog.d(TAG, "addFragmentJustLikeSingleTask");
        BaseFragment fragment = createFragment(cls);
        String tag = cls.getName();
        if (fragment == null) {
            return null;
        }
        if (bundle != null) {
            fragment.setArguments(bundle);
        }
        FragmentTransaction ft = fragmentActivity.getSupportFragmentManager().beginTransaction();
        if (hasAnimation) {
            ft.setCustomAnimations(R.anim.slide_right_in, R.anim.slide_right_out, R.anim.slide_right_in, R.anim.slide_right_out);
        }

        ArrayList<String> tagList = new ArrayList<String>();
        if (fragmentActivity.getSupportFragmentManager().findFragmentByTag(tag) != null) {
            int index = -1;
            int size = fragmentActivity.getSupportFragmentManager().getBackStackEntryCount();

            for (int i = size - 1; i > -1; i--) {
                if (fragmentActivity.getSupportFragmentManager().getBackStackEntryAt(i).getName().startsWith(tag)) {
                    index = i;
                    break;
                }
            }
            //MyLog.v(" remove old fragment, index=" + index + ", size=" + size);
            for (int i = size - 1; i > index - 1 && i > -1; i--) {
                tagList.add(fragmentActivity.getSupportFragmentManager().getBackStackEntryAt(i).getName());
            }
        }
        if (addToBackStack) {
            ft.addToBackStack(tag);
        }
        ft.add(containerViewId, fragment, tag);

        ft.commitAllowingStateLoss();
        for (int i = 0; i < tagList.size(); i++) {
            fragmentActivity.getSupportFragmentManager().popBackStackImmediate(tagList.get(i), 0);
        }
        return fragment;
    }

    public static BaseFragment addFragmentInFromBottom(FragmentActivity fragmentActivity, int containerViewId, Class<?> cls, Bundle bundle,
                                                       boolean addToBackStack, boolean hasAnimation, boolean allowStateLoss) {

        if (fragmentActivity == null) {
            return null;
        }
        String tag = cls.getName();
        FragmentManager fragmentManager = fragmentActivity.getSupportFragmentManager();
        BaseFragment fragment = null;
        BaseFragment oldFragment = null;
        FragmentTransaction ft = fragmentManager.beginTransaction();
        if (hasAnimation) {
            ft.setCustomAnimations(R.anim.slide_in_from_bottom, R.anim.slide_out_to_bottom, R.anim.slide_in_from_bottom, R.anim.slide_out_to_bottom);
        }
        if (fragmentManager.findFragmentByTag(tag) != null && (fragmentManager.findFragmentByTag(tag)) instanceof BaseFragment) {
            oldFragment = (BaseFragment) fragmentManager.findFragmentByTag(tag);
            if (oldFragment != null) {
                popFragment(fragmentActivity);
                ft.remove(oldFragment);
//                if(bundle == null){
//                    if(!oldFragment.isAdded()){
//                        ft.add(containerViewId, oldFragment, tag);
//                    }
//                    if(oldFragment.isHidden()){
//                        ft.show(oldFragment);
//                    }
//                    ft.commitAllowingStateLoss();
//                    return oldFragment;
//                }else{
//                    ft.remove(oldFragment);
//                }
            }
        }
        fragment = createFragment(cls);
        if (fragment == null) {
            return null;
        }
        if (bundle != null) {
            fragment.setArguments(bundle);
        }

        if (addToBackStack) {
            ft.addToBackStack(tag);
        }
        ft.add(containerViewId, fragment, tag);
        if (!fragmentActivity.isFinishing()) {
//            if (allowStateLoss) {
//                ft.commitAllowingStateLoss();
//            } else {
//                ft.commit();
//            }
            ft.commitAllowingStateLoss();
        }
        return fragment;
    }

    public static Fragment addFragmentWithFadeInOutAnimation(FragmentActivity fragmentActivity, int containerViewId, Class<?> cls, Bundle bundle,
                                                             boolean addToBackStack, boolean hasAnimation, boolean allowStateLoss) {
        if (fragmentActivity == null) {
            return null;
        }
        Fragment fragment = createFragment(cls);
        String tag = cls.getName() + System.currentTimeMillis();
        if (fragment == null) {
            return null;
        }
        if (bundle != null) {
            fragment.setArguments(bundle);
        }
        FragmentTransaction ft = fragmentActivity.getSupportFragmentManager().beginTransaction();
        if (hasAnimation) {
            ft.setCustomAnimations(R.anim.fragment_slide_fade_in, R.anim.fragment_slide_fade_out, R.anim.fragment_slide_fade_in, R.anim.fragment_slide_fade_out);
        }

        if (addToBackStack) {
            ft.addToBackStack(tag);
        }
        ft.add(containerViewId, fragment, tag);
        ft.commitAllowingStateLoss();
        return fragment;
    }

    public static void removeFragment(Fragment fragment) {
        removeFragment(fragment, false, true);
    }

    public static void removeFragment(Fragment fragment, boolean hasAnimation, boolean allowStateLoss) {
        if (fragment == null) {
            return;
        }
        FragmentActivity fragmentActivity = fragment.getActivity();
        if (null != fragmentActivity && !fragmentActivity.isFinishing()) {
            FragmentManager fm = fragmentActivity.getSupportFragmentManager();
            if (null != fm) {
                FragmentTransaction ft = fm.beginTransaction();
                if (hasAnimation) {
                    //ft.setCustomAnimations(0, R.anim.slide_right_out, 0, R.anim.slide_right_out);
                }
                ft.remove(fragment);
//                if (allowStateLoss) {
//                    ft.commitAllowingStateLoss();
//                } else {
//                    ft.commit();
//                }
                ft.commitAllowingStateLoss();
                popFragmentFromStack(fragmentActivity);
            }
        }
    }

    /** chenyong1 无动画 */
    public static void popFragmentFromStack(FragmentActivity activity) {
        if (activity == null) {
            return;
        }
        try {
            activity.getSupportFragmentManager().popBackStackImmediate();
        } catch (IllegalStateException e) {
            MyLog.e(e);
        }
    }

    /** chenyong1 有动画 */
    public static void popFragment(FragmentActivity activity) {
        if (activity == null) {
            return;
        }
        try {
            activity.getSupportFragmentManager().popBackStack();
        } catch (IllegalStateException e) {
            MyLog.e(e);
        }
    }

    public static void removeFragment(FragmentActivity activity, Fragment fragment) {
        if (null != activity && null != fragment) {
            FragmentManager fm = activity.getSupportFragmentManager();
            if (null != fm) {
                FragmentTransaction ft = fm.beginTransaction();
                ft.remove(fragment);
                ft.commitAllowingStateLoss();
            }
        }
    }

    public static void showFragment(Fragment fragment, FragmentActivity activity) {
        if (fragment == null || activity == null) {
            return;
        }
        FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
        transaction.show(fragment);
        transaction.commitAllowingStateLoss();
    }

    public static void hideFragment(Fragment fragment, FragmentActivity activity) {
        if (fragment == null || activity == null || activity.isFinishing()) {
            return;
        }
        FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
        transaction.hide(fragment);
        transaction.commitAllowingStateLoss();
    }


    public static void popAllFragmentFromStack(FragmentActivity activity) {
        if (activity == null) {
            return;
        }
        activity.getSupportFragmentManager().popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    public static BaseFragment createFragment(Class<?> cls) {
        Constructor<?> ctor;
        try {
            ctor = cls.getConstructor();
            Object object = ctor.newInstance();
            if (object instanceof BaseFragment) {
                return (BaseFragment) object;
            } else {
                throw new Exception();
            }
        } catch (Exception e) {
            MyLog.e(e);
            return null;
        }
    }

    public static Fragment addFragmentAndResetArgument(BaseActivity fragmentActivity, int containerViewId, Class<?> cls, Bundle bundle, boolean addToBackStack, boolean hasAnimation, int enterAnimation, int exitAnimation) {
        if (fragmentActivity == null) {
            return null;
        }
        String tag = cls.getName();
        FragmentManager fragmentManager = fragmentActivity.getSupportFragmentManager();
        BaseFragment oldFragment = null;
        BaseFragment newFragment = null;

        if (fragmentManager.findFragmentByTag(tag) != null && (fragmentManager.findFragmentByTag(tag)) instanceof BaseFragment) {
            oldFragment = (BaseFragment) fragmentManager.findFragmentByTag(tag);
        }

        newFragment = createFragment(cls);

        FragmentTransaction ft = fragmentManager.beginTransaction();
        if (oldFragment != null) {
            ft.remove(oldFragment);
        }
        if (newFragment != null && bundle != null) {
            newFragment.setArguments(bundle);
        }
        if (hasAnimation) {
            ft.setCustomAnimations(enterAnimation, exitAnimation, enterAnimation, exitAnimation);
        }
        if (addToBackStack) {
            ft.addToBackStack(tag);
        }
        if (newFragment != null && !newFragment.isAdded()) {
            ft.add(containerViewId, newFragment, tag);
        }
        if (!fragmentActivity.isFinishing()) {
            ft.commitAllowingStateLoss();
        }
        return newFragment;
    }

    public static Fragment addFragmentAndResetArgumentToBackStack(BaseActivity fragmentActivity, int containerViewId, Class<?> cls, Bundle bundle, boolean hasAnimation, int enterAnimation, int exitAnimation) {
        if (fragmentActivity == null) {
            return null;
        }
        String tag = cls.getName();
        FragmentManager fragmentManager = fragmentActivity.getSupportFragmentManager();
        BaseFragment oldFragment = null;
        BaseFragment newFragment = null;

        if (fragmentManager.findFragmentByTag(tag) != null && (fragmentManager.findFragmentByTag(tag)) instanceof BaseFragment) {
            oldFragment = (BaseFragment) fragmentManager.findFragmentByTag(tag);
        }

        newFragment = createFragment(cls);

        FragmentTransaction ft = fragmentManager.beginTransaction();
        if (oldFragment != null) {
            ft.remove(oldFragment);
        }
        if (newFragment != null && bundle != null) {
            newFragment.setArguments(bundle);
        }
        if (hasAnimation) {
            ft.setCustomAnimations(enterAnimation, exitAnimation, enterAnimation, exitAnimation);
        }
        if (newFragment != null && !newFragment.isAdded()) {
            ft.add(containerViewId, newFragment, tag);
        }
        ft.addToBackStack(tag);
        if (!fragmentActivity.isFinishing()) {
            ft.commitAllowingStateLoss();
        }
        return newFragment;
    }

    public static Fragment addFragmentToBackStack(BaseActivity fragmentActivity, int containerViewId, Class<?> cls, Bundle bundle, boolean hasAnimation, int enterAnimation, int exitAnimation) {
        if (fragmentActivity == null) {
            return null;
        }
        String tag = cls.getName() + System.currentTimeMillis();
        FragmentManager fragmentManager = fragmentActivity.getSupportFragmentManager();
        BaseFragment newFragment = null;
        newFragment = createFragment(cls);

        FragmentTransaction ft = fragmentManager.beginTransaction();
        if (newFragment != null && bundle != null) {
            newFragment.setArguments(bundle);
        }
        if (hasAnimation) {
            ft.setCustomAnimations(enterAnimation, exitAnimation, enterAnimation, exitAnimation);
        }
        if (newFragment != null && !newFragment.isAdded()) {
            ft.add(containerViewId, newFragment, tag);
        }
        ft.addToBackStack(tag);
        if (!fragmentActivity.isFinishing()) {
            ft.commitAllowingStateLoss();
        }
        return newFragment;
    }


    public static Fragment getTopFragment(FragmentActivity activity) {
        FragmentManager fm = activity.getSupportFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {
            String fName = fm.getBackStackEntryAt(fm.getBackStackEntryCount() - 1).getName();
            if (!TextUtils.isEmpty(fName)) {
                Fragment fragment = fm.findFragmentByTag(fName);
                if (null != fragment) {
                    return fragment;
                }
            }
        }
        return null;
    }

    public static int getStackFragmentCount(FragmentActivity activity) {
        FragmentManager fm = activity.getSupportFragmentManager();
        return fm.getBackStackEntryCount();
    }
}
