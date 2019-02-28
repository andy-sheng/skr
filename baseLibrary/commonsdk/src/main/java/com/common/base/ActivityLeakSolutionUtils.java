package com.common.base;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.common.log.MyLog;

import java.lang.reflect.Field;

public class ActivityLeakSolutionUtils {

    public final static String TAG = "ActivityLeakSolutionUtils";

    /**
     * 修复部分华为修改rom，其InputMethodManager会持有一个叫mLastSrvView的view，最终外部持有activity
     * 其它机型可能有"mCurRootView", "mServedView", "mNextServedView"的内存泄漏
     *
     * @param destContext
     */
    public static void fixInputMethodManagerLeak(Context destContext) {
        if (destContext != null) {
            InputMethodManager imm = (InputMethodManager) destContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                String[] arr = new String[]{"mCurRootView", "mServedView", "mNextServedView", "mLastSrvView"};
                Field field = null;
                Object obj_get = null;

                for (int i = 0; i < arr.length; i++) {
                    String param = arr[i];

                    try {
                        field = imm.getClass().getDeclaredField(param);
                        if (!field.isAccessible()) {
                            field.setAccessible(true);
                        }
                        obj_get = field.get(imm);
                        if (obj_get != null && obj_get instanceof View) {
                            View view = (View) obj_get;
                            if (view.getContext() != destContext) {
                                MyLog.d(TAG, "fixInputMethodManagerLeak break, context is not suitable");
                                break;
                            }

                            field.set(imm, null);
                        }
                    } catch (Throwable t) {
                        MyLog.d(TAG, "fixInputMethodManagerLeak, can't leak view");
                    }
                }
            }
        }
    }
}
