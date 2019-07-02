package com.module.home.updateinfo.fragment

import android.os.Bundle
import android.view.View

import com.common.base.BaseFragment
import com.common.utils.U
import com.module.home.R
import kotlinx.android.synthetic.main.edit_info_age_tag_fragment_layout.*
import com.common.view.ex.ExImageView
import com.common.view.titlebar.CommonTitleBar


class EditInfoAgeTagFragment : BaseFragment() {

    lateinit var mTitlebar: CommonTitleBar
    lateinit var mPrimaryIv: ExImageView
    lateinit var mSeniorIv: ExImageView
    lateinit var mCollegeIv: ExImageView
    lateinit var mWorksIv: ExImageView

    override fun initView(): Int {
        return R.layout.edit_info_age_tag_fragment_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        mTitlebar = mRootView.findViewById<View>(R.id.titlebar) as CommonTitleBar
        mPrimaryIv = mRootView.findViewById<View>(R.id.primary_iv) as ExImageView
        mSeniorIv = mRootView.findViewById<View>(R.id.senior_iv) as ExImageView
        mCollegeIv = mRootView.findViewById<View>(R.id.college_iv) as ExImageView
        mWorksIv = mRootView.findViewById<View>(R.id.works_iv) as ExImageView

        mTitlebar.leftTextView.setOnClickListener {
            U.getFragmentUtils().popFragment(this@EditInfoAgeTagFragment)
        }

        mTitlebar.rightTextView.setOnClickListener {

        }

        mPrimaryIv.setOnClickListener {
            setSelectTag(1)
        }

        mSeniorIv.setOnClickListener {
            setSelectTag(2)
        }

        mCollegeIv.setOnClickListener {
            setSelectTag(3)
        }

        mWorksIv.setOnClickListener {
            setSelectTag(4)
        }

    }

    private fun setSelectTag(i: Int) {
        when (i) {
            1 -> {
                primary_iv.isSelected = true
                senior_iv.isSelected = false
                college_iv.isSelected = false
                works_iv.isSelected = false
            }
            2 -> {
                primary_iv.isSelected = false
                senior_iv.isSelected = true
                college_iv.isSelected = false
                works_iv.isSelected = false
            }
            3 -> {
                primary_iv.isSelected = false
                senior_iv.isSelected = false
                college_iv.isSelected = true
                works_iv.isSelected = false
            }
            4 -> {
                primary_iv.isSelected = false
                senior_iv.isSelected = false
                college_iv.isSelected = false
                works_iv.isSelected = true
            }
        }

    }

    override fun useEventBus(): Boolean {
        return false
    }
}
