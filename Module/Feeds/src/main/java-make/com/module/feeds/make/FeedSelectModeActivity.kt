package com.module.feeds.make

import android.os.Bundle
import android.view.View
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.common.base.BaseActivity
import com.common.rxretrofit.ApiManager
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExImageView
import com.module.RouterConstants


@Route(path = RouterConstants.ACTIVITY_FEEDS_SELECT_MODE)
class FeedSelectModeActivity : BaseActivity() {
    lateinit var mUploadIv: ExImageView
    lateinit var mDabangIv: ExImageView
    lateinit var mDownIv: ExImageView
    override fun initView(savedInstanceState: Bundle?): Int {
        overridePendingTransition(com.module.feeds.R.anim.slide_in_bottom, com.module.feeds.R.anim.slide_out_bottom)
        return com.module.feeds.R.layout.feed_select_model_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        mUploadIv = findViewById(com.module.feeds.R.id.upload_iv)
        mDabangIv = findViewById(com.module.feeds.R.id.dabang_iv)
        mDownIv = findViewById(com.module.feeds.R.id.down_iv)

        mUploadIv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                ARouter.getInstance().build(RouterConstants.ACTIVITY_WEB)
                        .withString("url", ApiManager.getInstance().findRealUrlByChannel("https://app.inframe.mobi/feed/collection"))
                        .greenChannel().navigation()
                finish()
            }
        })

        mDownIv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                finish()
            }
        })

        mDabangIv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                ARouter.getInstance().build(RouterConstants.ACTIVITY_FEEDS_RANK)
                        .navigation()
                finish()
            }
        })
    }

    override fun useEventBus(): Boolean {
        return false
    }

    override fun canSlide(): Boolean {
        return false
    }

    override fun onBackPressed() {
        finish()
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(com.module.feeds.R.anim.slide_in_bottom, com.module.feeds.R.anim.slide_out_bottom)
    }
}