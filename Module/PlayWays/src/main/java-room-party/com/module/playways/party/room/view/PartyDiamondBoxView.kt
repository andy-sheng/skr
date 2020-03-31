package com.module.playways.party.room.view

import android.view.View
import android.view.ViewStub
import com.common.utils.U
import com.common.view.ExViewStub
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.module.playways.R
import com.zq.live.proto.Common.PBeginDiamondbox
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * 主题房宝箱
 */
class PartyDiamondBoxView(viewStub: ViewStub) : ExViewStub(viewStub) {

    private var mPBeginDiamondbox:PBeginDiamondbox? = null

    private var mDiamondBoxIcon:ExImageView? = null
    private var mDiamondSender:ExTextView? = null
    private var mDiamondCountDown:ExTextView? = null

    override fun init(parentView: View) {
        mDiamondBoxIcon = parentView.findViewById(R.id.diamond_icon)
        mDiamondSender = parentView.findViewById(R.id.diamond_sender)
        mDiamondCountDown = parentView.findViewById(R.id.diamond_countdown)
    }

    override fun layoutDesc(): Int {
        return R.layout.party_waiting_diamond_box_layout
    }

    fun bindData(pBeginDiamondbox: PBeginDiamondbox){
        mPBeginDiamondbox = pBeginDiamondbox

        mDiamondSender?.text = pBeginDiamondbox.user.userInfo.nickName


        Observable.intervalRange(0, 3 * 60, 0, 1,TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
//                    mDiamondCountDown.text = U.getDateTimeUtils().formatVideoTime(it)
                }

    }
}