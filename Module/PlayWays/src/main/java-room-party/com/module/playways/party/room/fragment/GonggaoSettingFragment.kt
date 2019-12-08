package com.module.playways.party.room.fragment

import android.os.Bundle
import com.common.base.BaseFragment
import com.common.core.view.setDebounceViewClickListener
import com.common.utils.U
import com.common.view.ex.ExTextView
import com.common.view.ex.NoLeakEditText
import com.common.view.titlebar.CommonTitleBar
import com.module.playways.R

class GonggaoSettingFragment : BaseFragment() {
    lateinit var titlebar: CommonTitleBar
    lateinit var editText: NoLeakEditText
    lateinit var saveTv: ExTextView
    override fun initView(): Int {
        return R.layout.gonggao_setting_fragment_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        titlebar = rootView.findViewById(R.id.titlebar)
        editText = rootView.findViewById(R.id.edit_text)
        saveTv = rootView.findViewById(R.id.save_tv)

        titlebar.leftTextView.setDebounceViewClickListener { finish() }

        saveTv.setDebounceViewClickListener {

        }
    }

    override fun destroy() {
        super.destroy()
        U.getSoundUtils().release(TAG)
    }

    override fun useEventBus(): Boolean {
        return false
    }
}
