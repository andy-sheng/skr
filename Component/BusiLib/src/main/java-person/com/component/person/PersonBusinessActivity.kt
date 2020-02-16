package com.component.person

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.common.base.BaseActivity
import com.component.busilib.R
import com.module.RouterConstants

// 名片页面 不知道这帮人怎么想的
@Route(path = RouterConstants.ACTIVITY_PERSON_BUSINESS)
class PersonBusinessActivity : BaseActivity() {
    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.person_business_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
    }

    override fun useEventBus(): Boolean {
        return false
    }
}