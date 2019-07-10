package com.module.playways.doubleplay.view

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v4.app.FragmentActivity
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.alibaba.fastjson.JSON
import com.common.log.MyLog
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ApiMethods
import com.common.rxretrofit.ApiObserver
import com.common.rxretrofit.ApiResult
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExImageView
import com.common.view.ex.drawable.DrawableCreator
import com.module.playways.R
import com.module.playways.doubleplay.DoubleRoomData
import com.module.playways.doubleplay.DoubleRoomServerApi
import com.module.playways.doubleplay.pbLocalModel.LocalCombineRoomMusic
import com.module.playways.songmanager.OwnerManagerActivity
import com.module.playways.songmanager.SongManageData
import com.zq.mediaengine.kit.ZqEngineKit
import io.reactivex.disposables.Disposable
import okhttp3.MediaType
import okhttp3.RequestBody


class DoubleSingSenceView : ConstraintLayout {
    val mShowCard1: DoubleSingCardView
    val mShowCard2: DoubleSingCardView
    val mMicIv: ExImageView
    val mPickIv: ImageView
    val mSelectIv: ImageView
    val mMicTv: TextView
    var mRoomData: DoubleRoomData? = null
    var mCurrentCardView: DoubleSingCardView? = null
    var mPickFun: (() -> Unit)? = null
    internal var mDoubleRoomServerApi = ApiManager.getInstance().createService(DoubleRoomServerApi::class.java)

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        inflate(context, com.module.playways.R.layout.double_sing_sence_layout, this)
        mShowCard1 = findViewById(com.module.playways.R.id.show_card1)
        mShowCard2 = findViewById(com.module.playways.R.id.show_card2)
        mMicIv = findViewById(com.module.playways.R.id.mic_iv)
        mPickIv = findViewById(com.module.playways.R.id.pick_iv) as ImageView
        mSelectIv = findViewById(com.module.playways.R.id.select_iv) as ImageView
        mMicTv = findViewById(com.module.playways.R.id.mic_tv)

        mSelectIv?.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                if (mRoomData!!.isRoomPrepared()) {
                    OwnerManagerActivity.open(context as FragmentActivity, SongManageData(mRoomData))
                } else {
                    U.getToastUtil().showShort("房间里还没有人哦～")
                }
            }
        })

        mShowCard1.mOnClickNextSongListener = {
            nextSong()
        }

        mShowCard2.mOnClickNextSongListener = {
            nextSong()
        }

        mPickIv.setOnClickListener {
            mPickFun?.invoke()
        }
    }

    var mDisposable: Disposable? = null
    fun nextSong() {
        mDisposable?.dispose()
        val mutableSet1 = mutableMapOf("roomID" to mRoomData!!.gameId)
        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(mutableSet1))
        mDisposable = ApiMethods.subscribe(mDoubleRoomServerApi.nextSong(body), object : ApiObserver<ApiResult>() {
            override fun process(obj: ApiResult?) {
                if (obj?.errno == 0) {
                    MyLog.w("DoubleSingSenceView", "nextSong success")
                } else {
                    U.getToastUtil().showShort(obj?.errmsg)
                }
            }

            override fun onError(e: Throwable) {
                U.getToastUtil().showShort("网络错误")
            }

        })
    }

    fun joinAgora() {
        MyLog.d("DoubleSingSenceView", "joinAgora")
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

    fun updateLockState() {
        mCurrentCardView?.updateLockState()
    }

    fun updateNextSongDec(mNext: String, hasNext: Boolean) {
        mCurrentCardView?.updateNextSongDec(mNext, hasNext)
    }

    fun startGame(mRoomData: DoubleRoomData, mCur: LocalCombineRoomMusic, mNext: String, hasNext: Boolean) {
        toNextSongCardView()
        mCurrentCardView?.visibility = View.VISIBLE
        mCurrentCardView?.playLyric(mRoomData!!, mCur, mNext, hasNext)
    }

    fun changeRound(mRoomData: DoubleRoomData, mCur: LocalCombineRoomMusic, mNext: String, hasNext: Boolean) {
        toNextSongCardView()
        mCurrentCardView?.playLyric(mRoomData!!, mCur, mNext, hasNext)
    }

    fun noMusic() {
        mShowCard1.visibility = View.GONE
        mShowCard2.visibility = View.GONE
    }

    fun toNextSongCardView() {
        if (mShowCard1.visibility == View.VISIBLE) {
            mShowCard1.goOut()
            mShowCard2.centerScale()
            mCurrentCardView = mShowCard2
        } else if (mShowCard2.visibility == View.VISIBLE) {
            mShowCard2.goOut()
            mShowCard1.centerScale()
            mCurrentCardView = mShowCard1
        } else {
            mShowCard1.centerScale()
            mCurrentCardView = mShowCard1
        }
    }

    fun setData() {

    }

    fun destroy() {
        mDisposable?.dispose()
    }
}