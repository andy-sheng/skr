package com.module.playways.party.room.view

import android.os.Handler
import android.view.View
import android.view.ViewStub
import com.common.log.MyLog
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ControlType
import com.common.rxretrofit.RequestControl
import com.common.rxretrofit.subscribe
import com.common.utils.U
import com.common.view.ExViewStub
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.module.playways.BaseRoomData
import com.module.playways.R
import com.module.playways.party.room.PartyRoomServerApi
import com.module.playways.party.room.model.PBeginDiamondboxModel
import com.module.playways.party.room.model.PartyDiamondboxModel
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
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
    private val mPartyRoomServerApi = ApiManager.getInstance().createService(PartyRoomServerApi::class.java)
    private val mUiHandler = Handler()

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
        val beginMs = mPBeginDiamondbox?.beginTimeMs?:return
        val leftMs = beginMs - (System.currentTimeMillis() - BaseRoomData.shiftTsForRelay)
        val timeRefreshDelay = 200L
        val refreshCount = leftMs / timeRefreshDelay + 1  //防止提前打开抢宝箱页面，导致无法抢到，延迟一个 timeRefreshDelay

        if(refreshCount > 0){
            MyLog.e(TAG, "开始倒计时 $leftMs $refreshCount")
            countDownDisposable = Observable.intervalRange(0, refreshCount, 0, timeRefreshDelay, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe( {
                        mDiamondCountDown?.text = U.getDateTimeUtils().formatVideoTime((leftMs - it * timeRefreshDelay).takeIf { it > 0 }?:0L)
                    }, {
                        MyLog.e(TAG, it)
                    }, {
                        MyLog.e(TAG, "倒计时结束")
                        showDiamondDialog(partyDiamondbox)
                        setVisibility(View.GONE)
                    })
        }else{
            MyLog.d("倒计时已经结束了不需要再显示了")
            setVisibility(View.GONE)
        }

        val endMs = partyDiamondbox.pBeginDiamondbox?.endTimeMs?:return
        val delay = endMs - (System.currentTimeMillis() - BaseRoomData.shiftTsForRelay) + 3000

        mUiHandler.postDelayed({
            checkDiamondBoxResult(partyDiamondbox.roomID!!, partyDiamondbox.pBeginDiamondbox!!.diamondboxTag!!)
        }, delay)

    }

    /**
     * 延迟一定时间后，检查抢宝箱的结果
     */
    private fun checkDiamondBoxResult(roomID:Int, diamondboxTag:String) {
        MainScope().launch {
            val apiResult = subscribe(RequestControl("checkGrabDiamondBoxResult", ControlType.CancelThis)){
                mPartyRoomServerApi.checkGrabDiamondBoxResult(roomID, diamondboxTag)
            }

            MyLog.d("检查宝箱分发结果 ${apiResult.errno} ${apiResult.errmsg} ${apiResult.data}")
        }
    }

    private fun showDiamondDialog(partyDiamondbox: PartyDiamondboxModel){
        mDiamondCountDown?.let {
            mPartyGrapDiamondDialogView = mPartyGrapDiamondDialogView?:PartyGrapDiamondDialogView(it.context)
            mPartyGrapDiamondDialogView?.showBeginGrabView(partyDiamondbox)
        }
    }

    override fun onViewDetachedFromWindow(v: View) {
        super.onViewDetachedFromWindow(v)
        countDownDisposable?.dispose()
    }

    fun destroy(){
        mPartyGrapDiamondDialogView?.destroy()
        countDownDisposable?.dispose()
        countDownDisposable = null
    }

}