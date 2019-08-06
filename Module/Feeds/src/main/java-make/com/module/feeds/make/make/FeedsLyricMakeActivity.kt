package com.module.feeds.make.make

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.WindowManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.common.base.BaseActivity
import com.common.view.titlebar.CommonTitleBar
import com.module.RouterConstants
import com.module.feeds.R
import com.module.feeds.make.FeedsMakeModel


@Route(path = RouterConstants.ACTIVITY_FEEDS_LYRIC_MAKE)
class FeedsLyricMakeActivity : BaseActivity() {

    var mFeedsMakeModel: FeedsMakeModel? = null

    lateinit var titleBar: CommonTitleBar
    lateinit var lyricRv: RecyclerView
    lateinit var lyricAdapter: FeedsLyricMakeAdapter

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.feeds_lyric_make_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        mFeedsMakeModel = sFeedsMakeModelHolder
        sFeedsMakeModelHolder = null
        titleBar = findViewById(R.id.title_bar)
        lyricRv = findViewById(R.id.lyric_rv)
        titleBar.leftImageButton.setOnClickListener { finish() }
        lyricRv.layoutManager = LinearLayoutManager(this)
        lyricAdapter = FeedsLyricMakeAdapter()
        lyricRv.adapter = lyricAdapter

        val list = ArrayList<LyricItem>()
        if (mFeedsMakeModel?.withBgm == true) {
            val size = mFeedsMakeModel?.songModel?.songTpl?.lrcTsReader?.lrcLineInfos?.size
            if (size != null && size > 0) {
                val map = mFeedsMakeModel?.songModel?.songTpl?.lrcTsReader?.lrcLineInfos
                map?.forEach {
                    val item = LyricItem(LyricItem.TYPE_NORMAL, it.value.lineLyrics)
                    item.startTs = it.value.startTime
                    item.endTs = it.value.endTime
                    list.add(item)
                }
            }
        } else {
            val lrcTxtStr = mFeedsMakeModel?.songModel?.songTpl?.lrcTxtStr
            if (!TextUtils.isEmpty(lrcTxtStr)) {
                lrcTxtStr?.split("\n")?.forEach {
                    val item = LyricItem(LyricItem.TYPE_NORMAL, it)
                    list.add(item)
                }
            }
        }

        list.add(0, LyricItem(LyricItem.TYPE_TITLE, mFeedsMakeModel?.songModel?.workName ?: ""))
        lyricAdapter.setData(list)
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

var sFeedsMakeModelHolder: FeedsMakeModel? = null

fun openLyricMakeActivity(mFeedsMakeModel: FeedsMakeModel? = null, activity: BaseActivity) {
    sFeedsMakeModelHolder = mFeedsMakeModel
    activity.startActivity(Intent(activity, FeedsLyricMakeActivity::class.java))
}