package com.module.msg.activity

import android.os.Bundle
import android.support.v7.widget.RecyclerView
import com.alibaba.android.arouter.facade.annotation.Route
import com.common.base.BaseActivity
import com.common.core.view.setDebounceViewClickListener
import com.common.view.titlebar.CommonTitleBar
import com.module.RouterConstants
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import io.rong.imkit.R

//特别关注页面
@Route(path = RouterConstants.ACTIVITY_SPECIAL_FOLLOW)
class SpecialFollowActivity : BaseActivity() {

    lateinit var titlebar: CommonTitleBar
    lateinit var refreshLayout: SmartRefreshLayout
    lateinit var contentRv: RecyclerView

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.special_follow_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        titlebar = findViewById(R.id.titlebar)
        refreshLayout = findViewById(R.id.refreshLayout)
        contentRv = findViewById(R.id.content_rv)

        titlebar.leftTextView.setDebounceViewClickListener {
            finish()
        }
    }

    override fun useEventBus(): Boolean {
        return false
    }
}