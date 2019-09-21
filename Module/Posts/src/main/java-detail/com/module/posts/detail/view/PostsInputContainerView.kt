package com.module.posts.detail.view

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Message
import android.support.constraint.ConstraintLayout
import android.support.constraint.Group
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import com.common.core.view.setDebounceViewClickListener
import com.common.emoji.EmotionKeyboard
import com.common.emoji.EmotionLayout
import com.common.emoji.LQREmotionKit
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.common.view.ex.NoLeakEditText
import com.module.posts.R
import com.module.posts.detail.adapter.PostsReplayImgAdapter
import com.module.posts.detail.event.PostsCommentBoardEvent
import org.greenrobot.eventbus.EventBus

class PostsInputContainerView : RelativeLayout, EmotionKeyboard.BoardStatusListener {
    internal var mEmotionKeyboard: EmotionKeyboard? = null
    var mInputContainer: ConstraintLayout? = null
    protected var mEtContent: NoLeakEditText? = null
    internal var mPlaceHolderView: ViewGroup? = null
    protected var mSendMsgBtn: View? = null
    internal var mElEmotion: EmotionLayout? = null
    lateinit var jianpanIv: ExImageView
    lateinit var tupianIv: ExImageView
    lateinit var yuyinIv: ExImageView
    lateinit var recyclerView: RecyclerView
    lateinit var audioRecordArea: ExTextView
    lateinit var selectImgGroup: Group

    var postsReplayImgAdapter: PostsReplayImgAdapter? = null
    protected var mHasPretend = false
    protected var mForceHide = false
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
        View.inflate(context, R.layout.posts_input_container_view_layout, this)
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
        mSendMsgBtn = this.findViewById(R.id.send_msg_btn)
        mElEmotion = this.findViewById<View>(R.id.elEmotion) as EmotionLayout
        jianpanIv = this.findViewById(R.id.jianpan_iv)
        tupianIv = this.findViewById(R.id.tupian_iv)
        yuyinIv = this.findViewById(R.id.yuyin_iv)
        recyclerView = rootView.findViewById(R.id.recycler_view)
        audioRecordArea = rootView.findViewById(R.id.audio_record_area)
        selectImgGroup = rootView.findViewById(R.id.select_img_group)

        postsReplayImgAdapter = PostsReplayImgAdapter()
        recyclerView.adapter = postsReplayImgAdapter
        recyclerView.layoutManager = LinearLayoutManager(context).also {
            it.setOrientation(LinearLayoutManager.HORIZONTAL)
        }

        postsReplayImgAdapter?.dataList = mutableListOf("ssss", "iuioiiu", "ssss", "iuioiiu", "ssss", "iuioiiu")

        initEmotionKeyboard()

        jianpanIv.setDebounceViewClickListener {
            showKeyBoard()
        }

        tupianIv.setDebounceViewClickListener {
            showImageSelectView()
        }

        yuyinIv.setDebounceViewClickListener {
            showAudioRecordView()
        }

        mSendMsgBtn?.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                if (!TextUtils.isEmpty(mEtContent?.text.toString())) {
                    mHasPretend = false
                    mSendCallBack?.let {
                        it(mEtContent?.text.toString())
                    }
                    mEtContent?.setText("")
                    hideSoftInput()
                } else {
                    U.getToastUtil().showShort("发送内容不能为空")
                }
            }
        })

        mInputContainer?.setOnClickListener {
            hideSoftInput()
        }
    }

    private fun showKeyBoard() {
        jianpanIv.visibility = View.GONE
        tupianIv.visibility = View.VISIBLE
        yuyinIv.visibility = View.VISIBLE

        audioRecordArea.visibility = View.GONE
        selectImgGroup.visibility = View.GONE
        mEmotionKeyboard?.showSoftInput()
    }

    private fun showImageSelectView() {
        jianpanIv.visibility = View.VISIBLE
        tupianIv.visibility = View.GONE
        yuyinIv.visibility = View.VISIBLE

        selectImgGroup.visibility = View.VISIBLE
        audioRecordArea.visibility = View.GONE
    }

    private fun showAudioRecordView() {
        jianpanIv.visibility = View.VISIBLE
        tupianIv.visibility = View.VISIBLE
        yuyinIv.visibility = View.GONE

        selectImgGroup.visibility = View.GONE
        audioRecordArea.visibility = View.VISIBLE
        mEmotionKeyboard?.hideSoftInput()
        audioRecordArea.getLayoutParams().height = U.getDisplayUtils().dip2px(260f)
    }

    private fun initEmotionKeyboard() {
        mEmotionKeyboard = EmotionKeyboard.with(context as Activity)
        mEmotionKeyboard?.bindToPlaceHodlerView(mPlaceHolderView)
        mEmotionKeyboard?.bindToEditText(mEtContent)
        mEmotionKeyboard?.setEmotionLayout(mElEmotion)
        mEmotionKeyboard?.setBoardStatusListener(this)
    }

    override fun onBoradShow() {
        EventBus.getDefault().post(PostsCommentBoardEvent(true))
        mInputContainer?.visibility = View.VISIBLE
    }

    override fun onBoradHide() {
        if (jianpanIv.visibility == View.GONE || mForceHide) {
            //当前是键盘状态，需要收起键盘，reset
            EventBus.getDefault().post(PostsCommentBoardEvent(false))
            mInputContainer?.visibility = View.GONE
            mEtContent?.hint = ""
        }

        mForceHide = false
    }

    fun reset() {
        //图片，音频的View需要这里reset

    }

    fun showSoftInput() {
        showKeyBoard()
    }

    fun hideSoftInput() {
        mForceHide = true
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
