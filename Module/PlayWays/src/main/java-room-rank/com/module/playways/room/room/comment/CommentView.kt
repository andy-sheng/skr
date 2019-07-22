package com.module.playways.room.room.comment

import android.content.Context
import android.content.res.TypedArray
import android.os.Handler
import android.os.Message
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import android.widget.AbsListView
import android.widget.RelativeLayout

import com.common.core.account.UserAccountManager
import com.common.log.MyLog
import com.common.player.IPlayer
import com.common.player.IPlayerCallback
import com.common.player.MyMediaPlayer
import com.common.utils.U
import com.module.playways.grab.room.GrabRoomData
import com.module.playways.grab.room.event.GrabSwitchRoomEvent
import com.module.playways.grab.room.model.GrabRoundInfoModel
import com.module.playways.room.msg.event.AudioMsgEvent
import com.module.playways.room.msg.event.DynamicEmojiMsgEvent
import com.module.playways.room.room.comment.adapter.CommentAdapter
import com.module.playways.room.room.comment.listener.CommentViewItemListener
import com.module.playways.room.room.comment.model.CommentAudioModel
import com.module.playways.room.room.comment.model.CommentDynamicModel
import com.module.playways.room.room.comment.model.CommentModel
import com.module.playways.room.room.comment.model.CommentTextModel
import com.module.playways.room.room.event.PretendCommentMsgEvent
import com.module.playways.room.room.event.RankToVoiceTransformDataEvent
import com.module.playways.songmanager.event.MuteAllVoiceEvent
import com.module.playways.voice.activity.VoiceRoomActivity
import com.module.playways.R
import com.module.playways.room.msg.event.CommentMsgEvent
import com.module.playways.BaseRoomData
import com.zq.mediaengine.kit.ZqEngineKit

