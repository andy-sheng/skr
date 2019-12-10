package com.module.playways.room.room.view

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.PopupWindow
import android.widget.RelativeLayout

import com.common.core.myinfo.MyUserInfoManager
import com.common.core.view.setDebounceViewClickListener
import com.common.log.MyLog
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.module.ModuleServiceManager
import com.module.common.ICallback
import com.module.msg.CustomMsgType
import com.module.msg.IMsgService
import com.module.playways.BaseRoomData
import com.module.playways.R
import com.module.playways.RoomDataUtils
import com.module.playways.grab.room.GrabRoomData
import com.module.playways.grab.room.dynamicmsg.DynamicMsgView
import com.module.playways.grab.room.voicemsg.VoiceRecordTextView
import com.module.playways.room.msg.BasePushInfo
import com.module.playways.room.msg.event.SpecialEmojiMsgEvent
import com.module.playways.room.prepare.model.BaseRoundInfoModel
import com.module.playways.room.room.event.InputBoardEvent
import com.zq.live.proto.Common.ESex
import com.zq.live.proto.Common.UserInfo
import com.zq.live.proto.GrabRoom.EMsgPosType
import com.zq.live.proto.GrabRoom.ERoomMsgType
import com.zq.live.proto.GrabRoom.RoomMsg
import com.zq.live.proto.GrabRoom.SpecialEmojiMsg
import com.zq.live.proto.GrabRoom.SpecialEmojiMsgType

import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * 一唱到底的看这个
 * [com.module.playways.grab.room.bottom.GrabBottomContainerView]
 * 排位赛看这个
 * [com.module.playways.room.room.bottom.RankBottomContainerView]
 * 擂台赛看这个
 * [com.module.playways.race.room.bottom.RaceBottomContainerView]
 */
open class BottomContainerView : RelativeLayout {

     var mRoomData: BaseRoomData<*>?=null

     var mBottomContainerListener: Listener? = null

     var mShowInputContainerBtn: ExTextView?=null
     var mEmojiArea: RelativeLayout?=null

     var mEmojiBtn: ExImageView?=null
     var mGiftMallBtn: ExImageView?=null
    //    lateinit var mIvRoomManage: ExImageView
     var mInputBtn: ExTextView?=null
     var mSpeakingDotAnimationView: View?=null
     var mVoiceRecordBtn: VoiceRecordTextView?=null
     var meiguiIv: ExImageView?=null

    var mDynamicMsgPopWindow: PopupWindow? = null    //动态表情弹出面板
    var mDynamicMsgView: DynamicMsgView? = null

    internal var type = TYPE_TEXT_INPUT

//    protected var mLastSendType: SpecialEmojiMsgType? = null
//    protected var mContinueCount = 1
//    protected var mContinueId = 0L

//    internal var mHandler: Handler = object : Handler() {
//        override fun handleMessage(msg: Message) {
//            super.handleMessage(msg)
//            if (msg.what == CLEAR_CONTINUE_FLAG) {
//                mLastSendType = null
//                mContinueCount = 1
//            }
//        }
//    }

    open fun getLayout():Int{
        return R.layout.bottom_container_view_layout
    }

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr){
        init()
    }

    protected fun onQuickMsgDialogShow(show: Boolean) {

    }

    protected open fun init() {
        View.inflate(context, getLayout(), this)
        mEmojiBtn = this.findViewById(R.id.emoji_btn)
        mGiftMallBtn = this.findViewById(R.id.gift_mall_btn)

        mShowInputContainerBtn = this.findViewById(R.id.show_input_container_btn)

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }

        mShowInputContainerBtn?.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                if (mBottomContainerListener != null) {
                    mBottomContainerListener!!.showInputBtnClick()
                }
            }
        })

//        mEmoji1Btn?.setOnClickListener(object : DebounceViewClickListener() {
//            override fun clickValid(v: View) {
//                // 发送动态表情，粑粑
//                sendSpecialEmojiMsg(SpecialEmojiMsgType.SP_EMOJI_TYPE_UNLIKE, "扔了粑粑")
//            }
//        })
        mEmojiArea = this.findViewById(R.id.emoji_area)
