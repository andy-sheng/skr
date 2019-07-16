package com.module.playways.grab.room.bottom

import android.content.Context
import android.graphics.Rect
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
import com.component.busilib.constans.GrabRoomType
import com.module.playways.BaseRoomData
import com.module.playways.R
import com.module.playways.grab.room.GrabRoomData
import com.module.playways.grab.room.dynamicmsg.DynamicMsgView
import com.module.playways.grab.room.event.GrabRoundChangeEvent
import com.module.playways.grab.room.event.GrabRoundStatusChangeEvent
import com.module.playways.grab.room.voicemsg.VoiceRecordTextView
import com.module.playways.room.room.view.BottomContainerView

import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class GrabBottomContainerView : BottomContainerView {

    lateinit var mEmojiArea: RelativeLayout
    lateinit var mIvRoomManage: ExImageView
    lateinit var mInputBtn: ExTextView
    lateinit var mSpeakingDotAnimationView: View
    lateinit var mShowInputContainerBtn: ExTextView
    lateinit var mVoiceRecordBtn: VoiceRecordTextView

    var mDynamicMsgPopWindow: PopupWindow? = null    //动态表情弹出面板
    var mDynamicMsgView: DynamicMsgView? = null

    var mGrabRoomData: GrabRoomData? = null

    internal var type = TYPE_TEXT_INPUT

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    override fun getLayout(): Int {
        return R.layout.grab_bottom_container_view_layout
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
        if (mRoomData is GrabRoomData) {
            mGrabRoomData = mRoomData as GrabRoomData
            if (mGrabRoomData?.ownerId != 0) {
                if (mGrabRoomData != null && mGrabRoomData!!.isOwner) {
                    //是房主
                    adjustUi(true, true)
                } else {
                    //不是一唱到底房主
                    adjustUi(false, true)
                }
            } else {
                adjustUi(false, false)
            }

            if (mGrabRoomData?.roomType == GrabRoomType.ROOM_TYPE_GUIDE) {
                mEmoji2Btn.visibility = View.GONE
            }
            mVoiceRecordBtn.mRoomData = mGrabRoomData
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
            val drawable = U.getDrawable(R.drawable.kuaijiehuifu_shou)
            drawable.bounds = Rect(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
            mInputBtn.setCompoundDrawables(null, null,
                    drawable, null)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: GrabRoundStatusChangeEvent) {
        //MyLog.d("GrabBottomContainerView","onEvent" + " event=" + event);
        //        GrabRoundInfoModel now = event.roundInfo;
        //        if (now != null && now.isSingStatus() && mGrabRoomData.isOwner()) {
        //            if (mGrabRoomData.isSpeaking() && !now.singBySelf()) {
        //                U.getToastUtil().showShort("有人上麦了,暂时不能说话哦", 0, Gravity.CENTER);
        //            }
        //            mQuickBtn.setImageResource(R.drawable.fz_anzhushuohua_b);
        //            mQuickBtn.setEnabled(false);
        //            mSpeakingDotAnimationView.setVisibility(GONE);
        //            mShowInputContainerBtn.setText("夸赞是一种美德");
        //            EventBus.getDefault().post(new GrabSpeakingControlEvent(false));
        //        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: GrabRoundChangeEvent) {
        //        if (mGrabRoomData != null && mGrabRoomData.isOwner()) {
        //            mQuickBtn.setEnabled(true);
        //            if (mGrabRoomData.isSpeaking()) {
        //                // 正在说话，就算了
        //            } else {
        //                mQuickBtn.setImageResource(R.drawable.fz_anzhushuohua);
        //            }
        //        }
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
