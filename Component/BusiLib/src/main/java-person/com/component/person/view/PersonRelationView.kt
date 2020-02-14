package com.component.person.view

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet

// 个人主页的关系
class PersonRelationView  : ConstraintLayout {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
}