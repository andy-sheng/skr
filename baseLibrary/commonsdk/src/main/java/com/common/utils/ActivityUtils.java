package com.common.utils;

import android.app.Activity;
import android.app.Dialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.design.widget.Snackbar;
import android.view.View;

import com.common.log.MyLog;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ActivityUtils {
    public final static String TAG = "ActivityUtils";

    ActivityUtils() {

    }

    final Map<Activity, Set<OnActivityDestroyedListener>> mDestroyedListenerMap = new HashMap<>();

    //    //true 为不需要加入到 Activity 容器进行统一管理,默认为 false
    public static final String IS_NOT_ADD_ACTIVITY_LIST = "is_not_add_activity_list";
    //    public static final int START_ACTIVITY = 5000;
//    public static final int SHOW_SNACKBAR = 5001;
//    public static final int KILL_ALL = 5002;
//    public static final int APP_EXIT = 5003;
    //管理所有存活的 Activity, 容器中的顺序仅仅是 Activity 的创建顺序, 并不能保证和 Activity 任务栈顺序一致
    private List<Activity> mActivityList;
    //当前在前台的 Activity
    private Activity mCurrentActivity;
    private boolean mIsAppForeground;
    private long mIsAppForegroundChangeTs;
    //提供给外部扩展 AppManager 的 onReceive 方法
//    private HandleListener mHandleListener;
//
//    public AppManager() {
//        EventBus.getDefault().register(this);
//    }
//
//
//    /**
//     * 通过 {@link EventBus#post(Object)} 事件, 远程遥控执行对应方法
//     * 可通过 {@link #setHandleListener(AppManager.HandleListener)}, 让外部可扩展新的事件
//     */
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onReceive(AppManager.MessageEvent messageEvent) {
//        Message message = messageEvent.msg;
//        switch (message.what) {
//            case START_ACTIVITY:
//                if (message.obj == null)
//                    break;
//                dispatchStart(message);
//                break;
//            case SHOW_SNACKBAR:
//                if (message.obj == null)
//                    break;
//                showSnackbar((String) message.obj, message.arg1 == 0 ? false : true);
//                break;
//            case KILL_ALL:
//                killAll();
//                break;
//            case APP_EXIT:
//                appExit();
//                break;
//            default:
//                MyLog.w(TAG, "The message.what not match");
//                break;
//        }
//        if (mHandleListener != null) {
//            mHandleListener.handleMessage(this, message);
//        }
//    }
//
//    private void dispatchStart(Message message) {
//        if (message.obj instanceof Intent)
//            startActivity((Intent) message.obj);
//        else if (message.obj instanceof Class)
//            startActivity((Class) message.obj);
//    }
//
//
//    public AppManager.HandleListener getHandleListener() {
//        return mHandleListener;
//    }
//
//    /**
//     * 提供给外部扩展 {@link AppManager} 的 {@link #onReceive} 方法(远程遥控 {@link AppManager} 的功能)
//     * 建议在 {@link ConfigModule#injectAppLifecycle(Context, List)} 中
//     * 通过 {@link AppLifecycles#onCreate(Application)} 在 App 初始化时,使用此方法传入自定义的 {@link AppManager.HandleListener}
//     *
//     * @param handleListener
//     */
//    public void setHandleListener(AppManager.HandleListener handleListener) {
//        this.mHandleListener = handleListener;
//    }
//
//    /**
//     * 通过此方法远程遥控 {@link AppManager} ,使 {@link #onReceive(AppManager.MessageEvent)} 执行对应方法
//     *
//     * @param msg
//     */
//    public static void post(Message msg) {
//        EventBus.getDefault().post(new AppManager.MessageEvent(msg));
//    }

    /**
     * 让在前台的 {@link Activity},使用 {@link Snackbar} 显示文本内容
     *
     * @param message
     * @param isLong
     */
    public void showSnackbar(String message, boolean isLong) {
        showSnackbar(getTopActivity(), message, isLong);
    }


    /**
     * 让在前台的 {@link Activity},使用 {@link Snackbar} 显示文本内容
     *
     * @param message
     * @param isLong
     */
    public void showSnackbar(Activity activity, String message, boolean isLong) {
        if (activity == null) {
            MyLog.w(TAG, "mCurrentActivity == null when showSnackbar(String,boolean)");
            return;
        }
        View view = activity.getWindow().getDecorView().findViewById(android.R.id.content);
        Snackbar.make(view, message, isLong ? Snackbar.LENGTH_LONG : Snackbar.LENGTH_SHORT).show();
    }

    /**
     * 让在栈顶的 {@link Activity} ,打开指定的 {@link Activity}
     *
     * @param intent
     */
    public void startActivity(Intent intent) {
        if (getTopActivity() == null) {
            MyLog.w(TAG, "mCurrentActivity == null when startActivity(Intent)");
            //如果没有前台的activity就使用new_task模式启动activity
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            U.app().startActivity(intent);
            return;
        }
        getTopActivity().startActivity(intent);
    }

    /**
     * 让在栈顶的 {@link Activity} ,打开指定的 {@link Activity}
     *
     * @param activityClass
     */
    public void startActivity(Class activityClass) {
        startActivity(new Intent(U.app(), activityClass));
    }

    /**
     * 释放资源
     */
    public void release() {
        EventBus.getDefault().unregister(this);
        mActivityList.clear();
//        mHandleListener = null;
        mActivityList = null;
        mCurrentActivity = null;
    }

    /**
     * 将在前台的 {@link Activity} 赋值给 {@code currentActivity}, 注意此方法是在 {@link Activity#onResume} 方法执行时将栈顶的 {@link Activity} 赋值给 {@code currentActivity}
     * 所以在栈顶的 {@link Activity} 执行 {@link Activity#onCreate} 方法时使用 {@link #getCurrentActivity()} 获取的就不是当前栈顶的 {@link Activity}, 可能是上一个 {@link Activity}
     * 如果在 App 启动第一个 {@link Activity} 执行 {@link Activity#onCreate} 方法时使用 {@link #getCurrentActivity()} 则会出现返回为 {@code null} 的情况
     * 想避免这种情况请使用 {@link #getTopActivity()}
     *
     * @param currentActivity
     */
    public void setCurrentActivity(Activity currentActivity) {
        this.mCurrentActivity = currentActivity;
    }

    /**
     * 获取在前台的 {@link Activity} (保证获取到的 {@link Activity} 正处于可见状态, 即未调用 {@link Activity#onStop()}), 获取的 {@link Activity} 存续时间
     * 是在 {@link Activity#onStop()} 之前, 所以如果当此 {@link Activity} 调用 {@link Activity#onStop()} 方法之后, 没有其他的 {@link Activity} 回到前台(用户返回桌面或者打开了其他 App 会出现此状况)
     * 这时调用 {@link #getCurrentActivity()} 有可能返回 {@code null}, 所以请注意使用场景和 {@link #getTopActivity()} 不一样
     * <p>
     * Example usage:
     * 使用场景比较适合, 只需要在可见状态的 {@link Activity} 上执行的操作
     * 如当后台 {@link Service} 执行某个任务时, 需要让前台 {@link Activity} ,做出某种响应操作或其他操作,如弹出 {@link Dialog}, 这时在 {@link Service} 中就可以使用 {@link #getCurrentActivity()}
     * 如果返回为 {@code null}, 说明没有前台 {@link Activity} (用户返回桌面或者打开了其他 App 会出现此状况), 则不做任何操作, 不为 {@code null}, 则弹出 {@link Dialog}
     *
     * @return
     */
    public Activity getCurrentActivity() {
        return mCurrentActivity != null ? mCurrentActivity : null;
    }

    /**
     * 获取最近启动的一个 {@link Activity}, 此方法不保证获取到的 {@link Activity} 正处于前台可见状态
     * 即使 App 进入后台或在这个 {@link Activity} 中打开一个之前已经存在的 {@link Activity}, 这时调用此方法
     * 还是会返回这个最近启动的 {@link Activity}, 因此基本不会出现 {@code null} 的情况
     * 比较适合大部分的使用场景, 如 startActivity
     * <p>
     * Tips: mActivityList 容器中的顺序仅仅是 Activity 的创建顺序, 并不能保证和 Activity 任务栈顺序一致
     *
     * @return
     */
    public Activity getTopActivity() {
        if (mActivityList == null) {
            MyLog.w(TAG, "mActivityList == null when getTopActivity()");
            return null;
        }
        return mActivityList.size() > 0 ? mActivityList.get(mActivityList.size() - 1) : null;
    }


    /**
     * 返回一个存储所有未销毁的 {@link Activity} 的集合
     *
     * @return
     */
    public List<Activity> getActivityList() {
        if (mActivityList == null) {
            mActivityList = new LinkedList<>();
        }
        return mActivityList;
    }


    /**
     * 添加 {@link Activity} 到集合
     */
    public void addActivity(Activity activity) {
        synchronized (ActivityUtils.class) {
            List<Activity> activities = getActivityList();
            if (!activities.contains(activity)) {
                activities.add(activity);
            }
        }
    }

    /**
     * 删除集合里的指定的 {@link Activity} 实例
     *
     * @param {@link Activity}
     */
    public void removeActivity(Activity activity) {
        if (mActivityList == null) {
            MyLog.w(TAG, "mActivityList == null when removeActivity(Activity)");
            return;
        }
        synchronized (this) {
            if (mActivityList.contains(activity)) {
                mActivityList.remove(activity);
            }
        }
        consumeOnActivityDestroyedListener(activity);
    }


    /**
     * 监听activity的destroy
     *
     * @param activity
     * @param listener
     */
    public void addOnActivityDestroyedListener(final Activity activity,
                                               final OnActivityDestroyedListener listener) {
        if (activity == null || listener == null) return;
        Set<OnActivityDestroyedListener> listeners;
        if (!mDestroyedListenerMap.containsKey(activity)) {
            listeners = new HashSet<>();
            mDestroyedListenerMap.put(activity, listeners);
        } else {
            listeners = mDestroyedListenerMap.get(activity);
            if (listeners.contains(listener)) return;
        }
        listeners.add(listener);
    }

    /**
     * 移除activity的监听
     *
     * @param activity
     */
    private void consumeOnActivityDestroyedListener(Activity activity) {
        Iterator<Map.Entry<Activity, Set<OnActivityDestroyedListener>>> iterator
                = mDestroyedListenerMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Activity, Set<OnActivityDestroyedListener>> entry = iterator.next();
            if (entry.getKey() == activity) {
                Set<OnActivityDestroyedListener> value = entry.getValue();
                for (OnActivityDestroyedListener listener : value) {
                    listener.onActivityDestroyed(activity);
                }
                iterator.remove();
            }
        }
    }


    /**
     * 由 ActivityLifecycle 来判断是否在前台
     *
     * @param isAppForeground
     */
    public void setAppForeground(boolean isAppForeground) {
        if (this.mIsAppForeground != isAppForeground) {
            MyLog.d(TAG, "发生前后台切换" + " isAppForeground=" + isAppForeground);
            this.mIsAppForeground = isAppForeground;
            mIsAppForegroundChangeTs = System.currentTimeMillis();
            EventBus.getDefault().post(new ForeOrBackgroundChange(mIsAppForeground));
        }

    }

    /**
     * 返回app 是否在前台
     */
    public boolean isAppForeground() {
        return this.mIsAppForeground;
    }

    public long getIsAppForegroundChangeTs() {
        return mIsAppForegroundChangeTs;
    }

    public Intent getLaunchIntentForPackage(String s) {
        Intent intent = U.app().getPackageManager().getLaunchIntentForPackage(s);
        return intent;
    }

    public boolean isHomeActivity(Activity activity) {
        if (activity != null && activity.getClass().getSimpleName().equals("HomeActivity")) {
            return true;
        }
        return false;
    }

    public Activity getHomeActivity() {
        for (Activity activity : getActivityList()) {
            if (isHomeActivity(activity)) {
                return activity;
            }
        }
        return null;
    }

    public boolean goHomeActivity() {
        boolean homeExist = false;
        for (Activity activity : getActivityList()) {
            if (isHomeActivity(activity)) {
                homeExist = true;
            } else {
                activity.finish();
            }
        }
        if (!homeExist) {

        }
        return homeExist;
    }

    public void safeGo(Intent intent) {
        if (U.app().getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).size() > 0) {
            U.app().startActivity(intent);
        }
    }

    /**
     * 是否在后台回调
     */
    public static class ForeOrBackgroundChange {
        public boolean foreground;

        public ForeOrBackgroundChange(boolean foreground) {
            this.foreground = foreground;
        }
    }

    public interface OnActivityDestroyedListener {
        void onActivityDestroyed(Activity activity);
    }
}
