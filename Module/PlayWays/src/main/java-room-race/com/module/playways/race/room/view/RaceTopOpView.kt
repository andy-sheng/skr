package com.module.playways.race.room.view

import android.animation.AnimatorSet
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExTextView
import com.module.playways.R
import com.module.playways.race.room.RaceRoomData

class RaceTopOpView : RelativeLayout {

    internal lateinit var mTvChangeRoom: ExTextView

    internal lateinit var mIvVoiceSetting: ImageView
    internal lateinit var mCameraIv: ImageView
    internal lateinit var mDivider: View
    internal lateinit var mGameRuleIv: ImageView
    internal lateinit var mFeedBackIv: ImageView

    internal lateinit var mExitTv: ExTextView

    internal var mOnClickChangeRoomListener: Listener? = null
    internal var mRoomData: RaceRoomData? = null

    internal var mAnimatorSet: AnimatorSet? = null  //金币加减的动画
    internal var mHzAnimatorSet: AnimatorSet? = null  //金币加减的动画


    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    fun setListener(onClickChangeRoomListener: Listener) {
        mOnClickChangeRoomListener = onClickChangeRoomListener
    }

    fun init() {
        View.inflate(context, R.layout.race_top_op_view, this)
        mTvChangeRoom = findViewById<View>(R.id.tv_change_room) as ExTextView
        mCameraIv = findViewById<View>(R.id.camera_iv) as ImageView
        mDivider = findViewById(R.id.divider) as View
        mGameRuleIv = findViewById<View>(R.id.game_rule_iv) as ImageView
        mFeedBackIv = findViewById<View>(R.id.feed_back_iv) as ImageView
        mExitTv = findViewById<View>(R.id.exit_tv) as ExTextView
        mIvVoiceSetting = findViewById<View>(R.id.iv_voice_setting) as ImageView

        mTvChangeRoom.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                if (mOnClickChangeRoomListener != null) {
                    mOnClickChangeRoomListener!!.changeRoom()
                }
            }
        })

        mIvVoiceSetting.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                if (mOnClickChangeRoomListener != null) {
                    mOnClickChangeRoomListener!!.onClickVoiceAudition()
                }
            }
        })

        mCameraIv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                if (mOnClickChangeRoomListener != null) {
                    mOnClickChangeRoomListener!!.onClickCamera()
                }
            }
        })

        mGameRuleIv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                if (mOnClickChangeRoomListener != null) {
                    mOnClickChangeRoomListener!!.onClickGameRule()
                }
            }
        })

        mFeedBackIv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                if (mOnClickChangeRoomListener != null) {
                    mOnClickChangeRoomListener!!.onClickFeedBack()
                }
            }
        })

        mExitTv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                if (mOnClickChangeRoomListener != null) {
                    mOnClickChangeRoomListener!!.closeBtnClick()
                }
            }
        })
    }

    fun setRoomData(roomData: RaceRoomData) {
        mRoomData = roomData
//        if (!mGrabRoomData.isVideoRoom) {
//            mCameraIv.visibility = View.GONE
//            mDivider.visibility = View.GONE
//            mIvVoiceSetting.background = U.getDrawable(R.drawable.yichangdaodi_yinyue_audio)
//        }
//        if (mGrabRoomData.isOwner) {
//            // 是房主，肯定不能切换房间
//            setChangeRoomBtnVisiable(false)
//        } else {
//            // 观众的话，私密房间也不能切
//            if (mGrabRoomData.roomType == GrabRoomType.ROOM_TYPE_SECRET || mGrabRoomData.roomType == GrabRoomType.ROOM_TYPE_FRIEND) {
//                setChangeRoomBtnVisiable(false)
//            } else {
//                setChangeRoomBtnVisiable(true)
//            }
//        }
//        if (mGrabRoomData.roomType == GrabRoomType.ROOM_TYPE_GUIDE) {
//            // 新手房
//            setChangeRoomBtnVisiable(false)
//            mIvVoiceSetting.visibility = View.GONE
//        }
    }

    /**
     * 切换房间按钮是否可见
     *
     * @param visiable
     */
    internal fun setChangeRoomBtnVisiable(visiable: Boolean) {
        if (visiable) {
            mTvChangeRoom.visibility = View.VISIBLE
        } else {
            mTvChangeRoom.visibility = View.GONE
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (mAnimatorSet != null) {
            mAnimatorSet!!.removeAllListeners()
            mAnimatorSet!!.cancel()
        }

        if (mHzAnimatorSet != null) {
            mHzAnimatorSet!!.removeAllListeners()
            mHzAnimatorSet!!.cancel()
        }
    }

    interface Listener {
        fun changeRoom()

        fun closeBtnClick()

        fun onVoiceChange(voiceOpen: Boolean)

        fun onClickGameRule()

        fun onClickFeedBack()

        fun onClickVoiceAudition()

        fun onClickCamera()
    }
}