import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class CommentView : RelativeLayout {

    internal var mCommentRv: RecyclerView? = null

    internal var mGameType = 0

    internal var mLinearLayoutManager: LinearLayoutManager? = null

    internal var mCommentAdapter: CommentAdapter? = null

    internal var mCommentItemListener: CommentViewItemListener? = null

    internal var mMediaPlayer: IPlayer? = null

    internal var maxHeight = U.getDisplayUtils().dip2px(260f)

    var roomData: BaseRoomData<*>? = null
    private var mOnBottom = true
    private var mDraging = false
    private var mHasDataUpdate = false
    private val mLastSetCommentListTs: Long = 0

    internal var mUIHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                MSG_ENSURE_AUTO_SCROLL_BOTTOM -> setOnBottom("AUTO_SCROLL_BOTTOM", true)
            }
        }
    }

    internal var mOnScrollListener: RecyclerView.OnScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
            MyLog.d(TAG, "onScrollStateChangd,newState:$newState,mOnBottom:$mOnBottom")
            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                // 闲置状态
                mDraging = false
                // 停下来判断是否是最后一个,这里忽然有次不能到底了会有bug
                // 如果最后一个可见的元素==列表中最后一个元素，则认为到底了,
                val firstVisiblePosition = mLinearLayoutManager?.findFirstVisibleItemPosition()
                MyLog.d(TAG, "onScrollStateChangd firstVisiblePosition :$firstVisiblePosition")
                if (firstVisiblePosition == 0) {
                    setOnBottom("onScrollStateChanged", true)
                } else {
                    setOnBottom("onScrollStateChanged", false)
                }
            } else if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                // 手动拖着滑动
                mDraging = true
                mUIHandler.removeCallbacksAndMessages(null)
            } else {
                // 自动滑动
                mUIHandler.removeCallbacksAndMessages(null)
            }
        }

        override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
            //            changeAlpha();
        }
    }

    val comments: List<CommentModel>
        get() = mCommentAdapter?.dataList ?: ArrayList()

    constructor(context: Context) : super(context) {
        init(null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs)
    }

    fun setListener(listener: CommentViewItemListener) {
        this.mCommentItemListener = listener
    }

    private fun setOnBottom(from: String, onBottom: Boolean) {
        MyLog.d(TAG, "onBottom:" + this.mOnBottom + "-->" + onBottom + " from:" + from)
        if (!onBottom) {
            mUIHandler.sendEmptyMessageDelayed(MSG_ENSURE_AUTO_SCROLL_BOTTOM, 5000)
        } else {
            mUIHandler.removeCallbacksAndMessages(null)
        }
        if (this.mOnBottom != onBottom) {
            this.mOnBottom = onBottom
            if (mOnBottom) {
                if (mHasDataUpdate) {
                    mCommentAdapter?.notifyDataSetChanged()
                }
                mCommentRv?.smoothScrollToPosition(0)
            }
            // 不在底部不需要更新数据
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        if (this.layoutParams.height > maxHeight) {
            val layoutParams = this.layoutParams as RelativeLayout.LayoutParams
            layoutParams.topMargin = layoutParams.topMargin + (layoutParams.height - maxHeight)
            layoutParams.height = maxHeight
            setLayoutParams(layoutParams)
        }
    }

    private fun init(attrs: AttributeSet?) {
        if (attrs != null) {
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.gameType)
            mGameType = typedArray.getInt(R.styleable.gameType_type, 0)
            typedArray.recycle()
        }

        View.inflate(context, R.layout.comment_view_layout, this)
        // TODO: 2019/4/28 必须提前注册
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        mCommentRv = this.findViewById(R.id.comment_rv)
        mLinearLayoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, true)
        mLinearLayoutManager?.stackFromEnd = true
        mCommentRv?.layoutManager = mLinearLayoutManager

        mCommentAdapter = CommentAdapter(object : CommentAdapter.CommentAdapterListener {
            override fun clickAvatar(userId: Int) {
                if (userId != UserAccountManager.SYSTEM_ID) {
                    mCommentItemListener?.clickAvatar(userId)
                }
            }

            override fun clickAudio(isPlaying: Boolean, commentAudioModel: CommentAudioModel?): Boolean {
                MyLog.d(TAG, "clickAudio isPlaying=$isPlaying commentAudioModel=$commentAudioModel")
                if (commentAudioModel == null) {
                    return false
                }
                if (isPlaying) {
                    // 暂停播放
                    mMediaPlayer?.reset()
                    mCommentAdapter?.setCurrentPlayAudioModel(null)
                    EventBus.getDefault().post(MuteAllVoiceEvent(false))
                } else {
                    // 重新开始播放
                    if (ZqEngineKit.getInstance().params.isAnchor && !ZqEngineKit.getInstance().params.isLocalAudioStreamMute) {
                        // 是主播切开麦不能录音
                        U.getToastUtil().showShort("在麦上无法收听语音")
                        return false
                    }
                    // 直接在此处播放，有需要在放到外面去
                    mCommentAdapter?.setCurrentPlayAudioModel(commentAudioModel)
                    if (mMediaPlayer == null) {
                        mMediaPlayer = MyMediaPlayer()
                        mMediaPlayer?.setCallback(object : IPlayerCallback {
                            override fun onPrepared() {

                            }

                            override fun onCompletion() {
                                mCommentAdapter?.setCurrentPlayAudioModel(null)
                                EventBus.getDefault().post(MuteAllVoiceEvent(false))
                            }

                            override fun onSeekComplete() {

                            }

                            override fun onVideoSizeChanged(width: Int, height: Int) {

                            }

                            override fun onError(what: Int, extra: Int) {

                            }

                            override fun onInfo(what: Int, extra: Int) {

                            }
                        })
                    }
                    if (!TextUtils.isEmpty(commentAudioModel.localPath)) {
                        // 播放本地
                        mMediaPlayer?.startPlay(commentAudioModel.localPath)
                    } else {
                        // 播放url
                        mMediaPlayer?.startPlay(commentAudioModel.msgUrl)
                    }
                    EventBus.getDefault().post(MuteAllVoiceEvent(true))
                }
                return true
            }
        })
        mCommentAdapter?.setGameType(mGameType)
        mCommentRv?.adapter = mCommentAdapter
        mCommentRv?.addOnScrollListener(mOnScrollListener)

        if (context is VoiceRoomActivity) {
            val rankToVoiceTransformDataEvent = EventBus.getDefault().getStickyEvent(RankToVoiceTransformDataEvent::class.java)
            EventBus.getDefault().removeStickyEvent(RankToVoiceTransformDataEvent::class.java)
            if (rankToVoiceTransformDataEvent != null) {
                mCommentAdapter?.dataList = rankToVoiceTransformDataEvent.mCommentModelList
            }
            mCommentRv?.scrollToPosition(0)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mUIHandler.removeCallbacksAndMessages(null)
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
        mMediaPlayer?.stop()
        mMediaPlayer?.release()
    }

    fun tryStopPlay() {
        mMediaPlayer?.reset()
        mCommentAdapter?.setCurrentPlayAudioModel(null)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: CommentMsgEvent) {
        MyLog.d(TAG, "onEvent" + " CommentMsgEvent = " + event.text)
        if (event.type == CommentMsgEvent.MSG_TYPE_SEND) {
            setOnBottom("CommentMsgEvent", true)
        }
        val commentTextModel = CommentTextModel.parseFromEvent(event, roomData)
        processCommentModel(commentTextModel)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: AudioMsgEvent) {
        MyLog.d(TAG, "onEvent AudioMsgEvent=$event")
        if (event.type == AudioMsgEvent.MSG_TYPE_SEND) {
            setOnBottom("AudioMsgEvent", true)
        }
        val commentAudioModel = CommentAudioModel.parseFromEvent(event, roomData)
        processCommentModel(commentAudioModel)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: PretendCommentMsgEvent) {
        MyLog.d(TAG, "onEvent" + " PresenterEvent =" + event.mCommentModel)
        processCommentModel(event.mCommentModel)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: DynamicEmojiMsgEvent) {
        MyLog.d(TAG, "onEvent event=$event")
        if (event.type == DynamicEmojiMsgEvent.MSG_TYPE_SEND) {
            setOnBottom("DynamicEmojiMsgEvent", true)
        }
        // TODO: 2019/4/9 特殊图片表情
        val commentDynamicModel = CommentDynamicModel.parseFromEvent(event, roomData)
        processCommentModel(commentDynamicModel)
    }

    internal fun processCommentModel(commentModel: CommentModel) {
        mCommentAdapter?.addToHead(commentModel)
        if (!mOnBottom || mDraging) {
            mHasDataUpdate = true
            //            mHasMore++;
            //            mMoveToLastItemIv.setVisibility(VISIBLE);
            //            String s = mHasMore > 99 ? "99+" : String.valueOf(mHasMore);
            //            mMoveToLastItemIv.setText(getResources().getQuantityString(R.plurals.more_comment_text, mHasMore, s));
            //            if (mRoomChatMsgManager != null) {
            //                mRoomChatMsgManager.updateMaxSize(Integer.MAX_VALUE);
            //            }
        } else {
            // TODO: 2018/12/23 后期可优化，只更新某一部分位置信息
            mCommentAdapter?.notifyDataSetChanged()
            mCommentRv?.smoothScrollToPosition(0)
        }
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    fun onEvent(event: GrabSwitchRoomEvent) {
        tryStopPlay()
        mCommentAdapter?.dataList?.clear()
        mCommentAdapter?.notifyDataSetChanged()
        mOnBottom = true
        mDraging = false
        mHasDataUpdate = false
    }

    companion object {
        val TAG = "CommentView"

        val MSG_ENSURE_AUTO_SCROLL_BOTTOM = 1  //自动滚动到底部
    }
}
