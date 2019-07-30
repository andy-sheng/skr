package com.module.feeds.detail.view

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Message
import android.support.constraint.ConstraintLayout
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout

import com.common.emoji.EmotionKeyboard
import com.common.emoji.EmotionLayout
import com.common.emoji.LQREmotionKit
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.common.view.ex.NoLeakEditText
import com.module.feeds.R

class FeedsInputContainerView : RelativeLayout, EmotionKeyboard.BoardStatusListener {
    internal var mEmotionKeyboard: EmotionKeyboard? = null
    internal var mInputContainer: ConstraintLayout? = null
    protected var mEtContent: NoLeakEditText? = null
    internal var mPlaceHolderView: ViewGroup? = null
    protected var mSendMsgBtn: View? = null
    internal var mElEmotion: EmotionLayout? = null

    protected var mHasPretend = false

    var mSendCallBack: ((String) -> Unit)? = null

    protected var mUiHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if (msg.what == 100) {
                mHasPretend = true
            }
        }
    }

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    protected fun init() {
        View.inflate(context, R.layout.feeds_input_container_view_layout, this)
        initInputView()
    }

    fun setETHint(str: String) {
        mEtContent?.hint = str
    }

    /**
     * 输入面板相关view的初始化
     */
    protected fun initInputView() {

        LQREmotionKit.tryInit(U.app())
        mInputContainer = this.findViewById(R.id.et_container)
        mEtContent = this.findViewById<View>(R.id.etContent) as NoLeakEditText
        mPlaceHolderView = this.findViewById(R.id.place_holder_view)
        mElEmotion = this.findViewById<View>(R.id.elEmotion) as EmotionLayout
        mSendMsgBtn = this.findViewById(R.id.send_msg_btn)

        initEmotionKeyboard()

        mSendMsgBtn?.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                if (!TextUtils.isEmpty(mEtContent?.text.toString())) {
                    mHasPretend = false
                    mSendCallBack?.let {
                        it(mEtContent?.text.toString())
                    }
                }
                mEtContent?.setText("")
                hideSoftInput()
            }
        })
    }

    private fun initEmotionKeyboard() {
        mEmotionKeyboard = EmotionKeyboard.with(context as Activity)
        mEmotionKeyboard?.bindToPlaceHodlerView(mPlaceHolderView)
        mEmotionKeyboard?.bindToEditText(mEtContent)
        mEmotionKeyboard?.setEmotionLayout(mElEmotion)
        mEmotionKeyboard?.setBoardStatusListener(this)
    }

    override fun onBoradShow() {
        //        EventBus.getDefault().post(new InputBoardEvent(true));
        mInputContainer?.visibility = View.VISIBLE
    }

    override fun onBoradHide() {
        //        EventBus.getDefault().post(new InputBoardEvent(false));
        mInputContainer?.visibility = View.GONE
        mEtContent?.hint = ""
    }

    fun showSoftInput() {
        mEmotionKeyboard?.showSoftInput()
    }

    fun hideSoftInput() {
        mEmotionKeyboard?.hideSoftInput()
    }

    fun onBackPressed(): Boolean {
        if (mEmotionKeyboard!!.isEmotionShown) {
            mEmotionKeyboard?.hideEmotionLayout(false)
            return true
        }
        return false
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mEmotionKeyboard?.destroy()
        mUiHandler.removeCallbacksAndMessages(null)
    }
}
