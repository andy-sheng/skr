
package com.base.utils.toast;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.widget.Toast;

import com.base.global.GlobalData;
import com.base.log.MyLog;


/**
 * @author linjinbin
 * @description Toast工具类，根据resId或者String提示
 */
public class ToastUtils {
    private static Toast sToast;

    private static Handler sMainHanlder = new Handler(Looper.getMainLooper());

    public static void showToast(int resId) {
        if (null == GlobalData.app()) {
            return;
        }
        final String tips = GlobalData.app().getString(resId);
        showToast(tips);
    }

    public static void showToast(int resId, Object... formatArgs) {
        if (null == GlobalData.app()) {
            return;
        }
        final String tips = GlobalData.app().getString(resId, formatArgs);
        showToast(tips);
    }

    public static void showToast(final String tips) {
        sMainHanlder.post(new Runnable() {
            @Override
            public void run() {
                try {
                    Toast.makeText(GlobalData.app(), tips, Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void showToast(final String tips, final int gravity) {
        sMainHanlder.post(new Runnable() {
            @Override
            public void run() {
                try {
                    Toast toast = Toast.makeText(GlobalData.app(), tips, Toast.LENGTH_SHORT);
                    toast.setGravity(gravity, 0, 0);
                    toast.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 根据resourceId提示
     *
     * @param activity
     * @param resId
     */
    public static void showToast(final Context activity, final int resId) {
        if (null != activity) {
            sMainHanlder.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        Toast.makeText(activity, activity.getString(resId), Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }

    }

    public static void showLongToast(final Context activity, final int resId) {
        if (null != activity) {
            sMainHanlder.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        Toast.makeText(activity, activity.getString(resId), Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    public static void showLongToast(final Context activity, final String string) {
        if (null != activity) {
            sMainHanlder.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        Toast.makeText(activity, string, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    /**
     * 根据字符串提示
     *
     * @param activity
     * @param tips
     */
    public static void showToast(final Context activity, final String tips) {
        if (null != activity) {
            sMainHanlder.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        Toast.makeText(activity, tips, Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        MyLog.v(e.getMessage());
                    }
                }
            });
        }
    }

    //通话toast自定义高度
    public static void showCallToast(final Context activity, final int resId) {
        if (null != activity) {
            showCallToast(activity, activity.getString(resId));
        }

    }

    public static void showCallToast(final Context activity, final String tips) {
        if (null != activity) {
            sMainHanlder.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        Toast toast = Toast.makeText(activity, tips, Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.BOTTOM, 0, 360);
                        toast.show();
                    } catch (Exception e) {
                        MyLog.v(e.getMessage());
                    }
                }
            });
        }

    }

    /**
     * 只显示当前的toast
     *
     * @param activity 上下文环境
     * @param resId    提示字的资源id
     * @param len      时间长短
     */
    public static void showWithDrawToast(final Context activity, final int resId,
                                         final int len) {
        if (null != activity) {
            sMainHanlder.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (sToast == null) {
                            sToast = new Toast(activity);
                            sToast = Toast.makeText(activity, resId, len);
                        }
                        sToast.setDuration(len);
                        sToast.setText(resId);
                        sToast.show();
                    } catch (Exception e) {
                        MyLog.v(e.getMessage());
                    }
                }
            });
        }
    }

}
