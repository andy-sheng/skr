package com.module.playways.party.room.activity

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.common.base.BaseActivity
import com.common.view.titlebar.CommonTitleBar
import com.common.view.viewpager.NestViewPager
import com.common.view.viewpager.SlidingTabLayout
import com.module.RouterConstants
import com.module.playways.R

@Route(path = RouterConstants.ACTIVITY_PARTY_SELECT_GAME)
class PartySelectGameActivity : BaseActivity() {
    lateinit var titlebar: CommonTitleBar
    lateinit var slidingTabLayout: SlidingTabLayout
    lateinit var viewPager: NestViewPager
    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.party_select_game_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        titlebar = findViewById(R.id.titlebar)
        slidingTabLayout = findViewById(R.id.sliding_tab_layout)
        viewPager = findViewById(R.id.viewPager)
    }
}