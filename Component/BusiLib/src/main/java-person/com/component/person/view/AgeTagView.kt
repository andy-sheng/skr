package com.component.person.view

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExImageView


class AgeTagView : ConstraintLayout {

    private var mPrimaryIv: ExImageView
    private var mSeniorIv: ExImageView
    private var mCollegeIv: ExImageView
    private var mWorksIv: ExImageView
    private val mPrimaryTv: TextView
    private val mSeniorTv: TextView
    private val mCollegeTv: TextView
    private val mWorksTv: TextView

    private var mListener: Listener? = null
    private var ageStage: Int = 0

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    init {
        View.inflate(context, com.component.busilib.R.layout.person_age_tag_view_layout, this)

        mPrimaryIv = findViewById(com.component.busilib.R.id.primary_iv)
        mSeniorIv = findViewById(com.component.busilib.R.id.senior_iv)
        mCollegeIv = findViewById(com.component.busilib.R.id.college_iv)
        mWorksIv = findViewById(com.component.busilib.R.id.works_iv)
        mPrimaryTv = findViewById(com.component.busilib.R.id.primary_tv)
        mSeniorTv = findViewById(com.component.busilib.R.id.senior_tv)
        mCollegeTv = findViewById(com.component.busilib.R.id.college_tv)
        mWorksTv = findViewById(com.component.busilib.R.id.works_tv)

        mPrimaryIv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                setSelectTag(1)
            }
        })

        mSeniorIv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                setSelectTag(2)
            }
        })

        mCollegeIv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                setSelectTag(3)
            }
        })

        mWorksIv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                setSelectTag(4)
            }
        })
    }

    fun setTextColor(color: Int) {
        mPrimaryTv.setTextColor(color)
        mSeniorTv.setTextColor(color)
        mCollegeTv.setTextColor(color)
        mWorksTv.setTextColor(color)
    }

    fun setSelectTag(ageTag: Int) {
        ageStage = ageTag
        mListener?.onSelectedAge(ageTag)
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

    fun getSelectTag(): Int {
        return ageStage
    }

    fun setListener(listener: Listener) {
        mListener = listener
    }

    public interface Listener {
        fun onSelectedAge(ageTag: Int)
    }
}
