package com.common.core.view

import android.view.View
import com.common.view.AnimateClickListener
import com.common.view.DebounceViewClickListener

fun View.setDebounceViewClickListener(click: (view: View?) -> Unit) {
    this.setOnClickListener(object : DebounceViewClickListener() {
        override fun clickValid(view: View?) {
            click?.invoke(view)
        }
    })
}

fun View.setAnimateDebounceViewClickListener(click: (view: View?) -> Unit) {
    this.setOnClickListener(object : AnimateClickListener() {
        override fun click(view: View) {
            click?.invoke(view)
        }
    })
}