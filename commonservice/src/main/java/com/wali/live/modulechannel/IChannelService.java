package com.wali.live.modulechannel;

import com.alibaba.android.arouter.facade.template.IProvider;

/**
 * channel module 对外提供服务的接口
 */
public interface IChannelService extends IProvider {
    Object getDataFromChannel(int type,Object object);
}
