package com.morgoo.droidplugin.hook.handle;

import android.content.Context;
import android.os.Build;
import android.os.RemoteException;

import com.morgoo.droidplugin.hook.HookedMethodHandler;
import com.morgoo.droidplugin.pm.PluginManager;

import java.lang.reflect.Method;

class ReplaceCallingPackageHookedMethodHandler extends HookedMethodHandler {

    public ReplaceCallingPackageHookedMethodHandler(Context hostContext) {
        super(hostContext);
    }

    @Override
    protected boolean beforeInvoke(Object receiver, Method method, Object[] args) throws Throwable {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            if (args != null && args.length > 0) {
                for (int index = 0; index < args.length; index++) {
                    if (args[index] != null && (args[index] instanceof String)) {
                        String str = ((String) args[index]);
                        if (isPackagePlugin(str)) {
                            /**
                             * 这里发现是插件的包名都会替换成宿主的包名
                             * 因为 App 调用系统 Api 时，都会到 AppOpsService 那进行鉴权
                             * AppOpsService 会判断 uid 和 包名，如果不匹配会抛出 bad call 的 SecurityException
                             * 我们启动插件时 uid 还是 宿主，所以包名也得是宿主，才能通过系统的校验
                             * 因为 App 的权限是跟 uid 挂钩的，所以插件 需要的权限 只能在 宿主中申请
                             */
                            args[index] = mHostContext.getPackageName();
                        }
                    }
                }
            }
        }
        return super.beforeInvoke(receiver, method, args);
    }

    private static boolean isPackagePlugin(String packageName) throws RemoteException {
        return PluginManager.getInstance().isPluginPackage(packageName);
    }
}