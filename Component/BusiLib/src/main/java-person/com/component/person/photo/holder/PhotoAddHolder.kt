package com.component.person.photo.holder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView

import com.common.view.DebounceViewClickListener
import com.common.view.recyclerview.RecyclerOnItemClickListener
import com.component.busilib.R

class PhotoAddHolder(itemView: View, var mOnClickAddPhotoListener: (() -> Unit)?) : RecyclerView.ViewHolder(itemView) {

    init {
        itemView.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                mOnClickAddPhotoListener?.invoke()
            }
        })
    }
}
