package com.common.core.view

import android.view.View
import com.common.view.DebounceViewClickListener

fun View.setDebounceViewClickListener(click: (view: View?) -> Unit) {
    this.setOnClickListener(object : DebounceViewClickListener() {
        override fun clickValid(v: View?) {
            click(v)
        }
    })
}