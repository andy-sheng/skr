//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit.utils;

import android.content.Context;

import java.lang.reflect.Method;

public class RongOperationPermissionUtils {
    public RongOperationPermissionUtils() {
    }

    public static boolean isMediaOperationPermit(Context context) {
        try {
            String clazzName = "io.io.rong.callkit.RongCallKit";
            Class<?> voipclazz = Class.forName(clazzName);
            if (voipclazz != null) {
                Method method = voipclazz.getMethod("isInVoipCall", Context.class);
                boolean isInVoipCall = (Boolean) method.invoke((Object) null, context);
                if (isInVoipCall) {
                    return false;
                }
            }
        } catch (Exception var5) {

        }

        return true;
    }
}
