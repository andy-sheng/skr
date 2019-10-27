package com.module.playways.race.room

import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.alibaba.fastjson.JSON
import com.common.base.BaseActivity
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.userinfo.model.ScoreStateModel
import com.common.log.MyLog
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.subscribe
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.component.level.view.LevelStarProgressBar
import com.component.level.view.NormalLevelView2
import com.module.RouterConstants
import com.module.playways.R
import com.module.playways.listener.AnimationListener
import com.module.playways.race.RaceRoomServerApi
import com.module.playways.race.room.model.LevelResultModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Route(path = RouterConstants.ACTIVITY_RACE_RESULT)
class RaceResultActivity : BaseActivity() {

    val mTag = "RaceResultActivity"

    lateinit var changeTv: TextView
    lateinit var levelView: NormalLevelView2
    lateinit var levelDescTv: ExTextView
    lateinit var levelProgress: LevelStarProgressBar
    lateinit var descTv: TextView
    lateinit var countDownTv: TextView
    lateinit var playAgainTv: ExTextView
    lateinit var ivBack: ExImageView

    val raceRoomServerApi = ApiManager.getInstance().createService(RaceRoomServerApi::class.java)

    var roomID: Int = -1
    var roundSeq: Int = -1

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.race_result_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        roomID = intent.getIntExtra("roomID", -1)
        roundSeq = intent.getIntExtra("roundSeq", -1)
        if (roomID == -1 || roundSeq == -1) {
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
                goMatchPage()
            }
        })

        descTv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                ARouter.getInstance().build(RouterConstants.ACTIVITY_WEB)
                        .withString(RouterConstants.KEY_WEB_URL, "https://fe.inframe.mobi/pages/banner/2p8p3gf3ujzxsw97z.html")
                        .navigation()
            }
        })

        getResult()
        U.getSoundUtils().preLoad(mTag, R.raw.newrank_resultpage)
        launch {
            delay(200)
            U.getSoundUtils().play(mTag, R.raw.newrank_resultpage)
        }
    }

    private fun getResult() {
        launch {
            val result = subscribe { raceRoomServerApi.getResult(roomID, MyUserInfoManager.getInstance().uid.toInt(), roundSeq) }
            if (result.errno == 0) {
                val raceResultModel = JSON.parseObject(result.data.getString("userScoreChange"), LevelResultModel::class.java)
                if (raceResultModel != null) {
                    showResult(raceResultModel)
                    return@launch
                } else {
                    MyLog.e(TAG, "getResult erro 服务器数据为空")
                }
            } else {

            }
        }
    }

    private fun showResult(raceResultModel: LevelResultModel) {
        descTv.text = "距离下次升段还需${raceResultModel.gap}经验"
        if (raceResultModel.get >= 0) {
            changeTv.text = "+${raceResultModel.get}"
        } else {
            changeTv.text = raceResultModel.get.toString()
        }

        val scoreState = raceResultModel.getLastState()
        scoreState?.let {
            levelDescTv.text = it.rankingDesc
            var progress = 0
            if (it.maxExp != 0) {
                progress = it.currExp * 100 / it.maxExp
            }
            levelProgress.setCurProgress(progress)
            levelView.bindData(it.mainRanking, it.subRanking)
        }

        launch {
            repeat(8) {
                countDownTv.text = "${8 - it}后自动进入下一场挑战"
                delay(1000)
            }
            goMatchPage()
        }
    }

    // 积分变化用CircleCountDownView 段位变化用一个svga
    fun levelChangeAnimation(before: ScoreStateModel, after: ScoreStateModel, listener: AnimationListener) {
        val start = before.currExp.toFloat() / before.maxExp.toFloat()
        val end = after.currExp.toFloat() / after.maxExp.toFloat()
        when (isLevelUp(before, after)) {
            true -> {
                // 段位上升 积分-段位-积分
                expAnimation(start, 1f, object : AnimationListener {
                    override fun onFinish() {
                        levelAnimation(before, after, object : AnimationListener {
                            override fun onFinish() {
                                expAnimation(0f, end, object : AnimationListener {
                                    override fun onFinish() {
                                        listener.onFinish()
                                    }
                                })
                            }
                        })
                    }
                })
            }
            false -> {
                // 段位下降 积分-段位-积分
                expAnimation(start, 0f, object : AnimationListener {
                    override fun onFinish() {
                        levelAnimation(before, after, object : AnimationListener {
                            override fun onFinish() {
                                expAnimation(1f, end, object : AnimationListener {
                                    override fun onFinish() {
                                        listener.onFinish()
                                    }
                                })
                            }
                        })
                    }
                })
            }
            else -> {
                // 段位不变 积分
                expAnimation(start, end, object : AnimationListener {
                    override fun onFinish() {
                        listener.onFinish()
                    }
                })
            }
        }
    }

    // 积分动画(两个百分比)
    fun expAnimation(start: Float, end: Float, listener: AnimationListener) {
        if (start == end) {
            listener.onFinish()
        } else {
            // 做动画吧
        }
    }

    // 段位变化动画
    fun levelAnimation(before: ScoreStateModel, after: ScoreStateModel, listener: AnimationListener) {
        if (before.mainRanking == after.mainRanking && before.subRanking == after.subRanking) {
            listener.onFinish()
        } else {
            // 做动画吧
        }
    }

    // 段位是否上升 null即没变化 true上升  false下降
    fun isLevelUp(before: ScoreStateModel, after: ScoreStateModel): Boolean? {
        return if (after.mainRanking == before.mainRanking && after.subRanking == before.subRanking) {
            null
        } else {
            when {
                after.mainRanking > before.mainRanking -> true
                after.mainRanking < before.mainRanking -> false
                else -> after.subRanking > before.subRanking
            }
        }
    }


    private fun goMatchPage() {
        finish()
        ARouter.getInstance().build(RouterConstants.ACTIVITY_RACE_MATCH_ROOM)
                .navigation()
    }

    override fun useEventBus(): Boolean {
        return false
    }

    override fun destroy() {
        super.destroy()
        U.getSoundUtils().release(mTag)
    }
}
