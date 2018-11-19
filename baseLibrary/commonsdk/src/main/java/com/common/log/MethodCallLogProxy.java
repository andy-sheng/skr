package com.common.log;

import android.text.TextUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;


/**
 * 为了打印出每个方法调用日志而使用的动态代理
 * 多用于回调，最多的是引擎的回调
 */
public class MethodCallLogProxy implements InvocationHandler {
    private Object obj;
    private String tag;

    private MethodCallLogProxy(Object obj, String tag) {
        this.obj = obj;
        this.tag = tag;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (!TextUtils.isEmpty(tag)) {
            StringBuilder sb = new StringBuilder();
            sb.append("method:").append(method.getName());
            for (int i = 0; i < args.length; i++) {
                sb.append(" ").append(args[i]);
            }
            MyLog.d(tag, sb.toString());
        }
        Object result = method.invoke(obj, args);
        return result;
    }

    public static <T> T attach(T obj,String tag){
        MethodCallLogProxy inter = new MethodCallLogProxy(obj,tag);
        //获取代理类实例sell
        T proxy = (T)(Proxy.newProxyInstance(obj.getClass().getClassLoader(), new Class[] {obj.getClass()}, inter));
        return proxy;
    }
}
