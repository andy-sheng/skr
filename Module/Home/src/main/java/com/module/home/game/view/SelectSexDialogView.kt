package com.module.home.game.view

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExTextView
import com.module.home.R

class SelectSexDialogView(context: Context?, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : ConstraintLayout(context, attrs, defStyleAttr) {
    var mFindMaleIv: ImageView? = null
    var mFindFemaleIv: ImageView? = null
    var mDivider: View? = null
    var mMeMaleIv: ExTextView? = null
    var mMeFemaleIv: ExTextView? = null
    var mStartMatchTv: ExTextView? = null
    //是否想找男的
    var mIsFindMale: Boolean? = null
    //自己是否是男的
    var mMeIsMale: Boolean? = null

    var onClickMatch: ((mIsFindMale: Boolean?, mMeIsMale: Boolean?) -> Unit)? = null

    init {
        View.inflate(context, R.layout.select_sex_layout, this)
        mFindMaleIv = findViewById(R.id.find_male_iv);
        mFindFemaleIv = findViewById(R.id.find_female_iv);
        mDivider = findViewById(R.id.divider);
        mMeMaleIv = findViewById(R.id.me_male_iv);
        mMeFemaleIv = findViewById(R.id.me_female_iv);
        mStartMatchTv = findViewById(R.id.start_match_tv);

        setOnClickListener(mFindMaleIv)
        setOnClickListener(mFindFemaleIv)
        setOnClickListener(mMeMaleIv)
        setOnClickListener(mMeFemaleIv)
        setOnClickListener(mStartMatchTv)
        mStartMatchTv?.isEnabled = false
    }

    private fun setOnClickListener(view: View?) {
        view?.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                onClickView(v)
            }
        })
    }

    fun reset() {
        mFindMaleIv?.isSelected = false
        mFindFemaleIv?.isSelected = false
        mMeMaleIv?.isSelected = false
        mMeFemaleIv?.isSelected = false
        mStartMatchTv?.isEnabled = false
        mIsFindMale = null
        mMeIsMale = null
    }

    private fun onClickView(view: View?) {
        when (view?.id) {
            R.id.find_male_iv -> {
                mIsFindMale = true
                mFindMaleIv?.isSelected = true
                mFindFemaleIv?.isSelected = false
                checkEnableStartMatch()
            }
            R.id.find_female_iv -> {
                mIsFindMale = false
                mFindFemaleIv?.isSelected = true
                mFindMaleIv?.isSelected = false
                checkEnableStartMatch()
            }
            R.id.me_male_iv -> {
                mMeIsMale = true
                mMeMaleIv?.isSelected = true
                mMeFemaleIv?.isSelected = false
                checkEnableStartMatch()
            }
            R.id.me_female_iv -> {
                mMeIsMale = false
                mMeFemaleIv?.isSelected = true
                mMeMaleIv?.isSelected = false
                checkEnableStartMatch()
            }
            R.id.start_match_tv -> {
                onClickMatch?.invoke(mIsFindMale, mMeIsMale)
            }
        }
    }

    private fun checkEnableStartMatch() {
        if (mIsFindMale != null && mMeIsMale != null) {
            mStartMatchTv?.isEnabled = true
        }
    }
}