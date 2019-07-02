package com.zq.dialog

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import com.common.core.myinfo.MyUserInfoManager
import com.common.utils.U

import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.component.busilib.R
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder

class AgeStageDialogView : ConstraintLayout {

    val lastShowTime: String = "SP_AGE_STAGE_DIALOG_SHOW_TIME"

    internal var mDialogPlus: DialogPlus? = null

    lateinit var mPrimaryIv: ExImageView
    lateinit var mSeniorIv: ExImageView
    lateinit var mCollegeIv: ExImageView
    lateinit var mWorksIv: ExImageView
    lateinit var mJumpTv: ExTextView
    lateinit var mSaveTv: ExTextView

    var ageStage: Int = 0

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        View.inflate(context, R.layout.age_stage_dialog_view, this)

        mPrimaryIv = findViewById(R.id.primary_iv)
        mSeniorIv = findViewById(R.id.senior_iv)
        mCollegeIv = findViewById(R.id.college_iv)
        mWorksIv = findViewById(R.id.works_iv)
        mJumpTv = findViewById(R.id.jump_tv)
        mSaveTv = findViewById(R.id.save_tv)

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

        mJumpTv.setOnClickListener {
            dismiss()
        }

        mSaveTv.setOnClickListener {
            if (ageStage == 0) {
                U.getToastUtil().showShort("您当前选择的年龄段为空")
            } else if (ageStage == MyUserInfoManager.getInstance().ageStage) {
                dismiss()
            } else {
                MyUserInfoManager.getInstance().updateInfo(MyUserInfoManager.newMyInfoUpdateParamsBuilder()
                        .setAgeStage(ageStage)
                        .build(), false, false, object : MyUserInfoManager.ServerCallback {
                    override fun onSucess() {
                        U.getToastUtil().showShort("年龄段更新成功")
                        dismiss()
                    }

                    override fun onFail() {

                    }
                })
            }
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

    @JvmOverloads
    fun showByDialog(canCancel: Boolean = true) {
        if ((System.currentTimeMillis() - U.getPreferenceUtils().getSettingLong(lastShowTime, 0)) <= 24 * 60 * 60 * 1000) {
            // 距离上次展示不够一天
            return
        }

        if (mDialogPlus != null) {
            mDialogPlus!!.dismiss(false)
        }
        U.getPreferenceUtils().setSettingLong(lastShowTime, System.currentTimeMillis())
        mDialogPlus = DialogPlus.newDialog(context)
                .setContentHolder(ViewHolder(this))
                .setGravity(Gravity.BOTTOM)
                .setMargin(U.getDisplayUtils().dip2px(10f), -1, U.getDisplayUtils().dip2px(10f), U.getDisplayUtils().dip2px(10f))
                .setContentBackgroundResource(R.color.transparent)
                .setOverlayBackgroundResource(R.color.black_trans_80)
                .setExpanded(false)
                .setCancelable(canCancel)
                .create()
        mDialogPlus!!.show()
    }

    fun dismiss() {
        if (mDialogPlus != null) {
            mDialogPlus!!.dismiss()
        }
    }

    fun dismiss(isAnimation: Boolean) {
        if (mDialogPlus != null) {
            mDialogPlus!!.dismiss(isAnimation)
        }
    }
}
