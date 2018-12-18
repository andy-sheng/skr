package com.common.utils;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;

import com.common.base.BaseFragment;
import com.common.base.FragmentDataListener;
import com.common.base.R;
import com.common.log.MyLog;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.List;

/**
 * 支持 v4 fragment 的一些工具方法
 * <p>
 * 参考  https://github.com/gpfduoduo/android-article/blob/master/Activity%20%2B%20%E5%A4%9AFrament%20%E4%BD%BF%E7%94%A8%E6%97%B6%E7%9A%84%E4%B8%80%E4%BA%9B%E5%9D%91.md
 */
public class FragmentUtils {

    public final static String TAG = "FragmentUtils";

    FragmentUtils() {

    }

    public BaseFragment getTopFragmentFromBackStack(FragmentActivity activity) {
        /**
         * 如果没有setAddToBackStack
         * 这里返回的是null啊
         */
        FragmentManager fm = activity.getSupportFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {
            //退出栈弹出
            String fName = fm.getBackStackEntryAt(fm.getBackStackEntryCount() - 1).getName();
            if (!TextUtils.isEmpty(fName)) {
                Fragment fragment = fm.findFragmentByTag(fName);
                if (fragment instanceof BaseFragment) {
                    return (BaseFragment) fragment;
                }
            }
        }
        return null;
    }

    public BaseFragment getTopFragment(FragmentActivity activity) {
        /**
         * 如果没有setAddToBackStack
         * 这里返回的是null啊
         */
        FragmentManager fm = activity.getSupportFragmentManager();
        List<Fragment> l = fm.getFragments();
        if (l.isEmpty()) {
            return null;
        }
        return (BaseFragment) l.get(l.size() - 1);
    }


    /**
     * 弹出activity 顶部的fragment
     */
    public boolean popFragment(FragmentActivity activity) {
        if (activity == null) {
            return false;
        }
        try {
//            if (immediate) {
            return activity.getSupportFragmentManager().popBackStackImmediate();
//            } else {
            // 这个方法会丢到主线程队列的末尾去执行
//                activity.getSupportFragmentManager().popBackStack();
//            }
        } catch (IllegalStateException e) {
            MyLog.e(e);
        }
        return false;
    }

    /**
     * 弹出activity fragment及其以上所有的fragment
     */
    public boolean popFragment(BaseFragment fragment) {
        popFragment(newPopParamsBuilder(fragment).build());
        return false;
    }

