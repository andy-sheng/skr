package com.module.home;

import android.content.Context;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.module.RouterConstants;
import com.module.home.event.AuthSuccessEvent;
import com.module.home.fragment.HalfRechargeFragment;
import com.module.home.fragment.PersonFragment3;

import org.greenrobot.eventbus.EventBus;

@Route(path = RouterConstants.SERVICE_HOME, name = "测试服务")
public class HomeServiceImpl implements IHomeService {
    public final static String TAG = "ChannelServiceImpl";

    /**
     * 主要返回的是只在 channel 自定义类型，注意在 commonservice 中增加接口，
     * 如是一个自定义view，增加自定义view需要的接口即可
     * 如果是一个实体类，可以简单的直接移动到 commonservice 相应的包下
     */
    @Override
    public Object getData(int type, Object object) {
        if (0 == type) {
            return PersonFragment3.class;
        } else if (1 == type) {
            return HomeActivity.class.getSimpleName();
        } else if (2 == type) {
            return HalfRechargeFragment.class;
        }

        return null;
    }

    @Override
    public void init(Context context) {

    }

    @Override
    public void authSuccess() {
        EventBus.getDefault().post(new AuthSuccessEvent());
    }
}
