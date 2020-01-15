package com.component.person.view

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.common.flowlayout.FlowLayout
import com.common.flowlayout.TagAdapter
import com.common.flowlayout.TagFlowLayout
import com.common.log.MyLog
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.component.busilib.R
import com.component.busilib.model.FeedTagModel


class AgeTagView : ConstraintLayout {

    private var tagAgeView: TagFlowLayout? = null
    private var tagAgeAdapter: TagAdapter<AgeTagModel>? = null
    private var tagDataList = ArrayList<AgeTagModel>()

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

        tagAgeAdapter = object : TagAdapter<AgeTagModel>(ArrayList()) {
            override fun getView(parent: FlowLayout?, position: Int, t: AgeTagModel?): View {
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
        tagDataList.clear()
        tagDataList.addAll(getAgeTagList())
        tagAgeAdapter?.setTagDatas(tagDataList)
        tagAgeView?.setMaxSelectCount(1)
        tagAgeView?.adapter = tagAgeAdapter

        tagAgeView?.setOnSelectListener {
            if (it.isNullOrEmpty()) {
                mListener?.onUnSelect()
            } else {
                it.forEach { index ->
                    mListener?.onSelectedAge(tagDataList[index].ageTag)
                    return@forEach
                }
            }
        }
    }

    fun setSelectTag(ageTag: Int) {
        tagDataList.forEachIndexed { index, model ->
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
            return tagDataList[it].ageTag
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

    private fun getAgeTagList(): List<AgeTagModel> {
        val list = ArrayList<AgeTagModel>()
        list.add(AgeTagModel(1, "05后"))
        list.add(AgeTagModel(2, "00后"))
        list.add(AgeTagModel(3, "95后"))
        list.add(AgeTagModel(4, "90后"))
        list.add(AgeTagModel(5, "80后"))
        list.add(AgeTagModel(6, "80前"))
        return list
    }

    inner class AgeTagModel(var ageTag: Int, var ageTagDesc: String)
}
