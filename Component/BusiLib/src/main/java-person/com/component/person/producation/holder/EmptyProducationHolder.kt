package com.component.person.producation.holder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView

import com.component.busilib.R

class EmptyProducationHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    internal var mEmptyImg: ImageView
    internal var mEmptyTxt: TextView

    init {
        mEmptyImg = itemView.findViewById<View>(R.id.empty_img) as ImageView
        mEmptyTxt = itemView.findViewById<View>(R.id.empty_txt) as TextView
    }

    fun bindData(isSelf: Boolean) {
        if (isSelf) {
            mEmptyTxt.text = "可以去练歌房录制作品哦～"
        } else {
            mEmptyTxt.text = "ta还没有作品哦～"
        }
    }
}
