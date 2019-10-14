package com.module.playways.race.room.view

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.view.setDebounceViewClickListener
import com.common.log.MyLog
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.module.playways.R
import com.module.playways.race.room.event.RaceWantSingChanceEvent
import com.module.playways.race.room.model.RaceGamePlayInfo
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class RaceSongInfoView : ConstraintLayout {
    val TAG = "RaceSongInfoViewRaceSongInfoView"
    var bg: ExImageView
    var songNameTv: ExTextView
    var anchorTv: ExTextView
    var lyricView: ExTextView
    var divider: ExImageView
    var signUpTv: ExTextView
    var signUpCall: ((Int, RaceGamePlayInfo?) -> Unit)? = null
    var model: RaceGamePlayInfo? = null
    var choiceId: Int = -1

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        View.inflate(context, R.layout.race_song_info_view_layout, this)
        bg = rootView.findViewById(R.id.bg)
        lyricView = rootView.findViewById(R.id.lyric_tv)
        songNameTv = rootView.findViewById(R.id.song_name_tv)
        anchorTv = rootView.findViewById(R.id.anchor_tv)
        divider = rootView.findViewById(R.id.divider)
        signUpTv = rootView.findViewById(R.id.sign_up_tv)

        signUpTv.setDebounceViewClickListener {
            signUpCall?.invoke(choiceId, model)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: RaceWantSingChanceEvent) {
        MyLog.d(TAG, "onEvent event = $event")
        signUpTv.isEnabled = false
        if (event.choiceID == choiceId && event.userID == MyUserInfoManager.getInstance().uid.toInt()) {
            signUpTv.text = "报名成功"
        } else {
            signUpTv.visibility = View.GONE
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        EventBus.getDefault().register(this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        EventBus.getDefault().unregister(this)
    }

    fun setData(index: Int, model: RaceGamePlayInfo) {
        signUpTv.isEnabled = true
        signUpTv.text = "报名"
        signUpTv.visibility = View.VISIBLE

        this.model = model
        choiceId = index + 1
        songNameTv.text = "《${model.commonMusic?.itemName}》"
        anchorTv.text = model.commonMusic?.writer
        lyricView.text = "歌词"
    }
}