package com.module.feeds.songmanage.activity

import android.os.Bundle

import com.common.base.BaseActivity
import com.module.feeds.R

/**
 * 歌曲搜索页面
 */
class FeedSongSearchActivity : BaseActivity() {
    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.feed_song_search_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {

    }

    override fun useEventBus(): Boolean {
        return false
    }
}