    public boolean popFragment(PopParams params) {
        FragmentManager fragmentManager = null;
        if (params.popFragment != null) {
            fragmentManager = params.popFragment.getActivity().getSupportFragmentManager();
            if (fragmentManager != null) {
                FragmentTransaction ft = fragmentManager.beginTransaction();

                for (Fragment f : fragmentManager.getFragments()) {
                    if (f == params.popFragment) {
                        if (params.popAbove) {
                            /**
                             *  至于int flags有两个取值：0或FragmentManager.POP_BACK_STACK_INCLUSIVE；
                             *  当取值0时，表示除了参数一指定这一层之上的所有层都退出栈，指定的这一层为栈顶层； 
                             *  当取值POP_BACK_STACK_INCLUSIVE时，表示连着参数一指定的这一层一起退出栈
                             *  另外第一个参数一般用 tag ，只有静态添加的 fragment 才用id
                             */
                            fragmentManager.popBackStackImmediate(f.getTag(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
                        } else {
                            ft = ft.remove(f);
                        }
                    }
                }

                if (params.showFragment != null) {
                    String showTag = params.showFragment.getName();
                    Fragment showFragment = fragmentManager.findFragmentByTag(showTag);
                    if (showFragment != null) {
                        ft = ft.show(showFragment);
                    }
                }
                ft.commitAllowingStateLoss();
                return true;
            }
        }
        return false;
    }

    /**
     * 所有的 fragment 都通过这个方法来添加
     * 这个方法会保证 同一个 classname 的 fragment 只会在 栈中 有一个实例
     * 通过 {@method FragmentUtils.newParamsBuilder()} 构造参数
     *
     * @param params
     * @return
     */
    public BaseFragment addFragment(AddParams params) {
        // 这里用 类名做TAG 保证了
        String showTag = params.targetFragment.getName();

        FragmentManager fragmentManager = params.fragmentActivity.getSupportFragmentManager();
        Object fragmentObject = fragmentManager.findFragmentByTag(showTag);

        FragmentTransaction ft = fragmentManager.beginTransaction();

        if (params.hideFragment != null) {
            String hideTag = params.hideFragment.getName();
            Fragment hideFragment = fragmentManager.findFragmentByTag(hideTag);
            if (hideFragment != null) {
                ft = ft.hide(hideFragment);
            }
        }

        if (fragmentObject != null
                && (fragmentObject instanceof BaseFragment)) {
            BaseFragment oldFragment = (BaseFragment) fragmentObject;
            if (oldFragment != null) {
                MyLog.d(TAG, "addFragment" + " oldFragment!=null");
                if (params.useOldFragmentIfExist) {
                    if (!oldFragment.isAdded()) {
                        ft = ft.add(params.containerViewId, oldFragment, showTag);
                    }
                    if (oldFragment.isHidden()) {
                        ft = ft.show(oldFragment);
                    }
                    ft.commitAllowingStateLoss();
                    return oldFragment;
                } else {
                    ft = ft.remove(oldFragment);
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
        fragment.setFragmentDataListener(params.fragmentDataListener);
        if (!params.dataBeforeAddMap.isEmpty()) {
            for (Integer dataType : params.dataBeforeAddMap.keySet()) {
                Object dataBeforeAdd = params.dataBeforeAddMap.get(dataType);
                fragment.setData(dataType, dataBeforeAdd);
            }
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
            ft = ft.setCustomAnimations(params.enterAnim, params.exitAnim, params.enterAnim, params.exitAnim);
        }
        /**
         * 是否加到回退栈中，back键盘返回上一个fragment
         * getBackStackEntryCount 的值 跟 这个 无关。
         */
        if (params.addToBackStack) {
            ft = ft.addToBackStack(showTag);
        }
        ft = ft.add(params.containerViewId, fragment, showTag);
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

    public static AddParams.Builder newAddParamsBuilder(FragmentActivity
                                                                fragmentActivity, Class<? extends Fragment> fragment) {
        return new AddParams.Builder()
                .setFragmentActivity(fragmentActivity)
                .setTargetFragment(fragment);
    }

    public static PopParams.Builder newPopParamsBuilder(BaseFragment popFragment) {
        return new PopParams.Builder()
                .setPopFragment(popFragment);
    }

    /**
     * 打开一个fragment可能需要的参数
     * 使用 builder 模式
     */
    static class PopParams {
        BaseFragment popFragment;// 要弹出fragment
        boolean popAbove = true;// 包不包括自己以上的所有fragment，true为包括，false为只会remove自己
        Class<? extends Fragment> showFragment;//弹出时要显示的fragment

        public void setPopFragment(BaseFragment popFragment) {
            this.popFragment = popFragment;
        }

        public void setShowFragment(Class<? extends Fragment> showFragment) {
            this.showFragment = showFragment;
        }

        public void setPopAbove(boolean popAbove) {
            this.popAbove = popAbove;
        }

        public static class Builder {
            PopParams mParams = new PopParams();

            Builder() {
            }

            public Builder setPopFragment(BaseFragment popFragment) {
                mParams.setPopFragment(popFragment);
                return this;
            }

            public Builder setShowFragment(Class<? extends Fragment> showFragment) {
                mParams.setShowFragment(showFragment);
                return this;
            }

            public Builder setPopAbove(boolean popAbove) {
                mParams.setPopAbove(popAbove);
                return this;
            }

            public PopParams build() {
                return mParams;
            }
        }
    }

    /**
     * 打开一个fragment可能需要的参数
     * 使用 builder 模式
     */
    static class AddParams {
        BaseFragment fromFragment;//来源于哪个 fragment 主要是 setTargetFragment 时使用
        Class<? extends Fragment> hideFragment;//启动的目标fragment时要隐藏的fragment
        Class<? extends Fragment> targetFragment;//要启动的目标fragment
        //  不用 content 做container，因为android4957等等都是基于actvity的根布局做逻辑的
        // int containerViewId = android.R.id.content;
        int containerViewId = R.id.main_act_container;
        Bundle bundle = null;
        boolean addToBackStack = true;
        boolean hasAnimation = false;
        boolean allowStateLoss = true;
        int enterAnim = R.anim.slide_right_in;
        int exitAnim = R.anim.slide_right_out;
        FragmentActivity fragmentActivity;
        FragmentDataListener fragmentDataListener;
        /**
         * 允许在Fragment 被add之前，给fragment 设置一些值。会调用 setData 方法。
         * 虽然大部分值可以通过bundle传递，但有些值不适合bundle传递，比如一些很长的list
         * 一个很特别的回调等等。
         */
        HashMap<Integer, Object> dataBeforeAddMap = new HashMap<>();

        /**
         * 用tag的一定要确保只有一个实例被添加
         * useOldFragmentIfExist = true
         * 低内存回收时，是否要用原内存中的fragment重建个fragment
         */
        boolean useOldFragmentIfExist = false;

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

        public void setFragmentDataListener(FragmentDataListener l) {
            this.fragmentDataListener = l;
        }

        public void addDataBeforeAdd(int dataType, Object dataBeforeAdd) {
            this.dataBeforeAddMap.put(dataType, dataBeforeAdd);
        }

        public void setUseOldFragmentIfExist(boolean enable) {
            this.useOldFragmentIfExist = enable;
        }

        public void setHideFragment(Class<? extends Fragment> hideFragment) {
            this.hideFragment = hideFragment;
        }

        public static class Builder {
            AddParams mParams = new AddParams();

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

            public Builder setFragmentDataListener(FragmentDataListener l) {
                mParams.setFragmentDataListener(l);
                return this;
            }

            public Builder addDataBeforeAdd(int dataType, Object dataBeforeAdd) {
                mParams.addDataBeforeAdd(dataType, dataBeforeAdd);
                return this;
            }

            public Builder setUseOldFragmentIfExist(boolean enable) {
                mParams.setUseOldFragmentIfExist(enable);
                return this;
            }

            public Builder setHideFragment(Class<? extends Fragment> hideFragment) {
                mParams.setHideFragment(hideFragment);
                return this;
            }


            public AddParams build() {
                return mParams;
            }
        }
    }


}
