package com.module.playways.party.room.fragment

import android.os.Bundle
import android.support.v7.widget.RecyclerView
import com.common.base.BaseFragment
import com.common.core.view.setDebounceViewClickListener
import com.common.utils.U
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.common.view.titlebar.CommonTitleBar
import com.module.playways.R

class AdministratorSelectFragment : BaseFragment() {
    lateinit var titlebar: CommonTitleBar
    lateinit var bgIv: ExImageView
    lateinit var recyclerView: RecyclerView

    override fun initView(): Int {
        return R.layout.host_change_fragment_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        titlebar = rootView.findViewById(R.id.titlebar)
        bgIv = rootView.findViewById(R.id.bg_iv)
        recyclerView = rootView.findViewById(R.id.recycler_view)

        titlebar.leftTextView.setDebounceViewClickListener { finish() }
    }

    override fun destroy() {
        super.destroy()
        U.getSoundUtils().release(TAG)
    }

    override fun useEventBus(): Boolean {
        return false
    }
}
