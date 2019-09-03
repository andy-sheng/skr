package com.module.playways.race.room.view

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import android.widget.ProgressBar
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.view.setDebounceViewClickListener
import com.common.log.MyLog
import com.common.utils.U
import com.common.view.ex.ExConstraintLayout
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.module.playways.R
import com.module.playways.race.room.RaceRoomData
import com.module.playways.race.room.model.RaceRoundInfoModel
import com.module.playways.race.room.model.RaceWantSingInfo
import com.zq.live.proto.RaceRoom.ERaceRoundStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class RaceSelectSongView : ExConstraintLayout {
    val TAG = "RaceSelectSongView"
    var bg: ImageView
    private val progressBg: ExImageView
    private val progressBar: ProgressBar
    private val forthSongItem: RaceSelectSongItemView
    private val firstSongItem: RaceSelectSongItemView
    private val secondSongItem: RaceSelectSongItemView
    private val thirdSongItem: RaceSelectSongItemView
    var countDownTv: ExTextView
    var blurBg: ImageView
    private val itemList: ArrayList<RaceSelectSongItemView> = ArrayList()
    var animator: ValueAnimator? = null
    var mRoomData: RaceRoomData? = null
    var mSelectCall: ((Int, Int) -> Unit)? = null
    var mNoSelectCall: (() -> Unit)? = null
    var mSeq = -1

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        View.inflate(context, R.layout.race_select_song_layout, this)
        bg = findViewById(R.id.bg)
        progressBg = findViewById(R.id.progress_bg)
        progressBar = findViewById(R.id.progress_bar)
        firstSongItem = findViewById(R.id.first_song_item)
        secondSongItem = findViewById(R.id.second_song_item)
        thirdSongItem = findViewById(R.id.third_song_item)
        forthSongItem = findViewById(R.id.forth_song_item)
        countDownTv = rootView.findViewById(R.id.count_down_tv)
        blurBg = rootView.findViewById(R.id.blur_bg)

        itemList.add(firstSongItem)
        itemList.add(secondSongItem)
        itemList.add(thirdSongItem)
        itemList.add(forthSongItem)
        progressBar.max = 360
        progressBar.progress = 0
        firstSongItem.setDebounceViewClickListener {
            firstSongItem.getSong()?.let {
                U.getSoundUtils().play(TAG, R.raw.newrank_picksong)
                if (mRoomData?.realRoundSeq == mSeq) {
                    MyLog.d(TAG, "mSeq is $mSeq")
                    mSelectCall?.invoke(1, mSeq)
                    mNoSelectCall = null
                }
            }
        }

        secondSongItem.setDebounceViewClickListener {
            secondSongItem.getSong()?.let {
                if (mRoomData?.realRoundSeq == mSeq) {
                    MyLog.d(TAG, "mSeq is $mSeq")
                    U.getSoundUtils().play(TAG, R.raw.newrank_picksong)
                    mSelectCall?.invoke(2, mSeq)
                    mNoSelectCall = null
                }
            }
        }

        thirdSongItem.setDebounceViewClickListener {
            thirdSongItem.getSong()?.let {
                if (mRoomData?.realRoundSeq == mSeq) {
                    MyLog.d(TAG, "mSeq is $mSeq")
                    U.getSoundUtils().play(TAG, R.raw.newrank_picksong)
                    mSelectCall?.invoke(3, mSeq)
                    mNoSelectCall = null
                }
            }
        }

        forthSongItem.setDebounceViewClickListener {
            forthSongItem.getSong()?.let {
                U.getSoundUtils().play(TAG, R.raw.newrank_picksong)
                if (mRoomData?.realRoundSeq == mSeq) {
                    MyLog.d(TAG, "mSeq is $mSeq")
                    mSelectCall?.invoke(4, mSeq)
                    mNoSelectCall = null
                }
            }
        }

        U.getSoundUtils().preLoad(TAG, R.raw.newrank_picksong)
    }

    fun setRoomData(roomData: RaceRoomData, selectCall: ((Int, Int) -> Unit)) {
        mRoomData = roomData
        firstSongItem.setRaceRoomData(roomData)
        secondSongItem.setRaceRoomData(roomData)
        thirdSongItem.setRaceRoomData(roomData)
        forthSongItem.setRaceRoomData(roomData)
        mSelectCall = selectCall
    }

    var scaleAnimatorSet: Animator? = null

    //倒计时3秒，选择6秒
    fun setSongName(seq: Int, noSelectCall: (() -> Unit)?) {
        MyLog.d(TAG, "setSongName seq = $seq, noSelectCall = $noSelectCall")
        progressBar.progress = 0
        mSeq = seq
        scaleAnimatorSet = ObjectAnimator.ofPropertyValuesHolder(
                this@RaceSelectSongView,
                PropertyValuesHolder.ofFloat("scaleX", 0.85f, 1f),
                PropertyValuesHolder.ofFloat("scaleY", 0.85f, 1f),
                PropertyValuesHolder.ofFloat("alpha", 0.6f, 1f)
        )
        scaleAnimatorSet?.duration = 500
        scaleAnimatorSet?.interpolator = OvershootInterpolator()
        scaleAnimatorSet?.start()

        mNoSelectCall = noSelectCall
        firstSongItem.reset()
        secondSongItem.reset()
        thirdSongItem.reset()
        forthSongItem.reset()
        mRoomData?.let {
            val info = it.realRoundInfo as RaceRoundInfoModel
            info?.let {
                for (i in 0 until it.games.size) {
                    if (i < 4) {
                        itemList[i].setSong(it.games[i])
                    }
                }
            }
        }

        var lastedTime = 9000
        if (mRoomData?.realRoundInfo?.enterStatus == ERaceRoundStatus.ERRS_CHOCING.value) {
            mRoomData?.realRoundInfo?.elapsedTimeMs?.let {
                //多3秒是因为中间动画（显示结果3秒|（无人抢唱+下一首）3秒）
                lastedTime = 13400 - it
                MyLog.d(TAG, "setSongName elapsedTimeMs is $it")
                if (lastedTime < 0) {
                    lastedTime = 1000
                } else if (lastedTime > 9000) {
//                    lastedTime = 9000
                }
            }
        }

        launch(Dispatchers.Main) {
            countDownTv.visibility = View.GONE
            enableSelectSong(false)
            var countDownSecond = (lastedTime - 6000) / 1000
            if (countDownSecond > 0) {
                countDownTv.visibility = View.VISIBLE
                blurBg.visibility = View.VISIBLE

                repeat(countDownSecond) {
                    countDownTv.text = (countDownSecond - it).toString()
                    delay(1000)
                }

                countDownTv.visibility = View.GONE
                blurBg.visibility = View.GONE
            }

            if (lastedTime > 6000) {
                startCountDown(6000)
            } else {
                startCountDown(lastedTime)
            }
        }
        updateSelectState()
    }

    private fun enableSelectSong(isEnabled: Boolean) {
        firstSongItem.isEnabled = isEnabled
        secondSongItem.isEnabled = isEnabled
        thirdSongItem.isEnabled = isEnabled
        forthSongItem.isEnabled = isEnabled
    }

    fun updateSelectState() {
        mRoomData?.let {
            it.realRoundInfo?.let { raceRoundInfoModel ->
                val map = HashMap<Int, ArrayList<RaceWantSingInfo>>()
                map.put(1, ArrayList<RaceWantSingInfo>())
                map.put(2, ArrayList<RaceWantSingInfo>())
                map.put(3, ArrayList<RaceWantSingInfo>())
                map.put(4, ArrayList<RaceWantSingInfo>())
                raceRoundInfoModel.wantSingInfos.forEach {
                    map[it.choiceID]?.add(it)
                    if (MyUserInfoManager.getInstance().uid == it.userID.toLong()) {
                        enableSelectSong(false)
                    }
                }

                for (i in 0 until map.size) {
                    if (i < 4) {
                        itemList[i].bindData(map[i + 1])
                    }
                }
            }
        }
    }

    fun startCountDown(countDownMic: Int) {
        MyLog.d(TAG, "startCountDown countDownMic is $countDownMic")
        enableSelectSong(true)
        animator = ValueAnimator.ofInt(((6000 - countDownMic) * 360) / 6000, 360)
        animator?.duration = countDownMic.toLong()
        animator?.addUpdateListener {
            progressBar.progress = it.animatedValue as Int
        }
        animator?.start()
        animator?.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {

            }

            override fun onAnimationEnd(animation: Animator?) {
                mNoSelectCall?.invoke()
            }

            override fun onAnimationCancel(animation: Animator?) {

            }

            override fun onAnimationStart(animation: Animator?) {

            }
        })
    }

    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)
        if (visibility == View.GONE) {
            mNoSelectCall = null
            animator?.cancel()
            scaleAnimatorSet?.cancel()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mNoSelectCall = null
        animator?.cancel()
        scaleAnimatorSet?.cancel()
        U.getSoundUtils().release(TAG)
    }
}
