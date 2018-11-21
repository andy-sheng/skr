package com.module.rankingmode;

import com.alibaba.android.arouter.facade.template.IProvider;

/**
 * channel module 对外提供服务的接口
 */
public interface IRankingModeService extends IProvider {
    Object getData(int type, Object object);
}
