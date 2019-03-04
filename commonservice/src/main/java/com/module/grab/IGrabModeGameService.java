package com.module.grab;

import com.alibaba.android.arouter.facade.template.IProvider;

/**
 * channel module 对外提供服务的接口
 */
public interface IGrabModeGameService extends IProvider {
    Object getData(int type, Object object);
    void jump(int type, Object... object);
}
