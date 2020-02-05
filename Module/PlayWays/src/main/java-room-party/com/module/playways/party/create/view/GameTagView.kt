package com.module.playways.party.create.view

import android.content.Context
import android.util.AttributeSet
import com.component.person.view.CommonTagView


class GameTagView : CommonTagView {
    private var list: ArrayList<TagModel>? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun getAgeTagList(): List<TagModel> {
        if (list == null) {
            list = ArrayList()
        }

        return list!!
    }

    fun bindData(list: List<TagModel>) {
        this.list?.addAll(list)
        tagAgeAdapter?.notifyDataChanged()
    }

}
