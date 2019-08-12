package com.common.core.userinfo;

/**
 * 泛型类，主要用于 API 中功能的回调处理,需要查询数据库的
 *
 * @param <T> 声明一个泛型 T。
 */
public abstract class ResultCallback<T> {

    public ResultCallback() {

    }

    /**
     * 本地数据库，成功时回调。
     *
     * @param t 已声明的类型。
     */
    public abstract boolean onGetLocalDB(T t);

    /**
     * 服务器，成功时回调。
     *
     * @param t 已声明的类型。
     */
    public abstract boolean onGetServer(T t);

}
