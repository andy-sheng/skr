package com.module.feeds.detail.activity

import android.content.Intent
import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.common.base.BaseActivity
import com.common.player.MyMediaPlayer
import com.common.utils.FragmentUtils
import com.common.utils.U
import com.module.feeds.watch.model.FeedsWatchModel
import com.module.RouterConstants
import com.module.feeds.R
import com.module.feeds.detail.fragment.FeedsDetailFragment

@Route(path = RouterConstants.ACTIVITY_FEEDS_DETAIL)
class FeedsDetailActivity : BaseActivity() {

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.feeds_detail_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        val feedsWatchModel = intent.getSerializableExtra("feed_model")
        if (feedsWatchModel == null) {
            finish()
            return
        }
        var player = player?: MyMediaPlayer()
        FeedsDetailActivity.player = null
        U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(this@FeedsDetailActivity, FeedsDetailFragment::class.java)
                .setAddToBackStack(false)
                .setHasAnimation(false)
                .addDataBeforeAdd(0, feedsWatchModel as FeedsWatchModel)
                //.addDataBeforeAdd(1,player)
                .build())

    }

    override fun resizeLayoutSelfWhenKeybordShow(): Boolean {
        return true
    }

    override fun useEventBus(): Boolean {
        return false
    }

    companion object{
        var player:MyMediaPlayer?=null
    }
}

fun startFeedsDetailActivity(activity: BaseActivity,player:MyMediaPlayer?){
    val intent = Intent(activity,FeedsDetailActivity::class.java)
    FeedsDetailActivity.player = player
    activity.startActivity(intent)
}