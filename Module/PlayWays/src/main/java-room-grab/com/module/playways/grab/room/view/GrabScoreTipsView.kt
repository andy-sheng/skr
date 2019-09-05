package com.module.playways.grab.room.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout

import com.common.log.MyLog
import com.module.playways.grab.room.GrabRoomData
import com.module.playways.grab.room.model.GrabConfigModel
import com.module.playways.grab.room.model.GrabRoundInfoModel
import com.module.playways.grab.room.model.GrabScoreTipMsgModel
import com.module.playways.room.room.score.bar.ScoreTipsView
import com.module.playways.R

class GrabScoreTipsView : RelativeLayout {

    val mTag = "GrabScoreTipsView"

    private var mRoomData: GrabRoomData? = null

    private var mLastItem: ScoreTipsView.Item? = null

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    init {
        View.inflate(context, R.layout.grab_score_tips_layout, this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
    }

    fun setRoomData(roomData: GrabRoomData) {
        mRoomData = roomData
    }

    fun updateScore(score1: Int, songLineNum: Int) {
        MyLog.d(mTag, "updateScore score1=$score1 songLineNum=$songLineNum")

        val gameConfigModel = mRoomData?.grabConfigModel
        val item = ScoreTipsView.Item()

        if (gameConfigModel != null) {
            // 总分是这个肯定没错
            val scoreTipMsgModelList = gameConfigModel.qScoreTipMsg
            if (scoreTipMsgModelList != null) {
                for (m in scoreTipMsgModelList) {
                    if (score1 >= m.fromScore && score1 < m.toScore) {
                        // 命中
                        when (m.tipType) {
                            0 -> {
                            }
                            1 -> item.level = ScoreTipsView.Level.Grab_renzhen
                            2 -> item.level = ScoreTipsView.Level.Grab_jiayou
                            3 -> item.level = ScoreTipsView.Level.Grab_bucuo
                            4 -> item.level = ScoreTipsView.Level.Grab_taibang
                            5 -> item.level = ScoreTipsView.Level.Grab_wanmei
                        }
                        break
                    }
                }
            }
        } else {
            when {
                score1 >= 85 -> item.level = ScoreTipsView.Level.Grab_wanmei
                score1 >= 60 -> item.level = ScoreTipsView.Level.Grab_taibang
                score1 >= 36 -> item.level = ScoreTipsView.Level.Grab_bucuo
                score1 < 7 -> item.level = ScoreTipsView.Level.Grab_jiayou
                score1 >= 0 -> item.level = ScoreTipsView.Level.Grab_renzhen
            }
        }
        if (item.level != null) {
            if (mLastItem != null && item.level == mLastItem!!.level) {
                item.num = mLastItem!!.num + 1
            }
            mLastItem = item
            val now = mRoomData?.realRoundInfo
            if (now != null && now.singBySelf()) {
                ScoreTipsView.play(this, item, 1)
            } else {
                ScoreTipsView.play(this, item, 1)
            }
        }
    }

    fun reset() {
        mLastItem = null
    }
}
