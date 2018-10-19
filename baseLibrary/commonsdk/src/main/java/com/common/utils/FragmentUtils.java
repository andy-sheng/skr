package com.common.utils;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.common.base.BaseFragment;
import com.common.base.R;
import com.common.log.MyLog;

import java.lang.reflect.Constructor;

/**
 * 支持 v4 fragment 的一些工具方法
 * <p>
 * 参考  https://github.com/gpfduoduo/android-article/blob/master/Activity%20%2B%20%E5%A4%9AFrament%20%E4%BD%BF%E7%94%A8%E6%97%B6%E7%9A%84%E4%B8%80%E4%BA%9B%E5%9D%91.md
 */
public class FragmentUtils {

    public final static String TAG = "FragmentUtils";

    FragmentUtils() {

    }


    /**
     * @param activity
     */
    public static void popFragment(FragmentActivity activity) {
        if (activity == null) {
            return;
        }
        try {
//            if (immediate) {
            activity.getSupportFragmentManager().popBackStackImmediate();
//            } else {
            // 这个方法会丢到主线程队列的末尾去执行
//                activity.getSupportFragmentManager().popBackStack();
//            }
        } catch (IllegalStateException e) {
            MyLog.e(e);
        }
    }

    /**
     * 所有的 fragment 都通过这个方法来添加
     * 这个方法会保证 同一个 classname 的 fragment 只会在 栈中 有一个实例
     * 通过 {@method FragmentUtils.newParamsBuilder()} 构造参数
     *
     * @param params
     * @return
     */
    public BaseFragment addFragment(Params params) {
        String tag = params.targetFragment.getName();

        FragmentManager fragmentManager = params.fragmentActivity.getSupportFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        Object fragmentObject = fragmentManager.findFragmentByTag(tag);

        if (fragmentObject != null
                && (fragmentObject instanceof BaseFragment)) {
            BaseFragment oldFragment = null;
            oldFragment = (BaseFragment) fragmentManager.findFragmentByTag(tag);
            if (oldFragment != null) {
                MyLog.d(TAG, "addFragment" + " oldFragment!=null");
                if (params.bundle == null) {
                    if (!oldFragment.isAdded()) {
                        ft.add(params.containerViewId, oldFragment, tag);
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

        BaseFragment fragment = null;
        fragment = createFragment(params.targetFragment);
        if (fragment == null) {
            return null;
        }
        if (params.bundle != null) {
            fragment.setArguments(params.bundle);
        }
        /**
         * 将两个 Fragment 建立联系, 使得
         * getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, intent)
         * 能够工作
         */
        if (params.fromFragment != null) {
            fragment.setTargetFragment(params.fromFragment, fragment.getRequestCode());
        }

        if (params.hasAnimation) {
            ft.setCustomAnimations(params.enterAnim, params.exitAnim, params.enterAnim, params.exitAnim);
        }
        /**
         * 是否加到回退栈中，back键盘返回上一个fragment
         * getBackStackEntryCount 的值 跟 这个 无关。
         */
        if (params.addToBackStack) {
            ft.addToBackStack(tag);
        }
        ft.add(params.containerViewId, fragment, tag);
        if (params.allowStateLoss) {
            ft.commitAllowingStateLoss();
        } else {
            ft.commit();
        }

        return fragment;
    }

    /**
     * 最简单的构造 fragment 的方法
     *
     * @param cls
     * @return
     */
    private BaseFragment createFragment(Class<?> cls) {
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

    public static Params.Builder newParamsBuilder(FragmentActivity fragmentActivity, Class<? extends Fragment> fragment) {
        return new Params.Builder()
                .setFragmentActivity(fragmentActivity)
                .setTargetFragment(fragment);
    }

    /**
     * 打开一个fragment可能需要的参数
     * 使用 builder 模式
     */
    static class Params {
        /**
         * 来源于哪个 fragment 主要是 setTargetFragment 时使用
         */
        BaseFragment fromFragment;
        Class<? extends Fragment> targetFragment;
        int containerViewId = android.R.id.content;
        Bundle bundle = null;
        boolean addToBackStack = false;
        boolean hasAnimation = false;
        boolean allowStateLoss = true;
        int enterAnim = R.anim.slide_right_in;
        int exitAnim = R.anim.slide_right_out;
        FragmentActivity fragmentActivity;

        public void setFromFragment(BaseFragment fromFragment) {
            this.fromFragment = fromFragment;
        }

        public void setTargetFragment(Class<? extends Fragment> targetFragment) {
            this.targetFragment = targetFragment;
        }

        public void setContainerViewId(int containerViewId) {
            this.containerViewId = containerViewId;
        }

        public void setBundle(Bundle bundle) {
            this.bundle = bundle;
        }

        public void setAddToBackStack(boolean addToBackStack) {
            this.addToBackStack = addToBackStack;
        }

        public void setHasAnimation(boolean hasAnimation) {
            this.hasAnimation = hasAnimation;
        }

        public void setAllowStateLoss(boolean allowStateLoss) {
            this.allowStateLoss = allowStateLoss;
        }

        public void setEnterAnim(int enterAnim) {
            this.enterAnim = enterAnim;
        }

        public void setExitAnim(int exitAnim) {
            this.exitAnim = exitAnim;
        }

        public void setFragmentActivity(FragmentActivity fragmentActivity) {
            this.fragmentActivity = fragmentActivity;
        }

        public static class Builder {
            Params mParams = new Params();

            Builder() {
            }

            public Builder setFromFragment(BaseFragment fromFragment) {
                mParams.setFromFragment(fromFragment);
                return this;
            }

            public Builder setTargetFragment(Class<? extends Fragment> targetFragment) {
                mParams.setTargetFragment(targetFragment);
                return this;
            }

            public Builder setContainerViewId(int containerViewId) {
                mParams.setContainerViewId(containerViewId);
                return this;
            }

            public Builder setBundle(Bundle bundle) {
                mParams.setBundle(bundle);
                return this;
            }

            public Builder setAddToBackStack(boolean addToBackStack) {
                mParams.setAddToBackStack(addToBackStack);
                return this;
            }

            public Builder setHasAnimation(boolean hasAnimation) {
                mParams.setHasAnimation(hasAnimation);
                return this;
            }

            public Builder setAllowStateLoss(boolean allowStateLoss) {
                mParams.setAllowStateLoss(allowStateLoss);
                return this;
            }

            public Builder setEnterAnim(int enterAnim) {
                mParams.setEnterAnim(enterAnim);
                return this;
            }

            public Builder setExitAnim(int exitAnim) {
                mParams.setExitAnim(exitAnim);
                return this;
            }

            public Builder setFragmentActivity(FragmentActivity fragmentActivity) {
                mParams.setFragmentActivity(fragmentActivity);
                return this;
            }

            public Params build() {
                return mParams;
            }
        }
    }


}
