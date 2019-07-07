package com.zq.person.view

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import com.common.view.ex.ExImageView
import com.component.busilib.R

class AgeTagView : ConstraintLayout {

    private var mPrimaryIv: ExImageView
    private var mSeniorIv: ExImageView
    private var mCollegeIv: ExImageView
    private var mWorksIv: ExImageView

    private var ageStage: Int = 0

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    init {
        View.inflate(context, R.layout.person_age_tag_view_layout, this)

        mPrimaryIv = findViewById(R.id.primary_iv)
        mSeniorIv = findViewById(R.id.senior_iv)
        mCollegeIv = findViewById(R.id.college_iv)
        mWorksIv = findViewById(R.id.works_iv)

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

    fun setSelectTag(ageTag: Int) {
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

    fun getSelectTag(): Int {
        return ageStage
    }

}
