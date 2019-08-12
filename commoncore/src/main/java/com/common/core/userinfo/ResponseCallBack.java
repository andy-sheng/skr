package com.common.core.userinfo;

/**
 * 泛型类，主要用于 API 中功能的回调处理。
 *
 * @param <T> 声明一个泛型 T。
 */
public abstract class ResponseCallBack<T> {


    public ResponseCallBack() {

    }

    /**
     * 服务器，成功时回调。
     *
     * @param t 已声明的类型。
     */
    public abstract void onServerSucess(T t);


    /**
     * 服务器，失败时回调。
     */
    public abstract void onServerFailed();

}
