package com.module.home.updateinfo.fragment

import android.os.Bundle
import android.view.View

import com.common.base.BaseFragment
import com.common.core.myinfo.MyUserInfoManager
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExTextView
import com.common.view.titlebar.CommonTitleBar
import com.module.home.R
import com.module.home.updateinfo.activity.EditAgeTagActivity
import com.zq.person.view.AgeTagView

class EditInfoAgeTagFragment : BaseFragment() {
    val FROM_HOME = 0
    val FROM_PERSON_INFO = 1
    lateinit var mTitlebar: CommonTitleBar
    lateinit var mAgeTagView: AgeTagView
    var mSaveBtn: ExTextView? = null
    var mFrom = FROM_PERSON_INFO
    //被选中的年龄
    var mAgeStage: Int? = 0
    var mActionRunnable:Runnable? = null

    override fun initView(): Int {
        return if (mFrom == FROM_HOME) R.layout.direct_edit_info_age_tag_fragment_layout else R.layout.edit_info_age_tag_fragment_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        mTitlebar = mRootView.findViewById<View>(R.id.titlebar) as CommonTitleBar
        mAgeTagView = mRootView.findViewById<View>(R.id.age_tag_view) as AgeTagView
        mSaveBtn = mRootView.findViewById(R.id.save_btn) ?: null

        mTitlebar.leftTextView.setOnClickListener {
            activity?.finish()
        }

        mTitlebar.rightTextView.setOnClickListener {
            save()
        }

        mSaveBtn?.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                save()
            }
        })

        if (MyUserInfoManager.getInstance().ageStage != 0) {
            mAgeTagView.setSelectTag(MyUserInfoManager.getInstance().ageStage)
        }

        if (mFrom == FROM_HOME) {
            mAgeTagView.setTextColor(U.getColor(R.color.white_trans_50))
        }
    }

    private fun save() {
        var ageStage = mAgeTagView.getSelectTag()
        if (ageStage == 0) {
            U.getToastUtil().showShort("您当前选择的年龄段为空")
        } else if (ageStage == MyUserInfoManager.getInstance().ageStage) {
            mAgeStage = ageStage
            mActionRunnable?.run()
            activity?.finish()
        } else {
            MyUserInfoManager.getInstance().updateInfo(MyUserInfoManager.newMyInfoUpdateParamsBuilder()
                    .setAgeStage(ageStage)
                    .build(), false, false, object : MyUserInfoManager.ServerCallback {
                override fun onSucess() {
                    U.getToastUtil().showShort("年龄段更新成功")
                    mAgeStage = ageStage
                    mActionRunnable?.run()
                    activity?.finish()
                }

                override fun onFail() {

                }
            })
        }
    }

    override fun destroy() {
        super.destroy()
    }

    override fun setData(type: Int, data: Any?) {
        if (type == 0) {
            mFrom = data as Int
        }else if(type == 1){
            mActionRunnable = data as Runnable
        }
    }

    override fun useEventBus(): Boolean {
        return false
    }
}
