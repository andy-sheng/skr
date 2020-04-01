package com.module.playways.party.room.view

import android.content.Context
import android.os.Handler
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import com.common.log.MyLog
import com.common.view.ex.ExImageView
import com.module.playways.R
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder

class PartyGrapDiamondDialogView :ConstraintLayout{

    private val TAG = PartyGrapDiamondDialogView::class.java.simpleName

    private var mDialogPlus:DialogPlus? = null
    private var mUiHandler = Handler()

    private val closeDelay = 30000L

    private var mDiamondImage:ExImageView? = null
    private var mDiamondButton:ExImageView? = null

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
    }

    fun showBeginGrapView(){
        MyLog.d(TAG, "弹出开始抢宝箱弹框")

        background = null
        mDiamondImage?.setImageResource(R.drawable.party_grab_diamond_begin)
        mDiamondImage?.setOnClickListener {
            MyLog.d(TAG,"打开宝箱")
            showGrapedView()
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
        mUiHandler.postDelayed(delayRunnable, closeDelay)
    }

    private fun showGrapedView(){
        setBackgroundResource(R.drawable.party_diamond_dialog_bg)
        mDiamondImage?.setImageResource(R.drawable.party_grab_diamond_collect)
        mDiamondImage?.setOnClickListener (null)

        mDiamondImage?.setOnClickListener{
            MyLog.d(TAG,"领取宝箱")
            showCloseView()
        }

        //从上一个Dialog中移除当前View
        parent?.let { it as ViewGroup }?.removeAllViews()

        mDialogPlus?.dismiss()
        MyLog.d("弹出领取宝箱弹框")
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
        mUiHandler.postDelayed(delayRunnable, closeDelay)
    }

    private fun showCloseView(){
        setBackgroundResource(R.drawable.party_diamond_dialog_bg)
        mDiamondImage?.setImageResource(R.drawable.party_grab_diamond_known)
        mDiamondImage?.setOnClickListener (null)

        mDiamondImage?.setOnClickListener{
            MyLog.d(TAG,"关闭宝箱")
        }

        //从上一个Dialog中移除当前View
        parent?.let { it as ViewGroup }?.removeAllViews()

        MyLog.d("弹出关闭宝箱弹框")
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
        mUiHandler.postDelayed(delayRunnable, closeDelay)
    }



    private val delayRunnable = Runnable {
        MyLog.d(TAG, "关闭宝箱弹框")
        mDialogPlus?.dismiss()
    }

    fun destory(){
        mUiHandler.removeCallbacks(delayRunnable)
        mDialogPlus?.dismiss()
    }

}