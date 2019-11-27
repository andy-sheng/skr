package com.module.playways.songmanager.fragment

import android.os.Bundle
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.View
import com.common.base.BaseFragment
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExTextView
import com.common.view.titlebar.CommonTitleBar
import com.common.view.viewpager.SlidingTabLayout
import com.module.playways.R
import com.module.playways.mic.room.MicRoomData
import com.module.playways.relay.room.RelayRoomData
import com.module.playways.songmanager.SongManagerActivity

// 接唱点歌曲管理
class RelaySongManageFragment : BaseFragment() {

    lateinit var titlebar: CommonTitleBar
    lateinit var searchSongIv: ExTextView
    lateinit var tagTab: SlidingTabLayout
    lateinit var viewpager: ViewPager

    lateinit var mPagerAdapter: PagerAdapter

    private var mRoomData: RelayRoomData? = null

    override fun initView(): Int {
        return R.layout.relay_song_manage_fragment_layout
    }

    override fun setData(type: Int, data: Any?) {
        super.setData(type, data)
        if (type == 0) {
            mRoomData = data as RelayRoomData?
        }
    }

    override fun initData(savedInstanceState: Bundle?) {

        if (mRoomData == null) {
            if (activity != null) {
                activity!!.finish()
            }
            return
        }

        titlebar = rootView.findViewById(R.id.titlebar)
        searchSongIv = rootView.findViewById(R.id.search_song_iv)
        tagTab = rootView.findViewById(R.id.tag_tab)
        viewpager = rootView.findViewById(R.id.viewpager)

        titlebar.leftTextView.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                if (activity is SongManagerActivity) {
                    if (activity != null) {
                        activity!!.finish()
                    }
                } else {
                    finish()
                }
            }
        })
    }


    override fun useEventBus(): Boolean {
        return false
    }
}