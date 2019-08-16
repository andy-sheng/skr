package com.module.feeds.make

import android.os.Bundle
import android.view.View
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.common.base.BaseActivity
import com.common.rxretrofit.ApiManager
import com.common.statistics.StatisticsAdapter
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExImageView
import com.module.RouterConstants
import com.module.feeds.R


@Route(path = RouterConstants.ACTIVITY_FEEDS_SELECT_MODE)
class FeedSelectModeActivity : BaseActivity() {
    lateinit var mUploadIv: ExImageView
    lateinit var mDabangIv: ExImageView
    lateinit var mDownIv: ExImageView
    lateinit var kuaichuanIv:ExImageView
    lateinit var gaibianIv:ExImageView

    override fun initView(savedInstanceState: Bundle?): Int {
        overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_bottom)
        return R.layout.feed_select_model_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        mUploadIv = findViewById(R.id.upload_iv)
        mDabangIv = findViewById(R.id.dabang_iv)
        mDownIv = findViewById(R.id.down_iv)
        kuaichuanIv = findViewById(R.id.kuaichuan_iv)
        gaibianIv = findViewById(R.id.gaibian_iv)

        mUploadIv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                StatisticsAdapter.recordCountEvent("music_publish", "upload", null)
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
                StatisticsAdapter.recordCountEvent("music_publish", "challenge", null)
                ARouter.getInstance().build(RouterConstants.ACTIVITY_FEEDS_RANK)
                        .navigation()
                finish()
            }
        })

        kuaichuanIv.setOnClickListener(object : DebounceViewClickListener(){
            override fun clickValid(v: View?) {
                StatisticsAdapter.recordCountEvent("music_publish", "sing", null)
                ARouter.getInstance().build(RouterConstants.ACTIVITY_FEEDS_SONG_MANAGE)
                        .withInt("from", FROM_QUICK_SING)
                        .navigation()
                finish()
            }
        })

        gaibianIv.setOnClickListener(object : DebounceViewClickListener(){
            override fun clickValid(v: View?) {
                StatisticsAdapter.recordCountEvent("music_publish", "change", null)
                ARouter.getInstance().build(RouterConstants.ACTIVITY_FEEDS_SONG_MANAGE)
                        .withInt("from", FROM_CHANGE_SING)
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