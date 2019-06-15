package com.moudule.playways.beauty.fragment

import android.os.Bundle

import com.common.base.BaseFragment
import com.module.playways.R

class BeautyPreviewFragment : BaseFragment() {
    override fun initView(): Int {
        return R.layout.beauty_preview_fragment_layout
    }

    override fun initData(savedInstanceState: Bundle?) {

    }

    override fun useEventBus(): Boolean {
        return false
    }
}
