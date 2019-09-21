package com.module.posts.detail.view

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Message
import android.support.constraint.ConstraintLayout
import android.support.constraint.Group
import android.support.v4.app.FragmentActivity
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
import com.common.view.ex.NoLeakEditText
import com.imagebrowse.ImageBrowseView
import com.imagebrowse.big.BigImageBrowseFragment
import com.imagebrowse.big.DefaultImageBrowserLoader
import com.module.posts.R
import com.module.posts.detail.adapter.PostsReplayImgAdapter
import com.module.posts.detail.event.PostsCommentBoardEvent
import com.module.posts.view.PostsVoiceRecordView
import com.respicker.ResPicker
import com.respicker.activity.ResPickerActivity
import com.respicker.model.ImageItem
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
    lateinit var imgRecyclerView: RecyclerView
    lateinit var postsVoiceRecordView: PostsVoiceRecordView
    lateinit var selectImgGroup: Group

    lateinit var postsReplayImgAdapter: PostsReplayImgAdapter
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
        imgRecyclerView = rootView.findViewById(R.id.recycler_view)
        postsVoiceRecordView = PostsVoiceRecordView(rootView.findViewById(R.id.posts_voice_record_view_stub))

        selectImgGroup = rootView.findViewById(R.id.select_img_group)

        postsReplayImgAdapter = PostsReplayImgAdapter()
        imgRecyclerView.adapter = postsReplayImgAdapter
        imgRecyclerView.layoutManager = LinearLayoutManager(context).also {
            it.setOrientation(LinearLayoutManager.HORIZONTAL)
        }

        postsReplayImgAdapter?.delClickListener = { m, pos ->
            var index = 0
            for (v in postsReplayImgAdapter.dataList) {
                if (m!! == v) {
                    break
                }
                index++
            }
            //model.imgUploadMap.remove(m?.path)
            postsReplayImgAdapter.dataList.removeAt(index)
            postsReplayImgAdapter.notifyItemRemoved(index)
            if (postsReplayImgAdapter.dataList.isEmpty()) {
                imgRecyclerView.visibility = View.GONE
            }
        }
        postsReplayImgAdapter.imgClickListener = { _, pos ->

            BigImageBrowseFragment.open(true, context as FragmentActivity, object : DefaultImageBrowserLoader<ImageItem>() {
                override fun init() {

                }

                override fun load(imageBrowseView: ImageBrowseView, position: Int, item: ImageItem) {
                    imageBrowseView.load(item.path)
                }

                override fun getInitCurrentItemPostion(): Int {
                    return pos
                }

                override fun getInitList(): List<ImageItem>? {
                    return postsReplayImgAdapter.dataList
                }
            })
        }

        initEmotionKeyboard()

        jianpanIv.setDebounceViewClickListener {
            showKeyBoard()
        }

        tupianIv.setDebounceViewClickListener {
            goAddImagePage()
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
        postsVoiceRecordView.okClickListener = { path ->
            U.getToastUtil().showShort("本地路径为$path ")
        }
    }

    private fun goAddImagePage() {
//        if (model.recordVoicePath?.isNotEmpty() == true) {
//            //如果已经录入语音
//            tipsDialogView = TipsDialogView.Builder(this)
//                    .setMessageTip("上传图片将清空语音,是否继续")
//                    .setConfirmTip("继续")
//                    .setCancelTip("取消")
//                    .setCancelBtnClickListener(object : AnimateClickListener() {
//                        override fun click(view: View?) {
//                            tipsDialogView?.dismiss()
//                        }
//                    })
//                    .setConfirmBtnClickListener(object : AnimateClickListener() {
//                        override fun click(view: View?) {
//                            audioDelIv.performClick()
//                            tipsDialogView?.dismiss(false)
//                            goAddImagePage()
//                        }
//                    })
//                    .build()
//            tipsDialogView?.showByDialog()
//        } else {
            U.getKeyBoardUtils().hideSoftInputKeyBoard(context as Activity)
            ResPicker.getInstance().params = ResPicker.newParamsBuilder()
                    .setMultiMode(true)
                    .setShowCamera(true)
                    .setIncludeGif(true)
                    .setCrop(false)
                    .setSelectLimit(9)
                    .build()
            ResPickerActivity.open(context as Activity, ArrayList<ImageItem>(postsReplayImgAdapter?.dataList))
//        }
    }

    fun onSelectImgOk(selectedImageList: java.util.ArrayList<ImageItem>) {
        postsReplayImgAdapter.dataList.clear()
        postsReplayImgAdapter.dataList.addAll(selectedImageList)
        postsReplayImgAdapter.notifyDataSetChanged()
        if (postsReplayImgAdapter.dataList.isNotEmpty()) {
            showImageSelectView()
        }
    }
    
    private fun showKeyBoard() {
        jianpanIv.visibility = View.GONE
        tupianIv.visibility = View.VISIBLE
        yuyinIv.visibility = View.VISIBLE

        postsVoiceRecordView.setVisibility(View.GONE)
        selectImgGroup.visibility = View.GONE
        mEmotionKeyboard?.showSoftInput()
    }

    private fun showImageSelectView() {
        jianpanIv.visibility = View.VISIBLE
        tupianIv.visibility = View.GONE
        yuyinIv.visibility = View.VISIBLE

        selectImgGroup.visibility = View.VISIBLE
        postsVoiceRecordView.setVisibility(View.GONE)
    }

    private fun showAudioRecordView() {
        jianpanIv.visibility = View.VISIBLE
        tupianIv.visibility = View.VISIBLE
        yuyinIv.visibility = View.GONE

        selectImgGroup.visibility = View.GONE
        postsVoiceRecordView.setVisibility(View.VISIBLE)
        mEmotionKeyboard?.hideSoftInput()
        postsVoiceRecordView.realView?.getLayoutParams()?.height = U.getDisplayUtils().dip2px(260f)
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
            reset()
        }

        mForceHide = false
    }

    fun reset() {
        //图片，音频的View需要这里reset
        postsReplayImgAdapter?.dataList = mutableListOf()
    }

    fun showSoftInput(type: SHOW_TYPE) {
        if (type == SHOW_TYPE.KEY_BOARD) {
            showKeyBoard()
        } else if (type == SHOW_TYPE.IMG) {
            showImageSelectView()
            mEmotionKeyboard?.showSoftInput()
            reset()
        } else if (type == SHOW_TYPE.AUDIO) {
            showAudioRecordView()
            mInputContainer?.visibility = View.VISIBLE
            reset()
        }
    }

    fun hideSoftInput() {
        mForceHide = true
        mEmotionKeyboard?.hideSoftInput()
        reset()
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

    enum class SHOW_TYPE {
        KEY_BOARD, IMG, AUDIO
    }
}