//        mIvRoomManage = this.findViewById(R.id.iv_room_manage)
        mInputBtn = this.findViewById(R.id.input_btn)
        mSpeakingDotAnimationView = this.findViewById(R.id.speaking_dot_animation_view)
        mShowInputContainerBtn = this.findViewById(R.id.show_input_container_btn)
        mVoiceRecordBtn = this.findViewById(R.id.voice_record_btn)
        meiguiIv = rootView.findViewById(R.id.meigui_iv)


//        mIvRoomManage.setOnClickListener(object : DebounceViewClickListener() {
//            override fun clickValid(v: View) {
//                if (mBottomContainerListener != null) {
//                    mBottomContainerListener.clickRoomManagerBtn()
//                }
//            }
//        })

        mShowInputContainerBtn?.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                    mBottomContainerListener?.showInputBtnClick()
            }
        })

        meiguiIv?.setDebounceViewClickListener {
            mBottomContainerListener?.onClickFlower()
        }


        mInputBtn?.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                if (type == TYPE_TEXT_INPUT) {
                    type = TYPE_VOICE_INPUT
                    mInputBtn?.background = U.getDrawable(R.drawable.grab_bottom_input_icon)
                    mShowInputContainerBtn?.visibility = View.GONE
                    mVoiceRecordBtn?.visibility = View.VISIBLE
                } else {
                    type = TYPE_TEXT_INPUT
                    mInputBtn?.background = U.getDrawable(R.drawable.grab_bottom_voice_icon)
                    mShowInputContainerBtn?.visibility = View.VISIBLE
                    mVoiceRecordBtn?.visibility = View.GONE
                }
            }
        })

        mEmojiBtn?.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                // 动态表情按钮
                val w = U.getDisplayUtils().screenWidth - U.getDisplayUtils().dip2px(32f)
                val h = U.getDisplayUtils().dip2px(72f)
                if (mDynamicMsgView == null) {
                    mDynamicMsgView = DynamicMsgView(context)
                    mDynamicMsgView!!.setData(mRoomData)
                    mDynamicMsgView!!.setListener {
                        if (mDynamicMsgPopWindow != null) {
                            mDynamicMsgPopWindow!!.dismiss()
                        }
                    }
                } else {
                    mDynamicMsgView!!.loadEmoji()
                }
                if (mDynamicMsgPopWindow == null) {
                    mDynamicMsgPopWindow = PopupWindow(mDynamicMsgView, w, h)
                    mDynamicMsgPopWindow!!.isFocusable = true
                    mDynamicMsgPopWindow!!.animationStyle = R.style.MyPopupWindow_anim_style
                    // 去除动画
                    //                      mDynamicMsgPopWindow.setAnimationStyle(R.style.anim_quickmsg_dialog);
                    mDynamicMsgPopWindow!!.setBackgroundDrawable(BitmapDrawable())
                    mDynamicMsgPopWindow!!.isOutsideTouchable = true
                }
                if (!mDynamicMsgPopWindow!!.isShowing) {
                    val l = IntArray(2)
                    mInputBtn?.getLocationInWindow(l)
                    mDynamicMsgPopWindow!!.showAtLocation(mInputBtn, Gravity.START or Gravity.TOP, l[0], l[1] - h - U.getDisplayUtils().dip2px(5f))
                } else {
                    mDynamicMsgPopWindow!!.dismiss()
                }
            }
        })

        mGiftMallBtn?.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                    mBottomContainerListener?.showGiftPanel()
            }
        })
    }

