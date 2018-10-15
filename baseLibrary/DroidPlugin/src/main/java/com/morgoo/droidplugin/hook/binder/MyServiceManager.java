/*
**        DroidPlugin Project
**
** Copyright(c) 2015 Andy Zhang <zhangyong232@gmail.com>
**
** This file is part of DroidPlugin.
**
** DroidPlugin is free software: you can redistribute it and/or
** modify it under the terms of the GNU Lesser General Public
** License as published by the Free Software Foundation, either
** version 3 of the License, or (at your option) any later version.
**
** DroidPlugin is distributed in the hope that it will be useful,
** but WITHOUT ANY WARRANTY; without even the implied warranty of
** MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
** Lesser General Public License for more details.
**
** You should have received a copy of the GNU Lesser General Public
** License along with DroidPlugin.  If not, see <http://www.gnu.org/licenses/lgpl.txt>
**
**/

package com.morgoo.droidplugin.hook.binder;

import android.os.IBinder;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Andy Zhang(zhangyong232@gmail.com) on 2015/3/2.
 */
public class MyServiceManager {

    /**
     *
     * 要拿到其他应用进程的Binder对象一般会使用ServiceConnection连接其他进程的Service拿到IBinder。
     * 然而系统的IBinder是用ServiceManager暴露给应用进程的。
     *
     *
     * ActivityThread 在 bindApplication() 时，会从ServiceManager 那边获得一个 Service Cache
     * 每次和某个Service进行通信时，都会检查 Cache 中是否有 这个对象，有的话就直接用，无需跟ServiceManager 再通信了
     * 这里存的就是这个Cache
     */
    private static Map<String, IBinder> mOriginServiceCache = new HashMap<String, IBinder>(1);

    /**
     * 上面 Cache 对应的 代理对象，因为要 hook 这些 binder 和 上层调用，所以必须把 ServiceCache 也替换成代理对象，
     * 每次调用都会走进 ServiceManagerCacheBinderHook 对象的 invoke 方法。
     */
    private static Map<String, IBinder> mProxiedServiceCache = new HashMap<String, IBinder>(1);

    private static Map<String, Object> mProxiedObjCache = new HashMap<String, Object>(1);

    static IBinder getOriginService(String serviceName) {
        return mOriginServiceCache.get(serviceName);
    }

    public static void addOriginService(String serviceName, IBinder service) {
        mOriginServiceCache.put(serviceName, service);
    }

    static  void addProxiedServiceCache(String serviceName, IBinder proxyService) {
        mProxiedServiceCache.put(serviceName, proxyService);
    }

    static Object getProxiedObj(String servicename) {
        return mProxiedObjCache.get(servicename);
    }

    static void addProxiedObj(String servicename, Object obj) {
        mProxiedObjCache.put(servicename, obj);
    }
}
