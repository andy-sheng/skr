package com.component.notification

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.common.view.ex.ExConstraintLayout
import com.component.busilib.R

/**
 * 一场到底5星
 */
class GrabFullStarNotifyView(context: Context) : ExConstraintLayout(context) {

    val bgIv: ImageView
    val contentTv: TextView

    init {
        View.inflate(context, R.layout.full_star_notify_view_layout, this)
        bgIv = this.findViewById(R.id.bg_iv)
        contentTv = this.findViewById(R.id.content_tv)
        //setBackgroundResource(R.color.Green)
    }

    fun bindData(content:String?){
        contentTv.text = content
    }
}
