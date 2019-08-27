package com.module.playways.race.room

import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.fastjson.JSON
import com.common.base.BaseActivity
import com.common.core.myinfo.MyUserInfoManager
import com.common.log.MyLog
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.subscribe
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.component.level.view.LevelStarProgressBar
import com.component.level.view.NormalLevelView2
import com.module.RouterConstants
import com.module.playways.R
import com.module.playways.race.RaceRoomServerApi
import com.module.playways.room.room.model.score.ScoreResultModel
import kotlinx.coroutines.launch

@Route(path = RouterConstants.ACTIVITY_RACE_RESULT)
class RaceResultActivity : BaseActivity() {
    lateinit var changeTv: TextView
    lateinit var levelView: NormalLevelView2
    lateinit var levelDescTv: ExTextView
    lateinit var levelProgress: LevelStarProgressBar
    lateinit var descTv: TextView
    lateinit var countDownTv: TextView
    lateinit var playAgainTv: ExTextView
    lateinit var ivBack: ExImageView

    val raceRoomServerApi = ApiManager.getInstance().createService(RaceRoomServerApi::class.java)

    var roomID: Long = -1L
    var roundSeq: Int = -1

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.race_result_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        roomID = intent.getLongExtra("roomID", -1L)
        roundSeq = intent.getIntExtra("roundSeq", -1)
        if (roomID == -1L || roundSeq == -1) {
            MyLog.e(TAG, "no roomID or roundSeq")
            finish()
            return
        }
        changeTv = findViewById(R.id.change_tv)
        levelView = findViewById(R.id.level_view)
        levelDescTv = findViewById(R.id.level_desc_tv)
        levelProgress = findViewById(R.id.level_progress)
        descTv = findViewById(R.id.desc_tv)
        countDownTv = findViewById(R.id.count_down_tv)
        playAgainTv = findViewById(R.id.play_again_tv)
        ivBack = findViewById(R.id.iv_back)

        ivBack.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                finish()
            }
        })

        playAgainTv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                // todo 再来一局，重新开始匹配
            }
        })

        getResult()
    }

    private fun getResult() {
        launch {
            val result = subscribe { raceRoomServerApi.getResult(roomID, MyUserInfoManager.getInstance().uid, roundSeq) }
            if (result.errno == 0) {
                val list = JSON.parseArray(result.data.getString("userScoreResult"), ScoreResultModel::class.java)
                if (!list.isNullOrEmpty()) {
                    for (scoreResultModel in list) {
                        if (scoreResultModel.userID.toLong() == MyUserInfoManager.getInstance().uid) {
                            showResult(scoreResultModel)
                            return@launch
                        }
                    }
                } else {
                    MyLog.e(TAG, "getResult erro 服务器数据为空")
                }
            } else {

            }
        }
    }

    private fun showResult(scoreResultModel: ScoreResultModel) {
        val scoreState = scoreResultModel.lastState
        scoreState?.let {
            levelDescTv.text = it.rankingDesc
            var progress = 0
            if (it.maxExp != 0) {
                progress = it.currExp * 100 / it.maxExp
            }
            levelProgress.setCurProgress(progress)
            descTv.text = "距离下次升段还需${it.maxExp - it.currExp}积分"
            levelView.bindData(it.mainRanking, it.subRanking)
        }
    }

    override fun useEventBus(): Boolean {
        return false
    }
}
