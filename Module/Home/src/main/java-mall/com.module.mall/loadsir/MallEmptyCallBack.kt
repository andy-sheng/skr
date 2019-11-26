package com.module.mall.loadsir

import com.kingja.loadsir.callback.Callback
import com.module.home.R

class MallEmptyCallBack : Callback() {
    override fun onCreateView(): Int {
        return R.layout.mall_list_empty_layout
    }
}
