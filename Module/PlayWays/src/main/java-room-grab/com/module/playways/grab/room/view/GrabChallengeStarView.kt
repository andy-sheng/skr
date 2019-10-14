package com.module.playways.grab.room.view


import android.os.Handler
import android.os.Message
import android.view.View
import android.view.ViewStub
import android.widget.ImageView
import android.widget.TextView
import com.common.core.view.setDebounceViewClickListener

import com.common.view.ExViewStub
import com.module.playways.R

/**
 * 其他人主场景
 */
class GrabChallengeStarView(mViewStub: ViewStub?) : ExViewStub(mViewStub) {
    val TAG = "GrabChallengeStarView"

    var challengeBgIv: ImageView? = null
    var challengingTv: TextView? = null
    var challengStarCntTv: TextView? = null

    var clickListener:(()->Unit)? = null

    val handler = object :Handler(){
        override fun handleMessage(msg: Message?) {
            super.handleMessage(msg)
            if(msg?.what==MSG_HIDE){
                setVisibility(View.GONE)
            }
        }
    }

    val MSG_HIDE = 9
    override fun init(parentView: View) {
        challengeBgIv = parentView.findViewById(R.id.challenge_bg_iv)
        challengingTv = parentView.findViewById(R.id.challenging_tv)
        challengStarCntTv = parentView.findViewById(R.id.challeng_star_cnt_tv)
        parentView.setDebounceViewClickListener {
            clickListener?.invoke()
        }
    }

    override fun layoutDesc(): Int {
        return R.layout.grab_challenge_star_view_layout
    }

    fun bindData(cnt: Int, justShowInChallenge: Boolean) {
        if(justShowInChallenge){
            challengingTv?.visibility = View.VISIBLE
            challengStarCntTv?.visibility = View.GONE
        }else{
            challengingTv?.visibility = View.GONE
            challengStarCntTv?.visibility = View.VISIBLE
            challengStarCntTv?.setText("评价:${cnt}")
        }
        handler.removeMessages(MSG_HIDE)
        handler.sendEmptyMessageDelayed(MSG_HIDE,10000)
    }

    override fun onViewDetachedFromWindow(v: View) {
        super.onViewDetachedFromWindow(v)
        handler.removeCallbacksAndMessages(null)
    }
}
