package com.module.playways.race.room.bottom

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.PopupWindow
import android.widget.RelativeLayout
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.module.playways.BaseRoomData
import com.module.playways.R
import com.module.playways.grab.room.dynamicmsg.DynamicMsgView
import com.module.playways.grab.room.voicemsg.VoiceRecordTextView
import com.module.playways.race.room.RaceRoomData
import com.module.playways.room.room.view.BottomContainerView

class RaceBottomContainerView : BottomContainerView {

    lateinit var mEmojiArea: RelativeLayout
    lateinit var mIvRoomManage: ExImageView
    lateinit var mInputBtn: ExTextView
    lateinit var mSpeakingDotAnimationView: View
    lateinit var mShowInputContainerBtn: ExTextView
    lateinit var mVoiceRecordBtn: VoiceRecordTextView

    var mDynamicMsgPopWindow: PopupWindow? = null    //动态表情弹出面板
    var mDynamicMsgView: DynamicMsgView? = null

    var mRaceRoomData: RaceRoomData? = null

    internal var type = TYPE_TEXT_INPUT

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    override fun getLayout(): Int {
        return R.layout.race_bottom_container_view_layout
    }

    override fun init() {
        super.init()
        mEmojiArea = this.findViewById(R.id.emoji_area)
        mIvRoomManage = this.findViewById(R.id.iv_room_manage)
        mInputBtn = this.findViewById(R.id.input_btn)
        mSpeakingDotAnimationView = this.findViewById(R.id.speaking_dot_animation_view)
        mShowInputContainerBtn = this.findViewById(R.id.show_input_container_btn)
        mVoiceRecordBtn = this.findViewById(R.id.voice_record_btn)

        mIvRoomManage.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                if (mBottomContainerListener != null) {
                    mBottomContainerListener.clickRoomManagerBtn()
                }
            }
        })

        mShowInputContainerBtn.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                if (mBottomContainerListener != null) {
                    mBottomContainerListener.showInputBtnClick()
                }
            }
        })


        mInputBtn.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                if (type == TYPE_TEXT_INPUT) {
                    type = TYPE_VOICE_INPUT
                    mInputBtn.background = U.getDrawable(R.drawable.grab_bottom_input_icon)
                    mShowInputContainerBtn.visibility = View.GONE
                    mVoiceRecordBtn.visibility = View.VISIBLE
                } else {
                    type = TYPE_TEXT_INPUT
                    mInputBtn.background = U.getDrawable(R.drawable.grab_bottom_voice_icon)
                    mShowInputContainerBtn.visibility = View.VISIBLE
                    mVoiceRecordBtn.visibility = View.GONE
                }
            }
        })

        mEmoji1Btn.setOnClickListener(object : DebounceViewClickListener() {
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
                    mInputBtn.getLocationInWindow(l)
                    mDynamicMsgPopWindow!!.showAtLocation(mInputBtn, Gravity.START or Gravity.TOP, l[0], l[1] - h - U.getDisplayUtils().dip2px(5f))
                } else {
                    mDynamicMsgPopWindow!!.dismiss()
                }
            }
        })

        mEmoji2Btn.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                if (mBottomContainerListener != null) {
                    mBottomContainerListener.showGiftPanel()
                }
            }
        })
    }

    fun setOpVisible(visible: Boolean) {
        mEmojiArea.visibility = if (visible) View.VISIBLE else View.INVISIBLE
        mEmoji2Btn.isEnabled = visible
        mEmoji1Btn.isEnabled = visible
        mIvRoomManage.isEnabled = visible
    }

    override fun setRoomData(roomData: BaseRoomData<*>) {
        super.setRoomData(roomData)
        if (mRoomData is RaceRoomData) {
            mRaceRoomData = mRoomData as RaceRoomData
            mVoiceRecordBtn.mRoomData = mRaceRoomData
        }
    }

    internal fun adjustUi(grabOwner: Boolean, isOwnerRoom: Boolean) {
        if (grabOwner) {
            mIvRoomManage.visibility = View.VISIBLE
            mIvRoomManage.setImageResource(R.drawable.ycdd_fangzhu)
        } else {
            if (isOwnerRoom) {
                mIvRoomManage.visibility = View.VISIBLE
                mIvRoomManage.setImageResource(R.drawable.ycdd_diange)
            } else {
                mIvRoomManage.visibility = View.GONE
            }
        }
    }

    override fun dismissPopWindow() {
        super.dismissPopWindow()
        if (mDynamicMsgPopWindow != null) {
            mDynamicMsgPopWindow!!.dismiss()
        }
    }

    companion object {
        internal val TYPE_TEXT_INPUT = 1    // 文本输入模式
        internal val TYPE_VOICE_INPUT = 2    // 语音输入模式
    }
}
