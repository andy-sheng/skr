package com.module.feeds.make.make

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.WindowManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.common.base.BaseActivity
import com.common.view.titlebar.CommonTitleBar
import com.module.RouterConstants
import com.module.feeds.R


@Route(path = RouterConstants.ACTIVITY_FEEDS_LYRIC_MAKE)
class FeedsLyricMakeActivity : BaseActivity() {

    lateinit var titleBar: CommonTitleBar
    lateinit var lyricRv: RecyclerView
    lateinit var lyricAdapter: FeedsLyricMakeAdapter

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.feeds_lyric_make_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        titleBar = findViewById(R.id.title_bar)
        lyricRv = findViewById(R.id.lyric_rv)
        titleBar.leftImageButton.setOnClickListener { finish() }
        lyricRv.layoutManager = LinearLayoutManager(this)
        lyricAdapter = FeedsLyricMakeAdapter()
        lyricRv.adapter = lyricAdapter

    }

    override fun onResume() {
        super.onResume()
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onDestroy() {
        super.onDestroy()
        window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun canSlide(): Boolean {
        return false
    }

    override fun useEventBus(): Boolean {
        return false
    }
}

fun openLyricMakeActivity(challenge: Long?) {

}