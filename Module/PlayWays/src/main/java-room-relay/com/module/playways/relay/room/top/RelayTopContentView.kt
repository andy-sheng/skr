package com.module.playways.relay.room.top

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.common.core.view.setDebounceViewClickListener
import com.facebook.drawee.view.SimpleDraweeView
import com.module.playways.R

// 顶部头像栏
class RelayTopContentView : ConstraintLayout {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val arrowIv: ImageView
    private val leftAvatarSdv: SimpleDraweeView
    private val loveBg: ImageView
    private val loveStatusIv: ImageView
    private val rightAvatarSdv: SimpleDraweeView
    private val countTimeTv: TextView
    private val tipsIv: ImageView

    var listener: Listener? = null
    var mIsOpen = true

    init {
        View.inflate(context, R.layout.relay_top_content_view_layout, this)

        arrowIv = this.findViewById(R.id.arrow_iv)
        leftAvatarSdv = this.findViewById(R.id.left_avatar_sdv)
        loveBg = this.findViewById(R.id.love_bg)
        loveStatusIv = this.findViewById(R.id.love_status_iv)
        rightAvatarSdv = this.findViewById(R.id.right_avatar_sdv)
        countTimeTv = this.findViewById(R.id.count_time_tv)
        tipsIv = this.findViewById(R.id.tips_iv)

        loveBg.setDebounceViewClickListener {
            listener?.clickLove()
        }

        arrowIv.setDebounceViewClickListener {
            listener?.clickArrow(!mIsOpen)
        }
    }

    fun setArrowIcon(open: Boolean) {
        if (open) {
            // 展开状态
            mIsOpen = true
            arrowIv.setImageResource(R.drawable.race_expand_icon)
        } else {
            // 折叠状态
            mIsOpen = false
            arrowIv.setImageResource(R.drawable.race_shrink_icon)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
//        if (!EventBus.getDefault().isRegistered(this)) {
//            EventBus.getDefault().register(this)
//        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
//        if (EventBus.getDefault().isRegistered(this)) {
//            EventBus.getDefault().unregister(this)
//        }
    }

    interface Listener {
        fun clickArrow(open: Boolean)
        fun clickLove()
    }
}