package com.module.playways.battle.songlist

import android.os.Bundle
import android.support.v4.view.ViewPager
import android.view.View
import android.widget.ImageView
import com.alibaba.android.arouter.facade.annotation.Route
import com.common.base.BaseActivity
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExImageView
import com.common.view.titlebar.CommonTitleBar
import com.common.view.viewpager.NestViewPager
import com.common.view.viewpager.SlidingTabLayout
import com.module.RouterConstants
import com.module.playways.R
import com.module.playways.battle.songlist.view.BattleRankView

@Route(path = RouterConstants.ACTIVITY_BATTLE_RANK)
class BattleRankActivity : BaseActivity() {

    lateinit var title: CommonTitleBar
    lateinit var selfArea: ExImageView
    lateinit var tagTab: SlidingTabLayout
    lateinit var viewpager: NestViewPager
    lateinit var ivBack: ImageView

    var battleRankViews = HashMap<Int,BattleRankView>()

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.battle_rank_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        title = findViewById(R.id.title)
        selfArea = findViewById(R.id.self_area)
        tagTab = findViewById(R.id.tag_tab)
        viewpager = findViewById(R.id.viewpager)
        ivBack = findViewById(R.id.iv_back)

        ivBack.setOnClickListener(object : DebounceViewClickListener(){
            override fun clickValid(v: View?) {
                finish()
            }
        })

        getBattleRankTags()
    }

    private fun getBattleRankTags() {
    }

    override fun useEventBus(): Boolean {
        return false
    }
}