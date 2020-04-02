package com.module.playways.party.room.view

import android.content.Context
import android.os.Handler
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import com.alibaba.fastjson.JSON
import com.common.log.MyLog
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ControlType
import com.common.rxretrofit.RequestControl
import com.common.rxretrofit.subscribe
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.common.view.ex.ExView
import com.module.playways.BaseRoomData
import com.module.playways.R
import com.module.playways.party.room.PartyRoomServerApi
import com.module.playways.party.room.model.PartyDiamondboxModel
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.RequestBody

class PartyGrapDiamondDialogView :ConstraintLayout{

    private val TAG = PartyGrapDiamondDialogView::class.java.simpleName

    private var mDialogPlus:DialogPlus? = null
    private var mUiHandler = Handler()

    private val closeDelay = 5000L

    private var mDiamondImage:ExImageView? = null
    private var mDiamondButton:ExImageView? = null
    private var mDiamondResultTitleTv:ExTextView? = null
    private var mDiamondResultTv:ExTextView? = null
    private var mDiamondBg: ExView? = null


    private val mPartyRoomServerApi = ApiManager.getInstance().createService(PartyRoomServerApi::class.java)

    constructor(context: Context?) : super(context){
        init()
    }
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs){
        init()
    }
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr){
        init()
    }

    fun init(){
        View.inflate(context, R.layout.party_grap_diamond_dialog_layout, this)

        mDiamondImage = findViewById(R.id.grab_diamond_img)
        mDiamondButton = findViewById(R.id.grab_diamond_btn_img)
        mDiamondBg = findViewById(R.id.grab_diamond_bg)
    }

    fun showBeginGrabView(partyDiamondbox: PartyDiamondboxModel){
        MyLog.d(TAG, "弹出开始抢宝箱弹框")
        val endMs = partyDiamondbox.pBeginDiamondbox!!.endTimeMs!!

        mDiamondBg?.background = null
        mDiamondImage?.setImageResource(R.drawable.party_grab_diamond_begin)

        mDiamondButton?.visibility = View.GONE
        mDiamondResultTitleTv?.visibility = View.GONE
        mDiamondResultTv?.visibility = View.GONE

        mDiamondImage?.setOnClickListener {
            MyLog.d(TAG,"打开宝箱")

            MainScope().launch {
                val map = mutableMapOf("roomID" to partyDiamondbox.roomID, "diamondboxTag" to partyDiamondbox.pBeginDiamondbox!!.diamondboxTag)
                val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))

                val apiResult = subscribe(RequestControl("grabDiamondBox", ControlType.CancelThis)){
                    mPartyRoomServerApi.grabDiamondBox(body)
                }

                if(apiResult.errno == 0){
                    val redDiamondCount = apiResult.data.getIntValue("dqCnt")
                    showSuccessfulView(redDiamondCount)
                }else{
                    MyLog.e(TAG, "${apiResult.errno} ${apiResult.errmsg}")
                    showFailedView()
                }
            }

        }

        mDiamondButton?.setOnClickListener(null)

        //从上一个Dialog中移除当前View
        parent?.let { it as ViewGroup }?.removeAllViews()

        mDialogPlus?.dismiss()
        mDialogPlus = DialogPlus.newDialog(context)
                .setContentHolder(ViewHolder(this))
                .setGravity(Gravity.CENTER)
                .setContentBackgroundResource(com.component.busilib.R.color.transparent)
                .setOverlayBackgroundResource(com.component.busilib.R.color.black_trans_80)
                .setExpanded(false)
                .setCancelable(false)
                .create()
        mDialogPlus?.show()

        mUiHandler.removeCallbacks(delayRunnable)
        mUiHandler.postDelayed(delayRunnable, endMs - (System.currentTimeMillis() - BaseRoomData.shiftTsForRelay))
    }

    private fun showSuccessfulView(diamondCout:Int){
        mDiamondBg?.setBackgroundResource(R.drawable.party_diamond_dialog_bg)
        mDiamondImage?.setImageResource(R.drawable.party_diamond_box_full)
        mDiamondButton?.setImageResource(R.drawable.party_grab_diamond_collect)

        mDiamondButton?.visibility = View.VISIBLE
        mDiamondResultTitleTv?.visibility = View.VISIBLE
        mDiamondResultTv?.visibility = View.VISIBLE

        mDiamondResultTitleTv?.text = "恭喜你"
        mDiamondResultTv?.text = "抢到${diamondCout}红钻"

        mDiamondImage?.setOnClickListener (null)

        mDiamondButton?.setOnClickListener{
            MyLog.d(TAG,"领取宝箱")
            mDialogPlus?.dismiss()
        }

        //从上一个Dialog中移除当前View
        parent?.let { it as ViewGroup }?.removeAllViews()

        mUiHandler.removeCallbacks(delayRunnable)
        mUiHandler.postDelayed(delayRunnable, closeDelay)
    }

    private fun showFailedView(){
        mDiamondBg?.setBackgroundResource(R.drawable.party_diamond_dialog_bg)
        mDiamondImage?.setImageResource(R.drawable.party_diamond_box_empty)
        mDiamondButton?.setImageResource(R.drawable.party_grab_diamond_known)

        mDiamondButton?.visibility = View.VISIBLE
        mDiamondResultTitleTv?.visibility = View.VISIBLE
        mDiamondResultTv?.visibility = View.VISIBLE

        mDiamondResultTitleTv?.text = "很遗憾"
        mDiamondResultTv?.text = "别气馁，下次再试试"

        mDiamondImage?.setOnClickListener (null)

        mDiamondButton?.setOnClickListener{
            MyLog.d(TAG,"关闭宝箱")
            mDialogPlus?.dismiss()
        }

        mUiHandler.removeCallbacks(delayRunnable)
        mUiHandler.postDelayed(delayRunnable, closeDelay)
    }



    private val delayRunnable = Runnable {
        MyLog.d(TAG, "关闭宝箱弹框")
        mDialogPlus?.dismiss()
    }

    fun destroy(){
        mUiHandler.removeCallbacks(delayRunnable)
        mDialogPlus?.dismiss()
    }

}