package com.module.feeds.detail.activity

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.common.base.BaseActivity
import com.common.utils.FragmentUtils
import com.common.utils.U
import com.module.RouterConstants
import com.module.feeds.R
import com.module.feeds.detail.manager.FeedSongPlayModeManager
import com.module.feeds.detail.fragment.FeedsDetailFragment

@Route(path = RouterConstants.ACTIVITY_FEEDS_DETAIL)
class FeedsDetailActivity : BaseActivity() {

    companion object {
        // 标记来源
        val FROM_HOME_PAGE = 1     //推荐、关注、个人中心
        val FROM_HOME_COLLECT = 2  //收藏
        val FROM_FEED_RANK = 3     //排行
        val FROM_SCHEME = 4 // scheme
        val FROM_COMMENT_LIKE = 5  //评论或赞
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 避免不停地进主页又从神曲进主页
        var num = 0
        for (i in U.getActivityUtils().activityList.size - 1 downTo 0) {
            val ac = U.getActivityUtils().activityList[i]
            if (ac is FeedsDetailActivity) {
                num++
                if (num >= 2) {
                    ac.finish()
                }
            }
        }
    }

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.feeds_detail_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        val feedID = intent.getIntExtra("feed_ID", -1)
        val from = intent.getIntExtra("from", FROM_HOME_PAGE)
        val playType = intent.getSerializableExtra("playType") as FeedSongPlayModeManager.PlayMode?
        U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(this@FeedsDetailActivity, FeedsDetailFragment::class.java)
                .setAddToBackStack(false)
                .setHasAnimation(false)
                .addDataBeforeAdd(0, feedID)
                .addDataBeforeAdd(1, from)
                .addDataBeforeAdd(2, playType)
                .build())

    }

    override fun resizeLayoutSelfWhenKeybordShow(): Boolean {
        return true
    }

    override fun useEventBus(): Boolean {
        return false
    }
}
