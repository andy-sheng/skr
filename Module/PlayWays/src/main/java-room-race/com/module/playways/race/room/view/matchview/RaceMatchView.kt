package com.module.playways.race.room.view.matchview

import android.content.Context
import android.os.Handler
import android.os.Message
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import com.common.utils.U
import com.module.playways.R


// 匹配中类似赌博机的效果
class RaceMatchView : ConstraintLayout {

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val leftView: RaceMatchItemView
    private val rightView: RaceMatchItemView
    private var dengView: ImageView? = null

    internal val MSG_START = 2
    internal val MSG_HIDE = 1

    internal var mIndex = 0

    internal var mMsgAnimationRes = arrayOf(R.drawable.race_match_view_deng1, R.drawable.race_match_view_deng2, R.drawable.race_match_view_deng3)

    internal var mUiHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if (msg.what == MSG_START) {
                dengView?.background = U.getDrawable(mMsgAnimationRes[mIndex++ % mMsgAnimationRes.size])
                this.sendEmptyMessageDelayed(MSG_START, 400)
            } else if (msg.what == MSG_HIDE) {
                this.removeMessages(MSG_START)
            }
        }
    }

    init {
        View.inflate(context, R.layout.race_match_view_layout, this)

        leftView = this.findViewById(R.id.left_view)
        rightView = this.findViewById(R.id.right_view)
        dengView = this.findViewById(R.id.deng_view)
    }

    fun bindData() {

    }

    //todo 停止可以直接滚到某个位置无动画来结束
    fun starAnimation() {
        leftView.setData(null, 0)
        rightView.setData(null, 0)
        mUiHandler.removeMessages(MSG_START)
        mUiHandler.sendEmptyMessage(MSG_START)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mUiHandler.removeCallbacks(null)
    }
}

