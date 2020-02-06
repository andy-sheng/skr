package com.component.person.view

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import com.common.flowlayout.FlowLayout
import com.common.flowlayout.TagAdapter
import com.common.flowlayout.TagFlowLayout
import com.common.log.MyLog
import com.common.view.ex.ExTextView
import com.component.busilib.R
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

abstract class CommonTagView : ConstraintLayout {
    val TAG = "CommonTagView"
    private var tagAgeView: TagFlowLayout? = null
    var tagAgeAdapter: TagAdapter<TagModel>? = null

    private var type = 1 //默认是修改

    private var mListener: Listener? = null

    constructor(context: Context) : super(context) {
        initView(null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initView(attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initView(attrs)
    }

    fun initView(attrs: AttributeSet?) {
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.ageTagView)
            type = typedArray.getInt(R.styleable.ageTagView_ageType, 1)
            typedArray.recycle()
        }

        View.inflate(context, R.layout.person_age_tag_view_layout, this)

        tagAgeView = this.findViewById(R.id.tag_age_view)

        tagAgeAdapter = object : TagAdapter<TagModel>(ArrayList()) {
            override fun getView(parent: FlowLayout?, position: Int, t: TagModel?): View {
                MyLog.d(TAG, "getView parent = $parent, position = $position, t = $t")
                val tv = if (type == 1) {
                    LayoutInflater.from(parent?.context).inflate(R.layout.person_edit_age_tag_item_layout,
                            parent, false) as ExTextView
                } else {
                    LayoutInflater.from(parent?.context).inflate(R.layout.person_age_tag_item_layout,
                            parent, false) as ExTextView
                }
                tv.text = t?.ageTagDesc
                return tv
            }
        }
        tagAgeAdapter?.tagDatas?.clear()
        tagAgeAdapter?.setTagDatas(getAgeTagList())
        tagAgeView?.setMaxSelectCount(1)
        tagAgeView?.adapter = tagAgeAdapter

        tagAgeView?.setOnSelectListener {
            if (it.isNullOrEmpty()) {
                mListener?.onUnSelect()
            } else {
                it.forEach { index ->
                    mListener?.onSelectedAge(tagAgeAdapter?.tagDatas?.get(index)?.ageTag ?: 0)
                    return@forEach
                }
            }
        }
    }

    fun setSelectTag(ageTag: Int) {
        tagAgeAdapter?.tagDatas?.forEachIndexed { index, model ->
            if (ageTag == model.ageTag) {
                val set = HashSet<Int>()
                set.add(index)
                tagAgeAdapter?.setSelectedList(set)
                tagAgeAdapter?.notifyDataChanged()
                return@forEachIndexed
            }
        }
    }

    fun getSelectTag(): Int {
        tagAgeView?.selectedList?.forEach {
            return tagAgeAdapter?.tagDatas?.get(it)?.ageTag ?: 0
        }
        return 0
    }

    fun setListener(listener: Listener) {
        mListener = listener
    }

    public interface Listener {
        fun onSelectedAge(ageTag: Int)

        fun onUnSelect()
    }

    abstract fun getAgeTagList(): List<TagModel>

    class TagModel(var ageTag: Int, var ageTagDesc: String)
}