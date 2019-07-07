package com.module.home.updateinfo.fragment

import android.os.Bundle
import android.view.View

import com.common.base.BaseFragment
import com.common.core.myinfo.MyUserInfoManager
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.common.view.titlebar.CommonTitleBar
import com.module.home.R
import com.zq.person.view.AgeTagView

class EditInfoAgeTagFragment : BaseFragment() {

    lateinit var mTitlebar: CommonTitleBar
    lateinit var mAgeTagView: AgeTagView

    override fun initView(): Int {
        return R.layout.edit_info_age_tag_fragment_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        mTitlebar = mRootView.findViewById<View>(R.id.titlebar) as CommonTitleBar
        mAgeTagView = mRootView.findViewById<View>(R.id.age_tag_view) as AgeTagView

        mTitlebar.leftTextView.setOnClickListener {
            U.getFragmentUtils().popFragment(this@EditInfoAgeTagFragment)
        }

        mTitlebar.rightTextView.setOnClickListener(object :DebounceViewClickListener(){
            override fun clickValid(v: View?) {
                var ageStage = mAgeTagView.getSelectTag()
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
        })

        if (MyUserInfoManager.getInstance().ageStage != 0) {
            mAgeTagView.setSelectTag(MyUserInfoManager.getInstance().ageStage)
        }
    }

    override fun useEventBus(): Boolean {
        return false
    }
}
