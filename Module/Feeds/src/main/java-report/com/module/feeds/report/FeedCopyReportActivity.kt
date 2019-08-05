package com.module.feeds.report

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter

import com.common.base.BaseActivity
import com.common.base.BaseFragment
import com.common.base.FragmentDataListener
import com.common.utils.FragmentUtils
import com.common.utils.U
import com.module.RouterConstants
import com.module.feeds.R
import com.module.feeds.watch.model.FeedsWatchModel
import com.module.feeds.watch.view.FeedsMoreDialogView
import com.module.home.IHomeService

@Route(path = RouterConstants.ACTIVITY_FEEDS_COPY_REPORT)
class FeedCopyReportActivity : BaseActivity() {
    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.empty_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        val from = intent.getIntExtra("from", FeedsMoreDialogView.FROM_FEED_HOME) //举报来源
        val model = intent.getSerializableExtra("watchModel") as FeedsWatchModel
        val channelService = ARouter.getInstance().build(RouterConstants.SERVICE_HOME).navigation() as IHomeService
        val baseFragmentClass = channelService.getData(3, null) as Class<BaseFragment>
        U.getFragmentUtils().addFragment(
                FragmentUtils.newAddParamsBuilder(this, baseFragmentClass)
                        .setAddToBackStack(false)
                        .setHasAnimation(false)
                        .addDataBeforeAdd(0, 2)
                        .addDataBeforeAdd(1, model.user?.userID)
                        .addDataBeforeAdd(2, model.feedID)
                        .addDataBeforeAdd(3, model.song?.songID)
                        .setFragmentDataListener(object : FragmentDataListener {
                            override fun onFragmentResult(requestCode: Int, resultCode: Int, bundle: Bundle?, obj: Any?) {
                                //todo 拿到结果再反馈吧

                            }
                        })
                        .build())
    }

    override fun useEventBus(): Boolean {
        return false
    }
}
