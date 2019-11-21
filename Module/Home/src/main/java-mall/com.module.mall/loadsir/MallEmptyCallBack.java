package com.module.mall.loadsir;

import com.kingja.loadsir.callback.Callback;
import com.module.home.R;

public class MallEmptyCallBack extends Callback {
    @Override
    protected int onCreateView() {
        return R.layout.mall_list_empty_layout;
    }
}
