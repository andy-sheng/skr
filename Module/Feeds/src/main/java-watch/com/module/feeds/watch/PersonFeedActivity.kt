package com.module.feeds.watch

import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import com.alibaba.android.arouter.facade.annotation.Route
import com.common.base.BaseActivity
import com.common.core.userinfo.model.UserInfoModel
import com.common.core.view.setDebounceViewClickListener
import com.common.log.MyLog
import com.module.RouterConstants
import com.module.feeds.R
import com.module.feeds.watch.watchview.PersonWatchView

@Route(path = RouterConstants.ACTIVITY_PERSON_FEED)
class PersonFeedActivity : BaseActivity() {

    lateinit var backIv: ImageView
    lateinit var divider: View
    lateinit var content: RelativeLayout

    var userInfoModel: UserInfoModel? = null
    var watchView: PersonWatchView? = null

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.person_feed_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        userInfoModel = intent.getSerializableExtra("userInfoModel") as UserInfoModel?
        if (userInfoModel == null) {
            MyLog.w(TAG, "PersonPostActivity userInfoModel = null")
            finish()
            return
        }

        backIv = findViewById(R.id.back_iv)
        divider = findViewById(R.id.divider)
        content = findViewById(R.id.content)

        backIv.setDebounceViewClickListener {
            finish()
        }

        if (watchView == null) {
            watchView = PersonWatchView(this, userInfoModel!!, null)
        }
        watchView?.layoutParams = ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        content.addView(watchView)

        watchView?.getFeeds(true)
    }

    override fun destroy() {
        super.destroy()
        watchView?.destroy()
    }

    override fun useEventBus(): Boolean {
        return false
    }

    override fun canSlide(): Boolean {
        return false
    }
}