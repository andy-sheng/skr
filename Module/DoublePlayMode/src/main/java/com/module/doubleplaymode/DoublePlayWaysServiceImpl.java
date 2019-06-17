package com.module.doubleplaymode;

import android.content.Context;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.module.RouterConstants;
import com.module.playways.IDoublePlaywaysModeService;

@Route(path = RouterConstants.SERVICE_DOUBLE_PLAY, name = "测试服务")
public class DoublePlayWaysServiceImpl implements IDoublePlaywaysModeService {
    public final static String TAG = "DoublePlayWaysServiceImpl";

    @Override
    public Object getData(int type, Object object) {
        return null;
    }

    @Override
    public void init(Context context) {

    }
}
