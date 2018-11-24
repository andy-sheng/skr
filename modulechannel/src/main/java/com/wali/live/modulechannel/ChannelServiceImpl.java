package com.wali.live.modulechannel;

import android.content.Context;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.module.RouterConstants;
import com.common.log.MyLog;
import com.common.utils.U;
import com.wali.live.modulechannel.model.channellist.ChannelShowModel;

@Route(path = RouterConstants.SERVICE_CHANNEL, name = "测试服务")
public class ChannelServiceImpl implements IChannelService {
    public final static String TAG = "ChannelServiceImpl";

    /**
     * 主要返回的是只在 channel 自定义类型，注意在 commonservice 中增加接口，
     * 如是一个自定义view，增加自定义view需要的接口即可
     * 如果是一个实体类，可以简单的直接移动到 commonservice 相应的包下
     *
     */
    @Override
    public Object getDataFromChannel(int type, Object object) {
        MyLog.d(TAG,"getDataFromChannel" + " type=" + type + " object=" + object);
        U.getToastUtil().showShort("我是 channle module 收到 数据 type:"+type+" object:"+object);
        if(type==1){
            return new ChannelShowModel();
        }
        return null;
    }

    @Override
    public void init(Context context) {
        MyLog.d(TAG,"init" + " context=" + context);
    }
}