//    protected fun sendSpecialEmojiMsg(type: SpecialEmojiMsgType, actionDesc: String) {
//        if (RoomDataUtils.isMyRound<BaseRoundInfoModel>(mRoomData?.realRoundInfo)) {
//            U.getToastUtil().showShort("暂时不能给自己送礼哦")
//            return
//        }
//        val msgService = ModuleServiceManager.getInstance().msgService
//        if (msgService != null) {
//            val ts = System.currentTimeMillis()
//            val count: Int
//            if (type == mLastSendType) {
//                count = mContinueCount + 1
//            } else {
//                count = 1
//                mContinueId = System.currentTimeMillis()
//            }
//
//            val senderInfo = UserInfo.Builder()
//                    .setUserID(MyUserInfoManager.uid.toInt())
//                    .setNickName(MyUserInfoManager.nickName)
//                    .setAvatar(MyUserInfoManager.avatar)
//                    .setSex(ESex.fromValue(MyUserInfoManager.sex))
//                    .setDescription("")
//                    .setIsSystem(false)
//                    .build()
//
//            val roomMsg = RoomMsg.Builder()
//                    .setTimeMs(ts)
//                    .setMsgType(ERoomMsgType.RM_SPECIAL_EMOJI)
//                    .setRoomID(mRoomData?.gameId)
//                    .setNo(ts)
//                    .setPosType(EMsgPosType.EPT_UNKNOWN)
//                    .setSender(senderInfo)
//                    .setSpecialEmojiMsg(SpecialEmojiMsg.Builder()
//                            .setContinueId(mContinueId)
//                            .setEmojiType(type)
//                            .setCount(count)
//                            .setEmojiAction(actionDesc)
//                            .build()
//                    )
//                    .build()
//
//            val contnet = U.getBase64Utils().encode(roomMsg.toByteArray())
//            msgService.sendChatRoomMessage(mRoomData?.gameId.toString(), CustomMsgType.MSG_TYPE_ROOM, contnet, object : ICallback {
//                override fun onSucess(obj: Any) {
//                    mContinueCount = count
//                    mLastSendType = type
//                    mHandler.removeMessages(CLEAR_CONTINUE_FLAG)
//                    // 5秒后连送重置
//                    mHandler.sendEmptyMessageDelayed(CLEAR_CONTINUE_FLAG, 5000)
//
//                    /**
//                     * 伪装成push过去
//                     */
//                    val basePushInfo = BasePushInfo()
//                    basePushInfo.roomID = mRoomData?.gameId!!
//                    basePushInfo.sender = senderInfo
//                    basePushInfo.timeMs = ts
//                    basePushInfo.no = ts
//
//                    val specialEmojiMsgEvent = SpecialEmojiMsgEvent(basePushInfo)
//                    specialEmojiMsgEvent.emojiType = type
//                    specialEmojiMsgEvent.count = count
//                    specialEmojiMsgEvent.action = actionDesc
//                    specialEmojiMsgEvent.coutinueId = mContinueId
//
//                    EventBus.getDefault().post(specialEmojiMsgEvent)
//                }
//
//                override fun onFailed(obj: Any, errcode: Int, message: String) {
//                    //TODO test 测试需要
//                    onSucess(obj)
//                }
//            })
//        }
//    }

    fun setOpVisible(visible: Boolean) {
        mEmojiArea?.visibility = if (visible) View.VISIBLE else View.INVISIBLE
        mGiftMallBtn?.isEnabled = visible
        mEmojiBtn?.isEnabled = visible
//        mIvRoomManage.isEnabled = visible
    }

    open fun dismissPopWindow() {
        if (mDynamicMsgPopWindow != null) {
            mDynamicMsgPopWindow!!.dismiss()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        MyLog.d("BottomContainerView", "onDetachedFromWindow")
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: InputBoardEvent) {
        if (event.show) {
            visibility = View.GONE
        } else {
            visibility = View.VISIBLE
        }
    }

    fun setListener(l: Listener) {
        mBottomContainerListener = l
    }

    open fun setRoomData(roomData: BaseRoomData<*>) {
        mRoomData = roomData
        mVoiceRecordBtn?.mRoomData = mRoomData
    }

    abstract class Listener {
        abstract fun showInputBtnClick()

        open fun clickRoomManagerBtn() {}

        open fun showGiftPanel() {

        }

        open fun onClickFlower() {

        }
    }

    companion object {
        internal val TYPE_TEXT_INPUT = 1    // 文本输入模式
        internal val TYPE_VOICE_INPUT = 2    // 语音输入模式
    }

}
