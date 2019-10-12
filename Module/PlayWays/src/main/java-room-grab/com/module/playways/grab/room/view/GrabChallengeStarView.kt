package com.module.playways.grab.room.view


import android.view.View
import android.view.ViewStub
import android.widget.ImageView
import android.widget.TextView

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


    override fun init(parentView: View) {
        challengeBgIv = parentView.findViewById(R.id.challenge_bg_iv)
        challengingTv = parentView.findViewById(R.id.challenging_tv)
        challengStarCntTv = parentView.findViewById(R.id.challeng_star_cnt_tv)
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
    }
}
