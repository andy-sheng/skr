package com.module.playways.songmanager.fragment

import android.os.Bundle

import com.common.base.BaseFragment
import com.module.playways.R

class DoubleSongManageFragment : BaseFragment() {
    override fun initView(): Int {
        return R.layout.double_song_manage_fragment_layout
    }

    override fun initData(savedInstanceState: Bundle?) {

    }

    override fun useEventBus(): Boolean {
        return false
    }
}
