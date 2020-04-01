package com.module.playways.party.room.view

import android.view.View
import android.view.ViewStub
import com.common.log.MyLog
import com.common.utils.U
import com.common.view.ExViewStub
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.module.playways.BaseRoomData
import com.module.playways.R
import com.module.playways.party.room.model.PBeginDiamondboxModel
import com.module.playways.party.room.model.PartyDiamondboxModel
import com.zq.live.proto.Common.PBeginDiamondbox
import com.zq.live.proto.broadcast.PartyDiamondbox
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit

/**
 * 主题房宝箱
 */
class PartyDiamondBoxView(viewStub: ViewStub) : ExViewStub(viewStub) {

    private val TAG = PartyDiamondBoxView::class.java.simpleName
    private var countDownDisposable:Disposable? = null

    private var mPBeginDiamondbox:PBeginDiamondboxModel? = null

    private var mDiamondBoxIcon:ExImageView? = null
    private var mDiamondSender:ExTextView? = null
    private var mDiamondCountDown:ExTextView? = null
    private var mPartyGrapDiamondDialogView:PartyGrapDiamondDialogView? = null

    override fun init(parentView: View) {
        mDiamondBoxIcon = parentView.findViewById(R.id.diamond_icon)
        mDiamondSender = parentView.findViewById(R.id.diamond_sender)
        mDiamondCountDown = parentView.findViewById(R.id.diamond_countdown)
    }

    override fun layoutDesc(): Int {
        return R.layout.party_waiting_diamond_box_layout
    }

    fun bindData(partyDiamondbox: PartyDiamondboxModel){
        mPBeginDiamondbox = partyDiamondbox.pBeginDiamondbox?:return

        mDiamondSender?.text = partyDiamondbox.pBeginDiamondbox?.user?.userInfo?.nickname

//        val count = ((partyDiamondbox.pBeginDiamondbox?.beginTimeMs?:0 - System.currentTimeMillis() + BaseRoomData.shiftTsForRelay) / 1000f).toLong()
        val count = 30L
        if(count > 0){
            MyLog.e(TAG, "开始倒计时 $count")
            countDownDisposable = Observable.intervalRange(0, count, 0, 1,TimeUnit.SECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe( {
                        mDiamondCountDown?.text = U.getDateTimeUtils().formatVideoTime(count - it)
                    }, {
                        MyLog.e(TAG, it)
                    }, {
                        MyLog.e(TAG, "倒计时结束")
                        showDiamondDialog()
                        setVisibility(View.GONE)
                    })
        }else{
            MyLog.d("倒计时已经结束了不需要再显示了")
            setVisibility(View.GONE)
        }

    }

    private fun showDiamondDialog(){

        mDiamondCountDown?.let {
            mPartyGrapDiamondDialogView = mPartyGrapDiamondDialogView?:PartyGrapDiamondDialogView(it.context)
            mPartyGrapDiamondDialogView?.showBeginGrapView()
        }

    }

    override fun onViewDetachedFromWindow(v: View) {
        super.onViewDetachedFromWindow(v)
        countDownDisposable?.dispose()
    }

    private fun destroy(){
        countDownDisposable?.dispose()
        countDownDisposable = null
    }
}