package com.module.playways.doubleplay.view

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.common.log.MyLog
import com.common.rxretrofit.ApiManager
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExImageView
import com.common.view.ex.drawable.DrawableCreator
import com.module.playways.R
import com.module.playways.doubleplay.DoubleRoomData
import com.module.playways.doubleplay.DoubleRoomServerApi
import com.zq.mediaengine.kit.ZqEngineKit
import kotlinx.android.synthetic.main.double_game_sence_layout.view.*


class DoubleGameSenceView : ConstraintLayout {
    val mShowCard: DoubleSingCardView
    val mMicIv: ExImageView
    val mPickIv: ImageView
    val mSelectIv: ImageView
    val mMicTv: TextView
    var mPickFun: (() -> Unit)? = null
    var mRoomData: DoubleRoomData? = null
    internal var mDoubleRoomServerApi = ApiManager.getInstance().createService(DoubleRoomServerApi::class.java)

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        inflate(context, R.layout.double_game_sence_layout, this)

        mShowCard = findViewById(R.id.show_card)
        mMicIv = findViewById(R.id.mic_iv)
        mPickIv = findViewById(R.id.pick_iv) as ImageView
        mSelectIv = findViewById(R.id.select_iv) as ImageView
        mMicTv = findViewById(R.id.mic_tv)
        mShowCard.mCutSongTv.text = "结束"
        mShowCard.mOnClickNextSongListener = {
            mDoubleRoomServerApi.nextSong(null)
        }

        mPickIv.setOnClickListener {
            mPickFun?.invoke()
            pick_diffuse_view.start(2000)
        }
    }

    fun joinAgora() {
        MyLog.d("DoubleGameSenceView", "joinAgora")
        val drawable = DrawableCreator.Builder()
                .setSelectedDrawable(U.getDrawable(R.drawable.skr_jingyin_able))
                .setUnSelectedDrawable(U.getDrawable(R.drawable.srf_bimai))
                .build()
        mMicIv?.background = drawable

        mMicIv?.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                // 开关麦克
                val isSelected = mMicIv?.isSelected
                ZqEngineKit.getInstance().muteLocalAudioStream(!isSelected)
                mMicIv?.setSelected(!isSelected)
            }
        })
    }

    fun selected() {
        mMicIv?.setSelected(ZqEngineKit.getInstance().params.isLocalAudioStreamMute)
    }

    fun setData() {

    }

    fun destroy() {

    }
}