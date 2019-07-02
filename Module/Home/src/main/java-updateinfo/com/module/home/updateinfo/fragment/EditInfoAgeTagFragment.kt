package com.module.home.updateinfo.fragment

import android.os.Bundle
import android.view.View

import com.common.base.BaseFragment
import com.common.core.myinfo.MyUserInfoManager
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

    var ageStage: Int = 0

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
            if (ageStage == 0) {
                U.getToastUtil().showShort("您当前选择的年龄段为空")
            } else if (ageStage == MyUserInfoManager.getInstance().ageStage) {
                U.getFragmentUtils().popFragment(this@EditInfoAgeTagFragment)
            } else {
                MyUserInfoManager.getInstance().updateInfo(MyUserInfoManager.newMyInfoUpdateParamsBuilder()
                        .setAgeStage(ageStage)
                        .build(), false, false, object : MyUserInfoManager.ServerCallback {
                    override fun onSucess() {
                        U.getToastUtil().showShort("年龄段更新成功")
                        U.getFragmentUtils().popFragment(this@EditInfoAgeTagFragment)
                    }

                    override fun onFail() {

                    }
                })
            }
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

        if (MyUserInfoManager.getInstance().ageStage != 0) {
            setSelectTag(MyUserInfoManager.getInstance().ageStage)
        }
    }

    private fun setSelectTag(ageTag: Int) {
        ageStage = ageTag
        when (ageTag) {
            1 -> {
                mPrimaryIv.isSelected = true
                mSeniorIv.isSelected = false
                mCollegeIv.isSelected = false
                mWorksIv.isSelected = false
            }
            2 -> {
                mPrimaryIv.isSelected = false
                mSeniorIv.isSelected = true
                mCollegeIv.isSelected = false
                mWorksIv.isSelected = false
            }
            3 -> {
                mPrimaryIv.isSelected = false
                mSeniorIv.isSelected = false
                mCollegeIv.isSelected = true
                mWorksIv.isSelected = false
            }
            4 -> {
                mPrimaryIv.isSelected = false
                mSeniorIv.isSelected = false
                mCollegeIv.isSelected = false
                mWorksIv.isSelected = true
            }
        }

    }

    override fun useEventBus(): Boolean {
        return false
    }
}
