package com.component.person.view

import android.content.Context
import android.util.AttributeSet


class AgeTagView : CommonTagView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun getAgeTagList(): List<TagModel> {
        val list = ArrayList<TagModel>()
        list.add(TagModel(1, "05后"))
        list.add(TagModel(2, "00后"))
        list.add(TagModel(3, "95后"))
        list.add(TagModel(4, "90后"))
        list.add(TagModel(5, "80后"))
        list.add(TagModel(6, "80前"))
        return list
    }
}
