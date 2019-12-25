package com.component.person.view

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExImageView
import com.component.busilib.R


class AgeTagView : ConstraintLayout {

    private val age05Iv: ExImageView
    private val age05Tv: TextView
    private val age00Iv: ExImageView
    private val age00Tv: TextView
    private val age95Iv: ExImageView
    private val age95Tv: TextView
    private val age90Iv: ExImageView
    private val age90Tv: TextView
    private val age80Iv: ExImageView
    private val age80Tv: TextView
    private val age70Iv: ExImageView
    private val age70Tv: TextView

    private var mListener: Listener? = null
    private var ageStage: Int = 0

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    init {
        View.inflate(context, R.layout.person_age_tag_view_layout, this)

        age05Iv = this.findViewById(R.id.age_05_iv)
        age05Tv = this.findViewById(R.id.age_05_tv)
        age00Iv = this.findViewById(R.id.age_00_iv)
        age00Tv = this.findViewById(R.id.age_00_tv)
        age95Iv = this.findViewById(R.id.age_95_iv)
        age95Tv = this.findViewById(R.id.age_95_tv)
        age90Iv = this.findViewById(R.id.age_90_iv)
        age90Tv = this.findViewById(R.id.age_90_tv)
        age80Iv = this.findViewById(R.id.age_80_iv)
        age80Tv = this.findViewById(R.id.age_80_tv)
        age70Iv = this.findViewById(R.id.age_70_iv)
        age70Tv = this.findViewById(R.id.age_70_tv)

        age05Iv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                setSelectTag(1)
            }
        })

        age00Iv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                setSelectTag(2)
            }
        })

        age95Iv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                setSelectTag(3)
            }
        })

        age90Iv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                setSelectTag(4)
            }
        })

        age80Iv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                setSelectTag(5)
            }
        })

        age70Iv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                setSelectTag(6)
            }
        })
    }

    fun setTextColor(color: Int) {
        age05Tv.setTextColor(color)
        age00Tv.setTextColor(color)
        age95Tv.setTextColor(color)
        age90Tv.setTextColor(color)
        age80Tv.setTextColor(color)
        age70Tv.setTextColor(color)
    }

    fun setSelectTag(ageTag: Int) {
        ageStage = ageTag
        age05Iv.isSelected = false
        age00Iv.isSelected = false
        age95Iv.isSelected = false
        age90Iv.isSelected = false
        age80Iv.isSelected = false
        age70Iv.isSelected = false
        mListener?.onSelectedAge(ageTag)
        when (ageTag) {
            1 -> {
                age05Iv.isSelected = true
            }
            2 -> {
                age00Iv.isSelected = true
            }
            3 -> {
                age95Iv.isSelected = true
            }
            4 -> {
                age90Iv.isSelected = true
            }
            5 -> {
                age80Iv.isSelected = true
            }
            6 -> {
                age70Iv.isSelected = true
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
